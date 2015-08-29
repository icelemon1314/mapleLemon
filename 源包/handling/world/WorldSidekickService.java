package handling.world;

import client.MapleCharacter;
import handling.channel.ChannelServer;
import handling.world.sidekick.MapleSidekick;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class WorldSidekickService {

    private final Map<Integer, MapleSidekick> sidekickList;
    private final ReentrantReadWriteLock lock;

    public static WorldSidekickService getInstance() {
        return SingletonHolder.instance;
    }

    private WorldSidekickService() {
        this.lock = new ReentrantReadWriteLock();
        this.sidekickList = new LinkedHashMap();
        for (MapleSidekick sidekick : MapleSidekick.loadAll()) {
            if (sidekick.getId() >= 0) {
                this.sidekickList.put(sidekick.getId(), sidekick);
            }
        }
    }

    public void addLoadedSidekick(MapleSidekick sidekick) {
        if (sidekick.getId() >= 0) {
            this.sidekickList.put(sidekick.getId(), sidekick);
        }
    }

    public int createSidekick(int leaderId, int leaderId2) {
        return MapleSidekick.create(leaderId, leaderId2);
    }

    public void eraseSidekick(int id) {
        this.lock.writeLock().lock();
        try {
            MapleSidekick ms = (MapleSidekick) this.sidekickList.remove(Integer.valueOf(id));
            if (ms != null) {
                erasePlayer(ms.getCharacter(0).getId());
                erasePlayer(ms.getCharacter(1).getId());
            }
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public void erasePlayer(int targetId) {
        int ch = WorldFindService.getInstance().findChannel(targetId);
        if (ch < 0) {
            return;
        }
        MapleCharacter player = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(targetId);
        if (player != null) {
            player.setSidekick(null);
        }
    }

    public MapleSidekick getSidekick(int id) {
        MapleSidekick ret = null;
        this.lock.readLock().lock();
        try {
            ret = (MapleSidekick) this.sidekickList.get(id);
        } finally {
            this.lock.readLock().unlock();
        }
        if (ret == null) {
            this.lock.writeLock().lock();
            try {
                ret = new MapleSidekick(id);
                if (ret.getId() < 0) {
                    return null;

                }
                this.sidekickList.put(id, ret);
            } finally {
                this.lock.writeLock().unlock();
            }
        }
        return ret;
    }

    public MapleSidekick getSidekickByChr(int id) {
        this.lock.readLock().lock();
        try {
            for (MapleSidekick sidekick : this.sidekickList.values()) {
                if ((sidekick.getCharacter(0).getId() == id) || (sidekick.getCharacter(1).getId() == id)) {
                    MapleSidekick localMapleSidekick1 = sidekick;
                    return localMapleSidekick1;
                }
            }
        } finally {
            this.lock.readLock().unlock();
        }
        return null;
    }

    private static class SingletonHolder {

        protected static final WorldSidekickService instance = new WorldSidekickService();
    }
}
