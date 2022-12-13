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

    public static boolean isInDebugger() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        List<String> inputArguments = runtimeMXBean.getInputArguments();

        return inputArguments.toString().contains("-agentlib:jdwp");
    }
}
