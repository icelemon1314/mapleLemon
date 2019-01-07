package handling.channel.handler;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleDisease;
import client.MapleQuestStatus;
import client.MapleStat;
import client.PlayerStats;
import client.Skill;
import client.SkillFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MapleRing;
import client.status.MonsterStatus;
import constants.GameConstants;
import constants.ItemConstants;
import handling.world.WorldBroadcastService;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import scripting.event.EventInstanceManager;
import scripting.event.EventManager;
import scripting.reactor.ReactorScriptManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import server.Randomizer;
import server.events.MapleCoconut;
import server.events.MapleEventType;
import server.maps.FieldLimitType;
import server.maps.MapleDoor;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleDefender;
import server.maps.MapleReactor;
import server.maps.MechDoor;
import server.quest.MapleQuest;
import tools.AttackPair;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Triple;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.SkillPacket;
import tools.packet.UIPacket;

public class PlayersHandler {

    public static void Note(SeekableLittleEndianAccessor slea, MapleCharacter chr) {
        byte type = slea.readByte();
        switch (type) {
            case 0:
                String name = slea.readMapleAsciiString();
                String msg = slea.readMapleAsciiString();
                boolean fame = slea.readByte() > 0;
                slea.readInt();
                Item itemz = chr.getCashInventory().findByCashId((int) slea.readLong());
                if ((itemz == null) || (!itemz.getGiftFrom().equalsIgnoreCase(name)) || (!chr.getCashInventory().canSendNote(itemz.getUniqueId()))) {
                    return;
                }
                try {
                    chr.sendNote(name, msg, fame ? 1 : 0);
                    chr.getCashInventory().sendedNote(itemz.getUniqueId());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            case 1:
                int num = slea.readShort();
                slea.readByte();
                for (int i = 0; i < num; i++) {
                    int id = slea.readInt();
                    int giveFame = slea.readByte();
                    chr.deleteNote(id, giveFame);
                }
                break;
            default:
                FileoutputUtil.log(new StringBuilder().append("Unhandled note action, ").append(type).append("").toString());
        }
    }

    public static void GiveFame(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        int who = slea.readInt();
        int mode = slea.readByte();
        int famechange = mode == 0 ? -1 : 1;
        MapleCharacter target = chr.getMap().getCharacterById(who);
        if ((target == null) || (target == chr)) {
            return;
        }
        if (chr.getLevel() < 15) {
            return;
        }
        switch (chr.canGiveFame(target)) {
            case OK:
                if (Math.abs(target.getFame() + famechange) <= 99999) {
                    target.addFame(famechange);
                    target.updateSingleStat(MapleStat.人气, target.getFame());
                }
                if (!chr.isGM()) {
                    chr.hasGivenFame(target);
                }
                c.getSession().write(MaplePacketCreator.giveFameResponse(mode, target.getName(), target.getFame()));
                target.getClient().getSession().write(MaplePacketCreator.receiveFame(mode, chr.getName()));
                break;
            case NOT_TODAY:
                c.getSession().write(MaplePacketCreator.giveFameErrorResponse(3));
                break;
            case NOT_THIS_MONTH:
                c.getSession().write(MaplePacketCreator.giveFameErrorResponse(4));
        }
    }

    /**
     * 通过传送门
     * @param slea
     * @param chr
     */
    public static void UseDoor(SeekableLittleEndianAccessor slea, MapleCharacter chr) {
        // 49 08 00 00 00 00
        int oid = slea.readInt();
        boolean mode = slea.readByte() == 0;
        for (MapleMapObject obj : chr.getMap().getAllDoorsThreadsafe()) {
            MapleDoor door = (MapleDoor) obj;
            if (door.getOwnerId() == oid) {
                door.warp(chr, mode);
                break;
            }
        }
        chr.getClient().getSession().write(MaplePacketCreator.enableActions());
    }

    public static void UseMechDoor(SeekableLittleEndianAccessor slea, MapleCharacter chr) {
        int oid = slea.readInt();
        Point pos = slea.readPos();
        int mode = slea.readByte();
        chr.getClient().getSession().write(MaplePacketCreator.enableActions());
        for (MapleMapObject obj : chr.getMap().getAllMechDoorsThreadsafe()) {
            MechDoor door = (MechDoor) obj;
            if ((door.getOwnerId() == oid) && (door.getId() == mode)) {
                chr.getMap().movePlayer(chr, pos);
                break;
            }
        }
    }

    public static void UseHolyFountain(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if ((chr == null) || (chr.getMap() == null)) {
            return;
        }
        int mode = slea.readByte();
        int oid = slea.readInt();
        int skillId = slea.readInt();
        Point position = slea.readPos();
        MapleDefender healDoor = c.getPlayer().getMap().getMistByOid(oid);
        if ((healDoor == null) || (!healDoor.isHolyFountain())) {
            return;
        }
        if ((healDoor.getHealCount() > 0) && (healDoor.getBox().contains(position))) {
            MapleCharacter owner = chr.getMap().getCharacterById(healDoor.getOwnerId());
            if ((healDoor.getOwnerId() == chr.getId()) || ((owner != null) && (owner.getParty() != null) && (chr.getParty() != null) && (owner.getParty().getId() == chr.getParty().getId()))) {
                int healHp = (int) (chr.getStat().getCurrentMaxHp() * (healDoor.getSource().getX() / 100.0D));
                chr.addHP(healHp);
                healDoor.setHealCount(healDoor.getHealCount() - 1);
                if (chr.isShowPacket()) {
                    chr.dropMessage(5, new StringBuilder().append("使用神圣源泉 - 恢复血量: ").append(healHp).append(" 百分比: ").append(healDoor.getSource().getX() / 100.0D).append(" 剩余次数: ").append(healDoor.getHealCount()).toString());
                }
                c.getSession().write(MaplePacketCreator.showHolyFountain(skillId));
            }
        } else if (chr.isShowPacket()) {
            chr.dropMessage(5, new StringBuilder().append("使用神圣源泉出现错误 - 源泉恢复的剩余次数: ").append(healDoor.getHealCount()).append(" 模式: ").append(mode).append(" 是否在范围内: ").append(healDoor.getBox().contains(position)).toString());
        }
        c.getSession().write(MaplePacketCreator.enableActions());
    }

    public static void TransformPlayer(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        slea.readInt();
        byte slot = (byte) slea.readShort();
        int itemId = slea.readInt();
        String target = slea.readMapleAsciiString();
        Item toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem((short) slot);
        if ((toUse == null) || (toUse.getQuantity() < 1) || (toUse.getItemId() != itemId)) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        switch (itemId) {
            case 2212000:
                MapleCharacter search_chr = chr.getMap().getCharacterByName(target);
                if (search_chr != null) {
                    MapleItemInformationProvider.getInstance().getItemEffect(2210023).applyTo(search_chr);
                    search_chr.dropMessage(6, new StringBuilder().append(chr.getName()).append(" has played a prank on you!").toString());
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
                } else {
                    chr.dropMessage(1, new StringBuilder().append("在当前地图中未找到 '").append(target).append("' 的玩家.").toString());
                }
        }

        c.getSession().write(MaplePacketCreator.enableActions());
    }

    public static void HitReactor(SeekableLittleEndianAccessor slea, MapleClient c) {
        int oid = slea.readInt();
        int charPos = slea.readInt();
        short stance = slea.readShort();
        MapleReactor reactor = c.getPlayer().getMap().getReactorByOid(oid);
        if ((reactor == null) || (!reactor.isAlive())) {
            return;
        }
        reactor.hitReactor(charPos, stance, c);
    }

    public static void TouchReactor(SeekableLittleEndianAccessor slea, MapleClient c) {
        int oid = slea.readInt();
        boolean touched = (slea.available() == 0L) || (slea.readByte() > 0);
        MapleReactor reactor = c.getPlayer().getMap().getReactorByOid(oid);
        if ((!touched) || (reactor == null) || (!reactor.isAlive()) || (reactor.getTouch() == 0)) {
            return;
        }
        if (c.getPlayer().isShowPacket()) {
            c.getPlayer().dropMessage(5, "反应堆信息 - 反应堆ID：" + reactor.getReactorId() + " oid: " + oid + " 碰触次数: " + reactor.getTouch() + " 是否定时出现: " + reactor.isTimerActive() + " 反应堆类型: " + reactor.getReactorType());
        }
        if (reactor.getTouch() == 2) {
            ReactorScriptManager.getInstance().act(c, reactor);
        } else if ((reactor.getTouch() == 1) && (!reactor.isTimerActive())) {
            if (reactor.getReactorType() == 100) {
                int itemid = GameConstants.getCustomReactItem(reactor.getReactorId(), (reactor.getReactItem().getLeft()).intValue());
                if (c.getPlayer().haveItem(itemid, (reactor.getReactItem().getRight()))) {
                    if (reactor.getArea().contains(c.getPlayer().getTruePosition())) {
                        MapleInventoryManipulator.removeById(c, ItemConstants.getInventoryType(itemid), itemid, (reactor.getReactItem().getRight()), true, false);
                        reactor.hitReactor(c);
                    } else {
                        c.getPlayer().dropMessage(5, "距离太远。请靠近后重新尝试。");
                    }
                } else {
                    c.getPlayer().dropMessage(5, "You don't have the item required.");
                }
            } else {
                reactor.hitReactor(c);
            }
        }
    }

    public static void hitCoconut(SeekableLittleEndianAccessor slea, MapleClient c) {
        int id = slea.readShort();
        String co = "coconut";
        MapleCoconut map = (MapleCoconut) c.getChannelServer().getEvent(MapleEventType.Coconut);
        if ((map == null) || (!map.isRunning())) {
            map = (MapleCoconut) c.getChannelServer().getEvent(MapleEventType.CokePlay);
            co = "coke cap";
            if ((map == null) || (!map.isRunning())) {
                return;
            }
        }

        MapleCoconut.MapleCoconuts nut = map.getCoconut(id);
        if ((nut == null) || (!nut.isHittable())) {
            return;
        }
        if (System.currentTimeMillis() < nut.getHitTime()) {
            return;
        }

        if ((nut.getHits() > 2) && (Math.random() < 0.4D) && (!nut.isStopped())) {
            nut.setHittable(false);
            if ((Math.random() < 0.01D) && (map.getStopped() > 0)) {
                nut.setStopped(true);
                map.stopCoconut();
                c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.hitCoconut(false, id, 1));
                return;
            }
            nut.resetHits();

            if ((Math.random() < 0.05D) && (map.getBombings() > 0)) {
                c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.hitCoconut(false, id, 2));
                map.bombCoconut();
            } else if (map.getFalling() > 0) {
                c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.hitCoconut(false, id, 3));
                map.fallCoconut();
                if (c.getPlayer().getTeam() == 0) {
                    map.addMapleScore();
                } else {
                    map.addStoryScore();
                }

                c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.coconutScore(map.getCoconutScore()));
            }
        } else {
            nut.hit();
            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.hitCoconut(false, id, 1));
        }
    }

    public static void FollowRequest(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter tt = c.getPlayer().getMap().getCharacterById(slea.readInt());
        if (slea.readByte() > 0) {
            tt = c.getPlayer().getMap().getCharacterById(c.getPlayer().getFollowId());
            if ((tt != null) && (tt.getFollowId() == c.getPlayer().getId())) {
                tt.setFollowOn(true);
                c.getPlayer().setFollowOn(true);
            }
            return;
        }
        if ((tt != null) && (tt.getPosition().distanceSq(c.getPlayer().getPosition()) < 10000.0D) && (tt.getFollowId() == 0) && (c.getPlayer().getFollowId() == 0) && (tt.getId() != c.getPlayer().getId())) {
            tt.setFollowId(c.getPlayer().getId());
            tt.setFollowOn(false);
            tt.setFollowInitiator(false);
            c.getPlayer().setFollowOn(false);
            c.getPlayer().setFollowInitiator(false);
            tt.getClient().getSession().write(MaplePacketCreator.followRequest(c.getPlayer().getId()));
        } else {
            c.getSession().write(MaplePacketCreator.serverMessageRedText("距离太远。"));
        }
    }

    public static void FollowReply(SeekableLittleEndianAccessor slea, MapleClient c) {
        if ((c.getPlayer().getFollowId() > 0) && (c.getPlayer().getFollowId() == slea.readInt())) {
            MapleCharacter tt = c.getPlayer().getMap().getCharacterById(c.getPlayer().getFollowId());
            if ((tt != null) && (tt.getPosition().distanceSq(c.getPlayer().getPosition()) < 10000.0D) && (tt.getFollowId() == 0) && (tt.getId() != c.getPlayer().getId())) {
                boolean accepted = slea.readByte() > 0;
                if (accepted) {
                    tt.setFollowId(c.getPlayer().getId());
                    tt.setFollowOn(true);
                    tt.setFollowInitiator(false);
                    c.getPlayer().setFollowOn(true);
                    c.getPlayer().setFollowInitiator(true);
                    c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.followEffect(tt.getId(), c.getPlayer().getId(), null));
                } else {
                    c.getPlayer().setFollowId(0);
                    tt.setFollowId(0);
                    tt.getClient().getSession().write(MaplePacketCreator.getFollowMsg(5));
                }
            } else {
                if (tt != null) {
                    tt.setFollowId(0);
                    c.getPlayer().setFollowId(0);
                }
                c.getSession().write(MaplePacketCreator.serverMessageRedText("距离太远."));
            }
        } else {
            c.getPlayer().setFollowId(0);
        }
    }

    public static void DoRing(MapleClient c, String name, int itemid) {
        int newItemId = getMarriageNewItemId(itemid);
        MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(name);

        int errcode = 0;
        if (c.getPlayer().getMarriageId() > 0) {
            errcode = 28;
        } else if (c.getPlayer().getMarriageItemId() > 0) {
            errcode = 26;
        } else if ((!c.getPlayer().haveItem(itemid, 1)) || (itemid < 2240004) || (itemid > 2240015)) {
            errcode = 15;
        } else if (chr == null) {
            errcode = 21;
        } else if (chr.getMapId() != c.getPlayer().getMapId()) {
            errcode = 22;
        } else if (chr.getGender() == c.getPlayer().getGender()) {
            errcode = 25;
        } else if (chr.getMarriageId() > 0) {
            errcode = 29;
        } else if (chr.getMarriageItemId() > 0) {
            errcode = 27;
        } else if (!MapleInventoryManipulator.checkSpace(c, newItemId, 1, "")) {
            errcode = 23;
            System.err.println(new StringBuilder().append("自己是否有位置: ").append(!MapleInventoryManipulator.checkSpace(c, newItemId, 1, "")).toString());
        } else if (!MapleInventoryManipulator.checkSpace(chr.getClient(), newItemId, 1, "")) {
            errcode = 24;
            System.err.println(new StringBuilder().append("对方是否有位置: ").append(!MapleInventoryManipulator.checkSpace(c, newItemId, 1, "")).toString());
        }
        if (errcode > 0) {
            c.getSession().write(SkillPacket.sendEngagement((byte) errcode, 0, null, null));
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        c.getPlayer().setMarriageItemId(itemid);
        chr.getClient().getSession().write(SkillPacket.sendEngagementRequest(c.getPlayer().getName(), c.getPlayer().getId()));
    }

    public static void RingAction(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte mode = slea.readByte();
        if (mode == 0) {
            DoRing(c, slea.readMapleAsciiString(), slea.readInt());
        } else if (mode == 1) {
            c.getPlayer().setMarriageItemId(0);
        } else if (mode == 2) {
            boolean accepted = slea.readByte() > 0;
            String name = slea.readMapleAsciiString();
            int id = slea.readInt();
            MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(name);
            if ((c.getPlayer().getMarriageId() > 0) || (chr == null) || (chr.getId() != id) || (chr.getMarriageItemId() <= 0) || (!chr.haveItem(chr.getMarriageItemId(), 1)) || (chr.getMarriageId() > 0) || (!chr.isAlive()) || (chr.getEventInstance() != null) || (!c.getPlayer().isAlive()) || (c.getPlayer().getEventInstance() != null)) {
                c.getSession().write(SkillPacket.sendEngagement((byte) 31, (byte) 0, null, null));
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            if (accepted) {
                int itemid = chr.getMarriageItemId();
                int newItemId = getMarriageNewItemId(itemid);
                if ((!MapleInventoryManipulator.checkSpace(c, newItemId, 1, "")) || (!MapleInventoryManipulator.checkSpace(chr.getClient(), newItemId, 1, ""))) {
                    c.getSession().write(SkillPacket.sendEngagement((byte) 21, (byte) 0, null, null));
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }
                try {
                    int[] ringID = MapleRing.makeRing(newItemId, c.getPlayer(), chr);
                    Equip eq = (Equip) MapleItemInformationProvider.getInstance().getEquipById(newItemId, ringID[1]);
                    MapleRing ring = MapleRing.loadFromDb(ringID[1]);
                    if (ring != null) {
                        eq.setRing(ring);
                    }
                    MapleInventoryManipulator.addbyItem(c, eq);
                    eq = (Equip) MapleItemInformationProvider.getInstance().getEquipById(newItemId, ringID[0]);
                    ring = MapleRing.loadFromDb(ringID[0]);
                    if (ring != null) {
                        eq.setRing(ring);
                    }
                    MapleInventoryManipulator.addbyItem(chr.getClient(), eq);
                    MapleInventoryManipulator.removeById(chr.getClient(), MapleInventoryType.USE, chr.getMarriageItemId(), 1, false, false);
                    chr.getClient().getSession().write(SkillPacket.sendEngagement((byte) 13, newItemId, chr, c.getPlayer()));
                    chr.setMarriageId(c.getPlayer().getId());
                    c.getPlayer().setMarriageId(chr.getId());
                    chr.fakeRelog();
                    c.getPlayer().fakeRelog();
                    WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.yellowChat(new StringBuilder().append("[系统公告] 恭喜：").append(c.getPlayer().getName()).append(" 和 ").append(chr.getName()).append("结为夫妻。 希望你们在 ").append(chr.getClient().getChannelServer().getServerName()).append(" 游戏中玩的愉快!").toString()));
                } catch (Exception e) {
                    FileoutputUtil.outputFileError(FileoutputUtil.Packet_Ex, e);
                }
            } else {
                chr.getClient().getSession().write(SkillPacket.sendEngagement((byte) 32, 0, null, null));
            }
            c.getSession().write(MaplePacketCreator.enableActions());
            chr.setMarriageItemId(0);
        } else if (mode == 3) {
            int itemId = slea.readInt();
            MapleInventoryType type = ItemConstants.getInventoryType(itemId);
            Item item = c.getPlayer().getInventory(type).findById(itemId);
            if ((item != null) && (type == MapleInventoryType.ETC) && (itemId / 10000 == 421)) {
                MapleInventoryManipulator.drop(c, type, item.getPosition(), item.getQuantity());
            }
        }
    }

    private static int getMarriageNewItemId(int itemId) {
        int newItemId;
        if (itemId == 2240004) {
            newItemId = 1112300;
        } else {
            if (itemId == 2240005) {
                newItemId = 1112301;
            } else {
                if (itemId == 2240006) {
                    newItemId = 1112302;
                } else {
                    if (itemId == 2240007) {
                        newItemId = 1112303;
                    } else {
                        if (itemId == 2240008) {
                            newItemId = 1112304;
                        } else {
                            if (itemId == 2240009) {
                                newItemId = 1112305;
                            } else {
                                if (itemId == 2240010) {
                                    newItemId = 1112306;
                                } else {
                                    if (itemId == 2240011) {
                                        newItemId = 1112307;
                                    } else {
                                        if (itemId == 2240012) {
                                            newItemId = 1112308;
                                        } else {
                                            if (itemId == 2240013) {
                                                newItemId = 1112309;
                                            } else {
                                                if (itemId == 2240014) {
                                                    newItemId = 1112310;
                                                } else {
                                                    if (itemId == 2240015) {
                                                        newItemId = 1112311;
                                                    } else {
                                                        throw new RuntimeException("Invalid Item Maker id");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return newItemId;
    }

    public static void Solomon(SeekableLittleEndianAccessor slea, MapleClient c) {
        c.getSession().write(MaplePacketCreator.enableActions());
        Item item = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slea.readShort());
        if ((item == null) || (item.getItemId() != slea.readInt()) || (item.getQuantity() <= 0) || (c.getPlayer().getGachExp() > 0) || (c.getPlayer().getLevel() > 50) || (MapleItemInformationProvider.getInstance().getItemEffect(item.getItemId()).getEXP() <= 0)) {
            return;
        }
        c.getPlayer().setGachExp(c.getPlayer().getGachExp() + MapleItemInformationProvider.getInstance().getItemEffect(item.getItemId()).getEXP());
        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, item.getPosition(), (short) 1, false);
    }

    public static void GachExp(SeekableLittleEndianAccessor slea, MapleClient c) {
        c.getSession().write(MaplePacketCreator.enableActions());
        slea.readInt();
        if (c.getPlayer().getGachExp() <= 0) {
            return;
        }
        c.getPlayer().gainExp(c.getPlayer().getGachExp() * GameConstants.getExpRate_Quest(c.getPlayer().getLevel()), true, true, false);
        c.getPlayer().setGachExp(0);
    }

    public static void ChangeSet(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if ((c.getPlayer() == null) || (c.getPlayer().getMap() == null)) {
            return;
        }
    }

    public static boolean inArea(MapleCharacter chr) {
        for (Rectangle rect : chr.getMap().getAreas()) {
            if (rect.contains(chr.getTruePosition())) {
                return true;
            }
        }
        for (MapleDefender mist : chr.getMap().getAllMistsThreadsafe()) {
            if ((mist.getOwnerId() == chr.getId()) && (mist.getMistType() == 2) && (mist.getBox().contains(chr.getTruePosition()))) {
                return true;
            }
        }
        return false;
    }

}
