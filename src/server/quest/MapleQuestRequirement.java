package server.quest;

import client.MapleCharacter;
import client.MapleQuestStatus;
import client.Skill;
import client.SkillFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.ItemConstants;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import tools.Pair;

public class MapleQuestRequirement implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    private MapleQuest quest;
    private MapleQuestRequirementType type;
    private int intStore;
    private String stringStore;
    private List<Pair<Integer, Integer>> dataStore;

    /**
     * 任务的必要条件
     * @param quest
     * @param type
     * @param rse
     * @throws SQLException
     */
    public MapleQuestRequirement(MapleQuest quest, MapleQuestRequirementType type, ResultSet rse) throws SQLException {
        this.type = type;
        this.quest = quest;
        this.intStore = Integer.parseInt(rse.getString("stringStore"));
    }

    public boolean check(MapleCharacter chr, Integer npcid) {
        switch (type) {
            case job:
                for (Pair a : this.dataStore) {
                    if ((((Integer) a.getRight()) == chr.getJob()) || (chr.isGM())) {
                        return true;
                    }
                }
                return false;
            case skill:
                for (Pair a : this.dataStore) {
                    boolean acquire = ((Integer) a.getRight()) > 0;
                    int skill = ((Integer) a.getLeft());
                    Skill skil = SkillFactory.getSkill(skill);
                    if (acquire) {
                        if (chr.getSkillLevel(skil) == 0) {
                            return false;
                        }
                    } else if ((chr.getSkillLevel(skil) > 0) || (chr.getMasterLevel(skil) > 0)) {
                        return false;
                    }
                }
                return true;
            case quest:
                for (Pair a : this.dataStore) {
                    MapleQuestStatus q = chr.getQuest(MapleQuest.getInstance(((Integer) a.getLeft()).intValue()));
                    int state = ((Integer) a.getRight());
                    if (state != 0) {
                        if ((q == null) && (state == 0)) {
                            continue;
                        }
                        if ((q == null) || (q.getStatus() != state)) {
                            return false;
                        }
                    }
                }
                return true;
            case item:
                for (Pair a : this.dataStore) {
                    int itemId = ((Integer) a.getLeft());
                    short quantity = 0;
                    MapleInventoryType iType = ItemConstants.getInventoryType(itemId);
                    for (Item item : chr.getInventory(iType).listById(itemId)) {
                        quantity = (short) (quantity + item.getQuantity());
                    }
                    int count = ((Integer) a.getRight());
                    if ((quantity < count) || ((count <= 0) && (quantity > 0))) {
                        return false;
                    }
                }
                return true;
            case lvmin:
                return chr.getLevel() >= this.intStore;
            case lvmax:
                return chr.getLevel() <= this.intStore;
            case end:
                String timeStr = this.stringStore;
                if ((timeStr == null) || (timeStr.length() <= 0)) {
                    return true;
                }
                Calendar cal = Calendar.getInstance();
                cal.set(Integer.parseInt(timeStr.substring(0, 4)), Integer.parseInt(timeStr.substring(4, 6)), Integer.parseInt(timeStr.substring(6, 8)), Integer.parseInt(timeStr.substring(8, 10)), 0);
                return cal.getTimeInMillis() >= System.currentTimeMillis();
            case mob:
                for (Pair a : this.dataStore) {
                    int mobId = ((Integer) a.getLeft());
                    int killReq = ((Integer) a.getRight());
                    if (chr.getQuest(this.quest).getMobKills(mobId) < killReq) {
                        return false;
                    }
                }
                return true;
            case npc:
                return (npcid == null) || (npcid == this.intStore);
            case fieldEnter:
                if (this.intStore > 0) {
                    return this.intStore == chr.getMapId();
                }
                return true;
            case pop:
                return chr.getFame() >= this.intStore;
            case questComplete:
                return chr.getNumQuest() >= this.intStore;
            case interval:
                return (chr.getQuest(this.quest).getStatus() != 2) || (chr.getQuest(this.quest).getCompletionTime() <= System.currentTimeMillis() - this.intStore * 60 * 1000L);
            case pet:
                for (Pair a : this.dataStore) {
                    if (chr.getSpawnPet() != null) {
                        return true;
                    }
                }
                return false;
            case pettamenessmin:
                MaplePet pet = chr.getSpawnPets();
                if ((pet != null) && (pet.getSummoned()) && (pet.getCloseness() >= this.intStore)) {
                    return true;
                }
                return false;
            case partyQuest_S:
                int[] partyQuests = {1200, 1201, 1202, 1203, 1204, 1205, 1206, 1300, 1301, 1302};
                int sRankings = 0;
                for (int i : partyQuests) {
                    String rank = chr.getOneInfo(i, "rank");
                    if ((rank != null) && (rank.equals("S"))) {
                        sRankings++;
                    }
                }
                return sRankings >= 5;
            case subJobFlags:
                return chr.getSubcategory() == this.intStore / 2;
            case craftMin:
            case willMin:
            case charismaMin:
            case insightMin:
            case charmMin:
            case senseMin:
//            case interval:
//            case startscript: 
        }
        return true;
    }

    public MapleQuestRequirementType getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return this.type.toString();
    }

    public List<Pair<Integer, Integer>> getDataStore() {
        return this.dataStore;
    }
}
