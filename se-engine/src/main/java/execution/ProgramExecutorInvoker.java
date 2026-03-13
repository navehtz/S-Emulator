package execution;

import architecture.ArchitectureType;
import engine.ProgramRegistry;
import operation.OperationInvoker;
import operation.OperationView;
import users.UserManager;
import variable.Variable;

import java.util.Objects;

public final class ProgramExecutorInvoker implements OperationInvoker {
    private final ProgramRegistry registry;
    private final ArchitectureType architectureTypeSelected;
    private final UserManager userManager;
    private int lastCycles = 0;

    public ProgramExecutorInvoker(ProgramRegistry registry, ArchitectureType architectureTypeSelected, UserManager userManager) {
        this.registry = Objects.requireNonNull(registry, "ProgramRegistry is required");
        this.architectureTypeSelected = Objects.requireNonNull(architectureTypeSelected, "ArchitectureType is required");
        this.userManager = Objects.requireNonNull(userManager, "UserManager is required");
    }

    @Override
    public long invokeOperation(OperationView op, String userName, long... args) {
        // Spin up an executor for the callee operation
        ProgramExecutorImpl programExecutor = new ProgramExecutorImpl(op, architectureTypeSelected, registry, userName, userManager);

        // ProgramExecutorImpl.run(int degree, Long... inputs) — box the args
        Long[] boxed = new Long[args.length];
        for (int i = 0; i < args.length; i++) boxed[i] = args[i];

        programExecutor.run(userName, 0, boxed);       // degree = 0 (no extra expansion unless you choose otherwise)
        lastCycles = programExecutor.getTotalCyclesOfProgram(); // capture total cycles of this invocation
        return programExecutor.getVariableValue(Variable.RESULT);
    }

    @Override
    public int getLastCycles() {
        return lastCycles;
    }
}
