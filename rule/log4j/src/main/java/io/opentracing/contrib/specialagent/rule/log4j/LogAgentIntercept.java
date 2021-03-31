package io.opentracing.contrib.specialagent.rule.log4j;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.apache.log4j.MDC;

import java.util.Hashtable;

public class LogAgentIntercept {

    static final Tracer tracer = GlobalTracer.get();

    public static Hashtable onMdcCopyEnter() {
        final Span span = tracer.activeSpan();

        Hashtable mdc = MDC.getContext();

        if (span == null) {
            return mdc == null ? new Hashtable() : (Hashtable) mdc.clone();
        }

        SpanContext spanContext = span.context();
        String spanId = spanContext.toSpanId();
        String traceId = spanContext.toTraceId();

        Hashtable newMdc = new Hashtable();
        if (mdc != null) {
            newMdc.putAll(mdc);
        }
        if (!newMdc.contains(spanId) && !newMdc.contains(traceId)) {
            newMdc.put(Log4jConstants.TRACE_ID, traceId);
            newMdc.put(Log4jConstants.SPAN_ID, spanId);
        }

        return newMdc;
    }


    public static Object onMdcExit(String key, Object value) {
        final Span span = tracer.activeSpan();

        if (span == null) {
            return value;
        }

        SpanContext spanContext = span.context();

        if (spanContext == null || ((key.equals(Log4jConstants.TRACE_ID) || key.equals(Log4jConstants.SPAN_ID)) && value != null)) {
            return value;
        }

        if (key.equals(Log4jConstants.TRACE_ID)) {
            String traceString = spanContext.toTraceId();
            return Log4jUtil.extractTraceId(traceString);
        }

        if (key.equals(Log4jConstants.SPAN_ID)) {
            String spanString = spanContext.toSpanId();
            return Log4jUtil.extractSpanId(spanString);
        }

        return value;
    }


}
