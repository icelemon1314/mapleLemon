package handling.channel.handler;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;

import static client.MapleJob.getJobName;
import client.PlayerStats;
import client.Skill;
import client.SkillFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.ModifyInventory;
import constants.GameConstants;
import handling.channel.ChannelServer;
import static handling.channel.handler.DamageParse.NotEffectforAttack;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import scripting.npc.NPCScriptManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MaplePortal;
import server.MapleStatEffect;
import server.events.MapleEvent;
import server.events.MapleEventType;
import server.events.MapleSnowball.MapleSnowballs;
import server.maps.MapleMap;
import server.quest.MapleQuest;
import server.skill.冒险家.勇士;
import server.skill.冒险家.独行客;

import tools.MapleLogger;
import tools.MaplePacketCreator;
import tools.data.input.LittleEndianAccessor;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.InventoryPacket;
import tools.packet.SkillPacket;

public class PlayerHandler {

    public static int fox = 0;

    // 消耗斗气的技能
    public static int isFinisher(int skillid) {
        switch (skillid) {
            case 勇士.黑暗之剑:
            case 勇士.黑暗之斧:
                return 2;
            case 勇士.气绝剑:
            case 勇士.气绝斧:
                return 4;
        }
        return 0;
    }

    public static void ChangeKeymap(SeekableLittleEndianAccessor slea, MapleCharacter chr) {
        if ((slea.available() > 8L) && (chr != null)) {
            slea.skip(4);
            int numChanges = slea.readInt();
            for (int i = 0; i < numChanges; i++) {
                int key = slea.readInt();
                byte type = slea.readByte();
                int action = slea.readInt();
                if ((type == 1) && (action >= 1000)) {
                    Skill skil = SkillFactory.getSkill(action);
                    if ((skil != null) && (((skil.isInvisible()) && (chr.getSkillLevel(skil) <= 0)) || (action % 10000 < 1000))) {
                        continue;
                    }
                }
                chr.changeKeybinding(key, type, action);
            }
        } else if (chr != null) {
            int type = slea.readInt();
            int data = slea.readInt();
            switch (type) {
                case 1:
                    if (data <= 0) {
                        chr.getQuestRemove(MapleQuest.getInstance(122221));
                    } else {
                        chr.getQuestNAdd(MapleQuest.getInstance(122221)).setCustomData(String.valueOf(data));
                    }
                    break;
                case 2:
                    if (data <= 0) {
                        chr.getQuestRemove(MapleQuest.getInstance(122223));
                    } else {
                        chr.getQuestNAdd(MapleQuest.getInstance(122223)).setCustomData(String.valueOf(data));
                    }
                    break;
                case 3:
                    if (data <= 0) {
                        chr.getQuestRemove(MapleQuest.getInstance(122224));
                    } else {
                        chr.getQuestNAdd(MapleQuest.getInstance(122224)).setCustomData(String.valueOf(data));
                    }
            }
        }
    }

    public static void CancelItemEffect(int id, MapleCharacter chr) {
        chr.cancelEffect(MapleItemInformationProvider.getInstance().getItemEffect(-id), false, -1L);
    }

    public static void CancelBuffHandler(int sourceid, MapleCharacter chr) {

    }

    public static void CancelMech(SeekableLittleEndianAccessor slea, MapleCharacter chr) {
        if (chr == null) {
            return;
        }
        chr.setnorba();
        int sourceid = slea.readInt();
        if ((sourceid % 10000 < 1000) && (SkillFactory.getSkill(sourceid) == null)) {
            sourceid += 1000;
        }
        Skill skill = SkillFactory.getSkill(sourceid);
        if (skill == null) {
            return;
        }
        MapleStatEffect eff = skill.getEffect(chr.getTotalSkillLevel(sourceid));
        if (skill.isChargeSkill()) {
            chr.setKeyDownSkill_Time(0L);
            chr.getMap().broadcastMessage(chr, MaplePacketCreator.skillCancel(chr, sourceid), false);
        }else {
            chr.cancelEffect(skill.getEffect(slea.readByte()), false, -1L);
        }
    }

    public static void QuickSlot(SeekableLittleEndianAccessor slea, MapleCharacter chr) {
        if (chr == null) {
            return;
        }
        chr.getQuickSlot().resetQuickSlot();
        for (int i = 0; i < 28; i++) {
            chr.getQuickSlot().addQuickSlot(i, slea.readInt());
        }
    }
    public static void AfterSkill(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        int skillid = slea.readInt();
        if ((chr == null) || (chr.hasBlockedInventory()) || (chr.getMap() == null)) {
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
    }

    public static void InnerPortal(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if ((chr == null) || (chr.getMap() == null)) {
            return;
        }
        MaplePortal portal = chr.getMap().getPortal(slea.readMapleAsciiString());
        int toX = slea.readShort();
        int toY = slea.readShort();

        if (portal == null) {
            return;
        }
        if ((portal.getPosition().distanceSq(chr.getTruePosition()) > 22500.0D) && (!chr.isGM())) {
            return;
        }
        chr.getMap().movePlayer(chr, new Point(toX, toY));
    }

    public static void snowBall(SeekableLittleEndianAccessor slea, MapleClient c) {
        c.sendPacket(MaplePacketCreator.enableActions());
    }

    public static void PlayerUpdate(MapleCharacter chr) {
//        boolean autoSave = true;
        if (chr == null || chr.getMap() == null) {
            return;
        }
            long startTime = System.currentTimeMillis();
            chr.saveToDB(false, false);
            if (chr.isShowPacket()) {
                chr.dropMessage(-11, "保存数据，耗时 " + (System.currentTimeMillis() - startTime) + " 毫秒");
            }
    }

    public static void LoadPlayerSuccess(MapleClient c, MapleCharacter chr) {
        if (chr == null || chr.getMap() == null) {
            return;
        }
        String msg = "欢迎来到#b蓝蜗牛区（仿官方）#k\r\n\r\n祝您玩的愉快！";
        int exp = c.getChannelServer().getExpRate(c.getWorld());
        if (exp > 1) {
            chr.dropSpouseMessage(20, "[系统提示] 当前服务器处于"+exp+"倍经验活动中，祝您玩的愉快！");
        }
        if (c.getChannelServer().getAutoGain() >= 2) {
            chr.dropSpouseMessage(25, "[系统提示] 在线时间奖励双倍活动正在举行.");
        }
    }

    public static void ChangeMarketMap(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if ((chr == null) || (chr.getMap() == null) || (chr.hasBlockedInventory())) {
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        int chc = slea.readByte() + 1;
        int toMapId = slea.readInt();

        if ((toMapId >= 910000001) && (toMapId <= 910000022)) {
            if (c.getChannel() != chc) {
                if (chr.getMapId() != toMapId) {
                    MapleMap to = ChannelServer.getInstance(chc).getMapFactory().getMap(toMapId);
                    chr.setMap(to);
                    chr.changeChannel(chc);
                } else {
                    chr.changeChannel(chc);
                }
            } else {
                MapleMap to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(toMapId);
                chr.changeMap(to, to.getPortal(0));
            }
        } else {
            c.sendPacket(MaplePacketCreator.enableActions());
        }
    }

    public static boolean isBossMap(int mapid) {
        switch (mapid) {
            case 0:
            case 105100300:
            case 105100400:
            case 211070100:
            case 211070101:
            case 211070110:
            case 220080001:
            case 240040700:
            case 240060200:
            case 240060201:
            case 270050100:
            case 271040100:
            case 271040200:
            case 280030000:
            case 280030001:
            case 280030100:
            case 300030310:
            case 551030200:
            case 802000111:
            case 802000211:
            case 802000311:
            case 802000411:
            case 802000611:
            case 802000711:
            case 802000801:
            case 802000802:
            case 802000803:
            case 802000821:
            case 802000823:
                return true;
        }
        return false;
    }

    public static void showPlayerCash(SeekableLittleEndianAccessor slea, MapleClient c) {
        int accId = slea.readInt();
        int playerId = slea.readInt();
    }

    public static void quickBuyCashShopItem(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if ((chr == null) || (chr.getMap() == null)) {
            //c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        int accId = slea.readInt();
        int playerId = slea.readInt();
        int mode = slea.readInt();
        int cssn = slea.readInt();
        int toCharge = slea.readByte() == 1 ? 1 : 2;
        if ((chr.getId() != playerId) || (chr.getAccountID() != accId)) {
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        switch (mode) {
            case 10:
                if ((chr.getCSPoints(toCharge) >= 600) && (chr.getStorage().getSlots() < 93)) {
                    chr.modifyCSPoints(toCharge, -600, false);
                    chr.getStorage().increaseSlots((byte) 4);
                    chr.getStorage().saveToDB();
//                    c.sendPacket(MaplePacketCreator.playerCashUpdate(mode, toCharge, chr));
                } else {
                    chr.dropMessage(5, "扩充失败，点券余额不足或者仓库栏位已超过上限。");
                }
                break;
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
                int iv = mode == 15 ? 5 : mode == 14 ? 4 : mode == 13 ? 3 : mode == 12 ? 2 : mode == 11 ? 1 : -1;
                if (iv > 0) {
                    MapleInventoryType tpye = MapleInventoryType.getByType((byte) iv);
                    if ((chr.getCSPoints(toCharge) >= 600) && (chr.getInventory(tpye).getSlotLimit() < 93)) {
                        chr.modifyCSPoints(toCharge, -600, false);
                        chr.getInventory(tpye).addSlot((byte) 4);
//                        c.sendPacket(MaplePacketCreator.playerCashUpdate(mode, toCharge, chr));
                    } else {
                        chr.dropMessage(1, "扩充失败，点券余额不足或者栏位已超过上限。");
                    }
                } else {
                    chr.dropMessage(1, "扩充失败，扩充的类型不正确。");
                }
        }
    }

    public static void getMonoid(SeekableLittleEndianAccessor slea, MapleCharacter chr) {
        int count = slea.readInt();
        int skill = slea.readInt();
        List<Integer> oid = new ArrayList();
        for (int i = 0; i < count; i++) {
            slea.skip(5);
            oid.add(slea.readInt());
            slea.skip(4);
        }
        switch (skill) {
            default:
                MapleLogger.info("技能ID：" + skill + " 循环次数：" + count + " 封包: " + slea.toString(true));
        }
    }

    /**
     *
     * @param slea
     * @param c
     */
    public static void handleCharInfo(SeekableLittleEndianAccessor slea, MapleClient c) {

    }
}
