package com.loudsight.useful.helper.debug;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

/**
 *
 * @author munyengm
 * 
 */
public class Debug {

    /**
     * <a href="http://stackoverflow.com/questions/4944606/how-to-find-out-if-a-java-process-was-started-in-debugger?rq=1">...</a>
     *
     * @return
     */
    private static final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    private static final List<String> inputArguments = runtimeMXBean.getInputArguments();

    public static boolean isInDebugger() {
        return inputArguments.toString().contains("-agentlib:jdwp");
    }
}
