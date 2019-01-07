package handling.world;

import client.MapleCharacter;
import client.MapleJob;
import handling.MapleServerHandler;
import handling.channel.ChannelServer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.swing.table.DefaultTableModel;
import server.ManagerSin;
import tools.FileoutputUtil;

public class WorldFindService {

    private final ReentrantReadWriteLock lock;
    private final HashMap<Integer, Integer> idToChannel;
    private final HashMap<String, Integer> nameToChannel;

    public static WorldFindService getInstance() {
        return SingletonHolder.instance;
    }

    private WorldFindService() {
        this.lock = new ReentrantReadWriteLock();
        this.idToChannel = new HashMap();
        this.nameToChannel = new HashMap();
    }

    public void register(int chrId, String chrName, int channel, MapleCharacter chr) {
        this.lock.writeLock().lock();
        try {
            this.idToChannel.put(chrId, channel);
            this.nameToChannel.put(chrName.toLowerCase(), channel);
        } finally {
            this.lock.writeLock().unlock();
        }
        if (channel == -10) {
            FileoutputUtil.log("[登陆信息] 玩家连接 - 角色ID: " + chrId + " 名字: " + chrName + " 进入商城");
        } else if (channel == -20) {
            FileoutputUtil.log("[登陆信息] 玩家连接 - 角色ID: " + chrId + " 名字: " + chrName + " 进入拍卖");
        } else if (channel > -1) {
            FileoutputUtil.log("[登陆信息] 玩家连接 - 角色ID: " + chrId + " 名字: " + chrName + " 频道: " + channel);
        } else {
            FileoutputUtil.log("[登陆信息] 玩家连接 - 角色ID: " + chrId + " 未处理的频道...");
        }
        try {
            int countRows = ManagerSin.jTable1.getRowCount();//获取当前表格总行数
            if (chr != null) {
                ((DefaultTableModel) ManagerSin.jTable1.getModel()).insertRow(countRows, new Object[]{chr.getClient().getAccID(), chrName + " (ID:" + chrId + ")", chr.getLevel(), MapleJob.getJobName(chr.getJob()) + "(" + chr.getJob() + ")", channel == -10 ? "现金商城" : channel == -20 ? "拍卖" : channel > -1 ? chr.getMap().getMapName() + "(" + chr.getMapId() + ")" : "未知频道"});
            }
        } catch (Exception e) {
            FileoutputUtil.outputFileError(FileoutputUtil.GUI_Ex_Log, e);
        }
    }

    public void forceDeregister(int chrId) {
        this.lock.writeLock().lock();
        try {
            this.idToChannel.remove(chrId);
        } finally {
            this.lock.writeLock().unlock();
        }
        FileoutputUtil.log("[玩家离开] 角色ID: " + chrId);
    }

    public void forceDeregister(String chrName) {
        this.lock.writeLock().lock();
        try {
            this.nameToChannel.remove(chrName.toLowerCase());
        } finally {
            this.lock.writeLock().unlock();
        }
        FileoutputUtil.log("[玩家离开] 角色名字: " + chrName);
    }

    public void forceDeregister(int chrId, String chrName) {
        this.lock.writeLock().lock();
        try {
            this.idToChannel.remove(chrId);
            this.nameToChannel.remove(chrName.toLowerCase());
        } finally {
            this.lock.writeLock().unlock();
        }
        FileoutputUtil.log("[玩家离开] 角色ID: " + chrId + " 名字: " + chrName);
    }

    public int findChannel(int chrId) {
        this.lock.readLock().lock();
        Integer ret;
        try {
            ret = this.idToChannel.get(chrId);
        } finally {
            this.lock.readLock().unlock();
        }
        if (ret != null) {
            if ((ret != MapleServerHandler.CASH_SHOP_SERVER) && (ChannelServer.getInstance(ret) == null)) {
                forceDeregister(chrId);
                return -1;
            }
            return ret;
        }
        return -1;
    }

    public int findChannel(String chrName) {
        this.lock.readLock().lock();
        Integer ret;
        try {
            ret = this.nameToChannel.get(chrName.toLowerCase());
        } finally {
            this.lock.readLock().unlock();
        }
        if (ret != null) {
            if ((ret != MapleServerHandler.CASH_SHOP_SERVER) && (ChannelServer.getInstance(ret) == null)) {
                forceDeregister(chrName);
                return -1;
            }
            return ret;
        }
        return -1;
    }

    public CharacterIdChannelPair[] multiBuddyFind(int charIdFrom, int[] characterIds) {
        List foundsChars = new ArrayList(characterIds.length);
        for (int i : characterIds) {
            int channel = findChannel(i);
            if (channel > 0) {
                foundsChars.add(new CharacterIdChannelPair(i, channel));
            }
        }
        Collections.sort(foundsChars);
        return (CharacterIdChannelPair[]) foundsChars.toArray(new CharacterIdChannelPair[foundsChars.size()]);
    }

    public MapleCharacter findCharacterByName(String name) {
        int ch = findChannel(name);
        if (ch > 0) {
            return ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(name);
        }
        return null;
    }

    public MapleCharacter findCharacterById(int id) {
        int ch = findChannel(id);
        if (ch > 0) {
            return ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(id);
        }
        return null;
    }

    private static class SingletonHolder {

        protected static final WorldFindService instance = new WorldFindService();
    }
}
