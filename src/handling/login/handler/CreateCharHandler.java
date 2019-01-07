package handling.login.handler;

import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import gui.ServerUI;
import handling.login.LoginInformationProvider;
import org.apache.log4j.Logger;
import server.MapleItemInformationProvider;
import server.ServerProperties;
import tools.FileoutputUtil;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

public class CreateCharHandler {

    private static final Logger log = Logger.getLogger(CreateCharHandler.class);

    public static void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (!c.isLoggedIn()) {
            c.getSession().close(true);
            return;
        }
        int faceMark = 0;
        int cape = 0;
        int bottom = 0;
        String name = slea.readMapleAsciiString();
        int face = slea.readInt();
        int hair = slea.readInt();
        int top = slea.readInt();
        bottom = slea.readInt();
        int shoes = slea.readInt();
        int weapon = slea.readInt();

        MapleItemInformationProvider li = MapleItemInformationProvider.getInstance();
        int[] items = {top, bottom, cape, shoes, weapon};
        for (int i : items) {
            if (!LoginInformationProvider.getInstance().isEligibleItem(i)) {
                FileoutputUtil.log("[作弊] 新建角色装备检测失败 名字: " + name + " 道具ID: " + i + " - " + li.getName(i));
                c.getSession().write(LoginPacket.charNameResponse(name, (byte) 3));
                return;
            }
        }

        short[] stat = new short[4];
        int totalStat = 0;
        for (int i = 0; i < 4; i++) {
            stat[i] = slea.readByte();
            if (stat[i] < 4 || stat[i] > 13) {
                FileoutputUtil.log("[作弊] 創建角色初始能力值過小或過大");
                return;
            }
            totalStat += stat[i];
        }
        if (totalStat != 25) {
            FileoutputUtil.log("[作弊] 創建角色初始總能力值不正確");
            return;
        }
        MapleCharacter newchar = MapleCharacter.getDefault(c, stat);
        newchar.setWorld((byte) c.getWorld());
        newchar.setFace(face);
        newchar.setHair(hair);
        newchar.setMap(0);
//        newchar.setGender(gender);
        newchar.setName(name);
//        newchar.setSkinColor(skin);
        newchar.setDecorate(faceMark);
        newchar.setLevel((short) 1);
        newchar.setGmLevel(6);

        MapleInventory equipedIv = newchar.getInventory(MapleInventoryType.EQUIPPED);
        int[][] equips = {{top, -5}, {bottom, -6}, {shoes, -7}, {cape, -9}, {weapon, -11}};
        for (int[] i : equips) {
            if (i[0] > 0) {
                Item item = li.getEquipById(i[0]);
                item.setPosition((byte) i[1]);
                item.setGMLog("角色创建");
                equipedIv.addFromDB(item);
            }
        }

        //newchar.getInventory(MapleInventoryType.USE).addItem(new Item(2000013, (short) 0, (short) 100, (short) 0));
        //newchar.getInventory(MapleInventoryType.USE).addItem(new Item(2000014, (short) 0, (short) 100, (short) 0));
        /*
         int[][] guidebooks = {{4161001, 0}, {4161047, 1}, {4161048, 2000}, {4161052, 2001}, {4161054, 3}, {4161079, 2002}};
         int guidebook = 0;
         for (int[] i : guidebooks) {
         if (newchar.getJob() == i[1]) {
         guidebook = i[0];
         } else if (newchar.getJob() / 1000 == i[1]) {
         guidebook = i[0];
         }
         }
         if (guidebook > 0) {
         newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(guidebook, (short) 0, (short) 1, (short) 0));
         }*/
        if ((MapleCharacterUtil.canCreateChar(name, c.isGm())) && ((!LoginInformationProvider.getInstance().isForbiddenName(name)) || (c.isGm())) && ((c.isGm()) || (c.canMakeCharacter(c.getWorld())))) {
            MapleCharacter.saveNewCharToDB(newchar);
            c.getSession().write(LoginPacket.addNewCharEntry(newchar, true));
            c.createdChar(newchar.getId());
//            ServerUI.getInstance().addCharTable(newchar); // 报错，先注释掉
        } else {
            c.getSession().write(LoginPacket.addNewCharEntry(newchar, false));
        }
    }
}
