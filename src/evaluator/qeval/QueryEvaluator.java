package evaluator.qeval;

import parser.Expression;
import parser.ListExpression;
import parser.SymbolExpression;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueryEvaluator {
    private final List<Expression> assertions = new ArrayList<>();
    private final List<Expression> rules = new ArrayList<>();
    private final Map<String, QueryProcedure> procedureMap = new HashMap<>();

    private int idCounter = 0;

    public QueryEvaluator() {
        procedureMap.put("and", this::conjoin);
        procedureMap.put("or", this::disjoin);
        procedureMap.put("not", this::negate);
        // Add your procedure ...
    }

    private List<Frame> conjoin(ListExpression conjuncts, List<Frame> frames) {
        if (conjuncts.isNil()) {
            return frames;
        } else {
            return conjoin(conjuncts.getTail(), eval(conjuncts.getHead(), frames));
        }
    }

    private List<Frame> disjoin(ListExpression disjuncts, List<Frame> frames) {
        if (disjuncts.isNil()) {
            return Collections.emptyList();
        } else {
            return Stream.concat(
                    eval(disjuncts.getHead(), frames).stream(),
                    disjoin(disjuncts.getTail(), frames).stream()
            ).collect(Collectors.toList());
        }
    }

    private List<Frame> negate(ListExpression body, List<Frame> frames) {
        return frames.stream().flatMap(frame -> {
            if (eval(body.getHead(), List.of(frame)).isEmpty()) {
                return Stream.of(frame);
            } else {
                return Stream.empty();
            }
        }).collect(Collectors.toList());
    }

    public Expression instantiate(Expression pattern, Frame frame) {
        return pattern.visit((list) -> {
            ListExpression result = new ListExpression();
            result.addAll(list.stream().map((e) -> instantiate(e, frame)).collect(Collectors.toList()));
            return result;
        }, (symbol) -> {
            if (isVariable(symbol.getSymbol())) {
                if (frame.containsKey(symbol.getSymbol())) {
                    return instantiate(frame.get(symbol.getSymbol()), frame);
                } else {
                    return new SymbolExpression("?");
                }
            } else {
                return symbol;
            }
        });
    }

    // (assert! body) add an assertion
    // (rule! conclusion body) add a rule
    // (...) query
    public List<Frame> eval(Expression e) {
        return e.visit((list) -> {
            if (list.testHead("assert!"::equals)) {
                assertions.add(list.get(1));
                System.out.println("Add assertion!");
                return Collections.emptyList();
            } else if (list.testHead("rule!"::equals)) {
                rules.add(list.getTail());
                System.out.println("Add rule!");
                return Collections.emptyList();
            } else {
                return eval(list, List.of(new Frame()));
            }
        }, (symbol) -> eval(symbol, List.of(new Frame())));
    }

    private List<Frame> eval(Expression e, List<Frame> frames) {
        return e.visit((list) -> {
            if (list.testHead(procedureMap::containsKey)) { // qproc
                return procedureMap.get(list.getHead().getSymbol()).query(list.getTail(), frames);
            } else {
                return simpleQuery(list, frames);
            }
        }, (symbol) -> simpleQuery(symbol, frames));
    }

    private List<Frame> simpleQuery(Expression pattern, List<Frame> frames) {
        return frames.stream().flatMap(frame ->
                Stream.concat(findAssertions(pattern, frame).stream(),
                        applyRules(pattern, frame).stream())
        ).collect(Collectors.toList());
    }

    private List<Expression> fetchAssertions(Expression pattern, Frame frame) {
        // You can optimize it.
        return assertions;
    }

    private List<Frame> findAssertions(Expression pattern, Frame frame) {
        return fetchAssertions(pattern, frame).stream().flatMap((assertion) -> patternMatch(pattern, assertion, frame).stream()).collect(Collectors.toList());
    }

    private boolean isVariable(String name) {
        return name.startsWith("?");
    }

    // variable <-match-> constant
    private Optional<Frame> patternMatch(Expression pattern, Expression assertion, Frame frame) {
        return Optional.ofNullable(frame).flatMap((f) -> pattern.visit((p) -> assertion.visit((a) -> {
            if (p.isNil() && a.isNil()) {
                return Optional.of(f);
            } else if (!p.isNil() && !a.isNil()) {
                return patternMatch(p.getTail(), a.getTail(), f).flatMap((tail) -> patternMatch(p.getHead(), a.getHead(), tail));
            } else {
                return Optional.empty();
            }
        }, (a) -> Optional.empty()), (p) -> assertion.visit((a) -> {
            if (isVariable(p.getSymbol())) {
                return extendConstant(p.getSymbol(), a, f);
            } else {
                return Optional.empty();
            }
        }, (a) -> {
            if (p.getSymbol().equals(a.getSymbol())) {
                return Optional.of(f);
            } else if (isVariable(p.getSymbol())) {
                return extendConstant(p.getSymbol(), a, f);
            } else {
                return Optional.empty();
            }
        })));
    }

    // variable <-bind-> constant
    private Optional<Frame> extendConstant(String variable, Expression bind, Frame frame) {
        if (frame.containsKey(variable)) {
            return patternMatch(frame.get(variable), bind, frame);
        } else {
            Frame result = new Frame(frame);
            result.put(variable, bind);
            return Optional.of(result);
        }
    }

    private List<Expression> fetchRules(Expression pattern, Frame frame) {
        // You can also optimize it.
        return rules;
    }

    private List<Frame> applyRules(Expression pattern, Frame frame) {
        return fetchRules(pattern, frame).stream().flatMap((rule) -> applyRule(pattern, rule, frame).stream()).collect(Collectors.toList());
    }

    // variable <-match-> variable
    private Optional<Frame> unifyMatch(Expression pattern, Expression conclusion, Frame frame) {
        return Optional.ofNullable(frame).flatMap((f) -> pattern.visit((p) -> conclusion.visit((c) -> {
            if (p.isNil() && c.isNil()) {
                return Optional.of(f);
            } else if (!p.isNil() && !c.isNil()) {
                return unifyMatch(p.getTail(), c.getTail(), f).flatMap((tail) -> unifyMatch(p.getHead(), c.getHead(), tail));
            } else {
                return Optional.empty();
            }
        }, (c) -> {
            if (isVariable(c.getSymbol())) {
                return extendVariable(c.getSymbol(), p, f);
            } else {
                return Optional.empty();
            }
        }), (p) -> conclusion.visit((c) -> {
            if (isVariable(p.getSymbol())) {
                return extendVariable(p.getSymbol(), c, f);
            } else {
                return Optional.empty();
            }
        }, (c) -> {
            if (p.getSymbol().equals(c.getSymbol())) {
                return Optional.of(f);
            } else if (isVariable(p.getSymbol())) {
                return extendVariable(p.getSymbol(), c, f);
            } else if (isVariable(c.getSymbol())) {
                return extendVariable(c.getSymbol(), p, f);
            } else {
                return Optional.empty();
            }
        })));
    }

    private boolean dependsOn(String variable, Expression bind, Frame frame) {
        return bind.visit((list) -> list.stream().noneMatch((e) -> dependsOn(variable, e, frame)),
                (symbol) -> {
                    if (symbol.getSymbol().equals(variable)) return true;
                    if (frame.containsKey(symbol.getSymbol())) {
                        return dependsOn(variable, frame.get(symbol.getSymbol()), frame);
                    } else {
                        return false;
                    }
                });
    }

    // variable <-bind-> variable
    private Optional<Frame> extendVariable(String variable, Expression bind, Frame frame) {
        if (frame.containsKey(variable)) {
            return unifyMatch(frame.get(variable), bind, frame);
        } else {
            // depends? ?x -> (... ?x)
            if (dependsOn(variable, bind, frame)) {
                return Optional.empty();
            } else {
                // Why not bind directly?
                // I think 'unifyMatch(variable,frame.get(bind.getSymbol()))' is unnecessary.
                Frame result = new Frame(frame);
                result.put(variable, bind);
                return Optional.of(result);
            }
        }
    }

    private String renameVariable(String name, int id) {
        return "?" + id + name.substring(1);
    }

    private Expression shadowRule(Expression rule, int id) {
        return rule.visit((list) -> {
            ListExpression result = new ListExpression();
            result.addAll(list.stream().map((e) -> shadowRule(e, id)).collect(Collectors.toList()));
            return result;
        }, (symbol) -> {
            if (isVariable(symbol.getSymbol())) {
                return new SymbolExpression(renameVariable(symbol.getSymbol(), id));
            } else {
                return symbol;
            }
        });
    }

    private List<Frame> applyRule(Expression pattern, Expression rule, Frame frame) {
        idCounter++;
        Expression shadow = shadowRule(rule, idCounter);
        return unifyMatch(pattern, shadow.getHead(), frame).stream().flatMap((f) -> {
            if (shadow.getTail().isNil()) {
                return Stream.of(f);
            } else {
                return eval(shadow.getTail().getHead(), List.of(f)).stream();
            }
        }).collect(Collectors.toList());
    }
}
