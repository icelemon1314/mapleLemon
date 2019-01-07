package server.life;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleDisease;
import client.Skill;
import client.SkillFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.GameConstants;
import constants.ItemConstants;
import handling.channel.ChannelServer;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import scripting.event.EventInstanceManager;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import server.Randomizer;
import server.Timer;
import server.Timer.EtcTimer;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.skill.冒险家.侠客;
import server.skill.冒险家.冰雷巫师;
import server.skill.冒险家.无影人;
import server.skill.冒险家.火毒巫师;
import tools.ConcurrentEnumMap;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.packet.MobPacket;
import tools.packet.SkillPacket;

public final class MapleMonster extends AbstractLoadedMapleLife {

    private MapleMonsterStats stats;
    private ChangeableStats ostats = null;
    private long hp;
    private long nextKill = 0L;
    private long lastDropTime = 0L;
    private int mp;
    private byte carnivalTeam = -1;
    private MapleMap map;
    private WeakReference<MapleMonster> sponge = new WeakReference(null);
    private int linkoid = 0;
    private int lastNode = -1;
    private int highestDamageChar = 0;
    private int linkCID = 0;
    private WeakReference<MapleCharacter> controller = new WeakReference(null);
    private boolean fake = false;
    private boolean dropsDisabled = false;
    private boolean controllerHasAggro = false;
    private final Collection<AttackerEntry> attackers = new LinkedList();
    private EventInstanceManager eventInstance;
    private MonsterListener listener = null;
    private byte[] reflectpack = null;
    private byte[] nodepack = null;
    private final ConcurrentEnumMap<MonsterStatus, MonsterStatusEffect> stati = new ConcurrentEnumMap<>(MonsterStatus.class);
    private final LinkedList<MonsterStatusEffect> poisons = new LinkedList();
    private final ReentrantReadWriteLock poisonsLock = new ReentrantReadWriteLock();
    private Map<Integer, Long> usedSkills;
    private int stolen = -1;
    private boolean shouldDropItem = false;
    private boolean killed = false;
    private int triangulation = 0;
    private boolean mark = false;
    private int 怪物类型 = -1;
    private boolean special = false;

    public MapleMonster(int id, MapleMonsterStats stats) {
        super(id);
        initWithStats(stats);
        setpecial(id);
    }

    public MapleMonster(MapleMonster monster) {
        super(monster);
        initWithStats(monster.stats);
        setpecial(monster.getId());
    }

    private void initWithStats(MapleMonsterStats stats) {
        setStance(5);
        this.stats = stats;
        this.hp = stats.getHp();
        this.mp = stats.getMp();

        if (stats.getNoSkills() > 0) {
            this.usedSkills = new HashMap();
        }
    }

    public ArrayList<AttackerEntry> getAttackers() {
        if ((this.attackers == null) || (this.attackers.size() <= 0)) {
            return new ArrayList();
        }
        ArrayList ret = new ArrayList();
        for (AttackerEntry e : this.attackers) {
            if (e != null) {
                ret.add(e);
            }
        }
        return ret;
    }

    public MapleMonsterStats getStats() {
        return this.stats;
    }

    public void disableDrops() {
        this.dropsDisabled = true;
    }

    public boolean dropsDisabled() {
        return this.dropsDisabled;
    }

    public boolean isSpecial() {
        return this.special;
    }

    public void setpecial(int id) {
        switch (id) {
            case 8910000:
            case 8910100:
                special = true;
                break;
        }
    }

    public void set怪物类型(int 怪物类型) {
        this.怪物类型 = 怪物类型;
    }

    public void setSponge(MapleMonster mob) {
        this.sponge = new WeakReference(mob);
        if (this.linkoid <= 0) {
            this.linkoid = mob.getObjectId();
        }
    }

    public void setMap(MapleMap map) {
        this.map = map;
        startDropItemSchedule();
    }

    public int getMobLevel() {
        if (this.ostats != null) {
            return this.ostats.level;
        }
        return this.stats.getLevel();
    }

    public long getHp() {
        return this.hp;
    }

    public void setHp(long hp) {
        this.hp = hp;
    }

    public ChangeableStats getChangedStats() {
        return this.ostats;
    }

    public long getMobMaxHp() {
        if (this.ostats != null) {
            return this.ostats.hp;
        }
        return this.stats.getHp();
    }

    public int getMp() {
        return this.mp;
    }

    public void setMp(int mp) {
        if (mp < 0) {
            mp = 0;
        }
        this.mp = mp;
    }

    public int getMobMaxMp() {
        if (this.ostats != null) {
            return this.ostats.mp;
        }
        return this.stats.getMp();
    }

    public int getMobExp() {
        if (this.ostats != null) {
            return this.ostats.exp;
        }
        return this.stats.getExp();
    }

    public void setOverrideStats(OverrideMonsterStats ostats) {
        this.ostats = new ChangeableStats(this.stats, ostats);
        this.hp = ostats.getHp();
        this.mp = ostats.getMp();
    }

    public void changeLevel(int newLevel) {
        changeLevel(newLevel, true);
    }

    public void changeLevel(int newLevel, boolean pqMob) {
        if (!this.stats.isChangeable()) {
            return;
        }
        this.ostats = new ChangeableStats(this.stats, newLevel, pqMob);
        this.hp = this.ostats.getHp();
        this.mp = this.ostats.getMp();
    }

    public void changeLevelmod(int newLevel, int multipler) {
        if (!this.stats.isChangeable()) {
            return;
        }
        this.ostats = new ChangeableStats(this.stats, newLevel, multipler);
        this.hp = this.ostats.getHp();
        this.mp = this.ostats.getMp();
    }

    public MapleMonster getSponge() {
        return (MapleMonster) this.sponge.get();
    }

    public void damage(MapleCharacter from, long damage, boolean updateAttackTime) {
        damage(from, damage, updateAttackTime, 0);
    }

    /**
     * 对怪物造成伤害
     * @param from
     * @param damage
     * @param updateAttackTime
     * @param lastSkill
     */
    public void damage(MapleCharacter from, long damage, boolean updateAttackTime, int lastSkill) {
        if ((from == null) || (damage <= 0L) || (!isAlive())) {
            return;
        }
        AttackerEntry attacker = null;
        if (from.getParty() != null) {
            attacker = new PartyAttackerEntry(from.getParty().getId());
        } else {
            attacker = new SingleAttackerEntry(from);
        }
        boolean replaced = false;
        for (AttackerEntry aentry : getAttackers()) {
            if ((aentry != null) && (aentry.equals(attacker))) {
                attacker = aentry;
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            this.attackers.add(attacker);
        }
        long rDamage = Math.max(0L, Math.min(damage, this.hp));
        attacker.addDamage(from, rDamage, updateAttackTime);

        if (this.stats.getSelfD() != -1) {
            this.hp -= rDamage;
            if (this.hp > 0L) {
                if (this.hp < this.stats.getSelfDHp()) {
                    this.map.killMonster(this, from, false, false, this.stats.getSelfD(), lastSkill);
                } else {
                    for (AttackerEntry mattacker : getAttackers()) {
                        for (AttackingMapleCharacter cattacker : mattacker.getAttackers()) {
                            if ((cattacker.getAttacker().getMap() == from.getMap())
                                    && (cattacker.getLastAttackTime() >= System.currentTimeMillis() - 4000L)) {
                                cattacker.getAttacker().getClient().getSession().write(MobPacket.showMonsterHP(getObjectId(), getHPPercent()));
                            }
                        }
                    }
                }
            } else {
                this.map.killMonster(this, from, true, false, (byte) 1, lastSkill);
            }
        } else {
            if (this.sponge.get() != null) {
                if (((MapleMonster) this.sponge.get()).hp > 0L) {
                    ((MapleMonster) this.sponge.get()).hp -= rDamage;
                    if (((MapleMonster) this.sponge.get()).hp <= 0L) {
                        this.map.broadcastMessage(MobPacket.showBossHP((this.sponge.get()).getId(), -1L, ((MapleMonster) this.sponge.get()).getMobMaxHp()));
                        this.map.killMonster(this.sponge.get(), from, true, false, (byte) 1, lastSkill);
                    } else {
                        this.map.broadcastMessage(MobPacket.showBossHP( this.sponge.get()));
                    }
                }
            }
            if (this.hp > 0L) {
                this.hp -= rDamage;
                if (this.eventInstance != null) {
                    this.eventInstance.monsterDamaged(from, this, (int) rDamage);
                } else {
                    EventInstanceManager em = from.getEventInstance();
                    if (em != null) {
                        em.monsterDamaged(from, this, (int) rDamage);
                    }
                }
                if ((this.sponge.get() == null) && (this.hp > 0L)) {
                    switch (this.stats.getHPDisplayType()) {
                        case 0:
                            this.map.broadcastMessage(MobPacket.showBossHP(this), getTruePosition());
                            break;
                        case 1:
                            this.map.broadcastMessage(from, MobPacket.damageFriendlyMob(this, damage, true), false);
                            break;
                        case 2:
                            this.map.broadcastMessage(MobPacket.showMonsterHP(getObjectId(), getHPPercent()));
                            from.mulung_EnergyModify(true);
                            break;
                        case 3:
                            for (AttackerEntry mattacker : getAttackers()) {
                                if (mattacker != null) {
                                    for (AttackingMapleCharacter cattacker : mattacker.getAttackers()) {
                                        if ((cattacker != null) && (cattacker.getAttacker().getMap() == from.getMap())
                                                && (cattacker.getLastAttackTime() >= System.currentTimeMillis() - 4000L)) {
                                            cattacker.getAttacker().getClient().getSession().write(MobPacket.showMonsterHP(getObjectId(), getHPPercent()));
                                        }
                                    }
                                }
                            }
                    }

                }

                if (this.hp <= 0L) {
                    if (this.stats.getHPDisplayType() == 0) {
                        this.map.broadcastMessage(MobPacket.showBossHP(getId(), -1L, getMobMaxHp()), getTruePosition());
                    }
                    this.map.killMonster(this, from, true, false, (byte) 1, lastSkill);
                }
            }
        }
        startDropItemSchedule();
    }

    public int getHPPercent() {
        return (int) Math.ceil(this.hp * 100.0D / getMobMaxHp());
    }

    public void heal(int hp, int mp, boolean broadcast) {
        long TotalHP = getHp() + hp;
        int TotalMP = getMp() + mp;
        if (TotalHP >= getMobMaxHp()) {
            setHp(getMobMaxHp());
        } else {
            setHp(TotalHP);
        }
        if (TotalMP >= getMp()) {
            setMp(getMp());
        } else {
            setMp(TotalMP);
        }
        if (broadcast) {
            this.map.broadcastMessage(MobPacket.healMonster(getObjectId(), hp));
        } else if (this.sponge.get() != null) {
            ((MapleMonster) this.sponge.get()).hp += hp;
        }
    }

    public void killed() {
        if (this.listener != null) {
            this.listener.monsterKilled();
        }
        this.listener = null;
    }

    private void giveExpToCharacter(MapleCharacter attacker, int exp, boolean 最高伤害, int numExpSharers, byte pty, byte Class_Bonus_EXP_PERCENT, byte Premium_Bonus_EXP_PERCENT, int lastskillID) {
        if (最高伤害) {
            if (this.eventInstance != null) {
                this.eventInstance.monsterKilled(attacker, this);
            } else {
                EventInstanceManager em = attacker.getEventInstance();
                if (em != null) {
                    em.monsterKilled(attacker, this);
                }
            }
            this.highestDamageChar = attacker.getId();
        }
        if (exp > 0) {
            if (attacker.hasDisease(MapleDisease.诅咒)) {
                exp /= 2;
            }

            int acExpRate = ChannelServer.getInstance(map.getChannel()).getExpRate(attacker.getWorld());
            if ((attacker.getLevel() < 10) && (GameConstants.is新手职业(0))) {
                acExpRate = 1;
            }
            long gainexp = exp * acExpRate;
            exp = (int) Math.min(1000000000, gainexp);

            int Class_Bonus_EXP = 0;
            if (Class_Bonus_EXP_PERCENT > 0) {
                Class_Bonus_EXP = (int) (exp / 100.0D * Class_Bonus_EXP_PERCENT);
            }
            attacker.gainExpMonster(exp, true, 最高伤害, pty, Class_Bonus_EXP, this);
        }
        attacker.mobKilled(getId(), lastskillID);
    }

    public void killGainExp(int lastSkill) {
        int totalBaseExp = getMobExp();
        AttackerEntry highest = null;
        long highdamage = 0L;
        List<AttackerEntry> list = getAttackers();
        for (AttackerEntry attackEntry : list) {
            if ((attackEntry != null) && (attackEntry.getDamage() > highdamage)) {
                highest = attackEntry;
                highdamage = attackEntry.getDamage();
            }
        }

        for (AttackerEntry attackEntry : list) {
            if (attackEntry != null) {
                int baseExp = (int) Math.ceil(totalBaseExp * ((double) attackEntry.getDamage() / getMobMaxHp()));
                attackEntry.killedMob(getMap(), baseExp, attackEntry == highest, lastSkill);
            }
        }
    }

    public int killBy(MapleCharacter killer, int lastSkill) {
        if (this.killed) {
            return -1;
        }
        this.killed = true;
        MapleCharacter controll = (MapleCharacter) this.controller.get();
        if (controll != null) {
            controll.getClient().getSession().write(MobPacket.stopControllingMonster(getObjectId()));
            controll.stopControllingMonster(this);
        }
        int achievement = 0;
        switch (getId()) {
            case 9400121:
                achievement = 12;
                break;
            case 8500002:
                achievement = 13;
                break;
            case 8510000:
            case 8520000:
                achievement = 14;
                break;
        }

        spawnRevives(getMap());
        if (this.eventInstance != null) {
            this.eventInstance.unregisterMonster(this);
            this.eventInstance = null;
        }
        this.hp = 0L;
        MapleMonster oldSponge = getSponge();
        this.sponge = new WeakReference(null);
        if ((oldSponge != null) && (oldSponge.isAlive())) {
            boolean set = true;
            for (MapleMapObject mon : this.map.getAllMonstersThreadsafe()) {
                MapleMonster mons = (MapleMonster) mon;
                if ((mons.isAlive()) && (mons.getObjectId() != oldSponge.getObjectId()) && (mons.getStats().getLevel() > 1) && (mons.getObjectId() != getObjectId()) && ((mons.getSponge() == oldSponge) || (mons.getLinkOid() == oldSponge.getObjectId()))) {
                    set = false;
                    break;
                }
            }
            if (set) {
                this.map.killMonster(oldSponge, killer, true, false, (byte) 1);
            }
        }

        this.reflectpack = null;
        this.nodepack = null;
        if (this.stati.size() > 0) {
            List<MonsterStatus> statuses = new LinkedList(this.stati.keySet());
            for (MonsterStatus ms : statuses) {
                cancelStatus(ms);
            }
            statuses.clear();
        }
        if (this.poisons.size() > 0) {
            List<MonsterStatusEffect> ps = new LinkedList();
            this.poisonsLock.readLock().lock();
            try {
                ps.addAll(this.poisons);
            } finally {
                this.poisonsLock.readLock().unlock();
            }
            for (MonsterStatusEffect p : ps) {
                cancelSingleStatus(p);
            }
            ps.clear();
        }

        cancelDropItem();
        int v1 = this.highestDamageChar;
        this.highestDamageChar = 0;
        return v1;
    }

    public void spawnRevives(MapleMap map) {
        List<Integer> toSpawn = stats.getRevives();
        if ((toSpawn == null) || (getLinkCID() > 0)) {
            return;
        }
        for (final int i : toSpawn) {
            final MapleMonster mob = MapleLifeFactory.getMonster(i);

            if (eventInstance != null) {
                eventInstance.registerMonster(mob);
            }
            mob.setPosition(getTruePosition());
            if (dropsDisabled()) {
                mob.disableDrops();
            }
            int objId = this.getObjectId();
            if (this.getId() == 5100001 || this.getId() == 5130106) {
                Timer.MapTimer.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        map.spawnRevives(mob, objId);
                    }
                }, 2800L);
            } else {
                map.spawnRevives(mob, this.getObjectId());
            }

        }
    }

    public boolean isAlive() {
        return this.hp > 0L;
    }

    public void setCarnivalTeam(byte team) {
        this.carnivalTeam = team;
    }

    public byte getCarnivalTeam() {
        return this.carnivalTeam;
    }

    public MapleCharacter getController() {
        return (MapleCharacter) this.controller.get();
    }

    public void setController(MapleCharacter controller) {
        this.controller = new WeakReference(controller);
    }

    public void switchController(MapleCharacter newController, boolean immediateAggro) {
        MapleCharacter controllers = getController();
        if (controllers == newController) {
            return;
        }
        if (controllers != null) {
            controllers.stopControllingMonster(this);
            controllers.getClient().getSession().write(MobPacket.stopControllingMonster(getObjectId()));
            sendStatus(controllers.getClient());
        }
        newController.controlMonster(this, immediateAggro);
        setController(newController);
        if (immediateAggro) {
            setControllerHasAggro(true);
        }
    }

    public void addListener(MonsterListener listener) {
        this.listener = listener;
    }

    public boolean isControllerHasAggro() {
        return this.controllerHasAggro;
    }

    public void setControllerHasAggro(boolean controllerHasAggro) {
        this.controllerHasAggro = controllerHasAggro;
    }

    public void sendStatus(MapleClient client) {
        if (this.reflectpack != null) {
            client.getSession().write(this.reflectpack);
        }
        if (this.poisons.size() > 0) {
            this.poisonsLock.readLock().lock();
            try {
                client.getSession().write(MobPacket.applyMonsterPoisonStatus(this, this.poisons,100));
            } finally {
                this.poisonsLock.readLock().unlock();
            }
        }
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        if (!isAlive()) {
            return;
        }
        client.getSession().write(MobPacket.spawnMonster(this, (fake) && (this.linkCID <= 0) ? -4 : -1, 0));//进入地图发的召唤怪物
        sendStatus(client);
        if (this.map != null && !this.stats.isEscort() && client.getPlayer() != null && (client.getPlayer().getTruePosition().distance(getTruePosition()) <= GameConstants.maxViewRangeSq())) {
            this.map.updateMonsterController(this);
        }
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        if ((this.stats.isEscort()) && (getEventInstance() != null) && (this.lastNode >= 0)) {
            this.map.resetShammos(client);
        } else {
            client.getSession().write(MobPacket.killMonster(getObjectId(), 0));
            if ((getController() != null) && (client.getPlayer() != null) && (client.getPlayer().getId() == getController().getId())) {
                client.getPlayer().stopControllingMonster(this);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.stats.getName());
        sb.append("(");
        sb.append(getId());
        sb.append(") 等级:");
        sb.append(this.stats.getLevel());
        if (this.ostats != null) {
            sb.append("→");
            sb.append(this.ostats.level);
        }
        sb.append(" 坐标(X:");
        sb.append(getTruePosition().x);
        sb.append("/Y:");
        sb.append(getTruePosition().y);
        sb.append(") 信息: ");
        sb.append(getHp());
        sb.append("/");
        sb.append(getMobMaxHp());
        sb.append("Hp, ");
        sb.append(getMp());
        sb.append("/");
        sb.append(getMobMaxMp());
        sb.append("Mp, oid: ");
        sb.append(getObjectId());
        sb.append("||仇恨目标: ");
        MapleCharacter chr = (MapleCharacter) this.controller.get();
        sb.append(chr != null ? chr.getName() : "无");
        return sb.toString();
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.MONSTER;
    }

    public EventInstanceManager getEventInstance() {
        return this.eventInstance;
    }

    public void setEventInstance(EventInstanceManager eventInstance) {
        this.eventInstance = eventInstance;
    }

    public int getStatusSourceID(MonsterStatus status) {
        if ((status == MonsterStatus.中毒) ) {
            this.poisonsLock.readLock().lock();
            try {
                for (MonsterStatusEffect ps : this.poisons) {
                    if (ps != null) {
                        int i = ps.getSkill();
                        return i;
                    }
                }
            } finally {
                this.poisonsLock.readLock().unlock();
            }
        }
        MonsterStatusEffect effect = (MonsterStatusEffect) this.stati.get(status);
        if (effect != null) {
            return effect.getSkill();
        }
        return -1;
    }

    public ElementalEffectiveness getEffectiveness(Element e) {
//        if ((this.stati.size() > 0) && (this.stati.containsKey(MonsterStatus.巫毒))) {
        if (this.stati.size() > 0) {
            return ElementalEffectiveness.NORMAL;
        }
        return this.stats.getEffectiveness(e);
    }

    /**
     * 给怪物加BUFF
     * @param from
     * @param status
     * @param poison
     * @param duration
     * @param checkboss
     * @param effect
     */
    public void applyStatus(MapleCharacter from, MonsterStatusEffect status, boolean poison, long duration, boolean checkboss, MapleStatEffect effect) {
        if ((!isAlive()) || (getLinkCID() > 0)) {
            return;
        }
        Skill skilz = SkillFactory.getSkill(status.getSkill());
        if (skilz != null) {
            switch (stats.getEffectiveness(skilz.getElement())) {
                case IMMUNE:
                case STRONG:
                    return;
                case NORMAL:
                case WEAK:
                    break;
                default:
                    throw new RuntimeException("Unknown elemental effectiveness: " + stats.getEffectiveness(skilz.getElement()));
            }
        }

        int statusSkill = status.getSkill();
        switch (statusSkill) {
            case 火毒巫师.火毒合击:
                switch (stats.getEffectiveness(Element.POISON)) {
                    case IMMUNE:
                    case STRONG:
                        return;
                }
                break;
            case 冰雷巫师.冰雷合击:
                switch (stats.getEffectiveness(Element.ICE)) {
                    case IMMUNE:
                    case STRONG:
                        return;
                }
                break;
        }

        MonsterStatus stat = status.getStati();
        if ((this.stats.isNoDoom())) {
            return;
        }
        if (this.stats.isBoss()) {
            if ((stat == MonsterStatus.眩晕) || (stat == MonsterStatus.速度)) {
                return;
            }
            if ((checkboss) && (stat != MonsterStatus.物攻) && (stat != MonsterStatus.中毒) ) {
                return;
            }

            if ((getId() == 8850011)) {
                return;
            }
        }
        if (((this.stats.isFriendly()) || (isFake())) && ((stat == MonsterStatus.眩晕) || (stat == MonsterStatus.速度) || (stat == MonsterStatus.中毒) )) {
            return;
        }

        if (((stat == MonsterStatus.中毒)) && (effect == null)) {
            return;
        }
        if (this.stati.containsKey(stat)) {
            cancelStatus(stat);
        }
        if ((stat == MonsterStatus.中毒) ) {
            this.poisonsLock.readLock().lock();
            try {
                for (MonsterStatusEffect mse : this.poisons) {
                    if ((mse != null) && ((mse.getSkill() == effect.getSourceId()) )) {
                        return;
                    }
                }
            } finally {
                this.poisonsLock.readLock().unlock();
            }
        }
        if ((poison) && (getHp() > 1L) && (effect != null)) {
            if (statusSkill == 火毒巫师.致命毒雾) {
                duration = effect.getDOTTime() * 1000;
            } else {
                duration = Math.max(duration, effect.getDOTTime() * 1000);
            }
        }

        duration += from.getStat().dotTime * 1000;

        if (duration >= 60000L) {
            duration = 10000L;
        }
        long aniTime = duration;
        status.setCancelTask(aniTime);
        if ((poison) && (getHp() > 1L)) {
            if (status.getchr() != null) {
                return;
            }
            status.setDotTime(duration);
            int poisonDot = from.getStat().dot;
            int damageIncrease = from.getStat().getDamageIncrease(effect.getSourceId());
            if (damageIncrease > effect.getDOT()) {
                poisonDot += damageIncrease;
            } else {
                poisonDot += effect.getDOT();
            }
            if (from.isShowPacket()) {
                from.dropSpouseMessage(18, "[持续伤害] 开始处理效果 - 技能ID：" + effect.getSourceId());
                from.dropSpouseMessage(18, "[持续伤害] 加成 - 技能ID：" + effect.getDOT() + " 被动： " + from.getStat().dot + " 被动加成： " + damageIncrease + " 最终加成：" + poisonDot);
            }
            status.setValue(status.getStati(), (int) (poisonDot * from.getStat().getCurrentMaxBaseDamage() / 100.0D));
            int poisonDamage = (int) (aniTime / 1000L * status.getX() / 2L);
            if (from.isShowPacket()) {
                from.dropSpouseMessage(18, "[持续伤害] 持续伤害： " + poisonDamage + " 持续时间：" + aniTime + " 持续掉血：" + status.getX());
            }
            status.setPoisonSchedule(poisonDamage, from);
            if (poisonDamage > 0) {
                if (poisonDamage >= this.hp) {
                    poisonDamage = (int) (this.hp - 1L);
                }
                damage(from, poisonDamage, false);
            }
        } else if ((statusSkill == 无影人.影网术)) {
            status.setValue(status.getStati(), (int) (getMobMaxHp() / 50.0D + 0.999D));
            status.setPoisonSchedule(status.getX(), from);
        }
        MapleCharacter con = getController();
        if ((stat == MonsterStatus.中毒) ) {
            this.poisonsLock.writeLock().lock();
            try {
                this.poisons.add(status);
                status.scheduledoPoison(this);
                if (con != null) {
                    this.map.broadcastMessage(con, MobPacket.applyMonsterPoisonStatus(this, this.poisons,(int)duration), getTruePosition());
                    con.getClient().getSession().write(MobPacket.applyMonsterPoisonStatus(this, this.poisons,(int)duration));
                } else {
                    this.map.broadcastMessage(MobPacket.applyMonsterPoisonStatus(this, this.poisons,(int)duration), getTruePosition());
                }
            } finally {
                this.poisonsLock.writeLock().unlock();
            }
        } else {
            this.stati.put(stat, status);
            if (con != null) {
                this.map.broadcastMessage(con, MobPacket.applyMonsterStatus(this, status,(int)duration), getTruePosition());
                // MapleMonster mons, MonsterStatusEffect ms
                // int oid, Map<MonsterStatus, Integer> stati, List<Integer> reflection, MobSkill skil
                con.getClient().getSession().write(MobPacket.applyMonsterStatus(this, status,(int)duration));
            } else {
                this.map.broadcastMessage(MobPacket.applyMonsterStatus(this, status,(int)duration), getTruePosition());
            }
        }
    }

    public void applyStatus(MonsterStatusEffect status) {
        if (this.stati.containsKey(status.getStati())) {
            cancelStatus(status.getStati());
        }
        this.stati.put(status.getStati(), status);
        this.map.broadcastMessage(MobPacket.applyMonsterStatus(this, status,status.getDotTime()), getTruePosition());
    }

    public void dispelSkill(MobSkill skillId) {
        List<MonsterStatus> toCancel = new ArrayList();
        for (Entry<MonsterStatus, MonsterStatusEffect> effects : this.stati.entrySet()) {
            MonsterStatusEffect mse = (MonsterStatusEffect) effects.getValue();
            if ((mse.getMobSkill() != null) && (mse.getMobSkill().getSkillId() == skillId.getSkillId())) {
                toCancel.add(effects.getKey());
            }
        }
        for (MonsterStatus stat : toCancel) {
            cancelStatus(stat);
        }
    }

    public void applyMonsterBuff(Map<MonsterStatus, Integer> effect, int skillId, long duration, MobSkill skill, List<Integer> reflection) {
        for (Map.Entry status : effect.entrySet()) {
            if (this.stati.containsKey((MonsterStatus) status.getKey())) {
                cancelStatus((MonsterStatus) status.getKey());
            }
            MonsterStatusEffect effectz = new MonsterStatusEffect((MonsterStatus) status.getKey(), (Integer) status.getValue(), 0, skill, true, reflection.size() > 0);
            effectz.setCancelTask(duration);
            this.stati.put((MonsterStatus) status.getKey(), effectz);
        }
        MapleCharacter con = getController();
        if (reflection.size() > 0) {
            this.reflectpack = MobPacket.applyMonsterStatus(getObjectId(), effect, reflection, skill,duration);
            if (con != null) {
                this.map.broadcastMessage(con, this.reflectpack, getTruePosition());
                con.getClient().getSession().write(this.reflectpack);
            } else {
                this.map.broadcastMessage(this.reflectpack, getTruePosition());
            }
        } else {
            for (Map.Entry status : effect.entrySet()) {
                if (con != null) {
                    this.map.broadcastMessage(con, MobPacket.applyMonsterStatus(getObjectId(), (MonsterStatus) status.getKey(), ((Integer) status.getValue()), skill,duration), getTruePosition());
                    con.getClient().getSession().write(MobPacket.applyMonsterStatus(getObjectId(), (MonsterStatus) status.getKey(), ((Integer) status.getValue()), skill,duration));
                } else {
                    this.map.broadcastMessage(MobPacket.applyMonsterStatus(getObjectId(), (MonsterStatus) status.getKey(), ((Integer) status.getValue()), skill,duration), getTruePosition());
                }
            }
        }
    }

    public void setTempEffectiveness(final Element e, long milli) {
        this.stats.setEffectiveness(e, ElementalEffectiveness.WEAK);
        EtcTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                MapleMonster.this.stats.removeEffectiveness(e);
            }
        }, milli);
    }

    public boolean isBuffed(MonsterStatus status) {
        if ((status == MonsterStatus.中毒) ) {
            return (this.poisons.size() > 0) || (this.stati.containsKey(status));
        }
        return this.stati.containsKey(status);
    }

    public MonsterStatusEffect getBuff(MonsterStatus status) {
        return (MonsterStatusEffect) this.stati.get(status);
    }

    public int getStatiSize() {
        return this.stati.size() + (this.poisons.size() > 0 ? 1 : 0);
    }

    public ArrayList<MonsterStatusEffect> getAllBuffs() {
        ArrayList ret = new ArrayList();
        for (MonsterStatusEffect e : this.stati.values()) {
            ret.add(e);
        }
        this.poisonsLock.readLock().lock();
        try {
            for (MonsterStatusEffect e : this.poisons) {
                ret.add(e);
            }
        } finally {
            this.poisonsLock.readLock().unlock();
        }
        return ret;
    }

    public void setFake(boolean fake) {
        this.fake = fake;
    }

    public boolean isFake() {
        return this.fake;
    }

    public MapleMap getMap() {
        return this.map;
    }

    public List<Pair<Integer, Integer>> getSkills() {
        return this.stats.getSkills();
    }

    public boolean hasSkill(int skillId, int level) {
        return this.stats.hasSkill(skillId, level);
    }

    public long getLastSkillUsed(int skillId) {
        if (this.usedSkills.containsKey(skillId)) {
            return (this.usedSkills.get(skillId));
        }
        return 0L;
    }

    public void setLastSkillUsed(int skillId, long now, long cooltime) {
        switch (skillId) {
            case 140:
                this.usedSkills.put(skillId, now + cooltime * 2L);
                this.usedSkills.put(141, now);
                break;
            case 141:
                this.usedSkills.put(skillId, now + cooltime * 2L);
                this.usedSkills.put(140, now + cooltime);
                break;
            default:
                this.usedSkills.put(skillId, now + cooltime);
        }
    }

    public byte getNoSkills() {
        return this.stats.getNoSkills();
    }

    public boolean isFirstAttack() {
        return this.stats.isFirstAttack();
    }

    public int getBuffToGive() {
        return this.stats.getBuffToGive();
    }

    public void doPoison(MonsterStatusEffect status, WeakReference<MapleCharacter> weakChr) {
        if (((status.getStati() == MonsterStatus.中毒)) && (this.poisons.size() <= 0)) {
            return;
        }
        if ((status.getStati() != MonsterStatus.中毒) && (!this.stati.containsKey(status.getStati()))) {
            return;
        }
        if (weakChr == null) {
            return;
        }
        long damage = status.getPoisonSchedule();
        boolean shadowWeb = (status.getSkill() == 无影人.影网术);
        MapleCharacter chr = weakChr.get();
        boolean cancel = (damage <= 0L) || (chr == null) || (chr.getMapId() != this.map.getId());
        if (damage >= this.hp) {
            damage = this.hp - 1L;
            cancel = (!shadowWeb) || (cancel);
        }
        if (!cancel) {
            damage(chr, damage, true);
            if (shadowWeb) {
                this.map.broadcastMessage(MobPacket.damageMonster(getObjectId(), damage), getTruePosition());
            }
        }
    }

    public int getLinkOid() {
        return this.linkoid;
    }

    public void setLinkOid(int lo) {
        this.linkoid = lo;
    }

    public ConcurrentEnumMap<MonsterStatus, MonsterStatusEffect> getStati() {
        return this.stati;
    }

    public void addEmpty() {
        for (MonsterStatus stat : MonsterStatus.values()) {
            if (stat.isEmpty()) {
                this.stati.put(stat, new MonsterStatusEffect(stat, 0, 0, null, false));
            }
        }
    }

    public int getStolen() {
        return this.stolen;
    }

    public void setStolen(int s) {
        this.stolen = s;
    }

    /**
     * 怪物被偷东西
     * @param chr
     */
    public void handleSteal(MapleCharacter chr) {
        double showdown = 100.0D;
        Skill steal = SkillFactory.getSkill(侠客.神通术);
        int level = chr.getTotalSkillLevel(steal);
        int chServerrate = ChannelServer.getInstance(chr.getClient().getChannel()).getDropRate(chr.getWorld());
        if ((level > 0) && (!getStats().isBoss()) && (this.stolen == -1) && (steal.getEffect(level).makeChanceResult())) {
            MapleMonsterInformationProvider mi = MapleMonsterInformationProvider.getInstance();
            List de = mi.retrieveDrop(getId());
            if (de == null) {
                this.stolen = 0;
                return;
            }
            List<MonsterDropEntry> dropEntry = new ArrayList(de);

            for (MonsterDropEntry d : dropEntry) {
                if ((d.itemId > 0) && (d.questid == 0) && (d.itemId / 10000 != 238) && (Randomizer.nextInt(GameConstants.DROP_ITEM_PER) < (int) (10 * d.chance * chServerrate * chr.getDropMod() * (chr.getStat().getDropBuff() / 100.0D) * (showdown / 100.0D)))) {
                    Item idrop;
                    if (ItemConstants.getInventoryType(d.itemId) == MapleInventoryType.EQUIP) {
                        Equip eq = (Equip) MapleItemInformationProvider.getInstance().getEquipById(d.itemId);
                        idrop = MapleItemInformationProvider.getInstance().randomizeStats(eq);
                    } else {
                        idrop = new Item(d.itemId, (byte) 0, (short) (d.Maximum != 1 ? Randomizer.nextInt(d.Maximum - d.Minimum) + d.Minimum : 1), (short) 0);
                    }
                    this.stolen = d.itemId;
                    this.map.spawnMobDrop(idrop, this.map.calcDropPos(getPosition(), getTruePosition()), this, chr, (byte) 0, 0);
                    break;
                }
            }
        }
    }

    public void setLastNode(int lastNode) {
        this.lastNode = lastNode;
    }

    public int getLastNode() {
        return this.lastNode;
    }

    public void cancelStatus(MonsterStatus stat) {
        MonsterStatusEffect mse = (MonsterStatusEffect) this.stati.get(stat);
        if ((mse == null) || (!isAlive())) {
            return;
        }
        if (mse.isReflect()) {
            this.reflectpack = null;
        }
        mse.cancelPoisonSchedule(this);
        MapleCharacter con = getController();
        if (con != null) {
            this.map.broadcastMessage(con, MobPacket.cancelMonsterStatus(getObjectId(), stat), getTruePosition());
            con.getClient().getSession().write(MobPacket.cancelMonsterStatus(getObjectId(), stat));
        } else {
            this.map.broadcastMessage(MobPacket.cancelMonsterStatus(getObjectId(), stat), getTruePosition());
        }
        this.stati.remove(stat);
    }

    public void cancelSingleStatus(MonsterStatusEffect stat) {
        if ((stat == null)  || (!isAlive())) {
            return;
        }
        if ((stat.getStati() != MonsterStatus.中毒) ) {
            cancelStatus(stat.getStati());
            return;
        }
        this.poisonsLock.writeLock().lock();
        try {
            if (!this.poisons.contains(stat)) {
                return;
            }
            this.poisons.remove(stat);
            if (stat.isReflect()) {
                this.reflectpack = null;
            }
            stat.cancelPoisonSchedule(this);
            MapleCharacter con = getController();
            if (con != null) {
                this.map.broadcastMessage(con, MobPacket.cancelMonsterPoisonStatus(getObjectId(), stat), getTruePosition());
                con.getClient().getSession().write(MobPacket.cancelMonsterPoisonStatus(getObjectId(), stat));
            } else {
                this.map.broadcastMessage(MobPacket.cancelMonsterPoisonStatus(getObjectId(), stat), getTruePosition());
            }
        } finally {
            this.poisonsLock.writeLock().unlock();
        }
    }

    public void cancelDropItem() {
        this.lastDropTime = 0L;
    }

    public void startDropItemSchedule() {
        cancelDropItem();
        if ((this.stats.getDropItemPeriod() <= 0) || (!isAlive())) {
            return;
        }
        this.shouldDropItem = false;
        this.lastDropTime = System.currentTimeMillis();
    }

    public boolean shouldDrop(long now) {
        return (this.lastDropTime > 0L) && (this.lastDropTime + this.stats.getDropItemPeriod() * 1000 < now);
    }

    public void doDropItem(long now) {
        int itemId;
        switch (getId()) {
            case 9300061:
                itemId = 4001101;
                break;
            default:
                cancelDropItem();
                return;
        }
        if ((isAlive()) && (this.map != null)) {
            if (this.shouldDropItem) {
                this.map.spawnAutoDrop(itemId, getTruePosition());
            } else {
                this.shouldDropItem = true;
            }
        }
        this.lastDropTime = now;
    }

    public byte[] getNodePacket() {
        return this.nodepack;
    }

    public void setNodePacket(byte[] np) {
        this.nodepack = np;
    }

    public void registerKill(long next) {
        this.nextKill = (System.currentTimeMillis() + next);
    }

    public boolean shouldKill(long now) {
        return (this.nextKill > 0L) && (now > this.nextKill);
    }

    public int getLinkCID() {
        return this.linkCID;
    }

    public void setLinkCID(int lc) {
        this.linkCID = lc;
        if (lc > 0) {
//            this.stati.put(MonsterStatus.心灵控制, new MonsterStatusEffect(MonsterStatus.心灵控制, 60000, 30001062, null, false));
        }
    }

    private class PartyAttackerEntry
            implements MapleMonster.AttackerEntry {

        private long totDamage = 0L;
        private final Map<Integer, MapleMonster.OnePartyAttacker> attackers = new HashMap(6);
        private final int partyid;

        public PartyAttackerEntry(int partyid) {
            this.partyid = partyid;
        }

        @Override
        public List<MapleMonster.AttackingMapleCharacter> getAttackers() {
            List ret = new ArrayList(this.attackers.size());
            for (Map.Entry entry : this.attackers.entrySet()) {
                MapleCharacter chr = MapleMonster.this.map.getCharacterById(((Integer) entry.getKey()).intValue());
                if (chr != null) {
                    ret.add(new MapleMonster.AttackingMapleCharacter(chr, ((MapleMonster.OnePartyAttacker) entry.getValue()).lastAttackTime));
                }
            }
            return ret;
        }

        private Map<MapleCharacter, MapleMonster.OnePartyAttacker> resolveAttackers() {
            Map ret = new HashMap(this.attackers.size());
            for (Map.Entry aentry : this.attackers.entrySet()) {
                MapleCharacter chr = MapleMonster.this.map.getCharacterById(((Integer) aentry.getKey()).intValue());
                if (chr != null) {
                    ret.put(chr, aentry.getValue());
                }
            }
            return ret;
        }

        @Override
        public boolean contains(MapleCharacter chr) {
            return this.attackers.containsKey(chr.getId());
        }

        @Override
        public long getDamage() {
            return this.totDamage;
        }

        @Override
        public void addDamage(MapleCharacter from, long damage, boolean updateAttackTime) {
            MapleMonster.OnePartyAttacker oldPartyAttacker = (MapleMonster.OnePartyAttacker) this.attackers.get(Integer.valueOf(from.getId()));
            if (oldPartyAttacker != null) {
                oldPartyAttacker.damage += damage;
                oldPartyAttacker.lastKnownParty = from.getParty();
                if (updateAttackTime) {
                    oldPartyAttacker.lastAttackTime = System.currentTimeMillis();
                }

            } else {
                MapleMonster.OnePartyAttacker onePartyAttacker = new MapleMonster.OnePartyAttacker(from.getParty(), damage);
                this.attackers.put(from.getId(), onePartyAttacker);
                if (!updateAttackTime) {
                    onePartyAttacker.lastAttackTime = 0L;
                }
            }
            this.totDamage += damage;
        }

        @Override
        public void killedMob(MapleMap map, int baseExp, boolean mostDamage, int lastSkill) {
            MapleCharacter highest = null;
            long highestDamage = 0L;
            int iexp = 0;
            double addedPartyLevel;
            //Map expMap = new HashMap(6);
            List<MapleCharacter> expApplicable;
            byte added_partyinc, 职业奖励经验, 召回戒指经验;
            double innerBaseExp;
            Map<MapleCharacter, ExpMap> expMap = new HashMap<>(6);
            for (final Entry<MapleCharacter, OnePartyAttacker> attacker : resolveAttackers().entrySet()) {
                MapleParty party = ((MapleMonster.OnePartyAttacker) attacker.getValue()).lastKnownParty;
                addedPartyLevel = 0.0D;
                added_partyinc = 0;
                职业奖励经验 = 0;
                召回戒指经验 = 0;
                expApplicable = new ArrayList();
                for (MaplePartyCharacter partychar : party.getMembers()) {
                    if ((((MapleCharacter) attacker.getKey()).getLevel() - partychar.getLevel() <= 5) || (MapleMonster.this.stats.getLevel() - partychar.getLevel() <= 5)) {
                        MapleCharacter pchr = map.getCharacterById(partychar.getId());
                        if ((pchr != null) && (pchr.isAlive())) {
                            expApplicable.add(pchr);
                            addedPartyLevel += pchr.getLevel();

                            职业奖励经验 = (byte) (职业奖励经验 + pchr.get精灵祝福());
                            if ((pchr.getStat().equippedWelcomeBackRing) && (召回戒指经验 == 0)) {
                                召回戒指经验 = 80;
                            }
                            if ((pchr.getStat().hasPartyBonus) && (added_partyinc < 4) && (map.getPartyBonusRate() <= 0)) {
                                added_partyinc = (byte) (added_partyinc + 1);
                            }
                        }
                    }
                }
                long iDamage = ((MapleMonster.OnePartyAttacker) attacker.getValue()).damage;
                if (iDamage > highestDamage) {
                    highest = (MapleCharacter) attacker.getKey();

                    highestDamage = iDamage;
                }
                innerBaseExp = baseExp * ((double) iDamage / this.totDamage);
                if (expApplicable.size() <= 1) {
                    职业奖励经验 = 0;

                }

                for (MapleCharacter expReceiver : expApplicable) {
                    iexp = expMap.get(expReceiver) == null ? 0 : ((MapleMonster.ExpMap) expMap.get(expReceiver)).exp;
                    double levelMod = expReceiver.getLevel() / addedPartyLevel * 0.4D;
                    iexp += (int) Math.round(((((MapleCharacter) attacker.getKey()).getId() == expReceiver.getId() ? 0.6D : 0.0D) + levelMod) * innerBaseExp);
                    expMap.put(expReceiver, new MapleMonster.ExpMap(iexp, (byte) (expApplicable.size() + added_partyinc), 职业奖励经验, 召回戒指经验));
                }
            }

            for (Entry<MapleCharacter, ExpMap> expReceiver : expMap.entrySet()) {
                MapleMonster.ExpMap expmap = (MapleMonster.ExpMap) expReceiver.getValue();
                MapleMonster.this.giveExpToCharacter((MapleCharacter) expReceiver.getKey(), expmap.exp, expReceiver.getKey() == highest, expMap.size(), expmap.ptysize, expmap.职业奖励经验, expmap.召回戒指经验, lastSkill);
            }
        }

        @Override
        public int hashCode() {
            int prime = 31;
            int result = 1;
            result = prime * result + this.partyid;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            PartyAttackerEntry other = (PartyAttackerEntry) obj;

            return this.partyid == other.partyid;
        }
    }

    private static final class OnePartyAttacker {

        public MapleParty lastKnownParty;
        public long damage;
        public long lastAttackTime;

        public OnePartyAttacker(MapleParty lastKnownParty, long damage) {
            this.lastKnownParty = lastKnownParty;
            this.damage = damage;
            this.lastAttackTime = System.currentTimeMillis();
        }
    }

    private static final class ExpMap {

        public final int exp;
        public final byte ptysize;
        public final byte 职业奖励经验;
        public final byte 召回戒指经验;

        public ExpMap(int exp, byte ptysize, byte 职业奖励经验, byte 召回戒指经验) {
            this.exp = exp;
            this.ptysize = ptysize;
            this.职业奖励经验 = 职业奖励经验;
            this.召回戒指经验 = 召回戒指经验;
        }
    }

    private final class SingleAttackerEntry
            implements MapleMonster.AttackerEntry {

        private long damage = 0L;
        private final int chrid;
        private long lastAttackTime;

        public SingleAttackerEntry(MapleCharacter from) {
            this.chrid = from.getId();
        }

        @Override
        public void addDamage(MapleCharacter from, long damage, boolean updateAttackTime) {
            if (this.chrid == from.getId()) {
                this.damage += damage;
                if (updateAttackTime) {
                    this.lastAttackTime = System.currentTimeMillis();
                }
            }
        }

        @Override
        public List<MapleMonster.AttackingMapleCharacter> getAttackers() {
            MapleCharacter chr = MapleMonster.this.map.getCharacterById(this.chrid);
            if (chr != null) {
                return Collections.singletonList(new MapleMonster.AttackingMapleCharacter(chr, this.lastAttackTime));
            }
            return Collections.emptyList();
        }

        @Override
        public boolean contains(MapleCharacter chr) {
            return this.chrid == chr.getId();
        }

        @Override
        public long getDamage() {
            return this.damage;
        }

        @Override
        public void killedMob(MapleMap map, int baseExp, boolean mostDamage, int lastSkill) {
            MapleCharacter chr = map.getCharacterById(this.chrid);
            if ((chr != null) && (chr.isAlive())) {
                MapleMonster.this.giveExpToCharacter(chr, baseExp, mostDamage, 1, (byte) 0, (byte) 0, (byte) 0, lastSkill);
            }
        }

        @Override
        public int hashCode() {
            return this.chrid;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            SingleAttackerEntry other = (SingleAttackerEntry) obj;
            return this.chrid == other.chrid;
        }
    }

    private static abstract interface AttackerEntry {

        public abstract List<MapleMonster.AttackingMapleCharacter> getAttackers();

        public abstract void addDamage(MapleCharacter paramMapleCharacter, long paramLong, boolean paramBoolean);

        public abstract long getDamage();

        public abstract boolean contains(MapleCharacter paramMapleCharacter);

        public abstract void killedMob(MapleMap paramMapleMap, int paramInt1, boolean paramBoolean, int paramInt2);
    }

    private static class AttackingMapleCharacter {

        private final MapleCharacter attacker;
        private long lastAttackTime;

        public AttackingMapleCharacter(MapleCharacter attacker, long lastAttackTime) {
            this.attacker = attacker;
            this.lastAttackTime = lastAttackTime;
        }

        public long getLastAttackTime() {
            return this.lastAttackTime;
        }

        public void setLastAttackTime(long lastAttackTime) {
            this.lastAttackTime = lastAttackTime;
        }

        public MapleCharacter getAttacker() {
            return this.attacker;
        }
    }

    public void setTriangulation(int triangulation) {
        this.triangulation = triangulation;
    }

    public int getTriangulation() {
        return triangulation;
    }

    public void setmark(boolean x) {
        this.mark = x;
    }

    public boolean getmark() {
        return mark;
    }
}
