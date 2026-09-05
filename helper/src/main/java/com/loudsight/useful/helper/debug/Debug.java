package com.loudsight.useful.helper.debug;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

/**
 * Utility class for debug-related operations.
 *
 * @author munyengm
 */
public final class Debug {

    private Debug() {
    }

    private static final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    private static final List<String> inputArguments = runtimeMXBean.getInputArguments();

    /**
     * Checks if the current JVM process was started in debug mode.
     *
     * @return true if running in debugger, false otherwise
     * @see <a href="http://stackoverflow.com/questions/4944606/how-to-find-out-if-a-java-process-was-started-in-debugger?rq=1">Stack Overflow</a>
     */
    public static boolean isInDebugger() {
        return inputArguments.toString().contains("-agentlib:jdwp");
    }
}
