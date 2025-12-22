package com.loudsight.useful.helper.logging;

/**
 * Interface for structured logging with support for multiple implementations.
 * Defines standard log level methods mirrored across all logging implementations.
 * 
 * The default implementation is DefaultLoggingHelper.
 * Specialized implementations (e.g., CommandLogger) can provide alternative routing.
 */
public interface LoggingHelper {
    
    void logTrace(String log, Object... params);
    
    void logDebug(String log, Object... params);
    
    void logInfo(String log, Object... params);
    
    void logWarn(String log, Object... params);
    
    void logError(String log, Object... params);
    
    void logError(String log, Throwable t);
    
    /**
     * Create a LoggingHelper for the given class (uses DefaultLoggingHelper).
     * @param className the fully qualified class name
     * @return a new LoggingHelper instance
     */
    static LoggingHelper wrap(String className) {
        return DefaultLoggingHelper.wrap(className);
    }
    
    /**
     * Create a LoggingHelper for the given class (uses DefaultLoggingHelper).
     * @param clazz the class to log from
     * @return a new LoggingHelper instance
     */
    static LoggingHelper wrap(Class<?> clazz) {
        return DefaultLoggingHelper.wrap(clazz);
    }
    
    /**
     * Create a LoggingHelper with a named logger category (uses DefaultLoggingHelper).
     * Useful for routing logs to specific appenders (e.g., "commands" for command.log).
     * 
     * @param loggerName the SLF4J logger name
     * @param calledFrom the class context for log message decoration
     * @return a new LoggingHelper instance
     */
    static LoggingHelper wrapByName(String loggerName, Class<?> calledFrom) {
        return DefaultLoggingHelper.wrapByName(loggerName, calledFrom);
    }
}

