package io.opentracing.contrib.specialagent.rule.log4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Log4jUtil {

    // Regex used as a workaround for the bug found in version 0.8.0 of the OTEL API.
    private static final Pattern spanIdPattern = Pattern.compile("^SpanId\\{spanId=(.*?)\\}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern traceIdPattern = Pattern.compile("^TraceId\\{traceId=(.*?)\\}$", Pattern.CASE_INSENSITIVE);

    public static String extractSpanId(String span) {
        if (span.startsWith("Span")) {
            final Matcher matcher = spanIdPattern.matcher(span);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return span;
    }

    public static String extractTraceId(String trace) {
        if (trace.startsWith("Trace")) {
            final Matcher matcher = traceIdPattern.matcher(trace);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return trace;
    }

    public static boolean shouldLogSpanIDs() {
        String logProperty = System.getProperty(Log4jConstants.LOG_SPAN_IDS);

        if (logProperty == null) {
            return false;
        }

        return Log4jUtil.parseProperty(logProperty);
    }

    private static boolean parseProperty(String str) {
        if (!(str == null || str.isEmpty())) {
            if (str.equalsIgnoreCase("true")) {
                return true;
            }
            if (str.equalsIgnoreCase("false")) {
                return false;
            }
        }
        throw new IllegalArgumentException("Bad input \"" + str + "\". Valid inputs are 'true' 'false'");
    }

}
