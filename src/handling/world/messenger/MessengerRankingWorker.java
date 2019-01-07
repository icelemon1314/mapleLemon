package handling.world.messenger;

import client.MapleCharacter;
import database.DatabaseConnection;
import handling.world.WorldFindService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import server.Timer.WorldTimer;

public class MessengerRankingWorker {

    private final MapleCharacter[] rankingPlayer;
    private final int[] rankingLove;
    private final long[] lastUpdateTime;

    public static MessengerRankingWorker getInstance() {
        return SingletonHolder.instance;
    }

    private MessengerRankingWorker() {
        this.rankingPlayer = new MapleCharacter[2];
        for (int i = 0; i < this.rankingPlayer.length; i++) {
            this.rankingPlayer[i] = null;
        }
        this.rankingLove = new int[2];
        for (int i = 0; i < this.rankingLove.length; i++) {
            this.rankingLove[i] = 0;
        }
        this.lastUpdateTime = new long[2];
        for (int i = 0; i < this.lastUpdateTime.length; i++) {
            this.lastUpdateTime[i] = 0L;
        }
        WorldTimer.getInstance().register(new Runnable() {
            @Override
            public void run() {
                MessengerRankingWorker.this.updateRankFromDB();
            }
        }, 10800000L);

//        System.err.println("[MessengerRankingWorker] 启动完成");
    }

    public void updateRankFromDB() {
        String malesql = "SELECT chr.id, chr.gender, chr.love FROM characters AS chr LEFT JOIN accounts AS acc ON chr.accountid = acc.id WHERE chr.gm = 0 AND chr.gender = 0 AND acc.banned = 0 AND chr.love > 0 ORDER BY chr.love LIMIT 1";
        String femalesql = "SELECT chr.id, chr.gender, chr.love FROM characters AS chr LEFT JOIN accounts AS acc ON chr.accountid = acc.id WHERE chr.gm = 0 AND chr.gender = 1 AND acc.banned = 0 AND chr.love > 0 ORDER BY chr.love LIMIT 1";
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement(malesql);
            ResultSet rs = ps.executeQuery();
            int maleId = 0;
            int malelove = 0;
            if (rs.next()) {
                maleId = rs.getInt("id");
                malelove = rs.getInt("love");
            }
            rs.close();
            ps.close();
            MapleCharacter malechr = null;
            if (maleId > 0) {
                malechr = WorldFindService.getInstance().findCharacterById(maleId);
                if (malechr == null) {
                    malechr = MapleCharacter.loadCharFromDB(maleId, null, false);
                }
            }
            this.rankingPlayer[0] = malechr;
            this.rankingLove[0] = malelove;
            this.lastUpdateTime[0] = System.currentTimeMillis();
            if (maleId > 0) {
                //System.err.println(new StringBuilder().append("更新聊天招待人气排行榜 男角色 - Id: ").append(StringUtil.getRightPaddedStr(String.valueOf(maleId), ' ', 6)).append(" 好感度: ").append(StringUtil.getRightPaddedStr(String.valueOf(malelove), ' ', 4)).append(" 名字: ").append(malechr != null ? malechr.getName() : "????").toString());
            } else {
                //System.err.println("更新聊天招待人气排行榜 男角色 - 暂无信息... ");
            }

            ps = con.prepareStatement(femalesql);
            rs = ps.executeQuery();
            int femaleId = 0;
            int femalelove = 0;
            if (rs.next()) {
                femaleId = rs.getInt("id");
                femalelove = rs.getInt("love");
            }
            rs.close();
            ps.close();
            MapleCharacter femalechr = null;
            if (femaleId > 0) {
                femalechr = WorldFindService.getInstance().findCharacterById(femaleId);
                if (femalechr == null) {
                    femalechr = MapleCharacter.loadCharFromDB(femaleId, null, false);
                }
            }
            this.rankingPlayer[1] = femalechr;
            this.rankingLove[1] = femalelove;
            this.lastUpdateTime[1] = System.currentTimeMillis();
            if (femaleId > 0) {
                //System.err.println(new StringBuilder().append("更新聊天招待人气排行榜 女角色 - Id: ").append(StringUtil.getRightPaddedStr(String.valueOf(femaleId), ' ', 6)).append(" 好感度: ").append(StringUtil.getRightPaddedStr(String.valueOf(femalelove), ' ', 4)).append(" 名字: ").append(femalechr != null ? femalechr.getName() : "????").toString());
            } else {
                //System.err.println("更新聊天招待人气排行榜 女角色 - 暂无信息... ");
            }
        } catch (SQLException se) {
            //System.err.println(new StringBuilder().append("更新聊天招待人气排行榜失败..").append(se).toString());
        }
    }

    public void updateRankFromPlayer(MapleCharacter chr) {
        if ((chr == null) || (chr.isGM())) {
            return;
        }
        int num = chr.getGender();
        if (chr.getLove() > this.rankingLove[num]) {
            this.rankingPlayer[num] = chr;
            this.rankingLove[num] = chr.getLove();
            this.lastUpdateTime[num] = System.currentTimeMillis();
        }
    }

    public MapleCharacter getRankingPlayer(int num) {
        return this.rankingPlayer[num];
    }

    public int getRankingLove(int num) {
        return this.rankingLove[num];
    }

    public long getLastUpdateTime(int num) {
        return this.lastUpdateTime[num];
    }

    public void resetLastUpdateTime(int num) {
        this.lastUpdateTime[num] = System.currentTimeMillis();
    }

    private static class SingletonHolder {

        protected static final MessengerRankingWorker instance = new MessengerRankingWorker();
    }
}
