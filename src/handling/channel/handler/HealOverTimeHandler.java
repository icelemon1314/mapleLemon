package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.PlayerStats;
import handling.MaplePacketHandler;
import handling.vo.recv.HealOverTimeRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class HealOverTimeHandler extends MaplePacketHandler<HealOverTimeRecvVO> {


    @Override
    public void handlePacket(HealOverTimeRecvVO recvVO, MapleClient c) {
        // 2F 00 14 00 00 0A 00 00 00 00 8E 9D 5C 00
        MapleCharacter chr = c.getPlayer();
        if (chr == null) {
            return;
        }
        int healHP = recvVO.getHealHp();
        int healMP = recvVO.getHealMp();
        PlayerStats stats = chr.getStat();
        if (stats.getHp() <= 0 || healHP > 100 || healMP > 100) {
            return;
        }
        long now = System.currentTimeMillis();
        if ((healHP != 0) && (chr.canHP(now + 1000L))) {
//            if (healHP > stats.getHealHP()) {
//                healHP = (int) stats.getHealHP();
//            }
            chr.addHP(healHP);
        }
        if ((healMP != 0) && (chr.canMP(now + 1000L))) {
            // @TODO 限制每次恢复的MP
//            if (healMP > stats.getHealMP()) {
//                healMP = (int) stats.getHealMP();
//            }
            chr.addMP(healMP);
        }
    }
}
