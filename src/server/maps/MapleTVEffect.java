package server.maps;

import client.MapleCharacter;
import client.MapleClient;
import handling.world.WorldBroadcastService;
import java.util.LinkedList;
import java.util.List;
import server.Timer.WorldTimer;
import tools.MaplePacketCreator;

public class MapleTVEffect {

    private List<String> message = new LinkedList();
    private final MapleCharacter user;
    private static boolean active;
    private final int type;
    private MapleCharacter partner = null;
    MapleClient c;

    public MapleTVEffect(MapleCharacter User, MapleCharacter Partner, List<String> Msg, int Type) {
        this.message = Msg;
        this.user = User;
        this.type = Type;
        this.partner = Partner;
    }

    public static boolean isActive() {
        return active;
    }

    public void stratMapleTV() {
        broadCastTV(true);
    }

    private void setActive(boolean set) {
        active = set;
    }

    private void broadCastTV(boolean isActive) {
        setActive(isActive);
        if (isActive) {
            int delay = getDelayTime(this.type);
            WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.enableTV());
            WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.sendTV(this.user, this.message, this.type <= 2 ? this.type : this.type - 3, this.partner, delay));

            WorldTimer.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    MapleTVEffect.this.broadCastTV(false);
                }
            }, delay * 1000);
        } else {
            WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.removeTV());
        }
    }

    public static int getDelayTime(int type) {
        switch (type) {
            case 0:
            case 3:
                return 15;
            case 1:
            case 4:
                return 30;
            case 2:
            case 5:
                return 60;
        }
        return 0;
    }
}
