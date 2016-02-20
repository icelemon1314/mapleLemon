package client.messages.commands;

import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import client.MapleJob;
import client.MapleStat;
import client.Skill;
import client.SkillFactory;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.messages.CommandProcessor;
import client.messages.CommandProcessorUtil;
import client.messages.PlayerGMRank;
import constants.GameConstants;
import handling.RecvPacketOpcode;
import handling.SendPacketOpcode;
import handling.channel.ChannelServer;
import handling.world.CheaterData;
import handling.world.World;
import handling.world.WorldBroadcastService;
import handling.world.WorldFindService;
import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import scripting.npc.NPCScriptManager;
import server.ItemInformation;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MaplePortal;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.quest.MapleQuest;
import server.shop.MapleShopFactory;
import server.squad.MapleSquadType;
import tools.HexTool;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.StringUtil;
import tools.packet.InventoryPacket;
import tools.packet.NPCPacket;

/**
 *
 * @author Emilyx3
 */
public class InternCommand {

    public static PlayerGMRank getPlayerLevelRequired() {
        return PlayerGMRank.INTERN;
    }

    public static class 隐身 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            Skill skill = SkillFactory.getSkill(9001004);
            Skill skill2 = SkillFactory.getSkill(1010);
            if (c.getPlayer().isHidden()) {
                c.getPlayer().cancelEffect(skill.getEffect(1), false, -1L);
                c.getPlayer().cancelEffect(skill2.getEffect(1), false, -1L);
                c.getPlayer().dropMessage(6, "隐身模式已关闭。");
            } else {
                skill.getEffect(1).applyTo(c.getPlayer());
                skill2.getEffect(1).applyTo(c.getPlayer());
                c.getPlayer().dropMessage(6, "隐身模式已开启。");
            }
            return 0;
        }
    }

    public static class 治愈 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().getStat().heal(c.getPlayer());
            c.getPlayer().dispelDebuffs();
            return 0;
        }
    }

    public static class 地图治愈 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            for (MapleCharacter mch : player.getMap().getCharacters()) {
                if (mch != null) {
                    mch.getStat().heal(mch);
                    mch.dispelDebuffs();
                }
            }
            return 1;
        }
    }


    public static class 踢 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <玩家名字> ([玩家名字] [玩家名字]...)");
                return 0;
            }
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[splitted.length - 1]);
            if (victim != null && c.getPlayer().getGMLevel() >= victim.getGMLevel()) {
                victim.getClient().getSession().close(true);
                victim.getClient().disconnect(true, false);
                return 1;
            } else {
                c.getPlayer().dropMessage(6, "受害者不存在");
                return 0;
            }
        }
    }

    public static class 杀死 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <玩家名字> ([玩家名字] [玩家名字]...)");
                return 0;
            }
            MapleCharacter player = c.getPlayer();
            MapleCharacter victim = null;
            for (int i = 1; i < splitted.length; i++) {
                try {
                    victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[i]);
                } catch (Exception e) {
                    c.getPlayer().dropMessage(6, "没找到玩家 " + splitted[i]);
                }
                if (victim != null && player.allowedToTarget(victim) && player.getGMLevel() >= victim.getGMLevel()) {
                    victim.getStat().setMp((short) 0);
                    victim.getStat().setHp((short) 0);
                    victim.updateSingleStat(MapleStat.HP, victim.getStat().getHp());
                    victim.updateSingleStat(MapleStat.MP, victim.getStat().getMp());
                }
            }
            return 1;
        }
    }

    public static class 我在哪 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().dropMessage(5, "你在地图(ID:" + c.getPlayer().getMap().getId() + ")");
            return 1;
        }
    }

    public static class 线上 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            String online = "";
            for (int i = 1; i <= ChannelServer.getChannelCount(); i++) {
                online += ChannelServer.getInstance(i).getPlayerStorage().getOnlinePlayers(true);
            }
            c.getPlayer().dropMessage(6, online);
            return 1;
        }
    }

    public static class 去往 extends CommandExecute {

        private static final HashMap<String, Integer> gotomaps = new HashMap<>();

        static {
            gotomaps.put("专业技术村庄", 910001000);
            gotomaps.put("阿里安特", 260000100);
            gotomaps.put("彩虹岛", 1010000);
            gotomaps.put("婚礼村", 680000000);
            gotomaps.put("蔚蓝道路", 860000000);
            gotomaps.put("水下世界", 230000000);
            gotomaps.put("驳船码头", 541000000);
//            gotomaps.put("cwk", 610030000);
            gotomaps.put("埃德尔斯坦", 310000000);
            gotomaps.put("艾琳森林", 300000000);
            gotomaps.put("魔法密林", 101000000);
            gotomaps.put("艾利涅胡", 101071300);
            gotomaps.put("埃欧雷", 101050000);
            gotomaps.put("冰峰雪域", 211000000);
            gotomaps.put("圣地", 130000000);
//            gotomaps.put("florina", 120000300);
            gotomaps.put("自由市场", 910000000);
            gotomaps.put("未来之门", 271000000);
            gotomaps.put("工作场所", 180000000);
            gotomaps.put("幸福村", 209000000);
            gotomaps.put("明珠港", 104000000);
            gotomaps.put("射手村", 100000000);
            gotomaps.put("百草堂", 251000000);
            gotomaps.put("甘榜村", 551000000);
            gotomaps.put("废弃都市", 103000000);
//            gotomaps.put("korean", 222000000);
            gotomaps.put("神木村", 240000000);
            gotomaps.put("玩具城", 220000000);
            gotomaps.put("马来西亚", 550000000);
            gotomaps.put("武陵", 250000000);
            gotomaps.put("诺特勒斯", 120000000);
            gotomaps.put("新野城", 600000000);
//            gotomaps.put("omega", 221000000);
            gotomaps.put("天空之城", 200000000);
            gotomaps.put("万神殿", 400000000);
            gotomaps.put("品克缤", 270050100);
            gotomaps.put("神的黄昏", 270050100);
//            gotomaps.put("phantom", 610010000);
            gotomaps.put("勇士部落", 102000000);
            gotomaps.put("里恩", 140000000);
            gotomaps.put("昭和村", 801000000);
            gotomaps.put("新加坡", 540000000);
            gotomaps.put("六岔路口", 104020000);
            gotomaps.put("林中之城", 105000000);
            gotomaps.put("南港", 2000000);
            gotomaps.put("大树口村", 866000000);
            gotomaps.put("时间神殿", 270000000);
            gotomaps.put("三个门", 270000000);
            gotomaps.put("黄昏勇士部落", 273000000);
            gotomaps.put("克林逊森林城堡", 301000000);
            gotomaps.put("城堡顶端", 301000000);
            gotomaps.put("皮亚奴斯", 230040420);
            gotomaps.put("皮亚奴斯洞穴", 230040420);
            gotomaps.put("黑龙", 240060200);
            gotomaps.put("暗黑龙王洞穴", 240060200);
            gotomaps.put("进阶黑龙", 240060201);
            gotomaps.put("进阶暗黑龙王洞穴", 240060201);
            gotomaps.put("天鹰", 240020101);
            gotomaps.put("格瑞芬多森林", 240020101);
            gotomaps.put("火焰龙", 240020401);
            gotomaps.put("喷火龙栖息地", 240020401);
            gotomaps.put("扎昆", 280030100);
            gotomaps.put("扎昆的祭台", 280030100);
            gotomaps.put("进阶扎昆", 280030000);
            gotomaps.put("进阶扎昆的祭台", 280030000);
            gotomaps.put("闹钟", 220080001);
            gotomaps.put("帕普拉图斯", 220080001);
            gotomaps.put("时间塔的本源", 220080001);
            gotomaps.put("OX问答", 109020001);
            gotomaps.put("上楼", 109030101);
            gotomaps.put("向高地", 109040000);
            gotomaps.put("雪球赛", 109060000);
            gotomaps.put("江户村", 800000000);
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <地图名>");
            } else {
                if (gotomaps.containsKey(splitted[1])) {
                    MapleMap target = c.getChannelServer().getMapFactory().getMap(gotomaps.get(splitted[1]));
                    if (target == null) {
                        c.getPlayer().dropMessage(6, "地图不存在");
                        return 0;
                    }
                    MaplePortal targetPortal = target.getPortal(0);
                    c.getPlayer().changeMap(target, targetPortal);
                } else {
                    if (splitted[1].equals("列表")) {
                        c.getPlayer().dropMessage(6, "地图列表: ");
                        StringBuilder sb = new StringBuilder();
                        for (String s : gotomaps.keySet()) {
                            sb.append(s).append(", ");
                        }
                        c.getPlayer().dropMessage(6, sb.substring(0, sb.length() - 2));
                    } else {
                        c.getPlayer().dropMessage(6, "命令错误: " + splitted[0] + " <地图名> 你可以使用 " + splitted[0] + " 列表 来获取可用地图列表");
                    }
                }
            }
            return 1;
        }
    }

    public static class 时钟 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " (时间:默认60秒)");
            }
            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.getClock(CommandProcessorUtil.getOptionalIntArg(splitted, 1, 60)));
            return 1;
        }
    }

    public static class 传送到这里 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <玩家名字>");
                return 0;
            }
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim != null) {
                if ((!c.getPlayer().isGM() && (victim.isInBlockedMap() || victim.isGM()))) {
                    c.getPlayer().dropMessage(5, "请稍后再试");
                    return 0;
                }
                victim.changeMap(c.getPlayer().getMap(), c.getPlayer().getMap().findClosestPortal(c.getPlayer().getTruePosition()));
            } else {
                int ch = WorldFindService.getInstance().findChannel(splitted[1]);
                if (ch < 0) {
                    c.getPlayer().dropMessage(5, "未找到");
                    return 0;
                }
                victim = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim == null  || (!c.getPlayer().isGM() && (victim.isInBlockedMap() || victim.isGM()))) {
                    c.getPlayer().dropMessage(5, "请稍后再试");
                    return 0;
                }
                c.getPlayer().dropMessage(5, "受害者正在改变频道");
                victim.dropMessage(5, "正在改变频道");
                victim.changeChannel(c.getChannel());
                if (victim.getMapId() != c.getPlayer().getMapId()) {
                    final MapleMap mapp = victim.getClient().getChannelServer().getMapFactory().getMap(c.getPlayer().getMapId());
                    victim.changeMap(mapp, mapp.findClosestPortal(c.getPlayer().getTruePosition()));
                }
            }
            return 1;
        }
    }

    public static class 传送 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0]);
                c.getPlayer().dropMessage(6, "用法一:(要传送的玩家名字) <地图ID> (portalID:默认无)");
                c.getPlayer().dropMessage(6, "用法二:<要传送到的玩家名字>");
                return 0;
            }
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim != null && c.getPlayer().getGMLevel() >= victim.getGMLevel()) {
                if (splitted.length == 2) {
                    c.getPlayer().changeMap(victim.getMap(), victim.getMap().findClosestSpawnpoint(victim.getTruePosition()));
                } else {
                    MapleMap target = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(Integer.parseInt(splitted[2]));
                    if (target == null) {
                        c.getPlayer().dropMessage(6, "地图不存在");
                        return 0;
                    }
                    MaplePortal targetPortal = null;
                    if (splitted.length > 3) {
                        try {
                            targetPortal = target.getPortal(Integer.parseInt(splitted[3]));
                        } catch (IndexOutOfBoundsException e) {
                            // noop, assume the gm didn't know how many portals there are
                            c.getPlayer().dropMessage(5, "portal选择无效");
                        } catch (NumberFormatException a) {
                            // noop, assume that the gm is drunk
                        }
                    }
                    if (targetPortal == null) {
                        targetPortal = target.getPortal(0);
                    }
                    victim.changeMap(target, targetPortal);
                }
            } else {
                try {
                    int ch = WorldFindService.getInstance().findChannel(splitted[1]);
                    if (ch < 0) {
                        MapleMap target = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(splitted[1]));
                        if (target == null) {
                            c.getPlayer().dropMessage(6, "地图不存在");
                            return 0;
                        }
                        MaplePortal targetPortal = null;
                        if (splitted.length > 2) {
                            try {
                                targetPortal = target.getPortal(Integer.parseInt(splitted[2]));
                            } catch (IndexOutOfBoundsException e) {
                                // noop, assume the gm didn't know how many portals there are
                                c.getPlayer().dropMessage(5, "portal选择无效");
                            } catch (NumberFormatException a) {
                                // noop, assume that the gm is drunk
                            }
                        }
                        if (targetPortal == null) {
                            targetPortal = target.getPortal(0);
                        }
                        c.getPlayer().changeMap(target, targetPortal);
                    } else {
                        victim = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(splitted[1]);
                        c.getPlayer().dropMessage(6, "正在改变频道, 请稍候");
                        if (victim.getMapId() != c.getPlayer().getMapId()) {
                            final MapleMap mapp = c.getChannelServer().getMapFactory().getMap(victim.getMapId());
                            c.getPlayer().changeMap(mapp, mapp.findClosestPortal(victim.getTruePosition()));
                        }
                        c.getPlayer().changeChannel(ch);
                    }
                } catch (NumberFormatException e) {
                    c.getPlayer().dropMessage(6, "出现错误: " + e.getMessage());
                    return 0;
                }
            }
            return 1;
        }
    }

    public static class 查找 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length == 1) {
                c.getPlayer().dropMessage(6, splitted[0] + ": <类型> <搜索信息>");
                c.getPlayer().dropMessage(6, "类型:NPC/怪物/物品/地图/技能/任务/包头");
            } else if (splitted.length == 2) {
                c.getPlayer().dropMessage(6, "请提供搜索信息");
            } else {
                String type = splitted[1];
                String search = StringUtil.joinStringFrom(splitted, 2);
                MapleData data;
                MapleDataProvider dataProvider = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath", "wz") + "/String.wz"));
                StringBuilder sb = new StringBuilder();
                sb.append("<<" + "类型: ").append(type).append(" | " + "搜索信息: ").append(search).append(">>");

                if (type.equalsIgnoreCase("NPC")) {
                    List<String> retNpcs = new ArrayList<>();
                    data = dataProvider.getData("Npc.img");
                    List<Pair<Integer, String>> npcPairList = new LinkedList<>();
                    for (MapleData npcIdData : data.getChildren()) {
                        npcPairList.add(new Pair<>(Integer.parseInt(npcIdData.getName()), MapleDataTool.getString(npcIdData.getChildByPath("name"), "无名字")));
                    }
                    for (Pair<Integer, String> npcPair : npcPairList) {
                        if (npcPair.getRight().toLowerCase().contains(search.toLowerCase())) {
                            retNpcs.add("\r\n" + npcPair.getLeft() + " - " + npcPair.getRight());
                        }
                    }
                    if (retNpcs.size() > 0) {
                        for (String singleRetNpc : retNpcs) {
                            if (sb.length() > 10000) {
                                sb.append("\r\n后面还有很多搜索结果, 但已经无法显示更多");
                                break;
                            }
                            sb.append(singleRetNpc);
                            //c.getSession().write(NPCPacket.getNPCTalk(9010000, (byte) 0, retNpcs.toString(), "00 00", (byte) 0, 9010000));
                            //c.getPlayer().dropMessage(6, singleRetNpc);
                        }
                    } else {
                        c.getPlayer().dropMessage(6, "该NPC信息搜索不到");
                    }

                } else if (type.equalsIgnoreCase("地图")) {
                    List<String> retMaps = new ArrayList<>();
                    data = dataProvider.getData("Map.img");
                    List<Pair<Integer, String>> mapPairList = new LinkedList<>();
                    for (MapleData mapAreaData : data.getChildren()) {
                        for (MapleData mapIdData : mapAreaData.getChildren()) {
                            mapPairList.add(new Pair<>(Integer.parseInt(mapIdData.getName()), MapleDataTool.getString(mapIdData.getChildByPath("streetName"), "NO-NAME") + " - " + MapleDataTool.getString(mapIdData.getChildByPath("mapName"), "NO-NAME")));
                        }
                    }
                    for (Pair<Integer, String> mapPair : mapPairList) {
                        if (mapPair.getRight().toLowerCase().contains(search.toLowerCase())) {
                            retMaps.add("\r\n" + mapPair.getLeft() + " - " + mapPair.getRight());
                        }
                    }
                    if (retMaps.size() > 0) {
                        for (String singleRetMap : retMaps) {
                            if (sb.length() > 10000) {
                                sb.append("\r\n后面还有很多搜索结果, 但已经无法显示更多");
                                break;
                            }
                            sb.append(singleRetMap);
                            //c.getSession().write(NPCPacket.getNPCTalk(9010000, (byte) 0, retMaps.toString(), "00 00", (byte) 0, 9010000));
                            //c.getPlayer().dropMessage(6, singleRetMap);
                        }
                    } else {
                        c.getPlayer().dropMessage(6, "该地图信息搜索不到");
                    }
                } else if (type.equalsIgnoreCase("怪物")) {
                    List<String> retMobs = new ArrayList<>();
                    data = dataProvider.getData("Mob.img");
                    List<Pair<Integer, String>> mobPairList = new LinkedList<>();
                    for (MapleData mobIdData : data.getChildren()) {
                        mobPairList.add(new Pair<>(Integer.parseInt(mobIdData.getName()), MapleDataTool.getString(mobIdData.getChildByPath("name"), "NO-NAME")));
                    }
                    for (Pair<Integer, String> mobPair : mobPairList) {
                        if (mobPair.getRight().toLowerCase().contains(search.toLowerCase())) {
                            retMobs.add("\r\n" + mobPair.getLeft() + " - " + mobPair.getRight());
                        }
                    }
                    if (retMobs.size() > 0) {
                        for (String singleRetMob : retMobs) {
                            if (sb.length() > 10000) {
                                sb.append("\r\n后面还有很多搜索结果, 但已经无法显示更多");
                                break;
                            }
                            sb.append(singleRetMob);
                            //c.getSession().write(NPCPacket.getNPCTalk(9010000, (byte) 0, retMobs.toString(), "00 00", (byte) 0, 9010000));
                            //c.getPlayer().dropMessage(6, singleRetMob);
                        }
                    } else {
                        c.getPlayer().dropMessage(6, "该怪物信息搜索不到");
                    }

                } else if (type.equalsIgnoreCase("物品")) {
                    List<String> retItems = new ArrayList<>();
                    for (ItemInformation itemPair : MapleItemInformationProvider.getInstance().getAllItems()) {
                        if (itemPair != null && itemPair.name != null && itemPair.name.toLowerCase().contains(search.toLowerCase())) {
                            retItems.add("\r\n" + itemPair.itemId + " - #i" + itemPair.itemId + ":# " + "#z" + itemPair.itemId + "#");
                        }
                    }
                    if (retItems.size() > 0) {
                        for (String singleRetItem : retItems) {
                            if (sb.length() > 10000) {
                                sb.append("\r\n后面还有很多搜索结果, 但已经无法显示更多");
                                break;
                            }
                            sb.append(singleRetItem);
                            //c.getSession().write(NPCPacket.getNPCTalk(9010000, (byte) 0, retItems.toString(), "00 00", (byte) 0, 9010000));
                            //c.getPlayer().dropMessage(6, singleRetItem);
                        }
                    } else {
                        c.getPlayer().dropMessage(6, "该物品信息搜索不到");
                    }
                } else if (type.equalsIgnoreCase("技能")) {
                    List<String> retSkills = new ArrayList<>();
                    for (Skill skill : SkillFactory.getAllSkills()) {
                        if (skill.getName() != null && skill.getName().toLowerCase().contains(search.toLowerCase())) {
                            retSkills.add("\r\n#s" + skill.getId() + "#" + skill.getId() + " - " + skill.getName());
                        }
                    }
                    if (retSkills.size() > 0) {
                        for (String singleRetSkill : retSkills) {
                            if (sb.length() > 10000) {
                                sb.append("\r\n后面还有很多搜索结果, 但已经无法显示更多");
                                break;
                            }
                            sb.append(singleRetSkill);
                            //c.getSession().write(NPCPacket.getNPCTalk(9010000, (byte) 0, retSkills.toString(), "00 00", (byte) 0, 9010000));
                            //    c.getPlayer().dropMessage(6, singleRetSkill);
                        }
                    } else {
                        c.getPlayer().dropMessage(6, "该技能信息搜索不到");
                    }
                } else if (type.equalsIgnoreCase("包头")) {
                    List<String> headers = new ArrayList<>();
                    headers.add("\r\n服务端包头:");
                    for (SendPacketOpcode send : SendPacketOpcode.values()) {
                        if (send.name() != null && send.name().toLowerCase().contains(search.toLowerCase())) {
                            headers.add("\r\n" + send.name() + " 值: " + send.getValue(false) + " 16进制: " + HexTool.getOpcodeToString(send.getValue(false)));
                        }
                    }
                    headers.add("\r\n客户端包头:");
                    for (RecvPacketOpcode recv : RecvPacketOpcode.values()) {
                        if (recv.name() != null && recv.name().toLowerCase().contains(search.toLowerCase())) {
                            headers.add("\r\n" + recv.name() + " 值: " + recv.getValue() + " 16进制: " + HexTool.getOpcodeToString(recv.getValue()));
                        }
                    }
                    for (String header : headers) {
                        if (sb.length() > 10000) {
                            sb.append("\r\n后面还有很多搜索结果, 但已经无法显示更多");
                            break;
                        }
                        sb.append(header);
                        //c.getSession().write(NPCPacket.getNPCTalk(9010000, (byte) 0, headers.toString(), "00 00", (byte) 0, 9010000));
                        //c.getPlayer().dropMessage(6, header);
                    }
                } else {
                    c.getPlayer().dropMessage(6, "对不起, 不支持这个检索命令");
                }
                c.getSession().write(NPCPacket.sendNPCSay(9010000, sb.toString()));
            }
            return 0;
        }
    }


    public static class WhosLast extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                StringBuilder sb = new StringBuilder("whoslast [type] where type can be:  ");
                for (MapleSquadType t : MapleSquadType.values()) {
                    sb.append(t.name()).append(", ");
                }
                c.getPlayer().dropMessage(6, sb.toString().substring(0, sb.length() - 2));
                return 0;
            }
            final MapleSquadType t = MapleSquadType.valueOf(splitted[1].toLowerCase());
            if (t == null) {
                StringBuilder sb = new StringBuilder("whoslast [type] where type can be:  ");
                for (MapleSquadType z : MapleSquadType.values()) {
                    sb.append(z.name()).append(", ");
                }
                c.getPlayer().dropMessage(6, sb.toString().substring(0, sb.length() - 2));
                return 0;
            }
            if (t.queuedPlayers.get(c.getChannel()) == null) {
                c.getPlayer().dropMessage(6, "The queue has not been initialized in this channel yet.");
                return 0;
            }
            c.getPlayer().dropMessage(6, "Queued players: " + t.queuedPlayers.get(c.getChannel()).size());
            StringBuilder sb = new StringBuilder("List of participants:  ");
            for (Pair<String, String> z : t.queuedPlayers.get(c.getChannel())) {
                sb.append(z.left).append('(').append(z.right).append(')').append(", ");
            }
            c.getPlayer().dropMessage(6, sb.toString().substring(0, sb.length() - 2));
            return 0;
        }
    }

    public static class WhosNext extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                StringBuilder sb = new StringBuilder("whosnext [type] where type can be:  ");
                for (MapleSquadType t : MapleSquadType.values()) {
                    sb.append(t.name()).append(", ");
                }
                c.getPlayer().dropMessage(6, sb.toString().substring(0, sb.length() - 2));
                return 0;
            }
            final MapleSquadType t = MapleSquadType.valueOf(splitted[1].toLowerCase());
            if (t == null) {
                StringBuilder sb = new StringBuilder("whosnext [type] where type can be:  ");
                for (MapleSquadType z : MapleSquadType.values()) {
                    sb.append(z.name()).append(", ");
                }
                c.getPlayer().dropMessage(6, sb.toString().substring(0, sb.length() - 2));
                return 0;
            }
            if (t.queue.get(c.getChannel()) == null) {
                c.getPlayer().dropMessage(6, "The queue has not been initialized in this channel yet.");
                return 0;
            }
            c.getPlayer().dropMessage(6, "Queued players: " + t.queue.get(c.getChannel()).size());
            StringBuilder sb = new StringBuilder("List of participants:  ");
            final long now = System.currentTimeMillis();
            for (Pair<String, Long> z : t.queue.get(c.getChannel())) {
                sb.append(z.left).append('(').append(StringUtil.getReadableMillis(z.right, now)).append(" ago),");
            }
            c.getPlayer().dropMessage(6, sb.toString().substring(0, sb.length() - 2));
            return 0;
        }
    }

    /*public static class WarpMap extends CommandExecute {

     @Override
     public int execute(MapleClient c, String[] splitted) {
     try {
     final MapleMap target = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(splitted[1]));
     if (target == null) {
     c.getPlayer().dropMessage(6, "Map does not exist");
     return 0;
     }
     final MapleMap from = c.getPlayer().getMap();
     for (MapleCharacter chr : from.getCharactersThreadsafe()) {
     chr.changeMap(target, target.getPortal(0));
     }
     } catch (NumberFormatException e) {
     c.getPlayer().dropMessage(5, "Error: " + e.getMessage());
     return 0; //assume drunk GM
     }
     return 1;
     }
     }*/
    public static class 清怪 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " (范围:默认全图) (地图ID:默认当前地图)");
            }
            MapleMap map = c.getPlayer().getMap();
            double range = Double.POSITIVE_INFINITY;

            if (splitted.length > 1) {
                int irange = Integer.parseInt(splitted[1]);
                if (splitted.length <= 2) {
                    range = irange * irange;
                } else {
                    map = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(splitted[2]));
                }
            }
            if (map == null) {
                c.getPlayer().dropMessage(6, "地图不存在");
                return 0;
            }
            MapleMonster mob;
            for (MapleMapObject monstermo : map.getMapObjectsInRange(c.getPlayer().getPosition(), range, Arrays.asList(MapleMapObjectType.MONSTER))) {
                mob = (MapleMonster) monstermo;
                if (!mob.getStats().isBoss() || mob.getStats().isPartyBonus() || c.getPlayer().isGM()) {
                    map.killMonster(mob, c.getPlayer(), false, false, (byte) 1);
                }
            }
            return 1;
        }
    }

    public static class 清怪爆物 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " (<范围:默认0全图> (地图:默认当前地图))");
            }
            MapleMap map = c.getPlayer().getMap();
            double range = Double.POSITIVE_INFINITY;

            if (splitted.length > 1) {
                int irange = Integer.parseInt(splitted[1]);
                if (irange != 0) {
                    range = irange * irange;
                }
                if (splitted.length > 2) {
                    map = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(splitted[2]));
                }
            }
            if (map == null) {
                c.getPlayer().dropMessage(6, "地图不存在");
                return 0;
            }
            MapleMonster mob;
            for (MapleMapObject monstermo : map.getMapObjectsInRange(c.getPlayer().getPosition(), range, Arrays.asList(MapleMapObjectType.MONSTER))) {
                mob = (MapleMonster) monstermo;
                if (!mob.getStats().isBoss() || mob.getStats().isPartyBonus() || c.getPlayer().isGM()) {
                    map.killMonster(mob, c.getPlayer(), true, false, (byte) 1);
                }
            }
            return 1;
        }
    }

    public static class 全屏捡物 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            final List<MapleMapObject> items = c.getPlayer().getMap().getMapObjectsInRange(c.getPlayer().getPosition(), GameConstants.maxViewRangeSq(), Arrays.asList(MapleMapObjectType.ITEM));
            MapleMapItem mapitem;
            for (MapleMapObject item : items) {
                mapitem = (MapleMapItem) item;
                if (mapitem.getMeso() > 0) {
                    c.getPlayer().gainMeso(mapitem.getMeso(), true);
                } else if (mapitem.getItem() == null || !MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true)) {
                    continue;
                }
                mapitem.setPickedUp(true);
                c.getPlayer().getMap().broadcastMessage(InventoryPacket.removeItemFromMap(mapitem.getObjectId(), 2, c.getPlayer().getId()), mapitem.getPosition());
                c.getPlayer().getMap().removeMapObject(item);

            }
            return 1;
        }
    }

    public static class 清除BUFF extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().cancelAllBuffs();
            return 1;
        }
    }

    public static class 换频道 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().changeChannel(Integer.parseInt(splitted[1]));
            return 1;
        }
    }

    public static class Reports extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            List<CheaterData> cheaters = World.getReports();
            for (int x = cheaters.size() - 1; x >= 0; x--) {
                CheaterData cheater = cheaters.get(x);
                c.getPlayer().dropMessage(6, cheater.getInfo());
            }
            return 1;
        }
    }


    public static class FakeRelog extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().fakeRelog();
            return 1;
        }
    }

    public static class 打开NPC extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <NPCID> (模式:默认空)");
                return 0;
            }
            NPCScriptManager.getInstance().start(c, Integer.parseInt(splitted[1]), splitted.length > 2 ? StringUtil.joinStringFrom(splitted, 2) : splitted[1]);
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

    public static class 杀死附近玩家 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleMap map = c.getPlayer().getMap();
            List<MapleMapObject> players = map.getMapObjectsInRange(c.getPlayer().getPosition(), 25000, Arrays.asList(MapleMapObjectType.PLAYER));
            for (MapleMapObject closeplayers : players) {
                MapleCharacter playernear = (MapleCharacter) closeplayers;
                if (playernear.isAlive() && playernear != c.getPlayer() && playernear.getGMLevel() < c.getPlayer().getGMLevel()) {
                    playernear.getStat().setHp((short) 0);
                    playernear.getStat().setMp((short) 0);
                    playernear.updateSingleStat(MapleStat.HP, playernear.getStat().getHp());
                    playernear.updateSingleStat(MapleStat.MP, playernear.getStat().getMp());
                    playernear.dropMessage(5, "你太靠近管理员了");
                }
            }
            return 1;
        }
    }

    /*public static class ManualEvent extends CommandExecute {

     @Override
     public int execute(MapleClient c, String[] splitted) {
     if (c.getChannelServer().manualEvent(c.getPlayer())) {
     for (MapleCharacter chrs : c.getChannelServer().getPlayerStorage().getAllCharacters()) {
     //chrs.dropMessage(0, "MapleGM is hosting an event! Use the @joinevent command to join the event!");
     //chrs.dropMessage(0, "Event Map: " + c.getPlayer().getMap().getMapName());
     //World.Broadcast.broadcastMessage(MaplePacketCreator.broadcastMsg(25, 0, "MapleGM is hosting an event! Use the @joinevent command to join the event!"));
     //World.Broadcast.broadcastMessage(MaplePacketCreator.broadcastMsg(26, 0, "Event Map: " + c.getPlayer().getMap().getMapName()));
     chrs.getClient().getSession().write(MaplePacketCreator.broadcastMsg(GameConstants.isEventMap(chrs.getMapId()) ? 0 : 25, c.getChannel(), "活动 : 管理员开启了一个活动, 使用 @参加活动 来加入活动吧!"));
     chrs.getClient().getSession().write(MaplePacketCreator.broadcastMsg(GameConstants.isEventMap(chrs.getMapId()) ? 0 : 26, c.getChannel(), "活动 : 活动频道: " + c.getChannel() + " 活动地图: " + c.getPlayer().getMap().getMapName()));
     }
     } else {
     for (MapleCharacter chrs : c.getChannelServer().getPlayerStorage().getAllCharacters()) {
     //World.Broadcast.broadcastMessage(MaplePacketCreator.broadcastMsg(22, 0, "Enteries to the GM event are closed. The event has began!"));
     chrs.getClient().getSession().write(MaplePacketCreator.broadcastMsg(GameConstants.isEventMap(chrs.getMapId()) ? 0 : 22, c.getChannel(), "活动 : Enteries to the GM event are closed. The event has began!"));
     }
     }
     return 1;
     }
     }*/

    /*public static class 开始炸弹人活动 extends CommandExecute {

     @Override
     public int execute(MapleClient c, String[] splitted) {
     MapleCharacter player = c.getPlayer();
     if (player.getMapId() != 109010100) {
     player.dropMessage(5, "该命令只能在地图(ID:109010100)处使用");
     } else {
     c.getChannelServer().toggleBomberman(c.getPlayer());
     for (MapleCharacter chr : player.getMap().getCharacters()) {
     if (!chr.isIntern()) {
     chr.cancelAllBuffs();
     chr.giveDebuff(MapleDisease.SEAL, MobSkillFactory.getMobSkill(120, 1));
     //MapleInventoryManipulator.removeById(chr.getClient(), MapleInventoryType.USE, 2100067, chr.getItemQuantity(2100067, false), true, true);
     //chr.gainItem(2100067, 30);
     //MapleInventoryManipulator.removeById(chr.getClient(), MapleInventoryType.ETC, 4031868, chr.getItemQuantity(4031868, false), true, true);
     //chr.gainItem(4031868, (short) 5);
     //chr.dropMessage(0, "You have been granted 5 jewels(lifes) and 30 bombs.");
     //chr.dropMessage(0, "Pick up as many bombs and jewels as you can!");
     //chr.dropMessage(0, "Check inventory for Bomb under use");
     }
     }
     for (MapleCharacter chrs : c.getChannelServer().getPlayerStorage().getAllCharacters()) {
     chrs.getClient().getSession().write(MaplePacketCreator.broadcastMsg(GameConstants.isEventMap(chrs.getMapId()) ? 0 : 22, c.getChannel(), "活动 : 炸弹人活动已经开始了!"));
     }
     player.getMap().broadcastMessage(CField.getClock(60));
     }
     return 1;
     }
     }

     public static class 结束炸弹人活动 extends CommandExecute {

     @Override
     public int execute(MapleClient c, String[] splitted) {
     MapleCharacter player = c.getPlayer();
     if (player.getMapId() != 109010100) {
     player.dropMessage(5, "该命令只能在地图(ID:109010100)处使用");
     } else {
     c.getChannelServer().toggleBomberman(c.getPlayer());
     int count = 0;
     String winner = "";
     for (MapleCharacter chr : player.getMap().getCharacters()) {
     if (!chr.isGM()) {
     if (count == 0) {
     winner = chr.getName();
     count++;
     } else {
     winner += " , " + chr.getName();
     }
     }
     }
     for (MapleCharacter chrs : c.getChannelServer().getPlayerStorage().getAllCharacters()) {
     chrs.getClient().getSession().write(MaplePacketCreator.broadcastMsg(GameConstants.isEventMap(chrs.getMapId()) ? 0 : 22, c.getChannel(), "活动 : 炸弹人活动已经结束, 胜利者是: " + winner));
     }
     }
     return 1;
     }
     }*/
    public static class 清理背包 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            if (splitted.length < 2 || player.hasBlockedInventory()) {
                c.getPlayer().dropMessage(5, splitted[0] + " <物品栏:装备 / 消耗 / 其他 / 设置 / 特殊 / 全部>");
                return 0;
            } else {
                MapleInventoryType type;
                if (splitted[1].equalsIgnoreCase("装备")) {
                    type = MapleInventoryType.EQUIP;
                } else if (splitted[1].equalsIgnoreCase("消耗")) {
                    type = MapleInventoryType.USE;
                } else if (splitted[1].equalsIgnoreCase("设置")) {
                    type = MapleInventoryType.SETUP;
                } else if (splitted[1].equalsIgnoreCase("其他")) {
                    type = MapleInventoryType.ETC;
                } else if (splitted[1].equalsIgnoreCase("特殊")) {
                    type = MapleInventoryType.CASH;
                } else if (splitted[1].equalsIgnoreCase("全部")) {
                    type = null;
                } else {
                    c.getPlayer().dropMessage(5, "找不到物品栏 <装备 / 消耗 / 其他 / 设置 / 特殊 / 全部>");
                    return 0;
                }
                if (type == null) { //All, a bit hacky, but it's okay 
                    MapleInventoryType[] invs = {MapleInventoryType.EQUIP, MapleInventoryType.USE, MapleInventoryType.SETUP, MapleInventoryType.ETC, MapleInventoryType.CASH};
                    for (MapleInventoryType t : invs) {
                        type = t;
                        MapleInventory inv = c.getPlayer().getInventory(type);
                        byte start = -1;
                        for (byte i = 0; i < inv.getSlotLimit(); i++) {
                            if (inv.getItem(i) != null) {
                                start = i;
                                break;
                            }
                        }
                        if (start == -1) {
                            c.getPlayer().dropMessage(5, "该物品栏没有物品");
                            return 0;
                        }
                        int end = 0;
                        for (byte i = start; i < inv.getSlotLimit(); i++) {
                            if (inv.getItem(i) != null) {
                                MapleInventoryManipulator.removeFromSlot(c, type, i, inv.getItem(i).getQuantity(), true);
                            } else {
                                end = i;
                                break;//Break at first empty space. 
                            }
                        }
                        c.getPlayer().dropMessage(5, "已经清除了第" + start + "格到第" + end + "格的物品");
                    }
                } else {
                    MapleInventory inv = c.getPlayer().getInventory(type);
                    byte start = -1;
                    for (byte i = 0; i < inv.getSlotLimit(); i++) {
                        if (inv.getItem(i) != null) {
                            start = i;
                            break;
                        }
                    }
                    if (start == -1) {
                        c.getPlayer().dropMessage(5, "该物品栏没有物品");
                        return 0;
                    }
                    byte end = 0;
                    for (byte i = start; i < inv.getSlotLimit(); i++) {
                        if (inv.getItem(i) != null) {
                            MapleInventoryManipulator.removeFromSlot(c, type, i, inv.getItem(i).getQuantity(), true);
                        } else {
                            end = i;
                            break;//Break at first empty space. 
                        }
                    }
                    c.getPlayer().dropMessage(5, "已经清除了第" + start + "格到第" + end + "格的物品");
                }
                return 1;
            }
        }
    }

    public static class 杀死地图玩家 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            for (MapleCharacter map : c.getPlayer().getMap().getCharactersThreadsafe()) {
                if (map != null && !map.isIntern()) {
                    map.getStat().setHp((short) 0);
                    map.getStat().setMp((short) 0);
                    map.updateSingleStat(MapleStat.HP, map.getStat().getHp());
                    map.updateSingleStat(MapleStat.MP, map.getStat().getMp());
                }
            }
            return 1;
        }
    }

    /*public static class  聊天类型 extends CommandExecute {

     @Override
     public int execute(MapleClient c, String[] splitted) {
     try {
     //c.getPlayer().setChatColour(c.getPlayer().getChatColor() == 0 ? (short) 11 : 0);
     c.getPlayer().setChatType(c.getPlayer().getChatType() == 0 ? (short) 11 : 0);
     c.getPlayer().dropMessage(0, "文字颜色已经改变");
     } catch (Exception e) {
     c.getPlayer().dropMessage(0, "发生未知的错误");
     }
     return 1;
     }
     }*/
    public static class 搜索命令 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <关键字词>");
                return 0;
            }
            c.getPlayer().dropMessage(6, "搜索命令(关键字词:" + splitted[1] + ")结果如下:");
            HashMap<Integer, ArrayList<String>> commandList = CommandProcessor.getCommandList();
            for (int i = 0; i <= c.getPlayer().getGMLevel(); i++) {
                if (commandList.containsKey(i)) {
                    final StringBuilder sb = new StringBuilder("");
                    final StringBuilder 命令前缀 = new StringBuilder("");
                    char[] gmRank = PlayerGMRank.getByLevel(i).getCommandPrefix();
                    for (int j = 0; j < gmRank.length; j++) {
                        命令前缀.append('"').append(gmRank[j]).append('"');
                        if (j != gmRank.length - 1 && gmRank.length != 1) {
                            命令前缀.append("或");
                        }
                    }
                    for (String s : commandList.get(i)) {
                        if (s.contains(splitted[1].toLowerCase())) {
                            if ((gmRank.length > 1 && s.substring(0, 1).equals(String.valueOf(gmRank[0]))) || gmRank.length == 1) {
                                sb.append(s.substring(1));
                                sb.append("，");
                            }
                        }
                    }
                    if (!sb.toString().equals("")) {
                        c.getPlayer().dropMessage(6, "-----------------------------------------------------------------------------------------");
                        if (i == 0) {
                            c.getPlayer().dropMessage(6, "玩家命令(前缀:" + 命令前缀 + ")：");
                        } else if (i == 1) {
                            c.getPlayer().dropMessage(6, "捐赠者命令(前缀:" + 命令前缀 + ")：");
                        } else if (i == 2) {
                            c.getPlayer().dropMessage(6, "高级捐赠者命令(前缀:" + 命令前缀 + ")：");
                        } else if (i == 3) {
                            c.getPlayer().dropMessage(6, "实习管理员命令(前缀:" + 命令前缀 + ")：");
                        } else if (i == 4) {
                            c.getPlayer().dropMessage(6, "游戏管理员命令(前缀:" + 命令前缀 + ")：");
                        } else if (i == 5) {
                            c.getPlayer().dropMessage(6, "高级理员命令(前缀:" + 命令前缀 + ")：");
                        } else if (i == 6) {
                            c.getPlayer().dropMessage(6, "服务器管理员命令(前缀:" + 命令前缀 + ")：");
                        }
                        c.getPlayer().dropMessage(6, sb.toString());
                    }
                }
            }
            return 1;
        }
    }

    public static class 查看封号 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <角色名字>");
                return 0;
            }
            String msg = MapleClient.getAccInfoByName(splitted[1], c.getPlayer().isAdmin());
            if (msg != null) {
                c.getPlayer().dropMessage(6, msg);
            } else {
                c.getPlayer().dropMessage(6, "输入的角色名字错误，无法找到信息。");
            }
            return 1;
        }
    }

    public static class 查看账号 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <玩家账号>");
                return 0;
            }
            String msg = MapleClient.getAccInfo(splitted[1], c.getPlayer().isAdmin());
            if (msg != null) {
                c.getPlayer().dropMessage(6, msg);
            } else {
                c.getPlayer().dropMessage(6, "输入的账号错误，无法找到信息。");
            }
            return 1;
        }
    }

    public static class 检测作弊 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            List cheaters = World.getCheaters();
            if (cheaters.isEmpty()) {
                c.getPlayer().dropMessage(6, "未检测到作弊。");
                return 1;
            }
            for (int x = cheaters.size() - 1; x >= 0; x--) {
                CheaterData cheater = (CheaterData) cheaters.get(x);
                c.getPlayer().dropMessage(6, cheater.getInfo());
            }
            return 1;
        }
    }

    public static class 角色信息 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <玩家名字>");
                return 0;
            }
            StringBuilder builder = new StringBuilder();
            MapleCharacter other = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (other == null) {
                builder.append("输入的角色不存在...");
                c.getPlayer().dropMessage(6, builder.toString());
                return 0;
            }
            if (other.getClient().getLastPing() <= 0L) {
                other.getClient().sendPing();
            }
            builder.append(MapleClient.getLogMessage(other, ""));
            builder.append(" 坐标 ").append(other.getPosition().x);
            builder.append(" /").append(other.getPosition().y);

            builder.append(" || 血 : ");
            builder.append(other.getStat().getHp());
            builder.append(" /");
            builder.append(other.getStat().getCurrentMaxHp());

            builder.append(" || 蓝 : ");
            builder.append(other.getStat().getMp());
            builder.append(" /");
            builder.append(other.getStat().getCurrentMaxMp());

            builder.append(" || 物理攻击力 : ");
            builder.append(other.getStat().getTotalWatk());
            builder.append(" || 魔法攻击力 : ");
            builder.append(other.getStat().getTotalMagic());
            builder.append(" || 最大攻击 : ");
            builder.append(other.getStat().getCurrentMaxBaseDamage());
            builder.append(" || 伤害% : ");
            builder.append(other.getStat().getDamageRate());
            builder.append(" || BOSS伤害% : ");
            builder.append(other.getStat().getBossDamageRate());
            builder.append(" || 爆击几率 : ");
            builder.append(other.getStat().passive_sharpeye_rate());
            builder.append(" || 暴击伤害 : ");
            builder.append(other.getStat().passive_sharpeye_percent());

            builder.append(" || 力量 : ");
            builder.append(other.getStat().getStr());
            builder.append(" || 敏捷 : ");
            builder.append(other.getStat().getDex());
            builder.append(" || 智力 : ");
            builder.append(other.getStat().getInt());
            builder.append(" || 运气 : ");
            builder.append(other.getStat().getLuk());

            builder.append(" || 全部力量 : ");
            builder.append(other.getStat().getTotalStr());
            builder.append(" || 全部敏捷 : ");
            builder.append(other.getStat().getTotalDex());
            builder.append(" || 全部智力 : ");
            builder.append(other.getStat().getTotalInt());
            builder.append(" || 全部运气 : ");
            builder.append(other.getStat().getTotalLuk());

            builder.append(" || 经验 : ");
            builder.append(other.getExp());
            builder.append(" || 金币 : ");
            builder.append(other.getMeso());

            builder.append(" || 是否组队 : ");
            builder.append(other.getParty() == null ? -1 : other.getParty().getId());

            builder.append(" || 是否交易: ");
            builder.append(other.getTrade() != null);
            builder.append(" || Latency: ");
            builder.append(other.getClient().getLatency());
            builder.append(" || PING: ");
            builder.append(other.getClient().getLastPing());
            builder.append(" || PONG: ");
            builder.append(other.getClient().getLastPong());
            builder.append(" || 连接地址: ");

            other.getClient().DebugMessage(builder);

            c.getPlayer().dropMessage(6, builder.toString());
            return 1;
        }
    }
}
