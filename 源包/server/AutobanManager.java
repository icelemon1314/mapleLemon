package server;

import client.MapleClient;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;
import tools.FileoutputUtil;

public class AutobanManager implements Runnable {

    private final Map<Integer, Integer> points = new HashMap();
    private final Map<Integer, List<String>> reasons = new HashMap();
    private final Set<ExpirationEntry> expirations = new TreeSet();
    private static final int AUTOBAN_POINTS = 5000;
    private static final AutobanManager instance = new AutobanManager();
    private final ReentrantLock lock = new ReentrantLock(true);
    private static final Logger log = Logger.getLogger(AutobanManager.class);

    public static AutobanManager getInstance() {
        return instance;
    }

    public void autoban(MapleClient c, String reason) {
        if (c.getPlayer() == null) {
            return;
        }
        if (c.getPlayer().isGM()) {
            c.getPlayer().dropMessage(5, new StringBuilder().append("[警告] A/b 触发: ").append(reason).toString());
            return;
        }
        addPoints(c, AUTOBAN_POINTS, 0L, reason);
    }

    public void autobanDamage(MapleClient c, String reason) {
        if (c.getPlayer() == null) {
            return;
        }
        if (c.getPlayer().isGM()) {
            c.getPlayer().dropMessage(5, new StringBuilder().append("[警告] : ").append(reason).toString());
        } else {
            c.getPlayer().sendPolice(new StringBuilder().append("[警告] : ").append(reason).toString());
        }
    }

    public void addPoints(MapleClient c, int points, long expiration, String reason) {
        this.lock.lock();
        try {
            int acc = c.getPlayer().getAccountID();

            if (this.points.containsKey(acc)) {
                int SavedPoints = (this.points.get(Integer.valueOf(acc)));
                if (SavedPoints >= AUTOBAN_POINTS) {
                    return;
                }
                this.points.put(acc, SavedPoints + points);
                List reasonList = (List) this.reasons.get(Integer.valueOf(acc));
                reasonList.add(reason);
            } else {
                this.points.put(acc, points);
                List reasonList = new LinkedList();
                reasonList.add(reason);
                this.reasons.put(acc, reasonList);
            }

            if ((this.points.get(acc)) >= AUTOBAN_POINTS) {
                FileoutputUtil.log(new StringBuilder().append("[作弊] 玩家 ").append(c.getPlayer().getName()).append(" A/b 触发 ").append(reason).toString());
                if (c.getPlayer().isGM()) {
                    c.getPlayer().dropMessage(5, new StringBuilder().append("[警告] A/b 触发 : ").append(reason).toString());
                    return;
                }
                StringBuilder sb = new StringBuilder("A/b ");
                sb.append(c.getPlayer().getName());
                sb.append(" (IP ");
                sb.append(c.getSession().getRemoteAddress().toString());
                sb.append("): ");
                for (String s : reasons.get(acc)) {
                    sb.append(s);
                    sb.append(", ");
                }
//                WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.serverNotice(0, new StringBuilder().append(" <").append(c.getPlayer().getName()).append("> 被系统封号 (原因: ").append(reason).append(")").toString()));
            } else if (expiration > 0L) {
                this.expirations.add(new ExpirationEntry(System.currentTimeMillis() + expiration, acc, points));
            }
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        for (ExpirationEntry e : this.expirations) {
            if (e.time <= now) {
                this.points.put(e.acc, (this.points.get(e.acc)) - e.points);
            } else {
                return;
            }
        }
    }

    private static class ExpirationEntry
            implements Comparable<ExpirationEntry> {

        public long time;
        public int acc;
        public int points;

        public ExpirationEntry(long time, int acc, int points) {
            this.time = time;
            this.acc = acc;
            this.points = points;
        }

        @Override
        public int compareTo(ExpirationEntry o) {
            return (int) (this.time - o.time);
        }

        @Override
        public boolean equals(Object oth) {
            if (!(oth instanceof ExpirationEntry)) {
                return false;
            }
            ExpirationEntry ee = (ExpirationEntry) oth;
            return (this.time == ee.time) && (this.points == ee.points) && (this.acc == ee.acc);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 83 * hash + (int) (this.time ^ (this.time >>> 32));
            hash = 83 * hash + this.acc;
            hash = 83 * hash + this.points;
            return hash;
        }
    }
}
