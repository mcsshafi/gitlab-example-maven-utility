package org.example.utility.logging;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.google.gson.Gson;

public class TagConverter extends ClassicConverter {
    public TagConverter() {

    }

    @Override
    public String convert(ILoggingEvent event) {
        Object tag = null;
        try {
            tag = getContext().getObject("tag");
        }
        catch(Exception ex) { }
        if(tag == null) {
            tag = new Object();
        }
        return new Gson().toJson(tag);
    }

    public class ExtendedPatternLayoutEncoder extends PatternLayoutEncoder {
        @Override
        public void start() {
            // put your converter
            PatternLayout.defaultConverterMap.put(
                    "tag", TagConverter.class.getName());
            super.start();
        }
    }
}
