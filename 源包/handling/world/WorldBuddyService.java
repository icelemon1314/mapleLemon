package handling.world;

import client.BuddyList;
import client.BuddylistEntry;
import client.MapleCharacter;
import handling.channel.ChannelServer;
import tools.MaplePacketCreator;
import tools.packet.BuddyListPacket;

public class WorldBuddyService {

    public static WorldBuddyService getInstance() {
        return SingletonHolder.instance;
    }

    public void buddyChat(int[] recipientCharacterIds, int chrIdFrom, String nameFrom, String chatText) {
        for (int characterId : recipientCharacterIds) {
            int ch = WorldFindService.getInstance().findChannel(characterId);
            if (ch > 0) {
                MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(characterId);
                if ((chr != null) && (chr.getBuddylist().containsVisible(chrIdFrom))) {
                    chr.getClient().getSession().write(MaplePacketCreator.multiChat(nameFrom, chatText, 0));
                    if (chr.getClient().isMonitored()) {
                        WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverMessageNotice("[GM 信息] " + nameFrom + " said to " + chr.getName() + " (好友): " + chatText));
                    }
                }
            }
        }
    }

    private void updateBuddies(int characterId, int channel, int[] buddies, boolean offline, String name) {
        for (int buddy : buddies) {
            int ch = WorldFindService.getInstance().findChannel(buddy);
            if (ch > 0) {
                MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(buddy);
                if (chr != null) {
                    BuddylistEntry ble = chr.getBuddylist().get(characterId);
                    if ((ble == null) || (!ble.isVisible())) {
                        continue;
                    }
                    int mcChannel;
                    if (offline) {
                        ble.setChannel(-1);
                        mcChannel = -1;
                    } else {
                        ble.setChannel(channel);
                        mcChannel = channel - 1;
                    }
                    chr.getClient().getSession().write(BuddyListPacket.updateBuddyChannel(ble.getCharacterId(), mcChannel, chr.getClient().getAccID()));
                }
            }
        }
    }

    public void buddyChanged(int chrId, int chrIdFrom, String name, int channel, BuddyList.BuddyOperation operation, String group) {
        int ch = WorldFindService.getInstance().findChannel(chrId);
        if (ch > 0) {
            MapleCharacter addChar = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(chrId);
            if (addChar != null) {
                BuddyList buddylist = addChar.getBuddylist();
                switch (operation) {
                    case 添加好友:
                        if (!buddylist.contains(chrIdFrom)) {
                            break;
                        }
                        buddylist.put(new BuddylistEntry(name, chrIdFrom, group, channel, true));
                        addChar.getClient().getSession().write(BuddyListPacket.updateBuddyChannel(chrIdFrom, channel - 1, addChar.getClient().getAccID()));
                        break;
                    case 删除好友:
                        if (!buddylist.contains(chrIdFrom)) {
                            break;
                        }
                        buddylist.put(new BuddylistEntry(name, chrIdFrom, group, -1, buddylist.get(chrIdFrom).isVisible()));
                        addChar.getClient().getSession().write(BuddyListPacket.updateBuddyChannel(chrIdFrom, -1, addChar.getClient().getAccID()));
                }
            }
        }
    }

    public BuddyList.BuddyAddResult requestBuddyAdd(String addName, int channelFrom, int chrIdFrom, String nameFrom, int levelFrom, int jobFrom) {
        int ch = WorldFindService.getInstance().findChannel(chrIdFrom);
        if (ch > 0) {
            MapleCharacter addChar = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(addName);
            if (addChar != null) {
                BuddyList buddylist = addChar.getBuddylist();
                if (buddylist.isFull()) {
                    return BuddyList.BuddyAddResult.好友列表已满;
                }
                if (!buddylist.contains(chrIdFrom)) {
                    buddylist.addBuddyRequest(addChar.getClient(), chrIdFrom, nameFrom, channelFrom, levelFrom, jobFrom, addChar.getAccountID());
                } else if (buddylist.containsVisible(chrIdFrom)) {
                    return BuddyList.BuddyAddResult.已经是好友关系;
                }
            }
        }

        return BuddyList.BuddyAddResult.添加好友成功;
    }

    public void loggedOn(String name, int chrId, int channel, int[] buddies) {
        updateBuddies(chrId, channel, buddies, false, name);
    }

    public void loggedOff(String name, int chrId, int channel, int[] buddies) {
        updateBuddies(chrId, channel, buddies, true, name);
    }

    private static class SingletonHolder {

        protected static final WorldBuddyService instance = new WorldBuddyService();
    }
}
