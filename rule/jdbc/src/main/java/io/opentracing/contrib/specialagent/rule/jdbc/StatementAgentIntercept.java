/* Copyright 2018 The OpenTracing Authors
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

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.specialagent.LocalSpanContext;
import io.opentracing.contrib.specialagent.OpenTracingApiUtil;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;

public class StatementAgentIntercept {

	protected static final Tracer tracer = GlobalTracer.get();

	/**
	 * Attempt to determine a name for this span based on the sql string that is executed. For regular statements, this
	 * name can be extracted from the sql string argument directly. For prepared and batch queries, we instead attempt
	 * to extract the name from inside the statement object, and in the worst case fall back to a default name.
	 *
	 * @param thiz The instrumented statement object
	 * @param sql The sql string that was used in this call, may be {@code null} for prepared and batch queries
	 * @return The best effort proposal for naming this span
	 */
	public static String getSpanName(final Object thiz, final Object sql) {
		if (sql instanceof String) {
			return sql.toString();
		}

		String candidate = thiz.toString();
		if (candidate.startsWith(thiz.getClass().getName())) {
			return "Unknown query";
		} else {
			return candidate;
		}
	}

	/**
	 * To avoid making multiple nested spans as the various execute methods are calling each other, we create only one
	 * span per thread for each database query. This is achieved by checking if the current thread already has an active
	 * span associated with a database query.
	 *
	 * @param thiz The instrumented statement object
	 * @param sql The sql string that was used in this call, may be {@code null} for prepared and batch queries
	 */
	public static void onExecuteEnter(final Object thiz, final Object sql) {
		LocalSpanContext context = LocalSpanContext.get(JdbcConstants.COMPONENT_NAME);
		// If a span has already been activated in a different execute method, we suppress nested creations in this
		// thread
		if (context != null) {
			context.increment();
			return;
		}

		final Span span = tracer.buildSpan(getSpanName(thiz, sql))
				.withTag(Tags.COMPONENT.getKey(), JdbcConstants.COMPONENT_NAME)
				.withTag("class", thiz.getClass().getName())
				.withTag(Tags.SPAN_KIND, Tags.SPAN_KIND_CLIENT)
				.start();

		final Scope scope = tracer.activateSpan(span);
		LocalSpanContext.set(JdbcConstants.COMPONENT_NAME, span, scope);
	}

	public static void onExecuteExit(final Throwable thrown) {
		final LocalSpanContext context = LocalSpanContext.get(JdbcConstants.COMPONENT_NAME);
		if (context == null) {
			return;
		}

		// Check if we are still unwrapping nested calls before actually closing the span
		if (context.decrementAndGet() > 0) {
			return;
		}

		// At this point, it is time to close the span
		if (thrown != null) {
			OpenTracingApiUtil.setErrorTag(context.getSpan(), thrown);
		}

		context.closeAndFinish();
	}
}
