/*
 * Copyright (c) 2013-2016 Cinchapi Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cinchapi.common.process;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ProcessWatcher {

    /**
     * Return the pid of the current process.
     * 
     * @return pid.
     */
    public static String getProcessWatcherPid() {
        return ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
    }

    /**
     * Check if the process with the processId is running.
     * 
     * @param pid Id for the input process.
     * @return true if its running, false if not.
     */
    private static boolean isProcessRunning(int pid) {
        Process process = null;
        String spid = String.valueOf(pid);
        if(OPERATING_SYSTEM.indexOf("mac") >= 0
                || OPERATING_SYSTEM.indexOf("nux") >= 0
                || OPERATING_SYSTEM.indexOf("sunos") >= 0) {
            try {
                process = Runtime.getRuntime().exec("ps axo pid");
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    if(line.contains(spid)) {
                        return true;
                    }
                }
                return false;
            }
            catch (Exception e) {
                if(e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                else {
                    throw new RuntimeException(e);
                }
            }
        }
        else {
            throw new UnsupportedOperationException(
                    "This feature to check if a ProcessRunning is not supported for this platform : "
                            + OPERATING_SYSTEM);
        }
    }

    /**
     * Get the JVM current running platform.
     */
    private static String OPERATING_SYSTEM = System.getProperty("os.name")
            .toLowerCase();

    /**
     * The amount of time between each ping to determine if the host process is
     * running.
     */
    private static int PING_INTERVAL = 5000;

    private ScheduledExecutorService executor;

    private final Map<Integer, Future<?>> watching = new HashMap<>();

    public ProcessWatcher() {
        this.executor = Executors.newScheduledThreadPool(
                Runtime.getRuntime().availableProcessors());
        ((ScheduledThreadPoolExecutor) executor).setRemoveOnCancelPolicy(true);
    }

    public void shutdown() {
        executor.shutdownNow();
    }

    /**
     * Watch the process identified by the specified {@code pid} every
     * {@link PING_INTERVAL} milliseconds. Whenever the process is terminated,
     * execute {@link ProcessTerminationListener#onTermination()}.
     * 
     * @param pid the id of the Process to watch
     * @param listener the task to execute when the watched process is
     *            terminated
     */
    public void watch(int pid, ProcessTerminationListener listener) {
        Future<?> ticket = executor.scheduleAtFixedRate(() -> {
            if(!isProcessRunning(pid)) {
                listener.onTermination();
                Future<?> ticket0 = watching.get(pid);
                ticket0.cancel(true);
            }
        }, PING_INTERVAL, PING_INTERVAL, TimeUnit.MILLISECONDS);
        watching.put(pid, ticket);
    }
}
