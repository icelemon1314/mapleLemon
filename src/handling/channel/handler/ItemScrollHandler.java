package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.PlayerStats;
import client.SkillFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleInventoryType;
import client.inventory.ModifyInventory;
import constants.ItemConstants;
import constants.ServerConstants;
import java.util.ArrayList;
import java.util.List;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.InventoryPacket;

public class ItemScrollHandler {

    /**
     * 砸卷
     * @param slea
     * @param c
     * @param chr
     * @param cash
     */
    public static void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr, boolean cash) {

    }


}
