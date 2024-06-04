package query;

import parser.*;

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

    public Expression instantiate(Expression pattern, Frame frame) {
        return pattern.accept(new ExpressionVisitor<>() {
            @Override
            public Expression visit(SymbolExpression expression) {
                if (isVariable(expression.getSymbol())) {
                    if (frame.containsKey(expression.getSymbol())) {
                        return instantiate(frame.get(expression.getSymbol()), frame);
                    } else {
                        return new SymbolExpression("?");
                    }
                } else {
                    return expression;
                }
            }

            @Override
            public Expression visit(PairExpression expression) {
                return new PairExpression(instantiate(expression.getHead(), frame), instantiate(expression.getTail(), frame));
            }

            @Override
            public Expression visit(NilExpression expression) {
                return expression;
            }
        });
    }

    private List<Frame> conjoin(ListExpression conjuncts, List<Frame> frames) {
        if (conjuncts.isNil()) {
            return frames;
        } else {
            PairExpression pair = conjuncts.accept(DefaultExpressionVisitor.expectPairExpression);
            return conjoin(pair.getTail().accept(DefaultExpressionVisitor.expectListExpression), query(pair.getHead(), frames));
        }
    }

    private List<Frame> disjoin(ListExpression disjuncts, List<Frame> frames) {
        if (disjuncts.isNil()) {
            return Collections.emptyList();
        } else {
            PairExpression pair = disjuncts.accept(DefaultExpressionVisitor.expectPairExpression);
            return Stream.concat(
                    query(pair.getHead(), frames).stream(),
                    disjoin(pair.getTail().accept(DefaultExpressionVisitor.expectListExpression), frames).stream()
            ).collect(Collectors.toList());
        }
    }

    private List<Frame> negate(ListExpression negation, List<Frame> frames) {
        return frames.stream().flatMap(frame -> {
            PairExpression pair = negation.accept(DefaultExpressionVisitor.expectPairExpression);
            if (query(pair.getHead(), List.of(frame)).isEmpty()) { // Filter
                return Stream.of(frame);
            } else {
                return Stream.empty();
            }
        }).collect(Collectors.toList());
    }

    // (assert! body) add an assertion
    // (rule! conclusion body) add a rule
    // (...) query
    public List<Frame> query(Expression e) {
        return e.accept(new ExpressionVisitor<>() {
            @Override
            public List<Frame> visit(SymbolExpression expression) {
                return query(expression, List.of(new Frame()));
            }

            @Override
            public List<Frame> visit(PairExpression expression) {
                if (expression.testHead("assert!"::equals)) {
                    assertions.add(expression.get(1));
                    System.out.println("Add assertion!");
                    return Collections.emptyList();
                } else if (expression.testHead("rule!"::equals)) {
                    rules.add(expression.getTail());
                    System.out.println("Add rule!");
                    return Collections.emptyList();
                } else {
                    return query(expression, List.of(new Frame()));
                }
            }

            @Override
            public List<Frame> visit(NilExpression expression) {
                throw new IllegalStateException("Query a NilExpression.");
            }
        });
    }

    private List<Frame> query(Expression e, List<Frame> frames) {
        return e.accept(new ExpressionVisitor<>() {
            @Override
            public List<Frame> visit(SymbolExpression expression) {
                return simpleQuery(expression, frames);
            }

            @Override
            public List<Frame> visit(PairExpression expression) {
                if (expression.testHead(procedureMap::containsKey)) { // Query procedure
                    return procedureMap.get(expression.getHead().accept(DefaultExpressionVisitor.expectSymbolExpression).getSymbol()).
                            query(expression.getTail().accept(DefaultExpressionVisitor.expectListExpression), frames);
                } else {
                    return simpleQuery(expression, frames);
                }
            }

            @Override
            public List<Frame> visit(NilExpression expression) {
                throw new IllegalStateException("Query a NilExpression.");
            }
        });
    }

    private List<Frame> simpleQuery(Expression pattern, List<Frame> frames) {
        return frames.stream().flatMap(frame ->
                Stream.concat(findAssertions(pattern, frame).stream(), // Apply assertions
                        applyRules(pattern, frame).stream()) // Apply rules
        ).collect(Collectors.toList());
    }

    private List<Expression> fetchAssertions(Expression pattern, Frame frame) {
        // You can optimize it.
        return assertions;
    }

    private List<Frame> findAssertions(Expression pattern, Frame frame) {
        return fetchAssertions(pattern, frame).stream().
                flatMap((assertion) -> patternMatch(pattern, assertion, frame).stream()).
                collect(Collectors.toList());
    }

    private boolean isVariable(String name) {
        return name.startsWith("?");
    }

    // variable <-match-> constant
    private Optional<Frame> patternMatch(Expression pattern, Expression assertion, Frame frame) {
        return Optional.ofNullable(frame).flatMap(f -> pattern.accept(new ExpressionVisitor<Optional<Frame>>() {
            @Override
            public Optional<Frame> visit(SymbolExpression patternExpression) {
                return assertion.accept(new ExpressionVisitor<>() {
                    @Override
                    public Optional<Frame> visit(SymbolExpression assertionExpression) {
                        if (patternExpression.getSymbol().equals(assertionExpression.getSymbol())) {
                            return Optional.of(f);
                        } else if (isVariable(patternExpression.getSymbol())) {
                            return extendConstant(patternExpression.getSymbol(), assertionExpression, f);
                        } else {
                            return Optional.empty();
                        }
                    }

                    @Override
                    public Optional<Frame> visit(PairExpression assertionExpression) {
                        if (isVariable(patternExpression.getSymbol())) {
                            return extendConstant(patternExpression.getSymbol(), assertionExpression, f);
                        } else {
                            return Optional.empty();
                        }
                    }

                    @Override
                    public Optional<Frame> visit(NilExpression assertionExpression) {
                        if (isVariable(patternExpression.getSymbol())) {
                            return extendConstant(patternExpression.getSymbol(), assertionExpression, f);
                        } else {
                            return Optional.empty();
                        }
                    }
                });
            }

            @Override
            public Optional<Frame> visit(PairExpression patternExpression) {
                return assertion.accept(new ExpressionVisitor<>() {
                    @Override
                    public Optional<Frame> visit(SymbolExpression assertionExpression) {
                        return Optional.empty();
                    }

                    @Override
                    public Optional<Frame> visit(PairExpression assertionExpression) {
                        return patternMatch(patternExpression.getHead(), assertionExpression.getHead(), f).
                                flatMap(fTail -> patternMatch(patternExpression.getTail(), assertionExpression.getTail(), fTail));
                    }

                    @Override
                    public Optional<Frame> visit(NilExpression assertionExpression) {
                        return Optional.empty();
                    }
                });
            }

            @Override
            public Optional<Frame> visit(NilExpression patternExpression) {
                return assertion.accept(new ExpressionVisitor<>() {
                    @Override
                    public Optional<Frame> visit(SymbolExpression assertionExpression) {
                        return Optional.empty();
                    }

                    @Override
                    public Optional<Frame> visit(PairExpression assertionExpression) {
                        return Optional.empty();
                    }

                    @Override
                    public Optional<Frame> visit(NilExpression assertionExpression) {
                        return Optional.of(f);
                    }
                });
            }
        }));
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
        return fetchRules(pattern, frame).stream().
                flatMap(rule -> applyRule(pattern, rule, frame).stream()).
                collect(Collectors.toList());
    }

    private List<Frame> applyRule(Expression pattern, Expression rule, Frame frame) {
        idCounter++;
        PairExpression shadow = shadowRule(rule, idCounter).accept(DefaultExpressionVisitor.expectPairExpression);
        return unifyMatch(pattern, shadow.getHead(), frame).stream().flatMap(f -> {
            if (shadow.getTail().accept(DefaultExpressionVisitor.expectListExpression).isNil()) { // Non-body
                return Stream.of(f);
            } else {
                return query(shadow.get(1), List.of(f)).stream(); // Body
            }
        }).collect(Collectors.toList());
    }

    // variable <-match-> variable
    private Optional<Frame> unifyMatch(Expression pattern, Expression conclusion, Frame frame) {
        return Optional.ofNullable(frame).flatMap(f -> pattern.accept(new ExpressionVisitor<Optional<Frame>>() {
            @Override
            public Optional<Frame> visit(SymbolExpression patternExpression) {
                return conclusion.accept(new ExpressionVisitor<>() {
                    @Override
                    public Optional<Frame> visit(SymbolExpression conclusionExpression) {
                        if (patternExpression.getSymbol().equals(conclusionExpression.getSymbol())) {
                            return Optional.of(f);
                        } else if (isVariable(patternExpression.getSymbol())) {
                            return extendVariable(patternExpression.getSymbol(), conclusionExpression, f);
                        } else if (isVariable(conclusionExpression.getSymbol())) {
                            return extendVariable(conclusionExpression.getSymbol(), patternExpression, f);
                        } else {
                            return Optional.empty();
                        }
                    }

                    @Override
                    public Optional<Frame> visit(PairExpression conclusionExpression) {
                        if (isVariable(patternExpression.getSymbol())) {
                            return extendVariable(patternExpression.getSymbol(), conclusionExpression, f);
                        } else {
                            return Optional.empty();
                        }
                    }

                    @Override
                    public Optional<Frame> visit(NilExpression conclusionExpression) {
                        if (isVariable(patternExpression.getSymbol())) {
                            return extendVariable(patternExpression.getSymbol(), conclusionExpression, f);
                        } else {
                            return Optional.empty();
                        }
                    }
                });
            }

            @Override
            public Optional<Frame> visit(PairExpression patternExpression) {
                return conclusion.accept(new ExpressionVisitor<>() {
                    @Override
                    public Optional<Frame> visit(SymbolExpression conclusionExpression) {
                        if (isVariable(conclusionExpression.getSymbol())) {
                            return extendVariable(conclusionExpression.getSymbol(), patternExpression, f);
                        } else {
                            return Optional.empty();
                        }
                    }

                    @Override
                    public Optional<Frame> visit(PairExpression conclusionExpression) {
                        return unifyMatch(patternExpression.getHead(), conclusionExpression.getHead(), f).
                                flatMap(fTail -> unifyMatch(patternExpression.getTail(), conclusionExpression.getTail(), fTail));
                    }

                    @Override
                    public Optional<Frame> visit(NilExpression conclusionExpression) {
                        return Optional.empty();
                    }
                });
            }

            @Override
            public Optional<Frame> visit(NilExpression patternExpression) {
                return conclusion.accept(new ExpressionVisitor<>() {
                    @Override
                    public Optional<Frame> visit(SymbolExpression conclusionExpression) {
                        if (isVariable(conclusionExpression.getSymbol())) {
                            return extendVariable(conclusionExpression.getSymbol(), patternExpression, f);
                        } else {
                            return Optional.empty();
                        }
                    }

                    @Override
                    public Optional<Frame> visit(PairExpression conclusionExpression) {
                        return Optional.empty();
                    }

                    @Override
                    public Optional<Frame> visit(NilExpression conclusionExpression) {
                        return Optional.of(f);
                    }
                });
            }
        }));
    }

    // variable <-bind-> variable
    private Optional<Frame> extendVariable(String variable, Expression bind, Frame frame) {
        if (frame.containsKey(variable)) {
            return unifyMatch(frame.get(variable), bind, frame);
        } else {
            if (dependsOn(variable, bind, frame)) { // depends? ?x -> (... ?x)
                return Optional.empty();
            } else {
                // Why not bind directly?
                Frame result = new Frame(frame);
                result.put(variable, bind);
                return Optional.of(result);
            }
        }
    }

    private boolean dependsOn(String variable, Expression bind, Frame frame) {
        return bind.accept(new ExpressionVisitor<>() {
            @Override
            public Boolean visit(SymbolExpression expression) {
                if (expression.getSymbol().equals(variable)) {
                    return true;
                } else if (frame.containsKey(expression.getSymbol())) {
                    return dependsOn(variable, frame.get(expression.getSymbol()), frame);
                } else {
                    return false;
                }
            }

            @Override
            public Boolean visit(PairExpression expression) {
                return !dependsOn(variable, expression.getHead(), frame) && !dependsOn(variable, expression.getTail(), frame);
            }

            @Override
            public Boolean visit(NilExpression expression) {
                return false;
            }
        });
    }

    private String renameVariable(String name, int id) {
        return name + "-" + id;
    }

    private Expression shadowRule(Expression rule, int id) {
        return rule.accept(new ExpressionVisitor<>() {
            @Override
            public Expression visit(SymbolExpression expression) {
                if (isVariable(expression.getSymbol())) { // Rename it
                    return new SymbolExpression(renameVariable(expression.getSymbol(), id));
                } else {
                    return expression;
                }
            }

            @Override
            public Expression visit(PairExpression expression) {
                return new PairExpression(shadowRule(expression.getHead(), id), shadowRule(expression.getTail(), id));
            }

            @Override
            public Expression visit(NilExpression expression) {
                return expression;
            }
        });
    }
}
