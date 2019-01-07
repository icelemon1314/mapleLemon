package handling.channel;

import client.MapleCharacter;
import client.MapleCharacterUtil;
import handling.world.CharacterTransfer;
import handling.world.CheaterData;
import handling.world.WorldFindService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import server.Timer.PingTimer;

public class PlayerStorage {

    private final int channel;
    private final ReentrantReadWriteLock mutex = new ReentrantReadWriteLock();
    private final Lock readLock = this.mutex.readLock();
    private final Lock writeLock = this.mutex.writeLock();
    private final ReentrantReadWriteLock mutex2 = new ReentrantReadWriteLock();
    private final Lock pendingReadLock = this.mutex2.readLock();
    private final Lock pendingWriteLock = this.mutex2.writeLock();
    private final Map<String, MapleCharacter> nameToChar = new HashMap();
    private final Map<Integer, MapleCharacter> idToChar = new HashMap();
    private final Map<Integer, CharacterTransfer> PendingCharacter = new HashMap();

    public PlayerStorage(int channel) {
        this.channel = channel;

        PingTimer.getInstance().register(new PersistingTask(), 60000L);
    }

    public ArrayList<MapleCharacter> getAllCharacters() {
        this.readLock.lock();
        try {
            ArrayList localArrayList = new ArrayList(this.idToChar.values());
            return localArrayList;
        } finally {
            this.readLock.unlock();
        }
    }

    public void registerPlayer(MapleCharacter chr) {
        this.writeLock.lock();
        try {
            this.nameToChar.put(chr.getName().toLowerCase(), chr);
            this.idToChar.put(chr.getId(), chr);
        } finally {
            this.writeLock.unlock();
        }
        WorldFindService.getInstance().register(chr.getId(), chr.getName(), this.channel, chr);
    }

    public void registerPendingPlayer(CharacterTransfer chr, int playerId) {
        this.pendingWriteLock.lock();
        try {
            this.PendingCharacter.put(playerId, chr);
        } finally {
            this.pendingWriteLock.unlock();
        }
    }

    public void deregisterPlayer(MapleCharacter chr) {
        deregisterPlayer(chr.getId(), chr.getName().toLowerCase());
        WorldFindService.getInstance().forceDeregister(chr.getId(), chr.getName());
    }

    public void deregisterPlayer(int idz, String namez) {
        this.writeLock.lock();
        try {
            this.nameToChar.remove(namez.toLowerCase());
            this.idToChar.remove(idz);
        } finally {
            this.writeLock.unlock();
        }
        WorldFindService.getInstance().forceDeregister(idz, namez);

    }

    public int pendingCharacterSize() {
        return this.PendingCharacter.size();
    }

    public void deregisterPendingPlayer(int charId) {
        this.pendingWriteLock.lock();
        try {
            this.PendingCharacter.remove(charId);
        } finally {
            this.pendingWriteLock.unlock();
        }
    }

    public CharacterTransfer getPendingCharacter(int charId) {
        this.pendingWriteLock.lock();
        try {
            CharacterTransfer localCharacterTransfer = (CharacterTransfer) this.PendingCharacter.remove(Integer.valueOf(charId));
            return localCharacterTransfer;
        } finally {
            this.pendingWriteLock.unlock();
        }
    }

    public MapleCharacter getCharacterByName(String name) {
        this.readLock.lock();
        try {
            MapleCharacter localMapleCharacter = (MapleCharacter) this.nameToChar.get(name.toLowerCase());
            return localMapleCharacter;
        } finally {
            this.readLock.unlock();
        }
    }

    public MapleCharacter getCharacterById(int id) {
        this.readLock.lock();
        try {
            MapleCharacter localMapleCharacter = (MapleCharacter) this.idToChar.get(Integer.valueOf(id));
            return localMapleCharacter;
        } finally {
            this.readLock.unlock();
        }
    }

    public int getConnectedClients() {
        return this.idToChar.size();
    }

    public void disconnectAll() {
        disconnectAll(false);
    }

    public void disconnectAll(boolean checkGM) {
        this.writeLock.lock();
        try {
            Iterator chrit = this.nameToChar.values().iterator();

            while (chrit.hasNext()) {
                MapleCharacter chr = (MapleCharacter) chrit.next();
                if (!chr.isGM() || !checkGM) {
                    chr.getClient().disconnect(false, false, true);
                    if (chr.getClient().getSession().isConnected()) {
                        chr.getClient().getSession().close(true);
                    }
                    WorldFindService.getInstance().forceDeregister(chr.getId(), chr.getName());
                    chrit.remove();
                }
            }
        } finally {
            this.writeLock.unlock();
        }
    }

    public String getOnlinePlayers(boolean byGM) {
        StringBuilder sb = new StringBuilder();
        if (byGM) {
            this.readLock.lock();
            try {
                Iterator itr = this.nameToChar.values().iterator();
                while (itr.hasNext()) {
                    sb.append(MapleCharacterUtil.makeMapleReadable(((MapleCharacter) itr.next()).getName()));
                    sb.append(", ");
                }
            } finally {
                this.readLock.unlock();
            }
        } else {
            this.readLock.lock();
            try {
                for (MapleCharacter chr : this.nameToChar.values()) {
                    if (!chr.isGM()) {
                        sb.append(MapleCharacterUtil.makeMapleReadable(chr.getName()));
                        sb.append(", ");
                    }
                }
            } finally {
                this.readLock.unlock();
            }
        }
        return sb.toString();
    }

    public void broadcastPacket(byte[] data) {
        this.readLock.lock();
        try {
            Iterator itr = this.nameToChar.values().iterator();
            while (itr.hasNext()) {
                ((MapleCharacter) itr.next()).getClient().getSession().write(data);
            }
        } finally {
            this.readLock.unlock();
        }
    }

    public void broadcastSmegaPacket(byte[] data) {
        this.readLock.lock();
        try {
            for (MapleCharacter chr : this.nameToChar.values()) {
                if ((chr.getClient().isLoggedIn()) && (chr.getSmega())) {
                    chr.getClient().getSession().write(data);
                }
            }
        } finally {
            this.readLock.unlock();
        }
    }

    public void broadcastGMPacket(byte[] data) {
        this.readLock.lock();
        try {
            for (MapleCharacter chr : this.nameToChar.values()) {
                if ((chr.getClient().isLoggedIn()) && (chr.isIntern())) {
                    chr.getClient().getSession().write(data);
                }
            }
        } finally {
            this.readLock.unlock();
        }
    }

    public class PersistingTask implements Runnable {

        public PersistingTask() {
        }

        @Override
        public void run() {
            PlayerStorage.this.pendingWriteLock.lock();
            try {
                long currenttime = System.currentTimeMillis();
                Iterator itr = PlayerStorage.this.PendingCharacter.entrySet().iterator();
                while (itr.hasNext()) {
                    if (currenttime - ((CharacterTransfer) ((Map.Entry) itr.next()).getValue()).TranferTime > 40000L) {
                        itr.remove();
                    }
                }
            } finally {
                PlayerStorage.this.pendingWriteLock.unlock();
            }
        }
    }
}
