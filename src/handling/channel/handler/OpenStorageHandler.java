package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleInventoryType;
import constants.ItemConstants;
import handling.MaplePacketHandler;
import handling.world.WorldBroadcastService;
import server.AutobanManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStorage;

import tools.MapleLogger;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.NPCPacket;

public class OpenStorageHandler extends MaplePacketHandler {


    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        byte mode = slea.readByte();
        if (chr == null) {
            return;
        }
        MapleStorage storage = chr.getStorage();
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        switch (mode) {
            case 4://取出
                byte type = slea.readByte();
                byte slot = slea.readByte();  // 从0开始计算的
                slot = storage.getSlot(MapleInventoryType.getByType(type), slot);
                Item item = storage.getItem(slot);
                if (item != null) {
                    if ((ii.isPickupRestricted(item.getItemId())) && (chr.getItemQuantity(item.getItemId(), true) > 0)) {
                        c.sendPacket(NPCPacket.getStorageError((byte) 9));
                        return;
                    }

                    long meso = (storage.getNpcId() == 9030100) || (storage.getNpcId() == 9031016) ? 1000 : 0;
                    if (chr.getMeso() < meso) {
                        c.sendPacket(NPCPacket.getStorageError((byte) 13));
                        return;
                    }

                    if (MapleInventoryManipulator.checkSpace(c, item.getItemId(), item.getQuantity(), item.getOwner())) {
                        item = storage.takeOut(slot);
                        short flag = item.getFlag();
                        if (ItemFlag.KARMA_USE.check(flag)) {
                            item.setFlag((short) (flag - ItemFlag.KARMA_USE.getValue()));
                        }
                        MapleInventoryManipulator.addFromDrop(c, item, false);
                        if (meso > 0) {
                            chr.gainMeso(-meso, false);
                        }
                        storage.sendTakenOut(c, ItemConstants.getInventoryType(item.getItemId()));
                    } else {
                        c.sendPacket(NPCPacket.getStorageError((byte) 9));
                    }
                } else {
                    MapleLogger.info("[作弊] " + chr.getName() + " (等级 " + chr.getLevel() + ") 试图从仓库取出不存在的道具.");
                    WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverMessageRedText("[GM 信息] 玩家: " + chr.getName() + " (等级 " + chr.getLevel() + ") 试图从仓库取出不存在的道具."));
                    c.sendPacket(MaplePacketCreator.enableActions());
                }
                break;
            case 5://放入仓库
                slot = (byte) slea.readShort();
                int itemId = slea.readInt();
                short quantity = slea.readShort();

                if (quantity < 1) {
                    AutobanManager.getInstance().autoban(c, "试图存入到仓库的道具数量: " + quantity + " 道具ID: " + itemId);
                    return;
                }

                if (storage.isFull()) {
                    c.sendPacket(NPCPacket.getStorageError((byte) 9));
                    return;
                }

                MapleInventoryType type1 = ItemConstants.getInventoryType(itemId);
                if (chr.getInventory(type1).getItem((short) slot) == null) {
                    c.sendPacket(MaplePacketCreator.enableActions());
                    return;
                }

                long meso = (storage.getNpcId() == 9030100) || (storage.getNpcId() == 9031016) ? 500 : 100;
                if (chr.getMeso() < meso) {
                    c.sendPacket(NPCPacket.getStorageError((byte) 13));
                    return;
                }

                item = chr.getInventory(type1).getItem((short) slot).copy();

                if (ItemConstants.isPet(item.getItemId())) {
                    c.sendPacket(MaplePacketCreator.enableActions());
                    return;
                }

                if ((ii.isPickupRestricted(item.getItemId())) && (storage.findById(item.getItemId()) != null)) {
                    c.sendPacket(MaplePacketCreator.enableActions());
                    return;
                }
                if ((item.getItemId() == itemId) && ((item.getQuantity() >= quantity) || (ItemConstants.isRechargable(itemId)))) {
                    if (ItemConstants.isRechargable(itemId)) {
                        quantity = item.getQuantity();
                    }
                    chr.gainMeso(-meso, false, false);
                    MapleInventoryManipulator.removeFromSlot(c, type1, (short) slot, quantity, false);
                    item.setQuantity(quantity);
                    storage.store(item);
                    storage.sendStored(c, ItemConstants.getInventoryType(itemId));
                } else {
                    AutobanManager.getInstance().addPoints(c, 1000, 0L, "试图存入到仓库的道具: " + itemId + " 数量: " + quantity + " 当前玩家用道具: " + item.getItemId() + " 数量: " + item.getQuantity());
                }
                break;
            case 6:
                meso = slea.readInt();
                long storageMesos = storage.getMeso();
                long playerMesos = chr.getMeso();
                if (((meso > 0) && (storageMesos >= meso)) || ((meso < 0) && (playerMesos >= -meso))) {
                    if ((meso < 0) && (storageMesos - meso < 0)) {
                        meso = -(9999999999L - storageMesos);
                        if (-meso > playerMesos) {
                            return;
                        }
                    } else if ((meso > 0) && (playerMesos + meso < 0)) {
                        meso = 9999999999L - playerMesos;
                        if (meso > storageMesos) {
                            return;
                        }
                    }
                    storage.setMeso(storageMesos - meso);
                    chr.gainMeso(meso, false, false);
                } else {
                    AutobanManager.getInstance().addPoints(c, 1000, 0L, "Trying to store or take out unavailable amount of mesos (" + meso + "/" + storage.getMeso() + "/" + c.getPlayer().getMeso() + ")");
                    return;
                }
                storage.sendMeso(c);
                break;
            case 7:
                storage.close();
                chr.setConversation(0);
                break;
            default:
                MapleLogger.info("Unhandled Storage mode : " + mode);
        }
    }
}
