package server.maps;

import client.MapleClient;
import tools.MaplePacketCreator;
import tools.packet.MTSCSPacket;

public class MapleMapEffect {

    private String msg = "";
    private int itemId = 0;
    private int effectType = -1;
    private boolean active = true;
    private boolean jukebox = false;

    public MapleMapEffect(String msg, int itemId) {
        this.msg = msg;
        this.itemId = itemId;
        this.effectType = -1;
    }

    public MapleMapEffect(String msg, int itemId, int effectType) {
        this.msg = msg;
        this.itemId = itemId;
        this.effectType = effectType;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setJukebox(boolean actie) {
        this.jukebox = actie;
    }

    public boolean isJukebox() {
        return this.jukebox;
    }

    public byte[] makeDestroyData() {
        //return MaplePacketCreator.stopFullScreenBless();
        return this.jukebox ? MTSCSPacket.playCashSong(0, "") : MaplePacketCreator.stopFullScreenBless();
    }

    public void sendStartData(MapleClient c) {

        c.getSession().write(makeStartData());
    }

    public byte[] makeStartData() {
        //return MaplePacketCreator.startFullScreenBless(this.itemId,this.msg);
        return this.jukebox ? MTSCSPacket.playCashSong(this.itemId, this.msg) : MaplePacketCreator.startFullScreenBless(this.itemId,this.msg);
    }
}
