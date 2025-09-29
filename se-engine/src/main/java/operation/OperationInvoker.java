package operation;

public interface OperationInvoker {
    long invokeOperation(Operation op, long... args);
}
