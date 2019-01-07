package handling.world;

import client.MapleCharacter;
import database.DatabaseConnection;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.channel.PlayerStorage;
import handling.world.party.ExpeditionType;
import handling.world.party.MapleExpedition;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import handling.world.party.PartySearch;
import handling.world.party.PartySearchType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.packet.PartyPacket;

public class WrodlPartyService {

    private final Map<Integer, MapleParty> partyList;
    private final Map<Integer, MapleExpedition> expedsList;
    private final Map<PartySearchType, List<PartySearch>> searcheList;
    private final AtomicInteger runningPartyId;
    private final AtomicInteger runningExpedId;

    public static WrodlPartyService getInstance() {
        return SingletonHolder.instance;
    }

    private WrodlPartyService() {
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET party = -1, fatigue = 0")) {
                ps.executeUpdate();
                ps.close();
            }
        } catch (SQLException e) {
            FileoutputUtil.log("更新角色组队为-1失败");
        }
        this.runningPartyId = new AtomicInteger(1);
        this.runningExpedId = new AtomicInteger(1);
        this.partyList = new HashMap();
        this.expedsList = new HashMap();
        this.searcheList = new EnumMap(PartySearchType.class);
        for (PartySearchType pst : PartySearchType.values()) {
            this.searcheList.put(pst, new ArrayList());
        }
    }

    public void partyChat(int partyId, String chatText, String nameFrom) {
        partyChat(partyId, chatText, nameFrom, 1);
    }

    public void expedChat(int expedId, String chatText, String nameFrom) {
        MapleExpedition expedition = getExped(expedId);
        if (expedition == null) {
            return;
        }
        for (Iterator i$ = expedition.getParties().iterator(); i$.hasNext();) {
            int i = ((Integer) i$.next());
            partyChat(i, chatText, nameFrom, 4);
        }
    }

    public void sendExpedPacket(int expedId, byte[] packet, MaplePartyCharacter exception) {
        MapleExpedition expedition = getExped(expedId);
        if (expedition == null) {
            return;
        }
        for (Iterator i$ = expedition.getParties().iterator(); i$.hasNext();) {
            int i = ((Integer) i$.next());
            sendPartyPacket(i, packet, exception);
        }
    }

    public void sendPartyPacket(int partyId, byte[] packet, MaplePartyCharacter exception) {
        MapleParty party = getParty(partyId);
        if (party == null) {
            return;
        }
        for (MaplePartyCharacter partychar : party.getMembers()) {
            int ch = WorldFindService.getInstance().findChannel(partychar.getName());
            if ((ch > 0) && ((exception == null) || (partychar.getId() != exception.getId()))) {
                MapleCharacter player = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(partychar.getName());
                if (player != null) {
                    player.getClient().getSession().write(packet);
                }
            }
        }
    }

    public void partyChat(int partyId, String chatText, String nameFrom, int mode) {
        MapleParty party = getParty(partyId);
        if (party == null) {
            return;
        }
        for (MaplePartyCharacter partychar : party.getMembers()) {
            int ch = WorldFindService.getInstance().findChannel(partychar.getName());
            if (ch > 0) {
                MapleCharacter player = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(partychar.getName());
                if ((player != null) && (!player.getName().equalsIgnoreCase(nameFrom))) {
                    player.getClient().getSession().write(MaplePacketCreator.multiChat(nameFrom, chatText, mode));
                    if (player.getClient().isMonitored()) {
                        WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverMessageMega("[GM 信息] " + nameFrom + " said to " + player.getName() + " (组队): " + chatText));
                    }
                }
            }
        }
    }

    public void partyMessage(int partyId, String chatText) {
        MapleParty party = getParty(partyId);
        if (party == null) {
            return;
        }
        for (MaplePartyCharacter partychar : party.getMembers()) {
            int ch = WorldFindService.getInstance().findChannel(partychar.getName());
            if (ch > 0) {
                MapleCharacter player = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(partychar.getName());
                if (player != null) {
                    player.dropMessage(5, chatText);
                }
            }
        }
    }

    public void expedMessage(int expedId, String chatText) {
        MapleExpedition expedition = getExped(expedId);
        if (expedition == null) {
            return;
        }
        for (Iterator i$ = expedition.getParties().iterator(); i$.hasNext();) {
            int i = ((Integer) i$.next());
            partyMessage(i, chatText);
        }
    }

    public void updateParty(int partyId, PartyOperation operation, MaplePartyCharacter target) {
        MapleParty party = getParty(partyId);
        if (party == null) {
            FileoutputUtil.log("no party with the specified partyid exists.");
            return;
        }

        int oldExped = party.getExpeditionId();
        int oldIndex = -1;
        if (oldExped > 0) {
            MapleExpedition exped = getExped(oldExped);
            if (exped != null) {
                oldIndex = exped.getIndex(partyId);
            }
        }
        switch (operation) {
            case 加入队伍:
                party.addMember(target);
                if (party.getMembers().size() < 6) {
                    break;
                }
                PartySearch toRemove = getSearchByParty(partyId);
                if (toRemove != null) {
                    removeSearch(toRemove, "队伍人数已满，组队广告已被删除。");
                } else if (party.getExpeditionId() > 0) {
                    MapleExpedition exped = getExped(party.getExpeditionId());
                    if ((exped != null) && (exped.getAllMembers() >= exped.getType().maxMembers)) {
                        toRemove = getSearchByExped(exped.getId());
                        if (toRemove != null) {
                            removeSearch(toRemove, "队伍人数已满，组队广告已被删除。");
                        }
                    }
                }
                break;
            case 驱逐成员:
            case 离开队伍:
                party.removeMember(target);
                break;
            case 解散队伍:
                disbandParty(partyId);
                break;
            case 更新队伍:
            case LOG_ONOFF:
                party.updateMember(target);
                break;
            case 改变队长:
            case CHANGE_LEADER_DC:
                party.setLeader(target);
                break;
            case 更新信息:
                break;
            default:
                throw new RuntimeException("Unhandeled updateParty operation " + operation.name());
        }
        if ((operation == PartyOperation.离开队伍) || (operation == PartyOperation.驱逐成员)) {
            int chz = WorldFindService.getInstance().findChannel(target.getName());
            if (chz > 0) {
                MapleCharacter player = getStorage(chz).getCharacterByName(target.getName());
                if (player != null) {
                    player.setParty(null);
                    player.getClient().getSession().write(PartyPacket.updateParty(player.getClient().getChannel(), party, operation, target));
                }
            }
            if ((target.getId() == party.getLeader().getId()) && (party.getMembers().size() > 0)) {
                MaplePartyCharacter lchr = null;
                for (MaplePartyCharacter pchr : party.getMembers()) {
                    if ((pchr != null) && ((lchr == null) || (lchr.getLevel() < pchr.getLevel()))) {
                        lchr = pchr;
                    }
                }
                if (lchr != null) {
                    updateParty(partyId, PartyOperation.CHANGE_LEADER_DC, lchr);
                }
            }
        }
        if (party.getMembers().size() <= 0) {
            disbandParty(partyId);
        }
        for (MaplePartyCharacter partychar : party.getMembers()) {
            if (partychar == null) {
                continue;
            }
            int ch = WorldFindService.getInstance().findChannel(partychar.getName());
            if (ch > 0) {
                MapleCharacter chr = getStorage(ch).getCharacterByName(partychar.getName());
                if (chr != null) {
                    if (operation == PartyOperation.解散队伍) {
                        chr.setParty(null);
                    } else {
                        chr.setParty(party);
                    }
                    chr.getClient().getSession().write(PartyPacket.updateParty(chr.getClient().getChannel(), party, operation, target));
                }
            }
        }
    }

    public MapleParty createParty(MaplePartyCharacter chrfor, boolean 非公开组队, String 组队名称) {
        MapleParty party = new MapleParty(this.runningPartyId.getAndIncrement(), chrfor, 非公开组队, 组队名称);
        this.partyList.put(party.getId(), party);
        return party;
    }

    /*public MapleParty createParty(MaplePartyCharacter chrfor) {
     MapleParty party = new MapleParty(this.runningPartyId.getAndIncrement(), chrfor);
     this.partyList.put(party.getId(), party);
     return party;
     }*/
    public MapleParty createParty(MaplePartyCharacter chrfor, int expedId) {
        ExpeditionType ex = ExpeditionType.getById(expedId);
        MapleParty party = new MapleParty(this.runningPartyId.getAndIncrement(), chrfor, ex != null ? this.runningExpedId.getAndIncrement() : -1);
        this.partyList.put(party.getId(), party);
        if (ex != null) {
            MapleExpedition expedition = new MapleExpedition(ex, chrfor.getId(), party.getExpeditionId());
            expedition.getParties().add(party.getId());
            this.expedsList.put(party.getExpeditionId(), expedition);
        }
        return party;
    }

    public MapleParty createPartyAndAdd(MaplePartyCharacter chrfor, int expedId) {
        MapleExpedition expedition = getExped(expedId);
        if (expedition == null) {
            return null;
        }
        MapleParty party = new MapleParty(this.runningPartyId.getAndIncrement(), chrfor, expedId);
        this.partyList.put(party.getId(), party);
        expedition.getParties().add(party.getId());
        return party;
    }

    public MapleParty getParty(int partyId) {
        return (MapleParty) this.partyList.get(partyId);
    }

    public MapleExpedition getExped(int partyId) {
        return (MapleExpedition) this.expedsList.get(partyId);
    }

    public MapleExpedition disbandExped(int partyId) {
        PartySearch toRemove = getSearchByExped(partyId);
        if (toRemove != null) {
            removeSearch(toRemove, "远征队解散，组队广告已被删除。");
        }
        MapleExpedition ret = (MapleExpedition) this.expedsList.remove(Integer.valueOf(partyId));
        Iterator i$;
        if (ret != null) {
            for (i$ = ret.getParties().iterator(); i$.hasNext();) {
                int p = ((Integer) i$.next());
                MapleParty pp = getParty(p);
                if (pp != null) {
                    updateParty(p, PartyOperation.解散队伍, pp.getLeader());
                }
            }
        }
        return ret;
    }

    public MapleParty disbandParty(int partyId) {
        PartySearch toRemove = getSearchByParty(partyId);
        if (toRemove != null) {
            removeSearch(toRemove, "组队解散，组队广告已被删除。");
        }
        MapleParty ret = (MapleParty) this.partyList.remove(Integer.valueOf(partyId));
        if (ret == null) {
            return null;
        }
        ret.disband();
        return ret;
    }

    public List<PartySearch> searchParty(PartySearchType pst) {
        return (List) this.searcheList.get(pst);
    }

    public void removeSearch(PartySearch ps, String text) {
        List ss = (List) this.searcheList.get(ps.getType());
        if (ss.contains(ps)) {
            ss.remove(ps);
            ps.cancelRemoval();
            if (ps.getType().exped) {
                expedMessage(ps.getId(), text);
                sendExpedPacket(ps.getId(), PartyPacket.removePartySearch(ps), null);
            } else {
                partyMessage(ps.getId(), text);
                sendPartyPacket(ps.getId(), PartyPacket.removePartySearch(ps), null);
            }
        }
    }

    public void addSearch(PartySearch ps) {
        ((List) this.searcheList.get(ps.getType())).add(ps);
    }

    public PartySearch getSearch(MapleParty party) {
        for (List<PartySearch> ps : this.searcheList.values()) {
            for (PartySearch p : ps) {
                if (((p.getId() == party.getId()) && (!p.getType().exped)) || ((p.getId() == party.getExpeditionId()) && (p.getType().exped))) {
                    return p;
                }
            }
        }
        return null;
    }

    public PartySearch getSearchByParty(int partyId) {
        for (List<PartySearch> ps : this.searcheList.values()) {
            for (PartySearch p : ps) {
                if ((p.getId() == partyId) && (!p.getType().exped)) {
                    return p;
                }
            }
        }
        return null;
    }

    public PartySearch getSearchByExped(int partyId) {
        for (List<PartySearch> ps : this.searcheList.values()) {
            for (PartySearch p : ps) {
                if ((p.getId() == partyId) && (p.getType().exped)) {
                    return p;
                }
            }
        }
        return null;
    }

    public boolean partyListed(MapleParty party) {
        return getSearchByParty(party.getId()) != null;
    }

    public PlayerStorage getStorage(int channel) {
        if (channel == -10) {
            return CashShopServer.getPlayerStorage();
        }
        return ChannelServer.getInstance(channel).getPlayerStorage();
    }

    private static class SingletonHolder {

        protected static final WrodlPartyService instance = new WrodlPartyService();
    }
}
