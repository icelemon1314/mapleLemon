package tools.packet;

import client.MapleCharacter;
import handling.SendPacketOpcode;
import handling.world.PartyOperation;
import handling.world.WrodlPartyService;
import handling.world.party.MapleExpedition;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import handling.world.party.PartySearch;
import handling.world.party.PartySearchType;
import handling.world.sidekick.MapleSidekick;
import handling.world.sidekick.MapleSidekickCharacter;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import server.ServerProperties;
import tools.data.output.MaplePacketLittleEndianWriter;

public class PartyPacket {

    private static final Logger log = Logger.getLogger(PartyPacket.class);

    public static byte[] partyCreated(MapleParty party) {//创建组队
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(0x07);//0x0E+1 119ok
        for (int i = 0; i < 5; i++) {
            if (i >= 0 && i <= 2)
                mplew.writeInt(1);
            else if (i >= 3 && i <= 4)
                mplew.writeShort(1);
        }

        return mplew.getPacket();
    }

    public static byte[] partyInvite(MapleCharacter from) {//组队邀请
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(0x4);//0x5是随机邀请
        mplew.writeInt(from.getParty() == null ? 0 : from.getParty().getId());
        mplew.writeMapleAsciiString(from.getName());

        return mplew.getPacket();
    }

    public static byte[] partyRequestInvite(MapleCharacter from) {//邀请反馈
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(0x08);//0x08+0 119ok
        mplew.writeInt(from.getId());
        mplew.writeMapleAsciiString(from.getName());
        mplew.writeInt(from.getLevel());
        mplew.writeInt(from.getJob());
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] partyStatusMessage(int message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(message);

        return mplew.getPacket();
    }

    public static byte[] partyStatusMessage(int message, String charName) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(message);
        mplew.writeMapleAsciiString(charName);

        return mplew.getPacket();
    }

    public static byte[] partyStatusMessage(String message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SPOUSE_MESSAGE.getValue());
        mplew.writeShort(11);
        mplew.writeMapleAsciiString(message);

        return mplew.getPacket();
    }

    private static void addPartyStatus(int forchannel, MapleParty party, MaplePacketLittleEndianWriter mplew, boolean leaving) {
        addPartyStatus(forchannel, party, mplew, leaving, false);
    }

    private static void addPartyStatus(int forchannel, MapleParty party, MaplePacketLittleEndianWriter mplew, boolean leaving, boolean exped) {
        List<MaplePartyCharacter> partymembers;
        if (party == null) {
            partymembers = new ArrayList();
        } else {
            partymembers = new ArrayList(party.getMembers());
        }
        while (partymembers.size() < 6) {
            partymembers.add(new MaplePartyCharacter());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            mplew.writeInt(partychar.getId());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            mplew.writeAsciiString(partychar.getName(), 0x13);
        }

        for (MaplePartyCharacter partychar : partymembers) {
            if (partychar.isOnline()) {
                // This handles the Map IDs, but in v40 beta if you're
                // not on the same map, the player appears offline. We
                // have to eventually find a workaround for this.
                mplew.writeInt(partychar.getMapid());
            } else {
                mplew.writeInt(-2);
            }
        }
        mplew.writeInt(party == null ? 0 : party.getLeader().getId());
        for (MaplePartyCharacter partychar : partymembers) {
            if (partychar.isOnline()) {
                mplew.writeInt(partychar.getChannel() - 1);
            } else {
                mplew.writeInt(-2);
            }
        }

        for (MaplePartyCharacter partychar : partymembers) {
            if ((partychar.getChannel() == forchannel) && (!leaving)) {
                mplew.writeInt(partychar.getDoorTown());
                mplew.writeInt(partychar.getDoorTarget());
                mplew.writeInt(partychar.getDoorPosition().x);
                mplew.writeInt(partychar.getDoorPosition().y);
            } else {
                mplew.writeInt(leaving ? 999999999 : 0);
                mplew.writeInt(leaving ? 999999999 : 0);
                mplew.writeInt(leaving ? -1 : 0);
                mplew.writeInt(leaving ? -1 : 0);
            }
        }
    }

    public static byte[] updateParty(int forChannel, MapleParty party, PartyOperation op, MaplePartyCharacter target) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PARTY_OPERATION.getValue());
        switch (op) {
            case 离开队伍:
            case 驱逐成员:
            case 解散队伍:
                mplew.write(0xB);//0x13+1 119ok
                mplew.writeInt(party.getId());
                mplew.writeInt(target.getId());
                if (op == PartyOperation.解散队伍) {
                    mplew.write(0);
                } else {
                    mplew.write(1);
                    mplew.write(op == PartyOperation.驱逐成员 ? 1 : 0);
                    mplew.writeMapleAsciiString(target.getName());
                    addPartyStatus(forChannel, party, mplew, op == PartyOperation.离开队伍);
                }
                break;
            case 加入队伍:
                mplew.write(0xE);//0x16+1 119ok
                mplew.writeInt(party.getId());
                mplew.writeMapleAsciiString(target.getName());
                addPartyStatus(forChannel, party, mplew, false);
                break;
            case 更新队伍:
            case LOG_ONOFF:
                mplew.write(op == PartyOperation.LOG_ONOFF ? 0x38 : 0x0E);//0x37+1|0x0D+1 119ok
                mplew.writeInt(party.getId());
                addPartyStatus(forChannel, party, mplew, op == PartyOperation.LOG_ONOFF);
                break;
            case 更新信息:
                mplew.write(0x4C);//new 119ok
                mplew.writeBool(party.is非公开组队());
                mplew.writeMapleAsciiString(new String[]{party.getName(), null, null, null});
                break;
            case 改变队长:
            case CHANGE_LEADER_DC:
                mplew.write(0x2F);//0x2E+1 119ok
                mplew.writeInt(target.getId());
                mplew.write(op == PartyOperation.CHANGE_LEADER_DC ? 1 : 0);
        }

        return mplew.getPacket();
    }

    public static byte[] partyPortal(int townId, int targetId, int skillId, Point position, boolean animation) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.writeShort(0x1A);
        mplew.writeInt(townId);
        mplew.writeInt(targetId);
        mplew.writePos(position);

        return mplew.getPacket();
    }

    public static byte[] updatePartyMemberHP(int chrId, int curhp, int maxhp) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.UPDATE_PARTYMEMBER_HP.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(curhp);
        mplew.writeInt(maxhp);

        return mplew.getPacket();
    }

    public static byte[] getPartyListing(PartySearchType pst) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(108);
        mplew.writeInt(pst.id);
        List<PartySearch> parties = WrodlPartyService.getInstance().searchParty(pst);
        mplew.writeInt(parties.size());
        for (PartySearch party : parties) {
            if (pst.exped) {
                MapleExpedition me = WrodlPartyService.getInstance().getExped(party.getId());
                mplew.writeInt(party.getId());
                mplew.writeAsciiString(party.getName(), 37);

                mplew.writeInt(pst.id);
                mplew.writeInt(0);
                for (int i = 0; i < 5; i++) {
                    if (i < me.getParties().size()) {
                        MapleParty part = WrodlPartyService.getInstance().getParty((me.getParties().get(i)).intValue());
                        if (part != null) {
                            addPartyStatus(-1, part, mplew, false, true);
                        } else {
                            mplew.writeZero(226);
                        }
                    } else {
                        mplew.writeZero(226);
                    }
                }
            } else {
                mplew.writeInt(party.getId());
                mplew.writeAsciiString(party.getName(), 37);
                addPartyStatus(-1, WrodlPartyService.getInstance().getParty(party.getId()), mplew, false, true);
            }
        }

        return mplew.getPacket();
    }

    public static byte[] partyListingAdded(PartySearch ps) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(106);
        mplew.writeInt(ps.getType().id);
        if (ps.getType().exped) {
            MapleExpedition me = WrodlPartyService.getInstance().getExped(ps.getId());

            mplew.writeInt(ps.getId());
            mplew.writeAsciiString(ps.getName(), 37);
            mplew.writeInt(ps.getType().id);
            mplew.writeInt(0);
            for (int i = 0; i < 5; i++) {
                if (i < me.getParties().size()) {
                    MapleParty party = WrodlPartyService.getInstance().getParty((me.getParties().get(i)).intValue());
                    if (party != null) {
                        addPartyStatus(-1, party, mplew, false, true);
                    } else {
                        mplew.writeZero(226);
                    }
                } else {
                    mplew.writeZero(226);
                }
            }
        } else {
            mplew.writeInt(ps.getId());
            mplew.writeAsciiString(ps.getName(), 37);
            addPartyStatus(-1, WrodlPartyService.getInstance().getParty(ps.getId()), mplew, false, true);
        }

        return mplew.getPacket();
    }

    public static byte[] removePartySearch(PartySearch ps) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(107);
        mplew.writeInt(ps.getType().id);
        mplew.writeInt(ps.getId());
        mplew.writeInt(2);

        return mplew.getPacket();
    }

    public static byte[] showMemberSearch(List<MapleCharacter> players) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.MEMBER_SEARCH.getValue());
        mplew.write(players.size());
        for (MapleCharacter chr : players) {
            mplew.writeInt(chr.getId());
            mplew.writeMapleAsciiString(chr.getName());
            mplew.writeInt(chr.getJob());
            mplew.write(chr.getLevel());
        }

        return mplew.getPacket();
    }

    public static byte[] showPartySearch(List<MapleParty> partylist) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.PARTY_SEARCH.getValue());
        mplew.write(partylist.size());
        for (MapleParty party : partylist) {
            mplew.writeInt(party.getId());
            mplew.writeMapleAsciiString(party.getLeader().getName());
            mplew.write(party.getLeader().getLevel());
            mplew.write(party.getLeader().isOnline() ? 1 : 0);
            mplew.writeMapleAsciiString(new String[]{party.getName(), null, null, null});
            mplew.write(party.getMembers().size());
            for (MaplePartyCharacter partyChr : party.getMembers()) {
                mplew.writeInt(partyChr.getId());
                mplew.writeMapleAsciiString(partyChr.getName());
                mplew.writeInt(partyChr.getJobId());
                mplew.write(partyChr.getLevel());
                mplew.write(partyChr.isOnline() ? 1 : 0);
            }
        }

        return mplew.getPacket();
    }

    public static byte[] sidekickInvite(MapleCharacter from) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SIDEKICK_OPERATION.getValue());
        mplew.write(65);
        mplew.writeInt(from.getId());
        mplew.writeMapleAsciiString(from.getName());
        mplew.writeInt(from.getLevel());
        mplew.writeInt(from.getJob());
        mplew.writeInt(0);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] disbandSidekick(MapleSidekick s) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SIDEKICK_OPERATION.getValue());
        mplew.write(75);
        mplew.writeInt(s.getId());
        mplew.writeInt(s.getCharacter(0).getId());
        mplew.write(0);
        mplew.writeInt(s.getCharacter(1).getId());

        return mplew.getPacket();
    }

    public static byte[] updateSidekick(MapleCharacter first, MapleSidekick s, boolean f) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SIDEKICK_OPERATION.getValue());
        mplew.write(f ? 0x4E : 0x46);
        MapleSidekickCharacter second = s.getCharacter(s.getCharacter(0).getId() == first.getId() ? 1 : 0);
        boolean online = first.getMap().getCharacterById(second.getId()) != null;
        mplew.writeInt(s.getId());
        if (f) {
            mplew.writeMapleAsciiString(second.getName());
        }
        List<String> msg = s.getSidekickMsg(online);
        mplew.writeInt(msg.size());
        for (String m : msg) {
            mplew.writeMapleAsciiString(m);
        }
        mplew.writeInt(first.getId());
        mplew.writeInt(second.getId());
        mplew.writeAsciiString(first.getName(), 13);
        mplew.writeAsciiString(second.getName(), 13);
        mplew.writeInt(first.getJob());
        mplew.writeInt(second.getJobId());
        mplew.writeInt(first.getLevel());
        mplew.writeInt(second.getLevel());
        mplew.writeInt(first.getClient().getChannel() - 1);
        mplew.writeInt(online ? first.getClient().getChannel() - 1 : 0);
        mplew.writeLong(0L);
        mplew.writeInt(first.getId());
        if (f) {
            mplew.writeInt(first.getId());
        }
        mplew.writeInt(second.getId());
        if (!f) {
            mplew.writeInt(first.getId());
        }
        mplew.writeInt(first.getMapId());
        mplew.writeInt(online ? first.getMapId() : 999999999);
        mplew.writeInt(1);
        mplew.write(Math.abs(first.getLevel() - second.getLevel()));
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(2147483647);
        mplew.writeInt(1);

        return mplew.getPacket();
    }
}
