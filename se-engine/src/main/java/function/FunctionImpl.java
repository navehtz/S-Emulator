package function;

import label.Label;
import operation.Operation;
import variable.Variable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class FunctionImpl extends Operation implements Function {
    private final String userString;
    private final List<Variable> parameters;
    private final Variable returnVariable; // nullable
    private final Label resolvedEntry;

    private FunctionImpl(Builder b) {
        super(b);
        this.userString = b.userString;
        this.parameters = List.copyOf(b.parameters);
        this.returnVariable = b.returnVariable;
        this.resolvedEntry = (this.entry != null) ? this.entry : firstLabeledInstruction();
    }

    @Override
    public List<Variable> parameters() {
        return parameters;
    }

    @Override
    public Optional<Variable> returnVariable() {
        return Optional.ofNullable(returnVariable);
    }

    @Override
    public String getUserString() {
        return userString;
    }

    @Override
    public Optional<Label> getEntry() {
        return Optional.of(resolvedEntry);
    }

    public static final class Builder extends Operation.Builder<Builder, FunctionImpl> {
        private final List<Variable> parameters = new ArrayList<>();
        private Variable returnVariable;
        private String userString;

        @Override
        protected Builder self() {
            return this;
        }

        public Builder withParameters(Collection<? extends Variable> params) {
            parameters.clear();
            if (params != null) parameters.addAll(params);
            return this;
        }

        public Builder addParameter(Variable v) {
            if (v != null) parameters.add(v);
            return this;
        }

        public Builder returns(Variable v) {
            this.returnVariable = v;
            return this;
        }

        public Builder withUserString(String userString) {
            this.userString = userString;
            return this;
        }

        @Override
        public FunctionImpl build() {
            return new FunctionImpl(this);
        }
    }
}
