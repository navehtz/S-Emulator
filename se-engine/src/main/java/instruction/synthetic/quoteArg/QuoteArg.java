package instruction.synthetic.quoteArg;

import execution.ExecutionContext;

public interface QuoteArg {
    long eval(ExecutionContext context);
    String render();
}
