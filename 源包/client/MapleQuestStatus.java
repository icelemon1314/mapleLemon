package client;

import constants.GameConstants;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import server.life.MapleLifeFactory;
import server.quest.MapleQuest;

public final class MapleQuestStatus implements Serializable {

    private static final long serialVersionUID = 91795419934134L;
    private transient MapleQuest quest;
    private byte status;
    private Map<Integer, Integer> killedMobs = null;
    private int npc;
    private long completionTime;
    private int forfeited = 0;
    private String customData="";


    public static final int QUEST_UNSTART = 0;
    public static final int QUEST_STARTED=1;
    public static final int QUEST_COMPLETED=2;

    public MapleQuestStatus(MapleQuest quest, int status) {
        this.quest = quest;
        setStatus((byte) status);
        this.completionTime = System.currentTimeMillis();
    }

    public MapleQuestStatus(MapleQuest quest, byte status, int npc) {
        this.quest = quest;
        setStatus(status);
        setNpc(npc);
        this.completionTime = System.currentTimeMillis();
    }

    public void setQuest(int qid) {
        this.quest = MapleQuest.getInstance(qid);
    }

    public MapleQuest getQuest() {
        return this.quest;
    }

    public byte getStatus() {
        return this.status;
    }

    /**
     * 设置任务状态
     * @param status
     */
    public void setStatus(byte status) {
        this.status = status;
    }

    public int getNpc() {
        return this.npc;
    }

    public void setNpc(int npc) {
        this.npc = npc;
    }

    private void registerMobs() {
        this.killedMobs = new LinkedHashMap();
    }

    private int maxMob(int mobid) {
        return 0;
    }

    public boolean mobKilled(int id, int skillID) {
        if ((this.quest != null) && (this.quest.getSkillID() > 0)
                && (this.quest.getSkillID() != skillID)) {
            return false;
        }

        Integer mob = this.killedMobs.get(Integer.valueOf(id));
        if (mob != null) {
            int mo = maxMob(id);
            if (mob >= mo) {
                return false;
            }
            this.killedMobs.put(id,Math.min(mob + 1, mo));
            return true;
        }
        for (Entry<Integer, Integer> mo : this.killedMobs.entrySet()) {
            if (questCount((mo.getKey()), id)) {
                int mobb = maxMob((mo.getKey()).intValue());
                if ((mo.getValue()) >= mobb) {
                    return false;
                }
                this.killedMobs.put(mo.getKey(), Math.min((mo.getValue()) + 1, mobb));
                return true;
            }
        }
        return false;
    }

    private boolean questCount(int mo, int id) {
        Iterator i$;
        if (MapleLifeFactory.getQuestCount(mo) != null) {
            for (i$ = MapleLifeFactory.getQuestCount(mo).iterator(); i$.hasNext();) {
                int i = ((Integer) i$.next());
                if (i == id) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setMobKills(int id, int count) {
        if (this.killedMobs == null) {
            registerMobs();
        }
        this.killedMobs.put(id, count);
    }

    public boolean hasMobKills() {
        if (this.killedMobs == null) {
            return false;
        }
        return this.killedMobs.size() > 0;
    }

    public int getMobKills(int id) {
        Integer mob = this.killedMobs.get(Integer.valueOf(id));
        if (mob == null) {
            return 0;
        }
        return mob;
    }

    public Map<Integer, Integer> getMobKills() {
        return this.killedMobs;
    }

    public long getCompletionTime() {
        return this.completionTime;
    }

    public void setCompletionTime(long completionTime) {
        this.completionTime = completionTime;
    }

    public int getForfeited() {
        return this.forfeited;
    }

    public void setForfeited(int forfeited) {
        if (forfeited >= this.forfeited) {
            this.forfeited = forfeited;
        } else {
            throw new IllegalArgumentException("Can't set forfeits to something lower than before.");
        }
    }

    public void setCustomData(String customData) {

        this.customData = customData;
    }

    public String getCustomData() {
        return this.customData;
    }

    public boolean isDailyQuest() {
        switch (this.quest.getId()) {
            case 11463:
            case 11464:
            case 11465:
            case 11468:
                return true;
            case 11466:
            case 11467:
        }
        return false;
    }
}
