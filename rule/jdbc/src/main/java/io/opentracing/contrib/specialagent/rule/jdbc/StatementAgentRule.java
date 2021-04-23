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

package io.opentracing.contrib.specialagent.rule.jdbc;

import io.opentracing.contrib.specialagent.AgentRule;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class StatementAgentRule extends AgentRule {

	@Advice.OnMethodEnter
	public static void enter(
			final @ClassName String className,
			final @Advice.Origin String origin,
			final @Advice.This Object thiz,
			final @Advice.Argument(value = 0, optional = true) Object sql) {
		if (isAllowed(className, origin)) {
			StatementAgentIntercept.onExecuteEnter(thiz, sql);
		}
	}

	@Advice.OnMethodExit(onThrowable = Throwable.class)
	public static void exit(
			final @ClassName String className,
			final @Advice.Origin String origin,
			final @Advice.Thrown Throwable thrown) {
		if (isAllowed(className, origin)) {
			StatementAgentIntercept.onExecuteExit(thrown);
		}
	}

	@Override
	public AgentBuilder buildAgentChainedGlobal1(final AgentBuilder builder) {
		return builder
				.type(hasSuperType(named("java.sql.Statement")).and(not(isInterface())))
				.transform(new AgentBuilder.Transformer() {
					@Override
					public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule) {
						return builder
								.visit(advice(typeDescription).to(StatementAgentRule.class).on(nameStartsWith("execute")));
					}
				});
	}
}
