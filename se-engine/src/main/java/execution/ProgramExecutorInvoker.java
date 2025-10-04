package execution;

import engine.ProgramRegistry;
import operation.OperationInvoker;
import operation.OperationView;
import variable.Variable;

import java.util.Objects;

public final class ProgramExecutorInvoker implements OperationInvoker {
    private final ProgramRegistry registry;
    private int lastCycles = 0;

    public ProgramExecutorInvoker(ProgramRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "ProgramRegistry is required");
    }

    @Override
    public long invokeOperation(OperationView op, long... args) {
        // Spin up an executor for the callee operation
        ProgramExecutorImpl exec = new ProgramExecutorImpl(op, registry);

        // ProgramExecutorImpl.run(int degree, Long... inputs) — box the args
        Long[] boxed = new Long[args.length];
        for (int i = 0; i < args.length; i++) boxed[i] = args[i];

        exec.run(0, boxed);                // degree = 0 (no extra expansion unless you choose otherwise)
        lastCycles = exec.getTotalCyclesOfProgram(); // capture total cycles of this invocation
        return exec.getVariableValue(Variable.RESULT);
    }

    @Override
    public int getLastCycles() {
        return lastCycles;
    }
}
