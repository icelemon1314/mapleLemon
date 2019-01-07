package client;

import client.messages.PlayerGMRank;
import constants.ServerConstants;
import database.DatabaseConnection;
import database.DatabaseException;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.login.LoginServer;
import handling.world.PartyOperation;
import handling.world.WorldBuddyService;
import handling.world.WorldFindService;
import handling.world.WorldGuildService;
import handling.world.WorldMessengerService;
import handling.world.WorldSidekickService;
import handling.world.WrodlPartyService;
import handling.world.guild.MapleGuildCharacter;
import handling.world.messenger.MapleMessengerCharacter;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import handling.world.sidekick.MapleSidekick;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.script.ScriptEngine;
import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;
import scripting.item.ItemActionManager;
import scripting.item.ItemScriptManager;
import scripting.npc.NPCConversationManager;
import scripting.npc.NPCScriptManager;
import scripting.quest.QuestActionManager;
import scripting.quest.QuestScriptManager;
import server.Timer.PingTimer;
import server.maps.MapleMap;
import server.quest.MapleQuest;
import server.shops.IMaplePlayerShop;
import tools.FileoutputUtil;
import tools.MapleAESOFB;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.packet.LoginPacket;

public class MapleClient implements Serializable {

    private static final Logger log = Logger.getLogger(MapleClient.class);
    private static final long serialVersionUID = 9179541993413738569L;
    public static final byte LOGIN_NOTLOGGEDIN = 0;
    public static final byte LOGIN_SERVER_TRANSITION = 1;
    public static final byte LOGIN_LOGGEDIN = 2;
    public static final byte CHANGE_CHANNEL = 3;
    public static final byte ENTERING_PIN = 4; // 需要设置性别
    public static final byte PIN_CORRECT = 5;
    public static final int DEFAULT_CHARSLOT = LoginServer.getMaxCharacters();
    public static final String CLIENT_KEY = "CLIENT";
    private final transient MapleAESOFB send, receive;
    private final transient IoSession session;
    private long sessionId;
    private MapleCharacter player;
    private int channel = 1;
    private int accId = -1;
    private int world;
    private int birthday;
    private int charslots = DEFAULT_CHARSLOT;
    private int cardslots = 3;
    private boolean loggedIn = false;
    private boolean serverTransition = false;
    private transient Calendar tempban = null;
    private String accountName;
    private transient long lastPong = 0L;
    private transient long lastPing = 0L;
    private boolean monitored = false;
    private boolean receiving = true;
    private int gmLevel;
    private byte greason = 1;
    private byte gender = -1;
    public transient short loginAttempt = 0;
    private final transient List<Integer> allowedChar = new LinkedList();
    private transient String mac = "00-00-00-00-00-00";
    private final transient List<String> maclist = new LinkedList();
    private final transient Map<String, ScriptEngine> engines = new HashMap();
    private transient ScheduledFuture<?> idleTask = null;
    private transient String secondPassword;
    private transient String salt2;
    private transient String tempIP = "";
    private final transient Lock mutex = new ReentrantLock(true);
    private final transient Lock npc_mutex = new ReentrantLock();
    private long lastNpcClick = 0L;
    private static final Lock login_mutex = new ReentrantLock(true);
    private final byte loginattempt = 0;
    private DebugWindow debugWindow;
    private final Map<Integer, Pair<Short, Short>> charInfo = new LinkedHashMap();

    public MapleClient(MapleAESOFB send, MapleAESOFB receive, IoSession session) {
        this.send = send;
        this.receive = receive;
        this.session = session;
    }

    public final MapleAESOFB getReceiveCrypto() {
        return receive;
    }

    public final MapleAESOFB getSendCrypto() {
        return send;
    }

    public final IoSession getSession() {
        return session;
    }

    public long getSessionId() {
        return this.sessionId;

    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public void StartWindow() {
        if (this.debugWindow != null) {
            this.debugWindow.setVisible(false);
            this.debugWindow = null;
        }
        this.debugWindow = new DebugWindow();
        this.debugWindow.setVisible(true);
        this.debugWindow.setC(this);
    }

    public Lock getLock() {
        return this.mutex;
    }

    public Lock getNPCLock() {
        return this.npc_mutex;
    }

    public MapleCharacter getPlayer() {
        return this.player;
    }

    public void setPlayer(MapleCharacter player) {
        this.player = player;
    }

    public void createdChar(int id) {
        this.allowedChar.add(id);
    }

    public boolean login_Auth(int id) {
        return this.allowedChar.contains(id);
    }

    public List<MapleCharacter> loadCharacters(int serverId) {
        List chars = new LinkedList();
        MapleCharacter chr;
        for (CharNameAndId cni : loadCharactersInternal(serverId)) {
            chr = MapleCharacter.loadCharFromDB(cni.id, this, false);
            chars.add(chr);
            charInfo.put(chr.getId(), new Pair(chr.getLevel(), chr.getJob()));
            if (!login_Auth(chr.getId())) {
                allowedChar.add(chr.getId());
            }
        }
        return chars;
    }

    public boolean canMakeCharacter(int serverId) {
        return loadCharactersSize(serverId) < getAccCharSlots();
    }

    public List<String> loadCharacterNames(int serverId) {
        List chars = new LinkedList();
        for (CharNameAndId cni : loadCharactersInternal(serverId)) {
            chars.add(cni.name);
        }
        return chars;
    }

    private List<CharNameAndId> loadCharactersInternal(int serverId) {
        List chars = new LinkedList();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT id, name, gm FROM characters WHERE accountid = ? AND world = ?")) {
            ps.setInt(1, this.accId);
            ps.setInt(2, serverId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    chars.add(new CharNameAndId(rs.getString("name"), rs.getInt("id")));
                    LoginServer.getLoginAuth(rs.getInt("id"));
                }
            }
            ps.close();
        } catch (SQLException e) {
            log.error("error loading characters internal", e);
        }
        return chars;
    }

    private int loadCharactersSize(int serverId) {
        int chars = 0;
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT count(*) FROM characters WHERE accountid = ? AND world = ?")) {
            ps.setInt(1, this.accId);
            ps.setInt(2, serverId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    chars = rs.getInt(1);
                }
            }
            ps.close();
        } catch (SQLException e) {
            log.error("error loading characters internal", e);
        }
        return chars;
    }

    public boolean isLoggedIn() {
        return loggedIn && accId > 0;
    }

    private Calendar getTempBanCalendar(ResultSet rs) throws SQLException {
        Calendar lTempban = Calendar.getInstance();
        if (rs.getLong("tempban") == 0L) {
            lTempban.setTimeInMillis(0L);
            return lTempban;
        }
        Calendar today = Calendar.getInstance();
        lTempban.setTimeInMillis(rs.getTimestamp("tempban").getTime());
        if (today.getTimeInMillis() < lTempban.getTimeInMillis()) {
            return lTempban;
        }
        lTempban.setTimeInMillis(0L);
        return lTempban;
    }

    public Calendar getTempBanCalendar() {
        return this.tempban;
    }

    public byte getBanReason() {
        return this.greason;
    }

    public boolean hasBannedMac() {
        if ((this.mac.equalsIgnoreCase("00-00-00-00-00-00")) || (this.mac.length() != 17)) {
            return false;
        }
        boolean ret = false;
        return ret;
    }

    public int finishLogin() {
        login_mutex.lock();
        try {
            if (getLoginState() > 0) {
                this.loggedIn = false;
                return 7;
            }
            updateLoginState(MapleClient.LOGIN_LOGGEDIN, getSessionIPAddress());
        } finally {
            login_mutex.unlock();
        }
        return 0;
    }

    public void clearInformation() {
        accountName = null;
        accId = -1;
        secondPassword = null;
        gmLevel = 0;
        loggedIn = false;
        mac = "00-00-00-00-00-00";
        maclist.clear();
    }

    public int changePassword(String oldpwd, String newpwd) {
        int ret = -1;
        try {
            Connection con = DatabaseConnection.getConnection();
            ResultSet rs;
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts WHERE name = ?")) {
                ps.setString(1, getAccountName());
                rs = ps.executeQuery();
                if (rs.next()) {
                    boolean updatePassword = false;
                    String passhash = rs.getString("password");
                    String salt = rs.getString("salt");
                    if ((passhash == null) || (passhash.isEmpty())) {
                        ret = -1;
                    } else if ((LoginCryptoLegacy.isLegacyPassword(passhash)) && (LoginCryptoLegacy.checkPassword(oldpwd, passhash))) {
                        ret = 0;
                        updatePassword = true;
                    } else if (oldpwd.equals(passhash)) {
                        ret = 0;
                        updatePassword = true;
                    } else if ((salt == null) && (LoginCrypto.checkSha1Hash(passhash, oldpwd))) {
                        ret = 0;
                        updatePassword = true;
                    } else if (LoginCrypto.checkSaltedSha512Hash(passhash, oldpwd, salt)) {
                        ret = 0;
                        updatePassword = true;
                    } else {
                        ret = -1;
                    }
                    if (updatePassword) {
                        try (PreparedStatement pss = con.prepareStatement("UPDATE `accounts` SET `password` = ?, `salt` = ? WHERE id = ?")) {
                            String newSalt = LoginCrypto.makeSalt();
                            pss.setString(1, LoginCrypto.makeSaltedSha512Hash(newpwd, newSalt));
                            pss.setString(2, newSalt);
                            pss.setInt(3, this.accId);
                            pss.executeUpdate();
                        }
                    }
                }
                ps.close();
            }
            rs.close();
        } catch (SQLException e) {
            log.error("修改密码出错\r\n", e);
        }
        return ret;
    }

    /**
     * 验证帐号密码
     * @param login
     * @param pwd
     * @param ipMacBanned
     * @return
     */
    public int login(String login, String pwd) {
        int loginok = 5;
        pwd = LoginCrypto.hexSha1(pwd); // 用最简单的sha1
        try {
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts WHERE name = ?")) {
                ps.setString(1, login);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        final int banned = rs.getInt("banned");
                        final String passhash = rs.getString("password");
                        final String oldSession = rs.getString("SessionIP");
                        accountName = login;
                        accId = rs.getInt("id");
                        gmLevel = rs.getInt("gm");
                        greason = rs.getByte("greason");
                        tempban = getTempBanCalendar(rs);
                        gender = rs.getByte("gender");

                        if (banned > 0 && gmLevel < 6) {
                            loginok = 3;
                        } else {
                            if (banned == -1) {
                                unban();
                            }
                            // Check if the passwords are correct here. :B
                            if (passhash == null || passhash.isEmpty()) {
                                //match by sessionIP
                                if (oldSession != null && !oldSession.isEmpty()) {
                                    loggedIn = getSessionIPAddress().equals(oldSession);
                                    loginok = loggedIn ? 0 : 4;
                                } else {
                                    loginok = 4;
                                    loggedIn = false;
                                }
                            } else if (pwd.equals(passhash)) {
                                loginok = 0;
                            } else {
                                loggedIn = false;
                                loginok = 4;
                            }
                            if (getLoginState() > MapleClient.LOGIN_NOTLOGGEDIN) { // already loggedin
                                if (loginok != 0) {
                                    loggedIn = false;
                                    loginok = 7;
                                } else {//解卡处理
                                    解卡账号();
                                }
                            }
                        }
                    }
                }
                ps.close();
            }
        } catch (SQLException e) {
            System.err.println("登录出错：" + e);
        }
        return loginok;
    }

    public void 解卡账号() {
        boolean 解卡在线 = false;
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (final MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                if (mch.getAccountID() == accId) {
                    try {
                        mch.getClient().getSession().write(MaplePacketCreator.serverMessagePopUp("当前账号在别的地方登录了\r\n若不是你本人操作请及时更改密码。"));
                        mch.getClient().disconnect(true, mch.getClient().getChannel() == -10);
                        Thread closeSession = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    sleep(3000);
                                } catch (InterruptedException ex) {
                                }
                                mch.getClient().getSession().close(true);
                            }
                        };
                        closeSession.start();
                    } catch (Exception ex) {
                    }
                    解卡在线 = true;
                }
            }
        }
        if (!解卡在线) {
            try {
                Connection con = DatabaseConnection.getConnection();
                try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET loggedin = 0 WHERE name = ?")) {
                    ps.setString(1, accountName);
                    ps.executeUpdate();
                    ps.close();
                }
            } catch (SQLException se) {
            }
        }
    }


    private void unban() {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET banned = 0, banreason = '' WHERE id = ?")) {
            ps.setInt(1, this.accId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            log.error("Error while unbanning", e);
        }
    }

    public static byte unban(String charname) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT accountid from characters where name = ?");
            ps.setString(1, charname);

            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            int accid = rs.getInt(1);
            rs.close();
            ps.close();

            ps = con.prepareStatement("UPDATE accounts SET banned = 0, banreason = '' WHERE id = ?");
            ps.setInt(1, accid);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            log.error("Error while unbanning", e);
            return -2;
        }
        return 0;
    }

    public void setAccID(int id) {
        this.accId = id;
    }

    public int getAccID() {
        return this.accId;
    }

    public void updateLoginState(int newstate) {
        updateLoginState(newstate, getSessionIPAddress());
    }

    public void updateLoginState(int newstate, String SessionID) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET loggedin = ?, SessionIP = ?, lastlogin = CURRENT_TIMESTAMP() WHERE id = ?")) {
            ps.setInt(1, newstate);
            ps.setString(2, SessionID);
            ps.setInt(3, getAccID());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            log.error("Error updating login state", e);
        }
        if (newstate == 0) {
            this.loggedIn = false;
            this.serverTransition = false;
        } else {
            this.serverTransition = ((newstate == 1) || (newstate == 3));
            this.loggedIn = (!this.serverTransition);
        }
    }

    public void updateSecondPassword() {
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE `accounts` SET `2ndpassword` = ?, `salt2` = ? WHERE id = ?")) {
                String newSalt = LoginCrypto.makeSalt();
                ps.setString(1, LoginCrypto.rand_s(LoginCrypto.makeSaltedSha512Hash(this.secondPassword, newSalt)));
                ps.setString(2, newSalt);
                ps.setInt(3, this.accId);
                ps.executeUpdate();
                ps.close();
            }
        } catch (SQLException e) {
            log.error("Error updating login state", e);
        }
    }

    public byte getLoginState() {
        Connection con = DatabaseConnection.getConnection();
        try {
            byte state;
            try (PreparedStatement ps = con.prepareStatement("SELECT loggedin, lastlogin, banned, gm, `birthday` + 0 AS `bday` FROM accounts WHERE id = ?")) {
                ps.setInt(1, getAccID());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || (rs.getInt("banned") > 0 && rs.getInt("gm") < 6)) {
                        ps.close();
                        rs.close();
                        session.close(true);
                        throw new DatabaseException("Account doesn't exist or is banned");
                    }
                    birthday = rs.getInt("bday");
                    state = rs.getByte("loggedin");
                    if ((state == MapleClient.LOGIN_SERVER_TRANSITION || state == MapleClient.CHANGE_CHANNEL) && (rs.getTimestamp("lastlogin").getTime() + 20000L < System.currentTimeMillis())) {
                        state = MapleClient.LOGIN_NOTLOGGEDIN;
                        updateLoginState(state, getSessionIPAddress());
                    }
                }
                ps.close();
            }
            loggedIn = state == MapleClient.LOGIN_LOGGEDIN;
            return state;
        } catch (SQLException e) {
            loggedIn = false;
            throw new DatabaseException("error getting login state", e);
        }
    }

    public boolean checkBirthDate(int date) {
        return this.birthday == date;
    }

    public void removalTask(boolean shutdown) {
        try {
            this.player.cancelAllBuffs_();
            this.player.cancelAllDebuffs();
            if (this.player.getMarriageId() > 0) {
                MapleQuestStatus stat1 = this.player.getQuestNoAdd(MapleQuest.getInstance(160001));
                MapleQuestStatus stat2 = this.player.getQuestNoAdd(MapleQuest.getInstance(160002));
                if ((stat1 != null) && (stat1.getCustomData() != null) && ((stat1.getCustomData().equals("2_")) || (stat1.getCustomData().equals("2")))) {
                    if ((stat2 != null) && (stat2.getCustomData() != null)) {
                        stat2.setCustomData("0");
                    }
                    stat1.setCustomData("3");
                }
            }
            if ((this.player.getMapId() == 180000001) && (!this.player.isIntern())) {
                MapleQuestStatus stat1 = this.player.getQuestNAdd(MapleQuest.getInstance(123455));
                MapleQuestStatus stat2 = this.player.getQuestNAdd(MapleQuest.getInstance(123456));
                if (stat1.getCustomData() == null) {
                    stat1.setCustomData(String.valueOf(System.currentTimeMillis()));
                } else if (stat2.getCustomData() == null) {
                    stat2.setCustomData("0");
                } else {
                    int seconds = Integer.parseInt(stat2.getCustomData()) - (int) ((System.currentTimeMillis() - Long.parseLong(stat1.getCustomData())) / 1000L);
                    if (seconds < 0) {
                        seconds = 0;
                    }
                    stat2.setCustomData(String.valueOf(seconds));
                }
            }
            this.player.changeRemoval(true);
            if (this.player.getEventInstance() != null) {
                this.player.getEventInstance().playerDisconnected(this.player, this.player.getId());
            }
            IMaplePlayerShop shop = this.player.getPlayerShop();
            if (shop != null) {
                shop.removeVisitor(this.player);
                if (shop.isOwner(this.player)) {
                    if ((shop.getShopType() == 1) && (shop.isAvailable()) && (!shutdown)) {
                        shop.setOpen(true);
                    } else {
                        shop.closeShop(true, !shutdown);
                    }
                }
            }
            this.player.setMessenger(null);
            if (this.player.getMap() != null) {
                if ((shutdown) || ((getChannelServer() != null) && (getChannelServer().isShutdown()))) {
                    int questID = -1;
                    switch (this.player.getMapId()) {
                        case 240060200:
                            questID = 160100;
                            break;
                        case 240060201:
                            questID = 160103;
                            break;
                        case 280030000:
                        case 280030100:
                            questID = 160101;
                            break;
                        case 280030001:
                            questID = 160102;
                            break;
                        case 270050100:
                            questID = 160104;
                            break;
                        case 105100300:
                        case 105100400:
                            questID = 160106;
                            break;
                        case 211070000:
                        case 211070100:
                        case 211070101:
                        case 211070110:
                            questID = 160107;
                            break;
                        case 551030200:
                            questID = 160108;
                            break;
                        case 271040100:
                            questID = 160109;
                    }

                    if (questID > 0) {
                        this.player.getQuestNAdd(MapleQuest.getInstance(questID)).setCustomData("0");
                    }
                } else if (this.player.isAlive()) {
                    switch (this.player.getMapId()) {
                        case 220080001:
                        case 541010100:
                        case 541020800:
                            this.player.getMap().addDisconnected(this.player.getId());
                    }
                }

                this.player.getMap().removePlayer(this.player);
            }
        } catch (NumberFormatException e) {
            FileoutputUtil.outputFileError(FileoutputUtil.Acc_Stuck, e);
        }
    }

    public void disconnect(boolean RemoveInChannelServer, boolean fromCS) {
        disconnect(RemoveInChannelServer, fromCS, false);
    }

    public void disconnect(boolean RemoveInChannelServer, boolean fromCS, boolean shutdown) {
        if (this.debugWindow != null) {
            this.debugWindow.setVisible(false);
            this.debugWindow = null;
        }
        if (this.player != null) {
            MapleMap map = player.getMap();
            MapleParty party = player.getParty();
            String namez = player.getName();
            int idz = player.getId();
            int messengerid = player.getMessenger() == null ? 0 : player.getMessenger().getId();
            int gid = player.getGuildId();
            BuddyList bl = player.getBuddylist();
            MaplePartyCharacter chrp = new MaplePartyCharacter(player);
            MapleMessengerCharacter chrm = new MapleMessengerCharacter(player);
            MapleGuildCharacter chrg = player.getMGC();
            removalTask(shutdown);
            LoginServer.getLoginAuth(player.getId());
            player.saveToDB(true, fromCS);
            if (shutdown) {
                player = null;
                receiving = false;
                return;
            }
            if (!fromCS) {
                ChannelServer ch = ChannelServer.getInstance(map == null ? channel : map.getChannel());
                int chz = WorldFindService.getInstance().findChannel(idz);
                if (chz < -1) {
                    disconnect(RemoveInChannelServer, true);
                    return;
                }
                try {
                    if ((chz == -1) || (ch == null) || (ch.isShutdown())) {
                        player = null;
                        return;
                    }
                    if (messengerid > 0) {
                        WorldMessengerService.getInstance().leaveMessenger(messengerid, chrm);
                    }
                    if (party != null) {
                        party.cancelAllPartyBuffsByChr(player.getId());
                        chrp.setOnline(false);
                        WrodlPartyService.getInstance().updateParty(party.getId(), PartyOperation.LOG_ONOFF, chrp);
                        if ((map != null) && (party.getLeader().getId() == idz)) {
                            MaplePartyCharacter lchr = null;
                            for (MaplePartyCharacter pchr : party.getMembers()) {
                                if ((pchr != null) && (map.getCharacterById(pchr.getId()) != null) && ((lchr == null) || (lchr.getLevel() < pchr.getLevel()))) {
                                    lchr = pchr;
                                }
                            }
                            if (lchr != null) {
                                WrodlPartyService.getInstance().updateParty(party.getId(), PartyOperation.CHANGE_LEADER_DC, lchr);
                            }
                        }
                    }
                    if (bl != null) {
                        if (!serverTransition) {
                            WorldBuddyService.getInstance().loggedOff(namez, idz, channel, bl.getBuddyIds());
                        } else {
                            WorldBuddyService.getInstance().loggedOn(namez, idz, channel, bl.getBuddyIds());
                        }
                    }
                    if ((gid > 0) && (chrg != null)) {
                        WorldGuildService.getInstance().setGuildMemberOnline(chrg, false, -1);
                    }
                } catch (Exception e) {
                    FileoutputUtil.outputFileError(FileoutputUtil.Acc_Stuck, e);
                    log.error(new StringBuilder().append(getLogMessage(this, "ERROR")).append(e).toString());
                } finally {
                    if ((RemoveInChannelServer) && (ch != null)) {
                        ch.removePlayer(player);
                    }
                    player = null;
                }
            } else {
                int ch = WorldFindService.getInstance().findChannel(idz);
                if (ch > 0) {
                    disconnect(RemoveInChannelServer, false);
                    return;
                }
                try {
                    if (party != null) {
                        chrp.setOnline(false);
                        WrodlPartyService.getInstance().updateParty(party.getId(), PartyOperation.LOG_ONOFF, chrp);
                    }
                    if (!serverTransition) {
                        WorldBuddyService.getInstance().loggedOff(namez, idz, this.channel, bl.getBuddyIds());
                    } else {
                        WorldBuddyService.getInstance().loggedOn(namez, idz, this.channel, bl.getBuddyIds());
                    }
                    if ((gid > 0) && (chrg != null)) {
                        WorldGuildService.getInstance().setGuildMemberOnline(chrg, false, -1);
                    }
                    if (player != null) {
                        player.setMessenger(null);
                    }
                } catch (Exception e) {
                    FileoutputUtil.outputFileError(FileoutputUtil.Acc_Stuck, e);
                    log.error(new StringBuilder().append(getLogMessage(this, "ERROR")).append(e).toString());
                } finally {
                    if ((RemoveInChannelServer) && (ch > 0)) {
                        CashShopServer.getPlayerStorage().deregisterPlayer(player);
                    }
                    player = null;
                }
            }
        }
        if ((!this.serverTransition) && (isLoggedIn())) {
            updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN, getLastIPAddress());
        }
        this.engines.clear();
    }

    public String getSessionIPAddress() {
        return this.session.getRemoteAddress().toString().split(":")[0];
    }

    public boolean CheckIPAddress() {
        if (this.accId < 0) {
            return false;
        }
        try {
            boolean canlogin;
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT SessionIP, banned FROM accounts WHERE id = ?")) {
                ps.setInt(1, this.accId);
                try (ResultSet rs = ps.executeQuery()) {
                    canlogin = false;
                    if (rs.next()) {
                        String sessionIP = rs.getString("SessionIP");
                        if (sessionIP != null) {
                            canlogin = getSessionIPAddress().equals(sessionIP.split(":")[0]);
                        }
                        if (rs.getInt("banned") > 0) {
                            canlogin = false;
                        }
                    }
                }
                ps.close();
            }
            return canlogin;
        } catch (SQLException e) {
            log.error("Failed in checking IP address for client.", e);
        }
        return true;
    }

    public String getLastIPAddress() {
        String LastIP = "/0:0:0:0";
        if (this.accId < 0) {
            return LastIP;
        }
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT SessionIP, banned FROM accounts WHERE id = ?")) {
            ps.setInt(1, this.accId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LastIP = rs.getString("SessionIP");
                }
            }
            ps.close();
            return LastIP;
        } catch (SQLException e) {
            log.error("获取登录IP出错.", e);
        }
        return LastIP;
    }

    public void DebugMessage(StringBuilder sb) {
        sb.append("IP: ");
        sb.append(getSession().getRemoteAddress());
        sb.append(" || 连接状态: ");
        sb.append(getSession().isConnected());
        sb.append(" || 正在关闭: ");
        sb.append(getSession().isClosing());
        sb.append(" || CLIENT: ");
        sb.append(getSession().getAttribute("CLIENT") != null);
        sb.append(" || 是否已登陆: ");
        sb.append(isLoggedIn());
        sb.append(" || 角色上线: ");
        sb.append(getPlayer() != null);
    }

    public int getChannel() {
        return this.channel;
    }

    public ChannelServer getChannelServer() {
        return ChannelServer.getInstance(this.channel);
    }

    public int deleteCharacter(int cid) {
        if (this.getPlayer() != null && this.getPlayer().getId() == cid) {
            return 2;
        }
        try {
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("SELECT guildid, guildrank, familyid, name FROM characters WHERE id = ? AND accountid = ?")) {
                ps.setInt(1, cid);
                ps.setInt(2, this.accId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        rs.close();
                        ps.close();
                        return 1;
                    }
                    if (rs.getInt("guildid") > 0) {
                        if (rs.getInt("guildrank") == 1) {
                            rs.close();
                            ps.close();
                            return 1;
                        }
                        WorldGuildService.getInstance().deleteGuildCharacter(rs.getInt("guildid"), cid);
                    }
                    MapleSidekick sidekick = WorldSidekickService.getInstance().getSidekickByChr(cid);
                    if (sidekick != null) {
                        sidekick.eraseToDB();
                    }
                }
                ps.close();
            }

            MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM characters WHERE id = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con, "UPDATE pokemon SET active = 0 WHERE characterid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM hiredmerch WHERE characterid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM mountdata WHERE characterid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM inventoryitems WHERE characterid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM famelog WHERE characterid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM famelog WHERE characterid_to = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM wishlist WHERE characterid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM buddies WHERE characterid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM buddies WHERE buddyid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM keymap WHERE characterid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM savedlocations WHERE characterid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM skills WHERE characterid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM skills WHERE teachId = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM familiars WHERE characterid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM mountdata WHERE characterid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM queststatus WHERE characterid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM inventoryslot WHERE characterid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM bank WHERE charid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM pqlog WHERE characterid = ?", cid);
            return 0;
        } catch (SQLException e) {
            FileoutputUtil.outputFileError(FileoutputUtil.SQL_Ex_Log, e);
            log.error("删除角色错误.", e);
        }
        return 1;
    }

    public byte getGender() {
        return this.gender;
    }

    public void setGender(byte gender) {
        this.gender = gender;
    }

    public void changeGender(byte gender) {
        this.gender = gender;
        Connection con = DatabaseConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET gender = ? WHERE id = ?")) {
            ps.setByte(1, gender);
            ps.setInt(2, this.accId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            log.error("设置性别出错", e);
        }
    }



    public String getSecondPassword() {
        return this.secondPassword;
    }

    public void setSecondPassword(String secondPassword) {
        this.secondPassword = secondPassword;
    }

    public String getAccountName() {
        return this.accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getWorld() {
        return this.world;
    }

    public void setWorld(int world) {
        this.world = world;
    }

    public int getLatency() {
        return (int) (this.lastPong - this.lastPing);
    }

    public long getLastPong() {
        return this.lastPong;
    }

    public long getLastPing() {
        return this.lastPing;
    }

    public void pongReceived() {
        this.lastPong = System.currentTimeMillis();
    }

    public void sendPing() {
        this.lastPing = System.currentTimeMillis();
        getSession().write(LoginPacket.getPing());

        PingTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                if (getLatency() < 0) {
                    disconnect(true, false);
                    if (getSession().isConnected()) {
                        FileoutputUtil.log(MapleClient.getLogMessage(MapleClient.this, "PING超时."));
                        getSession().close(true);
                    }
                }
            }
        }, 15000L);
    }

    public static String getLogMessage(MapleClient cfor, String message) {
        return getLogMessage(cfor, message, new Object[0]);
    }

    public static String getLogMessage(MapleCharacter cfor, String message) {
        return getLogMessage(cfor == null ? null : cfor.getClient(), message);
    }

    public static String getLogMessage(MapleCharacter cfor, String message, Object[] parms) {
        return getLogMessage(cfor == null ? null : cfor.getClient(), message, parms);
    }

    public static String getLogMessage(MapleClient cfor, String message, Object[] parms) {
        StringBuilder builder = new StringBuilder();
        if (cfor != null) {
            if (cfor.getPlayer() != null) {
                builder.append("<");
                builder.append(MapleCharacterUtil.makeMapleReadable(cfor.getPlayer().getName()));
                builder.append(" (角色ID: ");
                builder.append(cfor.getPlayer().getId());
                builder.append(")> ");
            }
            if (cfor.getAccountName() != null) {
                builder.append("(账号: ");
                builder.append(cfor.getAccountName());
                builder.append(") ");
            }
        }
        builder.append(message);

        for (Object parm : parms) {
            int start = builder.indexOf("{}");
            builder.replace(start, start + 2, parm.toString());
        }
        return builder.toString();
    }

    public static int findAccIdForCharacterName(String charName) {
        try {
            Connection con = DatabaseConnection.getConnection();
            int ret;
            try (PreparedStatement ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?")) {
                ps.setString(1, charName);
                try (ResultSet rs = ps.executeQuery()) {
                    ret = -1;
                    if (rs.next()) {
                        ret = rs.getInt("accountid");
                    }
                }
                ps.close();
            }
            return ret;
        } catch (SQLException e) {
            log.error("findAccIdForCharacterName SQL error", e);
        }
        return -1;
    }

    public boolean isSuperGM() {
        return this.gmLevel >= PlayerGMRank.SUPERGM.getLevel();
    }

    public boolean isIntern() {
        return this.gmLevel >= PlayerGMRank.INTERN.getLevel();
    }

    public boolean isGm() {
        return this.gmLevel >= PlayerGMRank.GM.getLevel();
    }

    public boolean isAdmin() {
        return this.gmLevel >= PlayerGMRank.ADMIN.getLevel();
    }

    public int getGmLevel() {
        return gmLevel;
    }

    public final void setGm(int gmLevel) {
        this.gmLevel = gmLevel;
    }

    public ScheduledFuture<?> getIdleTask() {
        return this.idleTask;
    }

    public void setIdleTask(ScheduledFuture<?> idleTask) {
        this.idleTask = idleTask;
    }

    public int getAccCharSlots() {
        if (isGm()) {
            return 30;
        }

        if (this.charslots != DEFAULT_CHARSLOT) {
            return this.charslots;
        }
        int theworld = world;
        if (player != null) {
            theworld = this.player.getWorld();
        }
        try {
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM character_slots WHERE accid = ? AND worldid = ?")) {
                ps.setInt(1, this.accId);
                ps.setInt(2, theworld);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        this.charslots = rs.getInt("charslots");
                    } else {
                        try (PreparedStatement psu = con.prepareStatement("INSERT INTO character_slots (accid, worldid, charslots) VALUES (?, ?, ?)")) {
                            psu.setInt(1, this.accId);
                            psu.setInt(2, this.world);
                            psu.setInt(3, this.charslots);
                            psu.executeUpdate();
                            psu.close();
                        }
                    }
                }
                ps.close();
            }
        } catch (SQLException e) {
            log.error("getAccCharSlots出错", e);
        }
        return this.charslots;
    }

    public boolean gainAccCharSlot() {
        if (getAccCharSlots() >= 30) {
            return false;
        }
        this.charslots++;
        int theworld = world;
        if (player != null) {
            theworld = this.player.getWorld();
        }
        Connection con = DatabaseConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement("UPDATE character_slots SET charslots = ? WHERE worldid = ? AND accid = ?")) {
            ps.setInt(1, this.charslots);
            ps.setInt(2, theworld);
            ps.setInt(3, this.accId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            log.error("gainAccCharSlot出错", e);
            return false;
        }
        return true;
    }

    public int getAccCardSlots() {
        Connection con = DatabaseConnection.getConnection();
        int theworld = world;
        if (player != null) {
            theworld = this.player.getWorld();
        }
        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts_info WHERE accId = ? AND worldId = ?")) {
            ps.setInt(1, this.accId);
            ps.setInt(2, theworld);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    this.cardslots = rs.getInt("cardSlots");
                } else {
                    try (PreparedStatement psu = con.prepareStatement("INSERT INTO accounts_info (accId, worldId, cardSlots) VALUES (?, ?, ?)")) {
                        psu.setInt(1, this.accId);
                        psu.setInt(2, this.world);
                        psu.setInt(3, this.cardslots);
                        psu.executeUpdate();
                        psu.close();
                    }
                }
            }
            ps.close();
        } catch (SQLException e) {
            log.error("getAccCardSlots出错", e);
        }
        return this.cardslots;
    }

    public boolean gainAccCardSlot() {
        if (getAccCardSlots() >= 9) {
            return false;
        }
        this.cardslots++;
        int theworld = world;
        if (player != null) {
            theworld = this.player.getWorld();
        }
        Connection con = DatabaseConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement("UPDATE accounts_info SET cardSlots = ? WHERE worldId = ? AND accId = ?")) {
            ps.setInt(1, this.cardslots);
            ps.setInt(2, theworld);
            ps.setInt(3, this.accId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            log.error("gainAccCardSlot出错", e);
            return false;
        }
        return true;
    }

    public static byte unbanIPMacs(String charname) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT accountid from characters where name = ?");
            ps.setString(1, charname);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            int accid = rs.getInt(1);
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
            ps.setInt(1, accid);
            rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            String sessionIP = rs.getString("sessionIP");
            String macs = rs.getString("macs");
            rs.close();
            ps.close();
            byte ret = 0;
            return ret;
        } catch (SQLException e) {
            log.error("Error while unbanning", e);
        }
        return -2;
    }

    public static byte unHellban(String charname) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT accountid from characters where name = ?");
            ps.setString(1, charname);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            int accid = rs.getInt(1);
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
            ps.setInt(1, accid);
            rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            String sessionIP = rs.getString("sessionIP");
            String email = rs.getString("email");
            rs.close();
            ps.close();
            ps = con.prepareStatement(new StringBuilder().append("UPDATE accounts SET banned = 0, banreason = '' WHERE email = ?").append(sessionIP == null ? "" : " OR sessionIP = ?").toString());
            ps.setString(1, email);
            if (sessionIP != null) {
                ps.setString(2, sessionIP);
            }
            ps.execute();
            ps.close();
            return 0;
        } catch (SQLException e) {
            log.error("Error while unbanning", e);
        }
        return -2;
    }

    public boolean isMonitored() {
        return this.monitored;
    }

    public void setMonitored(boolean m) {
        this.monitored = m;
    }

    public boolean isReceiving() {
        return this.receiving;
    }

    public void setReceiving(boolean m) {
        this.receiving = m;
    }

    public Timestamp getCreated() {
        try {
            Timestamp ret;
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT createdat FROM accounts WHERE id = ?")) {
                ps.setInt(1, getAccID());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        rs.close();
                        ps.close();
                        return null;
                    }
                    ret = rs.getTimestamp("createdat");
                }
                ps.close();
            }
            return ret;
        } catch (SQLException e) {
            throw new DatabaseException("error getting create", e);
        }
    }

    public String getTempIP() {
        return this.tempIP;
    }

    public void setTempIP(String s) {
        this.tempIP = s;
    }

    public boolean isLocalhost() {
        return (ServerConstants.USE_LOCALHOST) || (ServerConstants.isIPLocalhost(getSessionIPAddress()));
    }

//    public boolean hasCheck(int accid) {
//        boolean ret = false;
//        try {
//            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM accounts WHERE id = ?");
//            ps.setInt(1, accid);
//            ResultSet rs = ps.executeQuery();
//            if (rs.next()) {
//                ret = rs.getInt("check") > 0;
//            }
//            rs.close();
//            ps.close();
//        } catch (SQLException ex) {
//            log.error("Error checking ip Check", ex);
//        }
//        return ret;
//    }
    public static String getAccInfo(String accname, boolean admin) {
        StringBuilder ret = new StringBuilder(new StringBuilder().append("账号ID：").append(accname).append(" 信息-").toString());
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM accounts WHERE name = ?")) {
                ps.setString(1, accname);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int banned = rs.getInt("banned");
                        ret.append("封号状态：");
                        ret.append(banned > 0 ? "被封" : "没有被封");
                        ret.append("封号理由：");
                        ret.append(banned > 0 ? rs.getString("banreason") : "(没有封号)");
                        if (admin) {
                            ret.append("点券：");
                            ret.append(rs.getInt("ACash"));
                            ret.append("抵用券：");
                            ret.append(rs.getInt("mPoints"));
                        }
                    }
                }
                ps.close();
            }
        } catch (SQLException ex) {
            log.error("获取玩家封号理由信息出错:", ex);
        }
        return ret.toString();
    }

    public static String getAccInfoByName(String charname, boolean admin) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT accountid from characters where name = ?");
            ps.setString(1, charname);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return null;
            }
            int accid = rs.getInt(1);
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
            ps.setInt(1, accid);
            rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return null;
            }
            StringBuilder ret = new StringBuilder("玩家 " + charname + " 的帐号信息 -");
            int banned = rs.getInt("banned");
            if (admin) {
                ret.append(" 账号: ");
                ret.append(rs.getString("name"));
            }
            ret.append(" 状态: ");
            ret.append(banned > 0 ? "已封" : "正常");
            ret.append(" 封号理由: ");
            ret.append(banned > 0 ? rs.getString("banreason") : "(无描述)");
            rs.close();
            ps.close();
            return ret.toString();
        } catch (SQLException ex) {
            log.error("获取玩家封号理由信息出错", ex);
        }
        return null;
    }

    public void setScriptEngine(String name, ScriptEngine e) {
        this.engines.put(name, e);
    }

    public ScriptEngine getScriptEngine(String name) {
        return (ScriptEngine) this.engines.get(name);
    }

    public void removeScriptEngine(String name) {
        this.engines.remove(name);
    }

    public boolean canClickNPC() {
        return this.lastNpcClick + 500L < System.currentTimeMillis();
    }

    public void setClickedNPC() {
        this.lastNpcClick = System.currentTimeMillis();
    }

    public void removeClickedNPC() {
        this.lastNpcClick = 0L;
    }

    public NPCConversationManager getCM() {
        return NPCScriptManager.getInstance().getCM(this);
    }

    public QuestActionManager getQM() {
        return QuestScriptManager.getInstance().getQM(this);
    }

    public ItemActionManager getIM() {
        return ItemScriptManager.getInstance().getIM(this);
    }

    public boolean hasCheckMac(String macData) {
        if ((macData.equalsIgnoreCase("00-00-00-00-00-00")) || (macData.length() != 17) || (this.maclist.isEmpty())) {
            return false;
        }
        return this.maclist.contains(macData);
    }

    public boolean isAccountNameUsed(String accountName){
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT id from accounts where name = ?");
            ps.setString(1, accountName);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return false;
            }
        } catch (SQLException ex) {
            log.error("获取玩家封号理由信息出错", ex);
            return true;
        }
        return true;
    }

    protected static class CharNameAndId {

        public final String name;
        public final int id;

        public CharNameAndId(String name, int id) {
            this.name = name;
            this.id = id;
        }
    }
}
