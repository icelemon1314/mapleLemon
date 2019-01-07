package handling.world;

import client.MapleClient;
import constants.ServerConstants;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.channel.PlayerStorage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import server.shops.HiredMerchant;
import tools.CollectionUtil;

public class World {

    public static void init() {
        WorldFindService.getInstance();
        WorldBroadcastService.getInstance();
        WrodlPartyService.getInstance();
        WorldSidekickService.getInstance();
        WorldBuddyService.getInstance();
        WorldGuildService.getInstance();
        WorldMessengerService.getInstance();

    }

    public static String getStatus() {
        StringBuilder ret = new StringBuilder();
        int totalUsers = 0;
        for (ChannelServer cs : ChannelServer.getAllInstances()) {
            ret.append("频道 ");
            ret.append(cs.getChannel());
            ret.append(": ");
            int channelUsers = cs.getConnectedClients();
            totalUsers += channelUsers;
            ret.append(channelUsers);
            ret.append(" 玩家\n");
        }
        ret.append("总计在线: ");
        ret.append(totalUsers);
        ret.append("\n");
        return ret.toString();
    }

    public static Map<Integer, Integer> getConnected() {
        Map ret = new LinkedHashMap();
        int total = 0;
        for (ChannelServer ch : ChannelServer.getAllInstances()) {
            int chOnline = ch.getConnectedClients();
            ret.put(ch.getChannel(), chOnline);
            total += chOnline;
        }
        int csOnline = CashShopServer.getConnectedClients();
        ret.put(-10, csOnline);
        total += csOnline;

        ret.put(0, total);
        return ret;
    }

    public static List<CheaterData> getCheaters() {
        List allCheaters = new ArrayList();
        Collections.sort(allCheaters);
        return CollectionUtil.copyFirst(allCheaters, 20);
    }

    public static List<CheaterData> getReports() {
        List allCheaters = new ArrayList();
        Collections.sort(allCheaters);
        return CollectionUtil.copyFirst(allCheaters, 20);
    }

    public static boolean isConnected(String charName) {
        return WorldFindService.getInstance().findChannel(charName) > 0;
    }

    public static void toggleMegaphoneMuteState() {
        for (ChannelServer cs : ChannelServer.getAllInstances()) {
            cs.toggleMegaphoneMuteState();
        }
    }

    public static void ChannelChange_Data(CharacterTransfer Data, int characterid, int toChannel) {
        getStorage(toChannel).registerPendingPlayer(Data, characterid);
    }

    public static boolean isCharacterListConnected(List<String> charName) {
        for (ChannelServer cs : ChannelServer.getAllInstances()) {
            for (String c : charName) {
                if (cs.getPlayerStorage().getCharacterByName(c) != null) {
                    return true;
                }
            }
        }
        ChannelServer cs;
        return false;
    }

    public static boolean hasMerchant(int accountID) {
        for (ChannelServer cs : ChannelServer.getAllInstances()) {
            if (cs.containsMerchant(accountID)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasMerchant(int accountID, int characterID) {
        for (ChannelServer cs : ChannelServer.getAllInstances()) {
            if (cs.containsMerchant(accountID, characterID)) {
                return true;
            }
        }
        return false;
    }

    public static HiredMerchant getMerchant(int accountID, int characterID) {
        for (ChannelServer cs : ChannelServer.getAllInstances()) {
            if (cs.containsMerchant(accountID, characterID)) {
                return cs.getHiredMerchants(accountID, characterID);
            }
        }
        return null;
    }

    public static PlayerStorage getStorage(int channel) {
        if (channel == -10) {
            return CashShopServer.getPlayerStorage();
        }
        return ChannelServer.getInstance(channel).getPlayerStorage();
    }

    public static int getPendingCharacterSize() {
        int ret = CashShopServer.getPlayerStorage().pendingCharacterSize(); /*+ AuctionServer.getPlayerStorage().pendingCharacterSize()*///拍卖注释掉
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            ret += cserv.getPlayerStorage().pendingCharacterSize();
        }
        return ret;
    }

    public static boolean isChannelAvailable(int ch) {
        if ((ChannelServer.getInstance(ch) == null) || (ChannelServer.getInstance(ch).getPlayerStorage() == null)) {
            return false;
        }
        return ChannelServer.getInstance(ch).getPlayerStorage().getConnectedClients() < (ch == 1 ? 600 : 400);
    }

    public static class Client {

        private static final ArrayList<MapleClient> clients = new ArrayList();

        public static void addClient(MapleClient c) {
            if (!clients.contains(c)) {
                clients.add(c);
            }
        }

        public static boolean removeClient(MapleClient c) {
            return clients.remove(c);
        }

        public static ArrayList<MapleClient> getClients() {
            return clients;
        }
    }
}
