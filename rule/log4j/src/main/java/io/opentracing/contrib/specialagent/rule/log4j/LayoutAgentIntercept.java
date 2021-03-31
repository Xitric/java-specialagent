package io.opentracing.contrib.specialagent.rule.log4j;

import org.apache.log4j.helpers.PatternParser;

import java.text.MessageFormat;

public class LayoutAgentIntercept {

    public static Object onPatternParserExit(String pattern) {

        if (!Log4jUtil.shouldLogSpanIDs()){
            return new PatternParser(pattern);
        }

        String traceIdReference = "{" + Log4jConstants.TRACE_ID + "}";
        String spanIdReference = "{" + Log4jConstants.SPAN_ID + "}";

        if (pattern.contains(traceIdReference) || pattern.contains(spanIdReference)) {
            return new PatternParser(pattern);
        }

        String spanPattern = MessageFormat.format(" trace_id=%X{0} span_id=%X{1}", traceIdReference, spanIdReference);
        StringBuilder builder = new StringBuilder(pattern);
        int i = pattern.lastIndexOf("%n");
        builder.insert(i, spanPattern);
        pattern = builder.toString();

        return new PatternParser(pattern);
    }

}
