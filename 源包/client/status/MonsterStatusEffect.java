package client.status;

import client.MapleCharacter;
import java.lang.ref.WeakReference;
import java.util.TimerTask;
import server.life.MapleMonster;
import server.life.MobSkill;

public class MonsterStatusEffect {

    private MonsterStatus stati;
    private final int skill;
    private final MobSkill mobskill;
    private final boolean monsterSkill;
    private WeakReference<MapleCharacter> weakChr = null;
    private Integer x;
    private int poisonSchedule = 0;
    private boolean reflect = false;
    private long cancelTime = 0L;
    private long dotTime = 0L;
    private int count = 0;
    private boolean newpoison = true;

    public MonsterStatusEffect(MonsterStatus stat, Integer x, int skillId, MobSkill mobskill, boolean monsterSkill) {
        this.stati = stat;
        this.x = x;
        this.skill = skillId;
        this.mobskill = mobskill;
        this.monsterSkill = monsterSkill;
    }

    public MonsterStatusEffect(MonsterStatus stat, Integer x, int skillId, MobSkill mobskill, boolean monsterSkill, boolean reflect) {
        this.stati = stat;
        this.x = x;
        this.skill = skillId;
        this.mobskill = mobskill;
        this.monsterSkill = monsterSkill;
        this.reflect = reflect;
    }

    public MonsterStatus getStati() {
        return this.stati;
    }

    public WeakReference<MapleCharacter> getchr() {
        return this.weakChr;
    }

    public Integer getX() {
        return this.x;
    }

    public void setValue(MonsterStatus status, Integer newVal) {
        this.stati = status;
        this.x = newVal;
    }

    public int getSkill() {
        return this.skill;
    }

    public MobSkill getMobSkill() {
        return this.mobskill;
    }

    public boolean isMonsterSkill() {
        return this.monsterSkill;
    }

    public void setCancelTask(long cancelTask) {
        this.cancelTime = (System.currentTimeMillis() + cancelTask);
    }

    public long getCancelTask() {
        return this.cancelTime;
    }

    public void setDotTime(long duration) {
        this.dotTime = duration;
    }

    public long getDotTime() {
        return this.dotTime;
    }

    public void setPoisonSchedule(int poisonSchedule, MapleCharacter chrr) {
        if (this.weakChr == null) {
            this.poisonSchedule = poisonSchedule;
            this.weakChr = new WeakReference(chrr);
        }
    }

    public int getPoisonSchedule() {
        return this.poisonSchedule;
    }

    public boolean shouldCancel(long now) {
        return (this.cancelTime > 0L) && (this.cancelTime <= now);
    }

    public void cancelTask() {
        this.cancelTime = 0L;
    }

    public void setnewpoison(boolean s) {
        this.newpoison = s;
    }

    public boolean isReflect() {
        return this.reflect;
    }

    public int getFromID() {
        return (this.weakChr == null) || (this.weakChr.get() == null) ? 0 : ((MapleCharacter) this.weakChr.get()).getId();
    }

    public void cancelPoisonSchedule(MapleMonster mm) {
        mm.doPoison(this, this.weakChr);
        this.poisonSchedule = 0;
        this.weakChr = null;
    }

    public void scheduledoPoison(final MapleMonster mon) {
        final java.util.Timer timer = new java.util.Timer(true);
        final long time = System.currentTimeMillis();
        final MonsterStatusEffect eff = this;
        if (newpoison) {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    if (time + getDotTime() > System.currentTimeMillis() && mon.isAlive()) {
                        //每次需要执行的代码放到这里面。
                        if (weakChr.get().isShowPacket()) {
                            weakChr.get().dropSpouseMessage(18, "[持续伤害] 持续伤害");
                        }
                        setnewpoison(false);
                        mon.doPoison(eff, weakChr);
                    } else {
                        setnewpoison(true);
                        //cancelPoisonSchedule(mon);
                        timer.cancel();
                    }
                }
            };
            timer.schedule(task, 0, 1000);
        }
    }

    public int getcount() {
        return this.count;
    }

    public int setcount(int x) {
        return this.count = x;
    }

}
