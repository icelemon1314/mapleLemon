package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.Skill;
import client.SkillFactory;
import handling.channel.MapleGuildRanking;
import handling.world.WorldGuildService;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildResponse;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import server.MapleStatEffect;
import tools.FileoutputUtil;
import tools.Pair;
import tools.StringUtil;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.GuildPacket;

public class GuildHandler {

    private static final Map<String, Pair<Integer, Long>> invited = new HashMap();
    private static long nextPruneTime = System.currentTimeMillis() + 300000L;

    public static void DenyGuildRequest(String from, MapleClient c) {
        MapleCharacter cfrom = c.getChannelServer().getPlayerStorage().getCharacterByName(from);
        if ((cfrom != null) && (invited.remove(c.getPlayer().getName().toLowerCase()) != null)) {
            cfrom.dropMessage(5, "\"" + c.getPlayer().getName() + "\"拒绝了家族邀请.");
//            cfrom.getClient().getSession().write(GuildPacket.denyGuildInvitation(c.getPlayer().getName()));
        }
    }

    public static final void JoinGuildRequest(final int guildId, final MapleClient c) {
        c.getPlayer().setGuildId(guildId);
        int 添加申请列表 = WorldGuildService.getInstance().addGuildJoinMember(c.getPlayer().getMGC());
        if (添加申请列表 == 0) {
            c.getPlayer().dropMessage(1, "家族成员数已到达最高限制。");
            return;
        }
        c.getPlayer().setGuildId(0);
    }

    public static final void JoinGuildCancel(final MapleClient c) {
        c.getPlayer().setGuildId(MapleGuild.getJoinGuildId(c.getPlayer().getId()));
        WorldGuildService.getInstance().removeGuildJoinMember(c.getPlayer().getMGC());
        c.getSession().write(GuildPacket.removeGuildJoin(c.getPlayer().getId()));
        c.getPlayer().setGuildId(0);
    }

    public static final void AddGuildMember(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        slea.readByte();
        int memberId = slea.readInt();
        MapleCharacter chr = MapleCharacter.getCharacterById(memberId);
        chr.setGuildId(c.getPlayer().getGuildId());
        chr.setGuildRank((byte) 5);
        int s = WorldGuildService.getInstance().addGuildMember(chr.getMGC());
        if (s == 0) {
            c.getPlayer().dropMessage(1, "家族成员数已到达最高限制。");
            chr.setGuildId(0);
            return;
        }
        chr.getClient().getSession().write(GuildPacket.showGuildInfo(chr));
        chr.saveGuildStatus();
        respawnPlayer(chr);
    }

    public static final void DenyGuildJoin(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        for (int i = 0 ; i < (slea.readByte() & 0xFF) ; i++) {
            int cid = slea.readInt();
            MapleGuild guild = WorldGuildService.getInstance().getGuild(MapleGuild.getJoinGuildId(cid));
            guild.setGuildQuest(false, MapleCharacter.getCharacterById(cid));
            byte[] packet = GuildPacket.removeGuildJoin(cid);
            MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterById(cid);
            if (chr != null) {
                chr.getClient().getSession().write(packet);
            }
            guild.broadcast(packet);
        }
    }

    private static boolean isGuildNameAcceptable(String name) {
        return WorldGuildService.getInstance().getGuildByName(name) == null;
    }

    private static void respawnPlayer(MapleCharacter chr) {
        if (chr.getMap() == null) {
            return;
        }
        chr.getMap().broadcastMessage(GuildPacket.loadGuildName(chr));
        chr.getMap().broadcastMessage(GuildPacket.loadGuildIcon(chr));
    }
    private static String GuildName;

    public static void Guild(SeekableLittleEndianAccessor slea, MapleClient c) {
        long currentTime = System.currentTimeMillis();
        if (currentTime >= nextPruneTime) {
            Iterator itr = invited.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry inv = (Map.Entry) itr.next();
                if (currentTime >= ((Long) ((Pair) inv.getValue()).right)) {
                    itr.remove();
                }
            }
            nextPruneTime += 300000L;
        }
        MapleCharacter chr = c.getPlayer();
        byte mode = slea.readByte();
        int guildId;
        String name;
        int cid;
        Skill skilli;
        int eff;
        switch (mode) {
            case 0:
                c.getSession().write(GuildPacket.showGuildInfo(chr));
                break;
            case 0x01: // 接受邀请
                if (c.getPlayer().getGuildId() > 0) {
                    return;
                }
            case 0x02: // 显示公会
                guildId = slea.readInt();
                c.getSession().write(GuildPacket.GuildReceipt(guildId));
                break;
            case 0x04: // 创建判断家族名字
                GuildName = slea.readMapleAsciiString();

                if (!isGuildNameAcceptable(GuildName)) {
                    c.getSession().write(GuildPacket.genericGuildMessage((byte) 0x33));
                    return;
                }
                c.getSession().write(GuildPacket.createGuildNotice(GuildName));
                break;
            case 0x07: // 邀请
                if ((chr.getGuildId() <= 0) || (chr.getGuildRank() > 2)) {
                    return;
                }
                name = slea.readMapleAsciiString().toLowerCase();
                MapleCharacter cfrom = c.getChannelServer().getPlayerStorage().getCharacterByName(name);
                if (cfrom.getGuildId() > 0) {
                    chr.dropMessage(1, "对方已经加入其它家族了。");
                    return;
                }
                final MapleGuildResponse mgr = MapleGuild.sendInvite(c, name);
                if (mgr != null) {
                    c.getSession().write(mgr.getPacket());
                } else {
                    c.getPlayer().dropMessage(5, "已邀请'" + name + "'加入公会。");
                }
                break;
//                guildId = chr.getGuildId();
//                name = cfrom.getName().toLowerCase();
//                Pair gid = (Pair) invited.remove(name);
//                if ((gid == null) || (guildId != ((Integer) gid.left))) {
//                    break;
//                }
//                cfrom.setGuildId(guildId);
//                cfrom.setGuildRank((byte) 5);
//                int s = WorldGuildService.getInstance().addGuildMember(cfrom.getMGC());
//                if (s == 0) {
//                    chr.dropMessage(1, "家族成员数已到达最高限制。");
//                    cfrom.setGuildId(0);
//                    return;
//                }
//                cfrom.getClient().getSession().write(GuildPacket.showGuildInfo(cfrom));
//                cfrom.saveGuildStatus();
//                respawnPlayer(cfrom);
//                if (invited.containsKey(name)) {
//                    chr.dropMessage(5, "玩家 " + name + " 已在邀请中，请等待回应。");
//                    return;
//                }
//                MapleGuildResponse mgr = MapleGuild.sendInvite(c, name);
//                if (mgr != null) {
//                    c.getSession().write(mgr.getPacket());
//                } else {
//                    invited.put(name, new Pair(chr.getGuildId(), currentTime + 300000L));
//                }
//                break;
            case 0x0B: // 离开
                cid = slea.readInt();
                name = slea.readMapleAsciiString();
                if ((cid != chr.getId()) || (!name.equals(chr.getName())) || (chr.getGuildId() <= 0)) {
                    return;
                }
                WorldGuildService.getInstance().leaveGuild(chr.getMGC());
                c.getSession().write(GuildPacket.showGuildInfo(null));
                break;
            case 0x0C: // 驱逐
                cid = slea.readInt();
                name = slea.readMapleAsciiString();
                if ((chr.getGuildRank() > 2) || (chr.getGuildId() <= 0)) {
                    return;
                }
                WorldGuildService.getInstance().expelMember(chr.getMGC(), name, cid);
                break;
            case 0x12: // 修改职位名称
                if ((chr.getGuildId() <= 0) || (chr.getGuildRank() != 1)) {
                    return;
                }
                String[] ranks = new String[5];
                for (int i = 0; i < 5; i++) {
                    ranks[i] = slea.readMapleAsciiString();
                }
                WorldGuildService.getInstance().changeRankTitle(chr.getGuildId(), ranks);
                break;
            case 0x13: //修改职位
                cid = slea.readInt();
                byte newRank = slea.readByte();
                if ((newRank <= 1) || (newRank > 5) || (chr.getGuildRank() > 2) || ((newRank <= 2) && (chr.getGuildRank() != 1)) || (chr.getGuildId() <= 0)) {
                    return;
                }
                WorldGuildService.getInstance().changeRank(chr.getGuildId(), cid, newRank);
                break;
            case 0x14: // 修改图标
                if ((chr.getGuildId() <= 0) || (chr.getGuildRank() != 1)) {
                    return;
                }
                short bg = slea.readShort();
                byte bgcolor = slea.readByte();
                short logo = slea.readShort();
                byte logocolor = slea.readByte();
                WorldGuildService.getInstance().setGuildEmblem(chr.getGuildId(), bg, bgcolor, logo, logocolor);
                respawnPlayer(c.getPlayer());
                break;
            case 0x11: //更改家族公告
                String notice = slea.readMapleAsciiString();
                if ((notice.length() > 100) || (chr.getGuildId() <= 0) || (chr.getGuildRank() > 2)) {
                    return;
                }
                WorldGuildService.getInstance().setGuildNotice(chr.getGuildId(), notice);
                break;
            case 0x1F:
                cid = slea.readInt();
                if ((chr.getGuildId() <= 0) || (chr.getGuildRank() > 1)) {
                    return;
                }
                WorldGuildService.getInstance().setGuildLeader(chr.getGuildId(), cid);
                break;
            case 0x2D: // 公会搜寻
                switch (slea.readByte()) {
                    case 0:// 名字搜寻
                        String keyWord = slea.readMapleAsciiString();
                        c.getSession().write(GuildPacket.showSearchGuilds(MapleGuild.searchGuild(keyWord)));
                        break;
                    case 1:// 条件搜寻
                        int[] keyWords = new int[6];
                        for (int i = 0 ; i < 6 ; i++) {
                            keyWords[i] = slea.readByte() & 0xFF;
                        }
                        c.getSession().write(GuildPacket.showSearchGuilds(MapleGuild.searchGuild(keyWords)));
                        break;
                }
                break;
            case 0x35: // 家族创建提示
                int 创建 = slea.readByte(); // 1 确认  0 取消
                if (创建 == 1) {
                    int cost = c.getChannelServer().getCreateGuildCost();
                    if ((chr.getGuildId() > 0) || (chr.getMapId() != 200000301)) {
                        chr.dropMessage(1, "不能创建家族\r\n已经有家族或没在家族中心");
                        return;
                    }
                    if (chr.getMeso() < cost) {
                        chr.dropMessage(1, "你没有足够的金币创建一个家族。当前创建家族需要: " + cost + " 的金币。");
                        return;
                    }
                    guildId = WorldGuildService.getInstance().createGuild(c.getPlayer().getId(), GuildName);
                    if (guildId == 0) {
                        c.getPlayer().dropMessage(1, "创建公会出错\r\n请重试一次。");
                        return;
                    }
                    c.getPlayer().gainMeso(-cost, true, true);
                    c.getPlayer().setGuildId(guildId);
                    c.getPlayer().setGuildRank((byte) 1);
                    c.getPlayer().saveGuildStatus();
                    WorldGuildService.getInstance().setGuildMemberOnline(c.getPlayer().getMGC(), true, c.getChannel());
                    //c.getSession().write(GuildPacket.showGuildInfo(c.getPlayer()));
                    c.getSession().write(GuildPacket.newGuildInfo(c.getPlayer()));
                    WorldGuildService.getInstance().gainGP(c.getPlayer().getGuildId(), 500, c.getPlayer().getId());
                    MapleGuildRanking.getInstance().load(true);
                    //c.getPlayer().dropMessage(1, "恭喜你成功创建家族.");
                    //respawnPlayer(c.getPlayer());
                } else if (创建 == 0) {
                    c.getSession().write(GuildPacket.genericGuildMessage((byte) 0x3B));
                }
                break;
            default:
                FileoutputUtil.log("未知家族操作类型: ( 0x" + StringUtil.getLeftPaddedStr(Integer.toHexString(mode).toUpperCase(), '0', 2) + " )" + slea.toString());
        }
    }
}
