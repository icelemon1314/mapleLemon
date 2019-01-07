package handling.channel;

import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class MapleGuildRanking {

    private static final MapleGuildRanking instance = new MapleGuildRanking();
    private final List<GuildRankingInfo> ranks;

    public MapleGuildRanking() {
        this.ranks = new LinkedList();
    }

    public static MapleGuildRanking getInstance() {
        return instance;
    }

    public List<GuildRankingInfo> getRank() {
        return this.ranks;
    }

    public void load(boolean reload) {
        if (reload) {
            ranks.clear();
        }
        if (!ranks.isEmpty()) {
            return;
        }
        this.ranks.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            ResultSet rs;
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM guilds ORDER BY `GP` DESC LIMIT 50")) {
                rs = ps.executeQuery();
                while (rs.next()) {
                    GuildRankingInfo rank = new GuildRankingInfo(rs.getString("name"), rs.getInt("GP"), rs.getInt("logo"), rs.getInt("logoColor"), rs.getInt("logoBG"), rs.getInt("logoBGColor"));
                    
                    this.ranks.add(rank);
                }
                ps.close();
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error handling guildRanking" + e);
        }
    }

    public static class GuildRankingInfo {

        private final String name;
        private final int gp;
        private final int logo;
        private final int logocolor;
        private final int logobg;
        private final int logobgcolor;

        public GuildRankingInfo(String name, int gp, int logo, int logocolor, int logobg, int logobgcolor) {
            this.name = name;
            this.gp = gp;
            this.logo = logo;
            this.logocolor = logocolor;
            this.logobg = logobg;
            this.logobgcolor = logobgcolor;
        }

        public String getName() {
            return this.name;
        }

        public int getGP() {
            return this.gp;
        }

        public int getLogo() {
            return this.logo;
        }

        public int getLogoColor() {
            return this.logocolor;
        }

        public int getLogoBg() {
            return this.logobg;
        }

        public int getLogoBgColor() {
            return this.logobgcolor;
        }
    }
}
