package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class NpcTalkMoreRecvVO extends MaplePacketRecvVO {

    Byte lastMsgType;
    Byte action;
    Integer selection;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        // @TODO make sure for struct
        lastMsgType = slea.readByte();
        action = slea.readByte();
        selection = -1;
        if (lastMsgType == 3) { // 数字框
            if (slea.available() >= 4L) {
                selection = slea.readInt();
            } else if (slea.available() > 0L) {
                selection = (int)slea.readByte();
            }
        } else {
            if (slea.available() >= 4L) {
                selection = slea.readInt();
            } else if (slea.available() > 0L) {
                selection = (int)slea.readByte();
            }
        }
    }

    public Byte getLastMsgType() {
        return lastMsgType;
    }

    public Byte getAction() {
        return action;
    }

    public Integer getSelection() {
        return selection;
    }
}
