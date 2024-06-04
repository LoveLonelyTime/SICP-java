package compiler;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InstSequence {
    private final Set<Reg> needs = new HashSet<>();
    private final Set<Reg> modifies = new HashSet<>();
    private final List<String> insts = new ArrayList<>();

    public List<String> arrange(){
        return List.copyOf(insts);
    }

    public static InstSequence attach(String label) {
        return attach(label, new InstSequence());
    }

    public static InstSequence attach(String label, InstSequence instSequence) {
        instSequence.insts.add(0, label);
        return instSequence;
    }

    public static InstSequence append(InstSequence... instSequences){
        InstSequence instSequence = new InstSequence();
        for (InstSequence item : instSequences){
            instSequence = append(instSequence, item);
        }
        return instSequence;
    }

    public static InstSequence append(InstSequence i1, InstSequence i2) {
        InstSequence instSequence = new InstSequence();
        instSequence.modifies.addAll(i1.modifies);
        instSequence.modifies.addAll(i2.modifies);
        instSequence.needs.addAll(i1.needs);
        instSequence.needs.addAll(i2.needs.
                stream().
                filter(Predicate.not(i1.modifies::contains)).
                collect(Collectors.toSet())
        );
        instSequence.insts.addAll(i1.insts);
        instSequence.insts.addAll(i2.insts);
        return instSequence;
    }

    public static InstSequence preserving(InstSequence i1, InstSequence i2, Reg... preserved) {
        if (preserved.length == 0) {
            return append(i1, i2);
        } else {
            if (i1.modifies.contains(preserved[0]) && i2.needs.contains(preserved[0])) {
                InstSequence wrapped = append(
                        new InstSequenceBuilder().
                                setType(InstType.SAVE).
                                setReg(preserved[0]).
                                build(),
                        i1,
                        new InstSequenceBuilder().
                                setType(InstType.RESTORE).
                                setReg(preserved[0]).
                                build()
                );
                wrapped.modifies.remove(preserved[0]);
                return preserving(wrapped, i2, Arrays.copyOfRange(preserved, 1, preserved.length));
            } else {
                return preserving(i1, i2, Arrays.copyOfRange(preserved, 1, preserved.length));
            }
        }
    }

    public static InstSequence tackOn(InstSequence i1, InstSequence i2){
        InstSequence instSequence = new InstSequence();
        instSequence.needs.addAll(i1.needs);
        instSequence.modifies.addAll(i1.modifies);
        instSequence.insts.addAll(i1.insts);
        instSequence.insts.addAll(i2.insts);
        return instSequence;
    }

    public static InstSequence parallel(InstSequence i1, InstSequence i2){
        InstSequence instSequence = new InstSequence();
        instSequence.needs.addAll(i1.needs);
        instSequence.needs.addAll(i2.needs);
        instSequence.modifies.addAll(i1.modifies);
        instSequence.modifies.addAll(i2.modifies);
        instSequence.insts.addAll(i1.insts);
        instSequence.insts.addAll(i2.insts);
        return instSequence;
    }

    public static InstSequence call(InstSequence i1){
        InstSequence instSequence = new InstSequence();
        instSequence.needs.addAll(i1.needs);
        instSequence.modifies.addAll(i1.modifies);
        instSequence.insts.addAll(i1.insts);
        instSequence.modifies.addAll(Set.of(Reg.values()));
        return instSequence;
    }

    public static class InstSequenceBuilder {
        private InstType type;
        private Reg reg;
        private final List<Parameter> parameters = new ArrayList<>();

        public InstSequenceBuilder setType(InstType type) {
            this.type = type;
            return this;
        }

        public InstSequenceBuilder setReg(Reg reg) {
            this.reg = reg;
            return this;
        }

        public InstSequenceBuilder addParameter(Parameter parameter) {
            parameters.add(parameter);
            return this;
        }

        public InstSequence build() {
            InstSequence instSequence = new InstSequence();
            switch (type.regProperty) {
                case READ:
                    instSequence.needs.add(reg);
                    break;
                case WRITE:
                    instSequence.modifies.add(reg);
                    break;
            }
            instSequence.needs.addAll(parameters.
                    stream().
                    map(Parameter::getNeeds).
                    flatMap(Collection::stream).
                    collect(Collectors.toList())
            );

            // Type
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("(");
            stringBuilder.append(type.name);
            stringBuilder.append(" ");
            // Reg
            if (!Objects.isNull(reg)) {
                stringBuilder.append(reg.name);
                stringBuilder.append(" ");
            }
            // Parameter
            stringBuilder.append(parameters.stream().map(Parameter::toInst).collect(Collectors.joining(" ")));
            stringBuilder.append(")");
            instSequence.insts.add(stringBuilder.toString());
            return instSequence;
        }
    }
}
