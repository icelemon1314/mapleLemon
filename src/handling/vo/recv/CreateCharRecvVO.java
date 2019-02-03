package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.MapleLogger;
import tools.data.input.SeekableLittleEndianAccessor;

public class CreateCharRecvVO extends MaplePacketRecvVO {

    String charName;
    Integer face;
    Integer hair;
    Integer top;
    Integer bottom;
    Integer shoes;
    Integer weapon;
    Byte[] stat;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        charName = slea.readMapleAsciiString();
        face = slea.readInt();
        hair = slea.readInt();
        top = slea.readInt();
        bottom = slea.readInt();
        shoes = slea.readInt();
        weapon = slea.readInt();

        stat = new Byte[4];
        int totalStat = 0;
        for (int i = 0; i < 4; i++) {
            stat[i] = slea.readByte();
            if (stat[i] < 4 || stat[i] > 13) {
                MapleLogger.info("[作弊] 創建角色初始能力值過小或過大");
                return;
            }
            totalStat += stat[i];
        }
        if (totalStat != 25) {
            MapleLogger.info("[作弊] 創建角色初始總能力值不正確");
            return;
        }
    }

    public String getCharName() {
        return charName;
    }

    public Integer getFace() {
        return face;
    }

    public Integer getHair() {
        return hair;
    }

    public Integer getTop() {
        return top;
    }

    public Integer getBottom() {
        return bottom;
    }

    public Integer getShoes() {
        return shoes;
    }

    public Integer getWeapon() {
        return weapon;
    }

    public Byte[] getStat() {
        return stat;
    }
}
