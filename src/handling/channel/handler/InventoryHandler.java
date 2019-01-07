package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleQuestStatus;
import client.MapleStat;
import client.PlayerStats;
import client.Skill;
import client.SkillFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.MapleMount;
import client.inventory.MaplePet;
import client.inventory.ModifyInventory;
import constants.GameConstants;
import constants.ItemConstants;
import database.DatabaseConnection;
import handling.world.WorldBroadcastService;
import handling.world.party.MaplePartyCharacter;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import scripting.item.ItemScriptManager;
import scripting.npc.NPCScriptManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import server.RandomRewards;
import server.Randomizer;
import server.ScriptedItem;
import server.StructItemOption;
import server.StructRewardItem;
import server.cashshop.CashItemFactory;
import server.cashshop.CashItemInfo;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.maps.FieldLimitType;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.SavedLocationType;
import server.quest.MapleQuest;
import server.shops.HiredMerchant;
import server.shops.IMaplePlayerShop;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.InventoryPacket;
import tools.packet.MTSCSPacket;
import tools.packet.MobPacket;
import tools.packet.NPCPacket;
import tools.packet.PlayerShopPacket;

public class InventoryHandler {

    public static final int OWL_ID = 1;

    /**
     * 移动背包内的道具
     * @param slea
     * @param c
     */
    public static void ItemMove(SeekableLittleEndianAccessor slea, MapleClient c) {
        if ((c.getPlayer() == null) || (c.getPlayer().hasBlockedInventory())) {
            return;
        }
        c.getPlayer().setScrolledPosition((short) 0);
        MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());
        short src = slea.readShort();
        short dst = slea.readShort();
        short quantity = slea.readShort();
        if ((src < 0) && (dst > 0)) {
            MapleInventoryManipulator.unequip(c, src, dst);
        } else if (dst < 0) {
            MapleInventoryManipulator.equip(c, src, dst);
        } else if (dst == 0) {
            MapleInventoryManipulator.drop(c, type, src, quantity);
        } else {
            MapleInventoryManipulator.move(c, type, src, dst);
        }
    }

    public static void SwitchBag(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (c.getPlayer().hasBlockedInventory()) {
            return;
        }
        c.getPlayer().setScrolledPosition((short) 0);
        short src = (short) slea.readInt();
        short dst = (short) slea.readInt();
        if ((src < 100) || (dst < 100)) {
            return;
        }
        MapleInventoryManipulator.move(c, MapleInventoryType.ETC, src, dst);
    }

    public static void MoveBag(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (c.getPlayer().hasBlockedInventory()) {
            return;
        }
        c.getPlayer().setScrolledPosition((short) 0);
        boolean srcFirst = slea.readInt() > 0;
        if (slea.readByte() != 4) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        short dst = (short) slea.readInt();
        short src = slea.readShort();
        MapleInventoryManipulator.move(c, MapleInventoryType.ETC, srcFirst ? dst : src, srcFirst ? src : dst);
    }


    public static boolean UseCSbox(byte slot, int itemId, MapleClient c, MapleCharacter chr) {
        Item toUse = c.getPlayer().getInventory(ItemConstants.getInventoryType(itemId)).getItem((short) slot);
        c.getSession().write(MaplePacketCreator.enableActions());
        if ((toUse != null) && (toUse.getQuantity() >= 1) && (toUse.getItemId() == itemId) && (!chr.hasBlockedInventory())) {
            if ((chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot() > -1) && (chr.getInventory(MapleInventoryType.USE).getNextFreeSlot() > -1) && (chr.getInventory(MapleInventoryType.SETUP).getNextFreeSlot() > -1) && (chr.getInventory(MapleInventoryType.ETC).getNextFreeSlot() > -1)) {

                CashItemFactory ii = CashItemFactory.getInstance();
                List<Integer> rewards = ii.getRandomItemInfo().get(itemId);
                if ((rewards != null) && ((rewards.size()) > 0)) {
                    int ID = rewards.get(Randomizer.nextInt(rewards.size()));
                    if (chr.isShowPacket()) {
                        chr.dropMessage(5, "打开道具获得: " + ID);
                    }
                    MapleInventoryManipulator.addById(c, ID, (short) 1, "Reward item: " + itemId + " on " + FileoutputUtil.CurrentReadable_Date());
                    c.getSession().write(MaplePacketCreator.getShowItemGain(ID, (short) 1., true));
                    MapleInventoryManipulator.removeFromSlot(c, ItemConstants.getInventoryType(itemId), (short) slot, (byte) 1, false);
                    return true;
                } else {
                    chr.dropMessage(6, "出现未知错误.");
                }
            } else {
                chr.dropMessage(6, "背包空间不足。");
            }
        }
        return false;
    }

    public static boolean UseRewardItem(byte slot, int itemId, MapleClient c, MapleCharacter chr) {
        Item toUse = c.getPlayer().getInventory(ItemConstants.getInventoryType(itemId)).getItem((short) slot);
        c.getSession().write(MaplePacketCreator.enableActions());
        if ((toUse != null) && (toUse.getQuantity() >= 1) && (toUse.getItemId() == itemId) && (!chr.hasBlockedInventory())) {
            if ((chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot() > -1) && (chr.getInventory(MapleInventoryType.USE).getNextFreeSlot() > -1) && (chr.getInventory(MapleInventoryType.SETUP).getNextFreeSlot() > -1) && (chr.getInventory(MapleInventoryType.ETC).getNextFreeSlot() > -1)) {
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                if (itemId == 2028048) {
                    int mesars = 5000000;
                    if ((mesars > 0) && (chr.getMeso() < 2147483647 - mesars)) {
                        int gainmes = Randomizer.nextInt(mesars);
                        chr.gainMeso(gainmes, true, true);
                        c.getSession().write(MTSCSPacket.sendMesobagSuccess(gainmes));

                        MapleInventoryManipulator.removeFromSlot(c, ItemConstants.getInventoryType(itemId), (short) slot, (byte) 1, false);
                        return true;
                    }
                    chr.dropMessage(1, "金币已达到上限无法使用这个道具.");
                    return false;
                }

                Pair<Integer, List<StructRewardItem>> rewards = ii.getRewardItem(itemId);

                if ((rewards != null) && ((rewards.getLeft()) > 0)) {
                    while (true) {
                        for (StructRewardItem reward : rewards.getRight()) {
                            if ((reward.prob > 0) && (Randomizer.nextInt((rewards.getLeft())) < reward.prob)) {
                                if (ItemConstants.getInventoryType(reward.itemid) == MapleInventoryType.EQUIP) {
                                    Item item = ii.getEquipById(reward.itemid);
                                    if (reward.period > 0L) {
                                        item.setExpiration(System.currentTimeMillis() + reward.period * 60L * 1000L);
                                    }
                                    item.setGMLog("Reward item: " + itemId + " on " + FileoutputUtil.CurrentReadable_Date());
                                    if (chr.isShowPacket()) {
                                        chr.dropMessage(5, "打开道具获得: " + item.getItemId());
                                    }
                                    if (reward.itemid / 1000 == 1182) {
                                        ii.randomize休彼德蔓徽章((Equip) item);
                                    }
                                    MapleInventoryManipulator.addbyItem(c, item);
                                    c.getSession().write(MaplePacketCreator.getShowItemGain(item.getItemId(), item.getQuantity(), true));
                                } else {
                                    if (chr.isShowPacket()) {
                                        chr.dropMessage(5, "打开道具获得: " + reward.itemid + " - " + reward.quantity);
                                    }
                                    MapleInventoryManipulator.addById(c, reward.itemid, reward.quantity, "Reward item: " + itemId + " on " + FileoutputUtil.CurrentReadable_Date());
                                    c.getSession().write(MaplePacketCreator.getShowItemGain(reward.itemid, reward.quantity, true));
                                }

                                MapleInventoryManipulator.removeFromSlot(c, ItemConstants.getInventoryType(itemId), (short) slot, (byte) 1, false);
                                c.getSession().write(MaplePacketCreator.showRewardItemAnimation(reward.itemid, reward.effect));
                                chr.getMap().broadcastMessage(chr, MaplePacketCreator.showRewardItemAnimation(reward.itemid, reward.effect, chr.getId()), false);
                                return true;
                            }
                        }
                    }
                }
                chr.dropMessage(6, "出现未知错误.");
            } else {
                chr.dropMessage(6, "背包空间不足。");
            }
        }
        return false;
    }

    /**
     * 使用道具
     * @param slea
     * @param c
     * @param chr
     */
    public static void UseItem(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if ((chr == null) || (!chr.isAlive()) || (chr.getMapId() == 749040100) || (chr.getMap() == null) || (chr.hasBlockedInventory())) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        long time = System.currentTimeMillis();
        if (chr.getNextConsume() > time) {
            chr.dropMessage(5, "暂时无法使用这个道具，请稍后在试。");
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        byte slot = (byte) slea.readShort();
        int itemId = slea.readInt();
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem((short) slot);
        if ((toUse == null) || (toUse.getQuantity() < 1) || (toUse.getItemId() != itemId)) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        if (!FieldLimitType.PotionUse.check(chr.getMap().getFieldLimit())) {
            if (MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId()).applyTo(chr)) {
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (byte) 1, false);
                if (chr.getMap().getConsumeItemCoolTime() > 0) {
                    chr.setNextConsume(time + chr.getMap().getConsumeItemCoolTime() * 1000);
                }
            }
        } else {
            c.getSession().write(MaplePacketCreator.enableActions());
        }
    }

    public static void UseCosmetic(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if ((chr == null) || (!chr.isAlive()) || (chr.getMap() == null) || (chr.hasBlockedInventory())) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        byte slot = (byte) slea.readShort();
        int itemId = slea.readInt();
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem((short) slot);
        if ((toUse == null) || (toUse.getQuantity() < 1) || (toUse.getItemId() != itemId) || (itemId / 10000 != 254) || (itemId / 1000 % 10 != chr.getGender())) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        if (MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId()).applyTo(chr)) {
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (byte) 1, false);
        }
    }

    public static void UseReducer(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || !chr.isAlive() || chr.getMap() == null || chr.hasBlockedInventory()) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        int itemId = slea.readInt();
        byte slot = (byte) slea.readInt();
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem((short) slot);
        if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId || itemId / 1000 != 2702) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        chr.equipChanged();
        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, toUse.getPosition(), (byte) 1, false);
        c.getSession().write(MaplePacketCreator.enableActions());
    }

    /**
     * 使用回程卷轴
     * @param slea
     * @param c
     * @param chr
     */
    public static void UseReturnScroll(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if ((!chr.isAlive()) || (chr.getMapId() == 749040100) || (chr.hasBlockedInventory()) || (chr.isInBlockedMap())) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        byte slot = (byte) slea.readShort();
        int itemId = slea.readInt();
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem((short) slot);
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if ((toUse == null) || (toUse.getQuantity() < 1) || (toUse.getItemId() != itemId)) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        if (!FieldLimitType.PotionUse.check(chr.getMap().getFieldLimit())) {
            if (ii.getItemEffect(toUse.getItemId()).applyReturnScroll(chr)) {
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (byte) 1, false);
            } else {
                c.getSession().write(MaplePacketCreator.enableActions());
            }
        } else {
            c.getSession().write(MaplePacketCreator.enableActions());
        }
    }

    public static void UseCatchItem(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        c.getPlayer().setScrolledPosition((short) 0);
        byte slot = (byte) slea.readShort();
        int itemid = slea.readInt();
        MapleMonster mob = chr.getMap().getMonsterByOid(slea.readInt());
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem((short) slot);
        MapleMap map = chr.getMap();
        if ((toUse != null) && (toUse.getQuantity() > 0) && (toUse.getItemId() == itemid) && (mob != null) && (!chr.hasBlockedInventory()) && (itemid / 10000 == 227)) {
            if ((!MapleItemInformationProvider.getInstance().isMobHP(itemid)) || (mob.getHp() <= mob.getMobMaxHp() / 2L)) {
                map.broadcastMessage(MobPacket.catchMonster(mob.getObjectId(), itemid, (byte) 1));
                map.killMonster(mob, chr, true, false, (byte) 1);
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (byte) 1, false, false);
                if (MapleItemInformationProvider.getInstance().getCreateId(itemid) > 0) {
                    MapleInventoryManipulator.addById(c, MapleItemInformationProvider.getInstance().getCreateId(itemid), (short) 1, "Catch item " + itemid + " on " + FileoutputUtil.CurrentReadable_Date());
                }
            } else {
                map.broadcastMessage(MobPacket.catchMonster(mob.getObjectId(), itemid, (byte) 0));
                c.getSession().write(MobPacket.catchMob(mob.getId(), itemid, (byte) 0));
            }
        }
        c.getSession().write(MaplePacketCreator.enableActions());
    }

    public static void UseMountFood(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        byte slot = (byte) slea.readShort();
        int itemid = slea.readInt();
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem((short) slot);
        MapleMount mount = chr.getMount();
        if ((itemid / 10000 == 226) && (toUse != null) && (toUse.getQuantity() > 0) && (toUse.getItemId() == itemid) && (mount != null) && (!c.getPlayer().hasBlockedInventory())) {
            int fatigue = mount.getFatigue();
            boolean levelup = false;
            mount.setFatigue((byte) -30);
            if (fatigue > 0) {
                mount.increaseExp();
                int level = mount.getLevel();
                if ((level < 30) && (mount.getExp() >= GameConstants.getMountExpNeededForLevel(level + 1))) {
                    mount.setLevel((byte) (level + 1));
                    levelup = true;
                }
            }
            chr.getMap().broadcastMessage(MaplePacketCreator.updateMount(chr, levelup));
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (byte) 1, false);
        }
        c.getSession().write(MaplePacketCreator.enableActions());
    }

    public static void UseScriptedNPCItem(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        byte slot = (byte) slea.readShort();
        int itemId = slea.readInt();
        Item toUse = chr.getInventory(ItemConstants.getInventoryType(itemId)).getItem((short) slot);
        long expiration_days = 0L;
        int mountid = 0;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        ScriptedItem info = ii.getScriptedItemInfo(itemId);
        if (info == null) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        if ((toUse != null) && (toUse.getQuantity() >= 1) && (toUse.getItemId() == itemId) && (!chr.hasBlockedInventory())) {
            MapleQuestStatus marr;
            long lastTime;
            switch (toUse.getItemId()) {
                case 2430007:
                    MapleInventory inventory = chr.getInventory(MapleInventoryType.SETUP);
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (byte) 1, false);
                    if ((inventory.countById(3994102) >= 20) && (inventory.countById(3994103) >= 20) && (inventory.countById(3994104) >= 20) && (inventory.countById(3994105) >= 20)) {
                        MapleInventoryManipulator.addById(c, 2430008, (short) 1, "Scripted item: " + itemId + " on " + FileoutputUtil.CurrentReadable_Date());
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.SETUP, 3994102, 20, false, false);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.SETUP, 3994103, 20, false, false);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.SETUP, 3994104, 20, false, false);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.SETUP, 3994105, 20, false, false);
                    } else {
                        MapleInventoryManipulator.addById(c, 2430007, (short) 1, "Scripted item: " + itemId + " on " + FileoutputUtil.CurrentReadable_Date());
                    }
                    NPCScriptManager.getInstance().start(c, 2084001);
                    break;
                case 2430008:
                    chr.saveLocation(SavedLocationType.RICHIE);

                    boolean warped = false;
                    for (int i = 390001000; i <= 390001004; i++) {
                        MapleMap map = c.getChannelServer().getMapFactory().getMap(i);
                        if (map.getCharactersSize() == 0) {
                            chr.changeMap(map, map.getPortal(0));
                            warped = true;
                            break;
                        }
                    }
                    if (warped) {
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (byte) 1, false);
                    } else {
                        c.getPlayer().dropMessage(5, "All maps are currently in use, please try again later.");
                    }
                    break;
                case 2430112:
                    if (c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
                        if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430112) >= 25) {
                            if ((MapleInventoryManipulator.checkSpace(c, 2049400, 1, "")) && (MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 25, true, false))) {
                                MapleInventoryManipulator.addById(c, 2049400, (short) 1, "Scripted item: " + toUse.getItemId() + " on " + FileoutputUtil.CurrentReadable_Date());
                            } else {
                                c.getPlayer().dropMessage(5, "消耗栏空间位置不足.");
                            }
                        } else if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430112) >= 10) {
                            if ((MapleInventoryManipulator.checkSpace(c, 2049401, 1, "")) && (MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 10, true, false))) {
                                MapleInventoryManipulator.addById(c, 2049401, (short) 1, "Scripted item: " + toUse.getItemId() + " on " + FileoutputUtil.CurrentReadable_Date());
                            } else {
                                c.getPlayer().dropMessage(5, "消耗栏空间位置不足.");
                            }
                        } else {
                            ItemScriptManager.getInstance().start(c, info.getNpc(), toUse);
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "消耗栏空间位置不足.");
                    }
                    break;
                case 2430481:
                    if (c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
                        if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430481) >= 100) {
                            if ((MapleInventoryManipulator.checkSpace(c, 2049701, 1, "")) && (MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 100, true, false))) {
                                MapleInventoryManipulator.addById(c, 2049701, (short) 1, "Scripted item: " + toUse.getItemId() + " on " + FileoutputUtil.CurrentReadable_Date());
                            } else {
                                c.getPlayer().dropMessage(5, "消耗栏空间位置不足.");
                            }
                        } else if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430481) >= 30) {
                            if ((MapleInventoryManipulator.checkSpace(c, 2049400, 1, "")) && (MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 30, true, false))) {
                                MapleInventoryManipulator.addById(c, 2049400, (short) 1, "Scripted item: " + toUse.getItemId() + " on " + FileoutputUtil.CurrentReadable_Date());
                            } else {
                                c.getPlayer().dropMessage(5, "消耗栏空间位置不足.");
                            }
                        } else {
                            ItemScriptManager.getInstance().start(c, info.getNpc(), toUse);
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "消耗栏空间位置不足.");
                    }
                    break;
                case 2430760:
                    if (c.getPlayer().getInventory(MapleInventoryType.CASH).getNumFreeSlot() >= 1) {
                        if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430760) >= 10) {
                            if ((MapleInventoryManipulator.checkSpace(c, 5750000, 1, "")) && (MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 10, true, false))) {
                                MapleInventoryManipulator.addById(c, 5750000, (short) 1, "Scripted item: " + toUse.getItemId() + " on " + FileoutputUtil.CurrentReadable_Date());
                            } else {
                                c.getPlayer().dropMessage(5, "请检测背包空间是否足够.");
                            }
                        } else {
                            c.getPlayer().dropMessage(5, "10个星岩魔方碎片才可以兑换1个星岩魔方.");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "请检测背包空间是否足够.");
                    }
                    break;
                case 2430691:
                    if (c.getPlayer().getInventory(MapleInventoryType.CASH).getNumFreeSlot() >= 1) {
                        if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430691) >= 10) {
                            if ((MapleInventoryManipulator.checkSpace(c, 5750001, 1, "")) && (MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 10, true, false))) {
                                MapleInventoryManipulator.addById(c, 5750001, (short) 1, "Scripted item: " + toUse.getItemId() + " on " + FileoutputUtil.CurrentReadable_Date());
                            } else {
                                c.getPlayer().dropMessage(5, "请检测背包空间是否足够.");
                            }
                        } else {
                            c.getPlayer().dropMessage(5, "10个星岩电钻机碎片才可以兑换1个星岩电钻机.");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "请检测背包空间是否足够.");
                    }
                    break;
                case 5680019:
                    int hair = 32150 + c.getPlayer().getHair() % 10;
                    c.getPlayer().setHair(hair);
                    c.getPlayer().updateSingleStat(MapleStat.发型, hair);
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, (short) slot, (short) 1, false);

                    break;
                case 5680020:
                    hair = 32160 + c.getPlayer().getHair() % 10;
                    c.getPlayer().setHair(hair);
                    c.getPlayer().updateSingleStat(MapleStat.发型, hair);
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, (short) slot, (short) 1, false);

                    break;
                case 3994225:
                    c.getPlayer().dropMessage(5, "Please bring this item to the NPC.");
                    break;
                case 2430214:
                case 2430220:
                    if (c.getPlayer().getFatigue() <= 0) {
                        break;
                    }
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
                    c.getPlayer().setFatigue(c.getPlayer().getFatigue() - 30);
                    break;
                case 2430227:
                    if (c.getPlayer().getFatigue() <= 0) {
                        break;
                    }
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
                    c.getPlayer().setFatigue(c.getPlayer().getFatigue() - 50);
                    break;
                case 2430144:
                    int itemid = Randomizer.nextInt(373) + 2290000;
                    if ((!MapleItemInformationProvider.getInstance().itemExists(itemid)) || (MapleItemInformationProvider.getInstance().getName(itemid).contains("Special")) || (MapleItemInformationProvider.getInstance().getName(itemid).contains("Event"))) {
                        break;
                    }
                    MapleInventoryManipulator.addById(c, itemid, (short) 1, "Reward item: " + toUse.getItemId() + " on " + FileoutputUtil.CurrentReadable_Date());
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
                    break;
                case 2430370:
                    if (!MapleInventoryManipulator.checkSpace(c, 2028062, 1, "")) {
                        break;
                    }
                    MapleInventoryManipulator.addById(c, 2028062, (short) 1, "Reward item: " + toUse.getItemId() + " on " + FileoutputUtil.CurrentReadable_Date());
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
                    break;
                case 2430158:
                    if (c.getPlayer().getInventory(MapleInventoryType.ETC).getNumFreeSlot() >= 1) {
                        if (c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4000630) >= 100) {
                            if ((MapleInventoryManipulator.checkSpace(c, 4310010, 1, "")) && (MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 1, true, false))) {
                                MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4000630, 100, true, false);
                                MapleInventoryManipulator.addById(c, 4310010, (short) 1, "Scripted item: " + toUse.getItemId() + " on " + FileoutputUtil.CurrentReadable_Date());
                            } else {
                                c.getPlayer().dropMessage(5, "其他栏空间位置不足.");
                            }
                        } else if (c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4000630) >= 50) {
                            if ((MapleInventoryManipulator.checkSpace(c, 4310009, 1, "")) && (MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 1, true, false))) {
                                MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4000630, 50, true, false);
                                MapleInventoryManipulator.addById(c, 4310009, (short) 1, "Scripted item: " + toUse.getItemId() + " on " + FileoutputUtil.CurrentReadable_Date());
                            } else {
                                c.getPlayer().dropMessage(5, "其他栏空间位置不足.");
                            }
                        } else {
                            c.getPlayer().dropMessage(5, "需要50个净化图腾才能兑换出狮子王的贵族勋章，100个净化图腾才能兑换狮子王的皇家勋章。");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "其他栏空间位置不足.");
                    }
                    break;
                case 2430159:
                    MapleQuest.getInstance(3182).forceComplete(c.getPlayer(), 2161004);
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
                    break;
                case 2430200:
                    if (c.getPlayer().getQuestStatus(31152) != 2) {
                        c.getPlayer().dropMessage(5, "You have no idea how to use it.");
                    } else if (c.getPlayer().getInventory(MapleInventoryType.ETC).getNumFreeSlot() >= 1) {
                        if ((c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4000660) >= 1) && (c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4000661) >= 1) && (c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4000662) >= 1) && (c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4000663) >= 1)) {
                            if ((MapleInventoryManipulator.checkSpace(c, 4032923, 1, "")) && (MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 1, true, false)) && (MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4000660, 1, true, false)) && (MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4000661, 1, true, false)) && (MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4000662, 1, true, false)) && (MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4000663, 1, true, false))) {
                                MapleInventoryManipulator.addById(c, 4032923, (short) 1, "Scripted item: " + toUse.getItemId() + " on " + FileoutputUtil.CurrentReadable_Date());
                            } else {
                                c.getPlayer().dropMessage(5, "其他栏空间位置不足.");
                            }
                        } else {
                            c.getPlayer().dropMessage(5, "There needs to be 1 of each Stone for a Dream Key.");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "其他栏空间位置不足.");
                    }

                    break;
                case 2430132:
                case 2430133:
                case 2430134:
                case 2430142:
                    if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() >= 1) {
                        if ((c.getPlayer().getJob() == 3200) || (c.getPlayer().getJob() == 3210) || (c.getPlayer().getJob() == 3211) || (c.getPlayer().getJob() == 3212)) {
                            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
                            MapleInventoryManipulator.addById(c, 1382101, (short) 1, "Scripted item: " + itemId + " on " + FileoutputUtil.CurrentReadable_Date());
                        } else if ((c.getPlayer().getJob() == 3300) || (c.getPlayer().getJob() == 3310) || (c.getPlayer().getJob() == 3311) || (c.getPlayer().getJob() == 3312)) {
                            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
                            MapleInventoryManipulator.addById(c, 1462093, (short) 1, "Scripted item: " + itemId + " on " + FileoutputUtil.CurrentReadable_Date());
                        } else if ((c.getPlayer().getJob() == 3500) || (c.getPlayer().getJob() == 3510) || (c.getPlayer().getJob() == 3511) || (c.getPlayer().getJob() == 3512)) {
                            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
                            MapleInventoryManipulator.addById(c, 1492080, (short) 1, "Scripted item: " + itemId + " on " + FileoutputUtil.CurrentReadable_Date());
                        } else {
                            c.getPlayer().dropMessage(5, "您无法使用这个道具。");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "背包空间不足。");
                    }

                    break;
                case 2430455:
                    ItemScriptManager.getInstance().start(c, 9010000, toUse);
                    break;
                case 2430036: //TODO 添加骑宠代码
                    mountid = 1027;
                    expiration_days = 1L;
                    break;
                case 2430170:
                    mountid = 1027;
                    expiration_days = 7L;
                    break;
                case 2430037:
                    mountid = 1028;
                    expiration_days = 1L;
                    break;
                case 2430038:
                    mountid = 1029;
                    expiration_days = 1L;
                    break;
                case 2430039:
                    mountid = 1030;
                    expiration_days = 1L;
                    break;
                case 2430040:
                    mountid = 1031;
                    expiration_days = 1L;
                    break;
                case 2430223:
                    mountid = 1031;
                    expiration_days = 15L;
                    break;
                case 2430259:
                    mountid = 1031;
                    expiration_days = 3L;
                    break;
                case 2430242:
                    mountid = 80001018;
                    expiration_days = 10L;
                    break;
                case 2430243:
                    mountid = 80001019;
                    expiration_days = 10L;
                    break;
                case 2430261:
                    mountid = 80001019;
                    expiration_days = 3L;
                    break;
                case 2430249:
                    mountid = 80001027;
                    expiration_days = 3L;
                    break;
                case 2430225:
                    mountid = 1031;
                    expiration_days = 10L;
                    break;
                case 2430053:
                    mountid = 1027;
                    expiration_days = 1L;
                    break;
                case 2430054:
                    mountid = 1028;
                    expiration_days = 30L;
                    break;
                case 2430055:
                    mountid = 1029;
                    expiration_days = 30L;
                    break;
                case 2430257:
                    mountid = 1029;
                    expiration_days = 7L;
                    break;
                case 2430056:
                    mountid = 1035;
                    expiration_days = 30L;
                    break;
                case 2430057:
                    mountid = 1033;
                    expiration_days = 30L;
                    break;
                case 2430072:
                    mountid = 1034;
                    expiration_days = 7L;
                    break;
                case 2430073:
                    mountid = 1036;
                    expiration_days = 15L;
                    break;
                case 2430074:
                    mountid = 1037;
                    expiration_days = 15L;
                    break;
                case 2430272:
                    mountid = 1038;
                    expiration_days = 3L;
                    break;
                case 2430275:
                    mountid = 80001033;
                    expiration_days = 7L;
                    break;
                case 2430075:
                    mountid = 1038;
                    expiration_days = 15L;
                    break;
                case 2430076:
                    mountid = 1039;
                    expiration_days = 15L;
                    break;
                case 2430077:
                    mountid = 1040;
                    expiration_days = 15L;
                    break;
                case 2430080:
                    mountid = 1042;
                    expiration_days = 20L;
                    break;
                case 2430082:
                    mountid = 1044;
                    expiration_days = 7L;
                    break;
                case 2430260:
                    mountid = 1044;
                    expiration_days = 3L;
                    break;
                case 2430091:
                    mountid = 1049;
                    expiration_days = 10L;
                    break;
                case 2430092:
                    mountid = 1050;
                    expiration_days = 10L;
                    break;
                case 2430263:
                    mountid = 1050;
                    expiration_days = 3L;
                    break;
                case 2430093:
                    mountid = 1051;
                    expiration_days = 10L;
                    break;
                case 2430101:
                    mountid = 1052;
                    expiration_days = 10L;
                    break;
                case 2430102:
                    mountid = 1053;
                    expiration_days = 10L;
                    break;
                case 2430103:
                    mountid = 1054;
                    expiration_days = 30L;
                    break;
                case 2430266:
                    mountid = 1054;
                    expiration_days = 3L;
                    break;
                case 2430265:
                    mountid = 1151;
                    expiration_days = 3L;
                    break;
                case 2430258:
                    mountid = 1115;
                    expiration_days = 365L;
                    break;
                case 2430117:
                    mountid = 1036;
                    expiration_days = 365L;
                    break;
                case 2430118:
                    mountid = 1039;
                    expiration_days = 365L;
                    break;
                case 2430119:
                    mountid = 1040;
                    expiration_days = 365L;
                    break;
                case 2430120:
                    mountid = 1037;
                    expiration_days = 365L;
                    break;
                case 2430271:
                    mountid = 1069;
                    expiration_days = 3L;
                    break;
                case 2430136:
                    mountid = 1069;
                    expiration_days = 15L;
                    break;
                case 2430137:
                    mountid = 1069;
                    expiration_days = 30L;
                    break;
                case 2430138:
                    mountid = 1069;
                    expiration_days = 365L;
                    break;
                case 2430145:
                    mountid = 1070;
                    expiration_days = 30L;
                    break;
                case 2430146:
                    mountid = 1070;
                    expiration_days = 365L;
                    break;
                case 2430147:
                    mountid = 1071;
                    expiration_days = 30L;
                    break;
                case 2430148:
                    mountid = 1071;
                    expiration_days = 365L;
                    break;
                case 2430135:
                    mountid = 1065;
                    expiration_days = 15L;
                    break;
                case 2430149:
                    mountid = 1072;
                    expiration_days = 30L;
                    break;
                case 2430262:
                    mountid = 1072;
                    expiration_days = 3L;
                    break;
                case 2430179:
                    mountid = 1081;
                    expiration_days = 15L;
                    break;
                case 2430264:
                    mountid = 1081;
                    expiration_days = 3L;
                    break;
                case 2430201:
                    mountid = 1096;
                    expiration_days = 3L;
                    break;
                case 2430228:
                    mountid = 1101;
                    expiration_days = 15L;
                    break;
                case 2430276:
                    mountid = 1101;
                    expiration_days = 15L;
                    break;
                case 2430277:
                    mountid = 1101;
                    expiration_days = 365L;
                    break;
                case 2430283:
                    mountid = 1025;
                    expiration_days = 10L;
                    break;
                case 2430291:
                    mountid = 1145;
                    expiration_days = -1L;
                    break;
                case 2430293:
                    mountid = 1146;
                    expiration_days = -1L;
                    break;
                case 2430295:
                    mountid = 1147;
                    expiration_days = -1L;
                    break;
                case 2430297:
                    mountid = 1148;
                    expiration_days = -1L;
                    break;
                case 2430299:
                    mountid = 1149;
                    expiration_days = -1L;
                    break;
                case 2430301:
                    mountid = 1150;
                    expiration_days = -1L;
                    break;
                case 2430303:
                    mountid = 1151;
                    expiration_days = -1L;
                    break;
                case 2430305:
                    mountid = 1152;
                    expiration_days = -1L;
                    break;
                case 2430307:
                    mountid = 1153;
                    expiration_days = -1L;
                    break;
                case 2430309:
                    mountid = 1154;
                    expiration_days = -1L;
                    break;
                case 2430311:
                    mountid = 1156;
                    expiration_days = -1L;
                    break;
                case 2430313:
                    mountid = 1156;
                    expiration_days = -1L;
                    break;
                case 2430315:
                    mountid = 1118;
                    expiration_days = -1L;
                    break;
                case 2430317:
                    mountid = 1121;
                    expiration_days = -1L;
                    break;
                case 2430319:
                    mountid = 1122;
                    expiration_days = -1L;
                    break;
                case 2430321:
                    mountid = 1123;
                    expiration_days = -1L;
                    break;
                case 2430323:
                    mountid = 1124;
                    expiration_days = -1L;
                    break;
                case 2430325:
                    mountid = 1129;
                    expiration_days = -1L;
                    break;
                case 2430327:
                    mountid = 1130;
                    expiration_days = -1L;
                    break;
                case 2430329:
                    mountid = 1063;
                    expiration_days = -1L;
                    break;
                case 2430331:
                    mountid = 1025;
                    expiration_days = -1L;
                    break;
                case 2430333:
                    mountid = 1034;
                    expiration_days = -1L;
                    break;
                case 2430335:
                    mountid = 1136;
                    expiration_days = -1L;
                    break;
                case 2430337:
                    mountid = 1051;
                    expiration_days = -1L;
                    break;
                case 2430339:
                    mountid = 1138;
                    expiration_days = -1L;
                    break;
                case 2430341:
                    mountid = 1139;
                    expiration_days = -1L;
                    break;
                case 2430343:
                    mountid = 1027;
                    expiration_days = -1L;
                    break;
                case 2430346:
                    mountid = 1029;
                    expiration_days = -1L;
                    break;
                case 2430348:
                    mountid = 1028;
                    expiration_days = -1L;
                    break;
                case 2430350:
                    mountid = 1033;
                    expiration_days = -1L;
                    break;
                case 2430352:
                    mountid = 1064;
                    expiration_days = -1L;
                    break;
                case 2430354:
                    mountid = 1096;
                    expiration_days = -1L;
                    break;
                case 2430356:
                    mountid = 1101;
                    expiration_days = -1L;
                    break;
                case 2430358:
                    mountid = 1102;
                    expiration_days = -1L;
                    break;
                case 2430360:
                    mountid = 1054;
                    expiration_days = -1L;
                    break;
                case 2430362:
                    mountid = 1053;
                    expiration_days = -1L;
                    break;
                case 2430292:
                    mountid = 1145;
                    expiration_days = 90L;
                    break;
                case 2430294:
                    mountid = 1146;
                    expiration_days = 90L;
                    break;
                case 2430296:
                    mountid = 1147;
                    expiration_days = 90L;
                    break;
                case 2430298:
                    mountid = 1148;
                    expiration_days = 90L;
                    break;
                case 2430300:
                    mountid = 1149;
                    expiration_days = 90L;
                    break;
                case 2430302:
                    mountid = 1150;
                    expiration_days = 90L;
                    break;
                case 2430304:
                    mountid = 1151;
                    expiration_days = 90L;
                    break;
                case 2430306:
                    mountid = 1152;
                    expiration_days = 90L;
                    break;
                case 2430308:
                    mountid = 1153;
                    expiration_days = 90L;
                    break;
                case 2430310:
                    mountid = 1154;
                    expiration_days = 90L;
                    break;
                case 2430312:
                    mountid = 1156;
                    expiration_days = 90L;
                    break;
                case 2430314:
                    mountid = 1156;
                    expiration_days = 90L;
                    break;
                case 2430316:
                    mountid = 1118;
                    expiration_days = 90L;
                    break;
                case 2430318:
                    mountid = 1121;
                    expiration_days = 90L;
                    break;
                case 2430320:
                    mountid = 1122;
                    expiration_days = 90L;
                    break;
                case 2430322:
                    mountid = 1123;
                    expiration_days = 90L;
                    break;
                case 2430326:
                    mountid = 1129;
                    expiration_days = 90L;
                    break;
                case 2430328:
                    mountid = 1130;
                    expiration_days = 90L;
                    break;
                case 2430330:
                    mountid = 1063;
                    expiration_days = 90L;
                    break;
                case 2430332:
                    mountid = 1025;
                    expiration_days = 90L;
                    break;
                case 2430334:
                    mountid = 1034;
                    expiration_days = 90L;
                    break;
                case 2430336:
                    mountid = 1136;
                    expiration_days = 90L;
                    break;
                case 2430338:
                    mountid = 1051;
                    expiration_days = 90L;
                    break;
                case 2430340:
                    mountid = 1138;
                    expiration_days = 90L;
                    break;
                case 2430342:
                    mountid = 1139;
                    expiration_days = 90L;
                    break;
                case 2430344:
                    mountid = 1027;
                    expiration_days = 90L;
                    break;
                case 2430347:
                    mountid = 1029;
                    expiration_days = 90L;
                    break;
                case 2430349:
                    mountid = 1028;
                    expiration_days = 90L;
                    break;
                case 2430351:
                    mountid = 1033;
                    expiration_days = 90L;
                    break;
                case 2430353:
                    mountid = 1064;
                    expiration_days = 90L;
                    break;
                case 2430355:
                    mountid = 1096;
                    expiration_days = 90L;
                    break;
                case 2430357:
                    mountid = 1101;
                    expiration_days = 90L;
                    break;
                case 2430359:
                    mountid = 1102;
                    expiration_days = 90L;
                    break;
                case 2430361:
                    mountid = 1054;
                    expiration_days = 90L;
                    break;
                case 2430363:
                    mountid = 1053;
                    expiration_days = 90L;
                    break;
                case 2430324:
                    mountid = 1158;
                    expiration_days = -1L;
                    break;
                case 2430345:
                    mountid = 1158;
                    expiration_days = 90L;
                    break;
                case 2430367:
                    mountid = 1115;
                    expiration_days = 3L;
                    break;
                case 2430365:
                    mountid = 1025;
                    expiration_days = 365L;
                    break;
                case 2430366:
                    mountid = 1025;
                    expiration_days = 15L;
                    break;
                case 2430369:
                    mountid = 1049;
                    expiration_days = 10L;
                    break;
                case 2430392:
                    mountid = 80001038;
                    expiration_days = 90L;
                    break;
                case 2430476:
                    mountid = 1039;
                    expiration_days = 15L;
                    break;
                case 2430477:
                    mountid = 1039;
                    expiration_days = 365L;
                    break;
                case 2430232:
                    mountid = 1106;
                    expiration_days = 10L;
                    break;
                case 2430511:
                    mountid = 80001033;
                    expiration_days = 15L;
                    break;
                case 2430512:
                    mountid = 80001033;
                    expiration_days = 365L;
                    break;
                case 2430536:
                    mountid = 80001114;
                    expiration_days = -1L;
                    break;
                case 2430537:
                    mountid = 80001114;
                    expiration_days = 90L;
                    break;
                case 2430229:
                    mountid = 1102;
                    expiration_days = 60L;
                    break;
                case 2430199:
                    mountid = 1102;
                    expiration_days = 1L;
                    break;
                case 2430206:
                    mountid = 1089;
                    expiration_days = 7L;
                    break;
                case 2430211:
                    mountid = 80001009;
                    expiration_days = 30L;
                    break;
                case 2430050:
                    mountid = 1035;
                    expiration_days = -1;
                    break;
                case 2432311:
                    mountid = 1089;
                    expiration_days = -1;
                    break;
                case 2431473:
                    mountid = 80001257;
                    expiration_days = -1;
                    break;
                case 2430578:
                    mountid = 80001077;
                    expiration_days = 3;
                    break;
                case 2431855: {// 新手冒险家礼物箱
                    if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() >= 2 && c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 2) {
                        if (MapleInventoryManipulator.checkSpace(c, 1052646, 1, "") && MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 1, true, false)) {
                            MapleInventoryManipulator.addById(c, 1052646, (short) 1, "Scripted item: " + toUse.getItemId() + " on " + FileoutputUtil.CurrentReadable_Date());
                            MapleInventoryManipulator.addById(c, 1072850, (short) 1, "Scripted item: " + toUse.getItemId() + " on " + FileoutputUtil.CurrentReadable_Date());
                            MapleInventoryManipulator.addById(c, 2000013, (short) 50, "Scripted item: " + toUse.getItemId() + " on " + FileoutputUtil.CurrentReadable_Date());
                            MapleInventoryManipulator.addById(c, 2000014, (short) 50, "Scripted item: " + toUse.getItemId() + " on " + FileoutputUtil.CurrentReadable_Date());
                        } else {
                            c.getPlayer().dropMessage(0, "背包空间不足。");
                        }
                    } else {
                        c.getPlayer().dropMessage(0, "背包空间不足。");
                    }
                    break;
                }

                default:
                    ItemScriptManager.getInstance().start(c, info.getNpc(), toUse);
            }
        }

        if (mountid > 0) {
            mountid = mountid > 80001000 ? mountid : PlayerStats.getSkillByJob(mountid, c.getPlayer().getJob());
            int fk = GameConstants.getMountItem(mountid, c.getPlayer());

            if ((fk > 0) && (mountid < 80001000)) {
                for (int i = 80001001; i < 80001999; i++) {
                    Skill skill = SkillFactory.getSkill(i);
                    if ((skill != null) && (GameConstants.getMountItem(skill.getId(), c.getPlayer()) == fk)) {
                        mountid = i;
                        break;
                    }
                }
            }

            if (c.getPlayer().getSkillLevel(mountid) > 0) {
                c.getPlayer().dropMessage(1, "您已经拥有了[" + SkillFactory.getSkill(mountid).getName() + "]这个骑宠的技能，无法使用该道具。");
            } else if ((SkillFactory.getSkill(mountid) == null) || (GameConstants.getMountItem(mountid, c.getPlayer()) == 0)) {
                c.getPlayer().dropMessage(1, "您无法使用这个骑宠的技能.");
            } else if (expiration_days > 0L) {
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
                c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(mountid), (short) 1, (byte) 1, System.currentTimeMillis() + expiration_days * 24L * 60L * 60L * 1000L);
                c.getPlayer().dropMessage(1, "恭喜您获得[" + SkillFactory.getSkill(mountid).getName() + "]骑宠技能 " + expiration_days + " 天权。");
            } else if (expiration_days == -1L) {
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (byte) 1, false);
                c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(mountid), (short) 1, (byte) 1, -1L);
                c.getPlayer().dropMessage(1, "恭喜您获得[" + SkillFactory.getSkill(mountid).getName() + "]骑宠技能永久权。");
            }
        }
        c.getSession().write(MaplePacketCreator.enableActions());
    }

    /**
     * 使用召唤包
     * # 29 0B 00 08 0F 20 00
     * @param slea
     * @param c
     * @param chr
     */
    public static void UseSummonBag(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if ((!chr.isAlive()) || (chr.hasBlockedInventory())) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        byte slot = (byte) slea.readShort();
        int itemId = slea.readInt();
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem((short) slot);
        if ((toUse != null) && (toUse.getQuantity() >= 1) && (toUse.getItemId() == itemId) && ((c.getPlayer().getMapId() < 910000000) || (c.getPlayer().getMapId() > 910000022))) {
            Map<String, Integer> toSpawn = MapleItemInformationProvider.getInstance().getEquipStats(itemId);
            if (toSpawn == null) {
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            MapleMonster ht = null;
            int type = 0;

            for (Entry<String, Integer> i : toSpawn.entrySet()) {
                // for (Map.Entry i : toSpawn.entrySet()) {
                if (((i.getKey()).startsWith("mob")) && (Randomizer.nextInt(99) <= (i.getValue()))) {
                    ht = MapleLifeFactory.getMonster(Integer.parseInt(( i.getKey()).substring(3)));
                    chr.getMap().spawnMonster_sSack(ht, chr.getPosition(), type);
                }
            }
            if (ht == null) {
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
        }
        c.getSession().write(MaplePacketCreator.enableActions());
    }

    /**
     * 拾取道具
     * @param slea
     * @param c
     * @param chr
     */
    public static void PlayerPickupItem(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        // 63 CC FE 13 01 A4 86 01 00
        // 63 8E FF 8D 00 A1 86 01 00
        // 63 8E FF 8D 00 A1 86 01 00
        // 63 C7 FF F6 00 A8 86 01 00 拾取道具
        if (chr.hasBlockedInventory()) {
            chr.dropMessage(5, "现在还不能进行操作.");
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        c.getPlayer().setScrolledPosition((short) 0);
        Point Client_Reportedpos = slea.readPos();
        if (chr.getMap() == null) {
            return;
        }
        MapleMapObject ob = chr.getMap().getMapObject(slea.readInt(), MapleMapObjectType.ITEM);
        if (ob == null) {
            chr.dropMessage(5, "找不到地图上的道具");
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        MapleMapItem mapitem = (MapleMapItem) ob;
        Lock lock = mapitem.getLock();
        lock.lock();
        try {
            if (mapitem.isPickedUp()) {
                chr.dropMessage(5, "地图上的道具已经被拾取！");
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            if ((mapitem.getQuest() > 0) && (chr.getQuestStatus(mapitem.getQuest()) != 1)) {
                chr.dropMessage(5, "地图上的道具为任务道具");
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            if ((mapitem.getOwner() != chr.getId()) && (((!mapitem.isPlayerDrop()) && (mapitem.getDropType() == 0)) || ((mapitem.isPlayerDrop()) && (chr.getMap().getEverlast())))) {
                chr.dropMessage(5, "这个道具不属于你，等会再捡！");
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            if ((!mapitem.isPlayerDrop()) && (mapitem.getDropType() == 1) && (mapitem.getOwner() != chr.getId()) && ((chr.getParty() == null) || (chr.getParty().getMemberById(mapitem.getOwner()) == null))) {
                chr.dropMessage(5, "这个道具不属于你，等会再捡啊！");
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            // 飞镖类需要处理下数量问题
            if (ItemConstants.is飞镖道具(mapitem.getItemId())) {
                mapitem.getItem().setQuantity((short)0);
            }
            double Distance = Client_Reportedpos.distanceSq(mapitem.getPosition());
            if ((Distance > 5000.0D) && ((mapitem.getMeso() > 0) || (mapitem.getItemId() != 4001025))) {
                WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverMessageRedText("[GM 信息] " + chr.getName() + " ID: " + chr.getId() + " (等级 " + chr.getLevel() + ") 全屏捡物。地图ID: " + chr.getMapId() + " 范围: " + Distance));
            } else if (chr.getPosition().distanceSq(mapitem.getPosition()) > 640000.0D) {
                WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverMessageRedText("[GM 信息] " + chr.getName() + " ID: " + chr.getId() + " (等级 " + chr.getLevel() + ") 全屏捡物。地图ID: " + chr.getMapId() + " 范围: " + Distance));
            }
            if (mapitem.getMeso() > 0) { // 捡RMB
                if ((chr.getParty() != null) && (mapitem.getOwner() != chr.getId())) {
                    List<MapleCharacter> toGive = new LinkedList();
                    long splitMeso = mapitem.getMeso() * 40 / 100;
                    for (MaplePartyCharacter z : chr.getParty().getMembers()) {
                        MapleCharacter m = chr.getMap().getCharacterById(z.getId());
                        if ((m != null) && (m.getId() != chr.getId())) {
                            toGive.add(m);
                        }
                    }
                    for (MapleCharacter m : toGive) {
                        m.gainMeso(splitMeso / toGive.size() + (m.getStat().hasPartyBonus ? (int) (mapitem.getMeso() / 20.0D) : 0), true);
                    }
                    chr.gainMeso(mapitem.getMeso() - splitMeso, true);
                } else {
                    chr.gainMeso(mapitem.getMeso(), true);
                }
                removeItem(chr, mapitem, ob);
            } else if (MapleItemInformationProvider.getInstance().isPickupBlocked(mapitem.getItemId())) {
                chr.dropMessage(5, "这个道具无法捡取。");
                c.getSession().write(MaplePacketCreator.enableActions());
            } else if (useItem(c, (int) mapitem.getItemId())) {
                chr.dropMessage(5, "捡到立即使用的道具！");
                removeItem(c.getPlayer(), mapitem, ob);
            } else if ((mapitem.getItemId() / 10000 != 291) && (MapleInventoryManipulator.checkSpace(c, (int) mapitem.getItemId(), mapitem.getItem().getQuantity(), mapitem.getItem().getOwner()))) {
                MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true);
                removeItem(chr, mapitem, ob);
            } else {
                c.getSession().write(InventoryPacket.getInventoryFull());
                c.getSession().write(InventoryPacket.getShowInventoryFull());
                c.getSession().write(MaplePacketCreator.enableActions());
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 宠物拾取道具
     * @param slea
     * @param c
     * @param chr
     */
    public static void Pickup_Pet(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) { //TODO 修复宠物捡物造成卡
        // 4F 4C 00 F6 00 A3 86 01 00
        if (chr == null) {
            return;
        }
        if ((c.getPlayer().hasBlockedInventory())) {
            return;
        }
        c.getPlayer().setScrolledPosition((short) 0);
        MaplePet pet = chr.getSpawnPet();
        Point Client_Reportedpos = slea.readPos();
        MapleMapObject ob = chr.getMap().getMapObject(slea.readInt(), MapleMapObjectType.ITEM);
        if ((ob == null) || (pet == null)) {
            return;
        }
        MapleMapItem mapitem = (MapleMapItem) ob;
        Lock lock = mapitem.getLock();
        lock.lock();
        try {
            if (mapitem.isPickedUp()) {
                c.getSession().write(InventoryPacket.getInventoryFull());
                return;
            }
            if ((mapitem.getOwner() != chr.getId()) && (mapitem.isPlayerDrop())) {
                return;
            }
            if ((mapitem.getOwner() != chr.getId()) && (((!mapitem.isPlayerDrop()) && (mapitem.getDropType() == 0)) || ((mapitem.isPlayerDrop()) && (chr.getMap().getEverlast())))) {
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            if ((!mapitem.isPlayerDrop()) && (mapitem.getDropType() == 1) && (mapitem.getOwner() != chr.getId()) && ((chr.getParty() == null) || (chr.getParty().getMemberById(mapitem.getOwner()) == null))) {
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            double Distance = Client_Reportedpos.distanceSq(mapitem.getPosition());
            if ((Distance > 10000.0D) && ((mapitem.getMeso() > 0) || (mapitem.getItemId() != 4001025))) {

                WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverMessageRedText("[GM 信息] " + chr.getName() + " ID: " + chr.getId() + " (等级 " + chr.getLevel() + ") 全屏宠吸。地图ID: " + chr.getMapId() + " 范围: " + Distance));
            } else if (pet.getPos().distanceSq(mapitem.getPosition()) > 640000.0D) {

                WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverMessageRedText("[GM 信息] " + chr.getName() + " ID: " + chr.getId() + " (等级 " + chr.getLevel() + ") 全屏宠吸。地图ID: " + chr.getMapId() + " 范围: " + Distance));
            }
            if (mapitem.getMeso() > 0) {
                if ((chr.getParty() != null) && (mapitem.getOwner() != chr.getId())) {
                    List<MapleCharacter> toGive = new LinkedList();
                    long splitMeso = mapitem.getMeso() * 40 / 100;
                    for (MaplePartyCharacter z : chr.getParty().getMembers()) {
                        MapleCharacter m = chr.getMap().getCharacterById(z.getId());
                        if ((m != null) && (m.getId() != chr.getId())) {
                            toGive.add(m);
                        }
                    }
                    for (MapleCharacter m : toGive) {
                        m.gainMeso(splitMeso / toGive.size() + (m.getStat().hasPartyBonus ? (int) (mapitem.getMeso() / 20.0D) : 0), true);
                    }
                    chr.gainMeso(mapitem.getMeso() - splitMeso, true);
                } else {
                    chr.gainMeso(mapitem.getMeso(), true);
                }
                removeItem_Pet(chr, mapitem);
            } else if ((MapleItemInformationProvider.getInstance().isPickupBlocked((int) mapitem.getItemId())) || (mapitem.getItemId() / 10000 == 291)) {
                c.getSession().write(MaplePacketCreator.enableActions());
            } else if (useItem(c, (int) mapitem.getItemId())) {
                removeItem_Pet(chr, mapitem);
            } else if (MapleInventoryManipulator.checkSpace(c, (int) mapitem.getItemId(), mapitem.getItem().getQuantity(), mapitem.getItem().getOwner())) {
                if ((mapitem.getItem().getQuantity() >= 50) && (mapitem.getItemId() == 2340000)) {
                    c.setMonitored(true);
                }
                MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true/*, mapitem.getDropper() instanceof MapleMonster*/);
                removeItem_Pet(chr, mapitem);
            }
        } finally {
            lock.unlock();
        }
    }

    public static boolean useItem(MapleClient c, int id) {
        if (ItemConstants.isUse(id)) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            MapleStatEffect eff = ii.getItemEffect(id);
            if (eff == null) {
                return false;
            }

            if (id / 10000 == 291) {
                boolean area = false;
                for (Rectangle rect : c.getPlayer().getMap().getAreas()) {
                    if (rect.contains(c.getPlayer().getTruePosition())) {
                        area = true;
                        break;
                    }
                }
                if (((c.getPlayer().getTeam() == id - 2910000) && (area))) {
                    return false;
                }
            }
            int consumeval = eff.getConsume();
            if (consumeval > 0) {
                consumeItem(c, eff);
                consumeItem(c, ii.getItemEffectEX(id));
                c.getSession().write(MaplePacketCreator.getShowItemGain(id, (short) 1));
                return true;
            }
        }
        return false;
    }

    public static void consumeItem(MapleClient c, MapleStatEffect eff) {
        if (eff == null) {
            return;
        }
        if (eff.getConsume() == 2) {
            if ((c.getPlayer().getParty() != null) && (c.getPlayer().isAlive())) {
                for (MaplePartyCharacter pc : c.getPlayer().getParty().getMembers()) {
                    MapleCharacter chr = c.getPlayer().getMap().getCharacterById(pc.getId());
                    if ((chr != null) && (chr.isAlive())) {
                        eff.applyTo(chr);
                    }
                }
            } else {
                eff.applyTo(c.getPlayer());
            }
        } else if (c.getPlayer().isAlive()) {
            eff.applyTo(c.getPlayer());
        }
    }

    public static void removeItem_Pet(MapleCharacter chr, MapleMapItem mapitem) {
        mapitem.setPickedUp(true);
        chr.getMap().broadcastMessage(InventoryPacket.removeItemFromMap(mapitem.getObjectId(), 5, chr.getId()));
        chr.getMap().removeMapObject(mapitem);
        if (mapitem.isRandDrop()) {
            chr.getMap().spawnRandDrop();
        }
    }

    private static void removeItem(MapleCharacter chr, MapleMapItem mapitem, MapleMapObject ob) {
        mapitem.setPickedUp(true);
        chr.getMap().broadcastMessage(InventoryPacket.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()), mapitem.getPosition());
        chr.getMap().removeMapObject(ob);
        if (mapitem.isRandDrop()) {
            chr.getMap().spawnRandDrop();
        }
    }

    public static void OwlMinerva(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.skip(6);
        int itemId = slea.readInt();
        MapleData data;
        MapleDataProvider dataProvider = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath", "wz") + "/String.wz"));
        data = dataProvider.getData("Mob.img");
        List<Pair<Integer, String>> mobPairList = new LinkedList<>();
        for (MapleData mobIdData : data.getChildren()) {
            mobPairList.add(new Pair<>(Integer.parseInt(mobIdData.getName()), MapleDataTool.getString(mobIdData.getChildByPath("name"), "NO-NAME")));
        }
        StringBuilder sb = new StringBuilder();
        try {
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM drop_data WHERE itemid = ? ORDER BY mobID")) {
                ps.setInt(1, itemId);
                ResultSet rs = ps.executeQuery();
                int mobId = 0;
                while (rs.next()) {
                    if (mobId != rs.getInt("mobID")) {
                        if (sb.length() > 10000) {
                            sb.append("\r\n后面还有很多搜索结果, 但已经无法显示更多");
                            break;
                        }
                        mobId = rs.getInt("mobID");
                        for (Pair<Integer, String> mobPair : mobPairList) {
                            if (mobPair.getLeft() == mobId) {
                                if (c.getPlayer().isGM()) {
                                    sb.append("\r\n怪物名称：#e").append(mobPair.getRight()).append("#n(").append(mobPair.getLeft()).append(") 爆率：#e").append(rs.getInt("chance")).append("#n");
                                } else {
                                    sb.append("\r\n怪物名称：#e").append(mobPair.getRight()).append("#n 爆率：#e").append(rs.getInt("chance") / 1000.0D).append("%#n");
                                }
                            }
                        }
                    }
                }
                rs.close();
                ps.close();
            }
        } catch (SQLException e) {
        }
        if (sb.length() > 0) {
            c.getSession().write(NPCPacket.sendNPCSay(9010000, "搜索完成, 有以下怪物爆此物品：\r\n" + sb.toString()));
        } else {
            c.getPlayer().dropMessage(1, "没有怪物爆此物品。");
        }
        /*byte slot = (byte) slea.readShort();
         int itemid = slea.readInt();
         Item toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem((short) slot);
         if ((toUse != null) && (toUse.getQuantity() > 0) && (toUse.getItemId() == itemid) && (itemid == 2310000) && (!c.getPlayer().hasBlockedInventory())) {
         int itemSearch = slea.readInt();
         List hms = c.getChannelServer().searchMerchant(itemSearch);
         if (hms.size() > 0) {
         c.getSession().write(MaplePacketCreator.getOwlSearched(itemSearch, hms));
         MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemid, 1, true, false);
         } else {
         c.getPlayer().dropMessage(1, "没有找到这个道具.");
         }
         MapleCharacterUtil.addToItemSearch(itemSearch);
         }*/
        c.getSession().write(MaplePacketCreator.enableActions());
    }

    public static void Owl(SeekableLittleEndianAccessor slea, MapleClient c) {
        if ((c.getPlayer().getMapId() >= 910000000) && (c.getPlayer().getMapId() <= 910000022)) {
            c.getSession().write(MaplePacketCreator.getOwlOpen());
        } else {
            c.getPlayer().dropMessage(5, "商店搜索器只能在自由市场使用.");
            c.getSession().write(MaplePacketCreator.enableActions());
        }
    }

    public static void OwlWarp(SeekableLittleEndianAccessor slea, MapleClient c) {
        c.getSession().write(MaplePacketCreator.enableActions());
        if ((c.getPlayer().getMapId() >= 910000000) && (c.getPlayer().getMapId() <= 910000022) && (!c.getPlayer().hasBlockedInventory())) {
            int id = slea.readInt();
            int type = slea.readByte();
            int map = slea.readInt();
            if ((map >= 910000001) && (map <= 910000022)) {
                MapleMap mapp = c.getChannelServer().getMapFactory().getMap(map);
                c.getPlayer().changeMap(mapp, mapp.getPortal(0));
                HiredMerchant merchant = null;
                List<MapleMapObject> objects;
                switch (1) {
                    case 0:
                        objects = mapp.getAllHiredMerchantsThreadsafe();
                        for (MapleMapObject ob : objects) {
                            if ((ob instanceof IMaplePlayerShop)) {
                                IMaplePlayerShop ips = (IMaplePlayerShop) ob;
                                if ((ips instanceof HiredMerchant)) {
                                    HiredMerchant merch = (HiredMerchant) ips;
                                    if (merch.getOwnerId() == id) {
                                        merchant = merch;
                                        break;
                                    }
                                }
                            }
                        }
                        break;
                    case 1:
                        objects = mapp.getAllHiredMerchantsThreadsafe();
                        for (MapleMapObject ob : objects) {
                            if ((ob instanceof IMaplePlayerShop)) {
                                IMaplePlayerShop ips = (IMaplePlayerShop) ob;
                                if ((ips instanceof HiredMerchant)) {
                                    HiredMerchant merch = (HiredMerchant) ips;
                                    if (merch.getStoreId() == id) {
                                        merchant = merch;
                                        break;
                                    }
                                }
                            }
                        }
                        break;
                    default:
                        MapleMapObject ob = mapp.getMapObject(id, MapleMapObjectType.HIRED_MERCHANT);
                        if (!(ob instanceof IMaplePlayerShop)) {
                            break;
                        }
                        IMaplePlayerShop ips = (IMaplePlayerShop) ob;
                        if (!(ips instanceof HiredMerchant)) {
                            break;
                        }
                        merchant = (HiredMerchant) ips;
                }

                if (merchant != null) {
                    if (merchant.isOwner(c.getPlayer())) {
                        merchant.setOpen(false);
                        merchant.removeAllVisitors(18, 1);
                        c.getPlayer().setPlayerShop(merchant);
                        c.getSession().write(PlayerShopPacket.getHiredMerch(c.getPlayer(), merchant, false));
                    } else if ((!merchant.isOpen()) || (!merchant.isAvailable())) {
                        c.getPlayer().dropMessage(1, "主人正在整理商店物品\r\n请稍后再度光临！");
                    } else if (merchant.getFreeSlot() == -1) {
                        c.getPlayer().dropMessage(1, "店铺已达到最大人数\r\n请稍后再度光临！");
                    } else if (merchant.isInBlackList(c.getPlayer().getName())) {
                        c.getPlayer().dropMessage(1, "你被禁止进入该店铺.");
                    } else {
                        c.getPlayer().setPlayerShop(merchant);
                        merchant.addVisitor(c.getPlayer());
                        c.getSession().write(PlayerShopPacket.getHiredMerch(c.getPlayer(), merchant, false));
                    }
                } else {
                    c.getPlayer().dropMessage(1, "主人正在整理商店物品\r\n请稍后再度光临！");
                }
            }
        }
    }

    public static void PamSong(SeekableLittleEndianAccessor slea, MapleClient c) {
        Item pam = c.getPlayer().getInventory(MapleInventoryType.CASH).findById(5640000);
        if ((slea.readByte() > 0) && (c.getPlayer().getScrolledPosition() != 0) && (pam != null) && (pam.getQuantity() > 0)) {
            MapleInventoryType inv = c.getPlayer().getScrolledPosition() < 0 ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP;
            Item item = c.getPlayer().getInventory(inv).getItem(c.getPlayer().getScrolledPosition());
            c.getPlayer().setScrolledPosition((short) 0);
            if (item != null) {
                Equip eq = (Equip) item;
                eq.setUpgradeSlots((byte) (eq.getUpgradeSlots() + 1));
                c.getPlayer().forceUpdateItem(eq);
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, pam.getPosition(), (short) 1, true, false);
                c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.pamsSongEffect(c.getPlayer().getId()));
            }
        } else {
            c.getPlayer().setScrolledPosition((short) 0);
        }
    }

    public static void TeleRock(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte slot = (byte) slea.readShort();
        int itemId = slea.readInt();
        Item toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem((short) slot);
        if ((toUse == null) || (toUse.getQuantity() < 1) || (toUse.getItemId() != itemId) || (itemId / 10000 != 232) || (c.getPlayer().hasBlockedInventory())) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        boolean used = UseTeleRock(slea, c, itemId);
        if (used) {
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
        }
        c.getSession().write(MaplePacketCreator.enableActions());
    }

    /**
     * 使用缩地石
     * @param slea
     * @param c
     * @param itemId
     * @return
     */
    public static boolean UseTeleRock(SeekableLittleEndianAccessor slea, MapleClient c, int itemId) {
        boolean used = false;
        if (slea.readByte() == 0) {
            MapleMap target = c.getChannelServer().getMapFactory().getMap(slea.readInt());
            if (((itemId == 5041000) && (c.getPlayer().isRegRockMap(target.getId()))) || ((itemId != 5041000) && (c.getPlayer().isRegRockMap(target.getId()))) || (((itemId == 5040004) || (itemId == 5041001)) && ((GameConstants.isHyperTeleMap(target.getId())))
                    && (!FieldLimitType.VipRock.check(c.getPlayer().getMap().getFieldLimit())) && (!FieldLimitType.VipRock.check(target.getFieldLimit())) && (!c.getPlayer().isInBlockedMap()))) {
                c.getPlayer().changeMap(target, target.getPortal(0));
                used = true;
            }
        } else {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(slea.readMapleAsciiString());
            if ((victim != null) && (!victim.isIntern()) && (c.getPlayer().getEventInstance() == null) && (victim.getEventInstance() == null)) {
                if ((!FieldLimitType.VipRock.check(c.getPlayer().getMap().getFieldLimit())) && (!FieldLimitType.VipRock.check(c.getChannelServer().getMapFactory().getMap(victim.getMapId()).getFieldLimit())) && (!victim.isInBlockedMap()) && (!c.getPlayer().isInBlockedMap()) && ((itemId == 5041000) || (itemId == 5040004) || (itemId == 5041001) || (victim.getMapId() / 100000000 == c.getPlayer().getMapId() / 100000000))) {
                    c.getPlayer().changeMap(victim.getMap(), victim.getMap().findClosestPortal(victim.getTruePosition()));
                    used = true;
                }
            } else {
                c.getPlayer().dropMessage(1, "在此频道未找到该玩家.");
            }
        }
        return used;
    }

    public static void UseAdditionalItem(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if ((chr == null) || (!chr.isAlive()) || (chr.getMap() == null) || (chr.hasBlockedInventory())) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        byte slot = (byte) slea.readShort();
        byte toSlot = (byte) slea.readShort();
        Item scroll = chr.getInventory(MapleInventoryType.USE).getItem((short) slot);
        Equip toScroll = (Equip) chr.getInventory(MapleInventoryType.EQUIP).getItem((short) toSlot);
        if (scroll == null || scroll.getQuantity() < 0 || toScroll == null || toScroll.getQuantity() != 1) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        int successRate = ii.getScrollSuccess(scroll.getItemId());
        boolean noCursed = ii.isNoCursedScroll(scroll.getItemId());
        if (successRate <= 0) {
            c.getPlayer().dropMessage(1, "卷轴道具: " + scroll.getItemId() + " - " + ii.getName(scroll.getItemId()) + " 成功几率为: " + successRate + " 该卷轴可能还未修复.");
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        if (chr.isShowPacket()) {
            chr.dropSpouseMessage(11, "卷轴道具: " + scroll.getItemId() + " - " + ii.getName(scroll.getItemId()) + " 成功几率为: " + successRate + "% 卷轴是否失败不消失装备: " + noCursed);
        }
        c.getSession().write(MaplePacketCreator.enableActions());
    }

    public static void UseCarvedSeal(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if ((chr == null) || (!chr.isAlive()) || (chr.getMap() == null) || (chr.hasBlockedInventory())) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        byte slot = (byte) slea.readShort();
        byte toSlot = (byte) slea.readShort();
        Item scroll = chr.getInventory(MapleInventoryType.USE).getItem((short) slot);
        Equip toScroll = (Equip) chr.getInventory(MapleInventoryType.EQUIP).getItem((short) toSlot);
        if (scroll == null || scroll.getQuantity() < 0 || toScroll == null || toScroll.getQuantity() != 1) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        int successRate = ii.getScrollSuccess(scroll.getItemId());
        if (successRate <= 0) {
            c.getPlayer().dropMessage(1, "卷轴道具: " + scroll.getItemId() + " - " + ii.getName(scroll.getItemId()) + " 成功几率为: " + successRate + " 该卷轴可能还未修复.");
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        if (chr.isShowPacket()) {
            chr.dropSpouseMessage(11, "卷轴道具: " + scroll.getItemId() + " - " + ii.getName(scroll.getItemId()) + " 成功几率为: " + successRate + "%");
        }

        c.getSession().write(MaplePacketCreator.enableActions());
    }

}
