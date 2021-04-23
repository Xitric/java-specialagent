package io.opentracing.contrib.specialagent.rule.log4j;

import io.opentracing.contrib.specialagent.AgentRunner;
import io.opentracing.mock.MockTracer;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;

import java.io.IOException;
import java.io.Writer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

@RunWith(AgentRunner.class)
public class LogTest {

    private static final Logger logger = Logger.getLogger(LogTest.class);

    @Spy
    private WriterAppender appender;

    @Mock
    private Writer writer;

    @Captor
    private ArgumentCaptor<String> stringCaptor;

    @Before
    public void before(final MockTracer tracer) {
        MockitoAnnotations.initMocks(this);
        System.setProperty(Log4jConstants.LOG_SPAN_IDS, "true");
        appender.setWriter(writer);
        appender.setLayout(new PatternLayout());
        logger.addAppender(appender);
        tracer.reset();
        tracer.buildSpan("testOperation").startActive(true);
    }

    @Test
    public void logTest() throws IOException {
        logger.info("message1");
        verify(writer).write(stringCaptor.capture());
        String message = stringCaptor.getValue();
        assertEquals(message, "message1 trace_id=1 span_id=2\n");
    }

}
