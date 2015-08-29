package tools;

import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import org.apache.log4j.Logger;

public class DeadlockDetector extends Thread{

    private static final Logger log = Logger.getLogger(DeadlockDetector.class);
    private int checkInterval = 0;
    private static final String INDENT = "    ";
    private StringBuilder sb = null;

    public DeadlockDetector(int checkInterval) {
        this.checkInterval = (checkInterval * 1000);
    }

    @Override
    public void run() {
        boolean noDeadLocks = true;

        while (noDeadLocks) {
            try {
                ThreadMXBean bean = ManagementFactory.getThreadMXBean();
                long[] threadIds = bean.findDeadlockedThreads();

                if (threadIds != null) {
                    log.error("检测到死锁!!");
                    this.sb = new StringBuilder();
                    noDeadLocks = false;

                    ThreadInfo[] infos = bean.getThreadInfo(threadIds);
                    this.sb.append("\n线程锁信息: \n");
                    for (ThreadInfo threadInfo : infos) {
                        printThreadInfo(threadInfo);
                        LockInfo[] lockInfos = threadInfo.getLockedSynchronizers();
                        MonitorInfo[] monitorInfos = threadInfo.getLockedMonitors();

                        printLockInfo(lockInfos);
                        printMonitorInfo(threadInfo, monitorInfos);
                    }

                    this.sb.append("\n线程转储: \n");
                    for (ThreadInfo ti : bean.dumpAllThreads(true, true)) {
                        printThreadInfo(ti);
                    }
                    log.error(this.sb.toString());
                }
                Thread.sleep(this.checkInterval);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void printThreadInfo(ThreadInfo threadInfo) {
        printThread(threadInfo);
        this.sb.append(INDENT).append(threadInfo.toString()).append("\n");
        StackTraceElement[] stacktrace = threadInfo.getStackTrace();
        MonitorInfo[] monitors = threadInfo.getLockedMonitors();

        for (int i = 0; i < stacktrace.length; i++) {
            StackTraceElement ste = stacktrace[i];
            this.sb.append(INDENT).append("at ").append(ste.toString()).append("\n");
            for (MonitorInfo mi : monitors) {
                if (mi.getLockedStackDepth() == i) {
                    this.sb.append(INDENT).append("  - locked ").append(mi).append("\n");
                }
            }
        }
    }

    private void printThread(ThreadInfo ti) {
        this.sb.append("\nPrintThread\n");
        this.sb.append("\"").append(ti.getThreadName()).append("\" Id=").append(ti.getThreadId()).append(" in ").append(ti.getThreadState()).append("\n");
        if (ti.getLockName() != null) {
            this.sb.append(" on lock=").append(ti.getLockName()).append("\n");
        }
        if (ti.isSuspended()) {
            this.sb.append(" (suspended)\n");
        }
        if (ti.isInNative()) {
            this.sb.append(" (running in native)\n");
        }
        if (ti.getLockOwnerName() != null) {
            this.sb.append(INDENT).append(" owned by ").append(ti.getLockOwnerName()).append(" Id=").append(ti.getLockOwnerId()).append("\n");
        }
    }

    private void printMonitorInfo(ThreadInfo threadInfo, MonitorInfo[] monitorInfos) {
        this.sb.append(INDENT).append("Locked monitors: count = ").append(monitorInfos.length).append("\n");
        for (MonitorInfo monitorInfo : monitorInfos) {
            this.sb.append(INDENT).append("  - ").append(monitorInfo).append(" locked at \n");
            this.sb.append(INDENT).append("      ").append(monitorInfo.getLockedStackDepth()).append(" ").append(monitorInfo.getLockedStackFrame()).append("\n");
        }
    }

    private void printLockInfo(LockInfo[] lockInfos) {
        this.sb.append(INDENT).append("Locked synchronizers: count = ").append(lockInfos.length).append("\n");
        for (LockInfo lockInfo : lockInfos) {
            this.sb.append(INDENT).append("  - ").append(lockInfo).append("\n");
        }
    }
}
