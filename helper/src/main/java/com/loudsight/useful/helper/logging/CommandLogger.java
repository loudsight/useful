package com.loudsight.useful.helper.logging;

/**
 * Interface for command execution logging.
 * Provides standard log level methods compatible with LoggingHelper.
 * 
 * Implementations route logs to command-specific appenders
 * (e.g., command.log) while maintaining full SLF4J integration.
 */
public interface CommandLogger {
    
    void logTrace(String log, Object... params);
    
    void logDebug(String log, Object... params);
    
    void logInfo(String log, Object... params);
    
    void logWarn(String log, Object... params);
    
    void logError(String log, Object... params);
    
    void logError(String log, Throwable t);
    
    /**
     * No-op implementation for use as default when command logging is disabled.
     */
    CommandLogger NO_OP = new CommandLogger() {
        @Override
        public void logTrace(String log, Object... params) {}
        
        @Override
        public void logDebug(String log, Object... params) {}
        
        @Override
        public void logInfo(String log, Object... params) {}
        
        @Override
        public void logWarn(String log, Object... params) {}
        
        @Override
        public void logError(String log, Object... params) {}
        
        @Override
        public void logError(String log, Throwable t) {}
    };
}
