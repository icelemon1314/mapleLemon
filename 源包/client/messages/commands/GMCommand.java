package client.messages.commands;

import client.*;
import client.inventory.*;
import client.messages.CommandProcessorUtil;
import client.messages.PlayerGMRank;
import constants.GameConstants;
import constants.ItemConstants;
import handling.channel.ChannelServer;
import handling.world.WorldBroadcastService;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import scripting.event.EventManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MaplePortal;
import server.events.MapleEvent;
import server.events.MapleEventType;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleMonsterInformationProvider;
import server.life.MapleNPC;
import server.life.OverrideMonsterStats;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleReactor;
import server.quest.MapleQuest;
import server.shop.MapleShopFactory;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.StringUtil;

/**
 *
 * @author Emilyx3
 */
public class GMCommand {

    public static PlayerGMRank getPlayerLevelRequired() {
        return PlayerGMRank.GM;
    }


    public static class 给予宠物 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 7) {
                c.getPlayer().dropMessage(6, splitted[0] + " <玩家名字> <宠物ID> <宠物名称> <宠物等级> <宠物亲密度> <宠物饥饿感>");
                return 0;
            }
            MapleCharacter petowner = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            int id = Integer.parseInt(splitted[2]);
            String name = splitted[3];
            int level = Integer.parseInt(splitted[4]);
            int closeness = Integer.parseInt(splitted[5]);
            int fullness = Integer.parseInt(splitted[6]);
            long period = 20000;
            short flags = MapleItemInformationProvider.getInstance().getPetFlagInfo(id);
            if (id >= 5001000 || id < 5000000) {
                c.getPlayer().dropMessage(5, "宠物ID错误");
                return 0;
            }
            if (level > 30) {
                level = 30;
            }
            if (closeness > 30000) {
                closeness = 30000;
            }
            if (fullness > 100) {
                fullness = 100;
            }
            if (level < 1) {
                level = 1;
            }
            if (closeness < 0) {
                closeness = 0;
            }
            if (fullness < 0) {
                fullness = 0;
            }
            try {
                MapleInventoryManipulator.addById(petowner.getClient(), id, (short) 1, "", MaplePet.createPet(id, name, level, closeness, fullness, MapleInventoryIdentifier.getInstance(), id == 5000054 ? (int) period : 0, flags, 0), 45, null);
            } catch (NullPointerException ex) {
            }
            return 1;
        }
    }

    public static class 打开商店 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <商店ID>");
                return 0;
            }
            MapleShopFactory.getInstance().getShop(Integer.parseInt(splitted[1]));
            return 1;
        }
    }

    public static class 清扫地面 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().getMap().removeDrops();
            return 1;
        }
    }

    public static class 获得技能 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <技能ID> (技能等级:默认1) (技能最高等级:默认1)");
                return 0;
            }
            Skill skill = SkillFactory.getSkill(Integer.parseInt(splitted[1]));
            byte level = (byte) CommandProcessorUtil.getOptionalIntArg(splitted, 2, 1);
            byte masterlevel = (byte) CommandProcessorUtil.getOptionalIntArg(splitted, 3, 1);

            if (level > skill.getMaxLevel()) {
                level = (byte) skill.getMaxLevel();
            }
            if (masterlevel > skill.getMaxLevel()) {
                masterlevel = (byte) skill.getMaxLevel();
            }
            c.getPlayer().changeSingleSkillLevel(skill, level, masterlevel);
            return 1;
        }
    }

    public static class 增加人气 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(6, splitted[0] + " <玩家名字> <数量>");
                return 0;
            }
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            int fame;
            try {
                fame = Integer.parseInt(splitted[2]);
            } catch (NumberFormatException nfe) {
                c.getPlayer().dropMessage(5, "数量无效...");
                return 0;
            }
            if (victim != null && player.allowedToTarget(victim)) {
                victim.addFame(fame);
                victim.updateSingleStat(MapleStat.人气, victim.getFame());
            }
            return 1;
        }
    }

    public static class 技能点 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <点数>");
                return 0;
            }
            c.getPlayer().setRemainingSp(CommandProcessorUtil.getOptionalIntArg(splitted, 1, 1));
            c.getPlayer().updateSingleStat(MapleStat.AVAILABLESP, 0);
            return 1;
        }
    }

    public static class 职业 extends CommandExecute {

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

    public static class 玩家转职 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(6, splitted[0] + " <玩家名字> <职业ID>");
                return 0;
            }
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (!MapleJob.isExist(Integer.parseInt(splitted[2]))) {
                c.getPlayer().dropMessage(5, "职业ID无效");
                return 0;
            }
            victim.changeJob((short) Integer.parseInt(splitted[2]), true);
            c.getPlayer().setSubcategory(c.getPlayer().getSubcategory());
            return 1;
        }
    }

    public static class 商店 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <商店ID>");
                return 0;
            }
            MapleShopFactory shop = MapleShopFactory.getInstance();
            int shopId = Integer.parseInt(splitted[1]);
            if (shop.getShop(shopId) != null) {
                shop.getShop(shopId).sendShop(c);
            }
            return 1;
        }
    }

    public static class 升级 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().levelUp();
            return 1;
        }
    }

    public static class 升级到 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <等级>");
                return 0;
            }
            //for (int i = 0; i < Integer.parseInt(splitted[1]) - c.getPlayer().getLevel(); i++) {
            while (c.getPlayer().getLevel() < Integer.parseInt(splitted[1])) {
                if (c.getPlayer().getLevel() < 255) {
                    c.getPlayer().levelUp();
                }
            }
            //}
            return 1;
        }
    }

    public static class 升级玩家到 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(6, splitted[0] + " <玩家名字> <等级>");
                return 0;
            }
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            //for (int i = 0; i < Integer.parseInt(splitted[2]) - victim.getLevel(); i++) {
            while (victim.getLevel() < Integer.parseInt(splitted[2])) {
                if (victim.getLevel() < 255) {
                    victim.levelUp();
                }
            }
            //}
            return 1;
        }
    }

    public static class 物品 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <物品ID>");
                return 0;
            }
            final int itemId = Integer.parseInt(splitted[1]);
            final short quantity = (short) CommandProcessorUtil.getOptionalIntArg(splitted, 2, 1);

            if (!c.getPlayer().isAdmin()) {
                for (int i : GameConstants.itemBlock) {
                    if (itemId == i) {
                        c.getPlayer().dropMessage(5, "你的管理员等级没有制作该物品的权限");
                        return 0;
                    }
                }
            }
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            if (!ii.itemExists(itemId)) {
                c.getPlayer().dropMessage(5, "物品(ID:" + itemId + ")不存在");
            } else {
                if (itemId < 5001000 && itemId >= 5000000) {
                    MapleInventoryManipulator.addById(c, itemId, (short) 1, "", MaplePet.createPet(itemId, MapleInventoryIdentifier.getInstance()), 45, c.getPlayer().getName() + " 使用 " + splitted[0] + " 命令制作");
                    return 1;
                }
                Item item;
                short flag = (short) ItemFlag.封印.getValue();

                if (GameConstants.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
                    item = ii.getEquipById(itemId);
                } else {
                    item = new Item(itemId, (byte) 0, quantity, (byte) 0);
                }
                if (!c.getPlayer().isSuperGM()) {
                    item.setFlag(flag);
                }
                item.setOwner(c.getPlayer().getName());
                item.setGMLog(c.getPlayer().getName() + " 使用 " + splitted[0] + " 命令制作");
                MapleInventoryManipulator.addbyItem(c, item);
            }
            return 1;
        }
    }

    public static class 等级 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <等级>");
                return 0;
            }
            c.getPlayer().setLevel(Short.parseShort(splitted[1]));
            c.getPlayer().updateSingleStat(MapleStat.等级, Integer.parseInt(splitted[1]));
            c.getPlayer().setExp(0);
            c.getPlayer().levelUp();
            /*if (c.getPlayer().getExp() < 0L) {
             c.getPlayer().gainExp(-(int) c.getPlayer().getExp(), false, false, true);
             }*/
            return 1;
        }
    }

    public static class 玩家等级 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(6, splitted[0] + " <玩家名字> <等级>");
                return 0;
            }
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            victim.setLevel(Short.parseShort(splitted[2]));
            victim.updateSingleStat(MapleStat.等级, Integer.parseInt(splitted[2]));
            victim.setExp(0);
            c.getPlayer().levelUp();
            /*if (victim.getExp() < 0L) {
             victim.gainExp(-(int) c.getPlayer().getExp(), false, false, true);
             }*/
            return 1;
        }
    }

    public static class 设置事件 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleEvent.onStartEvent(c.getPlayer());
            return 1;
        }
    }

    public static class 开始事件 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c.getChannelServer().getEvent() == c.getPlayer().getMapId()) {
                MapleEvent.setEvent(c.getChannelServer(), false);
                c.getPlayer().dropMessage(5, "Started the event and closed off");
                return 1;
            } else {
                c.getPlayer().dropMessage(5, "该命令必须在事件地图才能使用。");
                return 0;
            }
        }
    }

    public static class 事件 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <脚本名>");
                return 0;
            }
            final MapleEventType type = MapleEventType.getByString(splitted[1]);
            if (type == null) {
                final StringBuilder sb = new StringBuilder("Wrong syntax: ");
                for (MapleEventType t : MapleEventType.values()) {
                    sb.append(t.name()).append(",");
                }
                c.getPlayer().dropMessage(5, sb.toString().substring(0, sb.toString().length() - 1));
                return 0;
            }
            final String msg = MapleEvent.scheduleEvent(type, c.getChannelServer());
            if (msg.length() > 0) {
                c.getPlayer().dropMessage(5, msg);
                return 0;
            }
            return 1;
        }
    }

    public static class 清除物品 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(6, splitted[0] + " <玩家名字> <物品ID>");
                return 0;
            }
            MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (chr == null) {
                c.getPlayer().dropMessage(6, "玩家不存在");
                return 0;
            }
            chr.removeAll(Integer.parseInt(splitted[2]), false, false);
            c.getPlayer().dropMessage(6, "玩家 " + splitted[1] + " 所有的物品(ID:" + splitted[2] + ")已被清除");
            return 1;

        }
    }

    /*public static class 喇叭 extends CommandExecute {

     @Override
     public int execute(MapleClient c, String[] splitted) {
     if (splitted.length < 2) {
     c.getPlayer().dropMessage(6, splitted[0] + " <内容>");
     return 0;
     }
     World.Broadcast.broadcastSmega(MaplePacketCreator.broadcastMsg(3, c.getPlayer() == null ? c.getChannel() : c.getPlayer().getClient().getChannel(), c.getPlayer() == null ? c.getPlayer().getName() : c.getPlayer().getName() + " : " + StringUtil.joinStringFrom(splitted, 1), true));
     /*if (splitted.length < 2) {
     c.getPlayer().dropMessage(0, "!smega <itemid> <message>");
     return 0;
     }
     final List<String> lines = new LinkedList<>();
     for (int i = 0; i < 4; i++) {
     final String text = StringUtil.joinStringFrom(splitted, 2);
     if (text.length() > 55) {
     continue;
     }
     lines.add(text);
     }
     final boolean ear = true;
     World.Broadcast.broadcastSmega(MaplePacketCreator.getAvatarMega(c.getPlayer(), c.getChannel(), Integer.parseInt(splitted[1]), lines, ear)); */
    /*return 1;
     }
     }*/

    /*public static class 玩家喇叭 extends CommandExecute {

     @Override
     public int execute(MapleClient c, String[] splitted) {
     if (splitted.length < 3) {
     c.getPlayer().dropMessage(6, splitted[0] + " <玩家名字> <内容>");
     return 0;
     }
     MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
     if (victim == null) {
     c.getPlayer().dropMessage(5, "找不到玩家:" + splitted[1]);
     return 0;
     }
     World.Broadcast.broadcastSmega(MaplePacketCreator.broadcastMsg(3, victim.getClient().getChannel(), victim.getName() + " : " + StringUtil.joinStringFrom(splitted, 2), true));
     *//* 
     if (splitted.length < 2) {
     c.getPlayer().dropMessage(0, "!smega <itemid> <victim> <message>");
     return 0;
     }
     final List<String> lines = new LinkedList<>();
     for (int i = 0; i < 4; i++) {
     final String text = StringUtil.joinStringFrom(splitted, 3);
     if (text.length() > 55) {
     continue;
     }
     lines.add(text);
     }
     final boolean ear = true;
     World.Broadcast.broadcastSmega(MaplePacketCreator.getAvatarMega(victim, victim.getClient().getChannel(), Integer.parseInt(splitted[1]), lines, ear));
     */
    /*return 1;
     }
     }*/

    public static class 说话 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            for (MapleCharacter mch : c.getChannelServer().getPlayerStorage().getAllCharacters()) {
                if (mch == null) {
                    return 0;
                } else {
                    mch.getMap().broadcastMessage(MaplePacketCreator.getChatText(mch.getId(), StringUtil.joinStringFrom(splitted, 1), mch.isGM(), 0));
                }
            }
            return 1;
        }
    }

    public static class 玩家说话 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(6, splitted[0] + " <玩家名字> <内容>");
                return 0;
            }
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim == null) {
                c.getPlayer().dropMessage(5, "找不到玩家:" + splitted[1]);
                return 0;
            } else {
                victim.getMap().broadcastMessage(MaplePacketCreator.getChatText(victim.getId(), StringUtil.joinStringFrom(splitted, 2), victim.isGM(), 0));
            }
            return 1;
        }
    }

    public static class 给予地图状态 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            boolean 返回 = false;
            int type = 0;
            if (splitted.length < 2) {
                返回 = true;
            } else if (splitted[1].equalsIgnoreCase("封印")) {
                type = 120;
            } else if (splitted[1].equalsIgnoreCase("黑暗")) {
                type = 121;
            } else if (splitted[1].equalsIgnoreCase("虚弱")) {
                type = 122;
            } else if (splitted[1].equalsIgnoreCase("昏迷")) {
                type = 123;
            } else if (splitted[1].equalsIgnoreCase("诅咒")) {
                type = 124;
            } else if (splitted[1].equalsIgnoreCase("中毒")) {
                type = 125;
            } else if (splitted[1].equalsIgnoreCase("缓慢")) {
                type = 126;
            } else if (splitted[1].equalsIgnoreCase("诱惑")) { //24, 289 and 29 are cool.
                type = 128;
            } else if (splitted[1].equalsIgnoreCase("反向")) {
                type = 132;
            } else if (splitted[1].equalsIgnoreCase("不死化")) {
                type = 133;
            } else if (splitted[1].equalsIgnoreCase("无法使用药水")) {
                type = 134;
            } else if (splitted[1].equalsIgnoreCase("SHADOW")) {
                type = 135;
            } else if (splitted[1].equalsIgnoreCase("致盲")) {
                type = 136;
            } else if (splitted[1].equalsIgnoreCase("FREEZE")) {
                type = 137;
            } else if (splitted[1].equalsIgnoreCase("POTENTIAL")) {
                type = 138;
            } else if (splitted[1].equalsIgnoreCase("变身")) {
                type = 172;
            } else if (splitted[1].equalsIgnoreCase("龙卷风")) {
                type = 173;
            } else if (splitted[1].equalsIgnoreCase("旗帜")) {
                type = 799;
            } else {
                返回 = true;
            }
            if (返回) {
                c.getPlayer().dropMessage(6, splitted[0] + " <类型> (等级:默认1) where 类型 = 封印/黑暗/虚弱/昏迷/诅咒/中毒/缓慢/诱惑/反向/不死化/无法使用药水/SHADOW/致盲/FREEZE/POTENTIAL/变身/龙卷风/旗帜");
                return 0;
            }
            for (MapleCharacter mch : c.getChannelServer().getPlayerStorage().getAllCharacters()) {
                if (mch.getMapId() == c.getPlayer().getMapId()) {
                    if (mch == null) {
                        c.getPlayer().dropMessage(5, "未找到");
                        return 0;
                    }
                    mch.disease(type, CommandProcessorUtil.getOptionalIntArg(splitted, 2, 1));
                }
            }
            return 1;
        }
    }

    public static class 给予状态 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            boolean 返回 = false;
            int type = 0;
            if (splitted.length < 3) {
                返回 = true;
            } else if (splitted[2].equalsIgnoreCase("封印")) {
                type = 120;
            } else if (splitted[2].equalsIgnoreCase("黑暗")) {
                type = 121;
            } else if (splitted[2].equalsIgnoreCase("虚弱")) {
                type = 122;
            } else if (splitted[2].equalsIgnoreCase("昏迷")) {
                type = 123;
            } else if (splitted[2].equalsIgnoreCase("诅咒")) {
                type = 124;
            } else if (splitted[2].equalsIgnoreCase("中毒")) {
                type = 125;
            } else if (splitted[2].equalsIgnoreCase("缓慢")) {
                type = 126;
            } else if (splitted[2].equalsIgnoreCase("诱惑")) { //24, 289 and 29 are cool.
                type = 128;
            } else if (splitted[2].equalsIgnoreCase("反向")) {
                type = 132;
            } else if (splitted[2].equalsIgnoreCase("不死化")) {
                type = 133;
            } else if (splitted[2].equalsIgnoreCase("无法使用药水")) {
                type = 134;
            } else if (splitted[2].equalsIgnoreCase("SHADOW")) {
                type = 135;
            } else if (splitted[2].equalsIgnoreCase("致盲")) {
                type = 136;
            } else if (splitted[2].equalsIgnoreCase("FREEZE")) {
                type = 137;
            } else if (splitted[2].equalsIgnoreCase("DISABLE_POTENTIAL")) {
                type = 138;
            } else if (splitted[2].equalsIgnoreCase("变身")) {
                type = 172;
            } else if (splitted[2].equalsIgnoreCase("龙卷风")) {
                type = 173;
            } else if (splitted[2].equalsIgnoreCase("旗帜")) {
                type = 799;
            } else {
                返回 = true;
            }
            if (返回) {
                c.getPlayer().dropMessage(6, splitted[0] + " <玩家名字> <类型> (等级:默认1) where 类型 = 封印/黑暗/虚弱/昏迷/诅咒/中毒/缓慢/诱惑/反向/不死化/无法使用药水/SHADOW/致盲/FREEZE/DISABLE_POTENTIAL/变身/龙卷风/旗帜");
                return 0;
            }
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim == null) {
                c.getPlayer().dropMessage(5, "未找到");
                return 0;
            }
            victim.disease(type, CommandProcessorUtil.getOptionalIntArg(splitted, 3, 1));
            return 1;
        }
    }

    /*public static class 克隆我 extends CommandExecute {

     @Override
     public int execute(MapleClient c, String[] splitted) {
     c.getPlayer().cloneLook();
     return 1;
     }
     }

     public static class 清除克隆 extends CommandExecute {

     @Override
     public int execute(MapleClient c, String[] splitted) {
     c.getPlayer().dropMessage(6, c.getPlayer().getCloneSize() + "个克隆人被清除了");
     c.getPlayer().disposeClones();
     return 1;
     }
     }*/

    /*public static class 设置事件实例属性 extends CommandExecute {

     @Override
     public int execute(MapleClient c, String[] splitted) {
     EventManager em = c.getChannelServer().getEventSM().getEventManager(splitted[1]);
     if (em == null || em.getInstances().size() <= 0) {
     c.getPlayer().dropMessage(5, "事件实例不存在。");
     } else {
     em.setProperty(splitted[2], splitted[3]);
     for (EventInstanceManager eim : em.getInstances()) {
     eim.setProperty(splitted[2], splitted[3]);
     }
     }
     return 1;
     }
     }

     public static class 事件实例属性 extends CommandExecute {

     @Override
     public int execute(MapleClient c, String[] splitted) {
     EventManager em = c.getChannelServer().getEventSM().getEventManager(splitted[1]);
     if (em == null || em.getInstances().size() <= 0) {
     c.getPlayer().dropMessage(5, "none");
     } else {
     for (EventInstanceManager eim : em.getInstances()) {
     c.getPlayer().dropMessage(5, "Event " + eim.getName() + ", eventManager: " + em.getName() + " iprops: " + eim.getProperty(splitted[2]) + ", eprops: " + em.getProperty(splitted[2]));
     }
     }
     return 0;
     }
     }*/
    public static class 离开事件实例 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().getEventInstance() == null) {
                c.getPlayer().dropMessage(5, "你没有在事件实例里面。");
            } else {
                c.getPlayer().getEventInstance().unregisterPlayer(c.getPlayer());
            }
            return 1;
        }
    }

    public static class 地图上的玩家 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            StringBuilder builder = new StringBuilder("地图上的玩家: 总共").append(c.getPlayer().getMap().getCharactersThreadsafe().size()).append("个, ");
            for (MapleCharacter chr : c.getPlayer().getMap().getCharactersThreadsafe()) {
                if (builder.length() > 150) { // wild guess :o
                    builder.setLength(builder.length() - 2);
                    c.getPlayer().dropMessage(6, builder.toString());
                    builder = new StringBuilder();
                }
                builder.append(MapleCharacterUtil.makeMapleReadable(chr.getName()));
                builder.append(", ");
            }
            builder.setLength(builder.length() - 2);
            c.getPlayer().dropMessage(6, builder.toString());
            return 1;
        }
    }

    /*public static class 开始事件实例 extends CommandExecute {

     @Override
     public int execute(MapleClient c, String[] splitted) {
     if (c.getPlayer().getEventInstance() != null) {
     c.getPlayer().dropMessage(5, "你已经在一个事件实例里了。");
     } else if (splitted.length > 2) {
     EventManager em = c.getChannelServer().getEventSM().getEventManager(splitted[1]);
     if (em == null || em.getInstance(splitted[2]) == null) {
     c.getPlayer().dropMessage(5, "不存在。");
     } else {
     em.getInstance(splitted[2]).registerPlayer(c.getPlayer());
     }
     } else {
     c.getPlayer().dropMessage(5, splitted[0] + " [eventmanager] [eventinstance]");
     }
     return 1;

     }
     }*/
    public static class 重置怪物 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().getMap().killAllMonsters(false);
            return 1;
        }
    }

    public static class 杀死OID怪物 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <怪物OID>");
                return 0;
            }
            MapleMap map = c.getPlayer().getMap();
            int targetId = Integer.parseInt(splitted[1]);
            MapleMonster monster = map.getMonsterByOid(targetId);
            if (monster != null) {
                map.killMonster(monster, c.getPlayer(), false, false, (byte) 1);
            }
            return 1;
        }
    }

    public static class 重载NPC extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().getMap().resetNPCs();
            return 1;
        }
    }

    public static class 聊天公告 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            for (MapleCharacter all : c.getChannelServer().getPlayerStorage().getAllCharacters()) {
                all.dropMessage(-6, StringUtil.joinStringFrom(splitted, 1));
            }
            return 1;
        }
    }

    public static class 公告事项 extends CommandExecute {

        protected static int getNoticeType(String typestring) {
            switch (typestring) {
                case "1":
                    return -1;
                case "2":
                    return -2;
                case "3":
                    return -3;
                case "4":
                    return -4;
                case "5":
                    return -5;
                case "6":
                    return -6;
                case "7":
                    return -7;
                case "8":
                    return -8;
                case "n":
                    return 0;
                case "p":
                    return 1;
                case "l":
                    return 2;
                case "nv":
                    return 5;
                case "v":
                    return 5;
                case "b":
                    return 6;
            }
            return -1;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " (对象:默认w) (类型:默认0) <公告内容>");
                c.getPlayer().dropMessage(6, splitted[0] + "对象:地图所有人 - m/频道所有人 - c/服务器所有人 - w");
                c.getPlayer().dropMessage(6, splitted[0] + "类型:1/2/3/4/5/6/7/8/n/弹窗 - p/小喇叭 - l/红字[公告事项] - nv/红字 - v/无[公告事项] - b");
                return 0;
            }
            int joinmod = 1;
            int range = -1;
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
            int tfrom = 2;
            if (range == -1) {
                range = 2;
                tfrom = 1;
            }
            int type = getNoticeType(splitted[tfrom]);
            if (type == -1) {
                joinmod = 0;
            }
            StringBuilder sb = new StringBuilder();
            if (splitted[tfrom].equals("nv")) {
                sb.append("[公告事项]");
            } else {
                sb.append("");
            }
            joinmod += tfrom;
            sb.append(StringUtil.joinStringFrom(splitted, joinmod));

            byte[] packet = MaplePacketCreator.serverMessageNotice(sb.toString());
            if (range == 0) {
                c.getPlayer().getMap().broadcastMessage(packet);
            } else if (range == 1) {
                ChannelServer.getInstance(c.getChannel()).broadcastPacket(packet);
            } else if (range == 2) {
                WorldBroadcastService.getInstance().broadcastMessage(packet);
            }
            return 1;
        }
    }

    public static class 黄字事项 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " (对象:默认w) <内容>");
                c.getPlayer().dropMessage(6, splitted[0] + "对象:地图所有人 - m/频道所有人 - c/服务器所有人 - w");
                return 0;
            }
            int range = -1;
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
            int tfrom = 2;
            if (range == -1) {
                range = 2;
                tfrom = 1;
            }
            byte[] packet = MaplePacketCreator.yellowChat((splitted[0].equals("!带名黄字事项") ? ("[" + c.getPlayer().getName() + "] ") : "") + StringUtil.joinStringFrom(splitted, tfrom));
            if (range == 0) {
                c.getPlayer().getMap().broadcastMessage(packet);
            } else if (range == 1) {
                ChannelServer.getInstance(c.getChannel()).broadcastPacket(packet);
            } else if (range == 2) {
                WorldBroadcastService.getInstance().broadcastMessage(packet);
            }
            return 1;
        }
    }

    public static class 带名黄字事项 extends 黄字事项 {
    }

    public static class 我的IP extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().dropMessage(5, "IP: " + c.getSession().getRemoteAddress().toString().split(":")[0]);
            return 1;
        }
    }
    public static class 切换爆物 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().getMap().toggleDrops();
            return 1;
        }
    }

    public static class 监狱 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(6, splitted[0] + " <玩家名字> <时间(分钟,0为永久)>");
                return 0;
            }
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            final int minutes = Math.max(0, Integer.parseInt(splitted[2]));
            if (victim != null && c.getPlayer().getGMLevel() >= victim.getGMLevel()) {
                MapleMap target = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(GameConstants.JAIL);
                victim.getQuestNAdd(MapleQuest.getInstance(GameConstants.JAIL_QUEST)).setCustomData(String.valueOf(minutes * 60));
                victim.changeMap(target, target.getPortal(0));
            } else {
                c.getPlayer().dropMessage(6, "请到玩家所在的频道");
                return 0;
            }
            return 1;
        }
    }

    public static class 查看NPC extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            for (MapleMapObject reactor1l : c.getPlayer().getMap().getAllNPCsThreadsafe()) {
                MapleNPC reactor2l = (MapleNPC) reactor1l;
                c.getPlayer().dropMessage(5, "NPC: oID: " + reactor2l.getObjectId() + " npcID: " + reactor2l.getId() + " 坐标: " + reactor2l.getPosition().toString() + " 名字: " + reactor2l.getName());
            }
            return 0;
        }
    }

    public static class 查看反应堆 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            for (MapleMapObject reactor1l : c.getPlayer().getMap().getAllReactorsThreadsafe()) {
                MapleReactor reactor2l = (MapleReactor) reactor1l;
                c.getPlayer().dropMessage(5, "反应堆Reactor: oID: " + reactor2l.getObjectId() + " 反应堆ReactorID: " + reactor2l.getReactorId() + " 坐标: " + reactor2l.getPosition().toString() + " 名字: " + reactor2l.getState() + " Name: " + reactor2l.getName());
            }
            return 0;
        }
    }

    public static class 查看传送点 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            for (MaplePortal portal : c.getPlayer().getMap().getPortals()) {
                c.getPlayer().dropMessage(5, "传送点Portal: ID: " + portal.getId() + " 脚本: " + portal.getScriptName() + " 名字: " + portal.getName() + " 坐标: " + portal.getPosition().x + "," + portal.getPosition().y + " 目标地图: " + portal.getTargetMapId() + " / " + portal.getTarget());
            }
            return 0;
        }
    }

    public static class 我的位置 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            Point pos = c.getPlayer().getPosition();
            c.getPlayer().dropMessage(6, "X: " + pos.x + " | Y: " + pos.y + " | RX0: " + (pos.x + 50) + " | RX1: " + (pos.x - 50) + " | FH: " + c.getPlayer().getFH());
            return 1;
        }
    }

    public static class 字母 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(6, splitted[0] + " <颜色 (绿/红)> <单词>");
                return 0;
            }
            int start;
            int nstart;
            if (splitted[1].equalsIgnoreCase("绿")) {
                start = 3991026;
                nstart = 3990019;
            } else if (splitted[1].equalsIgnoreCase("红")) {
                start = 3991000;
                nstart = 3990009;
            } else {
                c.getPlayer().dropMessage(6, "未知颜色");
                return 0;
            }
            String splitString = StringUtil.joinStringFrom(splitted, 2);
            List<Integer> chars = new ArrayList();
            splitString = splitString.toUpperCase();

            for (int i = 0; i < splitString.length(); ++i) {
                char chr = splitString.charAt(i);
                if (chr == ' ') {
                    chars.add(-1);
                } else if ((chr >= 'A') && (chr <= 'Z')) {
                    chars.add(Integer.valueOf(chr));
                } else if ((chr >= '0') && (chr <= '9')) {
                    chars.add(chr + 200);
                }
            }
            int w = 32;
            int dStart = c.getPlayer().getPosition().x - (splitString.length() / 2 * 32);
            for (Integer i : chars) {
                if (i == -1) {
                    dStart += 32;
                } else {
                    int val;
                    Item item;
                    if (i < 200) {
                        val = start + i - 65;
                        item = new Item(val, (byte) 0, (short) 1);
                        c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), item, new Point(dStart, c.getPlayer().getPosition().y), false, false);
                        dStart += 32;
                    } else if ((i >= 200) && (i <= 300)) {
                        val = nstart + i - 48 - 200;
                        item = new Item(val, (byte) 0, (short) 1);
                        c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), item, new Point(dStart, c.getPlayer().getPosition().y), false, false);
                        dStart += 32;
                    }
                }
            }
            return 1;
        }
    }

    public static class 召唤 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <怪物ID> (数量:默认1)");
                return 0;
            }
            final int mid = Integer.parseInt(splitted[1]);
            final int num = Math.min(CommandProcessorUtil.getOptionalIntArg(splitted, 2, 1), 500);
            Integer level = CommandProcessorUtil.getNamedIntArg(splitted, 1, "lvl");
            Long hp = CommandProcessorUtil.getNamedLongArg(splitted, 1, "hp");
            Integer exp = CommandProcessorUtil.getNamedIntArg(splitted, 1, "exp");
            Double php = CommandProcessorUtil.getNamedDoubleArg(splitted, 1, "php");
            Double pexp = CommandProcessorUtil.getNamedDoubleArg(splitted, 1, "pexp");

            MapleMonster onemob;
            try {
                onemob = MapleLifeFactory.getMonster(mid);
            } catch (RuntimeException e) {
                c.getPlayer().dropMessage(5, "错误: " + e.getMessage());
                return 0;
            }
            if (onemob == null) {
                c.getPlayer().dropMessage(5, "怪物不存在");
                return 0;
            }
            int newhp;
            int newexp;
            if (hp != null) {
                newhp = hp.intValue();
            } else if (php != null) {
                newhp = (int) (onemob.getMobMaxHp() * (php / 100));
            } else {
                newhp = (int) onemob.getMobMaxHp();
            }
            if (exp != null) {
                newexp = exp;
            } else if (pexp != null) {
                newexp = (int) (onemob.getMobExp() * (pexp / 100));
            } else {
                newexp = onemob.getMobExp();
            }
            if (newhp < 1) {
                newhp = 1;
            }

            final OverrideMonsterStats overrideStats = new OverrideMonsterStats(newhp, onemob.getMobMaxMp(), newexp, false);
            for (int i = 0; i < num; i++) {
                MapleMonster mob = MapleLifeFactory.getMonster(mid);
                mob.setHp(newhp);
                if (level != null) {
                    mob.changeLevel(level, false);
                } else {
                    mob.setOverrideStats(overrideStats);
                }
                c.getPlayer().getMap().spawnMonsterOnGroundBelow(mob, c.getPlayer().getPosition());
            }
            return 1;
        }
    }

    public static class 召唤怪物 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <怪物名称> (数量:默认1)");
                return 0;
            }
            final String mname = splitted[1];
            final int num = Math.min(CommandProcessorUtil.getOptionalIntArg(splitted, 2, 1), 500);
            int mid = 0;
            for (Map.Entry<Integer, String> mob : MapleMonsterInformationProvider.getInstance().getAllMonsters().entrySet()) {
                if (mob.getValue().toLowerCase().equals(mname.toLowerCase())) {
                    mid = mob.getKey();
                    break;
                }
            }

            MapleMonster onemob;
            try {
                onemob = MapleLifeFactory.getMonster(mid);
            } catch (RuntimeException e) {
                c.getPlayer().dropMessage(5, "错误: " + e.getMessage());
                return 0;
            }
            if (onemob == null) {
                c.getPlayer().dropMessage(5, "怪物不存在");
                return 0;
            }
            for (int i = 0; i < num; i++) {
                MapleMonster mob = MapleLifeFactory.getMonster(mid);
                c.getPlayer().getMap().spawnMonsterOnGroundBelow(mob, c.getPlayer().getPosition());
            }
            return 1;
        }
    }

    public static class 禁言 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String splitted[]) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <玩家名字>");
                return 0;
            }
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            victim.canTalk(false);
            return 1;
        }
    }

    public static class 取消禁言 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String splitted[]) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <玩家名字>");
                return 0;
            }
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            victim.canTalk(true);
            return 1;
        }
    }

    public static class 地图禁言 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String splitted[]) {
            for (MapleCharacter chr : c.getPlayer().getMap().getCharactersThreadsafe()) {
                chr.canTalk(false);
            }
            return 1;
        }
    }

    public static class 取消地图禁言 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String splitted[]) {
            for (MapleCharacter chr : c.getPlayer().getMap().getCharactersThreadsafe()) {
                chr.canTalk(true);
            }
            return 1;
        }
    }

    public static class 无敌 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            if (player.isInvincible()) {
                player.setInvincible(false);
                player.dropMessage(6, "无敌模式已关闭。");
            } else {
                player.setInvincible(true);
                player.dropMessage(6, "无敌模式已开启。");
            }
            return 1;
        }
    }

    public static class 封印物品 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(6, splitted[0] + " <玩家名字> <物品ID>");
                return 0;
            }
            MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (chr == null) {
                c.getPlayer().dropMessage(6, "输入的角色不存在或者角色不在线或者不在这个频道。");
                return 0;
            }
            int itemid = Integer.parseInt(splitted[2]);
            MapleInventoryType type = ItemConstants.getInventoryType(itemid);
            for (Item item : chr.getInventory(type).listById(itemid)) {
                item.setFlag((short) (byte) (item.getFlag() | ItemFlag.封印.getValue()));
                chr.forceUpdateItem(item);
            }
            if (type == MapleInventoryType.EQUIP) {
                type = MapleInventoryType.EQUIPPED;
                for (Item item : chr.getInventory(type).listById(itemid)) {
                    item.setFlag((short) (byte) (item.getFlag() | ItemFlag.封印.getValue()));
                    chr.forceUpdateItem(item);
                }
            }
            c.getPlayer().dropMessage(6, "已经成功的将ID为：" + splitted[2] + " 的所有道具锁定，执行角色为: " + splitted[1] + "。");
            return 1;
        }
    }

    public static class 线上玩家 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            int total = 0;
            c.getPlayer().dropMessage(6, "---------------------------------------------------------------------------------------");
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                int curConnected = cserv.getConnectedClients();
                c.getPlayer().dropMessage(6, new StringBuilder().append("频道: ").append(cserv.getChannel()).append(" 在线人数: ").append(curConnected).toString());
                total += curConnected;
                for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                    if (chr != null && c.getPlayer().getGMLevel() >= chr.getGMLevel()) {
                        StringBuilder ret = new StringBuilder();
                        ret.append(StringUtil.getRightPaddedStr(chr.getName(), ' ', 13));
                        ret.append(" ID: ");
                        ret.append(chr.getId());
                        ret.append(" 等级: ");
                        ret.append(StringUtil.getRightPaddedStr(String.valueOf(chr.getLevel()), ' ', 3));
                        if (chr.getMap() != null) {
                            ret.append(" 地图: ");
                            ret.append(chr.getMapId());
                            ret.append(" - ");
                            ret.append(chr.getMap().getMapName());
                        }
                        c.getPlayer().dropMessage(6, ret.toString());
                    }
                }
            }
            c.getPlayer().dropMessage(6, new StringBuilder().append("当前服务器总计在线: ").append(total).toString());
            c.getPlayer().dropMessage(6, "---------------------------------------------------------------------------------------");
            return 1;
        }
    }
}
