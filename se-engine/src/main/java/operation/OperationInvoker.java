package operation;

public interface OperationInvoker {
    long invokeOperation(OperationView op, long... args);
    int getLastCycles();
}
