/* Copyright 2021 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentracing.contrib.specialagent.rule.myfaces12;

import io.opentracing.contrib.specialagent.AgentRule;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class PhaseExecutorAgentRule extends AgentRule {

	@Override
	public AgentBuilder buildAgentChainedGlobal1(final AgentBuilder builder) {
		return builder
				.type(named("org.apache.myfaces.lifecycle.RestoreViewExecutor"))
				.transform(new Transformer() {
					@Override
					public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule) {
						return builder
								.visit(advice(typeDescription).to(RestoreViewExecutorAdvice.class).on(named("execute")));
					}
				})
				.type(named("org.apache.myfaces.lifecycle.ApplyRequestValuesExecutor"))
				.transform(new Transformer() {
					@Override
					public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule) {
						return builder
								.visit(advice(typeDescription).to(ApplyRequestValuesExecutorAdvice.class).on(named("execute")));
					}
				})
				.type(named("org.apache.myfaces.lifecycle.ProcessValidationsExecutor"))
				.transform(new Transformer() {
					@Override
					public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule) {
						return builder
								.visit(advice(typeDescription).to(ProcessValidationsExecutorAdvice.class).on(named("execute")));
					}
				})
				.type(named("org.apache.myfaces.lifecycle.UpdateModelValuesExecutor"))
				.transform(new Transformer() {
					@Override
					public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule) {
						return builder
								.visit(advice(typeDescription).to(UpdateModelValuesExecutorAdvice.class).on(named("execute")));
					}
				})
				.type(named("org.apache.myfaces.lifecycle.InvokeApplicationExecutor"))
				.transform(new Transformer() {
					@Override
					public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule) {
						return builder
								.visit(advice(typeDescription).to(InvokeApplicationExecutorAdvice.class).on(named("execute")));
					}
				})
				.type(named("org.apache.myfaces.lifecycle.RenderResponseExecutor"))
				.transform(new Transformer() {
					@Override
					public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule) {
						return builder
								.visit(advice(typeDescription).to(RenderResponseExecutorAdvice.class).on(named("execute")));
					}
				});
	}

	public static class RestoreViewExecutorAdvice {
		@Advice.OnMethodEnter
		public static void enter(
				final @ClassName String className,
				final @Advice.Origin String origin,
				final @Advice.This Object thiz,
				final @Advice.Argument(value = 0) Object context) {
			if (isAllowed(className, origin)) {
				PhaseExecutorAgentIntercept.onRestoreViewEnter(thiz, context);
			}
		}

		@Advice.OnMethodExit(onThrowable = Throwable.class)
		public static void exit(
				final @ClassName String className,
				final @Advice.Origin String origin,
				final @Advice.Thrown Throwable thrown) {
			if (isAllowed(className, origin)) {
				PhaseExecutorAgentIntercept.onExecuteExit(thrown);
			}
		}
	}

	public static class ApplyRequestValuesExecutorAdvice {
		@Advice.OnMethodEnter
		public static void enter(
				final @ClassName String className,
				final @Advice.Origin String origin,
				final @Advice.This Object thiz,
				final @Advice.Argument(value = 0) Object context) {
			if (isAllowed(className, origin)) {
				PhaseExecutorAgentIntercept.onApplyRequestValuesEnter(thiz, context);
			}
		}

		@Advice.OnMethodExit(onThrowable = Throwable.class)
		public static void exit(
				final @ClassName String className,
				final @Advice.Origin String origin,
				final @Advice.Thrown Throwable thrown) {
			if (isAllowed(className, origin)) {
				PhaseExecutorAgentIntercept.onExecuteExit(thrown);
			}
		}
	}

	public static class ProcessValidationsExecutorAdvice {
		@Advice.OnMethodEnter
		public static void enter(
				final @ClassName String className,
				final @Advice.Origin String origin,
				final @Advice.This Object thiz,
				final @Advice.Argument(value = 0) Object context) {
			if (isAllowed(className, origin)) {
				PhaseExecutorAgentIntercept.onProcessValidationsEnter(thiz, context);
			}
		}

		@Advice.OnMethodExit(onThrowable = Throwable.class)
		public static void exit(
				final @ClassName String className,
				final @Advice.Origin String origin,
				final @Advice.Thrown Throwable thrown) {
			if (isAllowed(className, origin)) {
				PhaseExecutorAgentIntercept.onExecuteExit(thrown);
			}
		}
	}

	public static class UpdateModelValuesExecutorAdvice {
		@Advice.OnMethodEnter
		public static void enter(
				final @ClassName String className,
				final @Advice.Origin String origin,
				final @Advice.This Object thiz,
				final @Advice.Argument(value = 0) Object context) {
			if (isAllowed(className, origin)) {
				PhaseExecutorAgentIntercept.onUpdateModelValuesEnter(thiz, context);
			}
		}

		@Advice.OnMethodExit(onThrowable = Throwable.class)
		public static void exit(
				final @ClassName String className,
				final @Advice.Origin String origin,
				final @Advice.Thrown Throwable thrown) {
			if (isAllowed(className, origin)) {
				PhaseExecutorAgentIntercept.onExecuteExit(thrown);
			}
		}
	}

	public static class InvokeApplicationExecutorAdvice {
		@Advice.OnMethodEnter
		public static void enter(
				final @ClassName String className,
				final @Advice.Origin String origin,
				final @Advice.This Object thiz,
				final @Advice.Argument(value = 0) Object context) {
			if (isAllowed(className, origin)) {
				PhaseExecutorAgentIntercept.onInvokeApplicationEnter(thiz, context);
			}
		}

		@Advice.OnMethodExit(onThrowable = Throwable.class)
		public static void exit(
				final @ClassName String className,
				final @Advice.Origin String origin,
				final @Advice.Thrown Throwable thrown) {
			if (isAllowed(className, origin)) {
				PhaseExecutorAgentIntercept.onExecuteExit(thrown);
			}
		}
	}

	public static class RenderResponseExecutorAdvice {
		@Advice.OnMethodEnter
		public static void enter(
				final @ClassName String className,
				final @Advice.Origin String origin,
				final @Advice.This Object thiz,
				final @Advice.Argument(value = 0) Object context) {
			if (isAllowed(className, origin)) {
				PhaseExecutorAgentIntercept.onRenderResponseEnter(thiz, context);
			}
		}

		@Advice.OnMethodExit(onThrowable = Throwable.class)
		public static void exit(
				final @ClassName String className,
				final @Advice.Origin String origin,
				final @Advice.Thrown Throwable thrown) {
			if (isAllowed(className, origin)) {
				PhaseExecutorAgentIntercept.onExecuteExit(thrown);
			}
		}
	}
}
