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
                    MapleLogger.info("未知的类型："+statupdate.getKey().getWriteByte());
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
            MapleLogger.info(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
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
            MapleLogger.info(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.REMOVE_DOOR.getValue());
        mplew.write(animation ? 0 : 1);
        mplew.writeInt(ownerId);

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
            MapleLogger.info(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GM_EFFECT.getValue());
        mplew.write(value);
        mplew.writeZero(17);

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
            MapleLogger.info(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
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
            MapleLogger.info(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
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
            MapleLogger.info(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
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
            MapleLogger.info(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
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
            MapleLogger.info(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
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

    public static byte[] rangedAttack(MapleCharacter chr, int skilllevel, int itemId, AttackInfo attackInfo) {
        if (ServerProperties.ShowPacket()) {
            MapleLogger.info(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
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
            MapleLogger.info(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
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
            MapleLogger.info(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
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
            MapleLogger.info(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
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
            MapleLogger.info(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
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

    public static byte[] skillEffect(int fromId, int skillId, byte level, byte display, byte direction, byte speed, Point position) {
        if (ServerProperties.ShowPacket()) {
            MapleLogger.info(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
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
            MapleLogger.info(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CANCEL_SKILL_EFFECT.getValue());
        mplew.writeInt(from.getId());
        mplew.writeInt(skillId);

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

    public static byte[] Mulung_DojoUp2() {
        if (ServerProperties.ShowPacket()) {
            MapleLogger.info(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
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
            MapleLogger.info(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.OX_QUIZ.getValue());
        mplew.write(askQuestion ? 1 : 0);
        mplew.write(questionSet);
        mplew.writeShort(questionId);

        return mplew.getPacket();
    }

    public static byte[] showHpHealed(int chrId, int amount) {
        if (ServerProperties.ShowPacket()) {
            MapleLogger.info(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
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
            MapleLogger.info(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
        mplew.write(0x20);
        mplew.writeInt(amount);

        return mplew.getPacket();
    }

    public static byte[] showBlessOfDarkness(int skillId) {
        if (ServerProperties.ShowPacket()) {
            MapleLogger.info(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
        mplew.write(0x8);
        mplew.writeInt(skillId);

        return mplew.getPacket();
    }

    public static byte[] showHolyFountain(int skillId) {
        if (ServerProperties.ShowPacket()) {
            MapleLogger.info(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
        mplew.write(0x3);
        mplew.writeInt(skillId);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] DublStart(boolean dark) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
        mplew.write(0x2F);
        mplew.write(dark ? 1 : 0);

        return mplew.getPacket();
    }


    public static byte[] showOwnCraftingEffect(String effect, int time, int mode) {
        if (ServerProperties.ShowPacket()) {
            MapleLogger.info(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
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
            MapleLogger.info(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
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

    public static byte[] showOwnChampionEffect() {
        if (ServerProperties.ShowPacket()) {
            MapleLogger.info(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
        mplew.write(0x21);
        mplew.writeInt(30000);

        return mplew.getPacket();
    }

    public static byte[] showChampionEffect(int from_playerid) {
        if (ServerProperties.ShowPacket()) {
            MapleLogger.info(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(from_playerid);
        mplew.write(32);
        mplew.writeInt(30000);

        return mplew.getPacket();
    }

    public static byte[] sendTestPacket(String test) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(HexTool.getByteArrayFromHexString(test));
        return mplew.getPacket();
    }

    public static byte[] showOwnJobChangedElf(String effect, int time, int itemId) {
        if (ServerProperties.ShowPacket()) {
            MapleLogger.info(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
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
            MapleLogger.info(new StringBuilder().append("调用: ").append(new java.lang.Throwable().getStackTrace()[0]).toString());
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
}
