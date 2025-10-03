package instruction;

import label.Label;
import variable.Variable;

import java.util.Map;

public final class RemapUtils {
    private RemapUtils() {}

    public static Variable mapVar(Map<Variable, Variable> varMap, Variable v) {
        return (v == null) ? null : varMap.getOrDefault(v, v);
    }
    public static Label mapLbl(Map<Label, Label> labelMap, Label l) {
        return (l == null) ? null : labelMap.getOrDefault(l, l);
    }
}
