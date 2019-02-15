package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.MapleLogger;
import tools.data.input.SeekableLittleEndianAccessor;

public class UseCashItemRecvVO extends MaplePacketRecvVO {

    Short slot;
    Integer itemId;
    Integer mapId;
    String charName;
    Integer apSpTo;
    Integer apSpFrom;
    String message;
    Long petUniqueId;
    String petName;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slot = slea.readShort();
        itemId = slea.readInt();
        Integer itemType = itemId / 10000;
        switch (itemType) {
            case 217: // 缩地石
                if (slea.readByte() == 0) {
                    mapId = slea.readInt();
                } else {
                    charName = slea.readMapleAsciiString();
                }
                break;
            case 218: // 洗点卷
                apSpTo = slea.readInt();
                apSpFrom = slea.readInt();
                break;
            case 208: // 喇叭
            case 213: // 真情告白
            case 209: // 地图祝福
                message = slea.readMapleAsciiString();
                break;
            case 216: // 消息
                charName = slea.readMapleAsciiString();
                message = slea.readMapleAsciiString();
                break;
            case 211: // 宠物取名卡
                petUniqueId = slea.readLong();
                petName = slea.readMapleAsciiString();
                break;
            default:
                MapleLogger.info(new StringBuilder().append("使用未处理的商城道具 : ").append(itemId).toString());
                MapleLogger.info(slea.toString(true));
                break;
        }
    }

    public Long getPetUniqueId() {
        return petUniqueId;
    }

    public String getPetName() {
        return petName;
    }

    public Short getSlot() {
        return slot;
    }

    public Integer getItemId() {
        return itemId;
    }

    public Integer getMapId() {
        return mapId;
    }

    public String getCharName() {
        return charName;
    }

    public Integer getApSpTo() {
        return apSpTo;
    }

    public Integer getApSpFrom() {
        return apSpFrom;
    }

    public String getMessage() {
        return message;
    }
}
