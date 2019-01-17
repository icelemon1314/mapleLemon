package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import constants.ItemConstants;
import handling.MaplePacketHandler;
import server.shop.MapleShop;
import tools.data.input.SeekableLittleEndianAccessor;

public class OpenNpcShopHandler extends MaplePacketHandler {


    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        // 25 00 00 00 60 4A 0F 00 01 00
        // 25 00 01 00 80 84 1E 00 01 00
        // 25 03
        MapleCharacter chr = c.getPlayer();
        byte bmode = slea.readByte();
        if (chr == null) {
            return;
        }
        switch (bmode) {
            case 0://购买
                MapleShop shop = chr.getShop();
                if (shop == null) {
                    return;
                }
                short position = slea.readShort();
                int itemId = slea.readInt();
                short quantity = slea.readShort();
                shop.buy(c, itemId, quantity, position);
                break;
            case 1://出售
                shop = chr.getShop();
                if (shop == null) {
                    return;
                }
                byte slot = (byte) slea.readShort();
                itemId = slea.readInt();
                quantity = slea.readShort();
                shop.sell(c, ItemConstants.getInventoryType(itemId), slot, quantity);
                break;
            case 2://充值
                shop = chr.getShop();
                if (shop == null) {
                    return;
                }
                slot = (byte) slea.readShort();
                shop.recharge(c, slot);
                break;
            default:
                chr.setConversation(0);break;
        }
    }
}
