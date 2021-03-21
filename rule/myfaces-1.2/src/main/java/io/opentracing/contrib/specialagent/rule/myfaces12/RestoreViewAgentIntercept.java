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

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

public class RestoreViewAgentIntercept {

	static final Tracer tracer = GlobalTracer.get();

	public static String getSpanName() {
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		return "Restore view " + (request == null ? "unknown" : request.getRequestURI());
	}

	public static void onExecuteEnter(final Object thiz, final Object c) {
		final FacesContext context = (FacesContext) c;
		final Span span = tracer.buildSpan(getSpanName())
				.withTag(Tags.COMPONENT.getKey(), MyFacesConstants.COMPONENT_NAME)
				.withTag("class", thiz.getClass().getName())
				.withTag(Tags.SPAN_KIND, Tags.SPAN_KIND_SERVER)
				.start();

		final Scope scope = tracer.activateSpan(span);
		LocalSpanContext.set(MyFacesConstants.COMPONENT_NAME, span, scope);
	}

	public static void onExecuteExit(final Throwable thrown) {
		final LocalSpanContext context = LocalSpanContext.get(MyFacesConstants.COMPONENT_NAME);
		if (context == null) {
			return;
		}

		if (thrown != null) {
			OpenTracingApiUtil.setErrorTag(context.getSpan(), thrown);
		}

		context.closeAndFinish();
	}
}
