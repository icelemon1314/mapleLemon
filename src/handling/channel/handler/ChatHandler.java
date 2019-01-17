package handling.channel.handler;

import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import client.messages.CommandProcessor;
import client.messages.CommandType;
import handling.channel.ChannelServer;
import handling.world.World;
import handling.world.WorldBroadcastService;
import handling.world.WorldBuddyService;
import handling.world.WorldFindService;
import handling.world.WorldGuildService;
import handling.world.WorldMessengerService;
import handling.world.WrodlPartyService;
import handling.world.messenger.MapleMessenger;
import handling.world.messenger.MapleMessengerCharacter;
import handling.world.messenger.MessengerType;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.StringUtil;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.MessengerPacket;
import tools.packet.WhisperPacket;

public class ChatHandler {

    public static void GeneralChat(String text, byte unk, MapleClient c, MapleCharacter chr) {

    }

    public static void Others(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        int type = slea.readByte();
        byte numRecipients = slea.readByte();
        if (numRecipients <= 0) {
            return;
        }
        int[] recipients = new int[numRecipients];

        for (byte i = 0; i < numRecipients; i = (byte) (i + 1)) {
            recipients[i] = slea.readInt();
        }
        String chattext = slea.readMapleAsciiString();
        if ((chr == null) || (!chr.getCanTalk())) {
            c.sendPacket(MaplePacketCreator.serverMessagePopUp("你被禁言了，暂时不能聊天！"));
            return;
        }
        if (c.isMonitored()) {
            String chattype = "未知";
            switch (type) {
                case 0:
                    chattype = "好友";
                    break;
                case 1:
                    chattype = "组队";
                    break;
                case 2:
                    chattype = "家族";
                    break;
                case 3:
                    chattype = "联盟";
                    break;
                case 4:
                    chattype = "远征";
            }

            WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverMessageNotice(new StringBuilder().append("[GM 信息] ").append(MapleCharacterUtil.makeMapleReadable(chr.getName())).append(" 在 (").append(chattype).append(") 中说: ").append(chattext).toString()));
        }

        if (chattext.length() <= 0 || CommandProcessor.processCommand(c, chattext,CommandType.NORMAL)) {
            return;
        }

        switch (type) {
            case 0:
                WorldBuddyService.getInstance().buddyChat(recipients, chr.getId(), chr.getName(), chattext);
                break;
            case 1:
                if (chr.getParty() != null) {
                    WrodlPartyService.getInstance().partyChat(chr.getParty().getId(), chattext, chr.getName());
                }
                break;
            case 2:
                if (chr.getGuildId() > 0) {
                    WorldGuildService.getInstance().guildChat(chr.getGuildId(), chr.getName(), chr.getId(), chattext);
                }
                break;
            case 4:
                if ((chr.getParty() != null) && (chr.getParty().getExpeditionId() > 0)) {
                    WrodlPartyService.getInstance().expedChat(chr.getParty().getExpeditionId(), chattext, chr.getName());
                }
                break;
        }
    }

    public static void Messenger(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleMessenger messenger = c.getPlayer().getMessenger();
        WorldMessengerService messengerService = WorldMessengerService.getInstance();
        int action = slea.readByte();
        switch (action) {
            case 0:
                if (messenger != null) {
                    MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(c.getPlayer());
                    messengerService.leaveMessenger(messenger.getId(), messengerplayer);
                    c.getPlayer().setMessenger(null);
                }
                int mode = slea.readByte();
                int maxMembers = slea.readByte();
                int messengerId = slea.readInt();

                if (messengerId == 0) {
                    MapleMessengerCharacter messengerPlayer = new MapleMessengerCharacter(c.getPlayer());
                    MessengerType type = MessengerType.getMessengerType(maxMembers, mode != 0);
                    if (type == null) {
                        return;
                    }
                    if (mode == 0) {
                        c.getPlayer().setMessenger(messengerService.createMessenger(messengerPlayer, type, c.getPlayer().isIntern()));
                    } else if (mode == 1) {
                        messenger = c.getPlayer().isIntern() ? messengerService.getRandomHideMessenger(type) : messengerService.getRandomMessenger(type);
                        if (messenger != null) {
                            int position = messenger.getLowestPosition();
                            if (position != -1) {
                                c.getPlayer().setMessenger(messenger);
                                messengerService.joinMessenger(messenger.getId(), new MapleMessengerCharacter(c.getPlayer()), c.getPlayer().getName(), c.getChannel());
                            }
                        } else {
                            c.getPlayer().setMessenger(messengerService.createMessenger(messengerPlayer, type, c.getPlayer().isIntern()));
                            c.sendPacket(MessengerPacket.joinMessenger(255));
                        }
                    }
                } else {
                    messenger = messengerService.getMessenger(messengerId);
                    if (messenger == null) {
                        break;
                    }
                    int position = messenger.getLowestPosition();
                    if (position != -1) {
                        c.getPlayer().setMessenger(messenger);
                        messengerService.joinMessenger(messenger.getId(), new MapleMessengerCharacter(c.getPlayer()), c.getPlayer().getName(), c.getChannel());
                    }
                }
                break;
            case 2:
                if (messenger == null) {
                    break;
                }
                MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(c.getPlayer());
                messengerService.leaveMessenger(messenger.getId(), messengerplayer);
                c.getPlayer().setMessenger(null);
                break;
            case 3:
                if (messenger == null) {
                    break;
                }
                int position = messenger.getLowestPosition();
                if (position == -1) {
                    return;
                }
                String input = slea.readMapleAsciiString();
                MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(input);
                if (target != null) {
                    if ((!target.isIntern()) || (c.getPlayer().isIntern())) {
                        c.sendPacket(MessengerPacket.messengerNote(input, 4, 1));
                        target.getClient().sendPacket(MessengerPacket.messengerInvite(c.getPlayer().getName(), messenger.getId(), c.getChannel() - 1));
                    } else {
                        c.sendPacket(MessengerPacket.messengerNote(input, 4, 1));
                    }
                } else if (World.isConnected(input)) {
                    messengerService.messengerInvite(c.getPlayer().getName(), messenger.getId(), input, c.getChannel(), c.getPlayer().isIntern());
                } else {
                    c.sendPacket(MessengerPacket.messengerNote(input, 4, 0));
                }

                break;
            case 5:
                String targeted = slea.readMapleAsciiString();
                target = c.getChannelServer().getPlayerStorage().getCharacterByName(targeted);
                if (target != null) {
                    if (target.getMessenger() == null) {
                        break;
                    }
                    target.getClient().sendPacket(MessengerPacket.messengerNote(c.getPlayer().getName(), 5, 0));
                } else {
                    if (c.getPlayer().isIntern()) {
                        break;
                    }
                    messengerService.declineChat(targeted, c.getPlayer().getName());
                }
                break;
            case 6:
                if (messenger == null) {
                    break;
                }
                String chattext = slea.readMapleAsciiString();
                position = 0;//这里看看
                if (slea.available() > 0) {
                    position = Integer.parseInt(slea.readMapleAsciiString());
                }

                messengerService.messengerChat(messenger.getId(), chattext, c.getPlayer().getName(), String.valueOf(position));
                if ((messenger.isMonitored()) && (chattext.length() > c.getPlayer().getName().length() + 3)) {
                    WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverMessageNotice(new StringBuilder().append("[GM 信息] ").append(MapleCharacterUtil.makeMapleReadable(c.getPlayer().getName())).append("(Messenger: ").append(messenger.getMemberNamesDEBUG()).append(") said: ").append(chattext).toString()));
                }
                break;
            case 9:
                if (messenger == null) {
                    break;
                }
                String name = slea.readMapleAsciiString();
                if (!messenger.getType().random) {
                    return;
                }
                MapleCharacter targetPlayer = WorldFindService.getInstance().findCharacterByName(name);
                if ((targetPlayer != null) && (targetPlayer.getId() != c.getPlayer().getId()) && (targetPlayer.getMessenger() != null) && (targetPlayer.getMessenger().getId() == messenger.getId())) {
                    switch (c.getPlayer().canGiveLove(targetPlayer)) {
                        case 0:
                            if (Math.abs(targetPlayer.getLove() + 1) <= 99999) {
                                targetPlayer.addLove(1);
                                targetPlayer.getClient().sendPacket(MessengerPacket.updateLove(targetPlayer.getLove()));
                            }
                            c.getPlayer().hasGiveLove(targetPlayer);
                            c.sendPacket(MessengerPacket.giveLoveResponse(0, c.getPlayer().getName(), targetPlayer.getName()));
                            targetPlayer.getClient().sendPacket(MessengerPacket.giveLoveResponse(0, c.getPlayer().getName(), targetPlayer.getName()));
                            break;
                        case 1:
                            c.sendPacket(MessengerPacket.giveLoveResponse(1, c.getPlayer().getName(), targetPlayer.getName()));
                            break;
                        case 2:
                            c.sendPacket(MessengerPacket.giveLoveResponse(2, c.getPlayer().getName(), targetPlayer.getName()));
                    }
                }

                break;
            case 11:
                if (messenger == null) {
                    break;
                }
                name = slea.readMapleAsciiString();
                MapleCharacter player = WorldFindService.getInstance().findCharacterByName(name);
                if (player != null) {
                    if ((player.getMessenger() != null) && (player.getMessenger().getId() == messenger.getId())) {
                        c.sendPacket(MessengerPacket.messengerPlayerInfo(player));
                    }
                } else {
                    c.sendPacket(MessengerPacket.messengerNote(name, 4, 0));
                }
                break;
            case 14:
                if (messenger == null) {
                    break;
                }
                String namefrom = slea.readMapleAsciiString();
                chattext = slea.readMapleAsciiString();
                position = slea.readByte();
                messengerService.messengerWhisper(messenger.getId(), chattext, namefrom, position);
                break;
            case 15:
                break;
            default:
                FileoutputUtil.log(new StringBuilder().append("聊天招待操作( 0x").append(StringUtil.getLeftPaddedStr(Integer.toHexString(action).toUpperCase(), '0', 2)).append(" ) 未知.").toString());
        }
    }

    public static void Whisper_Find(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte mode = slea.readByte();
        slea.readInt();
        switch (mode) {
            case 5:
            case 68:
                String recipient = slea.readMapleAsciiString();
                MapleCharacter player = c.getChannelServer().getPlayerStorage().getCharacterByName(recipient);
                if (player != null) {
                    if ((!player.isIntern()) || ((c.getPlayer().isIntern()) && (player.isIntern()))) {
                        c.sendPacket(WhisperPacket.getFindReplyWithMap(player.getName(), player.getMap().getId(), mode == 68));
                    } else {
                        c.sendPacket(WhisperPacket.getWhisperReply(recipient, (byte) 0));
                    }
                } else {
                    int ch = WorldFindService.getInstance().findChannel(recipient);
                    if (ch > 0) {
                        player = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(recipient);
                        if (player == null) {
                            break;
                        }
                        if ((!player.isIntern()) || ((c.getPlayer().isIntern()) && (player.isIntern()))) {
                            c.sendPacket(WhisperPacket.getFindReply(recipient, (byte) ch, mode == 68));
                        } else {
                            c.sendPacket(WhisperPacket.getWhisperReply(recipient, (byte) 0));
                        }
                    } else if (ch == -10) {
                        c.sendPacket(WhisperPacket.getFindReplyWithCS(recipient, mode == 68));
                    } else if (ch == -20) {
                        c.getPlayer().dropMessage(5, new StringBuilder().append("'").append(recipient).append("' is at the MTS.").toString());
                    } else {
                        c.sendPacket(WhisperPacket.getWhisperReply(recipient, (byte) 0));
                    }
                }
                break;
            case 6:
                if ((c.getPlayer() == null) || (c.getPlayer().getMap() == null)) {
                    return;
                }
                if (!c.getPlayer().getCanTalk()) {
                    c.sendPacket(MaplePacketCreator.serverMessagePopUp("You have been muted and are therefore unable to talk."));
                    return;
                }
                recipient = slea.readMapleAsciiString();
                String text = slea.readMapleAsciiString();
                int ch = WorldFindService.getInstance().findChannel(recipient);
                if (ch > 0) {
                    player = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(recipient);
                    if (player == null) {
                        break;
                    }
                    player.getClient().sendPacket(WhisperPacket.getWhisper(c.getPlayer().getName(), c.getChannel(), text));
                    if ((!c.getPlayer().isIntern()) && (player.isIntern())) {
                        c.sendPacket(WhisperPacket.getWhisperReply(recipient, (byte) 0));
                    } else {
                        c.sendPacket(WhisperPacket.getWhisperReply(recipient, (byte) 1));
                    }
                    if (c.isMonitored()) {
                        WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverMessageRedText(new StringBuilder().append(c.getPlayer().getName()).append(" whispered ").append(recipient).append(" : ").append(text).toString()));
                    } else if (player.getClient().isMonitored()) {
                        WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverMessageRedText(new StringBuilder().append(c.getPlayer().getName()).append(" whispered ").append(recipient).append(" : ").append(text).toString()));
                    }
                } else {
                    c.sendPacket(WhisperPacket.getWhisperReply(recipient, (byte) 0));
                }
        }
    }

    public static void ShowLoveRank(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte mode = slea.readByte();
        switch (mode) {
            case 7:
                c.sendPacket(MessengerPacket.showLoveRank(7));
                break;
            case 8:
        }
    }
}
