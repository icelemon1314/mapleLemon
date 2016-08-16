package tools.packet;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import constants.ServerConstants;
import constants.WorldConstants;
import handling.SendPacketOpcode;
import handling.login.LoginServer;

import java.util.*;

import org.apache.log4j.Logger;
import server.ServerProperties;
import tools.HexTool;
import tools.data.output.MaplePacketLittleEndianWriter;

public class LoginPacket {

    public static byte[] getHello(short mapleVersion, byte[] sendIv, byte[] recvIv) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(0xE);
        mplew.writeShort(mapleVersion);
        mplew.writeMapleAsciiString(ServerConstants.MAPLE_PATCH);
        mplew.write(recvIv);
        mplew.write(sendIv);
        mplew.write(ServerConstants.MAPLE_TYPE.getType());
        return mplew.getPacket();
    }

    public static byte[] getPing() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);

        mplew.write(SendPacketOpcode.PING.getValue());

        return mplew.getPacket();
    }

    public static byte[] getLoginAUTH() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(13);

        mplew.write(SendPacketOpcode.LOGIN_AUTH.getValue());
        //mplew.writeMapleAsciiString("MapLogin" + (Randomizer.nextInt(3) + 1));
        String[] a = {"MapLogin", "MapLogin1", "MapLogin2", "MapLogin3", "MapLogin4", "MapLogin5"};
        mplew.writeMapleAsciiString(a[(int) (Math.random() * a.length)]);
        mplew.writeInt(GameConstants.getCurrentDate());
        return mplew.getPacket();
    }

    public static byte[] licenseResult() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.LICENSE_RESULT.getValue());
        mplew.write(1);

        return mplew.getPacket();
    }

    /**
     * 发送选择性别的包
     * @param c
     * @return
     */
    public static byte[] genderNeeded(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);

        mplew.write(SendPacketOpcode.CHOOSE_GENDER.getValue());
        mplew.writeMapleAsciiString(c.getAccountName());

        return mplew.getPacket();
    }

    public static byte[] genderChanged(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);

        mplew.write(SendPacketOpcode.GENDER_SET.getValue());
        mplew.write(0);
        mplew.writeMapleAsciiString(c.getAccountName());
        mplew.writeMapleAsciiString(String.valueOf(c.getAccID()));

        return mplew.getPacket();
    }

    public static byte[] getLoginFailed(int reason) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);

        mplew.write(SendPacketOpcode.LOGIN_STATUS.getValue());
        mplew.write(reason);
        mplew.write(0);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] getPermBan(byte reason) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);

        mplew.write(SendPacketOpcode.LOGIN_STATUS.getValue());
        mplew.writeShort(2);
        mplew.write(0);
        mplew.writeShort((short) reason);
        mplew.write(HexTool.getByteArrayFromHexString("01 01 01 01 00"));

        return mplew.getPacket();
    }

    public static byte[] getTempBan(long timestampTill, byte reason) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(17);

        mplew.write(SendPacketOpcode.LOGIN_STATUS.getValue());
        mplew.writeShort(2);
        mplew.writeInt(0);
        mplew.write(reason);
        mplew.writeLong(timestampTill);

        return mplew.getPacket();
    }
    
    public static byte[] getTempBan(long timestampTill) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(17);

        mplew.write(SendPacketOpcode.LOGIN_STATUS.getValue());
        mplew.writeShort(2);
        mplew.writeLong(timestampTill);

        return mplew.getPacket();
    }

    /**
     * 登录成功
     * @param client
     * @return
     */
    public static byte[] getAuthSuccessRequest(MapleClient client) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.LOGIN_STATUS.getValue());
        mplew.write(0);

        mplew.writeInt(client.getAccID());
        mplew.write(client.getGender()); // 早期版本角色性别由帐号控制
        mplew.write(client.isGm() ? 1 : 0);//给客户端判断是否GM,是GM客户端会给/找人命令加地图ID,有删除人物按钮,被封印后能使用技能,其他未知


        mplew.writeMapleAsciiString(client.getAccountName());
        mplew.writeInt(client.getAccID());
        mplew.write(0);

        return mplew.getPacket();
    }

    /**
     * 检查是否需要排队
     * @param client
     * @return
     */
    public static byte[] checkUserLimit() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(0x03);
        mplew.write(4);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] getEjectWeb(byte i, String link) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.EJECT_WEB.getValue());
        mplew.write(i);
        mplew.writeMapleAsciiString(link);

        return mplew.getPacket();
    }

    public static byte[] deleteCharResponse(int chrId, int state) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.DELETE_CHAR_RESPONSE.getValue());
        mplew.writeInt(chrId);
        mplew.write(state);

        return mplew.getPacket();
    }

    public static byte[] secondPwError(byte mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);

        mplew.write(SendPacketOpcode.SECONDPW_ERROR.getValue());
        mplew.write(mode);

        return mplew.getPacket();
    }

    public static byte[] enableRecommended() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.ENABLE_RECOMMENDED.getValue());
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] sendRecommended(int world, String message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SEND_RECOMMENDED.getValue());
        mplew.write((message != null) && (GameConstants.GMS) ? 1 : 0);
        if ((message != null) && (GameConstants.GMS)) {
            mplew.writeInt(world);
            mplew.writeMapleAsciiString(message);
        }

        return mplew.getPacket();
    }

    /**
     * 服务器列表
     * @param world
     * @param channelLoad
     * @return
     */
    public static byte[] getServerList(WorldConstants.Option world, Map<Integer, Integer> channelLoad) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SERVERLIST.getValue());
        mplew.write(world.getWorld());
        mplew.writeMapleAsciiString(LoginServer.getServerName());
        int lastChannel = 1;
        final Set<Integer> channels = channelLoad.keySet();
        for (int i = 20; i > 0; i--) {
            if (channels.contains(i)) {
                lastChannel = i;
                break;
            }
        }
        mplew.write(lastChannel);
        for (int i = 1; i <= lastChannel; i++) {
            int load;
            if (channels.contains(i)) {
                load = channelLoad.get(i);
            } else {
                load = 255;
            }
            mplew.writeMapleAsciiString(world.name() + "-" + i);
            mplew.writeInt(load);
            mplew.write(world.getWorld());
            mplew.write(i-1);
            mplew.write(0);
        }
        return mplew.getPacket();
    }

    /**
     * 服务器列表结束
     * @return
     */
    public static byte[] getEndOfServerList() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SERVERLIST.getValue());
        mplew.write(0xFF);
        return mplew.getPacket();
    }

    // 获取服务器状态
    public static byte[] getServerStatus(int status) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.SERVERSTATUS.getValue());
        mplew.write(status);
        if (status == 1) {
            mplew.writeInt(0);
            mplew.write(0);
        }
        return mplew.getPacket();
    }

    public static byte[] EventCheck() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.EVENT_CHECK.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("00 05 00 00 10 40 00 46 E5 58 00 57 F5 98 00 04 00 00 00 5F F5 98 00 04 00 00 00 6C F5 98 00 94 CA 07 00 D0 C3 A0 00 1C 16 01 00"));

        return mplew.getPacket();
    }

    public static byte[] getChannelSelected() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CHANNEL_SELECTED.getValue());
        mplew.writeInt(3);

        return mplew.getPacket();
    }

    /**
     * 角色列表
     * @param secondpw
     * @param chars
     * @param charslots
     * @return
     */
    public static byte[] getCharList(String secondpw, List<MapleCharacter> chars, int charslots) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CHARLIST.getValue());
        mplew.write(0);
        mplew.writeInt(0);
        mplew.write(chars.size());
        for (MapleCharacter chr : chars) {
            addCharEntry(mplew, chr);
        }
        return mplew.getPacket();
    }

    /**
     * 新建角色
     * @param chr
     * @param worked
     * @return
     */
    public static byte[] addNewCharEntry(MapleCharacter chr, boolean worked) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.ADD_NEW_CHAR_ENTRY.getValue());
        mplew.write(worked ? 0 : 1);
        addCharEntry(mplew, chr);

        return mplew.getPacket();
    }

    /**
     * 检查角色名
     * @param charname
     * @param nameUsed
     * @return
     */
    public static byte[] charNameResponse(String charname, boolean nameUsed) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CHAR_NAME_RESPONSE.getValue());
        mplew.writeMapleAsciiString(charname);
        mplew.write(nameUsed ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] charNameResponse(String charname, byte type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CHAR_NAME_RESPONSE.getValue());
        mplew.writeMapleAsciiString(charname);
        mplew.write(type);

        return mplew.getPacket();
    }

    /**
     * 添加角色信息
     * @param mplew
     * @param chr
     */
    public static void addCharEntry(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        PacketHelper.addCharStats(mplew, chr);

        Map<Byte, Integer> myEquip = new LinkedHashMap();
        Map<Byte, Integer> maskedEquip = new LinkedHashMap();
        MapleInventory equip = chr.getInventory(MapleInventoryType.EQUIPPED);

        for (Item item : equip.newList()) {
            if (item.getPosition() < -128) {
                continue;
            }

            byte pos = (byte) (item.getPosition() * -1);
            if ((pos < 100) && (myEquip.get(pos) == null)) {
                myEquip.put(pos, item.getItemId());
            } else if (((pos > 100) || (pos == -128)) && (pos != 111)) {
                pos = (byte) (pos == -128 ? 28 : pos - 100);
                if (myEquip.get(pos) != null) {
                    maskedEquip.put(pos, myEquip.get(pos));
                }
                myEquip.put(pos, item.getItemId());
            } else if (myEquip.get(pos) != null) {
                maskedEquip.put(pos, item.getItemId());
            }
        }


        for (Map.Entry entry : myEquip.entrySet()) {
            mplew.write(((Byte) entry.getKey()));
            mplew.writeInt((Integer) entry.getValue());
        }
        mplew.write(0);
        for (Map.Entry entry : maskedEquip.entrySet()) {
            mplew.write((Byte) entry.getKey());
            mplew.writeInt((Integer) entry.getValue());
        }
        mplew.write(0);
    }

    public static byte[] showAllCharacter(int chars) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.ALL_CHARLIST.getValue());
        mplew.write(1);
        mplew.writeInt(chars);
        mplew.writeInt(chars + (3 - chars % 3));

        return mplew.getPacket();
    }

    public static byte[] showAllCharacterInfo(int worldid, List<MapleCharacter> chars, String pic) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.ALL_CHARLIST.getValue());
        mplew.write(chars.isEmpty() ? 5 : 0);
        mplew.write(worldid);
        mplew.write(chars.size());
        for (MapleCharacter chr : chars) {
            addCharEntry(mplew, chr);
        }
        mplew.write(pic.equals("") ? 2 : pic == null ? 0 : 1);

        return mplew.getPacket();
    }

    public static byte[] ShowAccCash(int ACash, int mPoints) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_ACC_CASH.getValue());
        mplew.writeInt(ACash);
        mplew.writeInt(mPoints);

        return mplew.getPacket();
    }

    public static byte[] RegisterInfo(boolean isAllow){
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.REGISTER_INFO.getValue());
        if (isAllow == true) {
            mplew.write(1);
        } else {
            mplew.write(0);
        }
        return mplew.getPacket();
    }

    public static byte[] CheckAccount(String accountName,boolean isUsed){
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CHECK_ACCOUNT_INFO.getValue());
        mplew.writeMapleAsciiString(accountName);
        mplew.write(isUsed ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] RegisterAccount(boolean result){
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.REGISTER_ACCOUNT.getValue());
        mplew.write(result ? 0 :1);

        return mplew.getPacket();
    }
}
