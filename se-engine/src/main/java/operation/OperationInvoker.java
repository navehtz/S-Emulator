package operation;

public interface OperationInvoker {
    long invokeOperation(OperationView op, String userName, long... args);
    int getLastCycles();
}
