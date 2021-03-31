package io.opentracing.contrib.specialagent.rule.log4j;

import io.opentracing.contrib.specialagent.AgentRule;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.utility.JavaModule;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class LayoutAgentRule extends AgentRule {

    private static class PatternParserHandler {

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void exit(
                final @AgentRule.ClassName String className,
                final @Advice.Origin String origin,
                final @Advice.Argument(value = 0) String pattern,
                @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object patternParser) {
            if (isAllowed(className, origin)) {
                patternParser = LayoutAgentIntercept.onPatternParserExit(pattern);
            }
        }

    }


    @Override
    public AgentBuilder buildAgentChainedGlobal1(final AgentBuilder builder) {
        return builder
                .type(named("org.apache.log4j.PatternLayout"))
                .transform(new AgentBuilder.Transformer() {
                    @Override
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule) {
                        return builder.visit(advice(typeDescription).to(PatternParserHandler.class).on(named("createPatternParser")));
                    }
                });

    }


}
