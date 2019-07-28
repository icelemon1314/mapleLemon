package handling.channel.handler;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import handling.MaplePacketHandler;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.vo.recv.EnterCashShopRecvVO;
import handling.world.CharacterTransfer;
import handling.world.PlayerBuffStorage;
import handling.world.World;
import handling.world.WorldMessengerService;
import handling.world.messenger.MapleMessengerCharacter;
import server.ManagerSin;

import tools.MapleLogger;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

import javax.swing.table.DefaultTableModel;

public class EnterCashShopHandler extends MaplePacketHandler<EnterCashShopRecvVO> {

    @Override
    public void handlePacket(EnterCashShopRecvVO recvVO, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if ((chr == null) || (chr.hasBlockedInventory()) || (chr.getMap() == null) || (chr.getEventInstance() != null) || (c.getChannelServer() == null)) {
//            c.sendPacket(MaplePacketCreator.serverBlocked(2));
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        if ((!chr.isAlive())) {
            String msg = "无法进入商城，请稍后再试。";
            if (!chr.isAlive()) {
                msg = "现在不能进入商城.";
            }
            c.getPlayer().dropMessage(1, msg);
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        if (World.getPendingCharacterSize() >= 10) {
            chr.dropMessage(1, "服务器忙，请稍后在试。");
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        ChannelServer ch = ChannelServer.getInstance(c.getChannel());
        chr.changeRemoval();
        if (chr.getBuffedValue(MapleBuffStat.召唤兽) != null) {
            chr.cancelEffectFromBuffStat(MapleBuffStat.召唤兽, -1);
        }
        PlayerBuffStorage.addBuffsToStorage(chr.getId(), chr.getAllBuffs());
        PlayerBuffStorage.addCooldownsToStorage(chr.getId(), chr.getCooldowns());
//        PlayerBuffStorage.addDiseaseToStorage(chr.getId(), chr.getAllDiseases());
        //chr.cancelAllBuffs();
        World.ChannelChange_Data(chr, -10);
        ch.removePlayer(chr);
        c.updateLoginState(MapleClient.CHANGE_CHANNEL, c.getSessionIPAddress());
        chr.saveToDB(false, false);
        if (chr.getMessenger() != null) {
            MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(chr);
            WorldMessengerService.getInstance().leaveMessenger(chr.getMessenger().getId(), messengerplayer);
        }
        chr.getMap().removePlayer(chr);
        c.sendPacket(MaplePacketCreator.getChannelChange(c, Integer.parseInt(CashShopServer.getIP().split(":")[1])));
//        CharacterTransfer transfer = CashShopServer.getPlayerStorage().getPendingCharacter(chr.getId());
//        CashShopOperation.EnterCS(transfer, c);
        c.setPlayer(null);
        c.setReceiving(false);

        try {
            int countRows = ManagerSin.jTable1.getRowCount();//获取当前表格总行数
            for (int i = 0; i < countRows; i++) {
                String sname = ManagerSin.jTable1.getValueAt(i, 1).toString();
                if (sname.equals(chr.getName())) {
                    ((DefaultTableModel) ManagerSin.jTable1.getModel()).setValueAt("现金商城", i, 4);
                    break;
                }
            }
        } catch (Exception e) {
            MapleLogger.error("gui error:", e);
        }
    }
}
