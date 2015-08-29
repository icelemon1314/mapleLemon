package handling.world;

import client.MapleCharacter;
import handling.channel.ChannelServer;
import java.util.Iterator;
import java.util.List;

public class WorldBroadcastService {

    public static WorldBroadcastService getInstance() {
        return SingletonHolder.instance;
    }

    public void broadcastSmega(byte[] message) {
        for (ChannelServer cs : ChannelServer.getAllInstances()) {
            cs.broadcastSmega(message);
        }
    }

    public void broadcastGMMessage(byte[] message) {
        for (ChannelServer cs : ChannelServer.getAllInstances()) {
            cs.broadcastGMMessage(message);
        }
    }

    public void broadcastMessage(byte[] message) {
        for (ChannelServer cs : ChannelServer.getAllInstances()) {
            cs.broadcastMessage(message);
        }
    }

    public void startMapEffect(String msg, int itemId) {
        for (ChannelServer cs : ChannelServer.getAllInstances()) {
            cs.startMapEffect(msg, itemId);
        }
    }

    public void startMapEffect(String msg, int itemId, int time) {
        for (ChannelServer cs : ChannelServer.getAllInstances()) {
            cs.startMapEffect(msg, itemId, time);
        }
    }

    public void sendPacket(List<Integer> targetIds, byte[] packet, int exception) {
        for (Iterator i$ = targetIds.iterator(); i$.hasNext();) {
            int i = ((Integer) i$.next());
            if (i == exception) {
                continue;
            }
            int ch = WorldFindService.getInstance().findChannel(i);
            if (ch < 0) {
                continue;
            }
            MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(i);
            if (chr != null) {
                chr.getClient().getSession().write(packet);
            }
        }
    }

    public void sendPacket(int targetId, byte[] packet) {
        int ch = WorldFindService.getInstance().findChannel(targetId);
        if (ch < 0) {
            return;
        }
        MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(targetId);
        if (chr != null) {
            chr.getClient().getSession().write(packet);
        }
    }

    public void sendGuildPacket(int targetIds, byte[] packet, int exception, int guildid) {
        if (targetIds == exception) {
            return;
        }
        int ch = WorldFindService.getInstance().findChannel(targetIds);
        if (ch < 0) {
            return;
        }
        MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(targetIds);
        if ((chr != null) && (chr.getGuildId() == guildid)) {
            chr.getClient().getSession().write(packet);
        }
    }

    private static class SingletonHolder {

        protected static final WorldBroadcastService instance = new WorldBroadcastService();
    }
}
