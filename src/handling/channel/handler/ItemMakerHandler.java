package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleInventoryType;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.maps.MapleExtractor;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class ItemMakerHandler {
    public static void UseRecipe(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if ((chr == null) || (!chr.isAlive()) || (chr.getMap() == null) || (chr.hasBlockedInventory())) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        byte slot = (byte) slea.readShort();
        int itemId = slea.readInt();
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem((short) slot);
        if ((toUse == null) || (toUse.getQuantity() < 1) || (toUse.getItemId() != itemId) || (itemId / 10000 != 251)) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        if (MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId()).applyTo(chr)) {
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
        }
    }

    public static void MakeExtractor(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if ((chr == null) || (!chr.isAlive()) || (chr.getMap() == null) || (chr.hasBlockedInventory())) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        int itemId = slea.readInt();
        if (itemId > 0) {
            int fee = slea.readInt();
            Item toUse = chr.getInventory(MapleInventoryType.SETUP).findById(itemId);
            if ((toUse == null) || (toUse.getQuantity() < 1) || (itemId / 10000 != 304) || (fee <= 0) || (chr.getExtractor() != null) || (!chr.getMap().isTown())) {
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            chr.setExtractor(new MapleExtractor(chr, itemId, fee, chr.getFH()));
            chr.getMap().spawnExtractor(chr.getExtractor());
        } else {
            chr.removeExtractor();
        }
    }
}
