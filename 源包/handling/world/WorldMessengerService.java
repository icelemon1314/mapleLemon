package handling.world;

import client.MapleCharacter;
import handling.channel.ChannelServer;
import handling.world.messenger.MapleMessenger;
import handling.world.messenger.MapleMessengerCharacter;
import handling.world.messenger.MessengerType;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import tools.packet.MessengerPacket;

public class WorldMessengerService {

    private final Map<Integer, MapleMessenger> messengers;
    private final AtomicInteger runningMessengerId;

    public static WorldMessengerService getInstance() {
        return SingletonHolder.instance;
    }

    private WorldMessengerService() {
        this.runningMessengerId = new AtomicInteger(1);
        this.messengers = new HashMap();
    }

    public MapleMessenger createMessenger(MapleMessengerCharacter chrfor, MessengerType type, boolean gm) {
        int messengerid = this.runningMessengerId.getAndIncrement();
        MapleMessenger messenger = new MapleMessenger(messengerid, chrfor, type, gm);
        this.messengers.put(messenger.getId(), messenger);
        return messenger;
    }

    public void declineChat(String target, String nameFrom) {
        MapleCharacter player = WorldFindService.getInstance().findCharacterByName(target);
        if ((player != null)
                && (player.getMessenger() != null)) {
            player.getClient().getSession().write(MessengerPacket.messengerNote(nameFrom, 5, 0));
        }
    }

    public MapleMessenger getMessenger(int messengerId) {
        return (MapleMessenger) this.messengers.get(messengerId);
    }

    public MapleMessenger getRandomMessenger(MessengerType type) {
        for (Map.Entry ms : this.messengers.entrySet()) {
            MapleMessenger messenger = (MapleMessenger) ms.getValue();
            if ((messenger != null) && (messenger.getType() == type) && (messenger.getLowestPosition() != -1) && (!messenger.isHide())) {
                return messenger;
            }
        }
        return null;
    }

    public MapleMessenger getRandomHideMessenger(MessengerType type) {
        for (Map.Entry ms : this.messengers.entrySet()) {
            MapleMessenger messenger = (MapleMessenger) ms.getValue();
            if ((messenger != null) && (messenger.getType() == type) && (messenger.getLowestPosition() != -1) && (messenger.isHide())) {
                return messenger;
            }
        }
        return null;
    }

    public void leaveMessenger(int messengerId, MapleMessengerCharacter target) {
        MapleMessenger messenger = getMessenger(messengerId);
        if (messenger == null) {
            throw new IllegalArgumentException("No messenger with the specified messengerid exists");
        }
          int position = messenger.getPositionByName(target.getName());
          messenger.removeMember(target);
          for (MapleMessengerCharacter mmc : messenger.getMembers())   {
            if (mmc != null) {
                  MapleCharacter player = WorldFindService.getInstance().findCharacterById(mmc.getId());
                  if (player != null)   {
                    player.getClient().getSession().write(MessengerPacket.removeMessengerPlayer(position));
                }
            }
        }
    }

    public void silentLeaveMessenger(int messengerId, MapleMessengerCharacter target) {
          MapleMessenger messenger = getMessenger(messengerId);
          if (messenger == null) {
              throw new IllegalArgumentException("No messenger with the specified messengerid exists");
        }
          messenger.silentRemoveMember(target);
    }

    public void silentJoinMessenger(int messengerId, MapleMessengerCharacter target) {
          MapleMessenger messenger = getMessenger(messengerId);
          if (messenger == null) {
              throw new IllegalArgumentException("No messenger with the specified messengerid exists");
        }
          messenger.silentAddMember(target);
    }

    public void updateMessenger(int messengerId, String nameFrom, int fromChannel) {
          MapleMessenger messenger = getMessenger(messengerId);
          if (messenger == null) {
              throw new IllegalArgumentException("No messenger with the specified messengerid exists");
        }
          int position = messenger.getPositionByName(nameFrom);
          for (MapleMessengerCharacter mmc : messenger.getMembers())   {
            if ((mmc != null) && (!mmc.getName().equals(nameFrom))) {
                  MapleCharacter player = WorldFindService.getInstance().findCharacterByName(mmc.getName());
                  if (player != null) {
                      MapleCharacter fromplayer = ChannelServer.getInstance(fromChannel).getPlayerStorage().getCharacterByName(nameFrom);
                      if (fromplayer != null)   {
                        player.getClient().getSession().write(MessengerPacket.updateMessengerPlayer(nameFrom, fromplayer, position, fromChannel - 1));
                    }
                }
            }
        }
    }

    public void joinMessenger(int messengerId, MapleMessengerCharacter target, String from, int fromChannel) {
          MapleMessenger messenger = getMessenger(messengerId);
          if (messenger == null) {
              throw new IllegalArgumentException("No messenger with the specified messengerid exists");
        }
          messenger.addMember(target);
          int position = messenger.getPositionByName(target.getName());
          for (MapleMessengerCharacter mmc : messenger.getMembers())   {
            if (mmc != null) {
                  int mposition = messenger.getPositionByName(mmc.getName());
                  MapleCharacter player = WorldFindService.getInstance().findCharacterByName(mmc.getName());
                  if (player != null)   {
                    if (!mmc.getName().equals(from)) {
                          MapleCharacter fromplayer = ChannelServer.getInstance(fromChannel).getPlayerStorage().getCharacterByName(from);
                          if (fromplayer != null) {
                              player.getClient().getSession().write(MessengerPacket.addMessengerPlayer(from, fromplayer, position, fromChannel - 1));
                              fromplayer.getClient().getSession().write(MessengerPacket.addMessengerPlayer(player.getName(), player, mposition, mmc.getChannel() - 1));
                        }
                    } else {
                          player.getClient().getSession().write(MessengerPacket.joinMessenger(mposition));
                    }
                }
            }
        }
    }

    public void messengerChat(int messengerId, String chatText, String namefrom, String postxt) {
          MapleMessenger messenger = getMessenger(messengerId);
          if (messenger == null) {
              throw new IllegalArgumentException("No messenger with the specified messengerid exists");
        }
          for (MapleMessengerCharacter mmc : messenger.getMembers())   {
            if ((mmc != null) && (!mmc.getName().equals(namefrom))) {
                  MapleCharacter player = WorldFindService.getInstance().findCharacterByName(mmc.getName());
                  if (player != null)   {
                    player.getClient().getSession().write(MessengerPacket.messengerChat(chatText, postxt));
                }
            }
        }
    }

    public void messengerWhisper(int messengerId, String chatText, String namefrom, int position) {
          MapleMessenger messenger = getMessenger(messengerId);
          if (messenger == null) {
              throw new IllegalArgumentException("No messenger with the specified messengerid exists");
        }
          MapleMessengerCharacter mmc = messenger.getMemberByPos(position);
          if ((mmc != null) && (!mmc.getName().equals(namefrom))) {
              MapleCharacter player = WorldFindService.getInstance().findCharacterByName(mmc.getName());
              if (player != null)   {
                player.getClient().getSession().write(MessengerPacket.messengerWhisper(namefrom, chatText));
            }
        }
    }

    public void messengerInvite(String sender, int messengerId, String target, int fromChannel, boolean gm) {
          MapleCharacter fromplayer = ChannelServer.getInstance(fromChannel).getPlayerStorage().getCharacterByName(sender);
          MapleCharacter targetplayer = WorldFindService.getInstance().findCharacterByName(target);
          if ((targetplayer != null) && (fromplayer != null))   {
            if ((!targetplayer.isIntern()) || (gm)) {
                  targetplayer.getClient().getSession().write(MessengerPacket.messengerInvite(sender, messengerId, fromChannel - 1));
                  fromplayer.getClient().getSession().write(MessengerPacket.messengerNote(target, 4, 1));
            } else {
                  fromplayer.getClient().getSession().write(MessengerPacket.messengerNote(target, 4, 0));
            }
        }
    }

    private static class SingletonHolder {
          protected static final WorldMessengerService instance = new WorldMessengerService();
    }
}


