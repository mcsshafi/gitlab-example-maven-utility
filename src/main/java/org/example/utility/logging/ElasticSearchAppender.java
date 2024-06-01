package org.example.utility.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import com.fasterxml.jackson.core.JsonGenerator;
import com.internetitem.logback.elasticsearch.AbstractElasticsearchAppender;
import com.internetitem.logback.elasticsearch.AbstractElasticsearchPublisher;
import com.internetitem.logback.elasticsearch.config.ElasticsearchProperties;
import com.internetitem.logback.elasticsearch.config.HttpRequestHeaders;
import com.internetitem.logback.elasticsearch.config.Property;
import com.internetitem.logback.elasticsearch.config.Settings;
import com.internetitem.logback.elasticsearch.util.AbstractPropertyAndEncoder;
import com.internetitem.logback.elasticsearch.util.ClassicPropertyAndEncoder;
import com.internetitem.logback.elasticsearch.util.ErrorReporter;

import java.io.IOException;
import java.util.Map;

public class ElasticSearchAppender extends AbstractElasticsearchAppender<ILoggingEvent> {


    public ElasticSearchAppender() {
    }

    public ElasticSearchAppender(Settings settings) {
        super(settings);
    }

    @Override
    protected void appendInternal(ILoggingEvent eventObject) {

        String targetLogger = eventObject.getLoggerName();

        String loggerName = settings.getLoggerName();
        if (loggerName != null && loggerName.equals(targetLogger)) {
            return;
        }

        String errorLoggerName = settings.getErrorLoggerName();
        if (errorLoggerName != null && errorLoggerName.equals(targetLogger)) {
            return;
        }

        eventObject.prepareForDeferredProcessing();
        if (settings.isIncludeCallerData()) {
            eventObject.getCallerData();
        }

        publishEvent(eventObject);
    }

    protected ClassicElasticsearchPublisher buildElasticsearchPublisher() throws IOException {
        return new ClassicElasticsearchPublisher(getContext(), errorReporter, settings, elasticsearchProperties, headers);
    }

    public static class ClassicElasticsearchPublisher extends AbstractElasticsearchPublisher<ILoggingEvent> {
        public ClassicElasticsearchPublisher(Context context, ErrorReporter errorReporter, Settings settings, ElasticsearchProperties properties, HttpRequestHeaders headers) throws IOException {
            super(context, errorReporter, settings, properties, headers);
        }

        @Override
        protected AbstractPropertyAndEncoder<ILoggingEvent> buildPropertyAndEncoder(Context context, Property property) {
            return new ClassicPropertyAndEncoder(property, context);
        }

        @Override
        protected void serializeCommonFields(JsonGenerator gen, ILoggingEvent event) throws IOException {
            gen.writeObjectField("@timestamp", getTimestamp(event.getTimeStamp()));

            if (settings.isRawJsonMessage()) {
                gen.writeFieldName("message");
                gen.writeRawValue(event.getFormattedMessage());
            } else {
                String formattedMessage = event.getFormattedMessage();
                if (settings.getMaxMessageSize() > 0 && formattedMessage.length() > settings.getMaxMessageSize()) {
                    formattedMessage = formattedMessage.substring(0, settings.getMaxMessageSize()) + "..";
                }
                gen.writeObjectField("message", formattedMessage);
            }

            Map<String, String> mdc = event.getMDCPropertyMap();
            if (settings.isIncludeMdc()) {
                for (Map.Entry<String, String> entry : mdc.entrySet()) {
                    String key = entry.getKey();
                    if (key != LogUtils.JSON_DETAILS_FIELD_KEY) {
                        gen.writeObjectField(entry.getKey(), entry.getValue());
                    }
                }
            }

            if (mdc.containsKey(LogUtils.JSON_DETAILS_FIELD_KEY) == true) {
                String __json_object = mdc.get(LogUtils.JSON_DETAILS_FIELD_KEY);
                if (__json_object != null && __json_object.trim().isEmpty() == false) {
                    gen.writeFieldName("details");
                    gen.writeRawValue(__json_object);
                }
                mdc.remove(LogUtils.JSON_DETAILS_FIELD_KEY);
            }
        }
    }
}
