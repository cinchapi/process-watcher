package com.cinchapi.common.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;


public class HostProcessWatcher {

    /**
     * The amount of time between each ping to determine if the host process is
     * running.
     */
    private static int HOST_PROCESS_PING_INTERVAL = 5000;

    /**
     * Get the JVM current running platform.
     */
    private static String platform = System.getProperty("os.name")
            .toLowerCase();

    /**
     * It initiates a thread that phones host process and checks
     * its status for every 5 seconds. Terminates plugin, if host process is
     * down.
     * 
     * @param pid
     */
    public static void watch() {
        String hostPid = System.getProperty("ConcourseServerPid");
        String currentPid = getCurrentPid();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if(hostPid != null) {
                            boolean status = isProcessRunning(hostPid);
                            if(!status) {
                                kill(currentPid);
                            }
                            Thread.sleep(HOST_PROCESS_PING_INTERVAL);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }).start();

    }

    /**
     * Return the pid of the current process.
     * 
     * @return pid.
     */
    private static String getCurrentPid() {
        return ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
    }

    /**
     * Check if the process with the processId is running.
     * 
     * @param pid Id for the input process.
     * @return true if its running, false if not.
     */
    private static boolean isProcessRunning(String pid) {
        Process process = null;
        System.out.println("platform : '{}' " + platform);
        try {
            if(platform.indexOf("mac") >= 0 || platform.indexOf("nux") >= 0
                    || platform.indexOf("sunos") >= 0) {
                process = Runtime.getRuntime().exec("ps aux | grep " + pid);
                int errCode = process.waitFor();
                if(errCode != 0) {
                    throw new RuntimeException(
                            "Exception while trying to get process status of id : "
                                    + pid);
                }
            }
            else {
                throw new UnsupportedOperationException(
                        "This feature to check if a ProcessRunning is not supported for this platform : "
                                + platform);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if(process != null) {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            try {
                while ((line = br.readLine()) != null) {
                    if(line.contains(pid)) {
                        return true;
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
        return true;// Processes should not shutdown because of exception while
                    // retrieving host process status.
    }

    /**
     * Kill the current process.
     */
    private static void kill(String currentPid) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("kill -9 " + currentPid);
            int errCode = process.waitFor();
            if(errCode != 0) {
                throw new RuntimeException(
                        "Exception while trying to get process status of id : "
                                + currentPid);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
 }
