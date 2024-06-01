package org.example.utility.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.contrib.jackson.JacksonJsonFormatter;
import ch.qos.logback.contrib.json.classic.JsonLayout;
import ch.qos.logback.core.CoreConstants;
import com.christ.utility.lib.vault.VaultUtils;
import com.google.gson.Gson;
import com.internetitem.logback.elasticsearch.ElasticsearchAppender;
import com.internetitem.logback.elasticsearch.config.*;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LogUtils {
    public final static String JSON_DETAILS_FIELD_KEY = "__json_object";

    private final static String Url;
    private final static String Name;
    private final static String Index;
    private final static String LoggerName;
    private final static String ErrorLoggerName;
    private final static int ConnectTimeout;
    private final static int MaxQueueSize;
    private final static int MaxRetries;
    private final static int ReadTimeout;
    private final static int SleepTime;
    private final static String MaxLevel;

    static {
        Url = VaultUtils.instance.get(VaultUtils.CONFIG_SECTION_APP_LOGGING_CONFIG, "logging.elasticsearch.url").getValue();
        Name = VaultUtils.instance.get(VaultUtils.CONFIG_SECTION_APP_LOGGING_CONFIG, "logging.name").getValue();
        Index = VaultUtils.instance.get(VaultUtils.CONFIG_SECTION_APP_LOGGING_CONFIG, "logging.index.pattern").getValue();
        MaxLevel = VaultUtils.instance.get(VaultUtils.CONFIG_SECTION_APP_LOGGING_CONFIG, "logging.max.level").getValue();
        SleepTime = VaultUtils.instance.get(VaultUtils.CONFIG_SECTION_APP_LOGGING_CONFIG, "logging.retry.sleep.time").getInt();
        MaxRetries = VaultUtils.instance.get(VaultUtils.CONFIG_SECTION_APP_LOGGING_CONFIG, "logging.max.retries").getInt();
        LoggerName = VaultUtils.instance.get(VaultUtils.CONFIG_SECTION_APP_LOGGING_CONFIG, "logging.logger.name").getValue();
        ReadTimeout = VaultUtils.instance.get(VaultUtils.CONFIG_SECTION_APP_LOGGING_CONFIG, "logging.read.timeout").getInt();
        MaxQueueSize = VaultUtils.instance.get(VaultUtils.CONFIG_SECTION_APP_LOGGING_CONFIG, "logging.max.queue.size").getInt();
        ConnectTimeout = VaultUtils.instance.get(VaultUtils.CONFIG_SECTION_APP_LOGGING_CONFIG, "logging.connect.timeout").getInt();
        ErrorLoggerName = VaultUtils.instance.get(VaultUtils.CONFIG_SECTION_APP_LOGGING_CONFIG, "logging.error.logger.name").getValue();
    }

    public static void runOnLogContext(ILogContext context) {
        runOnLogContext(context, null);
    }
    public static void runOnLogContext(ILogContext context, Object data) {
        if(data == null) {
            data = new Object();
        }
        MDC.put(JSON_DETAILS_FIELD_KEY, new Gson().toJson(data));
        context.run();
    }
    public static String getUniqueID() {
        return UUID.randomUUID().toString();
    }
    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }
    public static Logger getLogger(String name) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        setConversionRuleRegistry(context);

        JacksonJsonFormatter formatter = new JacksonJsonFormatter();
        formatter.setPrettyPrint(true);

        JsonLayout layout = new JsonLayout();
        layout.setContext(context);
        layout.setJsonFormatter(formatter);

        return getLogger(getAppender(context), name);
    }

    private static ElasticsearchAppender getAppender(LoggerContext context) {
        ElasticsearchAppender appender = new ElasticsearchAppender();
        try {
            appender.setUrl(LogUtils.Url);
        } catch (Exception e) {
        }
        appender.setName(LogUtils.Name);
        appender.setIndex(LogUtils.Index);
        appender.setType("_doc");
        appender.setLoggerName((LogUtils.LoggerName == null || LogUtils.LoggerName.trim().length() == 0) ? "es-logger" : LogUtils.LoggerName);
        appender.setErrorLoggerName((LogUtils.ErrorLoggerName == null || LogUtils.ErrorLoggerName.trim().length() == 0) ? "es-error-logger" : LogUtils.ErrorLoggerName);
        appender.setConnectTimeout(LogUtils.ConnectTimeout);
        appender.setErrorsToStderr(false);
        appender.setIncludeCallerData(false);
        appender.setLogsToStderr(false);
        appender.setMaxQueueSize(LogUtils.MaxQueueSize);
        appender.setMaxRetries(LogUtils.MaxRetries);
        appender.setReadTimeout(LogUtils.ReadTimeout);
        appender.setSleepTime(LogUtils.SleepTime);
        appender.setRawJsonMessage(false);
        appender.setIncludeMdc(false);
        appender.setAuthentication(new BasicAuthentication());
        appender.setContext(context);

        setHeaders(appender);
        setProperties(appender);

        appender.start();
        return appender;
    }
    private static void setProperties(ElasticsearchAppender appender) {
        ElasticsearchProperties properties = new ElasticsearchProperties();

        properties.addProperty(LogUtils.getProperty("severity", "%level"));
        properties.addProperty(LogUtils.getProperty("thread", "%thread"));
        properties.addProperty(LogUtils.getProperty("logger", "%logger"));

        appender.setProperties(properties);
    }
    private static Property getProperty(String key, String value) {
        Property property = new Property();
        property.setName(key);
        property.setValue(value);
        return property;
    }
    private static void setHeaders(ElasticsearchAppender appender) {
        HttpRequestHeaders headers = new HttpRequestHeaders();
        HttpRequestHeader header = new HttpRequestHeader();
        header.setName("Content-Type");
        header.setValue("application/json");
        headers.addHeader(header);
        appender.setHeaders(headers);
    }
    private static Logger getLogger(ElasticsearchAppender appender, String name) {
        Logger logger = (Logger) LoggerFactory.getLogger(name);
        logger.addAppender(appender);
        logger.setLevel(Level.toLevel(MaxLevel, Level.ALL));
        logger.setAdditive(false);
        return logger;
    }
    @SuppressWarnings("unchecked")
    private static void setConversionRuleRegistry(LoggerContext context) {
        Map<String, String> registry = null;
        try { registry = (Map<String, String>) context.getObject(CoreConstants.PATTERN_RULE_REGISTRY); }
        catch(Exception ex) { }
        if (registry == null) {
            registry = new HashMap<String, String>();
        }
        context.putObject(CoreConstants.PATTERN_RULE_REGISTRY, registry);
        registry.put("tag", "com.christ.utility.lib.TagConverter");
    }
}
