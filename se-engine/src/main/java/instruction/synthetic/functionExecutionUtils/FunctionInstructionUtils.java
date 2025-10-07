package instruction.synthetic.functionExecutionUtils;

import instruction.synthetic.quoteArg.QuoteArg;
import instruction.synthetic.quoteArg.CallArg;
import instruction.synthetic.quoteArg.VarArg;
import variable.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class FunctionInstructionUtils {

    public static List<QuoteArg> mapFunctionArgumentsToNewList(List<QuoteArg> arguments, Map<Variable, Variable> variableMapping, boolean toMapFlag) {
        List<QuoteArg> mappedArguments = new ArrayList<>();

        for (QuoteArg argument : arguments) {
            switch (argument) {
                case VarArg varArg -> {
                    Variable originalVariable = varArg.getVariable();
                    Variable newVariable;

                    if (toMapFlag) {
                        newVariable = variableMapping.get(originalVariable);
                        if (newVariable == null) {
                            throw new IllegalArgumentException(
                                    "In FunctionInstructionUtils: Variable not found in mapping: " + originalVariable.getRepresentation()
                            );
                        }
                    } else {
                        newVariable = originalVariable;
                    }


                    mappedArguments.add(new VarArg(newVariable));
                }

                case CallArg callArg -> {
                    List<QuoteArg> sameInnerArguments =    // Not mapped ! stayed the same
                            mapFunctionArgumentsToNewList(callArg.getArgs(), variableMapping, toMapFlag);

                    mappedArguments.add(new CallArg(callArg.getCallName(), sameInnerArguments));
                }

                default -> throw new IllegalStateException(
                        "In FunctionInstructionUtils: Unsupported QuoteArg type: " + argument.getClass()
                );
            }
        }
        return mappedArguments;
    }
}
