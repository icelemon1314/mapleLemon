package handling.login.handler;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import handling.MaplePacketHandler;
import handling.cashshop.CashShopServer;
import handling.cashshop.handler.CashShopOperation;
import handling.channel.ChannelServer;
import handling.login.LoginServer;
import handling.world.*;
import handling.world.guild.MapleGuild;
import handling.world.messenger.MapleMessenger;
import handling.world.messenger.MapleMessengerCharacter;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import server.ManagerSin;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.Triple;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.GuildPacket;
import tools.packet.PartyPacket;

import javax.swing.table.DefaultTableModel;

public class PlayerEnterGameHandler extends MaplePacketHandler {

    /**
     * 登录游戏
     *
     * @param c 客户端连接
     */
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        try {
            // c.setWorld(slea.readInt());
            int playerid = slea.readInt();
            CharacterTransfer transfer = CashShopServer.getPlayerStorage().getPendingCharacter(playerid);
            if (transfer != null) {
                CashShopOperation.EnterCS(transfer, c);
                return;
            }
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                transfer = cserv.getPlayerStorage().getPendingCharacter(playerid);
                if (transfer != null) {
                    c.setChannel(cserv.getChannel());
                    break;
                }
            }
            MapleCharacter player;
            if (transfer == null) {
                Triple ip = LoginServer.getLoginAuth(playerid);
                String s = c.getSessionIPAddress();
                if ((ip == null) || (!s.substring(s.indexOf(47) + 1, s.length()).equals(ip.left))) {
                    if (ip != null) {
                        LoginServer.putLoginAuth(playerid, (String) ip.left, (String) ip.mid, (Integer) ip.right);
                    }
                    c.getSession().close();
                    return;
                }
                c.setTempIP((String) ip.mid);
                c.setChannel((Integer) ip.right);
                player = MapleCharacter.loadCharFromDB(playerid, c, true);
            } else {
                player = MapleCharacter.ReconstructChr(transfer, c, true);
            }
            ChannelServer channelServer = c.getChannelServer();
            c.setPlayer(player);
            c.setAccID(player.getAccountID());
            if (!c.CheckIPAddress()) {
                c.getSession().close();
                FileoutputUtil.log(new StringBuilder().append("检测连接地址 - 2 ").append(!c.CheckIPAddress()).toString());
                return;
            }
            int state = c.getLoginState();
            boolean allowLogin = false;
            if ((state == 1) || (state == 3) || (state == 0)) {
                allowLogin = !World.isCharacterListConnected(c.loadCharacterNames(c.getWorld()));
            }
            if (!allowLogin) {
                c.setPlayer(null);
                c.getSession().close();
                FileoutputUtil.log(new StringBuilder().append("检测连接地址 - 3 ").append(!allowLogin).toString());
                return;
            }
            c.updateLoginState(MapleClient.LOGIN_LOGGEDIN, c.getSessionIPAddress());
            channelServer.addPlayer(player);
            player.setlogintime(System.currentTimeMillis());
            player.giveCoolDowns(PlayerBuffStorage.getCooldownsFromStorage(player.getId()));
            player.silentGiveBuffs(PlayerBuffStorage.getBuffsFromStorage(player.getId()));
            player.giveSilentDebuff(PlayerBuffStorage.getDiseaseFromStorage(player.getId()));
//            c.sendPacket(MaplePacketCreator.cancelTitleEffect(new int[]{-1, -1, -1, -1, -1}));
            c.sendPacket(MaplePacketCreator.getCharInfo(player));
//            c.sendPacket(MTSCSPacket.enableCSUse(0));
//            c.sendPacket(MaplePacketCreator.sendloginSuccess());
            player.getMap().addPlayer(player);
            if (player.getMount() != null) {
                //c.sendPacket(MaplePacketCreator.updateMount(player, false));
            }
            //c.sendPacket(MaplePacketCreator.getKeymap(player));
            //c.sendPacket(MaplePacketCreator.getQuickSlot(player.getQuickSlot()));
//            player.updatePetAuto();
//            player.sendMacros();
            //c.sendPacket(MaplePacketCreator.showCharCash(player));
            //c.sendPacket(MaplePacketCreator.reportResponse(0, 0));
            //c.sendPacket(MaplePacketCreator.enableReport());
            //c.sendPacket(MaplePacketCreator.temporaryStats_Reset());
//            c.sendPacket(MaplePacketCreator.enableActions());
            int[] buddyIds = player.getBuddylist().getBuddyIds();
            WorldBuddyService.getInstance().loggedOn(player.getName(), player.getId(), c.getChannel(), buddyIds);
            //c.sendPacket(MaplePacketCreator.pendantSlot((stat != null) && (stat.getCustomData() != null) && (Long.parseLong(stat.getCustomData()) > System.currentTimeMillis())));
            //c.sendPacket(InventoryPacket.updateInventory());
            MapleParty party = player.getParty();
            if (party != null) {
                WrodlPartyService.getInstance().updateParty(party.getId(), PartyOperation.LOG_ONOFF, new MaplePartyCharacter(player));
            }
            if (player.getSidekick() == null) {
                player.setSidekick(WorldSidekickService.getInstance().getSidekickByChr(player.getId()));
            }
            if (player.getSidekick() != null) {
                c.sendPacket(PartyPacket.updateSidekick(player, player.getSidekick(), false));
            }
            CharacterIdChannelPair[] onlineBuddies = WorldFindService.getInstance().multiBuddyFind(player.getId(), buddyIds);
            for (CharacterIdChannelPair onlineBuddy : onlineBuddies) {
                player.getBuddylist().get(onlineBuddy.getCharacterId()).setChannel(onlineBuddy.getChannel());
            }
            //c.sendPacket(BuddyListPacket.updateBuddylist(player.getBuddylist().getBuddies(), player.getId()));
            //c.sendPacket(BuddyListPacket.updateBuddylist(0x1F));
            MapleMessenger messenger = player.getMessenger();
            if (messenger != null) {
                WorldMessengerService.getInstance().silentJoinMessenger(messenger.getId(), new MapleMessengerCharacter(player));
                WorldMessengerService.getInstance().updateMessenger(messenger.getId(), player.getName(), c.getChannel());
            }
            if (player.getGuildId() > 0) {
                WorldGuildService.getInstance().setGuildMemberOnline(player.getMGC(), true, c.getChannel());
                c.sendPacket(GuildPacket.showGuildInfo(player));
                MapleGuild gs = WorldGuildService.getInstance().getGuild(player.getGuildId());
                if (gs != null) {
                    ///  List<byte[]> packetList = World.Alliance.getAllianceInfo(gs.getAllianceId(), true);

                } else {
                    player.setGuildId(0);
                    player.setGuildRank((byte) 5);
                    player.setAllianceRank((byte) 5);
                    player.saveGuildStatus();
                }
            }
            player.getClient().sendPacket(MaplePacketCreator.serverMessageTop("欢迎来到怀旧冒×岛，希望你能找到儿时的感觉，查看可用命令@help 如有bug可以加QQ群：479357604！dev by:icelemon1314"));
//            player.showNote();
           // player.sendImp();
           // player.updatePartyMemberHP();
//            player.startFairySchedule(false);
//            player.baseSkills();
//            player.expirationTask();
//            player.spawnSavedPets();
//            if (player.getStat().equippedSummon > 0) { // 装备召唤？
//                Skill skill = SkillFactory.getSkill(player.getStat().equippedSummon);
//                if (skill != null) {
//                    skill.getEffect(1).applyTo(player);
//                }
//            }
//           if (player.isIntern()) {
//                SkillFactory.getSkill(9001004).getEffect(1).applyTo(player);
//                SkillFactory.getSkill(1010).getEffect(1).applyTo(player);
//            }
        } catch (NumberFormatException e) {
            FileoutputUtil.outputFileError(FileoutputUtil.Login_Error, e);
        }
    }

    /**
     * 更换频道
     * @param slea
     * @param c
     * @param chr
     */
    public static void ChangeChannel(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
//        if ((chr == null) || (chr.hasBlockedInventory()) || (chr.getEventInstance() != null) || (chr.getMap() == null) || (chr.isInBlockedMap()) || (FieldLimitType.ChannelSwitch.check(chr.getMap().getFieldLimit()))) {
//            chr.dropMessage(5, "FieldLimitType！");
//            c.sendPacket(MaplePacketCreator.enableActions());
//            return;
//        }
        if (World.getPendingCharacterSize() >= 10) {
            chr.dropMessage(1, "服务器忙，请稍后在试。");
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        int chc = slea.readByte() + 1;
        if (!World.isChannelAvailable(chc)) {
            chr.dropMessage(1, "该频道玩家已满，请切换到其它频道进行游戏。");
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        chr.changeChannel(chc);
    }
}
