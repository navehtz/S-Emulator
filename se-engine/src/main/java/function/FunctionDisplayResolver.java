package function;

import engine.ProgramRegistry;
import instruction.Instruction;
import instruction.synthetic.QuoteInstruction;
import instruction.synthetic.quoteArg.QuoteArg;
import instruction.synthetic.quoteArg.CallArg;
import operation.OperationView;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public final class FunctionDisplayResolver {

    private FunctionDisplayResolver() {}

    public static void populateDisplayNames(Collection<OperationView> ops, ProgramRegistry registry) {
        Objects.requireNonNull(registry, "registry");
        if (ops == null) return;
        for (OperationView op : ops) {
            populateDisplayNames(op, registry);
        }
    }

    public static void populateDisplayNames(OperationView op, ProgramRegistry registry) {
        Objects.requireNonNull(registry, "registry");
        if (op == null) return;
        for (Instruction ins : op.getInstructionsList()) {
            if (ins instanceof QuoteInstruction qi) {
                setDisplayIfAbsent(qi::setDisplayName, lookupUserString(qi.getFunctionName(),registry));

                resolveArgSymbols(qi.getFunctionArguments(), registry);

            }
        }

    }

    private static void resolveArgSymbols(Iterable<? extends QuoteArg> args,
                                          ProgramRegistry registry) {
        if (args == null) return;
        for (QuoteArg arg : args) {
            if (arg instanceof CallArg call) {
                setDisplayIfAbsent(call::setDisplayName,
                        lookupUserString(call.getCallName(), registry));

                resolveArgSymbols(call.getArgs(), registry);
            }
        }
        // VarArg: nothing to do
    }

    private static Optional<String> lookupUserString(String functionName, ProgramRegistry registry) {
        if (functionName == null || functionName.isBlank()) return Optional.empty();

        Optional<Function> function = Optional.ofNullable((Function) registry.getProgramByName(functionName));
        if (function.isPresent()) {
            String userString = function.get().getUserString();
            if (userString != null && !userString.isBlank()) return Optional.of(userString);
            else return Optional.empty();
        }

        Optional<? extends OperationView> operationView = Optional.ofNullable(registry.getProgramByName(functionName));
        if (operationView.isPresent() && operationView.get() instanceof Function fun) {
            String userString = fun.getUserString();
            if (userString != null && !userString.isBlank()) return Optional.of(userString);
            else return Optional.empty();
        }

        return Optional.empty();
    }


    private static void setDisplayIfAbsent(Consumer<String> setter, Optional<String> userString) {
        userString.ifPresent(setter);
    }
}
