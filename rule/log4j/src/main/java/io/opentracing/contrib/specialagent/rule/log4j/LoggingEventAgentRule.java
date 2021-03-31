package io.opentracing.contrib.specialagent.rule.log4j;

import io.opentracing.contrib.specialagent.AgentRule;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

import java.util.Hashtable;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class LoggingEventAgentRule extends AgentRule {

    public static class MdcCopyHandler {
        @Advice.OnMethodEnter
        public static void enter(
                final @ClassName String className,
                final @Advice.Origin String origin,
                @Advice.FieldValue(value = "mdcCopy", readOnly = false) Hashtable mdcCopy,
                @Advice.FieldValue(value = "mdcCopyLookupRequired", readOnly = false) boolean mdcCopyLookupRequired) {
            if (isAllowed(className, origin) && mdcCopyLookupRequired) {
                mdcCopyLookupRequired = false;
                mdcCopy = LoggingEventAgentIntercept.onMdcCopyEnter();
            }
        }

    }

    public static class MdcHandler {

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void exit(
                final @ClassName String className,
                final @Advice.Origin String origin,
                final @Advice.Argument(0) String key,
                @Advice.Return(readOnly = false) Object value) {
            if (isAllowed(className, origin)) {
                value = LoggingEventAgentIntercept.onMdcExit(key, value);
            }
        }
    }


    @Override
    public AgentBuilder buildAgentChainedGlobal2(final AgentBuilder builder) {
        return builder
                .type(named("org.apache.log4j.spi.LoggingEvent"))
                .transform(new Transformer() {
                    @Override
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule) {
                        return builder
                                .visit(advice(typeDescription).to(MdcCopyHandler.class).on(named("getMDCCopy")))
                                .visit(advice(typeDescription).to(MdcHandler.class).on(named("getMDC")));
                    }
                });
    }


}