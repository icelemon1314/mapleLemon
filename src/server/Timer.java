package server;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import tools.FileoutputUtil;

public abstract class Timer {

    private ScheduledThreadPoolExecutor ses;
    protected String file;
    protected String name;
    private static final AtomicInteger threadNumber = new AtomicInteger(1);

    public void start() {
        if ((this.ses != null) && (!this.ses.isShutdown()) && (!this.ses.isTerminated())) {
            return;
        }
        this.file = ("日志\\日志_" + this.name + "_异常.rtf");
        this.ses = new ScheduledThreadPoolExecutor(5, new RejectedThreadFactory());
        this.ses.setKeepAliveTime(10L, TimeUnit.MINUTES);
        this.ses.allowCoreThreadTimeOut(true);
        this.ses.setMaximumPoolSize(8);
        this.ses.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
    }

    public ScheduledThreadPoolExecutor getSES() {
        return this.ses;
    }

    public void stop() {
        if (this.ses != null) {
            this.ses.shutdown();
        }
    }

    public ScheduledFuture<?> register(Runnable r, long repeatTime, long delay) {
        if (this.ses == null) {
            return null;
        }
        return this.ses.scheduleAtFixedRate(new LoggingSaveRunnable(r, this.file), delay, repeatTime, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> register(Runnable r, long repeatTime) {
        if (this.ses == null) {
            return null;
        }
        return this.ses.scheduleAtFixedRate(new LoggingSaveRunnable(r, this.file), 0L, repeatTime, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> schedule(Runnable r, long delay) {
        if (this.ses == null) {
            return null;
        }
        return this.ses.schedule(new LoggingSaveRunnable(r, this.file), delay, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> scheduleAtTimestamp(Runnable r, long timestamp) {
        return schedule(r, timestamp - System.currentTimeMillis());
    }

    private class RejectedThreadFactory implements ThreadFactory {

        private final AtomicInteger threadNumber2 = new AtomicInteger(1);
        private final String tname;

        public RejectedThreadFactory() {
            this.tname = (Timer.this.name + Randomizer.nextInt());
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName(this.tname + "-W-" + Timer.threadNumber.getAndIncrement() + "-" + this.threadNumber2.getAndIncrement());
            return t;
        }
    }

    private static class LoggingSaveRunnable
            implements Runnable {

        Runnable r;
        String file;

        public LoggingSaveRunnable(Runnable r, String file) {
            this.r = r;
            this.file = file;
        }

        @Override
        public void run() {
            try {
                this.r.run();
            } catch (Throwable t) {
                FileoutputUtil.outputFileError(this.file, t);
            }
        }
    }

    public static class PingTimer extends Timer {

        private static final PingTimer instance = new PingTimer();

        private PingTimer() {
            this.name = "PING计时线程";
        }

        public static PingTimer getInstance() {
            return instance;
        }
    }

    public static class CheatTimer extends Timer {

        private static final CheatTimer instance = new CheatTimer();

        private CheatTimer() {
            this.name = "作弊计时线程";
        }

        public static CheatTimer getInstance() {
            return instance;
        }
    }

    public static class EtcTimer extends Timer {

        private static final EtcTimer instance = new EtcTimer();

        private EtcTimer() {
            this.name = "其他计时线程";
        }

        public static EtcTimer getInstance() {
            return instance;
        }

        void schedule(Runnable runnable) {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    public static class CloneTimer extends Timer {

        private static final CloneTimer instance = new CloneTimer();

        private CloneTimer() {
            this.name = "克隆计时线程";
        }

        public static CloneTimer getInstance() {
            return instance;
        }
    }

    public static class EventTimer extends Timer {

        private static final EventTimer instance = new EventTimer();

        private EventTimer() {
            this.name = "事件计时线程";
        }

        public static EventTimer getInstance() {
            return instance;
        }
    }

    public static class BuffTimer extends Timer {

        private static final BuffTimer instance = new BuffTimer();

        private BuffTimer() {
            this.name = "BUFF计时线程";
        }

        public static BuffTimer getInstance() {
            return instance;
        }
    }

    public static class MapTimer extends Timer {

        private static final MapTimer instance = new MapTimer();

        private MapTimer() {
            this.name = "地图计时线程";
        }

        public static MapTimer getInstance() {
            return instance;
        }
    }

    public static class WorldTimer extends Timer {

        private static final WorldTimer instance = new WorldTimer();

        private WorldTimer() {
            this.name = "世界计时线程";
        }

        public static WorldTimer getInstance() {
            return instance;
        }
    }
}
