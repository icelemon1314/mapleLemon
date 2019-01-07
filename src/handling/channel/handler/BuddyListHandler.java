package handling.channel.handler;

import client.BuddyList;
import client.BuddylistEntry;
import client.CharacterNameAndId;
import client.MapleCharacter;
import client.MapleClient;
import database.DatabaseConnection;
import handling.channel.ChannelServer;
import handling.world.WorldBuddyService;
import handling.world.WorldFindService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.BuddyListPacket;

public class BuddyListHandler {

    private static CharacterIdNameBuddyCapacity getCharacterIdAndNameFromDatabase(String name, String group)
            throws SQLException {
        CharacterIdNameBuddyCapacity ret;
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM characters WHERE name LIKE ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                ret = null;
                if ((rs.next()) && (rs.getInt("gm") < 3)) {
                    ret = new CharacterIdNameBuddyCapacity(rs.getInt("id"), rs.getString("name"), group, rs.getInt("buddyCapacity"));
                }
            }
            ps.close();
        }
        return ret;
    }

    public static void BuddyOperation(SeekableLittleEndianAccessor slea, MapleClient c) {
        int mode = slea.readByte();
        BuddyList buddylist = c.getPlayer().getBuddylist();

        /*
         0x15 好友满
         0x16 对方好友满
         0x17 已是好友
         0x18 已申请账号好友
         0x19 等待通过好友申请
         0x1C 角色不存在
         */
        if (mode == 1) {//添加好友
            String addName = slea.readMapleAsciiString();
            String groupName = slea.readMapleAsciiString();
            String remark = slea.readMapleAsciiString();
            String alias = null;
            if (slea.readByte() == 1) {
                alias = slea.readMapleAsciiString();
            }
            if (alias != null) {
                c.getPlayer().dropMessage(1, "暂时不支持账号综合好友的功能。");
                return;
            }
            BuddylistEntry ble = buddylist.get(addName);
            if (addName.getBytes().length > 13 || groupName.getBytes().length > 16 || (alias != null && alias.getBytes().length > 13) || remark.getBytes().length > 133) {
                return;
            }
            if ((ble != null) && ((ble.getGroup().equals(groupName)) || (!ble.isVisible()))) {
                c.getSession().write(BuddyListPacket.buddylistMessage(alias != null ? 0x18 : 0x19));
            } else if ((ble != null) && (ble.isVisible())) {
                ble.setGroup(groupName);
                c.getSession().write(BuddyListPacket.updateBuddylist(buddylist.getBuddies(), 0x11, false, c.getPlayer().getId()));
            } else if (buddylist.isFull()) {
                c.getSession().write(BuddyListPacket.buddylistMessage(0x15));
            } else {
                try {
                    CharacterIdNameBuddyCapacity charWithId = null;
                    int channel = WorldFindService.getInstance().findChannel(addName);
                    if (channel > 0) {
                        MapleCharacter otherChar = ChannelServer.getInstance(channel).getPlayerStorage().getCharacterByName(addName);
                        if (otherChar == null) {
                            charWithId = getCharacterIdAndNameFromDatabase(addName, groupName);
                        } else if ((!otherChar.isIntern()) || (c.getPlayer().isIntern())) {
                            charWithId = new CharacterIdNameBuddyCapacity(otherChar.getId(), otherChar.getName(), groupName, otherChar.getBuddylist().getCapacity());
                        }
                    } else {
                        charWithId = getCharacterIdAndNameFromDatabase(addName, groupName);
                    }
                    if (charWithId != null) {
                        BuddyList.BuddyAddResult buddyAddResult = null;
                        //被加方处理
                        if (channel > 0) {
                            buddyAddResult = WorldBuddyService.getInstance().requestBuddyAdd(addName, c.getChannel(), c.getPlayer().getId(), c.getPlayer().getName(), c.getPlayer().getLevel(), c.getPlayer().getJob());
                            MapleCharacter chr = ChannelServer.getInstance(channel).getPlayerStorage().getCharacterByName(addName);
                            chr.getClient().getSession().write(BuddyListPacket.requestBuddylistAdd(c.getPlayer().getId(), c.getPlayer().getName(), -1));
                        } else {
                            Connection con = DatabaseConnection.getConnection();
                            PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) as buddyCount FROM buddies WHERE characterid = ? AND pending = 0");
                            ps.setInt(1, charWithId.getId());
                            ResultSet rs = ps.executeQuery();
                            if (!rs.next()) {
                                ps.close();
                                rs.close();
                                throw new RuntimeException("Result set expected");
                            }
                            int count = rs.getInt("buddyCount");
                            if (count >= charWithId.getBuddyCapacity()) {
                                buddyAddResult = BuddyList.BuddyAddResult.好友列表已满;
                            }

                            rs.close();
                            ps.close();

                            ps = con.prepareStatement("SELECT pending FROM buddies WHERE characterid = ? AND buddyid = ?");
                            ps.setInt(1, charWithId.getId());
                            ps.setInt(2, c.getPlayer().getId());
                            rs = ps.executeQuery();
                            if (rs.next()) {
                                buddyAddResult = BuddyList.BuddyAddResult.已经是好友关系;
                            }
                            rs.close();
                            ps.close();
                        }
                        //加好友方处理
                        if (buddyAddResult == BuddyList.BuddyAddResult.好友列表已满) {//对方好友列表已满
                            c.getSession().write(BuddyListPacket.buddylistMessage(0x16));//0x19-3
                        } else {
                            int displayChannel = -1;
                            int otherCid = charWithId.getId();
                            if ((buddyAddResult == BuddyList.BuddyAddResult.已经是好友关系) && (channel > 0)) {
                                displayChannel = channel;
                                notifyRemoteChannel(c, channel, otherCid, groupName, BuddyList.BuddyOperation.添加好友);
                            } else if (buddyAddResult != BuddyList.BuddyAddResult.已经是好友关系) {
                                Connection con = DatabaseConnection.getConnection();
                                try (PreparedStatement ps = con.prepareStatement("INSERT INTO buddies (`characterid`, `buddyid`, `groupname`, `pending`) VALUES (?, ?, ?, 1)")) {
                                    ps.setInt(1, charWithId.getId());
                                    ps.setInt(2, c.getPlayer().getId());
                                    ps.setString(3, groupName);
                                    ps.executeUpdate();
                                    ps.close();
                                }
                            }
                            buddylist.put(new BuddylistEntry(charWithId.getName(), otherCid, groupName, displayChannel, true));
                            c.getSession().write(BuddyListPacket.requestBuddylistAdd(otherCid, charWithId.getName(), displayChannel));
                            //c.getSession().write(BuddyListPacket.updateBuddylist(buddylist.getBuddies(), 0x11, false, c.getPlayer().getId()));
                            c.getSession().write(BuddyListPacket.buddylistPrompt(0x14, charWithId.getName()));//向charWithId.getName()发送了好友申请。
                        }
                    } else {//角色不存在
                        c.getSession().write(BuddyListPacket.buddylistMessage(0x1C));//0x19+3
                    }
                } catch (SQLException e) {
                    System.err.println("SQL THROW" + e);
                }
            }
        } else if (mode == 2) {//同意添加好友
            int otherCid = slea.readInt();
            BuddylistEntry ble = buddylist.get(otherCid);
            if (!buddylist.isFull() && ble != null && !ble.isVisible()) {
                int channel = WorldFindService.getInstance().findChannel(otherCid);
                buddylist.put(new BuddylistEntry(ble.getName(), otherCid, "未指定群组", channel, true));
                c.getSession().write(BuddyListPacket.requestBuddylistAdd(otherCid, ble.getName(), channel));
                //c.getSession().write(BuddyListPacket.updateBuddylist(buddylist.getBuddies(), 0x11, false, c.getPlayer().getId()));
                notifyRemoteChannel(c, channel, otherCid, "未指定群组", BuddyList.BuddyOperation.添加好友);
            } else {
                c.getSession().write(BuddyListPacket.buddylistMessage(0x16));
            }
        } else if (mode == 4) {//删除好友
            int otherCid = slea.readInt();
            BuddylistEntry blz = buddylist.get(otherCid);
            if ((blz != null) && (blz.isVisible())) {
                notifyRemoteChannel(c, WorldFindService.getInstance().findChannel(otherCid), otherCid, blz.getGroup(), BuddyList.BuddyOperation.删除好友);
            }
            buddylist.remove(otherCid);
            c.getSession().write(BuddyListPacket.updateBuddylist(null, 0x22, true, otherCid));
        } else if (mode == 6) {//拒绝添加好友
            int fromCid = slea.readInt();
            buddylist.remove(fromCid);
            c.getSession().write(BuddyListPacket.updateBuddylist(null, 0x22, true, fromCid));
            MapleCharacter from = MapleCharacter.getOnlineCharacterById(fromCid);
            if (from == null) {
                return;
            }
            from.getBuddylist().remove(c.getPlayer().getId());
            from.getClient().getSession().write(BuddyListPacket.updateBuddylist(null, 0x22, true, c.getPlayer().getId()));
            from.getClient().getSession().write(BuddyListPacket.buddylistPrompt(0x29, c.getPlayer().getName()));//c.getPlayer().getName()拒绝了好友申请。
        } else if (mode == 0xA) {//增加好友上限
            int capacity = c.getPlayer().getBuddyCapacity();
            if ((capacity >= 100) || (c.getPlayer().getMeso() < 50000)) {
                c.getPlayer().dropMessage(1, "金币不足，或已扩充达到上限。包括基本格数在内，好友目录中只能加入100个好友。您当前的好友数量为: " + capacity);
            } else {
                int newcapacity = capacity + 5;
                c.getPlayer().gainMeso(-50000, true, true);
                c.getPlayer().setBuddyCapacity((byte) newcapacity);
            }
        } else {
            System.err.println("未处理好友操作码：" + mode);
        }
    }

    private static void notifyRemoteChannel(MapleClient c, int remoteChannel, int otherCid, String group, BuddyList.BuddyOperation operation) {
        MapleCharacter player = c.getPlayer();
        if (remoteChannel > 0) {
            WorldBuddyService.getInstance().buddyChanged(otherCid, player.getId(), player.getName(), c.getChannel(), operation, group);
        }
    }

    private static final class CharacterIdNameBuddyCapacity extends CharacterNameAndId {

        private final int buddyCapacity;

        public CharacterIdNameBuddyCapacity(int id, String name, String group, int buddyCapacity) {
            super(id, name, group);
            this.buddyCapacity = buddyCapacity;
        }

        public int getBuddyCapacity() {
            return this.buddyCapacity;
        }
    }
}
