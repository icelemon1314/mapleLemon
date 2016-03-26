package server.quest;

import client.MapleCharacter;
import client.MapleQuestStatus;
import client.MapleStat;
import client.Skill;
import client.SkillEntry;
import client.SkillFactory;
import client.inventory.InventoryException;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import constants.ItemConstants;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.RandomRewards;
import server.Randomizer;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Triple;

public class MapleQuestAction implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    private MapleQuestActionType type;
    private MapleQuest quest;
    private int intStore = 0;
    private List<Integer> applicableJobs = new ArrayList();
    private List<QuestItem> items = null;
    private List<Triple<Integer, Integer, Integer>> skill = null;
    private List<Pair<Integer, Integer>> state = null;

    public MapleQuestAction(MapleQuestActionType type, ResultSet rse, MapleQuest quest, PreparedStatement psq, PreparedStatement psi)
            throws SQLException {
        this.type = type;
        this.quest = quest;

        this.intStore = rse.getInt("intStore");
        String[] jobs = rse.getString("applicableJobs").split(", ");
        if ((jobs.length <= 0) && (rse.getString("applicableJobs").length() > 0)) {
            this.applicableJobs.add(Integer.parseInt(rse.getString("applicableJobs")));
        }
        for (String j : jobs) {
            if (j.length() > 0) {
                this.applicableJobs.add(Integer.parseInt(j));
            }
        }
        ResultSet rs;
        switch (type) {
            case item:
                this.items = new ArrayList();
                psi.setInt(1, rse.getInt("uniqueid"));
                rs = psi.executeQuery();
                while (rs.next()) {
                    this.items.add(new QuestItem(rs.getInt("itemid"), rs.getInt("count"), rs.getInt("period"), rs.getInt("gender"), rs.getInt("job"), rs.getInt("jobEx"), rs.getInt("prop")));
                }
                rs.close();
                break;
            case quest:
                this.state = new ArrayList();
                psq.setInt(1, rse.getInt("uniqueid"));
                rs = psq.executeQuery();
                while (rs.next()) {
                    this.state.add(new Pair(rs.getInt("quest"), rs.getInt("state")));
                }
                rs.close();
                break;
        }
    }

    private static boolean canGetItem(QuestItem item, MapleCharacter chr) {
        if ((item.gender != 2) && (item.gender >= 0) && (item.gender != chr.getGender())) {
            return false;
        }
        if (item.job > 0) {
            List code = getJobBy5ByteEncoding(item.job);
            boolean jobFound = false;
            for (Iterator i$ = code.iterator(); i$.hasNext();) {
                int codec = ((Integer) i$.next());
                if (codec / 100 == chr.getJob() / 100) {
                    jobFound = true;
                    break;
                }
            }
            Iterator i$;
            if ((!jobFound) && (item.jobEx > 0)) {
                List codeEx = getJobBySimpleEncoding(item.jobEx);
                for (i$ = codeEx.iterator(); i$.hasNext();) {
                    int codec = ((Integer) i$.next());
                    if (codec / 100 % 10 == chr.getJob() / 100 % 10) {
                        jobFound = true;
                        break;
                    }
                }
            }
            return jobFound;
        }
        return true;
    }

    public boolean RestoreLostItem(MapleCharacter chr, int itemid) {
        if (this.type == MapleQuestActionType.item) {
            for (QuestItem item : this.items) {
                if (item.itemid == itemid) {
                    if (!chr.haveItem(item.itemid, item.count, true, false)) {
                        MapleInventoryManipulator.addById(chr.getClient(), item.itemid, (short) item.count, "Obtained from quest (Restored) " + this.quest.getId() + " on " + FileoutputUtil.CurrentReadable_Date());
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 开始执行任务
     * @param chr
     * @param extSelection
     */
    public void runStart(MapleCharacter chr, Integer extSelection) {
        MapleQuestStatus status;
        int selection;
        int extNum;
        switch (type) {
            case exp:
                status = chr.getQuest(this.quest);
                if (status.getForfeited() > 0) {
                    break;
                }
                chr.gainExp(this.intStore * GameConstants.getExpRate_Quest(chr.getLevel()) * chr.getStat().questBonus , true, true, true);
                break;
            case item:
                Map props = new HashMap();
                for (QuestItem item : this.items) {
                    if ((item.prop > 0) && (canGetItem(item, chr))) {
                        for (int i = 0; i < item.prop; i++) {
                            props.put(props.size(), item.itemid);
                        }
                    }
                }
                selection = 0;
                extNum = 0;
                if (props.size() > 0) {
                    selection = ((Integer) props.get(Randomizer.nextInt(props.size())));
                }
                for (QuestItem item : this.items) {
                    if (!canGetItem(item, chr)) {
                        continue;
                    }
                    int id = item.itemid;
                    if ((item.prop != -2)
                            && (item.prop == -1
                            ? (extSelection != null) || (extSelection != extNum++)
                            : id != selection)) {
                        continue;
                    }
                    short count = (short) item.count;
                    if (count < 0) {
                        try {
                            MapleInventoryManipulator.removeById(chr.getClient(), ItemConstants.getInventoryType(id), id, count * -1, true, false);
                        } catch (InventoryException ie) {
                            System.err.println("[h4x] Completing a quest without meeting the requirements" + ie);
                        }
                        chr.getClient().getSession().write(MaplePacketCreator.getShowItemGain(id, count, true));
                    } else {
                        int period = item.period / 1440;
                        String name = MapleItemInformationProvider.getInstance().getName(id);
                        if ((id / 10000 == 114) && (name != null) && (name.length() > 0)) {
                            String msg = "恭喜您获得勋章 <" + name + ">";
                            chr.dropMessage(-1, msg);
                            chr.dropMessage(5, msg);
                        }
                        MapleInventoryManipulator.addById(chr.getClient(), id, count, "", null, period, "任务获得 " + this.quest.getId() + " 时间: " + FileoutputUtil.CurrentReadable_Date());
                        chr.getClient().getSession().write(MaplePacketCreator.getShowItemGain(id, count, true));
                    }
                }
                break;
            case nextQuest:
                status = chr.getQuest(this.quest);
                if (status.getForfeited() > 0) {
                    break;
                }
                chr.getClient().getSession().write(MaplePacketCreator.updateQuestFinish(this.quest.getId(), status.getNpc(), this.intStore));
                break;
            case money:
                status = chr.getQuest(this.quest);
                if (status.getForfeited() > 0) {
                    break;
                }
                chr.gainMeso(this.intStore, true, true);
                break;
            case quest:
                for (Pair q : this.state) {
                    chr.updateQuest(new MapleQuestStatus(MapleQuest.getInstance(((Integer) q.left)), ((Integer) q.right)));
                }
                break;
            case skill:
                Map list = new HashMap();
                for (Triple skills : this.skill) {
                    int skillid = ((Integer) skills.left);
                    int skillLevel = ((Integer) skills.mid);
                    int masterLevel = ((Integer) skills.right);
                    Skill skillObject = SkillFactory.getSkill(skillid);
                    boolean found = false;
                    for (Iterator i$ = this.applicableJobs.iterator(); i$.hasNext();) {
                        int applicableJob = ((Integer) i$.next());
                        if (chr.getJob() == applicableJob) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        list.put(skillObject, new SkillEntry((byte) Math.max(skillLevel, chr.getSkillLevel(skillObject)), (byte) Math.max(masterLevel, chr.getMasterLevel(skillObject)), SkillFactory.getDefaultSExpiry(skillObject)));
                    }
                }
                chr.changeSkillsLevel(list);
                break;
            case buffItemID:
                status = chr.getQuest(this.quest);
                if (status.getForfeited() > 0) {
                    break;
                }
                int tobuff = this.intStore;
                if (tobuff <= 0) {
                    break;
                }
                MapleItemInformationProvider.getInstance().getItemEffect(tobuff).applyTo(chr);
                break;
            case infoNumber:
                break;
            case sp:
                status = chr.getQuest(this.quest);
                if (status.getForfeited() > 0) {
                    break;
                }
                int sp_val = this.intStore;
                if (this.applicableJobs.size() > 0) {
                    int finalJob = 0;
                    for (Iterator i$ = this.applicableJobs.iterator(); i$.hasNext();) {
                        int job_val = ((Integer) i$.next());
                        if ((chr.getJob() >= job_val) && (job_val > finalJob)) {
                            finalJob = job_val;
                        }
                    }
                    if (finalJob == 0) {
                        chr.gainSP(sp_val);
                    } else {
                        chr.gainSP(sp_val, GameConstants.getSkillBookByJob(finalJob));
                    }
                } else {
                    chr.gainSP(sp_val);
                }
                break;
            case charmEXP:
            case charismaEXP:
            case craftEXP:
            case insightEXP:
            case senseEXP:
            case willEXP:
                status = chr.getQuest(this.quest);
                if (status.getForfeited() > 0) {
                    break;
                }
                break;
        }
    }

    public boolean checkEnd(MapleCharacter chr, Integer extSelection) {
        switch (this.type) {
            case item:
                Map props = new HashMap();

                for (QuestItem item : this.items) {
                    if ((item.prop > 0) && (canGetItem(item, chr))) {
                        for (int i = 0; i < item.prop; i++) {
                            props.put(props.size(), item.itemid);
                        }
                    }
                }
                int selection = 0;
                int extNum = 0;
                if (props.size() > 0) {
                    selection = ((Integer) props.get(Randomizer.nextInt(props.size())));
                }
                byte eq = 0;
                byte use = 0;
                byte setup = 0;
                byte etc = 0;
                byte cash = 0;

                for (QuestItem item : this.items) {
                    if (!canGetItem(item, chr)) {
                        continue;
                    }
                    int id = item.itemid;
                    if ((item.prop != -2)
                            && (item.prop == -1
                            ? (extSelection != null) || (extSelection != extNum++)
                            : id != selection)) {
                        continue;
                    }
                    short count = (short) item.count;
                    if (count < 0) {
                        if (!chr.haveItem(id, count, false, true)) {
                            chr.dropMessage(1, "您的任务道具不够，还不能完成任务.");
                            return false;
                        }
                    } else {
                        if ((MapleItemInformationProvider.getInstance().isPickupRestricted(id)) && (chr.haveItem(id, 1, true, false))) {
                            chr.dropMessage(1, "You have this item already: " + MapleItemInformationProvider.getInstance().getName(id));
                            return false;
                        }
                        switch (ItemConstants.getInventoryType(id)) {
                            case EQUIP:
                                eq++;
                                break;
                            case USE:
                                use++;
                                break;
                            case SETUP:
                                setup++;
                                break;
                            case ETC:
                                etc++;
                                break;
                            case CASH:
                                cash++;
                                break;
                        }
                    }
                }

                if (chr.getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() < eq) {
                    chr.dropMessage(1, "装备栏空间不足.");
                    return false;
                }
                if (chr.getInventory(MapleInventoryType.USE).getNumFreeSlot() < use) {
                    chr.dropMessage(1, "消耗栏空间不足.");
                    return false;
                }
                if (chr.getInventory(MapleInventoryType.SETUP).getNumFreeSlot() < setup) {
                    chr.dropMessage(1, "设置栏空间不足.");
                    return false;
                }
                if (chr.getInventory(MapleInventoryType.ETC).getNumFreeSlot() < etc) {
                    chr.dropMessage(1, "其他栏空间不足.");
                    return false;
                }
                if (chr.getInventory(MapleInventoryType.CASH).getNumFreeSlot() < cash) {
                    chr.dropMessage(1, "特殊栏空间不足.");
                    return false;
                }
                return true;
            case money:
                int meso = this.intStore;
                if (chr.getMeso() + meso < 0) {
                    chr.dropMessage(1, "携带金币数量已达限制.");
                    return false;
                }
                if ((meso < 0) && (chr.getMeso() < Math.abs(meso))) {
                    chr.dropMessage(1, "金币不足.");
                    return false;
                }
                return true;
        }

        return true;
    }

    public void runEnd(MapleCharacter chr, Integer extSelection) {
        int selection;
        int extNum;
        switch (type) {
            case exp:
                chr.gainExp(this.intStore * GameConstants.getExpRate_Quest(chr.getLevel()) * chr.getStat().questBonus, true, true, true);
                break;
            case item:
                Map props = new HashMap();
                for (QuestItem item : this.items) {
                    if ((item.prop > 0) && (canGetItem(item, chr))) {
                        for (int i = 0; i < item.prop; i++) {
                            props.put(props.size(), item.itemid);
                        }
                    }
                }
                selection = 0;
                extNum = 0;
                if (props.size() > 0) {
                    selection = ((Integer) props.get(Randomizer.nextInt(props.size())));
                }
                for (QuestItem item : this.items) {
                    if (!canGetItem(item, chr)) {
                        continue;
                    }
                    int id = item.itemid;
                    if ((item.prop != -2)
                            && (item.prop == -1
                            ? (extSelection != null) || (extSelection != extNum++)
                            : id != selection)) {
                        continue;
                    }
                    short count = (short) item.count;
                    if (count < 0) {
                        MapleInventoryManipulator.removeById(chr.getClient(), ItemConstants.getInventoryType(id), id, count * -1, true, false);
                        chr.getClient().getSession().write(MaplePacketCreator.getShowItemGain(id, count, true));
                    } else {
                        int period = item.period / 1440;
                        String name = MapleItemInformationProvider.getInstance().getName(id);
                        if ((id / 10000 == 114) && (name != null) && (name.length() > 0)) {
                            String msg = "你获得了勋章 <" + name + ">";
                            chr.dropMessage(-1, msg);
                            chr.dropMessage(5, msg);
                        }
                        MapleInventoryManipulator.addById(chr.getClient(), id, count, "", null, period, "任务获得 " + this.quest.getId() + " 时间: " + FileoutputUtil.CurrentReadable_Date());
                        chr.getClient().getSession().write(MaplePacketCreator.getShowItemGain(id, count, true));
                    }
                }
                break;
            case nextQuest:
                chr.getClient().getSession().write(MaplePacketCreator.updateQuestFinish(this.quest.getId(), chr.getQuest(this.quest).getNpc(), this.intStore));
                break;
            case money:
                chr.gainMeso(this.intStore, true, true);
                break;
            case quest:
                for (Pair q : this.state) {
                    chr.updateQuest(new MapleQuestStatus(MapleQuest.getInstance(((Integer) q.left)), ((Integer) q.right)));
                }
                break;
            case skill:
                Map list = new HashMap();
                for (Triple skills : this.skill) {
                    int skillid = ((Integer) skills.left);
                    int skillLevel = ((Integer) skills.mid);
                    int masterLevel = ((Integer) skills.right);
                    Skill skillObject = SkillFactory.getSkill(skillid);
                    boolean found = false;
                    for (Iterator i$ = this.applicableJobs.iterator(); i$.hasNext();) {
                        int applicableJob = ((Integer) i$.next());
                        if (chr.getJob() == applicableJob) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        list.put(skillObject, new SkillEntry((byte) Math.max(skillLevel, chr.getSkillLevel(skillObject)), (byte) Math.max(masterLevel, chr.getMasterLevel(skillObject)), SkillFactory.getDefaultSExpiry(skillObject)));
                    }
                }
                chr.changeSkillsLevel(list);
                break;
            case buffItemID:
                int tobuff = this.intStore;
                if (tobuff <= 0) {
                    break;
                }
                MapleItemInformationProvider.getInstance().getItemEffect(tobuff).applyTo(chr);
                break;
            case infoNumber:
                break;
            case sp:
                int sp_val = this.intStore;
                if (this.applicableJobs.size() > 0) {
                    int finalJob = 0;
                    for (Iterator i$ = this.applicableJobs.iterator(); i$.hasNext();) {
                        int job_val = ((Integer) i$.next());
                        if ((chr.getJob() >= job_val) && (job_val > finalJob)) {
                            finalJob = job_val;
                        }
                    }
                    if (finalJob == 0) {
                        chr.gainSP(sp_val);
                    } else {
                        chr.gainSP(sp_val, GameConstants.getSkillBookByJob(finalJob));
                    }
                } else {
                    chr.gainSP(sp_val);
                }
                break;
            case charmEXP:
            case charismaEXP:
            case craftEXP:
            case insightEXP:
            case senseEXP:
            case willEXP:
                break;
        }
    }

    private static List<Integer> getJobBy5ByteEncoding(int encoded) {
        List ret = new ArrayList();
        if ((encoded & 0x1) != 0) {
            ret.add(0);
        }
        if ((encoded & 0x2) != 0) {
            ret.add(100);
        }
        if ((encoded & 0x4) != 0) {
            ret.add(200);
        }
        if ((encoded & 0x8) != 0) {
            ret.add(300);
        }
        if ((encoded & 0x10) != 0) {
            ret.add(400);
        }
        if ((encoded & 0x20) != 0) {
            ret.add(500);
        }
        if ((encoded & 0x400) != 0) {
            ret.add(1000);
        }
        if ((encoded & 0x800) != 0) {
            ret.add(1100);
        }
        if ((encoded & 0x1000) != 0) {
            ret.add(1200);
        }
        if ((encoded & 0x2000) != 0) {
            ret.add(1300);
        }
        if ((encoded & 0x4000) != 0) {
            ret.add(1400);
        }
        if ((encoded & 0x8000) != 0) {
            ret.add(1500);
        }
        if ((encoded & 0x20000) != 0) {
            ret.add(2001);
            ret.add(2200);
        }
        if ((encoded & 0x100000) != 0) {
            ret.add(2000);
            ret.add(2001);
        }
        if ((encoded & 0x200000) != 0) {
            ret.add(2100);
        }
        if ((encoded & 0x400000) != 0) {
            ret.add(2001);
            ret.add(2200);
        }
        if ((encoded & 0x40000000) != 0) {
            ret.add(3000);
            ret.add(3200);
            ret.add(3300);
            ret.add(3500);
        }
        return ret;
    }

    private static List<Integer> getJobBySimpleEncoding(int encoded) {
        List ret = new ArrayList();
        if ((encoded & 0x1) != 0) {
            ret.add(200);
        }
        if ((encoded & 0x2) != 0) {
            ret.add(300);
        }
        if ((encoded & 0x4) != 0) {
            ret.add(400);
        }
        if ((encoded & 0x8) != 0) {
            ret.add(500);
        }
        return ret;
    }

    public MapleQuestActionType getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return this.type.toString();
    }

    public List<Triple<Integer, Integer, Integer>> getSkills() {
        return this.skill;
    }

    public List<QuestItem> getItems() {
        return this.items;
    }

    public static class QuestItem {

        public int itemid;
        public int count;
        public int period;
        public int gender;
        public int job;
        public int jobEx;
        public int prop;

        public QuestItem(int itemid, int count, int period, int gender, int job, int jobEx, int prop) {
            if (RandomRewards.getTenPercent().contains(itemid)) {
                count += Randomizer.nextInt(3);
            }
            this.itemid = itemid;
            this.count = count;
            this.period = period;
            this.gender = gender;
            this.job = job;
            this.jobEx = jobEx;
            this.prop = prop;
        }
    }
}
