package tools;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleExpStat;
import client.MapleKeyLayout;
import client.MapleQuestStatus;
import client.MapleQuickSlot;
import client.MapleStat;
import client.MonsterFamiliar;
import client.Skill;
import client.SkillEntry;
import client.SkillMacro;
import client.inventory.Equip;
import client.inventory.ImpFlag;
import client.inventory.Item;
import client.inventory.MapleImp;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.MapleRing;
import constants.GameConstants;
import constants.ItemConstants;
import constants.ServerConstants;
import custom.LoadPacket;
import handling.SendPacketOpcode;
import handling.channel.handler.AttackInfo;
import handling.world.WorldGuildService;
import handling.world.guild.MapleGuild;
import java.awt.Point;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import org.apache.log4j.Logger;
import server.MapleDueyActions;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.RankingWorker;
import server.ServerProperties;
import server.StructFamiliar;
import server.events.MapleSnowball;
import server.maps.MapleDefender;
import server.maps.MapleMap;
import server.maps.MapleNodes.MaplePlatform;
import server.maps.MapleQuickMove;
import server.maps.MapleReactor;
import server.movement.LifeMovementFragment;
import server.quest.MapleQuest;
import server.shops.HiredMerchant;
import server.shops.MaplePlayerShopItem;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.packet.LoginPacket;
import tools.packet.PacketHelper;
import tools.packet.PetPacket;

public class MaplePacketCreator {

    private static final Logger log = Logger.getLogger(MaplePacketCreator.class);

    public static byte[] getWzCheck(String WzCheckPack) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.WZ_CHECK.getValue());
        mplew.write(HexTool.getByteArrayFromHexString(WzCheckPack));

        return mplew.getPacket();
    }

    /**
     * 通知客户端频道服务器地址
     * @param c
     * @param port
     * @param charId
     * @return
     */
    public static byte[] getServerIP(MapleClient c, int port, int charId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SERVER_IP.getValue());
        mplew.writeShort(0);
        mplew.write(ServerConstants.NEXON_IP);
        mplew.writeShort(port);
        mplew.writeInt(charId);
        mplew.write(0);

        return mplew.getPacket();
    }

    /**
     * 更换频道
     * @param c
     * @param port
     * @return
     */
    public static byte[] getChannelChange(MapleClient c, int port) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CHANGE_CHANNEL.getValue());
        mplew.write(1);
        mplew.write(ServerConstants.NEXON_IP);
        mplew.writeShort(port);

        return mplew.getPacket();
    }

    public static byte[] cancelTitleEffect(int[] titles) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CANCEL_TITLE_EFFECT.getValue());
        for (int i = 0; i < 5; i++) {
            mplew.writeMapleAsciiString("");
            if (titles.length < i + 1) {
                mplew.write(-1);
            } else {
                mplew.write(titles[i]);
            }
        }

        return mplew.getPacket();
    }

    /**
     * 玩家进入地图需要的信息
     * @param chr
     * @return
     */
    public static byte[] getCharInfo(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.WARP_TO_MAP.getValue());
        mplew.writeInt(chr.getClient().getChannel() - 1);
        mplew.write(0);
        mplew.write(1);
        mplew.writeInt(Randomizer.nextInt());
        mplew.writeInt(Randomizer.nextInt());
        mplew.writeInt(Randomizer.nextInt());
        mplew.writeInt(0);
        PacketHelper.addCharacterInfo(mplew, chr);

        return mplew.getPacket();
    }

    public static byte[] enableActions() {
        return updatePlayerStats(new EnumMap(MapleStat.class), true, null);
    }

    public static byte[] updatePlayerStats(Map<MapleStat, Long> stats, MapleCharacter chr) {
        return updatePlayerStats(stats, false, chr);
    }

    /**
     * 更新玩家状态信息
     * @param mystats
     * @param itemReaction
     * @param chr
     * @return
     */
    public static byte[] updatePlayerStats(Map<MapleStat, Long> mystats, boolean itemReaction, MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        // 19 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00
        mplew.write(SendPacketOpcode.UPDATE_STATS.getValue());
        mplew.write(itemReaction ? 1 : 0);
        int updateMask = 0;
        for (MapleStat statupdate : mystats.keySet()) {
            updateMask |= statupdate.getValue();
        }
        mplew.writeInt(updateMask);
        for (Entry<MapleStat, Long> statupdate : mystats.entrySet()) {
            switch (statupdate.getKey().getWriteByte()) {
                case 1:
                    mplew.write(statupdate.getValue().byteValue());
                    break;
                case 2:
                    mplew.writeShort(statupdate.getValue().shortValue());
                    break;
                case 4:
                    mplew.writeInt(statupdate.getValue().intValue());
                    break;
                case 8:
                    mplew.writeLong(statupdate.getValue().longValue());
                    break;
                default:
                    FileoutputUtil.log("未知的类型："+statupdate.getKey().getWriteByte());
                    break;
            }
        }
        return mplew.getPacket();
    }

    /**
     * 地图之间的传送
     * @param to
     * @param spawnPoint
     * @param chr
     * @return
     */
    public static byte[] getWarpToMap(MapleMap to, int spawnPoint, MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.WARP_TO_MAP.getValue());
        mplew.writeInt(chr.getClient().getChannel() - 1);
        mplew.write(to.getPortals().size());
        mplew.write(0);
        mplew.writeInt(to.getId());
        mplew.write(spawnPoint);
        mplew.writeShort(chr.getStat().getHp());

        return mplew.getPacket();
    }

    public static byte[] instantMapWarp(byte portal) {
      
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CURRENT_MAP_WARP.getValue());
        mplew.writeShort(0);
        mplew.writeInt(portal);

        return mplew.getPacket();
    }

    /**
     * 召唤传送口
     * @param townId
     * @param targetId
     * @param skillId
     * @param pos
     * @return
     */
    public static byte[] spawnPortal(int townId, int targetId, int skillId, Point pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        // 27 40 23 05 06 B4 4A 05 06 7A FB 14 08
        mplew.write(SendPacketOpcode.SPAWN_PORTAL.getValue());
        mplew.writeInt(townId);
        mplew.writeInt(targetId);
        if ((townId != 999999999) && (targetId != 999999999)) {
            mplew.writeShort(pos.x);
            mplew.writeShort(pos.y);
        }

        return mplew.getPacket();
    }

    /**
     * 召唤时空门
     * @param ownerId
     * @param skillId
     * @param pos
     * @param animation
     * @return
     */
    public static byte[] spawnDoor(int ownerId, int skillId, Point pos, boolean animation) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SPAWN_DOOR.getValue());
        mplew.write(animation ? 0 : 1);
        mplew.writeInt(ownerId);
        // 不知道有啥区别，先这样了吧
        if (animation == true) {
            mplew.writePos(pos);
        } else {
            mplew.writePos(pos);
        }

        return mplew.getPacket();
    }

    /**
     * 移除时空门
     * @param ownerId
     * @param animation
     * @return
     */
    public static byte[] removeDoor(int ownerId, boolean animation) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.REMOVE_DOOR.getValue());
        mplew.write(animation ? 0 : 1);
        mplew.writeInt(ownerId);

        return mplew.getPacket();
    }

    public static byte[] resetScreen() {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.RESET_SCREEN.getValue());

        return mplew.getPacket();
    }

    public static byte[] mapBlocked(int type) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MAP_BLOCKED.getValue());
        mplew.write(type);

        return mplew.getPacket();
    }

    public static byte[] serverBlocked(int type) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SERVER_BLOCKED.getValue());
        mplew.write(type);

        return mplew.getPacket();
    }

    public static byte[] partyBlocked(int type) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PARTY_BLOCKED.getValue());
        mplew.write(type);

        return mplew.getPacket();
    }

    public static byte[] serverNotice(int type, String message) {
        return serverMessage(type, 0, message, false);
    }

    public static byte[] serverNotice(int type, int channel, String message) {
        return serverMessage(type, channel, message, false);
    }

    public static byte[] serverNotice(int type, int channel, String message, boolean smegaEar) {
        return serverMessage(type, channel, message, smegaEar);
    }

    /**
     * 服务器公告
     * @param message
     * @return
     */
    public static byte[] serverMessageNotice(String message){
        return serverMessage(0,0,message,false);
    }

    /**
     * 弹窗消息
     * @param message
     * @return
     */
    public static byte[] serverMessagePopUp(String message){
        return serverMessage(1,0,message,false);
    }

    /**
     * 带白色背景消息
     * @param message
     * @return
     */
    public static byte[] serverMessageMega(String message){
        return serverMessage(2,0,message,false);
    }

    /**
     * 全服一般喇叭
     * @param message
     * @return
     */
    public static byte[] serverMessageSmega(String message,int channel,boolean megeEar){
        return serverMessage(3,channel,message,megeEar);
    }

    /**
     * 顶部公告
     * @param message
     * @return
     */
    public static byte[] serverMessageTop(String message){
        return serverMessage(4,0,message,false);
    }

    /**
     * 红字消息
     * @param message
     * @return
     */
    public static byte[] serverMessageRedText(String message){
        return serverMessage(5,0,message,false);
    }


    /**
     *
     * @param type
     * @param channel
     * @param message
     * @param megaEar 是否私聊
     * @return
     */
    private static byte[] serverMessage(int type, int channel, String message, boolean megaEar) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(type > 5 ? type == 6 ? 0 : 5 : type); // v40 beta only has 0: [Notice], 1: Popup, 2: Mega, 3: Smega, 4: Header, and 5: Red text
        if (type == 4) {
            mplew.write(1); // 1-开启 0-关闭
        }
        mplew.writeMapleAsciiString(message);
        switch (type) {
            case 3:
                mplew.write(channel - 1);
                mplew.write(megaEar ? 1 : 0);
                break;
            default:break;
        }
        return mplew.getPacket();
    }

    public static byte[] getChatText(int cidfrom, String text, boolean whiteBG, int show) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CHATTEXT.getValue());
        mplew.writeInt(cidfrom);
        mplew.write(whiteBG ? 1 : 0);
        mplew.writeMapleAsciiString(text);

        return mplew.getPacket();
    }

    public static byte[] GameMaster_Func(int value) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GM_EFFECT.getValue());
        mplew.write(value);
        mplew.writeZero(17);

        return mplew.getPacket();
    }

    public static byte[] ShowAranCombo(int combo) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.ARAN_COMBO.getValue());
        mplew.writeInt(combo);

        return mplew.getPacket();
    }

    public static byte[] rechargeCombo(int value) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.ARAN_COMBO_RECHARGE.getValue());
        mplew.writeInt(value);

        return mplew.getPacket();
    }

    public static byte[] getPacketFromHexString(String hex) {
        return HexTool.getByteArrayFromHexString(hex);
    }

    /**
     * 增加经验
     * @TODO 和GainEXP_Others合并掉
     * @param gain
     * @param 白色
     * @param expStats
     * @return
     */
    public static byte[] GainEXP_Monster(int gain, boolean 白色, Map<MapleExpStat, Integer> expStats) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(3);
        mplew.write(白色 ? 1 : 0);
        mplew.writeInt(gain);
        mplew.write(0); // 是否显示在聊天窗口

        return mplew.getPacket();
    }

    /**
     * 捡取金币消息
     * @param gain
     * @param inChat
     * @return
     */
    public static byte[] showMesoGain(long gain, boolean inChat) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(0);
        mplew.write(1); // 捡到金币
        mplew.writeInt((int)gain);

        return mplew.getPacket();
    }

    public static byte[] getShowItemGain(int itemId, short quantity) {
        return getShowItemGain(itemId, quantity, false);
    }

    /**
     * 捡取道具消息
     * @param itemId
     * @param quantity
     * @param inChat
     * @return
     */
    public static byte[] getShowItemGain(int itemId, short quantity, boolean inChat) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(0);
        mplew.write(0); //-2：你不能再取得这种道具，非0 不能捡取物品
        mplew.writeInt(itemId);
        mplew.writeInt(quantity);
        return mplew.getPacket();
    }

    public static byte[] getShowItemGain(Map<Integer, Integer> showItems) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
        mplew.write(0x6);
        mplew.write(showItems.size());
        for (Map.Entry items : showItems.entrySet()) {
            mplew.writeInt(((Integer) items.getKey()));
            mplew.writeInt(((Integer) items.getValue()));
        }
        return mplew.getPacket();
    }

    public static byte[] getShowItemGain(List<Pair<Integer, Integer>> showItems) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
        mplew.write(0x6);
        mplew.write(showItems.size());
        for (Pair items : showItems) {
            mplew.writeInt(((Integer) items.left));
            mplew.writeInt(((Integer) items.right));
        }
        return mplew.getPacket();
    }

    /**
     * 普通道具过期消息
     * @param itemId
     * @return
     */
    public static byte[] showItemExpired(int itemId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(5);
        mplew.write(1);// 道具个数，支持多个
        mplew.writeInt(itemId);

        return mplew.getPacket();
    }

    /**
     * 现金道具过期清除消息
     * @param itemId
     * @return
     */
    public static byte[] showCashItemExpired(int itemId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(2);
        mplew.writeInt(itemId);

        return mplew.getPacket();
    }

    public static byte[] showRewardItemAnimation(int itemId, String effect) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
        mplew.write(0x13);
        mplew.writeInt(itemId);
        mplew.write((effect != null) && (effect.length() > 0) ? 1 : 0);
        if ((effect != null) && (effect.length() > 0)) {
            mplew.writeMapleAsciiString(effect);
        }

        return mplew.getPacket();
    }

    public static byte[] showRewardItemAnimation(int itemId, String effect, int from_playerid) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(from_playerid);
        mplew.write(18);
        mplew.writeInt(itemId);
        mplew.write((effect != null) && (effect.length() > 0) ? 1 : 0);
        if ((effect != null) && (effect.length() > 0)) {
            mplew.writeMapleAsciiString(effect);
        }

        return mplew.getPacket();
    }

    /**
     * 召唤玩家
     * @param chr
     * @return
     */
    public static byte[] spawnPlayerMapobject(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SPAWN_PLAYER.getValue());
        mplew.writeInt(chr.getId());
        mplew.writeMapleAsciiString(chr.getName());
        int buffmask = 0;
        Integer buffvalue = null;

//        if (chr.getBuffedValue(MapleBuffStat.DARKSIGHT) != null && !chr.isHidden()) {
//            buffmask |= MapleBuffStat.DARKSIGHT.getValue();
//        }
        if (chr.getBuffedValue(MapleBuffStat.斗气集中) != null) {
            buffmask |= MapleBuffStat.斗气集中.getValue();
            buffvalue = Integer.valueOf(chr.getBuffedValue(MapleBuffStat.斗气集中).intValue());
        }
//        if (chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null) {
//            buffmask |= MapleBuffStat.SHADOWPARTNER.getValue();
//        }

        mplew.writeInt(buffmask);

        PacketHelper.addCharLook(mplew, chr, true, chr.isZeroSecondLook());

        mplew.writeInt(0);
        mplew.writeInt(chr.getItemEffect());
        mplew.writeInt(chr.getChair());
        mplew.writeShort(chr.getPosition().x);
        mplew.writeShort(chr.getPosition().y);
        mplew.write(chr.getStance());
        mplew.writeShort(0); // FH


//        if (chr.getSpawnPets() != null) {
//            mplew.write(1);
//            MaplePet pet = chr.getSpawnPets()[0];
//            mplew.writeInt(pet.getPetItemId());
//            mplew.writeMapleAsciiString(pet.getName());
//            mplew.writeLong(pet.getUniqueId());
//            mplew.writeShort(pet.getPos().x);
//            mplew.writeShort(pet.getPos().y);
//            mplew.write(pet.getStance());
//            mplew.writeShort(pet.getFh());
//        } else {
            mplew.write(0);
//        }

        // Mini Game & Interaction Boxes
//        if (chr.getPlayerShop() != null && chr.getPlayerShop().isOwner(chr)) {
//            addShopBox(mplew, chr.getPlayerShop());
//        } else if ((MapleMiniGame) chr.getInteraction() != null) {
//            addAnnounceBox(mplew, chr.getInteraction());
//        } else {
            mplew.write(0);
//        }
        //List<MapleRing> rings = getRing(chr);
//        mplew.write(rings.size());
//        for (MapleRing ring : rings) {
//            mplew.writeInt(ring.getRingId());
//            mplew.writeInt(0);
//            mplew.writeInt(ring.getPartnerRingId());
//            mplew.writeInt(0);
//            mplew.writeInt(ring.getItemId());
//        }
        Triple rings = chr.getRings(false);
        addRingInfo(mplew, (List) rings.getLeft());
        return mplew.getPacket();
    }

    public static void addMountId(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, int buffSrc) {
        Item c_mount = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -123);
        Item mount = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -18);
        int mountId = GameConstants.getMountItem(buffSrc, chr);
        if ((mountId == 0) && (c_mount != null) && (chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -124) != null)) {
            mplew.writeInt(c_mount.getItemId());
        } else if ((mountId == 0) && (mount != null) && (chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -19) != null)) {
            mplew.writeInt(mount.getItemId());
        } else {
            mplew.writeInt(mountId);
        }
    }

    public static byte[] removePlayerFromMap(int chrId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.REMOVE_PLAYER_FROM_MAP.getValue());
        mplew.writeInt(chrId);

        return mplew.getPacket();
    }

    public static byte[] facialExpression(MapleCharacter from, int expression) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.FACIAL_EXPRESSION.getValue());
        mplew.writeInt(from.getId());
        mplew.writeInt(expression);

        return mplew.getPacket();
    }

    /**
     * 玩家移动
     * @param chrId
     * @return
     */
    public static byte[] movePlayer(int chrId, SeekableLittleEndianAccessor slea) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MOVE_PLAYER.getValue());
        mplew.writeInt(chrId);
        mplew.write(slea.read((int)slea.available()));

        return mplew.getPacket();
    }

    public static byte[] closeRangeAttack(MapleCharacter chr, int skilllevel, int itemId, AttackInfo attackInfo, boolean hasMoonBuff) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CLOSE_RANGE_ATTACK.getValue());
        addAttackBody(mplew, chr, skilllevel, itemId, attackInfo, hasMoonBuff, false);

        return mplew.getPacket();
    }

    public static byte[] passiveAttack(MapleCharacter chr, int skilllevel, int itemId, AttackInfo attackInfo, boolean hasMoonBuff) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PASSIVE_ATTACK.getValue());
        addAttackBody(mplew, chr, skilllevel, itemId, attackInfo, hasMoonBuff, false);

        return mplew.getPacket();
    }

    public static byte[] rangedAttack(MapleCharacter chr, int skilllevel, int itemId, AttackInfo attackInfo) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.RANGED_ATTACK.getValue());
        addAttackBody(mplew, chr, skilllevel, itemId, attackInfo, false, true);
        if ((attackInfo.skillId >= 100000000) || attackInfo.skillId == 13121052) {
            mplew.writeInt(attackInfo.position.x);
            mplew.writeInt(attackInfo.position.y);
        } else if (attackInfo.skillposition != null) {
            mplew.writePos(attackInfo.skillposition);
        }

        return mplew.getPacket();
    }

    public static byte[] magicAttack(MapleCharacter chr, int skilllevel, int itemId, AttackInfo attackInfo) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MAGIC_ATTACK.getValue());
        addAttackBody(mplew, chr, skilllevel, itemId, attackInfo, false, false);
        if (attackInfo.charge > 0) {
            mplew.writeInt(attackInfo.charge);
        }

        return mplew.getPacket();
    }

    public static void addAttackBody(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, int skilllevel, int itemId, AttackInfo attackInfo, boolean hasMoonBuff, boolean RangedAttack) {
        int skillId = attackInfo.skillId;
        mplew.writeInt(chr.getId());
        mplew.write(attackInfo.numAttackedAndDamage);
        if (skillId > 0) {
            mplew.write(skilllevel);
            mplew.writeInt(skillId);
        } else {
            mplew.write(0);
        }
        mplew.write(attackInfo.stance);
        mplew.write(attackInfo.direction);
        mplew.write(attackInfo.speed);
        mplew.writeInt(itemId);
        for (AttackPair oned : attackInfo.allDamage) {
            if (oned.attack != null) {
                mplew.writeInt(oned.objectid);
                mplew.write(6);
                for (Pair eachd : oned.attack) {
                    if (((Boolean) eachd.right)) {
                        mplew.writeInt(((Integer) eachd.left) + -2147483648);
                    } else {
                        mplew.writeInt(((Integer) eachd.left));
                    }
                }
            }
        }
        mplew.writeInt(chr.getOldPosition().x);
        mplew.writeInt(chr.getOldPosition().y);
    }

    // 有些特殊攻击不应在其他玩家那边显示攻击
    public static int Attacktype(int skillId) {
        switch (skillId) {
            case 2121054:
            case 65121052:
                return 4;
            default:
                return 0;
        }
    }

    public static byte[] showSpecialAttack(int chrId, int tickCount, int pot_x, int pot_y, int display, int skillId, int skilllevel, boolean isLeft, int speed) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_SPECIAL_ATTACK.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(tickCount);
        mplew.writeInt(pot_x);
        mplew.writeInt(pot_y);
        mplew.writeInt(display);
        mplew.writeInt(skillId);
        mplew.writeInt(0);//新增加 还不知道是什么
        mplew.writeInt(skilllevel);
        mplew.write(isLeft ? 1 : 0);
        mplew.writeInt(speed);

        return mplew.getPacket();
    }

    /**
     * 更新角色外观
     * @param chr
     * @return
     */
    public static byte[] updateCharLook(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.UPDATE_CHAR_LOOK.getValue());
        mplew.writeInt(-1); // flag
        mplew.write(0);
        mplew.writeInt(chr.getId());
        mplew.write(1);
        PacketHelper.addCharLook(mplew, chr, false, chr.isZeroSecondLook());
        mplew.writeInt(0); // Unknown: 4bytes (int)
        mplew.write(0); // Unknown: 8bytes (int, int)
        mplew.write(0); // Unknown: 1bytes (byte)

        Triple rings = chr.getRings(false);
        addMRingInfo(mplew, (List) rings.getRight(), chr);
        return mplew.getPacket();
    }

    public static byte[] removeZeroFromMap(int chrId) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.REMOVE_ZERO_FROM_MAP.getValue());
        mplew.writeInt(chrId);

        return mplew.getPacket();
    }

    /**
     * 添加戒指相关数据
     * @param mplew
     * @param rings
     */
    public static void addRingInfo(MaplePacketLittleEndianWriter mplew, List<MapleRing> rings) {
        mplew.write(rings.size());
        for (MapleRing ring : rings) {
            mplew.writeLong(ring.getRingId());
            mplew.writeInt(0);
            mplew.writeLong(ring.getPartnerRingId());
            mplew.writeInt(0);
            mplew.writeInt(ring.getItemId());
        }
    }

    /**
     * 结婚戒指信息
     * @param mplew
     * @param rings
     * @param chr
     */
    public static void addMRingInfo(MaplePacketLittleEndianWriter mplew, List<MapleRing> rings, MapleCharacter chr) {
        mplew.write(rings.size());
        for (MapleRing ring : rings) {
            mplew.writeLong(chr.getId());
            mplew.writeLong(ring.getPartnerChrId());
//            mplew.writeInt(ring.getItemId());
        }
    }

    public static byte[] damagePlayer(int chrId, int type, int damage, int monsteridfrom, byte direction, int skillid, int pDMG, boolean pPhysical, int pID, byte pType, Point pPos, byte offset, int offset_d, int fake) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.DAMAGE_PLAYER.getValue());
        mplew.writeInt(chrId);
        mplew.write(type);
        mplew.writeInt(damage);
        mplew.write(0);
        if (type >= -1) {
            mplew.writeInt(monsteridfrom);
            mplew.write(direction);
            mplew.writeInt(skillid);
            mplew.writeInt(pDMG);
            mplew.write(0);
            if (pDMG > 0) {
                mplew.write(pPhysical ? 1 : 0);
                mplew.writeInt(pID);
                mplew.write(pType);
                mplew.writePos(pPos);
            }
            mplew.write(offset);
            if (offset == 1) {
                mplew.writeInt(offset_d);
            }
        }
        mplew.writeInt(damage);
        if ((damage <= 0) || (fake > 0)) {
            mplew.writeInt(fake);
        }
        return mplew.getPacket();
    }

    public static byte[] damagePlayer(int chrId, int type, int monsteridfrom, int damage) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.DAMAGE_PLAYER.getValue());
        mplew.writeInt(chrId);
        mplew.write(type);
        mplew.writeInt(damage);
        mplew.write(0);
        mplew.writeInt(monsteridfrom);
        mplew.write(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.write(0);
        mplew.writeInt(damage);

        return mplew.getPacket();
    }

    /**
     * 更新任务状态
     * @param quest
     * @return
     */
    public static byte[] updateQuest(MapleQuestStatus quest) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        byte isUpdateQuest = 1; // 0-移除任务 1-修改任务状态
        mplew.write(isUpdateQuest);
        mplew.writeInt(quest.getQuest().getId());
        if (isUpdateQuest == 1) {
            // 使用对应的状态值即可
            mplew.writeMapleAsciiString(quest.getCustomData() != null ? quest.getCustomData() : "");
        }

        return mplew.getPacket();
    }

    public static byte[] updateQuestInfo(int quest, int npc, boolean updata) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.UPDATE_QUEST_INFO.getValue());
        mplew.write(0x0B);
        mplew.writeShort(quest);
        mplew.writeInt(npc);
        mplew.writeShort(0);
        mplew.write(updata ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] updateQuestFinish(int quest, int npc, int nextquest) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.UPDATE_QUEST_INFO.getValue());
        mplew.write(11);
        mplew.writeShort(quest);
        mplew.writeInt(npc);
        mplew.writeShort(nextquest);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] updateMedalQuestInfo(byte op, int itemId) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.REISSUE_MEDAL.getValue());

        mplew.write(op);
        mplew.writeInt(itemId);

        return mplew.getPacket();
    }

    /**
     * 观察人物信息
     * @param chr
     * @param isSelf
     * @return
     */
    public static byte[] charInfo(MapleCharacter chr, boolean isSelf) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CHAR_INFO.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(chr.getLevel());
        mplew.writeShort(chr.getJob());
        mplew.writeShort(chr.getFame());
        mplew.writeMapleAsciiString("ICELEMON"); // 社区名字
        /*
        MapleRing mRing = chr.getMarriageRing();
        mplew.write(mRing != null ? 1 : 0);
        if (mRing != null) {
            mplew.writeInt(mRing.getRingId());
            mplew.writeInt(chr.getId());
            mplew.writeInt(mRing.getPartnerChrId());
            mplew.writeShort(3);
            mplew.writeInt(mRing.getItemId());
            mplew.writeInt(mRing.getItemId());
            mplew.writeAsciiString(chr.getName(), 13);
            mplew.writeAsciiString(mRing.getPartnerName(), 13);
        }
        */
        mplew.write(0);
        /*
        MaplePet pet = chr.getSpawnPet();
        mplew.write(pet != null ? 1 : 0);
        if (pet != null) {
            mplew.writeInt(pet.getPetItemId());
            mplew.writeMapleAsciiString(pet.getName());
            mplew.write(pet.getLevel());
            mplew.writeShort(pet.getCloseness());
            mplew.write(pet.getFullness());
            mplew.writeInt(0);
        }*/

        mplew.write(0); // wishlist 4*size
//        mplew.writeLong(0); // ring
        return mplew.getPacket();
    }

    public static byte[] updateMount(MapleCharacter chr, boolean levelup) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.UPDATE_MOUNT.getValue());
        mplew.writeInt(chr.getId());
        mplew.writeInt(chr.getMount().getLevel());
        mplew.writeInt(chr.getMount().getExp());
        mplew.writeInt(chr.getMount().getFatigue());
        mplew.write(levelup ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] mountInfo(MapleCharacter chr) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.UPDATE_MOUNT.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(1);
        mplew.writeInt(chr.getMount().getLevel());
        mplew.writeInt(chr.getMount().getExp());
        mplew.writeInt(chr.getMount().getFatigue());

        return mplew.getPacket();
    }

    public static byte[] showForeignEffect(int chrId, int effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(chrId);

        mplew.write(effect);

        return mplew.getPacket();
    }

    public static byte[] showBuffeffect(MapleCharacter chr, int skillid, int effectid, int playerLevel, int skillLevel) {
        return showBuffeffect(chr, skillid, effectid, playerLevel, skillLevel, (byte) 3);
    }

    public static byte[] showBuffeffect(MapleCharacter chr, int skillid, int effectid, int playerLevel, int skillLevel, byte direction) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(effectid);
        mplew.writeInt(skillid);
        mplew.write(playerLevel);
        mplew.write(skillLevel);
        if (direction != 3) {
            mplew.write(direction);
            switch (skillid) {
                case 65121052:
                    mplew.writeInt(chr.getTruePosition().x);
                    mplew.writeInt(chr.getTruePosition().y);
                    mplew.write(1);
            }
        }

        return mplew.getPacket();
    }

    public static byte[] showOwnBuffEffect(int skillid, int effectid, int playerLevel, int skillLevel) {
        return showOwnBuffEffect(skillid, effectid, playerLevel, skillLevel, (byte) 3);
    }

    public static byte[] showOwnBuffEffect(int skillid, int effectid, int skillLevel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
        mplew.write(effectid);
        mplew.writeInt(skillid);
        mplew.write(skillLevel);

        return mplew.getPacket();
    }

    /**
     * 显示自己的BUFF效果
     * @param skillid
     * @param effectid
     * @param playerLevel
     * @param skillLevel
     * @param direction
     * @return
     */
    public static byte[] showOwnBuffEffect(int skillid, int effectid, int playerLevel, int skillLevel, byte direction) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
        mplew.write(effectid);
        mplew.writeInt(skillid);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] showOwnDiceEffect(int skillid, int effectid, int effectid2, int level, int rand) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
        mplew.write(0x4);
        mplew.writeInt(effectid);
        mplew.writeInt(effectid2);
        mplew.writeInt(skillid);
        mplew.write(level);
        mplew.write(rand);

        return mplew.getPacket();
    }

    public static byte[] showDiceEffect(int chrId, int skillid, int effectid, int effectid2, int level) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(chrId);
        mplew.write(3);
        mplew.writeInt(effectid);
        mplew.writeInt(effectid2);
        mplew.writeInt(skillid);
        mplew.write(level);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] showItemLevelupEffect() {
        return showSpecialEffect(0x14);
    }

    public static byte[] showForeignItemLevelupEffect(int chrId) {
        return showSpecialEffect(chrId, 0x14);
    }

    public static byte[] showSpecialEffect(int effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
        mplew.write(effect);

        return mplew.getPacket();
    }

    public static byte[] showSpecialEffect(int chrId, int effect) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(chrId);
        mplew.write(effect);

        return mplew.getPacket();
    }

    public static byte[] updateSkill(int skillid, int level, int masterlevel, long expiration) {
        boolean isProfession = (skillid == 92000000) || (skillid == 92010000) || (skillid == 92020000) || (skillid == 92030000) || (skillid == 92040000);
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.UPDATE_SKILLS.getValue());
        mplew.write(isProfession ? 0 : 1);
        mplew.write(0);
        mplew.write(0);
        mplew.writeShort(1);
        mplew.writeInt(skillid);
        mplew.writeInt(level);
        mplew.writeInt(masterlevel);
        PacketHelper.addExpirationTime(mplew, expiration);
        mplew.write(isProfession ? 4 : 3);

        return mplew.getPacket();
    }

    /**
     * 更新技能等级
     * @param update
     * @return
     */
    public static byte[] updateSkills(Map<Skill, SkillEntry> update) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.UPDATE_SKILLS.getValue());
        mplew.write(1);
        mplew.writeShort(update.size());
        for (Map.Entry skills : update.entrySet()) {
            mplew.writeInt(((Skill) skills.getKey()).getId());
            mplew.writeInt(((SkillEntry) skills.getValue()).skillLevel);
        }
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] updatePetSkill(int skillid, int level, int masterlevel, long expiration) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.UPDATE_SKILLS.getValue());
        mplew.write(0);
        mplew.write(1);
        mplew.write(0);
        mplew.writeShort(1);
        mplew.writeInt(skillid);
        mplew.writeInt(level == 0 ? -1 : level);
        mplew.writeInt(masterlevel);
        PacketHelper.addExpirationTime(mplew, expiration);
        mplew.write(4);

        return mplew.getPacket();
    }


    public static byte[] getShowQuestCompletion(int id) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_QUEST_COMPLETION.getValue());
        mplew.writeShort(id);

        return mplew.getPacket();
    }

    public static byte[] getKeymap(MapleCharacter chr) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.KEYMAP.getValue());
        MapleKeyLayout keymap = chr.getKeyLayout();
        keymap.writeData(mplew, 1);

        return mplew.getPacket();
    }

    public static byte[] petAutoHP(int itemId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PET_AUTO_HP.getValue());
        mplew.writeInt(itemId);

        return mplew.getPacket();
    }

    public static byte[] petAutoMP(int itemId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PET_AUTO_MP.getValue());
        mplew.writeInt(itemId);

        return mplew.getPacket();
    }

    public static byte[] petAutoBuff(int skillId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PET_AUTO_BUFF.getValue());
        mplew.writeInt(skillId);

        return mplew.getPacket();
    }

    public static byte[] openFishingStorage(int npcId, byte slots) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.FISHING_STORE.getValue());
        mplew.write(33);
        mplew.writeLong(-1L);
        mplew.write(slots);
        mplew.writeLong(0L);
        mplew.writeInt(npcId);

        return mplew.getPacket();
    }

    public static byte[] fairyPendantMessage(int position, int percent) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.FAIRY_PEND_MSG.getValue());
        mplew.writeInt(position);
        mplew.writeInt(0);
        mplew.writeInt(percent);

        return mplew.getPacket();
    }

    /**
     * 给玩家增加人气
     * @param mode
     * @param charname
     * @param newfame
     * @return
     */
    public static byte[] giveFameResponse(int mode, String charname, int newfame) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.FAME_RESPONSE.getValue());
        mplew.write(0);
        mplew.writeMapleAsciiString(charname);
        mplew.write(mode); // 1-增加 0-降低
        mplew.writeInt(newfame);

        return mplew.getPacket();
    }

    /**
     * 人气操作失败
     * @param status
     * @return
     */
    public static byte[] giveFameErrorResponse(int status) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.FAME_RESPONSE.getValue());
        mplew.writeInt(status);

        return mplew.getPacket();
    }

    /**
     * 收到人气数据
     * @param mode
     * @param charnameFrom
     * @return
     */
    public static byte[] receiveFame(int mode, String charnameFrom) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.FAME_RESPONSE.getValue());
        mplew.write(5);
        mplew.writeMapleAsciiString(charnameFrom);
        mplew.write(mode); // 0-降低 1-增加

        return mplew.getPacket();
    }

    public static byte[] multiChat(String name, String chattext, int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MULTICHAT.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(name);
        mplew.writeMapleAsciiString(chattext);

        return mplew.getPacket();
    }

    public static byte[] getClockType0(int time) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CLOCK.getValue());
        mplew.write(0);
        mplew.writeInt(time);

        return mplew.getPacket();
    }

    public static byte[] getClock(int time) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CLOCK.getValue());
        mplew.write(2);
        mplew.writeInt(time);

        return mplew.getPacket();
    }

    public static byte[] getClockTime(int hour, int min, int sec) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CLOCK.getValue());
        mplew.write(1);
        mplew.write(hour);
        mplew.write(min);
        mplew.write(sec);

        return mplew.getPacket();
    }

    public static byte[] stopClock() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.STOP_CLOCK.getValue());

        return mplew.getPacket();
    }

    /**
     * 召唤迷雾
     * @param mist
     * @return
     */
    public static byte[] spawnMist(MapleDefender mist) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SPAWN_DEFENDER.getValue());
        mplew.writeInt(mist.getObjectId());
        mplew.write(mist.getMistType());
        if (mist.getMobSkill() == null) {
            mplew.writeInt(mist.getSourceSkill().getId());
        } else {
            mplew.writeInt(mist.getMobSkill().getSkillId());
        }
        mplew.write(mist.getSkillLevel());
        mplew.writeShort(mist.getSkillDelay());
        mplew.writeRect(mist.getBox());

        return mplew.getPacket();
    }

    /**
     * 取消迷雾
     * @param oid
     * @param eruption
     * @return
     */
    public static byte[] removeMist(int oid, boolean eruption) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.REMOVE_DEFENDER.getValue());
        mplew.writeInt(oid);

        return mplew.getPacket();
    }

    /**
     * 召唤爱心
     * @param oid
     * @param itemid
     * @param name
     * @param msg
     * @param pos
     * @param ft
     * @return
     */
    public static byte[] spawnLove(int oid, int itemid, String name, String msg, Point pos, int ft) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SPAWN_LOVE.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(itemid);
        mplew.writeMapleAsciiString(msg);
        mplew.writeMapleAsciiString(name);
        mplew.writeShort(pos.x);
        mplew.writeShort(pos.y + ft);

        return mplew.getPacket();
    }

    /**
     * 移除爱心
     * @param oid
     * @param itemid
     * @return
     */
    public static byte[] removeLove(int oid, int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.REMOVE_LOVE.getValue());
        mplew.write(0);
        mplew.writeInt(oid);
        //mplew.writeInt(itemid);

        return mplew.getPacket();
    }

    public static byte[] itemEffect(int chrId, int itemid) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_ITEM_EFFECT.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(itemid);

        return mplew.getPacket();
    }

    public static byte[] showTitleEffect(int chrId, int itemid) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_TITLE_EFFECT.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(itemid);
        mplew.writeLong(0);
        return mplew.getPacket();
    }

    public static byte[] showUnkEffect(int chrId, int itemid) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_UNK_EFFECT.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(itemid);

        return mplew.getPacket();
    }

    /**
     * 坐椅子
     * @param characterid
     * @param itemid
     * @return
     */
    public static byte[] showChair(int characterid, int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_CHAIR.getValue());
        mplew.write(itemid == -1 ? 0 : 1);
        if (itemid != -1) {
            mplew.writeShort(itemid);
        };

        return mplew.getPacket();
    }

    public static byte[] cancelChair(int id, int cid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CANCEL_CHAIR.getValue());
        if (id == -1) {
            mplew.writeInt(cid);
            mplew.write(0);
        } else {
            mplew.writeInt(cid);
            mplew.write(1);
            mplew.writeShort(id);
        }

        return mplew.getPacket();
    }

    /**
     * 召唤反应堆
     * @param reactor
     * @return
     */
    public static byte[] spawnReactor(MapleReactor reactor) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.REACTOR_SPAWN.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.writeInt(reactor.getReactorId());
        mplew.write(reactor.getState());
        mplew.writePos(reactor.getTruePosition());
        mplew.write(reactor.getFacingDirection());
//        mplew.writeMapleAsciiString(reactor.getName());

        return mplew.getPacket();
    }

    /**
     * 敲打反应堆
     * @param reactor
     * @param stance
     * @return
     */
    public static byte[] triggerReactor(MapleReactor reactor, int stance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.REACTOR_HIT.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.write(reactor.getState());

        mplew.writePos(reactor.getTruePosition());
        mplew.writeShort(stance);
        mplew.write(0);
        mplew.write(0);

        return mplew.getPacket();
    }

    /**
     * 销毁反应堆
     * @param reactor
     * @return
     */
    public static byte[] destroyReactor(MapleReactor reactor) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.REACTOR_DESTROY.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.write(reactor.getState());
        mplew.writePos(reactor.getPosition());

        return mplew.getPacket();
    }

    public static byte[] musicChange(String song) {
        return startMapSoundEffect(song);
    }

    public static byte[] showEffect(String effect) {
        return environmentChange(effect, 0x0C);
    }

    public static byte[] playSound(String sound) {
        return environmentChange(sound, 5);
    }

    public static byte[] environmentChange(String env, int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.BOSS_ENV.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(env);

        return mplew.getPacket();
    }

    public static byte[] environmentMove(String env, int mode) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MOVE_ENV.getValue());
        mplew.writeMapleAsciiString(env);
        mplew.writeInt(mode);

        return mplew.getPacket();
    }

    public static byte[] startMapEffect(String msg, int itemid, boolean active) {
        return startMapEffect(msg, 0, -1, active);
    }

    /**
     * 播放地图效果
     * @param msg
     * @param itemid
     * @param effectType
     * @param active
     * @return
     */
    public static byte[] startMapEffect(String msg, int itemid, int effectType, boolean active) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MAP_EFFECT.getValue());
        mplew.write(active ? 0 : 1);
        mplew.writeInt(itemid);

        return mplew.getPacket();
    }

    public static byte[] startMapSummonEffect(){
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MAP_EFFECT.getValue());
        mplew.write(0);

        mplew.write(0);
        mplew.writeInt(0);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] startMap2Effect(){
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MAP_EFFECT.getValue());
        mplew.write(2);

        mplew.write(0);
        mplew.writeMapleAsciiString("icelemon1314");

        return mplew.getPacket();
    }

    public static byte[] startShopBackgroundEffect(){
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MAP_EFFECT.getValue());
        mplew.write(3);

        mplew.writeMapleAsciiString("icelemon1314");

        return mplew.getPacket();
    }

    public static byte[] startMap4Effect(){
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MAP_EFFECT.getValue());
        mplew.write(4);

        mplew.writeMapleAsciiString("icelemon1314");

        return mplew.getPacket();
    }

    public static byte[] startMap5Effect(){
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MAP_EFFECT.getValue());
        mplew.write(5);

        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    /**
     * 播放背景音乐
     * @param soundPath
     * @return
     */
    public static byte[] startMapSoundEffect(String soundPath){
        // sound/Object.img/Whistle 轮船来了
        // sound/Game.img/LevelUp
        // Sound/Jukebox.img/Congratulation 音乐盒
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MAP_EFFECT.getValue());
        mplew.write(6);

        mplew.writeMapleAsciiString(soundPath);

        return mplew.getPacket();
    }

    public static byte[] startFullScreenBless(int itemId,String bless){
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.FULLSCREEN_BLESS.getValue());

        mplew.writeInt(itemId);
        mplew.writeMapleAsciiString(bless);

        return mplew.getPacket();

    }

    public static byte[] stopFullScreenBless(){
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.FULLSCREEN_BLESS.getValue());

        mplew.writeInt(0);
        return mplew.getPacket();

    }

    public static byte[] removeMapEffect() {
        return startMapEffect(null, 0, -1, false);
    }

    public static byte[] showPredictCard(String name, String otherName, int love, int cardId, int commentId) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_PREDICT_CARD.getValue());
        mplew.writeMapleAsciiString(name);
        mplew.writeMapleAsciiString(otherName);
        mplew.writeInt(love);
        mplew.writeInt(cardId);
        mplew.writeInt(commentId);

        return mplew.getPacket();
    }

    public static byte[] skillEffect(int fromId, int skillId, byte level, byte display, byte direction, byte speed, Point position) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SKILL_EFFECT.getValue());
        mplew.writeInt(fromId);
        mplew.writeInt(skillId);
        mplew.write(level);
        mplew.write(display);
        mplew.write(direction);
        mplew.write(speed);
        if (position != null) {
            mplew.writePos(position);
        }

        return mplew.getPacket();
    }

    public static byte[] skillCancel(MapleCharacter from, int skillId) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CANCEL_SKILL_EFFECT.getValue());
        mplew.writeInt(from.getId());
        mplew.writeInt(skillId);

        return mplew.getPacket();
    }

    public static byte[] sendHint(String hint, int width, int height) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (width < 1) {
            width = hint.length() * 10;
            if (width < 40) {
                width = 40;
            }
        }
        if (height < 5) {
            height = 5;
        }
        mplew.write(SendPacketOpcode.PLAYER_HINT.getValue());
        mplew.writeMapleAsciiString(hint);
        mplew.writeShort(width);
        mplew.writeShort(height);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] showEquipEffect() {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_EQUIP_EFFECT.getValue());

        return mplew.getPacket();
    }

    public static byte[] showEquipEffect(int team) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_EQUIP_EFFECT.getValue());
        mplew.writeShort(team);

        return mplew.getPacket();
    }

    public static byte[] skillCooldown(int skillId, int time) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.COOLDOWN.getValue());
        mplew.writeInt(skillId);
        mplew.writeInt(time);

        return mplew.getPacket();
    }

    public static byte[] useSkillBook(MapleCharacter chr, int skillid, int maxlevel, boolean canuse, boolean success) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.USE_SKILL_BOOK.getValue());
        mplew.write(0);
        mplew.writeInt(chr.getId());
        mplew.write(1);
        mplew.writeInt(skillid);
        mplew.writeInt(maxlevel);
        mplew.write(canuse ? 1 : 0);
        mplew.write(success ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] updateAriantPQRanking(String name, int score, boolean empty) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.ARIANT_PQ_START.getValue());
        mplew.write(empty ? 0 : 1);
        if (!empty) {
            mplew.writeMapleAsciiString(name);
            mplew.writeInt(score);
        }

        return mplew.getPacket();
    }

    public static byte[] showAriantScoreBoard() {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.ARIANT_SCOREBOARD.getValue());

        return mplew.getPacket();
    }

    /**
     * 控制船的来和走
     * @param isEnter
     * @return
     */
    public static byte[] boatPacket(boolean isEnter) {
        // 3C 0D 07 船来了
        // 3C 09 03 船走了
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.BOAT_EFFECT.getValue());
        if (isEnter == true) {
            mplew.write(0x0D);
            mplew.write(0x07);
        } else {
            mplew.write(0x09);
            mplew.write(0x03);
        }
        return mplew.getPacket();
    }

    /**
     * 蝙蝠魔的船
     * @param isEnter
     * @return
     */
    public static byte[] MonsterBoat(boolean isEnter){
        // 3C 0B 05 // 蝙蝠魔的船来了
        // 3C 0B 06 // 蝙蝠魔的船走了
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.BOAT_EFFECT.getValue());
        if (isEnter == true) {
            mplew.write(0x0B);
            mplew.write(0x05);
        } else {
            mplew.write(0x0B);
            mplew.write(0x06);
        }
        return mplew.getPacket();
    }

    public static byte[] boatEffect(int effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.BOAT_EFF.getValue());
        mplew.writeMapleAsciiString("ship/ossyria");
        mplew.writeShort(effect);

        return mplew.getPacket();
    }

    public static byte[] removeItemFromDuey(boolean remove, int Package) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.DUEY.getValue());
        mplew.write(24);
        mplew.writeInt(Package);
        mplew.write(remove ? 3 : 4);

        return mplew.getPacket();
    }

    public static byte[] sendDuey(byte operation, List<MapleDueyActions> packages) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.DUEY.getValue());
        mplew.write(operation);
        switch (operation) {
            case 9:
                mplew.write(1);

                break;
            case 10:
                mplew.write(0);
                mplew.write(packages.size());
                for (MapleDueyActions dp : packages) {
                    mplew.writeInt(dp.getPackageId());
                    mplew.writeAsciiString(dp.getSender(), 13);
                    mplew.writeInt(dp.getMesos());
                    mplew.writeLong(PacketHelper.getTime(dp.getSentTime()));
                    mplew.writeZero(202);
                    if (dp.getItem() != null) {
                        mplew.write(1);
                        PacketHelper.addItemInfo(mplew, dp.getItem());
                    } else {
                        mplew.write(0);
                    }
                }
                mplew.write(0);
        }

        return mplew.getPacket();
    }

    public static byte[] enableTV() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.ENABLE_TV.getValue());
        mplew.writeInt(0);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] removeTV() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.REMOVE_TV.getValue());

        return mplew.getPacket();
    }

    public static byte[] sendTV(MapleCharacter chr, List<String> messages, int type, MapleCharacter partner, int delay) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.START_TV.getValue());
        mplew.write(partner != null ? 2 : 1);
        mplew.write(type);
        PacketHelper.addCharLook(mplew, chr, false, chr.isZeroSecondLook());
        mplew.writeMapleAsciiString(chr.getName());

        if (partner != null) {
            mplew.writeMapleAsciiString(partner.getName());
        } else {
            mplew.writeShort(0);
        }
        for (int i = 0; i < messages.size(); i++) {
            if ((i == 4) && (((String) messages.get(4)).length() > 15)) {
                mplew.writeMapleAsciiString(((String) messages.get(4)).substring(0, 15));
            } else {
                mplew.writeMapleAsciiString((String) messages.get(i));
            }
        }
        mplew.writeInt(delay);
        if (partner != null) {
            PacketHelper.addCharLook(mplew, partner, false, partner.isZeroSecondLook());
        }

        return mplew.getPacket();
    }

    public static byte[] Mulung_DojoUp2() {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
        mplew.write(0x0C);

        return mplew.getPacket();
    }

    public static byte[] showQuestMsg(String msg) {
        return serverMessageNotice(msg);
    }

    public static byte[] Mulung_Pts(int recv, int total) {
        return showQuestMsg(new StringBuilder().append("获得了 ").append(recv).append(" 点修炼点数。总修炼点数为 ").append(total).append(" 点。").toString());
    }

    public static byte[] showOXQuiz(int questionSet, int questionId, boolean askQuestion) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.OX_QUIZ.getValue());
        mplew.write(askQuestion ? 1 : 0);
        mplew.write(questionSet);
        mplew.writeShort(questionId);

        return mplew.getPacket();
    }

    public static byte[] leftKnockBack() {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.LEFT_KNOCK_BACK.getValue());

        return mplew.getPacket();
    }

    public static byte[] rollSnowball(int type, MapleSnowball.MapleSnowballs ball1, MapleSnowball.MapleSnowballs ball2) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.ROLL_SNOWBALL.getValue());
        mplew.write(type);
        mplew.writeInt(ball1 == null ? 0 : ball1.getSnowmanHP() / 75);
        mplew.writeInt(ball2 == null ? 0 : ball2.getSnowmanHP() / 75);
        mplew.writeShort(ball1 == null ? 0 : ball1.getPosition());
        mplew.write(0);
        mplew.writeShort(ball2 == null ? 0 : ball2.getPosition());
        mplew.writeZero(11);

        return mplew.getPacket();
    }

    public static byte[] enterSnowBall() {
        return rollSnowball(0, null, null);
    }

    public static byte[] hitSnowBall(int team, int damage, int distance, int delay) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.HIT_SNOWBALL.getValue());
        mplew.write(team);
        mplew.writeShort(damage);
        mplew.write(distance);
        mplew.write(delay);

        return mplew.getPacket();
    }

    public static byte[] snowballMessage(int team, int message) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SNOWBALL_MESSAGE.getValue());
        mplew.write(team);
        mplew.writeInt(message);

        return mplew.getPacket();
    }

    public static byte[] finishedSort(int type) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }

        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.FINISH_SORT.getValue());
        mplew.write(1);
        mplew.write(type);

        return mplew.getPacket();
    }

    public static byte[] coconutScore(int[] coconutscore) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.COCONUT_SCORE.getValue());
        mplew.writeShort(coconutscore[0]);
        mplew.writeShort(coconutscore[1]);

        return mplew.getPacket();
    }

    public static byte[] hitCoconut(boolean spawn, int id, int type) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.HIT_COCONUT.getValue());
        if (spawn) {
            mplew.write(0);
            mplew.writeInt(128);
        } else {
            mplew.writeInt(id);
            mplew.write(type);
        }

        return mplew.getPacket();
    }

    public static byte[] finishedGather(int type) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }

        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.FINISH_GATHER.getValue());
        mplew.write(1);
        mplew.write(type);

        return mplew.getPacket();
    }

    public static byte[] yellowChat(String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SPOUSE_MESSAGE.getValue());
        mplew.writeShort(7);
        mplew.writeMapleAsciiString(msg);

        return mplew.getPacket();
    }

    public static byte[] getPeanutResult(int itemId, short quantity, int itemId2, short quantity2, int ourItem) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PIGMI_REWARD.getValue());
        mplew.writeInt(itemId);
        mplew.writeShort(quantity);
        mplew.writeInt(ourItem);
        mplew.writeInt(itemId2);
        mplew.writeInt(quantity2);

        return mplew.getPacket();
    }

    public static byte[] sendLevelup(boolean family, int level, String name) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.LEVEL_UPDATE.getValue());
        mplew.write(family ? 1 : 2);
        mplew.writeInt(level);
        mplew.writeMapleAsciiString(name);

        return mplew.getPacket();
    }

    public static byte[] sendMarriage(boolean family, String name) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MARRIAGE_UPDATE.getValue());
        mplew.write(family ? 1 : 0);
        mplew.writeMapleAsciiString(name);

        return mplew.getPacket();
    }

    public static byte[] sendJobup(boolean family, int jobid, String name) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.JOB_UPDATE.getValue());
        mplew.write(family ? 1 : 0);
        mplew.writeInt(jobid);
        mplew.writeMapleAsciiString(new StringBuilder().append((GameConstants.GMS) && (!family) ? "> " : "").append(name).toString());

        return mplew.getPacket();
    }

    public static byte[] showHorntailShrine(boolean spawned, int time) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.HORNTAIL_SHRINE.getValue());
        mplew.write(spawned ? 1 : 0);
        mplew.writeInt(time);

        return mplew.getPacket();
    }

    public static byte[] showChaosZakumShrine(boolean spawned, int time) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CHAOS_ZAKUM_SHRINE.getValue());
        mplew.write(spawned ? 1 : 0);
        mplew.writeInt(time);

        return mplew.getPacket();
    }

    public static byte[] showChaosHorntailShrine(boolean spawned, int time) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CHAOS_HORNTAIL_SHRINE.getValue());
        mplew.write(spawned ? 1 : 0);
        mplew.writeInt(time);

        return mplew.getPacket();
    }


    public static byte[] temporaryStats_Aran() {
        Map stats = new EnumMap(MapleStat.Temp.class);
        stats.put(MapleStat.Temp.力量, 999);
        stats.put(MapleStat.Temp.敏捷, 999);
        stats.put(MapleStat.Temp.智力, 999);
        stats.put(MapleStat.Temp.运气, 999);
        stats.put(MapleStat.Temp.物攻, 255);
        stats.put(MapleStat.Temp.命中, 999);
        stats.put(MapleStat.Temp.回避, 999);
        stats.put(MapleStat.Temp.速度, 140);
        stats.put(MapleStat.Temp.跳跃, 120);
        return temporaryStats(stats);
    }

    public static byte[] temporaryStats_Balrog(MapleCharacter chr) {
        Map stats = new EnumMap(MapleStat.Temp.class);
        int offset = 1 + (chr.getLevel() - 90) / 20;

        stats.put(MapleStat.Temp.力量, chr.getStat().getTotalStr() / offset);
        stats.put(MapleStat.Temp.敏捷, chr.getStat().getTotalDex() / offset);
        stats.put(MapleStat.Temp.智力, chr.getStat().getTotalInt() / offset);
        stats.put(MapleStat.Temp.运气, chr.getStat().getTotalLuk() / offset);
        stats.put(MapleStat.Temp.物攻, chr.getStat().getTotalWatk() / offset);
        stats.put(MapleStat.Temp.物防, chr.getStat().getTotalMagic() / offset);
        return temporaryStats(stats);
    }

    public static byte[] temporaryStats(Map<MapleStat.Temp, Integer> mystats) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.TEMP_STATS.getValue());

        int updateMask = 0;
        for (MapleStat.Temp statupdate : mystats.keySet()) {
            updateMask |= statupdate.getValue();
        }
        mplew.writeInt(updateMask);

        for (Map.Entry statupdate : mystats.entrySet()) {
            Integer value = ((MapleStat.Temp) statupdate.getKey()).getValue();
            if (value >= 1) {
                if (value <= 512) {
                    mplew.writeShort(((Integer) statupdate.getValue()).shortValue());
                } else {
                    mplew.write(((Integer) statupdate.getValue()).byteValue());
                }
            }
        }

        return mplew.getPacket();
    }

    public static byte[] temporaryStats_Reset() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.TEMP_STATS_RESET.getValue());

        return mplew.getPacket();
    }

    public static byte[] showHpHealed(int chrId, int amount) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(chrId);
        mplew.write(31);
        mplew.writeInt(amount);

        return mplew.getPacket();
    }

    public static byte[] showOwnHpHealed(int amount) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
        mplew.write(0x20);
        mplew.writeInt(amount);

        return mplew.getPacket();
    }

    public static byte[] showBlessOfDarkness(int skillId) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
        mplew.write(0x8);
        mplew.writeInt(skillId);

        return mplew.getPacket();
    }

    public static byte[] showHolyFountain(int skillId) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
        mplew.write(0x3);
        mplew.writeInt(skillId);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] sendLinkSkillWindow(int skillId) {
        return sendUIWindow(3, skillId);
    }

    public static byte[] sendPartyWindow(int npc) {
        return sendUIWindow(21, npc);
    }

    public static byte[] sendRepairWindow(int npc) {
        return sendUIWindow(33, npc);
    }

    public static byte[] sendProfessionWindow(int npc) {
        return sendUIWindow(42, npc);
    }

    public static byte[] sendUIWindow(int op, int npc) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.OPEN_UI_OPTION.getValue());

        mplew.writeInt(op);
        mplew.writeInt(npc);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] sendPVPWindow(int npc) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.EVENT_WINDOW.getValue());
        mplew.writeInt(50);
        if (npc > 0) {
            mplew.writeInt(npc);
        }

        return mplew.getPacket();
    }

    public static byte[] sendEventWindow(int npc) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.EVENT_WINDOW.getValue());
        mplew.writeInt(55);
        if (npc > 0) {
            mplew.writeInt(npc);
        }
        return mplew.getPacket();
    }

    public static byte[] sendPVPMaps() {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PVP_INFO.getValue());
        mplew.write(1);
        mplew.writeInt(0);
        for (int i = 0; i < 3; i++) {
            mplew.writeInt(1);
        }
        mplew.writeLong(0L);
        for (int i = 0; i < 3; i++) {
            mplew.writeInt(1);
        }
        mplew.writeLong(0L);
        for (int i = 0; i < 4; i++) {
            mplew.writeInt(1);
        }
        for (int i = 0; i < 10; i++) {
            mplew.writeInt(1);
        }
        mplew.writeInt(14);
        mplew.writeShort(100);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] sendPyramidUpdate(int amount) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PYRAMID_UPDATE.getValue());
        mplew.writeInt(amount);

        return mplew.getPacket();
    }

    public static byte[] sendPyramidResult(byte rank, int amount) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PYRAMID_RESULT.getValue());
        mplew.write(rank);
        mplew.writeInt(amount);

        return mplew.getPacket();
    }

    public static byte[] sendPyramidEnergy(String type, String amount) {
        return sendString(1, type, amount);
    }

    public static byte[] sendString(int type, String object, String amount) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        switch (type) {
            case 1:
                mplew.write(SendPacketOpcode.ENERGY.getValue());
                break;
            case 2:
                mplew.write(SendPacketOpcode.GHOST_POINT.getValue());
                break;
            case 3:
                mplew.write(SendPacketOpcode.GHOST_STATUS.getValue());
        }

        mplew.writeMapleAsciiString(object);
        mplew.writeMapleAsciiString(amount);

        return mplew.getPacket();
    }

    public static byte[] sendGhostPoint(String type, String amount) {
        return sendString(2, type, amount);
    }

    public static byte[] sendGhostStatus(String type, String amount) {
        return sendString(3, type, amount);
    }

    public static byte[] MulungEnergy(int energy) {
        return sendPyramidEnergy("energy", String.valueOf(energy));
    }

    public static byte[] getPollQuestion() {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GAME_POLL_QUESTION.getValue());
        mplew.writeInt(1);
        mplew.writeInt(14);
        mplew.writeMapleAsciiString(ServerConstants.Poll_Question);
        mplew.writeInt(ServerConstants.Poll_Answers.length);
        for (byte i = 0; i < ServerConstants.Poll_Answers.length; i = (byte) (i + 1)) {
            mplew.writeMapleAsciiString(ServerConstants.Poll_Answers[i]);
        }

        return mplew.getPacket();
    }

    public static byte[] getPollReply(String message) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GAME_POLL_REPLY.getValue());
        mplew.writeMapleAsciiString(message);

        return mplew.getPacket();
    }

    public static byte[] showEventInstructions() {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GMEVENT_INSTRUCTIONS.getValue());
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] getOwlOpen() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.OWL_OF_MINERVA.getValue());
        mplew.write(0xA);
        List<Integer> owlItems = RankingWorker.getItemSearch();
        mplew.write(owlItems.size());
        for (Integer i : owlItems) {
            mplew.writeInt(i);
        }

        return mplew.getPacket();
    }

    public static byte[] getOwlSearched(int itemSearch, List<HiredMerchant> hms) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.OWL_OF_MINERVA.getValue());
        mplew.write(0x9);
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.writeInt(itemSearch);
        int size = 0;
        for (HiredMerchant hm : hms) {
            size += hm.searchItem(itemSearch).size();
        }
        mplew.writeInt(size);
        for (HiredMerchant hm : hms) {
            List<MaplePlayerShopItem> items = hm.searchItem(itemSearch);
            for (MaplePlayerShopItem item : items) {
                mplew.writeMapleAsciiString(hm.getOwnerName());
                mplew.writeInt(hm.getMap().getId());
                mplew.writeMapleAsciiString(hm.getDescription());
                mplew.writeInt(item.item.getQuantity());
                mplew.writeInt(item.bundles);
                mplew.writeLong(item.price);
                switch (1) {
                    case 0:
                        mplew.writeInt(hm.getOwnerId());
                        break;
                    case 1:
                        mplew.writeInt(hm.getStoreId());
                        break;
                    default:
                        mplew.writeInt(hm.getObjectId());
                }

                mplew.write(hm.getChannel() - 1);
                mplew.write(ItemConstants.getInventoryType(itemSearch).getType());
                if (ItemConstants.getInventoryType(itemSearch) == MapleInventoryType.EQUIP) {
                    PacketHelper.addItemInfo(mplew, item.item);
                }
            }
        }
        HiredMerchant hm;
        return mplew.getPacket();
    }

    public static byte[] getRPSMode(byte mode, int mesos, int selection, int answer) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.RPS_GAME.getValue());
        mplew.write(mode);
        switch (mode) {
            case 6:
                if (mesos == -1) {
                    break;
                }
                mplew.writeInt(mesos);
                break;
            case 8:
                mplew.writeInt(9000019);
                break;
            case 11:
                mplew.write(selection);
                mplew.write(answer);
        }

        return mplew.getPacket();
    }

    public static byte[] followRequest(int chrid) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.FOLLOW_REQUEST.getValue());
        mplew.writeInt(chrid);

        return mplew.getPacket();
    }

    public static byte[] followEffect(int initiator, int replier, Point toMap) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.FOLLOW_EFFECT.getValue());
        mplew.writeInt(initiator);
        mplew.writeInt(replier);
        if (replier == 0) {
            mplew.write(toMap == null ? 0 : 1);
            if (toMap != null) {
                mplew.writeInt(toMap.x);
                mplew.writeInt(toMap.y);
            }
        }
        return mplew.getPacket();
    }

    public static byte[] getFollowMsg(int opcode) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.FOLLOW_MSG.getValue());

        mplew.writeLong(opcode);

        return mplew.getPacket();
    }

    public static byte[] moveFollow(Point otherStart, Point myStart, Point otherEnd, List<LifeMovementFragment> moves) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.FOLLOW_MOVE.getValue());
        mplew.writeInt(0);
        mplew.writePos(otherStart);
        mplew.writePos(myStart);
        PacketHelper.serializeMovementList(mplew, moves);
        mplew.write(17);
        for (int i = 0; i < 8; i++) {
            mplew.write(0);
        }
        mplew.write(0);
        mplew.writePos(otherEnd);
        mplew.writePos(otherStart);

        return mplew.getPacket();
    }

    public static byte[] getFollowMessage(String msg) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SPOUSE_MESSAGE.getValue());
        mplew.writeShort(11);
        mplew.writeMapleAsciiString(msg);

        return mplew.getPacket();
    }

    public static byte[] getMovingPlatforms(MapleMap map) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MOVE_PLATFORM.getValue());
        mplew.writeInt(map.getPlatforms().size());
        for (MaplePlatform mp : map.getPlatforms()) {
            mplew.writeMapleAsciiString(mp.name);
            mplew.writeInt(mp.start);
            mplew.writeInt(mp.SN.size());
            for (Integer SN : mp.SN) {
                mplew.writeInt(SN);
            }
            mplew.writeInt(mp.speed);
            mplew.writeInt(mp.x1);
            mplew.writeInt(mp.x2);
            mplew.writeInt(mp.y1);
            mplew.writeInt(mp.y2);
            mplew.writeInt(mp.x1);
            mplew.writeInt(mp.y1);
            mplew.writeShort(mp.r);
        }
        return mplew.getPacket();
    }

    public static byte[] getUpdateEnvironment(MapleMap map) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.UPDATE_ENV.getValue());
        mplew.writeInt(map.getEnvironment().size());
        for (Map.Entry mp : map.getEnvironment().entrySet()) {
            mplew.writeMapleAsciiString((String) mp.getKey());
            mplew.writeInt(((Integer) mp.getValue()));
        }
        return mplew.getPacket();
    }

    public static byte[] trembleEffect(int type, int delay) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.BOSS_ENV.getValue());
        mplew.write(1);
        mplew.write(type);
        mplew.writeInt(delay);
        mplew.writeShort(30);
        return mplew.getPacket();
    }

    public static byte[] DublStart(boolean dark) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
        mplew.write(0x2F);
        mplew.write(dark ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] useSPReset(int chrId) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SP_RESET.getValue());
        mplew.write(1);
        mplew.writeInt(chrId);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] useAPReset(int chrId) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.AP_RESET.getValue());
        mplew.write(1);
        mplew.writeInt(chrId);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] playerDamaged(int chrId, int dmg) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PLAYER_DAMAGED.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(dmg);

        return mplew.getPacket();
    }

    public static byte[] pamsSongEffect(int chrId) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PAMS_SONG.getValue());
        mplew.writeInt(chrId);

        return mplew.getPacket();
    }

    public static byte[] pamsSongUI() {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PAMS_SONG.getValue());
        mplew.writeShort(0);

        return mplew.getPacket();
    }

    public static byte[] englishQuizMsg(String msg) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.ENGLISH_QUIZ.getValue());
        mplew.writeInt(20);
        mplew.writeMapleAsciiString(msg);

        return mplew.getPacket();
    }

    public static byte[] report(int err) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.REPORT_RESULT.getValue());
        mplew.write(err);
        if (err == 2) {
            mplew.write(0);
            mplew.writeInt(1);
        }
        return mplew.getPacket();
    }

    public static byte[] sendLieDetector(byte[] image, int attempt, int refresh) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).append(" 测谎仪图片大小: ").append(image.length).append(" 换图次数: ").append(attempt - 1).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.LIE_DETECTOR.getValue());
        mplew.write(9);
        mplew.write(1);
        mplew.write(attempt);//错误次数 递减
        mplew.write(refresh);//刷新次数 递增
        if (image == null) {
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        mplew.writeInt(image.length);
        mplew.write(image);

        return mplew.getPacket();
    }

    public static byte[] sendLieDetector(String target) {

        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.LIE_DETECTOR.getValue());
        mplew.write(0x8);
        mplew.write(0x1);
        mplew.writeMapleAsciiString(target);
        return mplew.getPacket();
    }

    public static byte[] LieDetectorResponse(byte msg) {
        return LieDetectorResponse(msg, (byte) 0);
    }

    public static byte[] LieDetectorResponse(byte msg, byte msg2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.LIE_DETECTOR.getValue());

        mplew.write(msg);

        mplew.write(msg2);

        return mplew.getPacket();
    }

    public static byte[] enableReport() {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);

        mplew.write(SendPacketOpcode.ENABLE_REPORT.getValue());
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] reportResponse(int mode, int remainingReports) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.REPORT_RESPONSE.getValue());
        mplew.writeShort((short) mode);
        if (mode == 2) {
            mplew.write(1);
            mplew.writeInt(remainingReports);
        }

        return mplew.getPacket();
    }

    public static byte[] ultimateExplorer() {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.ULTIMATE_EXPLORER.getValue());

        return mplew.getPacket();
    }

    public static byte[] GMPoliceMessage() {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GM_POLICE.getValue());
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] pamSongUI() {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PAM_SONG.getValue());

        return mplew.getPacket();
    }

    public static byte[] dragonBlink(int portalId) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.DRAGON_BLINK.getValue());
        mplew.write(portalId);

        return mplew.getPacket();
    }

    public static byte[] harvestMessage(int oid, int msg) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.HARVEST_MESSAGE.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(msg);

        return mplew.getPacket();
    }

    public static byte[] harvestResult(int chrId, boolean success) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.HARVESTED.getValue());
        mplew.writeInt(chrId);
        mplew.write(success ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] showHarvesting(int chrId, int tool) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_HARVEST.getValue());
        mplew.writeInt(chrId);
        mplew.write(tool > 0 ? 1 : 0);
        if (tool > 0) {
            mplew.writeInt(tool);
            mplew.writeInt(0);
        }

        return mplew.getPacket();
    }

    public static byte[] makeExtractor(int chrId, String cname, Point pos, int timeLeft, int itemId, int fee) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SPAWN_EXTRACTOR.getValue());
        mplew.writeInt(chrId);
        mplew.writeMapleAsciiString(cname);
        mplew.writeInt(pos.x);
        mplew.writeInt(pos.y);
        mplew.writeShort(timeLeft);
        mplew.writeInt(itemId);
        mplew.writeInt(fee);

        return mplew.getPacket();
    }

    public static byte[] removeExtractor(int chrId) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.REMOVE_EXTRACTOR.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(1);

        return mplew.getPacket();
    }

    public static byte[] spouseMessage(String msg, boolean white) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SPOUSE_MESSAGE.getValue());
        mplew.writeShort(white ? 10 : 6);
        mplew.writeMapleAsciiString(msg);

        return mplew.getPacket();
    }

    public static byte[] spouseMessage(int op, String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SPOUSE_MESSAGE.getValue());

        mplew.writeShort(op);
        mplew.writeMapleAsciiString(msg);

        return mplew.getPacket();
    }

    public static byte[] openBag(int index, int itemId, boolean firstTime) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.OPEN_BAG.getValue());
        mplew.writeInt(index);
        mplew.writeInt(itemId);
        mplew.writeShort(firstTime ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] showOwnCraftingEffect(String effect, int time, int mode) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
        mplew.write(0x26);
        mplew.writeMapleAsciiString(effect);
        mplew.write(1);
        mplew.writeInt(time);
        mplew.writeInt(mode);

        return mplew.getPacket();
    }

    public static byte[] showCraftingEffect(int chrId, String effect, int time, int mode) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(chrId);
        mplew.write(37);
        mplew.writeMapleAsciiString(effect);
        mplew.write(1);
        mplew.writeInt(time);
        mplew.writeInt(mode);

        return mplew.getPacket();
    }

    public static byte[] craftMake(int chrId, int something, int time) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CRAFT_EFFECT.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(something);
        mplew.writeInt(time);

        return mplew.getPacket();
    }

    public static byte[] craftFinished(int chrId, int craftID, int ranking, int itemId, int quantity, int exp) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CRAFT_COMPLETE.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(craftID);
        mplew.writeInt(ranking);

        if ((ranking == 21) || (ranking == 22) || (ranking == 23)) {
            mplew.writeInt(itemId);
            mplew.writeInt(quantity);
        }
        mplew.writeInt(exp);

        return mplew.getPacket();
    }

    public static byte[] craftMessage(String msg) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CRAFT_MESSAGE.getValue());
        mplew.writeMapleAsciiString(msg);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] shopDiscount(int percent) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOP_DISCOUNT.getValue());
        mplew.write(percent);

        return mplew.getPacket();
    }

    public static byte[] changeCardSet(int set) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CARD_SET.getValue());
        mplew.writeInt(set);

        return mplew.getPacket();
    }

    public static byte[] getCard(int itemid, int level) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GET_CARD.getValue());
        mplew.write(itemid > 0 ? 1 : 0);
        if (itemid > 0) {
            mplew.writeInt(itemid);
            mplew.writeInt(level);
        }

        return mplew.getPacket();
    }

    public static byte[] upgradeBook(Item book, MapleCharacter chr) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.BOOK_STATS.getValue());
        mplew.writeInt(book.getPosition());
        PacketHelper.addItemInfo(mplew, book);

        return mplew.getPacket();
    }

    public static byte[] pendantSlot(boolean p) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PENDANT_SLOT.getValue());
        mplew.write(p ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] getBuffBar(long millis) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.BUFF_BAR.getValue());
        mplew.writeLong(millis);

        return mplew.getPacket();
    }

    public static byte[] showMidMsg(String s, int l) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MID_MSG.getValue());
        mplew.write(l);
        mplew.writeMapleAsciiString(s);
        mplew.write(s.length() > 0 ? 0 : 1);

        return mplew.getPacket();
    }

    public static byte[] showBackgroundEffect(String eff, int value) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.VISITOR.getValue());
        mplew.writeMapleAsciiString(eff);
        mplew.write(value);

        return mplew.getPacket();
    }

    public static byte[] updateGender(MapleCharacter chr) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.UPDATE_GENDER.getValue());
        mplew.write(chr.getGender());

        return mplew.getPacket();
    }

    public static byte[] registerFamiliar(MonsterFamiliar mf) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.REGISTER_FAMILIAR.getValue());
        mplew.writeLong(mf.getId());
        mf.writeRegisterPacket(mplew, false);
        mplew.writeShort(mf.getVitality() >= 3 ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] touchFamiliar(int chrId, byte unk, int objectid, int type, int delay, int damage) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.TOUCH_FAMILIAR.getValue());
        mplew.writeInt(chrId);
        mplew.write(0);
        mplew.write(unk);
        mplew.writeInt(objectid);
        mplew.writeInt(type);
        mplew.writeInt(delay);
        mplew.writeInt(damage);

        return mplew.getPacket();
    }

    public static byte[] familiarAttack(int chrId, byte unk, List<Triple<Integer, Integer, List<Integer>>> attackPair) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.ATTACK_FAMILIAR.getValue());
        mplew.writeInt(chrId);
        mplew.write(0);
        mplew.write(unk);
        mplew.write(attackPair.size());
        for (Triple s : attackPair) {
            mplew.writeInt(((Integer) s.left));
            mplew.write(((Integer) s.mid));
            mplew.write(((List) s.right).size());
            for (Iterator i = ((List) s.right).iterator(); i.hasNext();) {
                int damage = ((Integer) i.next());
                mplew.writeInt(damage);
            }
        }
        Iterator i;
        return mplew.getPacket();
    }

    public static byte[] updateFamiliar(MonsterFamiliar mf) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.UPDATE_FAMILIAR.getValue());
        mplew.writeInt(mf.getCharacterId());
        mplew.writeInt(mf.getFamiliar());
        mplew.writeInt(mf.getFatigue());
        mplew.writeLong(PacketHelper.getTime(mf.getVitality() >= 3 ? System.currentTimeMillis() : -2L));

        return mplew.getPacket();
    }

    public static byte[] removeFamiliar(int chrId) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SPAWN_FAMILIAR.getValue());
        mplew.writeInt(chrId);
        mplew.writeShort(0);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] spawnFamiliar(MonsterFamiliar mf, boolean spawn) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SPAWN_FAMILIAR.getValue());
        mplew.writeInt(mf.getCharacterId());
        mplew.writeShort(spawn ? 1 : 0);
        mplew.write(0);
        if (spawn) {
            mplew.writeInt(mf.getFamiliar());
            mplew.writeInt(mf.getFatigue());
            mplew.writeInt(mf.getVitality() * 300);
            mplew.writeMapleAsciiString(mf.getName());
            mplew.writePos(mf.getTruePosition());
            mplew.write(mf.getStance());
            mplew.writeShort(mf.getFh());
        }
        return mplew.getPacket();
    }

    public static byte[] moveFamiliar(int chrId, Point startPos, List<LifeMovementFragment> moves) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MOVE_FAMILIAR.getValue());
        mplew.writeInt(chrId);
        mplew.write(0);
        mplew.writePos(startPos);
        mplew.writeInt(0);
        PacketHelper.serializeMovementList(mplew, moves);

        return mplew.getPacket();
    }

    public static byte[] achievementRatio(int amount) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.ACHIEVEMENT_RATIO.getValue());
        mplew.writeInt(amount);

        return mplew.getPacket();
    }

    public static byte[] createUltimate(int amount) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CREATE_ULTIMATE.getValue());
        mplew.writeInt(amount);

        return mplew.getPacket();
    }

    public static byte[] updateSpecialStat(String stat, int array, int mode, boolean unk, int chance) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PROFESSION_INFO.getValue());
        mplew.writeMapleAsciiString(stat);
        mplew.writeInt(array);
        mplew.writeInt(mode);
        mplew.write(unk ? 1 : 0);
        mplew.writeInt(chance);

        return mplew.getPacket();
    }

    public static byte[] getQuickSlot(MapleQuickSlot quickslot) {

        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.QUICK_SLOT.getValue());
        quickslot.writeData(mplew);

        return mplew.getPacket();
    }

    public static byte[] getFamiliarInfo(MapleCharacter chr) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.FAMILIAR_INFO.getValue());
        mplew.writeInt(chr.getFamiliars().size());
        for (MonsterFamiliar mf : chr.getFamiliars().values()) {
            mf.writeRegisterPacket(mplew, true);
        }
        List<Pair> size = new ArrayList();
        for (Item i : chr.getInventory(MapleInventoryType.USE).list()) {
            if (i.getItemId() / 10000 == 287) {
                StructFamiliar f = MapleItemInformationProvider.getInstance().getFamiliarByItem(i.getItemId());
                if (f != null) {
                    size.add(new Pair(f.familiar, i.getInventoryId()));
                }
            }
        }
        mplew.writeInt(size.size());
        for (Pair s : size) {
            mplew.writeInt(chr.getId());
            mplew.writeInt(((Integer) s.left));
            mplew.writeLong(((Long) s.right));
            mplew.write(0);
        }
        size.clear();
        return mplew.getPacket();
    }

    public static byte[] updateImp(MapleImp imp, int mask, int index, boolean login) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.ITEM_POT.getValue());
        mplew.write(login ? 0 : 1);
        mplew.writeInt(index + 1);
        mplew.writeInt(mask);
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0) {
            Pair i = MapleItemInformationProvider.getInstance().getPot(imp.getItemId());
            if (i == null) {
                return enableActions();
            }
            mplew.writeInt(((Integer) i.left));
            mplew.write(imp.getLevel());
        }
        if (((mask & ImpFlag.SUMMONED.getValue()) != 0) || ((mask & ImpFlag.STATE.getValue()) != 0)) {
            mplew.write(imp.getState());
        }
        if (((mask & ImpFlag.SUMMONED.getValue()) != 0) || ((mask & ImpFlag.FULLNESS.getValue()) != 0)) {
            mplew.writeInt(imp.getFullness());
        }
        if (((mask & ImpFlag.SUMMONED.getValue()) != 0) || ((mask & ImpFlag.CLOSENESS.getValue()) != 0)) {
            mplew.writeInt(imp.getCloseness());
        }
        if (((mask & ImpFlag.SUMMONED.getValue()) != 0) || ((mask & ImpFlag.CLOSENESS_LEFT.getValue()) != 0)) {
            mplew.writeInt(1);
        }
        if (((mask & ImpFlag.SUMMONED.getValue()) != 0) || ((mask & ImpFlag.MINUTES_LEFT.getValue()) != 0)) {
            mplew.writeInt(0);
        }
        if (((mask & ImpFlag.SUMMONED.getValue()) != 0) || ((mask & ImpFlag.LEVEL.getValue()) != 0)) {
            mplew.write(1);
        }
        if (((mask & ImpFlag.SUMMONED.getValue()) != 0) || ((mask & ImpFlag.FULLNESS_2.getValue()) != 0)) {
            mplew.writeInt(imp.getFullness());
        }
        if (((mask & ImpFlag.SUMMONED.getValue()) != 0) || ((mask & ImpFlag.UPDATE_TIME.getValue()) != 0)) {
            mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        }
        if (((mask & ImpFlag.SUMMONED.getValue()) != 0) || ((mask & ImpFlag.CREATE_TIME.getValue()) != 0)) {
            mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        }
        if (((mask & ImpFlag.SUMMONED.getValue()) != 0) || ((mask & ImpFlag.AWAKE_TIME.getValue()) != 0)) {
            mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        }
        if (((mask & ImpFlag.SUMMONED.getValue()) != 0) || ((mask & ImpFlag.SLEEP_TIME.getValue()) != 0)) {
            mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        }
        if (((mask & ImpFlag.SUMMONED.getValue()) != 0) || ((mask & ImpFlag.MAX_CLOSENESS.getValue()) != 0)) {
            mplew.writeInt(100);
        }
        if (((mask & ImpFlag.SUMMONED.getValue()) != 0) || ((mask & ImpFlag.MAX_DELAY.getValue()) != 0)) {
            mplew.writeInt(1000);
        }
        if (((mask & ImpFlag.SUMMONED.getValue()) != 0) || ((mask & ImpFlag.MAX_FULLNESS.getValue()) != 0)) {
            mplew.writeInt(1000);
        }
        if (((mask & ImpFlag.SUMMONED.getValue()) != 0) || ((mask & ImpFlag.MAX_ALIVE.getValue()) != 0)) {
            mplew.writeInt(1);
        }
        if (((mask & ImpFlag.SUMMONED.getValue()) != 0) || ((mask & ImpFlag.MAX_MINUTES.getValue()) != 0)) {
            mplew.writeInt(10);
        }
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] spawnFlags(List<Pair<String, Integer>> flags) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.LOGIN_WELCOME.getValue());
        mplew.write(flags == null ? 0 : flags.size());
        if (flags != null) {
            for (Pair f : flags) {
                mplew.writeMapleAsciiString((String) f.left);
                mplew.write(((Integer) f.right));
            }
        }

        return mplew.getPacket();
    }

    public static byte[] getPVPScoreboard(List<Pair<Integer, MapleCharacter>> flags, int type) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PVP_SCOREBOARD.getValue());
        mplew.writeShort(flags.size());
        for (Pair f : flags) {
            mplew.writeInt(((MapleCharacter) f.right).getId());
            mplew.writeMapleAsciiString(((MapleCharacter) f.right).getName());
            mplew.writeInt(((Integer) f.left));
            mplew.write(type == 0 ? 0 : ((MapleCharacter) f.right).getTeam() + 1);
        }

        return mplew.getPacket();
    }

    public static byte[] getPVPResult(List<Pair<Integer, MapleCharacter>> flags, int exp, int winningTeam, int playerTeam) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PVP_RESULT.getValue());
        mplew.writeInt(flags.size());
        for (Pair f : flags) {
            mplew.writeInt(((MapleCharacter) f.right).getId());
            mplew.writeMapleAsciiString(((MapleCharacter) f.right).getName());
            mplew.writeInt(((Integer) f.left));
            mplew.writeShort(((MapleCharacter) f.right).getTeam() + 1);
            if (GameConstants.GMS) {
                mplew.writeInt(0);
            }
        }
        mplew.writeZero(24);
        mplew.writeInt(exp);
        mplew.write(0);
        if (GameConstants.GMS) {
            mplew.writeShort(100);
            mplew.writeInt(0);
        }
        mplew.write(winningTeam);
        mplew.write(playerTeam);

        return mplew.getPacket();
    }

    public static byte[] showOwnChampionEffect() {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
        mplew.write(0x21);
        mplew.writeInt(30000);

        return mplew.getPacket();
    }

    public static byte[] showChampionEffect(int from_playerid) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(from_playerid);
        mplew.write(32);
        mplew.writeInt(30000);

        return mplew.getPacket();
    }

    public static byte[] enablePVP(boolean enabled) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PVP_ENABLED.getValue());
        mplew.write(enabled ? 1 : 2);

        return mplew.getPacket();
    }

    public static byte[] getPVPMode(int mode) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PVP_MODE.getValue());
        mplew.write(mode);

        return mplew.getPacket();
    }

    public static byte[] getPVPType(int type, List<Pair<Integer, String>> players1, int team, boolean enabled, int lvl) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PVP_TYPE.getValue());
        mplew.write(type);
        mplew.write(lvl);
        mplew.write(enabled ? 1 : 0);
        if (type > 0) {
            mplew.write(team);
            mplew.writeInt(players1.size());
            for (Pair pl : players1) {
                mplew.writeInt(((Integer) pl.left));
                mplew.writeMapleAsciiString((String) pl.right);
                mplew.writeShort(2660);
            }
        }

        return mplew.getPacket();
    }

    public static byte[] getPVPTeam(List<Pair<Integer, String>> players) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PVP_TEAM.getValue());
        mplew.writeInt(players.size());
        for (Pair pl : players) {
            mplew.writeInt(((Integer) pl.left));
            mplew.writeMapleAsciiString((String) pl.right);
            mplew.writeShort(2660);
        }

        return mplew.getPacket();
    }

    public static byte[] getPVPScore(int score, boolean kill) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PVP_SCORE.getValue());
        mplew.writeInt(score);
        mplew.write(kill ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] getPVPIceGage(int score) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PVP_ICEGAGE.getValue());
        mplew.writeInt(score);

        return mplew.getPacket();
    }

    public static byte[] getPVPKilled(String lastWords) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PVP_KILLED.getValue());
        mplew.writeMapleAsciiString(lastWords);

        return mplew.getPacket();
    }

    public static byte[] getPVPPoints(int p1, int p2) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PVP_POINTS.getValue());

        mplew.writeInt(p1);
        mplew.writeInt(p2);

        return mplew.getPacket();
    }

    public static byte[] getPVPHPBar(int cid, int hp, int maxHp) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PVP_HP.getValue());

        mplew.writeInt(cid);
        mplew.writeInt(hp);
        mplew.writeInt(maxHp);

        return mplew.getPacket();
    }

    public static byte[] getPVPIceHPBar(int hp, int maxHp) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PVP_ICEKNIGHT.getValue());

        mplew.writeInt(hp);
        mplew.writeInt(maxHp);

        return mplew.getPacket();
    }

    public static byte[] getPVPMist(int cid, int mistSkill, int mistLevel, int damage) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PVP_MIST.getValue());

        mplew.writeInt(cid);
        mplew.writeInt(mistSkill);
        mplew.write(mistLevel);
        mplew.writeInt(damage);
        mplew.write(8);
        mplew.writeInt(1000);

        return mplew.getPacket();
    }

    public static byte[] getCaptureFlags(MapleMap map) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CAPTURE_FLAGS.getValue());
        mplew.writeRect(map.getArea(0));
        mplew.writeInt(((Point) ((Pair) map.getGuardians().get(0)).left).x);
        mplew.writeInt(((Point) ((Pair) map.getGuardians().get(0)).left).y);
        mplew.writeRect(map.getArea(1));
        mplew.writeInt(((Point) ((Pair) map.getGuardians().get(1)).left).x);
        mplew.writeInt(((Point) ((Pair) map.getGuardians().get(1)).left).y);
        return mplew.getPacket();
    }

    public static byte[] getCapturePosition(MapleMap map) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        Point p1 = map.getPointOfItem(2910000);
        Point p2 = map.getPointOfItem(2910001);
        mplew.write(SendPacketOpcode.CAPTURE_POSITION.getValue());
        mplew.write(p1 == null ? 0 : 1);
        if (p1 != null) {
            mplew.writeInt(p1.x);
            mplew.writeInt(p1.y);
        }
        mplew.write(p2 == null ? 0 : 1);
        if (p2 != null) {
            mplew.writeInt(p2.x);
            mplew.writeInt(p2.y);
        }

        return mplew.getPacket();
    }

    public static byte[] resetCapture() {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CAPTURE_RESET.getValue());

        return mplew.getPacket();
    }

    public static byte[] pvpAttack(int cid, int playerLevel, int skill, int skillLevel, int speed, int mastery, int projectile, int attackCount, int chargeTime, int stance, int direction, int range, int linkSkill, int linkSkillLevel, boolean movementSkill, boolean pushTarget, boolean pullTarget, List<AttackPair> attack) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PVP_ATTACK.getValue());
        mplew.writeInt(cid);
        mplew.write(playerLevel);
        mplew.writeInt(skill);
        mplew.write(skillLevel);
        mplew.writeInt(linkSkill != skill ? linkSkill : 0);
        mplew.write(linkSkillLevel != skillLevel ? linkSkillLevel : 0);
        mplew.write(direction);
        mplew.write(movementSkill ? 1 : 0);
        mplew.write(pushTarget ? 1 : 0);
        mplew.write(pullTarget ? 1 : 0);
        mplew.write(0);
        mplew.writeShort(stance);
        mplew.write(speed);
        mplew.write(mastery);
        mplew.writeInt(projectile);
        mplew.writeInt(chargeTime);
        mplew.writeInt(range);
        mplew.writeShort(attack.size());
        if (GameConstants.GMS) {
            mplew.writeInt(0);
        }
        mplew.write(attackCount);
        mplew.write(0);
        for (AttackPair p : attack) {
            mplew.writeInt(p.objectid);
            if (GameConstants.GMS) {
                mplew.writeInt(0);
            }
            mplew.writePos(p.point);
            mplew.writeZero(5);
            for (Pair atk : p.attack) {
                mplew.writeInt(((Integer) atk.left));
                if (GameConstants.GMS) {
                    mplew.writeInt(0);
                }
                mplew.write(((Boolean) atk.right) ? 1 : 0);
                mplew.writeShort(0);
            }
        }

        return mplew.getPacket();
    }

    public static byte[] pvpCool(int cid, List<Integer> attack) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PVP_COOL.getValue());
        mplew.writeInt(cid);
        mplew.write(attack.size());
        for (Iterator i$ = attack.iterator(); i$.hasNext();) {
            int b = ((Integer) i$.next());
            mplew.writeInt(b);
        }
        return mplew.getPacket();
    }

    public static byte[] getPVPClock(int type, int time) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CLOCK.getValue());
        mplew.write(3);
        mplew.write(type);
        mplew.writeInt(time);

        return mplew.getPacket();
    }

    public static byte[] getPVPTransform(int type) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PVP_TRANSFORM.getValue());
        mplew.write(type);

        return mplew.getPacket();
    }

    public static byte[] changeTeam(int cid, int type) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.LOAD_TEAM.getValue());
        mplew.writeInt(cid);
        mplew.write(type);

        return mplew.getPacket();
    }

    public static byte[] getQuickMoveInfo(boolean show, List<MapleQuickMove.QuickMoveNPC> qm) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.QUICK_MOVE.getValue());
        mplew.write(qm.size() <= 0 ? 0 : show ? qm.size() : 0);
        if (show && qm.size() > 0) {
            int i = 0;
            for (MapleQuickMove.QuickMoveNPC qmn : qm) {
                mplew.writeInt(i++);
                mplew.writeInt(qmn.getId());
                mplew.writeInt(qmn.getType());
                mplew.writeInt(qmn.getLevel());
                mplew.writeMapleAsciiString(qmn.getDescription());
                mplew.writeLong(PacketHelper.getTime(-2));
                mplew.writeLong(PacketHelper.getTime(-1));
            }
        }

        return mplew.getPacket();
    }

    public static byte[] sendloginSuccess() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.LOGIN_SUCC.getValue());

        return mplew.getPacket();
    }

    public static byte[] showCharCash(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CHAR_CASH.getValue());
        mplew.writeInt(chr.getId());
        mplew.writeInt(chr.getCSPoints(2));

        return mplew.getPacket();
    }

    public static byte[] showPlayerCash(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_PLAYER_CASH.getValue());
        mplew.writeInt(chr.getCSPoints(1));
        mplew.writeInt(chr.getCSPoints(2));

        return mplew.getPacket();
    }

    public static byte[] playerCashUpdate(int mode, int toCharge, MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PLAYER_CASH_UPDATE.getValue());
        mplew.writeInt(mode);
        mplew.writeInt(toCharge == 1 ? chr.getCSPoints(1) : 0);
        mplew.writeInt(chr.getCSPoints(2));
        mplew.write(toCharge);
        mplew.writeShort(0);

        return mplew.getPacket();
    }

    public static byte[] sendTestPacket(String test) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(HexTool.getByteArrayFromHexString(test));
        return mplew.getPacket();
    }

    public static byte[] giveCharacterSkill(int skillId, int toChrId, String toChrName) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GIVE_CHARACTER_SKILL.getValue());
        mplew.writeInt(0);
        mplew.writeInt(skillId);
        mplew.writeInt(toChrId);
        mplew.writeMapleAsciiString(toChrName);

        return mplew.getPacket();
    }

    public static byte[] showDoJangRank() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MULUNG_DOJO_RANKING.getValue());
        mplew.writeInt(1);
        mplew.writeShort(1);
        mplew.writeMapleAsciiString("落叶无痕");
        mplew.writeLong(60L);

        return mplew.getPacket();
    }

    public static byte[] confirmCrossHunter(byte code) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CONFIRM_CROSS_HUNTER.getValue());

        mplew.write(code);

        return mplew.getPacket();
    }

    public static byte[] openWeb(String web) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.OPEN_WEB.getValue());
        mplew.writeMapleAsciiString(web);

        return mplew.getPacket();
    }

    public static byte[] updateInnerSkill(int skillId, int skillevel, byte position, byte rank) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.UPDATE_INNER_SKILL.getValue());
        mplew.write(1);
        mplew.write(1);
        mplew.writeShort((short) position);
        mplew.writeInt(skillId);
        mplew.writeShort(skillevel);
        mplew.writeShort((short) rank);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] updateInnerStats(int honor) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.UPDATE_INNER_STATS.getValue());
        mplew.writeInt(honor);

        return mplew.getPacket();
    }

    public static byte[] sendPolice(String text) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MAPLE_ADMIN.getValue());
        mplew.writeMapleAsciiString(text);

        return mplew.getPacket();
    }


    public static byte[] showOwnJobChangedElf(String effect, int time, int itemId) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
        mplew.write(0x26);
        mplew.writeMapleAsciiString(effect);
        mplew.write(1);
        mplew.writeInt(0);
        mplew.writeInt(time);
        mplew.writeInt(itemId);

        return mplew.getPacket();
    }

    public static byte[] showJobChangedElf(int chrId, String effect, int time, int itemId) {
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(chrId);
        mplew.write(37);
        mplew.writeMapleAsciiString(effect);
        mplew.write(1);
        mplew.writeInt(0);
        mplew.writeInt(time);
        mplew.writeInt(itemId);

        return mplew.getPacket();
    }

    public static byte[] testPacket(String testmsg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(HexTool.getByteArrayFromHexString(testmsg));

        return mplew.getPacket();
    }

    public static byte[] testPacket(byte[] testmsg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(testmsg);

        return mplew.getPacket();
    }

    public static byte[] testPacket(String op, String text) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(HexTool.getByteArrayFromHexString(op));
        mplew.writeMapleAsciiString(text);

        return mplew.getPacket();
    }

    public static byte[] sendUnkPacket1FC() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.UNKNOWN_1FC.getValue());
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] SystemProcess() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SYSTEM_PROCESS_LIST.getValue());
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] exitGame() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.EXIT_GAME.getValue());

        return mplew.getPacket();
    }

    public static byte[] HSCheck(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.HS_CHECK.getValue());
        mplew.write(type);
        return mplew.getPacket();
    }

    public static byte[] UpdateMacrSkill(int index, int skillid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.UPDATE_MAC_SKILL.getValue());
        mplew.write(1);
        mplew.write(index);
        mplew.writeInt(skillid);
        mplew.write(0);
        return mplew.getPacket();
    }


}
