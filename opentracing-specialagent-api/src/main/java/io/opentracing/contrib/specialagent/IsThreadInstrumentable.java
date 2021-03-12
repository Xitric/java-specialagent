package io.opentracing.contrib.specialagent;

public class IsThreadInstrumentable extends InheritableThreadLocal<Boolean> {
    @Override
    protected Boolean childValue(Boolean parentValue) {
        if (parentValue == null) {
            parentValue = Boolean.TRUE;
        }

        if (!parentValue || Adapter.tracerClassLoader == null)
        return parentValue;

        return !AgentRuleUtil.isFromClassLoader(AgentRuleUtil.getExecutionStack(), Adapter.tracerClassLoader);
    }

    @Override
    public Boolean get() {
        Boolean state = super.get();
        if (state == null) {
            set(state = Boolean.TRUE);
        }

        return state;
    }
}
