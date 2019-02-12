package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import constants.ItemConstants;
import handling.MaplePacketHandler;
import handling.vo.recv.OpenNpcShopRecvVO;
import server.shop.MapleShop;
import tools.data.input.SeekableLittleEndianAccessor;

public class OpenNpcShopHandler extends MaplePacketHandler<OpenNpcShopRecvVO> {


    @Override
    public void handlePacket(OpenNpcShopRecvVO recvVO, MapleClient c) {
        // 25 00 00 00 60 4A 0F 00 01 00
        // 25 00 01 00 80 84 1E 00 01 00
        // 25 03
        MapleCharacter chr = c.getPlayer();
        byte bmode = recvVO.getType();
        if (chr == null) {
            return;
        }
        switch (bmode) {
            case 0://购买
                MapleShop shop = chr.getShop();
                if (shop == null) {
                    return;
                }
                short slot = recvVO.getSlot();
                int itemId = recvVO.getItemId();
                short quantity = recvVO.getQuantity();
                shop.buy(c, itemId, quantity, slot);
                break;
            case 1://出售
                shop = chr.getShop();
                if (shop == null) {
                    return;
                }
                slot = recvVO.getSlot();
                itemId = recvVO.getItemId();
                quantity = recvVO.getQuantity();
                shop.sell(c, ItemConstants.getInventoryType(itemId), slot, quantity);
                break;
            case 2://充值
                shop = chr.getShop();
                if (shop == null) {
                    return;
                }
                slot = recvVO.getSlot();
                shop.recharge(c, slot);
                break;
            default:
                chr.setConversation(0);break;
        }
    }
}
