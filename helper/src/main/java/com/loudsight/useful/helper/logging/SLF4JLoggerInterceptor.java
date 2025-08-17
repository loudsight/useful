package com.loudsight.useful.helper.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.LoggerFactory;
import org.slf4j.ILoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class SLF4JLoggerInterceptor {
    
    private static final LoggingHelper LOG = LoggingHelper.wrap(SLF4JLoggerInterceptor.class);
    
    private static final Map<String, InterceptingAppender> interceptors = new ConcurrentHashMap<>();
    private static boolean globalInterceptionEnabled = false;
    
    /**
     * Enable global interception for ALL SLF4J loggers
     */
    public static void enableGlobalLogInterception() {
        ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
        
        if (!(loggerFactory instanceof LoggerContext)) {
            throw new IllegalStateException("SLF4J is not bound to Logback. " +
                "Current binding: " + loggerFactory.getClass().getName());
        }

        LoggerContext context = (LoggerContext) loggerFactory;

        // Create and configure the intercepting appender
        InterceptingAppender interceptor = new InterceptingAppender();
        interceptor.setName("GLOBAL_INTERCEPTOR");
        interceptor.setContext(context);
        interceptor.start();
        
        // Add to root logger to catch ALL logging
        Logger rootLogger = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(interceptor);
        
        globalInterceptionEnabled = true;
        LOG.logInfo("Global SLF4J log interception enabled");
    }
    
    /**
     * Enable interception for a specific logger by name
     */
    public static void enableLoggerInterception(String loggerName) {
        ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
        
        if (!(loggerFactory instanceof LoggerContext)) {
            throw new IllegalStateException("SLF4J is not bound to Logback");
        }
        
        LoggerContext context = (LoggerContext) loggerFactory;
        
        // Create interceptor for this specific logger
        InterceptingAppender interceptor = new InterceptingAppender();
        interceptor.setName("INTERCEPTOR_" + loggerName);
        interceptor.setContext(context);
        interceptor.start();
        
        // Get the specific logger and add our interceptor
        Logger logger = context.getLogger(loggerName);
        logger.addAppender(interceptor);
        
        interceptors.put(loggerName, interceptor);
        LOG.logInfo("Interception enabled for logger: " + loggerName);
    }
    
    /**
     * Enable interception for a specific logger by class
     */
    public static void enableLoggerInterception(Class<?> clazz) {
        enableLoggerInterception(clazz.getName());
    }
    
    /**
     * Disable interception for a specific logger
     */
    public static void disableLoggerInterception(String loggerName) {
        InterceptingAppender interceptor = interceptors.remove(loggerName);
        if (interceptor != null) {
            ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
            LoggerContext context = (LoggerContext) loggerFactory;
            Logger logger = context.getLogger(loggerName);
            
            logger.detachAppender(interceptor);
            interceptor.stop();
            LOG.logInfo("Interception disabled for logger: " + loggerName);
        }
    }
    
    /**
     * Disable global interception
     */
    public static void disableGlobalLogInterception() {
        if (globalInterceptionEnabled) {
            ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
            LoggerContext context = (LoggerContext) loggerFactory;
            Logger rootLogger = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
            
            // Remove the global interceptor
            rootLogger.detachAppender("GLOBAL_INTERCEPTOR");
            globalInterceptionEnabled = false;
            LOG.logInfo("Global SLF4J log interception disabled");
        }
    }
    
    /**
     * Get all logger names currently registered in the LoggerContext
     */
    public static void listAllLoggers() {
        ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
        LoggerContext context = (LoggerContext) loggerFactory;
        
        LOG.logInfo("Registered loggers:");
        for (Logger logger : context.getLoggerList()) {
            LOG.logInfo("  - " + logger.getName() + " (Level: " + logger.getLevel() + ")");
        }
    }
    
    /**
     * Intercept all loggers for classes in a specific package
     */
    public static void enablePackageInterception(String packageName) {
        ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
        LoggerContext context = (LoggerContext) loggerFactory;
        
        // Get all existing loggers that match the package
        for (Logger logger : context.getLoggerList()) {
            if (logger.getName().startsWith(packageName)) {
                enableLoggerInterception(logger.getName());
            }
        }
        
        LOG.logInfo("Interception enabled for package: " + packageName);
    }
    
    /**
     * Custom appender that intercepts log events
     */
    public static class InterceptingAppender extends AppenderBase<ILoggingEvent> {
        
        @Override
        protected void append(ILoggingEvent event) {
            interceptLogEvent(event);
        }
        
        private void interceptLogEvent(ILoggingEvent event) {
            // Extract comprehensive information from the event
            String loggerName = event.getLoggerName();
            String level = event.getLevel().toString();
            String message = event.getFormattedMessage();
            String threadName = event.getThreadName();
            long timestamp = event.getTimeStamp();
            
            // Get caller information if available
            StackTraceElement[] callerData = event.getCallerData();
            String callerInfo = "Unknown";
            if (callerData != null && callerData.length > 0) {
                StackTraceElement caller = callerData[0];
                callerInfo = caller.getClassName() + "." + caller.getMethodName() + ":" + caller.getLineNumber();
            }
            
            // Your custom interception logic here
            LOG.logInfo(
                    "[INTERCEPTED] %tF %<tT.%<tL [%s] %-5s %s - %s (Called from: %s)%n",
                timestamp, threadName, level, loggerName, message, callerInfo
            );
            
            // Handle exceptions
            if (event.getThrowableProxy() != null) {
                LOG.logInfo("  Exception: " + event.getThrowableProxy().getMessage());
            }
            
            // You can add custom logic here:
            // - Store in database
            // - Send to monitoring system
            // - Filter sensitive information
            // - Transform log messages
            // - Count log events by level/logger
            
            handleCustomInterceptionLogic(event);
        }
        
        private void handleCustomInterceptionLogic(ILoggingEvent event) {
            // Example: Count errors per logger
            if ("ERROR".equals(event.getLevel().toString())) {
                // Increment error counter for this logger
                LOG.logInfo("  >>> ERROR detected in " + event.getLoggerName());
            }
            
            // Example: Detect specific patterns
            if (event.getFormattedMessage().contains("SECURITY")) {
                LOG.logInfo("  >>> SECURITY-related log detected!");
            }
        }
    }
    
    // Example usage and testing
    public static void main(String[] args) {
        // Enable global interception
        enableGlobalLogInterception();
        
        // Test with different loggers
        LoggingHelper logger1 = LoggingHelper.wrap("com.example.TestClass1");
        LoggingHelper logger2 = LoggingHelper.wrap("com.example.TestClass2");
        LoggingHelper logger3 = LoggingHelper.wrap(SLF4JLoggerInterceptor.class);
        
        // These will all be intercepted
        logger1.logInfo("This is an info message from TestClass1");
        logger2.logError("This is an error message from TestClass2");
        logger3.logDebug("This is a debug message from SLF4JLoggerInterceptor");
        logger1.logWarn("Warning with exception", new RuntimeException("Test exception"));
        
        // List all loggers
        LOG.logInfo("\n" + "=".repeat(50));
        listAllLoggers();
        
        // Test specific logger interception
        LOG.logInfo("\n" + "=".repeat(50));
        disableGlobalLogInterception();
        enableLoggerInterception("com.example.TestClass1");
        
        logger1.logInfo("This will be intercepted (specific logger)");
        logger2.logInfo("This will NOT be intercepted");
        
        // Test package interception
        LOG.logInfo("\n" + "=".repeat(50));
        enablePackageInterception("com.example");
        logger2.logInfo("Now this will also be intercepted (package level)");
    }
}