package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.messages.CommandProcessor;
import client.messages.CommandType;
import handling.MaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class GeneralChatHandler extends MaplePacketHandler {


    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        String text = slea.readMapleAsciiString();
        MapleCharacter chr = c.getPlayer();
        int unk = 0;
        if ((text.length() > 0) && (chr != null) && (chr.getMap() != null)) {
            if (!CommandProcessor.processCommand(c, text,  CommandType.NORMAL)) {
                if ((!chr.isIntern()) && (text.length() >= 80)) {
                    return;
                }
                if ((chr.getCanTalk()) || (chr.isStaff())) {
                    if (chr.isHidden()) {
                        if ((chr.isIntern()) && (!chr.isSuperGM()) && (unk == 0)) {
                            chr.getMap().broadcastGMMessage(chr, MaplePacketCreator.getChatText(chr.getId(), text, false, 1), true);
//                            if (unk == 0) {
//                                chr.getMap().broadcastGMMessage(chr, MaplePacketCreator.serverNotice(2, new StringBuilder().append(chr.getName()).append(" : ").append(text).toString()), true);
//                            }
                        } else {
                            chr.getMap().broadcastGMMessage(chr, MaplePacketCreator.getChatText(chr.getId(), text, c.getPlayer().isSuperGM(), unk), true);
                        }
                    } else {
                        if ((chr.isIntern()) && (!chr.isSuperGM()) && (unk == 0)) {
                            chr.getMap().broadcastMessage(MaplePacketCreator.getChatText(chr.getId(), text, false, 1), c.getPlayer().getTruePosition());
//                            if (unk == 0) {
//                                chr.getMap().broadcastMessage(MaplePacketCreator.serverNotice(2, new StringBuilder().append(chr.getName()).append(" : ").append(text).toString()), c.getPlayer().getTruePosition());
//                            }
                        } else {
                            chr.getMap().broadcastMessage(MaplePacketCreator.getChatText(chr.getId(), text, c.getPlayer().isSuperGM(), unk), c.getPlayer().getTruePosition());
                        }
                    }
                } else {
                    c.sendPacket(MaplePacketCreator.serverMessagePopUp("你被禁言了，所以暂时还不能聊天！"));
                }
            }
        }
    }
}
