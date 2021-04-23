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

import javax.faces.event.PhaseId;

public class PhaseExecutorAgentIntercept {

	static final Tracer tracer = GlobalTracer.get();

	public static void onRestoreViewEnter(final Object thiz, final Object c) {
		onExecuteEnter(thiz, PhaseId.RESTORE_VIEW, c);
	}

	public static void onApplyRequestValuesEnter(final Object thiz, final Object c) {
		onExecuteEnter(thiz, PhaseId.APPLY_REQUEST_VALUES, c);
	}

	public static void onProcessValidationsEnter(final Object thiz, final Object c) {
		onExecuteEnter(thiz, PhaseId.PROCESS_VALIDATIONS, c);
	}

	public static void onUpdateModelValuesEnter(final Object thiz, final Object c) {
		onExecuteEnter(thiz, PhaseId.UPDATE_MODEL_VALUES, c);
	}

	public static void onInvokeApplicationEnter(final Object thiz, final Object c) {
		onExecuteEnter(thiz, PhaseId.INVOKE_APPLICATION, c);
	}

	public static void onRenderResponseEnter(final Object thiz, final Object c) {
		onExecuteEnter(thiz, PhaseId.RENDER_RESPONSE, c);
	}

	private static void onExecuteEnter(final Object thiz, final PhaseId phase, final Object c) {
		final Span span = tracer.buildSpan(phase.toString())
				.withTag(Tags.COMPONENT.getKey(), MyFacesConstants.COMPONENT_NAME)
				.withTag("code.namespace", thiz.getClass().getName())
				.start();

		final Scope scope = tracer.activateSpan(span);
		LocalSpanContext.set(MyFacesConstants.KEY_LIFECYCLE, span, scope);
	}

	public static void onExecuteExit(final Throwable thrown) {
		final LocalSpanContext context = LocalSpanContext.get(MyFacesConstants.KEY_LIFECYCLE);
		if (context == null) {
			return;
		}

		if (thrown != null) {
			OpenTracingApiUtil.setErrorTag(context.getSpan(), thrown);
		}

		context.closeAndFinish();
	}
}
