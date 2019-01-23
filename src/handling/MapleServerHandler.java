package handling;

import client.MapleClient;
import constants.ServerConstants;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.channel.handler.*;
import handling.login.LoginServer;
import handling.login.LoginWorker;
import handling.login.handler.*;
import handling.mina.MaplePacketDecoder;
import handling.RecvPacketOpcode;
import handling.world.World;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import scripting.npc.NPCScriptManager;
import tools.*;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericSeekableLittleEndianAccessor;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

public class MapleServerHandler extends ChannelInboundHandlerAdapter {

    private final int channel;
    public final static int CASH_SHOP_SERVER = -10;
    public final static int LOGIN_SERVER = 0;
    private static final boolean show = false;
    private final List<String> BlockIPList = new ArrayList();
    private final Map<String, Pair<Long, Byte>> tracker = new ConcurrentHashMap();
    private Map<Byte, MaplePacketHandler> handlers;

    public MapleServerHandler(int channel) {
        this.handlers = new HashMap<>();
        this.channel = channel;
    }

    private void RegisterHandlers() {
        for(RecvPacketOpcode recvOpcode : RecvPacketOpcode.values()) {
            String handleClass = getHandleClassPath(recvOpcode.name());
            String className = "handling." + handleClass + ".handler." + UnderlineToHump(recvOpcode.name()) + "Handler";
            try {
                Class handler = Class.forName(className);
                MaplePacketHandler mapleHandler = (MaplePacketHandler)handler.newInstance();
                handlers.put(recvOpcode.getValue(), mapleHandler);
            } catch (Exception e) {
                MapleLogger.error("registerHandlers errors:" + e.getMessage());
            }
        }
    }

    private String UnderlineToHump(String para) {
        StringBuilder result=new StringBuilder();
        String[] a=para.split("_");
        int index = 0;
        for(String s:a){
            if (index == 0) {
                index++;
                continue;
            }
            result.append(s.substring(0, 1).toUpperCase());
            result.append(s.substring(1).toLowerCase());
            index++;
        }
        return result.toString();
    }

    private String getHandleClassPath(String param) {
        String[] a = param.split("_");
        return a[0].toLowerCase();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext session, Throwable cause) { //异常捕获
        if (cause.getMessage() != null) {
            System.err.println("[异常信息] " + cause.getMessage());
            cause.printStackTrace();
            cause.getLocalizedMessage();
            MapleLogger.error(cause.getMessage());
        }
        if ((!(cause instanceof IOException))) {
            MapleClient client = session.channel().attr(MapleClient.CLIENT_KEY).get();
            if ((client != null) && (client.getPlayer() != null)) {
                client.getPlayer().saveToDB(false, channel == MapleServerHandler.CASH_SHOP_SERVER);
                MapleLogger.error("发现异常 by: 玩家:" + client.getPlayer().getName() + " 职业:" + client.getPlayer().getJob() + " 地图:" + client.getPlayer().getMap().getMapName() + " - " + client.getPlayer().getMapId());
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext session) {
        // 起始 IP 检查
        String address = session.channel().remoteAddress().toString().split(":")[0];
        MapleLogger.info("[登陆服务] " + address + " 已连接");

        if (BlockIPList.contains(address)) {
            session.close();
            return;
        }
        Pair track = tracker.get(address);

        byte count;
        if (track == null) {
            count = 1;
        } else {
            count = (Byte) track.right;

            long difference = System.currentTimeMillis() - (Long) track.left;
            if (difference < 2000L) {// 小于2秒
                count = (byte) (count + 1);
            } else if (difference > 20000L) {// 超过2秒
                count = 1;
            }
            if (count > 10) {
                BlockIPList.add(address);
                tracker.remove(address);// 清理
                session.close();
                return;
            }
        }
        tracker.put(address, new Pair(System.currentTimeMillis(), count));
        // 结束 ID 检查
        String IP = address.substring(address.indexOf('/') + 1, address.length());

        if (channel == MapleServerHandler.CASH_SHOP_SERVER) {
            if (CashShopServer.isShutdown()) {
                session.close();
                return;
            }
        } else if (channel == MapleServerHandler.LOGIN_SERVER) {
            if (LoginServer.isShutdown()) {
                session.close();
                return;
            }
        } else if (this.channel > MapleServerHandler.LOGIN_SERVER) {
            if (ChannelServer.getInstance(this.channel).isShutdown()) {
                session.close();
                return;
            }
            if (!LoginServer.containsIPAuth(IP)) {
                session.close();
                return;
            }
        } else {
            MapleLogger.info("[連結錯誤] 未知類型: " + channel);
            session.close();
            return;
        }

        LoginServer.removeIPAuth(IP);
        byte[] ivRecv = {70, 114, 122, 82};
        byte[] ivSend = {82, 48, 120, 115};
        ivRecv[3] = (byte) (int) (Math.random() * 255.0);
        ivSend[3] = (byte) (int) (Math.random() * 255.0);
        MapleAESOFB sendCypher = new MapleAESOFB(ivSend, 0xFFFF - ServerConstants.MAPLE_VERSION, false);
        MapleAESOFB recvCypher = new MapleAESOFB(ivRecv, ServerConstants.MAPLE_VERSION, false);
        MapleClient client = new MapleClient(sendCypher, recvCypher, session.channel());
        client.setChannel(channel);
        MaplePacketDecoder.DecoderState decoderState = new MaplePacketDecoder.DecoderState();
        session.channel().attr(MaplePacketDecoder.DECODER_STATE_KEY).set(decoderState);

        byte[] handShakePacket = LoginPacket.getHello(ServerConstants.MAPLE_VERSION, ivSend, ivRecv);
        session.channel().writeAndFlush(handShakePacket);

        RegisterHandlers();

        MapleLogger.info("[登陆服务] " + address + ", 发送握手包成功！");
        Random r = new Random();
        client.setSessionId(r.nextLong()); // Generates a random session id.  
        session.channel().attr(MapleClient.CLIENT_KEY).set(client);
        World.Client.addClient(client);
    }

    @Override
    public void channelInactive(ChannelHandlerContext session) {
        MapleClient client = session.channel().attr(MapleClient.CLIENT_KEY).get();
        if (client != null) {
//            try {
//                int countRows = ManagerSin.jTable1.getRowCount();//获取当前表格总行数
//                for (int i = 0; i < countRows; i++) {
//                    int AID = (Integer) ManagerSin.jTable1.getValueAt(i, 0);
//                    if (AID == client.getAccID()) {
//                        ((DefaultTableModel) ManagerSin.jTable1.getModel()).removeRow(i);
//                        break;
//                    }
//                }
//            } catch (Exception e) {
//                MapleLogger.error(FileoutputUtil.GUI_Ex_Log, e);
//            }

            try {
                client.disconnect(true, (channel == MapleServerHandler.CASH_SHOP_SERVER));
            } finally {
                World.Client.removeClient(client);
                session.channel().close();
                session.channel().attr(MapleClient.CLIENT_KEY).set(null);
                session.channel().attr(MaplePacketDecoder.DECODER_STATE_KEY).set(null);
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext session, Object status) {
        MapleClient client = session.channel().attr(MapleClient.CLIENT_KEY).get();
        if (client != null) {
            //client.sendPing();
        }
//        super.sessionIdle(session, status);
    }

    @Override
    public void channelRead(ChannelHandlerContext session, Object message) {
        if (message == null || session == null) {
            return;
        }
        SeekableLittleEndianAccessor slea = new GenericSeekableLittleEndianAccessor(new ByteArrayByteStream((byte[]) message));
        if (slea.available() < 1) {
            MapleLogger.info("数据包长度异常：" + slea.toString());
            return;
        }
        MapleClient client = session.channel().attr(MapleClient.CLIENT_KEY).get();
        if (client == null || !client.isReceiving()) {
            return;
        }
        byte packetId = slea.readByte();

        try {
            MaplePacketHandler handler = handlers.get(packetId);
            if (handler == null) {
                MapleLogger.error("unhandler packet:" + slea.toString());
                return ;
            }
            handler.handlePacket(slea, client);
        } catch (Exception e) {
            MapleLogger.error(e.getMessage());
        }


//        for (RecvPacketOpcode recv : RecvPacketOpcode.values()) {
//            if (recv.getValue() == packetId) {
//                if (recv.NeedsChecking() && !client.isLoggedIn()) {
//                    MapleLogger.info("客户端没有登录，丢弃包！");
//                    return;
//                }
//                try {
//                    handlePacket(recv, slea, client);
//                } catch (InterruptedException e) {
//                    MapleLogger.info(FileoutputUtil.Packet_Ex, new StringBuilder().append("封包: ").append(lookupRecv(packetId)).append("\r\n").append(slea.toString(true)).toString());
//                    MapleLogger.error(FileoutputUtil.Packet_Ex, e);
//                }
//                return;
//            }
//        }
    }

    private String lookupRecv(short header) {
        for (RecvPacketOpcode recv : RecvPacketOpcode.values()) {
            if (recv.getValue() == header) {
                return recv.name();
            }
        }
        return "UNKNOWN";
    }
/*
    public static void handlePacket(final RecvPacketOpcode header, final SeekableLittleEndianAccessor slea, final MapleClient c) throws InterruptedException {
        switch (header) {
//            case ENTER:
//                final byte switchs = slea.readByte();
//                if (switchs == 1) {
//                    c.sendPacket(MaplePacketCreator.enableActions());
//                }
//                break;
            case STRANGE_DATA:
                c.sendPacket(MaplePacketCreator.enableActions());
                break;
            case PONG:
                c.pongReceived();
                break;
            case CLIENT_ERROR:
                ClientErrorHandler.handlePacket(slea, c);
                break;
//            case PACKET_ERROR:
//                PacketErrorHandler.handlePacket(slea, c);
//                break;
            case LOGIN_PASSWORD:
                //LoginPasswordHandler.handlePacket(slea, c);
                break;
            case UPDATE_CHANNEL:
                LoginWorker.updateChannel(c);
                break;
            case LICENSE_REQUEST:
                LicenseRequestHandler.handlePacket(slea, c);
                break;
            case SET_GENDER:
                SetGenderHandler.handlePacket(slea, c);
                break;
            case SET_ACC_CASH:
                ShowAccCash.handlePacket(slea, c);
                break;
            case QUICK_BUY_CS_ITEM:
                PlayerHandler.quickBuyCashShopItem(slea, c, c.getPlayer());
                break;
            case VIEW_SERVERLIST:
                if (slea.readByte() == 0) {
                    ServerlistRequestHandler.handlePacket(c, false);
                }
                break;
            case SERVERLIST_REQUEST:
                ServerlistRequestHandler.handlePacket(c, true);
                break;
            case CLIENT_HELLO:
                MapLoginHandler.handlePacket(slea, c);
                break;
            case GET_SERVER:
                c.sendPacket(LoginPacket.getLoginAUTH());
                break;
            case CHARLIST_REQUEST:
                CharlistRequestHandler.handlePacket(slea, c);
                break;
            case SERVERSTATUS_REQUEST:
                ServerStatusRequestHandler.handlePacket(slea, c);
                break;
            case CHECK_REGISTER_ACCOUNT:
                RegisterAccountHandler.handlePacket(slea,c);
                break;
            case CHECK_ACCOUNT:
                RegisterAccountHandler.CheckAccount(slea,c);
                break;
            case REGISTER_ACCOUNT:
                RegisterAccountHandler.RegisterAccount(slea,c);
                break;
            case CHECK_CHAR_NAME:
                CheckCharNameHandler.handlePacket(slea, c);
                break;
            case CREATE_CHAR:
                CreateCharHandler.handlePacket(slea, c);
                break;
            case DELETE_CHAR:
                DeleteCharHandler.handlePacket(slea, c);
                break;
            case VIEW_ALL_CHAR:
                ViewCharHandler.handlePacket(slea, c);
                break;
            case PICK_ALL_CHAR:
                WithoutSecondPasswordHandler.handlePacket(slea, c, false, true);
                break;
            case CHAR_SELECT_NO_PIC:
                WithoutSecondPasswordHandler.handlePacket(slea, c, false, false);
                break;
            case VIEW_REGISTER_PIC:
                WithoutSecondPasswordHandler.handlePacket(slea, c, true, true);
                break;
            case CHAR_SELECT:
                CharSelectHandler.handlePacket(slea, c);
                break;
            case VIEW_SELECT_PIC:
                WithSecondPasswordHandler.handlePacket(slea, c, true);
                break;
            case AUTH_SECOND_PASSWORD:
                WithSecondPasswordHandler.handlePacket(slea, c, false);
                break;
            case CHANGE_CHANNEL:
                InterServerHandler.ChangeChannel(slea, c, c.getPlayer());
                break;
            case PLAYER_LOGGEDIN://TODO 需要获取正确的世界
//                c.setWorld(slea.readInt());
                final int playerid = slea.readInt();
                InterServerHandler.Loggedin(playerid, c);
                break;
            case ENTER_CASH_SHOP:
                InterServerHandler.EnterCS(c, c.getPlayer());
                break;
            case MOVE_PLAYER:
                PlayerHandler.MovePlayer(slea, c, c.getPlayer());
                break;
            case CHAR_INFO_REQUEST:
                PlayerHandler.CharInfoRequest(slea.readInt(), c, c.getPlayer());
                break;
            case DAMAGE_SUMMON:
                SummonHandler.DamageSummon(slea, c.getPlayer());
                break;
            case MOVE_SUMMON:
                SummonHandler.MoveSummon(slea, c.getPlayer());
                break;
            case SUMMON_ATTACK:
            case CLOSE_RANGE_ATTACK:
            case RANGED_ATTACK:
            case MAGIC_ATTACK:
                PlayerHandler.攻击处理(slea, c, header);
                break;
            case SPECIAL_SKILL:
                PlayerHandler.SpecialSkill(slea, c, c.getPlayer());
                break;
            case AFTER_SKILL:
                PlayerHandler.AfterSkill(slea, c, c.getPlayer());
                break;
            case CHANGE_SET:
                PlayersHandler.ChangeSet(slea, c, c.getPlayer());
                break;
            case MAKE_EXTRACTOR:
                ItemMakerHandler.MakeExtractor(slea, c, c.getPlayer());
                break;
            case RENAME_FAMILIAR:
                MobHandler.RenameFamiliar(slea, c, c.getPlayer());
                break;
            case MOVE_FAMILIAR:
                MobHandler.MoveFamiliar(slea, c, c.getPlayer());
                break;
            case ATTACK_FAMILIAR:
                MobHandler.AttackFamiliar(slea, c, c.getPlayer());
                break;
            case TOUCH_FAMILIAR:
                MobHandler.TouchFamiliar(slea, c, c.getPlayer());
                break;
            case USE_RECIPE:
                ItemMakerHandler.UseRecipe(slea, c, c.getPlayer());
                break;
            case FACE_EXPRESSION:
                PlayerHandler.ChangeEmotion(slea.readInt(), c.getPlayer());
                break;
            case TAKE_DAMAGE:
                TakeDamageHandler.TakeDamage(slea, c, c.getPlayer());
                break;
            case HEAL_OVER_TIME:
                PlayerHandler.Heal(slea, c.getPlayer());
                break;
            case CANCEL_BUFF:
                PlayerHandler.CancelBuffHandler(slea.readInt(), c.getPlayer());
                break;
            case MECH_CANCEL:
                PlayerHandler.CancelMech(slea, c.getPlayer());
                break;
            case USE_HOLY_FOUNTAIN:
                PlayersHandler.UseHolyFountain(slea, c, c.getPlayer());
                break;
            case CANCEL_ITEM_EFFECT:
                PlayerHandler.CancelItemEffect(slea.readInt(), c.getPlayer());
                break;
            case USE_CHAIR:
                PlayerHandler.UseChair(slea.readShort(), c, c.getPlayer());
                break;
            case CANCEL_CHAIR:
                PlayerHandler.CancelChair(slea.readShort(), c, c.getPlayer());
                break;
            case USE_ITEM_EFFECT:
                PlayerHandler.UseItemEffect(slea.readInt(), c, c.getPlayer());
                break;
            case USE_TITLE_EFFECT:
                PlayerHandler.UseTitleEffect(slea.readInt(), c, c.getPlayer());
                break;
            case SKILL_EFFECT:
                PlayerHandler.SkillEffect(slea, c.getPlayer());
                break;
            case QUICK_SLOT:
                PlayerHandler.QuickSlot(slea, c.getPlayer());
                break;
            case MESO_DROP:
                PlayerHandler.DropMeso(slea.readInt(), c.getPlayer());
                break;
            case CHANGE_KEYMAP:
                PlayerHandler.ChangeKeymap(slea, c.getPlayer());
                break;
            case CHANGE_MAP:
                //if (type == ServerType.商城服务器) {
                if (c.getPlayer().getMap() == null) {
                    CashShopOperation.LeaveCS(slea, c, c.getPlayer());
                } else {
                    PlayerHandler.ChangeMap(slea, c, c.getPlayer());
                }
                break;
            case CHANGE_MAP_SPECIAL:
                PlayerHandler.ChangeMapSpecial(slea.readMapleAsciiString(), c, c.getPlayer());
                break;
            case USE_INNER_PORTAL:
                slea.skip(1);
                PlayerHandler.InnerPortal(slea, c, c.getPlayer());
                break;
            case TROCK_ADD_MAP:
                PlayerHandler.TrockAddMap(slea, c, c.getPlayer());
                break;
            case SPECIAL_ATTACK:
                PlayerHandler.specialAttack(slea, c, c.getPlayer());
                break;
            case GIVE_FAME:
                PlayersHandler.GiveFame(slea, c, c.getPlayer());
                break;
            case TRANSFORM_PLAYER:
                PlayersHandler.TransformPlayer(slea, c, c.getPlayer());
                break;
            case NOTE_ACTION:
                PlayersHandler.Note(slea, c.getPlayer());
                break;
            case USE_DOOR:
                PlayersHandler.UseDoor(slea, c.getPlayer());
                break;
            case USE_MECH_DOOR:
                PlayersHandler.UseMechDoor(slea, c.getPlayer());
                break;
            case DAMAGE_REACTOR:
                PlayersHandler.HitReactor(slea, c);
                break;
            case CLICK_REACTOR://重新领取勋章
            case TOUCH_REACTOR://双击反应堆
                PlayersHandler.TouchReactor(slea, c);
                break;
            case CLOSE_CHALKBOARD:
                c.getPlayer().setChalkboard(null);
                break;
            case ITEM_MOVE:
                InventoryHandler.ItemMove(slea, c);
                break;
            case MOVE_BAG:
                InventoryHandler.MoveBag(slea, c);
                break;
            case SWITCH_BAG:
                InventoryHandler.SwitchBag(slea, c);
                break;
            case ITEM_PICKUP:
                InventoryHandler.PlayerPickupItem(slea, c, c.getPlayer());
                break;
            case USE_CASH_ITEM:
                UseCashItemHandler.handlePacket(slea, c, c.getPlayer());
                break;
            case USE_ADDITIONAL_ITEM:
                InventoryHandler.UseAdditionalItem(slea, c, c.getPlayer());
                break;
            case USE_ITEM:
                InventoryHandler.UseItem(slea, c, c.getPlayer());
                break;
            case USE_COSMETIC:
                InventoryHandler.UseCosmetic(slea, c, c.getPlayer());
                break;
            case USE_REDUCER:
                InventoryHandler.UseReducer(slea, c, c.getPlayer());
                break;
            case USE_CARVED_SEAL:
                InventoryHandler.UseCarvedSeal(slea, c, c.getPlayer());
                break;
            case USE_SCRIPTED_NPC_ITEM:
                InventoryHandler.UseScriptedNPCItem(slea, c, c.getPlayer());
                break;
            case USE_RETURN_SCROLL:
                InventoryHandler.UseReturnScroll(slea, c, c.getPlayer());
                break;
            case USE_SUMMON_BAG:
                InventoryHandler.UseSummonBag(slea, c, c.getPlayer());
                break;
            case USE_CATCH_ITEM:
                InventoryHandler.UseCatchItem(slea, c, c.getPlayer());
                break;
            case USE_MOUNT_FOOD:
                InventoryHandler.UseMountFood(slea, c, c.getPlayer());
                break;
            case REWARD_ITEM:
                InventoryHandler.UseRewardItem((byte) slea.readShort(), slea.readInt(), c, c.getPlayer());
                break;
            case HYPNOTIZE_DMG:
                MobHandler.HypnotizeDmg(slea, c.getPlayer());
                break;
            case MOB_NODE:
                MobHandler.MobNode(slea, c.getPlayer());
                break;
            case DISPLAY_NODE:
                MobHandler.DisplayNode(slea, c.getPlayer());
                break;
            case MOVE_LIFE:
                MobHandler.MoveMonster(slea, c, c.getPlayer());
                break;
            case AUTO_AGGRO:
                MobHandler.AutoAggro(slea.readInt(), c.getPlayer(), slea);
                break;
            case FRIENDLY_DAMAGE:
                MobHandler.FriendlyDamage(slea, c.getPlayer());
                break;
            case MONSTER_BOMB:
                MobHandler.MonsterBomb(slea.readInt(), c.getPlayer());
                break;
            case MOB_BOMB:
                MobHandler.MobBomb(slea, c.getPlayer());
                break;
            case NPC_SHOP:
                NPCHandler.NPCShop(slea, c, c.getPlayer());
                break;
            case NPC_TALK:
                NPCHandler.NPCTalk(slea, c, c.getPlayer());
                break;
            case NPC_TALK_MORE:
                NPCHandler.NPCMoreTalk(slea, c);
                break;
            case NPC_ACTION:
                NPCHandler.NPCAnimation(slea, c);
                break;
            case QUEST_ACTION:
                NPCHandler.QuestAction(slea, c, c.getPlayer());
                break;
            case REISSUE_MEDAL:
                PlayerHandler.ReIssueMedal(slea, c, c.getPlayer());
                break;
            case STORAGE:
                NPCHandler.Storage(slea, c, c.getPlayer());
                break;
            case GENERAL_CHAT:
                if ((c.getPlayer() == null) || (c.getPlayer().getMap() == null)) {
                    break;
                }
                ChatHandler.GeneralChat(slea.readMapleAsciiString(), (byte)0, c, c.getPlayer());
                break;
            case PARTYCHAT:
                ChatHandler.Others(slea, c, c.getPlayer());
                break;
            case WHISPER:
                ChatHandler.Whisper_Find(slea, c);
                break;
            case MESSENGER:
                ChatHandler.Messenger(slea, c);
                break;
            case SHOW_LOVE_RANK:
                ChatHandler.ShowLoveRank(slea, c);
                break;
            case DISTRIBUTE_AP:
                StatsHandling.DistributeAP(slea, c, c.getPlayer());
                break;
            case DISTRIBUTE_SP:
                StatsHandling.DistributeSP(slea, c, c.getPlayer());
                break;
            case PLAYER_INTERACTION:
                PlayerInteractionHandler.PlayerInteraction(slea, c, c.getPlayer());
                break;
            case GUILD_OPERATION:
                GuildHandler.Guild(slea, c);
                break;
            case DENY_GUILD_REQUEST:
                slea.skip(1);
                GuildHandler.DenyGuildRequest(slea.readMapleAsciiString(), c);
                break;
            case JOIN_GUILD_REQUEST:
                GuildHandler.JoinGuildRequest(slea.readInt(), c);
                break;
            case JOIN_GUILD_CANCEL:
                GuildHandler.JoinGuildCancel(c);
                break;
            case ALLOW_GUILD_JOIN:
                GuildHandler.AddGuildMember(slea, c);
                break;
            case DENY_GUILD_JOIN:
                GuildHandler.DenyGuildJoin(slea, c);
                break;

            case PARTY_OPERATION:
                PartyHandler.PartyOperation(slea, c);
                break;
            case DENY_PARTY_REQUEST:
                PartyHandler.DenyPartyRequest(slea, c);
                break;
            case ALLOW_PARTY_INVITE:
                PartyHandler.AllowPartyInvite(slea, c);
                break;
            case SIDEKICK_OPERATION:
                PartyHandler.SidekickOperation(slea, c);
                break;
            case DENY_SIDEKICK_REQUEST:
                PartyHandler.DenySidekickRequest(slea, c);
                break;
            case BUDDYLIST_MODIFY:
                BuddyListHandler.BuddyOperation(slea, c);
                break;
            case CYGNUS_SUMMON:
                UserInterfaceHandler.CygnusSummon_NPCRequest(c);
                break;
            case SHIP_OBJECT:
                UserInterfaceHandler.ShipObjectRequest(slea.readInt(), c);
                break;
            case BUY_CS_ITEM: // 购买商城道具
                BuyCashItemHandler.BuyCashItem(slea, c, c.getPlayer());
                break;
            case COUPON_CODE:
                CouponCodeHandler.handlePacket(slea, c, c.getPlayer());
                break;
            case CS_UPDATE:
                CashShopOperation.CSUpdate(c);
                break;
            case SEND_CS_GIFI:
                BuyCashItemHandler.商城送礼(slea, c, c.getPlayer());
                break;
            case SUB_SUMMON:
                SummonHandler.SubSummon(slea, c.getPlayer());
                break;
            case REMOVE_SUMMON:
                SummonHandler.RemoveSummon(slea, c);
                break;
            case SPAWN_PET:
                PetHandler.SpawnPet(slea, c, c.getPlayer());
                break;
            case MOVE_PET:
                PetHandler.MovePet(slea, c.getPlayer());
                break;
            case PET_CHAT:
                PetHandler.PetChat(slea, c, c.getPlayer());
                break;
            case PET_COMMAND:
                PetHandler.PetCommand(slea, c, c.getPlayer());
                break;
            case PET_FOOD:
                PetHandler.PetFood(slea, c, c.getPlayer());
                break;
            case PET_LOOT:
                InventoryHandler.Pickup_Pet(slea, c, c.getPlayer());
                break;
            case PET_AUTO_POT:
                PetHandler.Pet_AutoPotion(slea, c, c.getPlayer());
                break;
            case PET_EXCEPTION_LIST:
                PetHandler.PetExcludeItems(slea, c, c.getPlayer());
                break;
            case PET_AOTO_EAT:
                slea.skip(4);
                PetHandler.PetFood(slea, c, c.getPlayer());
                break;
            case ALLOW_PET_LOOT:
                PetHandler.AllowPetLoot(slea, c, c.getPlayer());
                break;
            case ALLOW_PET_AOTO_EAT:
                PetHandler.AllowPetAutoEat(slea, c, c.getPlayer());
                break;
            case USE_HIRED_MERCHANT:
                HiredMerchantHandler.UseHiredMerchant(c, true);
                break;
            case MERCH_ITEM_STORE:
                HiredMerchantHandler.MerchantItemStore(slea, c);
                break;
            case CANCEL_DEBUFF:
                break;
            case MAPLETV:
                break;
            case LEFT_KNOCK_BACK:
                PlayerHandler.leftKnockBack(slea, c);
                break;
            case SNOWBALL:
                PlayerHandler.snowBall(slea, c);
                break;
            case COCONUT:
                PlayersHandler.hitCoconut(slea, c);
                break;
            case GAME_POLL:
                UserInterfaceHandler.InGame_Poll(slea, c);
                break;
            case OWL:
                InventoryHandler.Owl(slea, c);
                break;
            case OWL_WARP:
                InventoryHandler.OwlWarp(slea, c);
                break;
            case RPS_GAME:
                NPCHandler.RPSGame(slea, c);
                break;
            case UPDATE_QUEST:
                NPCHandler.UpdateQuest(slea, c);
                break;
            case USE_ITEM_QUEST:
                NPCHandler.UseItemQuest(slea, c);
                break;
            case RING_ACTION:
                PlayersHandler.RingAction(slea, c);
                break;
            case SOLOMON:
                PlayersHandler.Solomon(slea, c);
                break;
            case GACH_EXP:
                PlayersHandler.GachExp(slea, c);
                break;
            case PARTY_MEMBER_SEARCH:
                PartyHandler.MemberSearch(slea, c);
                break;
            case PARTY_SEARCH:
                PartyHandler.PartySearch(slea, c);
                break;
            case USE_TELE_ROCK:
                InventoryHandler.TeleRock(slea, c);
                break;
            case PAM_SONG:
                InventoryHandler.PamSong(slea, c);
                break;
            case REMOTE_STORE:
                HiredMerchantHandler.RemoteStore(slea, c);
                break;

            case LOAD_PLAYER_SCCUCESS:
                PlayerHandler.LoadPlayerSuccess(c, c.getPlayer());
                break;
            case PLAYER_UPDATE:
                PlayerHandler.PlayerUpdate(c.getPlayer());
                break;
            case CHANGE_MARKET_MAP:
                PlayerHandler.ChangeMarketMap(slea, c, c.getPlayer());
                break;
            case SET_CHAR_CASH:
                PlayerHandler.showPlayerCash(slea, c);
                break;
            case OPEN_ROOT_NPC:
                NPCScriptManager.getInstance().dispose(c);
//                NPCScriptManager.getInstance().start(c, 1064026, 1);
                break;
            case GETMONOID:
                //PlayerHandler.getMonoid(slea, c.getPlayer());
                break;
            case UPDATE_MAC_SKILL:
                PlayerHandler.UpdateMacrSkill(slea, c.getPlayer());
                break;
            case OPEN_MAP:
                c.sendPacket(UIPacket.openMap());
                break;
            default:
                MapleLogger.info(new StringBuilder().append("[未处理封包] Recv ").append(header.toString()).append(" [").append(HexTool.getOpcodeToString(header.getValue())).append("]").toString());
                break;
        }
    }*/
}
