package server;

import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.FileoutputUtil;
import tools.StringUtil;

public class RankingWorker {

    private static final Map<Integer, List<RankingInformation>> rankings = new HashMap();
    private static final Map<String, Integer> jobCommands = new HashMap();
    private static final List<PokemonInformation> pokemon = new ArrayList();
    private static final List<PokedexInformation> pokemon_seen = new ArrayList();
    private static final List<PokebattleInformation> pokemon_ratio = new ArrayList();
    private static final List<Integer> itemSearch = new ArrayList();

    public static Integer getJobCommand(String job) {
        return jobCommands.get(job);
    }

    public static Map<String, Integer> getJobCommands() {
        return jobCommands;
    }

    public static List<RankingInformation> getRankingInfo(int job) {
        return (List) rankings.get(job);
    }

    public static List<PokemonInformation> getPokemonInfo() {
        return pokemon;
    }

    public static List<PokedexInformation> getPokemonCaught() {
        return pokemon_seen;
    }

    public static List<PokebattleInformation> getPokemonRatio() {
        return pokemon_ratio;
    }

    public static List<Integer> getItemSearch() {
        return itemSearch;
    }

    public static void start() {
        FileoutputUtil.log("系统自动更新玩家排名功能已启动...");
        FileoutputUtil.log(new StringBuilder().append("更新间隔时间为: ").append(Start.instance.getRankTime()).append(" 分钟1次。").toString());
        Timer.WorldTimer.getInstance().register(new Runnable() {
            @Override
            public void run() {
                RankingWorker.jobCommands.clear();
                RankingWorker.rankings.clear();
                RankingWorker.pokemon.clear();
                RankingWorker.pokemon_seen.clear();
                RankingWorker.pokemon_ratio.clear();
                RankingWorker.itemSearch.clear();
                RankingWorker.updateRank();
            }
        }, 60000 * Start.instance.getRankTime());
    }

    public static void updateRank() {
        FileoutputUtil.log("开始更新玩家排名...");
        long startTime = System.currentTimeMillis();
        loadJobCommands();
        Connection con = DatabaseConnection.getConnection();
        try {
            con.setAutoCommit(false);
            updateRanking(con);
            updatePokemonRatio(con);
            updateItemSearch(con);
            con.commit();
            con.setAutoCommit(true);
        } catch (Exception ex) {
            try {
                con.rollback();
                con.setAutoCommit(true);
                FileoutputUtil.outputFileError(FileoutputUtil.ScriptEx_Log, ex);
                System.err.println("更新玩家排名出错");
            } catch (SQLException ex2) {
                FileoutputUtil.outputFileError(FileoutputUtil.ScriptEx_Log, ex2);
                System.err.println("Could not rollback unfinished ranking transaction");
            }
        }
        FileoutputUtil.log(new StringBuilder().append("玩家排名更新完成 耗时: ").append((System.currentTimeMillis() - startTime) / 1000L).append(" 秒..").toString());
    }

    public static void printSection(String s) {
        s = new StringBuilder().append("-[ ").append(s).append(" ]").toString();
        while (s.getBytes().length < 79) {
            s = new StringBuilder().append("=").append(s).toString();
        }
        FileoutputUtil.log(s);
    }

    private static void updateRanking(Connection con) throws Exception {
        StringBuilder sb = new StringBuilder("SELECT c.id, c.job, c.exp, c.level, c.name, c.jobRank, c.rank, c.fame");
        sb.append(" FROM characters AS c LEFT JOIN accounts AS a ON c.accountid = a.id WHERE c.gm = 0 AND a.banned = 0 AND c.level >= 160");
        sb.append(" ORDER BY c.level DESC , c.exp DESC , c.fame DESC , c.rank ASC");

        PreparedStatement ps;
        try (PreparedStatement charSelect = con.prepareStatement(sb.toString()); ResultSet rs = charSelect.executeQuery()) {
            ps = con.prepareStatement("UPDATE characters SET jobRank = ?, jobRankMove = ?, rank = ?, rankMove = ? WHERE id = ?");
            int rank = 0;
            Map rankMap = new LinkedHashMap();
            for (Iterator i$ = jobCommands.values().iterator(); i$.hasNext();) {
                int i = ((Integer) i$.next());
                rankMap.put(i, 0);
                rankings.put(i, new ArrayList());
            }
            while (rs.next()) {
                int job = rs.getInt("job");
                if (!rankMap.containsKey(job / 100)) {
                    continue;
                }
                int jobRank = ((Integer) rankMap.get(Integer.valueOf(job / 100))) + 1;
                rankMap.put(job / 100, jobRank);
                rank++;
                ((List) rankings.get(-1)).add(new RankingInformation(rs.getString("name"), job, rs.getInt("level"), rs.getLong("exp"), rank, rs.getInt("fame")));
                ((List) rankings.get(job / 100)).add(new RankingInformation(rs.getString("name"), job, rs.getInt("level"), rs.getLong("exp"), jobRank, rs.getInt("fame")));
                ps.setInt(1, jobRank);
                ps.setInt(2, rs.getInt("jobRank") - jobRank);
                ps.setInt(3, rank);
                ps.setInt(4, rs.getInt("rank") - rank);
                ps.setInt(5, rs.getInt("id"));
                ps.addBatch();
            }
            ps.executeBatch();
        }
        ps.close();
    }

    private static void updatePokemonRatio(Connection con) throws Exception {
        StringBuilder sb = new StringBuilder("SELECT (c.totalWins / c.totalLosses) AS mc, c.name, c.totalWins, c.totalLosses ");
        sb.append(" FROM characters AS c LEFT JOIN accounts AS a ON c.accountid = a.id");
        sb.append(" WHERE c.gm = 0 AND a.banned = 0 AND c.totalWins > 10 AND c.totalLosses > 0");
        sb.append(" ORDER BY mc DESC, c.totalWins DESC, c.totalLosses ASC LIMIT 50");

        try (PreparedStatement charSelect = con.prepareStatement(sb.toString()); ResultSet rs = charSelect.executeQuery()) {
            int rank = 0;
            while (rs.next()) {
                rank++;
                pokemon_ratio.add(new PokebattleInformation(rs.getString("name"), rs.getInt("totalWins"), rs.getInt("totalLosses"), rs.getDouble("mc"), rank));
            }
        }
    }

    private static void updateItemSearch(Connection con) throws Exception {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        try (PreparedStatement ps = con.prepareStatement("SELECT itemid, count FROM itemsearch WHERE count > 0 ORDER BY count DESC LIMIT 10"); ResultSet rs = ps.executeQuery()) {
            itemSearch.clear();
            while (rs.next()) {
                int itemId = rs.getInt("itemid");

                if ((itemSearch.contains(itemId)) || (!ii.itemExists(itemId))) {
                    continue;
                }
                itemSearch.add(itemId);
            }
        }
    }

    public static void loadJobCommands() {
        jobCommands.put("所有", -1);
        jobCommands.put("新手", 0);
        jobCommands.put("战士", 1);
        jobCommands.put("魔法师", 2);
        jobCommands.put("弓箭手", 3);
        jobCommands.put("飞侠", 4);
        jobCommands.put("勇士", 20);
    }

    public static class PokebattleInformation {

        public String toString;

        public PokebattleInformation(String name, int totalWins, int totalLosses, double caught, int rank) {
            StringBuilder builder = new StringBuilder("Rank ");
            builder.append(rank);
            builder.append(" : #e");
            builder.append(name);
            builder.append("#n - #rRatio: ");
            builder.append(caught);
            builder.append("\r\n");
            this.toString = builder.toString();
        }

        @Override
        public String toString() {
            return this.toString;
        }
    }

    public static class PokedexInformation {

        public String toString;

        public PokedexInformation(String name, int caught, int rank) {
            StringBuilder builder = new StringBuilder("排名 ");
            builder.append(rank);
            builder.append(" : #e");
            builder.append(name);
            builder.append("#n - #rCaught: ");
            builder.append(caught);
            builder.append("\r\n");
            this.toString = builder.toString();
        }

        @Override
        public String toString() {
            return this.toString;
        }
    }

    public static class PokemonInformation {

        public String toString;

        public PokemonInformation(String name, int totalWins, int totalLosses, int caught, int rank) {
            StringBuilder builder = new StringBuilder("排名 ");
            builder.append(rank);
            builder.append(" : #e");
            builder.append(name);
            builder.append("#n - #r胜利: ");
            builder.append(totalWins);
            builder.append("#b 失败: ");
            builder.append(totalLosses);
            builder.append("#k Caught:");
            builder.append(caught);
            builder.append("\r\n");
            this.toString = builder.toString();
        }

        @Override
        public String toString() {
            return this.toString;
        }
    }

    public static class RankingInformation {

        public String toString;
        public int rank;

        public RankingInformation(String name, int job, int level, long exp, int rank, int fame) {
            this.rank = rank;
            StringBuilder builder = new StringBuilder("排名 ");
            builder.append(StringUtil.getRightPaddedStr(String.valueOf(rank), ' ', 3));
            builder.append(" : ");
            builder.append(StringUtil.getRightPaddedStr(name, ' ', 13));
            builder.append(" 等级: ");
            builder.append(StringUtil.getRightPaddedStr(String.valueOf(level), ' ', 3));
            builder.append(" 职业: ");
            builder.append(StringUtil.getRightPaddedStr(MapleCarnivalChallenge.getJobNameById(job), ' ', 10));

            builder.append("\r\n");
            this.toString = builder.toString();
        }

        @Override
        public String toString() {
            return this.toString;
        }
    }
}
