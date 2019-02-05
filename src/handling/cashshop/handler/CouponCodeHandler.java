package handling.cashshop.handler;

import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import constants.ItemConstants;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import handling.MaplePacketHandler;
import handling.vo.recv.CouponCodeRecvVO;
import server.MapleInventoryManipulator;
import server.cashshop.CashItemFactory;
import server.cashshop.CashItemInfo;

import tools.MapleLogger;
import tools.Triple;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.MTSCSPacket;

public class CouponCodeHandler extends MaplePacketHandler<CouponCodeRecvVO> {
    // 商城优惠券
    public void handlePacket(CouponCodeRecvVO recvMsg, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        String toPlayer = recvMsg.getToPlayer();
        if (toPlayer.length() > 0) {
            c.sendPacket(MTSCSPacket.商城错误提示(22));
            c.sendPacket(MTSCSPacket.刷新点券信息(chr));
            return;
        }
        String code = recvMsg.getCode();
        if (code.length() <= 0) {
            c.sendPacket(MTSCSPacket.刷新点券信息(chr));
            return;
        }
        Triple info = null;
        try {
            info = MapleCharacterUtil.getNXCodeInfo(code);
        } catch (SQLException e) {
            MapleLogger.info("错误 getNXCodeInfo" + e);
        }
        if ((info != null) && (((Boolean) info.left))) {
            int type = ((Integer) info.mid);
            int item = ((Integer) info.right);
            try {
                MapleCharacterUtil.setNXCodeUsed(chr.getName(), code);
            } catch (SQLException e) {
                MapleLogger.info("错误 setNXCodeUsed" + e);
                c.sendPacket(MTSCSPacket.商城错误提示(0));
                return;
            }

            Map itemz = new HashMap();
            int maplePoints = 0;
            int mesos = 0;
            switch (type) {
                case 1:
                case 2:
                    c.getPlayer().modifyCSPoints(type, item, false);
                    maplePoints = item;
                    break;
                case 3:
                    CashItemInfo itez = CashItemFactory.getInstance().getItem(item);
                    if (itez == null) {
                        c.sendPacket(MTSCSPacket.商城错误提示(0));
                        return;
                    }
                    byte slot = MapleInventoryManipulator.addId(c, itez.getId(), (byte) 1, "", "商城道具卡兑换 时间: " + System.currentTimeMillis());
                    if (slot <= -1) {
                        c.sendPacket(MTSCSPacket.商城错误提示(0));
                        return;
                    }
                    itemz.put(item, chr.getInventory(ItemConstants.getInventoryType(item)).getItem((short) slot));

                    break;
                case 4:
                    chr.gainMeso(item, false);
                    mesos = item;
            }

            c.sendPacket(MTSCSPacket.showCouponRedeemedItem(itemz, mesos, maplePoints, c));
            c.sendPacket(MTSCSPacket.刷新点券信息(chr));
        } else {
            c.sendPacket(MTSCSPacket.商城错误提示(info == null ? 14 : 16));
        }
    }
}
