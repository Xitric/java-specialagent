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

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.specialagent.LocalSpanContext;
import io.opentracing.contrib.specialagent.OpenTracingApiUtil;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;

import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.faces.component.ActionSource2;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

public class ActionListenerAgentIntercept {

	static final Tracer tracer = GlobalTracer.get();

	private static Tracer.SpanBuilder makeSpan(final Object thiz, final ActionEvent event) {
		Tracer.SpanBuilder builder = null;

		ELContext context = FacesContext.getCurrentInstance().getELContext();
		if (context != null) {
			if (event != null && event.getComponent() instanceof ActionSource2) {
				ActionSource2 actionSource = (ActionSource2) event.getComponent();
				MethodExpression expression = actionSource.getActionExpression();
				if (expression != null) {
					String expString = expression.getExpressionString();
					builder = tracer.buildSpan(expString)
							.withTag("code.function", expString);
				}
			}
		}

		if (builder == null) {
			builder = tracer.buildSpan("processAction")
					.withTag("code.function", "processAction()");
		}

		if (event != null) {
			builder.withTag("code.ui_component", event.getComponent().getClass().getName())
					.withTag("code.ui_component_id", event.getComponent().getId());
		}

		return builder.withTag(Tags.COMPONENT.getKey(), MyFacesConstants.COMPONENT_NAME)
				.withTag("code.namespace", thiz.getClass().getName());
	}

	public static void onProcessActionEnter(final Object thiz, final Object evt) {
		ActionEvent event = (ActionEvent) evt;

		LocalSpanContext context = LocalSpanContext.get(MyFacesConstants.KEY_ACTION_LISTENER);
		// If a span has already been activated in a different execute method, we suppress nested creations in this
		// thread
		if (context != null) {
			context.increment();
			context.getSpan().setTag("code.namespace", thiz.getClass().getName());
			return;
		}

		final Span span = makeSpan(thiz, event).start();

		final Scope scope = tracer.activateSpan(span);
		LocalSpanContext.set(MyFacesConstants.KEY_ACTION_LISTENER, span, scope);
	}

	public static void onProcessActionExit(final Throwable thrown) {
		final LocalSpanContext context = LocalSpanContext.get(MyFacesConstants.KEY_ACTION_LISTENER);
		if (context == null) {
			return;
		}

		// Check if we are still unwrapping nested calls before actually closing the span
		if (context.decrementAndGet() > 0) {
			return;
		}

		if (thrown != null) {
			OpenTracingApiUtil.setErrorTag(context.getSpan(), thrown);
		}

		context.closeAndFinish();
	}
}
