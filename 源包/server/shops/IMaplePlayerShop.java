package server.shops;

import client.MapleCharacter;
import client.MapleClient;
import java.util.List;
import tools.Pair;

public abstract interface IMaplePlayerShop {

    public static final byte HIRED_MERCHANT = 1;
    public static final byte PLAYER_SHOP = 2;
    public static final byte OMOK = 3;
    public static final byte MATCH_CARD = 4;

    public abstract String getOwnerName();

    public abstract String getDescription();

    public abstract void setDescription(String paramString);

    public abstract List<Pair<Byte, MapleCharacter>> getVisitors();

    public abstract List<MaplePlayerShopItem> getItems();

    public abstract boolean isOpen();

    public abstract boolean saveItems();

    public abstract boolean removeItem(int paramInt);

    public abstract boolean isOwner(MapleCharacter paramMapleCharacter);

    public abstract byte getShopType();

    public abstract byte getVisitorSlot(MapleCharacter paramMapleCharacter);

    public abstract byte getFreeSlot();

    public abstract int getItemId();

    public abstract int getMeso();

    public abstract int getOwnerId();

    public abstract int getOwnerAccId();

    public abstract void setOpen(boolean paramBoolean);

    public abstract void setMeso(int paramInt);

    public abstract void addItem(MaplePlayerShopItem paramMaplePlayerShopItem);

    public abstract void removeFromSlot(int paramInt);

    public abstract void broadcastToVisitors(byte[] paramArrayOfByte);

    public abstract void addVisitor(MapleCharacter paramMapleCharacter);

    public abstract void removeVisitor(MapleCharacter paramMapleCharacter);

    public abstract void removeAllVisitors(int paramInt1, int paramInt2);

    public abstract void buy(MapleClient paramMapleClient, int paramInt, short paramShort);

    public abstract void closeShop(boolean paramBoolean1, boolean paramBoolean2);

    public abstract String getPassword();

    public abstract int getMaxSize();

    public abstract int getSize();

    public abstract int getGameType();

    public abstract void update();

    public abstract void setAvailable(boolean paramBoolean);

    public abstract boolean isAvailable();

    public abstract List<AbstractPlayerStore.BoughtItem> getBoughtItems();

    public abstract List<Pair<String, Byte>> getMessages();

    public abstract int getMapId();

    public abstract int getChannel();
}
