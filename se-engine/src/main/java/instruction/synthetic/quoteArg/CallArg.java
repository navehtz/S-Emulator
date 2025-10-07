package instruction.synthetic.quoteArg;

import execution.ExecutionContext;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class CallArg implements QuoteArg, Serializable {
    private final String callName;
    private final List<QuoteArg> args;
    private String displayName;

    public CallArg(String name, List<QuoteArg> args) {
        this.callName = name;
        this.args = args;
    }

    @Override
    public long eval(ExecutionContext context) {

        long[] argsValues = new long[args.size()];

        for(int i = 0; i < args.size(); i++) {
            argsValues[i] = args.get(i).eval(context);
        }

        return context.invokeOperation(callName, argsValues);
    }

    @Override
    public String render() {
        String shownName = (displayName != null) ? displayName : callName;
        String itemsInside = args.stream().map(QuoteArg::render).collect(Collectors.joining(","));
        return "(" + shownName + (itemsInside.isEmpty() ? "" : "," + itemsInside) + ")";
    }

    public String getCallName() {
        return callName;
    }

    public List<QuoteArg> getArgs() {
        return args;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = (displayName == null || displayName.isBlank()) ? null : displayName;
    }
}
