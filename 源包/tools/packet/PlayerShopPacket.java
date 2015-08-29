package tools.packet;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import handling.InteractionOpcode;
import handling.SendPacketOpcode;
import java.util.List;
import java.util.Map;
import server.MerchItemPackage;
import server.shops.AbstractPlayerStore;
import server.shops.HiredMerchant;
import server.shops.IMaplePlayerShop;
import server.shops.MapleMiniGame;
import server.shops.MaplePlayerShop;
import server.shops.MaplePlayerShopItem;
import tools.Pair;
import tools.data.output.MaplePacketLittleEndianWriter;

public class PlayerShopPacket {

    public static byte[] sendTitleBox(int message) {
        return sendTitleBox(message, 0, 0);
    }

    public static byte[] sendTitleBox(int message, int mapId, int ch) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SEND_TITLE_BOX.getValue());
        mplew.write(message);
        switch (message) {
            case 7:
            case 9:
            case 15:
                break;
            case 8:
            case 16:
                mplew.writeInt(mapId);
                mplew.write(ch);
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
        }
        return mplew.getPacket();
    }

    public static byte[] sendPlayerShopBox(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(chr.getId());
        PacketHelper.addAnnounceBox(mplew, chr);

        return mplew.getPacket();
    }

    public static byte[] getHiredMerch(MapleCharacter chr, HiredMerchant merch, boolean firstTime) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(InteractionOpcode.房间.getValue());
        mplew.write(6);
        mplew.write(merch.getMaxSize());
        mplew.writeShort((short) merch.getVisitorSlot(chr));
        mplew.writeInt(merch.getItemId());
        mplew.writeMapleAsciiString("雇佣商人");
        for (Pair storechr : merch.getVisitors()) {
            mplew.write(((Byte) storechr.left));
            PacketHelper.addCharLook(mplew, (MapleCharacter) storechr.right, true, ((MapleCharacter) storechr.right).isZeroSecondLook());
            mplew.writeMapleAsciiString(((MapleCharacter) storechr.right).getName());
            mplew.writeShort(((MapleCharacter) storechr.right).getJob());
        }
        mplew.write(-1);
        mplew.writeShort(merch.isOwner(chr) ? merch.getMessages().size() : 0);
        if (merch.isOwner(chr)) {
            for (int i = 0; i < merch.getMessages().size(); i++) {
                mplew.writeMapleAsciiString((String) ((Pair) merch.getMessages().get(i)).getLeft());
                mplew.write(((Byte) ((Pair) merch.getMessages().get(i)).getRight()));
            }
        }
        mplew.writeMapleAsciiString(merch.getOwnerName());
        if (merch.isOwner(chr)) {
            mplew.writeInt(merch.getTimeLeft(firstTime));
            mplew.write(firstTime ? 1 : 0);
            mplew.write(merch.getBoughtItems().size());
            for (AbstractPlayerStore.BoughtItem SoldItem : merch.getBoughtItems()) {
                mplew.writeInt(SoldItem.id);
                mplew.writeShort(SoldItem.quantity);
                mplew.writeLong(SoldItem.totalPrice);
                mplew.writeMapleAsciiString(SoldItem.buyer);
            }
            mplew.writeLong(merch.getMeso());
        }
        mplew.writeInt(merch.getObjectId());
        mplew.writeMapleAsciiString(merch.getDescription());
        mplew.write(16);
        mplew.writeLong(merch.getMeso());
        mplew.write(merch.getItems().size());
        for (MaplePlayerShopItem item : merch.getItems()) {
            mplew.writeShort(item.bundles);
            mplew.writeShort(item.item.getQuantity());
            mplew.writeLong(item.price);
            PacketHelper.addItemInfo(mplew, item.item);
        }
        mplew.writeShort(0);

        return mplew.getPacket();
    }

    public static byte[] getPlayerStore(MapleCharacter chr, boolean firstTime) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        IMaplePlayerShop ips = chr.getPlayerShop();
        mplew.write(5);
        switch (ips.getShopType()) {
            case 2:
                mplew.write(4);
                mplew.write(4);
                break;
            case 3:
                mplew.write(2);
                mplew.write(2);
                break;
            case 4:
                mplew.write(1);
                mplew.write(2);
        }

        mplew.writeShort((short) ips.getVisitorSlot(chr));
        PacketHelper.addCharLook(mplew, ((MaplePlayerShop) ips).getMCOwner(), false, ((MaplePlayerShop) ips).getMCOwner().isZeroSecondLook());
        mplew.writeMapleAsciiString(ips.getOwnerName());
        mplew.writeShort(((MaplePlayerShop) ips).getMCOwner().getJob());
        for (Pair storechr : ips.getVisitors()) {
            mplew.write(((Byte) storechr.left));
            PacketHelper.addCharLook(mplew, (MapleCharacter) storechr.right, false, ((MapleCharacter) storechr.right).isZeroSecondLook());
            mplew.writeMapleAsciiString(((MapleCharacter) storechr.right).getName());
            mplew.writeShort(((MapleCharacter) storechr.right).getJob());
        }
        mplew.write(255);
        mplew.writeMapleAsciiString(ips.getDescription());
        mplew.write(10);
        mplew.write(ips.getItems().size());
        for (MaplePlayerShopItem item : ips.getItems()) {
            mplew.writeShort(item.bundles);
            mplew.writeShort(item.item.getQuantity());
            mplew.writeInt(item.price);
            PacketHelper.addItemInfo(mplew, item.item);
        }
        return mplew.getPacket();
    }

    public static byte[] shopChat(String message, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(InteractionOpcode.聊天.getValue());
        mplew.write(InteractionOpcode.聊天事件.getValue());
        mplew.write(slot);
        mplew.writeMapleAsciiString(message);

        return mplew.getPacket();
    }

    public static byte[] shopErrorMessage(int error, int type) {
        return shopErrorMessage(false, error, type);
    }

    public static byte[] shopErrorMessage(boolean room, int error, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(room ? InteractionOpcode.房间.getValue() : InteractionOpcode.退出.getValue());
        mplew.write(type);
        mplew.write(error);

        return mplew.getPacket();
    }

    public static byte[] spawnHiredMerchant(HiredMerchant hm) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SPAWN_HIRED_MERCHANT.getValue());
        mplew.writeInt(hm.getOwnerId());
        mplew.writeInt(hm.getItemId());
        mplew.writePos(hm.getTruePosition());
        mplew.writeShort(0);
        mplew.writeMapleAsciiString(hm.getOwnerName());
        PacketHelper.addInteraction(mplew, hm);

        return mplew.getPacket();
    }

    public static byte[] destroyHiredMerchant(int id) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.DESTROY_HIRED_MERCHANT.getValue());
        mplew.writeInt(id);

        return mplew.getPacket();
    }

    public static byte[] shopItemUpdate(IMaplePlayerShop shop) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(InteractionOpcode.雇佣商店_更新信息.getValue());
        if (shop.getShopType() == 1) {
            mplew.writeLong(shop.getMeso());
        }
        mplew.write(shop.getItems().size());
        for (MaplePlayerShopItem item : shop.getItems()) {
            mplew.writeShort(item.bundles);
            mplew.writeShort(item.item.getQuantity());
            mplew.writeLong(item.price);
            PacketHelper.addItemInfo(mplew, item.item);
        }
        mplew.writeShort(0);

        return mplew.getPacket();
    }

    public static byte[] shopVisitorAdd(MapleCharacter chr, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(InteractionOpcode.访问.getValue());
        mplew.write(slot);
        PacketHelper.addCharLook(mplew, chr, false, chr.isZeroSecondLook());
        mplew.writeMapleAsciiString(chr.getName());
        mplew.writeShort(chr.getJob());

        return mplew.getPacket();
    }

    public static byte[] shopVisitorLeave(byte slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(InteractionOpcode.退出.getValue());
        mplew.write(slot);

        return mplew.getPacket();
    }

    public static byte[] Merchant_Buy_Error(byte message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());

        mplew.write(InteractionOpcode.雇佣商店_错误提示.getValue());

        mplew.write(message);

        return mplew.getPacket();
    }

    public static byte[] updateHiredMerchant(HiredMerchant shop) {
        return updateHiredMerchant(shop, true);
    }

    public static byte[] updateHiredMerchant(HiredMerchant shop, boolean update) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(update ? SendPacketOpcode.UPDATE_HIRED_MERCHANT.getValue() : SendPacketOpcode.CHANGE_HIRED_MERCHANT_NAME.getValue());
        mplew.writeInt(shop.getOwnerId());
        PacketHelper.addInteraction(mplew, shop);

        return mplew.getPacket();
    }

    public static byte[] hiredMerchantOwnerLeave() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(InteractionOpcode.雇佣商店_关闭完成.getValue());
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] merchItem_Message(byte op) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MERCH_ITEM_MSG.getValue());
        mplew.write(op);

        return mplew.getPacket();
    }

    public static byte[] merchItemStore(byte op) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.MERCH_ITEM_STORE.getValue());

        mplew.write(op);
        switch (op) {
            case 42:
                mplew.writeInt(0);
                mplew.writeLong(0L);
                break;
            default:
                mplew.write(0);
        }

        return mplew.getPacket();
    }

    public static byte[] merchItemStore(int mapId, int ch) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MERCH_ITEM_STORE.getValue());

        mplew.write(43);
        mplew.writeInt(9030000);
        mplew.writeInt(mapId);
        mplew.write(mapId != 999999999 ? ch : 0);

        return mplew.getPacket();
    }

    public static byte[] merchItemStore_ItemData(MerchItemPackage pack) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MERCH_ITEM_STORE.getValue());

        mplew.write(41);
        mplew.writeInt(9030000);
        mplew.writeInt(32272);
        mplew.writeZero(5);
        mplew.writeLong(pack.getMesos());
        mplew.write(0);
        mplew.write(pack.getItems().size());
        for (Item item : pack.getItems()) {
            PacketHelper.addItemInfo(mplew, item);
        }
        mplew.writeZero(3);

        return mplew.getPacket();
    }

    public static byte[] getMiniGame(MapleClient c, MapleMiniGame minigame) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(5);
        mplew.write(minigame.getGameType());
        mplew.write(minigame.getMaxSize());
        mplew.writeShort((short) minigame.getVisitorSlot(c.getPlayer()));
        PacketHelper.addCharLook(mplew, minigame.getMCOwner(), false, false);
        mplew.writeMapleAsciiString(minigame.getOwnerName());
        mplew.writeShort(minigame.getMCOwner().getJob());
        for (Pair visitorz : minigame.getVisitors()) {
            mplew.write(((Byte) visitorz.getLeft()));
            PacketHelper.addCharLook(mplew, (MapleCharacter) visitorz.getRight(), false, false);
            mplew.writeMapleAsciiString(((MapleCharacter) visitorz.getRight()).getName());
            mplew.writeShort(((MapleCharacter) visitorz.getRight()).getJob());
        }
        mplew.write(-1);
        mplew.write(0);
        addGameInfo(mplew, minigame.getMCOwner(), minigame);
        for (Pair visitorz : minigame.getVisitors()) {
            mplew.write(((Byte) visitorz.getLeft()));
            addGameInfo(mplew, (MapleCharacter) visitorz.getRight(), minigame);
        }
        mplew.write(-1);
        mplew.writeMapleAsciiString(minigame.getDescription());
        mplew.writeShort(minigame.getPieceType());
        return mplew.getPacket();
    }

    public static byte[] getMiniGameReady(boolean ready) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(ready ? 56 : 57);
        return mplew.getPacket();
    }

    public static byte[] getMiniGameExitAfter(boolean ready) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(ready ? 54 : 55);
        return mplew.getPacket();
    }

    public static byte[] getMiniGameStart(int loser) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(59);
        mplew.write(loser == 1 ? 0 : 1);
        return mplew.getPacket();
    }

    public static byte[] getMiniGameSkip(int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(61);

        mplew.write(slot);
        return mplew.getPacket();
    }

    public static byte[] getMiniGameRequestTie() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(48);
        return mplew.getPacket();
    }

    public static byte[] getMiniGameDenyTie() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(49);
        return mplew.getPacket();
    }

    public static byte[] getMiniGameFull() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.writeShort(5);
        mplew.write(2);
        return mplew.getPacket();
    }

    public static byte[] getMiniGameMoveOmok(int move1, int move2, int move3) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(62);
        mplew.writeInt(move1);
        mplew.writeInt(move2);
        mplew.write(move3);
        return mplew.getPacket();
    }

    public static byte[] getMiniGameNewVisitor(MapleCharacter c, int slot, MapleMiniGame game) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(4);
        mplew.write(slot);
        PacketHelper.addCharLook(mplew, c, false, false);
        mplew.writeMapleAsciiString(c.getName());
        mplew.writeShort(c.getJob());
        addGameInfo(mplew, c, game);
        return mplew.getPacket();
    }

    public static void addGameInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, MapleMiniGame game) {
        mplew.writeInt(game.getGameType());
        mplew.writeInt(game.getWins(chr));
        mplew.writeInt(game.getTies(chr));
        mplew.writeInt(game.getLosses(chr));
        mplew.writeInt(game.getScore(chr));
    }

    public static byte[] getMiniGameClose(byte number) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(10);
        mplew.write(1);
        mplew.write(number);
        return mplew.getPacket();
    }

    public static byte[] getMatchCardStart(MapleMiniGame game, int loser) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(59);
        mplew.write(loser == 1 ? 0 : 1);
        int times = game.getPieceType() == 2 ? 30 : game.getPieceType() == 1 ? 20 : 12;
        mplew.write(times);
        for (int i = 1; i <= times; i++) {
            mplew.writeInt(game.getCardId(i));
        }
        return mplew.getPacket();
    }

    public static byte[] getMatchCardSelect(int turn, int slot, int firstslot, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(66);
        mplew.write(turn);
        mplew.write(slot);
        if (turn == 0) {
            mplew.write(firstslot);
            mplew.write(type);
        }
        return mplew.getPacket();
    }

    public static byte[] getMiniGameResult(MapleMiniGame game, int type, int x) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(60);
        mplew.write(type);
        game.setPoints(x, type);
        if (type != 0) {
            game.setPoints(x == 1 ? 0 : 1, type == 2 ? 0 : 1);
        }
        if (type != 1) {
            if (type == 0) {
                mplew.write(x == 1 ? 0 : 1);
            } else {
                mplew.write(x);
            }
        }
        addGameInfo(mplew, game.getMCOwner(), game);
        for (Pair visitorz : game.getVisitors()) {
            addGameInfo(mplew, (MapleCharacter) visitorz.right, game);
        }

        return mplew.getPacket();
    }

    public static byte[] MerchantVisitorView(Map<String, AbstractPlayerStore.VisitorInfo> visitor) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(InteractionOpcode.雇佣商店_查看访问名单.getValue());
        mplew.writeShort(visitor.size());
        for (Map.Entry ret : visitor.entrySet()) {
            mplew.writeMapleAsciiString((String) ret.getKey());
            mplew.writeInt(((AbstractPlayerStore.VisitorInfo) ret.getValue()).getInTime());
        }

        return mplew.getPacket();
    }

    public static byte[] MerchantBlackListView(List<String> blackList) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(InteractionOpcode.雇佣商店_查看黑名单.getValue());
        mplew.writeShort(blackList.size());
        for (String visit : blackList) {
            mplew.writeMapleAsciiString(visit);
        }
        return mplew.getPacket();
    }
}
