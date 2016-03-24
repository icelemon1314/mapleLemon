package client.messages.commands;

import client.MapleClient;
import client.MapleJob;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.messages.CommandProcessorUtil;
import client.messages.PlayerGMRank;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import constants.GameConstants;
import custom.LoadPacket;
import database.DatabaseConnection;
import handling.channel.ChannelServer;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import scripting.item.ItemScriptManager;
import scripting.npc.NPCScriptManager;
import scripting.quest.QuestScriptManager;
import server.ItemInformation;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.shop.MapleShopFactory;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.StringUtil;
import tools.packet.NPCPacket;

/**
 *
 * @author Emilyx3
 */
public class PlayerCommand {

    public static PlayerGMRank getPlayerLevelRequired() {
        return PlayerGMRank.NORMAL;
    }

    public static class 添加怪物 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(6, splitted[0] + " <怪物ID> <数量>");
                return 0;
            }
            int mobid = Integer.parseInt(splitted[1]);
            int mobTime = Integer.parseInt(splitted[2]);
            MapleMonster npc;
            try {
                npc = MapleLifeFactory.getMonster(mobid);
            } catch (RuntimeException e) {
                c.getPlayer().dropMessage(5, "错误: " + e.getMessage());
                return 0;
            }
            if (npc != null) {
                final int xpos = c.getPlayer().getPosition().x;
                final int ypos = c.getPlayer().getPosition().y;
                final int fh = c.getPlayer().getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId();
                npc.setPosition(c.getPlayer().getPosition());
                npc.setCy(ypos);
                npc.setRx0(xpos);
                npc.setRx1(xpos);
                npc.setFh(fh);
                c.getPlayer().getMap().addMonsterSpawn(npc, mobTime, (byte) -1, null);
                c.getPlayer().dropMessage(6, "请不要重载此地图, 否则服务器重启后怪物会消失");
            } else {
                c.getPlayer().dropMessage(6, "你输入了一个无效的怪物ID");
                return 0;
            }
            return 1;
        }
    }

    public static class 怪物 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleMonster mob = null;
            for (final MapleMapObject monstermo : c.getPlayer().getMap().getMapObjectsInRange(c.getPlayer().getPosition(), 100000, Arrays.asList(MapleMapObjectType.MONSTER))) {
                mob = (MapleMonster) monstermo;
                if (mob.isAlive()) {
                    c.getPlayer().dropMessage(6, "怪物: " + mob.toString());
                    break; //only one
                }
            }
            if (mob == null) {
                c.getPlayer().dropMessage(6, "没找到任何怪物");
            }
            return 1;
        }
    }

    /*public static class 活动 extends CommandExecute {

     @Override
     public int execute(MapleClient c, String[] splitted) {
     if (c.getPlayer().isInBlockedMap() || c.getPlayer().hasBlockedInventory()) {
     c.getPlayer().dropMessage(5, "在这里无法使用该命令");
     return 0;
     }
     NPCScriptManager.getInstance().start(c, 9000000, null);
     return 1;
     }
     }*/

    public static class 解卡 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.removeClickedNPC();
            NPCScriptManager.getInstance().dispose(c);
            ItemScriptManager.getInstance().dispose(c);
            QuestScriptManager.getInstance().dispose(c);
            c.getSession().write(MaplePacketCreator.enableActions());
            c.getPlayer().dropMessage(6, "解卡成功。");
            return 1;
        }
    }

    public static class item extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <道具ID> (数量:默认1)");
                return 0;
            }
            final int itemId = Integer.parseInt(splitted[1]);
            final short quantity = (short) CommandProcessorUtil.getOptionalIntArg(splitted, 2, 1);
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            if (!ii.itemExists(itemId)) {
                c.getPlayer().dropMessage(5, itemId + "不存在");
            } else {
                Item toDrop;
                if (GameConstants.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
                    toDrop = ii.randomizeStats((Equip) ii.getEquipById(itemId));
                } else {
                    toDrop = new client.inventory.Item(itemId, (byte) 0, quantity, (byte) 0);
                }
                toDrop.setGMLog(c.getPlayer().getName() + " 使用 " + splitted[0] + " 命令制作");
                //toDrop.setOwner(c.getPlayer().getName());
                c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), toDrop, c.getPlayer().getPosition(), true, true);
            }
            return 1;
        }
    }

    public static class job extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <职业ID>");
                return 0;
            }
            int jobid = Integer.parseInt(splitted[1]);
            if (!MapleJob.isExist(jobid)) {
                c.getPlayer().dropMessage(5, "职业ID无效");
                return 0;
            }
            c.getPlayer().changeJob((short) jobid, true);
            c.getPlayer().setSubcategory(c.getPlayer().getSubcategory());
            return 1;
        }
    }

    public static class tomap extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted){
            final int mapId = Integer.parseInt(splitted[1]);
            c.getPlayer().changeMap(mapId,0);
            return 0;
        }
    }


    public static class help extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().dropMessage(6, "怀旧冒×岛所有内容还原GF设定，转职请找转职教官！");
            c.getPlayer().dropMessage(6, "制作道具：@item 道具ID");
            c.getPlayer().dropMessage(6, "传送到指定地图：@tomap 地图ID");
            c.getPlayer().dropMessage(6, "快速转职：@job 职业ID");
            return 1;
        }
    }

    /**
     * 重置事件脚本
     */
    public static class reloadevents extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            for (ChannelServer instance : ChannelServer.getAllInstances()) {
                instance.reloadEvents();
            }
            return 1;
        }
    }

    public static class 搜索物品 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <搜索信息>");
                return 0;
            }
            String search = StringUtil.joinStringFrom(splitted, 1);
            String result = "";
            MapleData data = null;
            MapleDataProvider dataProvider = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath", "wz") + "/String.wz"));
            List<String> retItems = new ArrayList<>();
            int selection = 0;
            for (ItemInformation itemPair : MapleItemInformationProvider.getInstance().getAllItems()) {
                if (itemPair != null && itemPair.name != null && itemPair.name.toLowerCase().contains(search.toLowerCase())) {
                    retItems.add("\r\n#L" + selection + "##b" + itemPair.itemId + " " + " #k- " + " #r#z" + itemPair.itemId + "##k");
                    selection++;
                }
            }
            if (retItems != null && retItems.size() > 0) {
                for (String singleRetItem : retItems) {
                    if (result.length() < 10000) {
                        result += singleRetItem;
                    } else {
                        result += "\r\n#b无法载入所有物品, 因为搜索出来的数量太多了#k";
                        c.getSession().write(NPCPacket.sendNPCSay(9010000,result));
                        return 1;
                    }
                }
            } else {
                result = "找不到物品";
            }
            c.getSession().write(NPCPacket.sendNPCSay(9010000, result));
            return 1;
        }
    }

    public static class 扔物品 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <数量> <道具名稱>");
                return 0;
            }
            final String itemName = StringUtil.joinStringFrom(splitted, 2);
            final short quantity = (short) CommandProcessorUtil.getOptionalIntArg(splitted, 1, 1);
            int itemId = 0;
            for (Pair<Integer, String> item : MapleItemInformationProvider.getInstance().getAllItems2()) {
                if (item.getRight().toLowerCase().equals(itemName.toLowerCase())) {
                    itemId = item.getLeft();
                    break;
                }
            }
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            if (!ii.itemExists(itemId)) {
                c.getPlayer().dropMessage(5, itemName + "不存在");
            } else {
                Item toDrop;
                if (GameConstants.getInventoryType(itemId) == MapleInventoryType.EQUIP) {

                    toDrop = ii.getEquipById(itemId);
                } else {
                    toDrop = new client.inventory.Item(itemId, (byte) 0, quantity, (byte) 0);
                }
                toDrop.setGMLog(c.getPlayer().getName() + " 使用 " + splitted[0] + " 命令制作");
                toDrop.setOwner(c.getPlayer().getName());
                c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), toDrop, c.getPlayer().getPosition(), true, true);
            }
            return 1;
        }
    }

    public static class 金币 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <金额>");
                return 0;
            }
            c.getPlayer().gainMeso(Long.parseLong(splitted[1]), true);
            return 1;
        }
    }

    public static class fp extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getSession().write(LoadPacket.getPacket());
            return 1;
        }
    }

    public static class 重载商店 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleShopFactory.getInstance().clear();
            return 1;
        }
    }

    public static class 帮助 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            StringBuilder sb = new StringBuilder();
//            sb.append("\r\n@丢掉现金物品 < 可以将身上的现金物品丢到地上 >");
//            sb.append("\r\n@参加活动 < 如果活动正在进行你可以使用这个命令参加活动 >");
//            sb.append("\r\n@召唤炸弹 < 炸弹活动中使用的命令,可以召唤出炸弹 >");
            sb.append("\r\n@呼叫管理员 < 发送消息给所有在线的管理员 >");
            sb.append("\r\n@怪物 < 距离最近的怪物的信息 >");
            sb.append("\r\n@解卡 < 显如果你卡死或者无法打开NPC可以使用这个命令 >");
//            sb.append("\r\n@活动 < 打开活动NPC >");
            sb.append("\r\n@卡图 < 只有卡图无法出来时才能使用 >");
            sb.append("\r\n@获取猫头鹰 < 获得一个可以搜索怪物爆物的猫头鹰 >");
            //sb.append("\r\n@获取经验卡 (倍数，默认2) < 获得一个经验值卡，每周有次数限制 >");
            if (c.canClickNPC()) {
                NPCPacket.sendNPCSay(9010000, sb.toString());
            }
            for (String command : sb.toString().split("\r\n")) {
                c.getPlayer().dropMessage(5, command);
            }
            return 1;
        }
    }

    public static class 呼叫管理员 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
                cserv.broadcastGMMessage(MaplePacketCreator.multiChat("[管理员帮助] " + c.getPlayer().getName(), StringUtil.joinStringFrom(splitted, 1), 0));
            }
            c.getPlayer().dropMessage(5, "你的消息已经发送成功");
            return 1;
        }
    }

    public static class 卡图 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().卡图 == c.getPlayer().getMapId() && c.getPlayer().getMapId() / 1000000 != 4) {
                c.getPlayer().changeMap(100000000, 0);
            } else {
                c.getPlayer().dropMessage(1, "你没有卡图啊。");
            }
            return 1;
        }
    }

}
