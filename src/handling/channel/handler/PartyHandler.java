package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import constants.GameConstants;
import handling.channel.ChannelServer;
import handling.world.PartyOperation;
import handling.world.World;
import handling.world.WorldFindService;
import handling.world.WorldSidekickService;
import handling.world.WrodlPartyService;
import handling.world.party.ExpeditionType;
import handling.world.party.MapleExpedition;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import handling.world.party.PartySearch;
import handling.world.party.PartySearchType;
import handling.world.sidekick.MapleSidekick;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import server.ServerProperties;
import server.maps.FieldLimitType;
import server.quest.MapleQuest;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.StringUtil;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.PartyPacket;

public class PartyHandler {

    public static void DenyPartyRequest(SeekableLittleEndianAccessor slea, MapleClient c) {
        WrodlPartyService partyService = WrodlPartyService.getInstance();
        if ((c.getPlayer().getParty() == null) && (c.getPlayer().getQuestNoAdd(MapleQuest.getInstance(122901)) == null)) {
            int action = slea.readByte();
            int partyId = slea.readInt();
            MapleParty party = partyService.getParty(partyId);
            if (party != null) {
                if (party.getExpeditionId() > 0) {
                    c.getPlayer().dropMessage(5, "加入远征队伍的状态下无法进行此操作。");
                    return;
                }
                switch (action) {
                    case 0x1F:
                    case 0x20://0x21-1 119ok
                        break;
                    case 0x24://0x23+1 119ok
                    case 0x3A:
                        MapleCharacter cfrom = c.getChannelServer().getPlayerStorage().getCharacterById(party.getLeader().getId());
                        if (cfrom == null) {
                            break;
                        }
                        cfrom.dropMessage(5, new StringBuilder().append("'").append(c.getPlayer().getName()).append("'玩家拒绝了组队招待。").toString());
                        break;
                    case 0x25://0x24+1 119ok
                    case 0x3B:
                        if (party.getMembers().size() < 6) {
                            c.getPlayer().setParty(party);
                            partyService.updateParty(partyId, PartyOperation.加入队伍, new MaplePartyCharacter(c.getPlayer()));
                            c.getPlayer().receivePartyMemberHP();
                            c.getPlayer().updatePartyMemberHP();
                        } else {
                            c.getPlayer().dropMessage(5, "组队成员已满");
                        }
                        break;
                    default:
                        FileoutputUtil.log(new StringBuilder().append("第二方收到组队邀请处理( 0x").append(StringUtil.getLeftPaddedStr(Integer.toHexString(action).toUpperCase(), '0', 2)).append(" ) 未知.").toString());
                }
            } else {
                c.getPlayer().dropMessage(5, "要参加的队伍不存在。");
            }
        } else {
            int action = slea.readByte();
            int charId = slea.readInt();
            MapleCharacter cfrom;
            MapleParty party = partyService.getParty(c.getPlayer().getParty().getId());
            switch (action) {
                case 0x25:
                    c.getPlayer().dropMessage(5, "您已经有一个组队，无法加入其他组队!");
                    break;
                case 0x42:
                    break;
                case 0x48:
                    cfrom = c.getChannelServer().getPlayerStorage().getCharacterById(charId);
                    if (cfrom == null) {
                        break;
                    }
                    cfrom.dropMessage(5, new StringBuilder().append(c.getPlayer().getName()).append("拒绝了组队加入申请。").toString());
                    break;
                case 0x49:
                    cfrom = c.getChannelServer().getPlayerStorage().getCharacterById(charId);
                    if (cfrom == null) {
                        break;
                    }
                    if (party.getMembers().size() < 6) {
                        cfrom.setParty(party);
                        partyService.updateParty(party.getId(), PartyOperation.加入队伍, new MaplePartyCharacter(cfrom));
                        cfrom.receivePartyMemberHP();
                        cfrom.updatePartyMemberHP();
                    } else {
                        c.getPlayer().dropMessage(5, "组队成员已满。");
                        cfrom.dropMessage(5, "组队成员已满。");
                    }
                    break;
                default:
                    FileoutputUtil.log(new StringBuilder().append("第二方收到申请加入组队处理( 0x").append(StringUtil.getLeftPaddedStr(Integer.toHexString(action).toUpperCase(), '0', 2)).append(" ) 未知.").toString());
            }
        }
    }

    public static void PartyOperation(SeekableLittleEndianAccessor slea, MapleClient c) {
        int operation = slea.readByte();
        MapleParty party = c.getPlayer().getParty();
        WrodlPartyService partyService = WrodlPartyService.getInstance();
        MaplePartyCharacter partyPlayer = new MaplePartyCharacter(c.getPlayer());
        switch (operation) {
            case 1://创建组队
                if (party == null) {
                    boolean 非公开组队 = slea.readByte() == 1;
                    String 组队名称 = slea.readMapleAsciiString();
                    party = partyService.createParty(partyPlayer, 非公开组队, 组队名称);
                    c.getPlayer().setParty(party);
                    c.getSession().write(PartyPacket.partyCreated(party));
                } else {
                    if (party.getExpeditionId() > 0) {
                        c.getPlayer().dropMessage(5, "加入远征队伍的状态下无法进行此操作。");
                        return;
                    }
                    if ((partyPlayer.equals(party.getLeader())) && (party.getMembers().size() == 1)) {
                        c.getSession().write(PartyPacket.partyCreated(party));
                    } else {
                        c.getPlayer().dropMessage(5, "你已经存在一个队伍中，无法创建！");
                    }
                }
                break;
            case 2:
                if (party == null) {
                    break;
                }
                if (party.getExpeditionId() > 0) {
                    c.getPlayer().dropMessage(5, "加入远征队伍的状态下无法进行此操作。");
                    return;
                }
                if (partyPlayer.equals(party.getLeader())) {
                    partyService.updateParty(party.getId(), PartyOperation.解散队伍, partyPlayer);
                    if (c.getPlayer().getEventInstance() != null) {
                        c.getPlayer().getEventInstance().disbandParty();
                    }
                } else {
                    if (c.getPlayer().getEventInstance() != null) {
                        c.getPlayer().getEventInstance().leftParty(c.getPlayer());
                    }
                    partyService.updateParty(party.getId(), PartyOperation.离开队伍, partyPlayer);
                }
                c.getPlayer().setParty(null);
                break;
            case 3:
                int partyid = slea.readInt();
                if (party == null) {
                    party = partyService.getParty(partyid);
                    if (party != null) {
                        if (party.getExpeditionId() > 0) {
                            c.getPlayer().dropMessage(5, "加入远征队伍的状态下无法进行此操作。");
                            return;
                        }
                        if ((party.getMembers().size() < 6) && (c.getPlayer().getQuestNoAdd(MapleQuest.getInstance(122901)) == null)) {
                            c.getPlayer().setParty(party);
                            partyService.updateParty(party.getId(), PartyOperation.加入队伍, partyPlayer);
                            c.getPlayer().receivePartyMemberHP();
                            c.getPlayer().updatePartyMemberHP();
                        } else {
                            c.getPlayer().dropMessage(5, "组队成员已满");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "要加入的队伍不存在");
                    }
                } else {
                    c.getPlayer().dropMessage(5, "您已经有一个组队，无法加入其他组队!");
                }
                break;
            case 4:
                if (party == null) {
                    party = partyService.createParty(partyPlayer, false, c.getPlayer().getName() + "的组队");
                    c.getPlayer().setParty(party);
                    c.getSession().write(PartyPacket.partyCreated(party));
                }
                String theName = slea.readMapleAsciiString();
                int theCh = WorldFindService.getInstance().findChannel(theName);
                if (theCh > 0) {
                    MapleCharacter invited = ChannelServer.getInstance(theCh).getPlayerStorage().getCharacterByName(theName);
                    if (invited != null) {
                        if (party.getExpeditionId() > 0) {
                            c.getPlayer().dropMessage(5, "加入远征队伍的状态下无法进行此操作。");
                        } else if (invited.getParty() != null) {
                            c.getPlayer().dropMessage(5, new StringBuilder().append("'").append(theName).append("'已经加入其他组。").toString());
                        } else if (invited.getQuestNoAdd(MapleQuest.getInstance(122901)) != null) {
                            c.getPlayer().dropMessage(5, new StringBuilder().append("'").append(theName).append("'玩家处于拒绝组队状态。").toString());
                        } else if (party.getMembers().size() < 6) {
                            c.getSession().write(PartyPacket.partyStatusMessage(0x20, invited.getName()));//0x1F+1 119ok
                            invited.getClient().getSession().write(PartyPacket.partyInvite(c.getPlayer()));
                        } else {
                            c.getPlayer().dropMessage(5, "组队成员已满");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, new StringBuilder().append("在当前服务器找不到..'").append(theName).append("'。").toString());
                    }
                } else {
                    c.getPlayer().dropMessage(5, new StringBuilder().append("在当前服务器找不到..'").append(theName).append("'。").toString());
                }
                break;
            case 6:
                if ((party == null) || (!partyPlayer.equals(party.getLeader()))) {
                    break;
                }
                if (party.getExpeditionId() > 0) {
                    c.getPlayer().dropMessage(5, "加入远征队伍的状态下无法进行此操作。");
                    return;
                }
                MaplePartyCharacter expelled = party.getMemberById(slea.readInt());
                if (expelled != null) {
                    partyService.updateParty(party.getId(), PartyOperation.驱逐成员, expelled);
                    if ((c.getPlayer().getEventInstance() != null)
                            && (expelled.isOnline())) {
                        c.getPlayer().getEventInstance().disbandParty();
                    }
                }

                break;
            case 7:
                if (party == null) {
                    break;
                }
                if (party.getExpeditionId() > 0) {
                    c.getPlayer().dropMessage(5, "加入远征队伍的状态下无法进行此操作。");
                    return;
                }
                MaplePartyCharacter newleader = party.getMemberById(slea.readInt());
                if ((newleader != null) && (partyPlayer.equals(party.getLeader()))) {
                    partyService.updateParty(party.getId(), PartyOperation.改变队长, newleader);
                }
                break;
            case 8:
                if (party != null) {
                    if ((c.getPlayer().getEventInstance() != null) || (party.getExpeditionId() > 0)) {
                        c.getPlayer().dropMessage(5, "加入远征队伍的状态下无法进行此操作。");
                        return;
                    }
                    if (partyPlayer.equals(party.getLeader())) {
                        partyService.updateParty(party.getId(), PartyOperation.解散队伍, partyPlayer);
                    } else {
                        partyService.updateParty(party.getId(), PartyOperation.离开队伍, partyPlayer);
                    }
                    c.getPlayer().setParty(null);
                }
                int toPartyId = slea.readInt();
                if (party == null) {
                    party = partyService.getParty(toPartyId);
                    if ((party != null) && (party.getMembers().size() < 6)) {
                        if (party.getExpeditionId() > 0) {
                            c.getPlayer().dropMessage(5, "加入远征队伍的状态下无法进行此操作。");
                            return;
                        }
                        MapleCharacter cfrom = c.getPlayer().getMap().getCharacterById(party.getLeader().getId());
                        if ((cfrom != null) && (cfrom.getQuestNoAdd(MapleQuest.getInstance(122900)) == null)) {
                            c.getSession().write(PartyPacket.partyStatusMessage(0x42, c.getPlayer().getName()));//0x41+1 119ok
                            cfrom.getClient().getSession().write(PartyPacket.partyRequestInvite(c.getPlayer()));
                        } else {
                            c.getPlayer().dropMessage(5, "没有在该地图找此队伍的队长.");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "要加入的队伍不存在或者人数已满");
                    }
                } else {
                    c.getPlayer().dropMessage(5, "无法找到组队。请重新确认组队信息。");
                }
                break;
            case 9:
                if (slea.readByte() > 0) {
                    c.getPlayer().getQuestRemove(MapleQuest.getInstance(122900));
                } else {
                    c.getPlayer().getQuestNAdd(MapleQuest.getInstance(122900));
                }
                break;
            case 13:
                if (party == null) {
                    break;
                }
                boolean 非公开组队 = slea.readByte() == 1;
                String 组队名称 = slea.readMapleAsciiString();
                c.getPlayer().getParty().setName(组队名称);
                c.getPlayer().getParty().set非公开组队(非公开组队);
                partyService.updateParty(c.getPlayer().getParty().getId(), PartyOperation.更新信息, partyPlayer);
                break;
            case 5:
            default:
                if (!ServerProperties.ShowPacket()) {
                    break;
                }
                FileoutputUtil.log(new StringBuilder().append("组队邀请处理( ").append(operation).append(" ) 未知.").toString());
        }
    }

    public static void AllowPartyInvite(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (slea.readByte() > 0) {
            c.getPlayer().getQuestRemove(MapleQuest.getInstance(122901));
        } else {
            c.getPlayer().getQuestNAdd(MapleQuest.getInstance(122901));
        }
    }

    public static void DenySidekickRequest(SeekableLittleEndianAccessor slea, MapleClient c) {
        int action = slea.readByte();
        int cid = slea.readInt();
        if ((c.getPlayer().getSidekick() == null) && (action == 90)) {
            MapleCharacter party = c.getPlayer().getMap().getCharacterById(cid);
            if (party != null) {
                if ((party.getSidekick() != null) || (!MapleSidekick.checkLevels(c.getPlayer().getLevel(), party.getLevel()))) {
                    return;
                }
                int sid = WorldSidekickService.getInstance().createSidekick(c.getPlayer().getId(), party.getId());
                if (sid <= 0) {
                    c.getPlayer().dropMessage(5, "Please try again.");
                } else {
                    MapleSidekick s = WorldSidekickService.getInstance().getSidekick(sid);
                    c.getPlayer().setSidekick(s);
                    c.getSession().write(PartyPacket.updateSidekick(c.getPlayer(), s, true));
                    party.setSidekick(s);
                    party.getClient().getSession().write(PartyPacket.updateSidekick(party, s, true));
                }
            } else {
                c.getPlayer().dropMessage(5, "The sidekick you are trying to join does not exist");
            }
        }
    }

    public static void SidekickOperation(SeekableLittleEndianAccessor slea, MapleClient c) {
        int operation = slea.readByte();
        switch (operation) {
            case 65:
                if (c.getPlayer().getSidekick() != null) {
                    break;
                }
                MapleCharacter other = c.getPlayer().getMap().getCharacterByName(slea.readMapleAsciiString());
                if ((other.getSidekick() == null) && (MapleSidekick.checkLevels(c.getPlayer().getLevel(), other.getLevel()))) {
                    other.getClient().getSession().write(PartyPacket.sidekickInvite(c.getPlayer()));
                    c.getPlayer().dropMessage(1, new StringBuilder().append("You have sent the sidekick invite to ").append(other.getName()).append(".").toString());
                }
                break;
            case 63:
                if (c.getPlayer().getSidekick() == null) {
                    break;
                }
                c.getPlayer().getSidekick().eraseToDB();
        }
    }

    public static void MemberSearch(SeekableLittleEndianAccessor slea, MapleClient c) {
        if ((c.getPlayer().isInBlockedMap()) || (FieldLimitType.VipRock.check(c.getPlayer().getMap().getFieldLimit()))) {
            c.getPlayer().dropMessage(5, "无法在这个地方进行搜索。");
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        List members = new ArrayList();
        for (MapleCharacter chr : c.getPlayer().getMap().getCharactersThreadsafe()) {
            if (chr.getId() != c.getPlayer().getId() && chr.getParty() == null && chr.getGMLevel() <= c.getPlayer().getGMLevel()) {
                members.add(chr);
            }
        }
        c.getSession().write(PartyPacket.showMemberSearch(members));
    }

    public static void PartySearch(SeekableLittleEndianAccessor slea, MapleClient c) {
        if ((c.getPlayer().isInBlockedMap()) || (FieldLimitType.VipRock.check(c.getPlayer().getMap().getFieldLimit()))) {
            c.getPlayer().dropMessage(5, "无法在这个地方进行搜索。");
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        List parties = new ArrayList();
        for (MapleCharacter chr : c.getPlayer().getMap().getCharactersThreadsafe()) {
            if (chr.getParty() != null && !parties.contains(chr.getParty()) && chr.getParty().is非公开组队()) {
                if (c.getPlayer().getParty() != null && chr.getParty().getId() == c.getPlayer().getParty().getId()) {
                    continue;
                }
                parties.add(chr.getParty());
            }
        }
        c.getSession().write(PartyPacket.showPartySearch(parties));
    }

}
