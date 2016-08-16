package server.shops;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HiredMerchantSave {

    public static final int NumSavingThreads = 5;
    private static final TimingThread[] Threads = new TimingThread[5];
    private static final AtomicInteger Distribute;

    public static void QueueShopForSave(HiredMerchant hm) {
        int Current = Distribute.getAndIncrement() % 5;
        Threads[Current].getRunnable().Queue(hm);
    }

    public static void Execute(Object ToNotify) {
        for (TimingThread Thread : Threads) {
            Thread.getRunnable().SetToNotify(ToNotify);
        }
        for (TimingThread Thread : Threads) {
            Thread.start();
        }
    }

    static {
        for (int i = 0; i < Threads.length; i++) {
            Threads[i] = new TimingThread(new HiredMerchantSaveRunnable());
        }

        Distribute = new AtomicInteger(0);
    }

    private static class TimingThread extends Thread {

        private final HiredMerchantSave.HiredMerchantSaveRunnable ext;

        public TimingThread(HiredMerchantSave.HiredMerchantSaveRunnable r) {
            super();
            this.ext = r;
        }

        public HiredMerchantSave.HiredMerchantSaveRunnable getRunnable() {
            return this.ext;
        }
    }

    private static class HiredMerchantSaveRunnable
            implements Runnable {

        private static final AtomicInteger RunningThreadID = new AtomicInteger(0);
        private final int ThreadID = RunningThreadID.incrementAndGet();
        private long TimeTaken = 0L;
        private int ShopsSaved = 0;
        private Object ToNotify;
        private final ArrayBlockingQueue<HiredMerchant> Queue = new ArrayBlockingQueue(500);

        @Override
        public void run() {
            try {
                while (!this.Queue.isEmpty()) {
                    HiredMerchant next = (HiredMerchant) this.Queue.take();
                    long Start = System.currentTimeMillis();
                    if ((next.getMCOwner() != null) && (next.getMCOwner().getPlayerShop() == next)) {
                        next.getMCOwner().setPlayerShop(null);
                    }
                    next.closeShop(true, false);
                    this.TimeTaken += System.currentTimeMillis() - Start;
                    this.ShopsSaved += 1;
                }
                synchronized (this.ToNotify) {
                    this.ToNotify.notify();
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(HiredMerchantSave.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        private void Queue(HiredMerchant hm) {
            this.Queue.add(hm);
        }

        private void SetToNotify(Object o) {
            if (this.ToNotify == null) {
                this.ToNotify = o;
            }
        }
    }
}
