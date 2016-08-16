package handling.channel.handler;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.Skill;
import client.SkillFactory;
import handling.cashshop.CashShopServer;
import handling.cashshop.handler.CashShopOperation;
import handling.channel.ChannelServer;
import handling.login.LoginServer;
import handling.world.CharacterIdChannelPair;
import handling.world.CharacterTransfer;
import handling.world.PartyOperation;
import handling.world.PlayerBuffStorage;
import handling.world.World;
import handling.world.WorldBuddyService;
import handling.world.WorldFindService;
import handling.world.WorldGuildService;
import handling.world.WorldMessengerService;
import handling.world.WorldSidekickService;
import handling.world.WrodlPartyService;
import handling.world.guild.MapleGuild;
import handling.world.messenger.MapleMessenger;
import handling.world.messenger.MapleMessengerCharacter;
import handling.world.party.MapleExpedition;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.Logger;
import server.ManagerSin;
import server.maps.FieldLimitType;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.Triple;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.GuildPacket;
import tools.packet.PartyPacket;

public class InterServerHandler {
    /**
     * 进入商城
     * @param c
     * @param chr
     */
    public static void EnterCS(MapleClient c, MapleCharacter chr) {
        if ((chr == null) || (chr.hasBlockedInventory()) || (chr.getMap() == null) || (chr.getEventInstance() != null) || (c.getChannelServer() == null)) {
            c.getSession().write(MaplePacketCreator.serverBlocked(2));
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        if ((!chr.isAlive())) {
            String msg = "无法进入商城，请稍后再试。";
            if (!chr.isAlive()) {
                msg = "现在不能进入商城.";
            }
            c.getPlayer().dropMessage(1, msg);
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        if (World.getPendingCharacterSize() >= 10) {
            chr.dropMessage(1, "服务器忙，请稍后在试。");
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        ChannelServer ch = ChannelServer.getInstance(c.getChannel());
        chr.changeRemoval();
        if (chr.getBuffedValue(MapleBuffStat.召唤兽) != null) {
            chr.cancelEffectFromBuffStat(MapleBuffStat.召唤兽, -1);
        }
        PlayerBuffStorage.addBuffsToStorage(chr.getId(), chr.getAllBuffs());
        PlayerBuffStorage.addCooldownsToStorage(chr.getId(), chr.getCooldowns());
//        PlayerBuffStorage.addDiseaseToStorage(chr.getId(), chr.getAllDiseases());
        //chr.cancelAllBuffs();
        World.ChannelChange_Data(new CharacterTransfer(chr), chr.getId(), -10);
        ch.removePlayer(chr);
        c.updateLoginState(MapleClient.CHANGE_CHANNEL, c.getSessionIPAddress());
        chr.saveToDB(false, false);
        if (chr.getMessenger() != null) {
            MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(chr);
            WorldMessengerService.getInstance().leaveMessenger(chr.getMessenger().getId(), messengerplayer);
        }
        chr.getMap().removePlayer(chr);
        c.getSession().write(MaplePacketCreator.getChannelChange(c, Integer.parseInt(CashShopServer.getIP().split(":")[1])));
//        CharacterTransfer transfer = CashShopServer.getPlayerStorage().getPendingCharacter(chr.getId());
//        CashShopOperation.EnterCS(transfer, c);
        c.setPlayer(null);
        c.setReceiving(false);

        try {
            int countRows = ManagerSin.jTable1.getRowCount();//获取当前表格总行数
            for (int i = 0; i < countRows; i++) {
                String sname = ManagerSin.jTable1.getValueAt(i, 1).toString();
                if (sname.equals(chr.getName())) {
                    ((DefaultTableModel) ManagerSin.jTable1.getModel()).setValueAt("现金商城", i, 4);
                    break;
                }
            }
        } catch (Exception e) {
            FileoutputUtil.outputFileError(FileoutputUtil.GUI_Ex_Log, e);
        }
    }

    /**
     * 登录游戏
     *
     * @param playerid 角色ID
     * @param c 客户端连接
     */
    public static void Loggedin(final int playerid, final MapleClient c) {
        try {
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
                    c.getSession().close(true);
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
                c.getSession().close(true);
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
                c.getSession().close(true);
                FileoutputUtil.log(new StringBuilder().append("检测连接地址 - 3 ").append(!allowLogin).toString());
                return;
            }
            c.updateLoginState(MapleClient.LOGIN_LOGGEDIN, c.getSessionIPAddress());
            channelServer.addPlayer(player);
            player.setlogintime(System.currentTimeMillis());
            player.giveCoolDowns(PlayerBuffStorage.getCooldownsFromStorage(player.getId()));
            player.silentGiveBuffs(PlayerBuffStorage.getBuffsFromStorage(player.getId()));
            player.giveSilentDebuff(PlayerBuffStorage.getDiseaseFromStorage(player.getId()));
//            c.getSession().write(MaplePacketCreator.cancelTitleEffect(new int[]{-1, -1, -1, -1, -1}));
            c.getSession().write(MaplePacketCreator.getCharInfo(player));
//            c.getSession().write(MTSCSPacket.enableCSUse(0));
//            c.getSession().write(MaplePacketCreator.sendloginSuccess());
            player.getMap().addPlayer(player);
            if (player.getMount() != null) {
                //c.getSession().write(MaplePacketCreator.updateMount(player, false));
            }
            //c.getSession().write(MaplePacketCreator.getKeymap(player));
            //c.getSession().write(MaplePacketCreator.getQuickSlot(player.getQuickSlot()));
//            player.updatePetAuto();
//            player.sendMacros();
            //c.getSession().write(MaplePacketCreator.showCharCash(player));
            //c.getSession().write(MaplePacketCreator.reportResponse(0, 0));
            //c.getSession().write(MaplePacketCreator.enableReport());
            //c.getSession().write(MaplePacketCreator.temporaryStats_Reset());
//            c.getSession().write(MaplePacketCreator.enableActions());
            int[] buddyIds = player.getBuddylist().getBuddyIds();
            WorldBuddyService.getInstance().loggedOn(player.getName(), player.getId(), c.getChannel(), buddyIds);
            //c.getSession().write(MaplePacketCreator.pendantSlot((stat != null) && (stat.getCustomData() != null) && (Long.parseLong(stat.getCustomData()) > System.currentTimeMillis())));
            //c.getSession().write(InventoryPacket.updateInventory());
            MapleParty party = player.getParty();
            if (party != null) {
                WrodlPartyService.getInstance().updateParty(party.getId(), PartyOperation.LOG_ONOFF, new MaplePartyCharacter(player));
            }
            if (player.getSidekick() == null) {
                player.setSidekick(WorldSidekickService.getInstance().getSidekickByChr(player.getId()));
            }
            if (player.getSidekick() != null) {
                c.getSession().write(PartyPacket.updateSidekick(player, player.getSidekick(), false));
            }
            CharacterIdChannelPair[] onlineBuddies = WorldFindService.getInstance().multiBuddyFind(player.getId(), buddyIds);
            for (CharacterIdChannelPair onlineBuddy : onlineBuddies) {
                player.getBuddylist().get(onlineBuddy.getCharacterId()).setChannel(onlineBuddy.getChannel());
            }
            //c.getSession().write(BuddyListPacket.updateBuddylist(player.getBuddylist().getBuddies(), player.getId()));
            //c.getSession().write(BuddyListPacket.updateBuddylist(0x1F));
            MapleMessenger messenger = player.getMessenger();
            if (messenger != null) {
                WorldMessengerService.getInstance().silentJoinMessenger(messenger.getId(), new MapleMessengerCharacter(player));
                WorldMessengerService.getInstance().updateMessenger(messenger.getId(), player.getName(), c.getChannel());
            }
            if (player.getGuildId() > 0) {
                WorldGuildService.getInstance().setGuildMemberOnline(player.getMGC(), true, c.getChannel());
                c.getSession().write(GuildPacket.showGuildInfo(player));
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
            player.getClient().getSession().write(MaplePacketCreator.serverMessageTop("欢迎来到怀旧冒×岛，希望你能找到儿时的感觉，查看可用命令@help 如有bug可以加QQ群：479357604！dev by:icelemon1314"));
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
//            c.getSession().write(MaplePacketCreator.enableActions());
//            return;
//        }
        if (World.getPendingCharacterSize() >= 10) {
            chr.dropMessage(1, "服务器忙，请稍后在试。");
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        int chc = slea.readByte() + 1;
        if (!World.isChannelAvailable(chc)) {
            chr.dropMessage(1, "该频道玩家已满，请切换到其它频道进行游戏。");
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        chr.changeChannel(chc);
    }
}
