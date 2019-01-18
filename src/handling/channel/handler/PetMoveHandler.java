package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.MaplePet;
import handling.MaplePacketHandler;

import tools.MapleLogger;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.PetPacket;

import java.awt.*;
import java.util.List;

public class PetMoveHandler extends MaplePacketHandler {


    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        // 4C
        // 0F 00 B3 00
        // 06 00 08 00 B3 00 83 FF 02 00 15 00 03 3C 00 00 07 00 B4 00 83 FF FE FF 15 00 03 0A 00 00 F6 FF B3 00 84 FF FE FF 14 00 03 8C 00 00 EE FF B3 00 CC FF FF FF 14 00 19 5A 00 00 EC FF B3 00 FC FF 00 00 14 00 19 3C 00 00 EC FF B3 00 00 00 00 00 14 00 19 96 00 00
        MapleCharacter chr = c.getPlayer();
        MaplePet pet = chr.getSpawnPet();
        if (pet == null) {
            return;
        }
        Point startPos = slea.readPos();
        List res = MovementParse.parseMovement(slea, 3);
        if ((res != null) && (chr != null) && (!res.isEmpty()) && (chr.getMap() != null)) {
            if (slea.available() != 1) {
                MapleLogger.info("slea.available != 1 (宠物移动出错) 剩余封包长度: " + slea.available());
                return;
            }
            chr.getSpawnPet().updatePosition(res);
//            chr.getClient().sendPacket(PetPacket.movePet(chr.getId(),1,startPos,res));
            chr.getMap().broadcastMessage(chr, PetPacket.movePet(chr.getId(), (byte)1, startPos, res), false);
        }
    }
}
