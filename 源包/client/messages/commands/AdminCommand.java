package client.messages.commands;

import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.messages.PlayerGMRank;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import custom.LoadPacket;
import database.DatabaseConnection;
import handling.channel.ChannelServer;
import handling.login.LoginServer;
import handling.world.WorldBroadcastService;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import scripting.npc.NPCScriptManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MaplePortal;
import server.ShutdownServer;
import server.Timer.EventTimer;
import tools.FileoutputUtil;
import tools.HexTool;
import tools.MaplePacketCreator;
import tools.StringUtil;

public class AdminCommand {

    public static PlayerGMRank getPlayerLevelRequired() {
        return PlayerGMRank.ADMIN;
    }

    public static class DamageBuff extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            SkillFactory.getSkill(9101003).getEffect(1).applyTo(c.getPlayer());
            return 1;
        }
    }

    public static class 文件封包 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getSession().write(LoadPacket.getPacket());
            return 1;
        }
    }

    public static class 最近的传送点 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MaplePortal portal = c.getPlayer().getMap().findClosestPortal(c.getPlayer().getTruePosition());
            c.getPlayer().dropMessage(6, portal.getName() + " id: " + portal.getId() + " script: " + portal.getScriptName());

            return 1;
        }
    }

    public static class Uptime extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().dropMessage(6, "Server has been up for " + StringUtil.getReadableMillis(ChannelServer.serverStartTime, System.currentTimeMillis()));
            return 1;
        }
    }
    public static class DropMessage extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            String type = splitted[1];
            String text = splitted[2];
            if (type == null) {
                c.getPlayer().dropMessage(6, "Syntax error: !dropmessage type text");
                return 0;
            }
            if (type.length() > 1) {
                c.getPlayer().dropMessage(6, "Type must be just with one word");
                return 0;
            }
            if (text == null || text.length() < 1) {
                c.getPlayer().dropMessage(6, "Text must be 1 letter or more!!");
                return 0;
            }
            c.getPlayer().dropMessage(Integer.parseInt(type), text);
            return 1;
        }
    }

    public static class DropMsg extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            String type = splitted[1];
            String text = splitted[2];
            if (type == null) {
                c.getPlayer().dropMessage(6, "Syntax error: !dropmessage type text");
                return 0;
            }
            if (type.length() > 1) {
                c.getPlayer().dropMessage(6, "Type must be just with one word");
                return 0;
            }
            if (text == null || text.length() < 1) {
                c.getPlayer().dropMessage(6, "Text must be 1 letter or more!!");
                return 0;
            }
            //c.getPlayer().dropMsg(Integer.parseInt(type), text);
            return 1;
        }
    }

    public static class 设置管理员 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(0, splitted[0] + " <玩家名字> <管理员等级>");
                return 0;
            }
            c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]).setGmLevel(Byte.parseByte(splitted[2]));
            return 1;
        }
    }

    public static class 切换自动注册状态 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            LoginServer.autoReg = !LoginServer.autoReg;
            c.getPlayer().dropMessage(0, "自动注册状态: " + (LoginServer.isAutoReg() ? "开启" : "关闭"));
            FileoutputUtil.log("自动注册状态: " + (LoginServer.isAutoReg() ? "开启" : "关闭"));
            return 1;
        }
    }

    public static class 封包 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(0, splitted[0] + " <封包内容>");
                return 0;
            }
            c.getSession().write(HexTool.getByteArrayFromHexString(StringUtil.joinStringFrom(splitted, 1)));
            return 1;
        }
    }

    public static class 脱掉所有人 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            ChannelServer cs = c.getChannelServer();
            for (MapleCharacter mchr : cs.getPlayerStorage().getAllCharacters()) {
                if (c.getPlayer().isGM()) {
                    continue;
                }
                MapleInventory equipped = mchr.getInventory(MapleInventoryType.EQUIPPED);
                MapleInventory equip = mchr.getInventory(MapleInventoryType.EQUIP);
                List<Short> ids = new ArrayList<>();
                for (Item item : equipped.newList()) {
                    ids.add((short)item.getPosition());
                }
                for (short id : ids) {
                    MapleInventoryManipulator.unequip(mchr.getClient(), id, equip.getNextFreeSlot());
                }
            }
            return 1;
        }
    }

    public static class 送给所有人金币 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(0, splitted[0] + " <金额>");
                return 0;
            }
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                    mch.gainMeso(Long.parseLong(splitted[1]), true);
                }
            }
            return 1;
        }
    }

    public static class HotTime extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                    if (mch.getClient().canClickNPC() && !mch.isIntern()) {
                        NPCScriptManager.getInstance().start(mch.getClient(), 9010010,"HOTTIME");
                    }
                }
            }
            FileoutputUtil.log("HotTime操作已经完成");
            return 1;
        }
    }

    public static class 传送所有人到这里 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            for (MapleCharacter mch : c.getChannelServer().getPlayerStorage().getAllCharacters()) {
                if (mch.getMapId() != c.getPlayer().getMapId()) {
                    mch.changeMap(c.getPlayer().getMap(), c.getPlayer().getPosition());
                }
            }
            return 1;
        }
    }

    public static class 踢所有人 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            int range = -1;
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(0, splitted[0] + " <对象: 默认c(m - 当前地图/c - 当前频道/w - 当前服务器)>");
            } else {
                switch (splitted[1]) {
                    case "m":
                        range = 0;
                        break;
                    case "c":
                        range = 1;
                        break;
                    case "w":
                        range = 2;
                        break;
                }
            }
            if (range == -1) {
                range = 1;
            }
            if (range == 0) {
                c.getPlayer().getMap().disconnectAll();
            } else if (range == 1) {
                c.getChannelServer().getPlayerStorage().disconnectAll(true);
            } else if (range == 2) {
                for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                    cserv.getPlayerStorage().disconnectAll(true);
                }
            }
            return 1;
        }
    }

    public static class 关闭服务器 extends CommandExecute {

        protected static Thread t = null;

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().dropMessage(6, "正在关闭服务器...");
            if (t == null || !t.isAlive()) {
                t = new Thread(ShutdownServer.getInstance());
                ShutdownServer.getInstance().shutdown();
                t.start();
            } else {
                c.getPlayer().dropMessage(6, "关闭进程正在进行或者关闭已完成, 请稍候");
            }
            return 1;
        }
    }

    public static class 定时关闭服务器 extends 关闭服务器 {

        private static ScheduledFuture<?> ts = null;
        private int minutesLeft = 0;

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(0, splitted[0] + " <时间:分钟>");
                return 0;
            }
            minutesLeft = Integer.parseInt(splitted[1]);
            c.getPlayer().dropMessage(6, "服务器将在" + minutesLeft + "分钟后关闭");
            if (ts == null && (t == null || !t.isAlive())) {
                t = new Thread(ShutdownServer.getInstance());
                ts = EventTimer.getInstance().register(new Runnable() {
                    @Override
                    public void run() {
                        if (minutesLeft == 0) {
                            ShutdownServer.getInstance().shutdown();
                            t.start();
                            ts.cancel(false);
                            return;
                        }
                        WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.serverMessageNotice("服务器将在" + minutesLeft + "分钟后进行停机维护, 请及时安全下线, 以免造成不必要的损失"));
                        minutesLeft--;
                    }
                }, 60000);
            } else {
                c.getPlayer().dropMessage(6, "关闭进程正在进行或者关闭已完成, 请稍候");
            }
            return 1;
        }
    }

    public static class 数据库 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(0, splitted[0] + " <SQL命令>");
                return 0;
            }
            try {
                Connection con = (Connection) DatabaseConnection.getConnection();
                PreparedStatement ps = (PreparedStatement) con.prepareStatement(StringUtil.joinStringFrom(splitted, 1));
                ps.executeUpdate();
                ps.close();
            } catch (SQLException e) {
                c.getPlayer().dropMessage(0, "执行SQL命令失败");
                return 0;
            }
            return 1;
        }
    }

    public static class 检测复制 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

            Iterator i$;
            MapleCharacter player;
            List<String> msgs = new ArrayList();
            Map checkItems = new LinkedHashMap();
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (i$ = cserv.getPlayerStorage().getAllCharacters().iterator(); i$.hasNext();) {
                    player = (MapleCharacter) i$.next();
                    if ((player != null) && (player.getMap() != null)) {
                        MapleInventory equip = player.getInventory(MapleInventoryType.EQUIP);
                        for (Item item : equip.list()) {
                            if (item.getEquipOnlyId() > 0) {
                                CopyItemInfo ret = new CopyItemInfo(item.getItemId(), player.getId(), player.getName());
                                if (checkItems.containsKey(item.getEquipOnlyId())) {
                                    ret = (CopyItemInfo) checkItems.get(item.getEquipOnlyId());
                                    if (ret.itemId == item.getItemId()) {
                                        if (ret.isFirst()) {
                                            ret.setFirst(false);
                                            msgs.add("角色: " + StringUtil.getRightPaddedStr(ret.name, ' ', 13) + " 角色ID: " + StringUtil.getRightPaddedStr(String.valueOf(ret.chrId), ' ', 6) + " 道具: " + ret.itemId + " - " + ii.getName(ret.itemId) + " 唯一ID: " + item.getEquipOnlyId());
                                        } else {
                                            msgs.add("角色: " + StringUtil.getRightPaddedStr(player.getName(), ' ', 13) + " 角色ID: " + StringUtil.getRightPaddedStr(String.valueOf(player.getId()), ' ', 6) + " 道具: " + item.getItemId() + " - " + ii.getName(item.getItemId()) + " 唯一ID: " + item.getEquipOnlyId());
                                        }
                                    }
                                } else {
                                    checkItems.put(item.getEquipOnlyId(), ret);
                                }
                            }
                        }

                        equip = player.getInventory(MapleInventoryType.EQUIPPED);
                        for (Item item : equip.list()) {
                            if (item.getEquipOnlyId() > 0) {
                                CopyItemInfo ret = new CopyItemInfo(item.getItemId(), player.getId(), player.getName());
                                if (checkItems.containsKey(item.getEquipOnlyId())) {
                                    ret = (CopyItemInfo) checkItems.get(item.getEquipOnlyId());
                                    if (ret.itemId == item.getItemId()) {
                                        if (ret.isFirst()) {
                                            ret.setFirst(false);
                                            msgs.add("角色: " + StringUtil.getRightPaddedStr(ret.name, ' ', 13) + " 角色ID: " + StringUtil.getRightPaddedStr(String.valueOf(ret.chrId), ' ', 6) + " 道具: " + ret.itemId + " - " + ii.getName(ret.itemId) + " 唯一ID: " + item.getEquipOnlyId());
                                        } else {
                                            msgs.add("角色: " + StringUtil.getRightPaddedStr(player.getName(), ' ', 13) + " 角色ID: " + StringUtil.getRightPaddedStr(String.valueOf(player.getId()), ' ', 6) + " 道具: " + item.getItemId() + " - " + ii.getName(item.getItemId()) + " 唯一ID: " + item.getEquipOnlyId());
                                        }
                                    }
                                } else {
                                    checkItems.put(item.getEquipOnlyId(), ret);
                                }
                            }
                        }
                    }
                }
            }
            checkItems.clear();
            if (msgs.size() > 0) {
                c.getPlayer().dropMessage(5, "检测完成，共有: " + msgs.size() + " 个复制信息");
                FileoutputUtil.log(FileoutputUtil.复制装备, "检测完成，共有: " + msgs.size() + " 个复制信息", true);
                for (String s : msgs) {
                    c.getPlayer().dropMessage(5, s);
                    FileoutputUtil.log(FileoutputUtil.复制装备, s, true);
                }
                c.getPlayer().dropMessage(5, "以上信息为拥有复制道具的玩家。");
            } else {
                c.getPlayer().dropMessage(5, "未检测到游戏中的角色有复制的道具信息。");
            }
            return 1;
        }
    }
}
