package com.loudsight.useful.helper.logging;

import com.loudsight.useful.helper.JvmClassHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of LoggingHelper using SLF4J.
 * Provides structured logging with support for calling class context.
 */
public class DefaultLoggingHelper implements LoggingHelper {

    private final Logger logger;
    private final Class<?> calledFrom;

    private DefaultLoggingHelper(Logger logger, Class<?> calledFrom) {
        this.logger = logger;
        this.calledFrom = calledFrom;
    }

    public static DefaultLoggingHelper wrap(String className) {
        return wrap(JvmClassHelper.classForName(className));
    }

    public static DefaultLoggingHelper wrap(Class<?> clazz) {
        var logger = LoggerFactory.getLogger(clazz);
        return new DefaultLoggingHelper(logger, clazz);
    }

    /**
     * Create a DefaultLoggingHelper with a named logger category.
     * Useful for routing logs to specific appenders (e.g., "commands" for command.log).
     * 
     * @param loggerName the SLF4J logger name
     * @param calledFrom the class context for log message decoration
     * @return a new DefaultLoggingHelper instance
     */
    public static DefaultLoggingHelper wrapByName(String loggerName, Class<?> calledFrom) {
        var logger = LoggerFactory.getLogger(loggerName);
        return new DefaultLoggingHelper(logger, calledFrom);
    }

    @Override
    public void logTrace(String log, Object... params) {
        if (logger.isTraceEnabled()) {
            String msg = formatMessage(log);
            logger.trace(msg, params);
        }
    }

    @Override
    public void logDebug(String log, Object... params) {
        if (logger.isDebugEnabled()) {
            String msg = formatMessage(log);
            logger.debug(msg, params);
        }
    }

    @Override
    public void logInfo(String log, Object... params) {
        if (logger.isInfoEnabled()) {
            String msg = formatMessage(log);
            logger.info(msg, params);
        }
    }

    @Override
    public void logWarn(String log, Object... params) {
        if (logger.isWarnEnabled()) {
            String msg = formatMessage(log);
            logger.warn(msg, params);
        }
    }

    @Override
    public void logError(String log, Object... params) {
        if (logger.isErrorEnabled()) {
            String msg = formatMessage(log);
            logger.error(msg, params);
        }
    }

    @Override
    public void logError(String log, Throwable t) {
        if (logger.isErrorEnabled()) {
            String msg = formatMessage(log);
            logger.error(msg, t);
        }
    }

    /**
     * Format message with calling class context if available.
     * @param log the log message
     * @return formatted message with [ClassName] prefix if calledFrom is set
     */
    private String formatMessage(String log) {
        if (calledFrom != null) {
            return String.format("[%s] %s", calledFrom.getSimpleName(), log);
        }
        return log;
    }
}
