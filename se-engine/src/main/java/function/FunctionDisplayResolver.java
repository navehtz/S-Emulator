package function;

import engine.ProgramRegistry;
import instruction.Instruction;
import instruction.synthetic.QuoteInstruction;
import operation.Operation;
import instruction.synthetic.quoteArg.QuoteArg;
import instruction.synthetic.quoteArg.CallArg;

import java.util.Collection;

public final class FunctionDisplayResolver {

    private FunctionDisplayResolver() {}

    public static void populateDisplayNames(Collection<Operation> ops, ProgramRegistry registry) {
        for (Operation op : ops) {
            for (Instruction ins : op.getInstructionsList()) {
                if (ins instanceof QuoteInstruction qi) {
                    setDisplayFromRegistry(qi.getFunctionName(), registry, qi::setDisplayName);

                    for (QuoteArg quoteArg : qi.getFunctionArguments()) {
                        resolveArgSymbols(quoteArg, registry);
                    }
                }
            }
        }
    }

    private static void resolveArgSymbols(QuoteArg arg,
                                          engine.ProgramRegistry registry) {
        if (arg instanceof CallArg call) {
            setDisplayFromRegistry(call.getCallName(), registry, call::setDisplayName);
            for (QuoteArg child : call.getArgs()) {
                resolveArgSymbols(child, registry); // recurse
            }
        }
        // VarArg: nothing to do
    }

    private static void setDisplayFromRegistry(String functionName,
                                               ProgramRegistry registry,
                                               java.util.function.Consumer<String> setter) {
        try {
            Operation callee = registry.getByName(functionName);
            if (callee instanceof FunctionImpl function) {
                String userString = function.getUserString();
                if (userString != null && !userString.isBlank()) setter.accept(userString);
            }
        } catch (Exception ignored) {}
    }
}
