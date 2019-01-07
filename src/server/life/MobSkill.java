package server.life;

import client.MapleCharacter;
import client.MapleDisease;
import client.status.MonsterStatus;
import constants.GameConstants;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import server.ServerProperties;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleDefender;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.packet.MobPacket;

public class MobSkill {

    private final int skillId;
    private final int skillLevel;
    private int mpCon;
    private int spawnEffect;
    private int hp;
    private int x;
    private int y;
    private long duration;
    private long cooltime;
    private float prop;
    private short limit;
    private List<Integer> toSummon = new ArrayList();
    private Point lt;
    private Point rb;
    private boolean summonOnce;

    public MobSkill(int skillId, int level) {
        this.skillId = skillId;
        this.skillLevel = level;
    }

    public void setOnce(boolean o) {
        this.summonOnce = o;
    }

    public boolean onlyOnce() {
        return this.summonOnce;
    }

    public void setMpCon(int mpCon) {
        this.mpCon = mpCon;
    }

    public void addSummons(List<Integer> toSummon) {
        this.toSummon = toSummon;
    }

    public void setSpawnEffect(int spawnEffect) {
        this.spawnEffect = spawnEffect;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setCoolTime(long cooltime) {
        this.cooltime = cooltime;
    }

    public void setProp(float prop) {
        this.prop = prop;
    }

    public void setLtRb(Point lt, Point rb) {
        this.lt = lt;
        this.rb = rb;
    }

    public void setLimit(short limit) {
        this.limit = limit;
    }

    public void applyEffect(MapleCharacter player, MapleMonster monster, boolean skill) {
        MapleDisease disease = MapleDisease.getBySkill(this.skillId);
        Map stats = new EnumMap(MonsterStatus.class);
        List reflection = new LinkedList();

        switch (this.skillId) {
            case 100:
            case 110:
            case 150:
                stats.put(MonsterStatus.物攻提升, Integer.valueOf(this.x));
                break;
            case 101:
            case 111:
            case 151:
                stats.put(MonsterStatus.魔攻提升, Integer.valueOf(this.x));
                break;
            case 102:
            case 112:
            case 152:
                stats.put(MonsterStatus.物防提升, Integer.valueOf(this.x));
                break;
            case 103:
            case 113:
            case 153:
                stats.put(MonsterStatus.魔防提升, Integer.valueOf(this.x));
                break;
            case 154:
                stats.put(MonsterStatus.命中, Integer.valueOf(this.x));
                break;
            case 155:
                stats.put(MonsterStatus.回避, Integer.valueOf(this.x));
                break;
            case 115:
            case 156:
                stats.put(MonsterStatus.速度, Integer.valueOf(this.x));
                break;
            case 157:
                stats.put(MonsterStatus.沉默, Integer.valueOf(this.x));
                break;
            case 114:
                int hps;
                if ((this.lt != null) && (this.rb != null) && (skill) && (monster != null)) {
                    List<MapleMapObject> objects = getObjectsInRange(monster, MapleMapObjectType.MONSTER);
                    hps = getX() / 1000 * (int) (950.0D + 1050.0D * Math.random());
                    for (MapleMapObject mons : objects) {
                        ((MapleMonster) mons).heal(hps, getY(), true);
                    }
                } else {
                    if (monster == null) {
                        break;
                    }
                    monster.heal(getX(), getY(), true);
                }
                break;
            case 105:
                if ((this.lt != null) && (this.rb != null) && (skill) && (monster != null)) {
                    List<MapleMapObject> objects = getObjectsInRange(monster, MapleMapObjectType.MONSTER);
                    for (MapleMapObject mons : objects) {
                        if (mons.getObjectId() != monster.getObjectId()) {
                            player.getMap().killMonster((MapleMonster) mons, player, true, false, (byte) 1, 0);
                            monster.heal(getX(), getY(), true);
                            break;
                        }
                    }
                } else {
                    if (monster == null) {
                        break;
                    }
                    monster.heal(getX(), getY(), true);
                }
                break;
            case 127:
                if ((this.lt != null) && (this.rb != null) && (skill) && (monster != null) && (player != null)) {
                    for (MapleCharacter character : getPlayersInRange(monster, player)) {
                        character.dispel();
                    }
                } else {
                    if (player == null) {
                        break;
                    }
                    player.dispel();
                }
                break;
            case 129:
                if ((monster == null) || (monster.getMap().getSquadByMap() != null) || ((monster.getEventInstance() != null) && (monster.getEventInstance().getName().contains("BossQuest")))) {
                    break;
                }
                BanishInfo info = monster.getStats().getBanishInfo();
                if (info != null) {
                    if ((this.lt != null) && (this.rb != null) && (skill) && (player != null)) {
                        for (MapleCharacter chr : getPlayersInRange(monster, player)) {
                            if (!chr.hasBlockedInventory()) {
                                chr.changeMapBanish(info.getMap(), info.getPortal(), info.getMsg());
                            }
                        }
                    } else if ((player != null) && (!player.hasBlockedInventory())) {
                        player.changeMapBanish(info.getMap(), info.getPortal(), info.getMsg());
                    }
                }
                break;
            case 191:
            case 131:
                if (monster == null) {
                    break;
                }
                monster.getMap().spawnMist(new MapleDefender(calculateBoundingBox(monster.getTruePosition(), true), monster, this), this.x * 10, false);
                break;
//            case 140:
//                stats.put(MonsterStatus.免疫物攻, Integer.valueOf(this.x));
//                break;
//            case 141:
//                stats.put(MonsterStatus.免疫魔攻, Integer.valueOf(this.x));
//                break;
//            case 142:
//                stats.put(MonsterStatus.免疫伤害, Integer.valueOf(this.x));
//                break;
//            case 143:
//                stats.put(MonsterStatus.反射物攻, Integer.valueOf(this.x));
//                stats.put(MonsterStatus.免疫物攻, this.x);
//                reflection.add(this.x);
//                if (monster == null) {
//                    break;
//                }
//                monster.getMap().broadcastMessage(MaplePacketCreator.spouseMessage(10, "[系统提示] 注意 " + monster.getStats().getName() + " 开启了反射物攻状态。"));
//                break;
//            case 144:
//                stats.put(MonsterStatus.反射魔攻, Integer.valueOf(this.x));
//                stats.put(MonsterStatus.免疫魔攻, this.x);
//                reflection.add(this.x);
//                if (monster == null) {
//                    break;
//                }
//                monster.getMap().broadcastMessage(MaplePacketCreator.spouseMessage(10, "[系统提示] 注意 " + monster.getStats().getName() + " 开启了反射魔攻状态。"));
//                break;
//            case 145:
//                stats.put(MonsterStatus.反射物攻, Integer.valueOf(this.x));
//                stats.put(MonsterStatus.免疫物攻, this.x);
//                stats.put(MonsterStatus.反射魔攻, this.x);
//                stats.put(MonsterStatus.免疫魔攻, this.x);
//                reflection.add(this.x);
//                reflection.add(this.x);
//                if (monster == null) {
//                    break;
//                }
//                monster.getMap().broadcastMessage(MaplePacketCreator.spouseMessage(10, "[系统提示] 注意 " + monster.getStats().getName() + " 开启了反射物攻和魔攻状态。"));
//                break;
            case 200:
                if (monster == null) {
                    return;
                }
                for (Integer mobId : getSummons()) {
                    MapleMonster toSpawn;
                    try {
                        toSpawn = MapleLifeFactory.getMonster(GameConstants.getCustomSpawnID(monster.getId(), mobId));
                    } catch (RuntimeException e) {
                        // }
                        continue;
                    }
                    if (toSpawn != null) {
                        toSpawn.setPosition(monster.getTruePosition());
                        int ypos = (int) monster.getTruePosition().getY();
                        int xpos = (int) monster.getTruePosition().getX();
                        switch (mobId) {
                            case 8500003:
                                toSpawn.setFh((int) Math.ceil(Math.random() * 19.0D));
                                ypos = -590;
                                break;
                            case 8500004:
                                xpos = (int) (monster.getTruePosition().getX() + Math.ceil(Math.random() * 1000.0D) - 500.0D);
                                ypos = (int) monster.getTruePosition().getY();
                                break;
                            case 8510100:
                                if (Math.ceil(Math.random() * 5.0D) == 1.0D) {
                                    ypos = 78;
                                    xpos = (int) (0.0D + Math.ceil(Math.random() * 5.0D)) + (Math.ceil(Math.random() * 2.0D) == 1.0D ? 180 : 0);
                                } else {
                                    xpos = (int) (monster.getTruePosition().getX() + Math.ceil(Math.random() * 1000.0D) - 500.0D);
                                }
                                break;
                            case 8820007:
                            case 8820107:
                                break;
                        }
                        // Get spawn coordinates (This fixes monster lock)
                        // TODO get map left and right wall.
                        switch (monster.getMap().getId()) {
                            case 220080001:
                                if (xpos < -890) {
                                    xpos = (int) (-890 + Math.ceil(Math.random() * 150));
                                } else if (xpos > 230) {
                                    xpos = (int) (230 - Math.ceil(Math.random() * 150));
                                }
                                break;
                            case 230040420:
                                if (xpos < -239) {
                                    xpos = (int) (-239 + Math.ceil(Math.random() * 150));
                                } else if (xpos > 371) {
                                    xpos = (int) (371 - Math.ceil(Math.random() * 150));
                                }
                                break;
                        }
                        monster.getMap().spawnMonsterWithEffect(toSpawn, getSpawnEffect(), monster.getMap().calcPointBelow(new Point(xpos, ypos - 1)));
                    }
                }
                break;
            default:
                if ((disease != null) || (!ServerProperties.ShowPacket())) {
                    break;
                }
                FileoutputUtil.log(FileoutputUtil.未处理的怪物技能, "怪物ID:" + monster.getStats().getId() + " 未处理的怪物技能 skillid : " + this.skillId);
                FileoutputUtil.log("未处理的怪物技能:\n\r怪物ID:" + monster.getStats().getId() + " skillid : " + this.skillId);
        }

        if ((stats.size() > 0) && (monster != null)) {
            if ((this.lt != null) && (this.rb != null) && (skill)) {
                for (MapleMapObject mons : getObjectsInRange(monster, MapleMapObjectType.MONSTER)) {
                    ((MapleMonster) mons).applyMonsterBuff(stats, getSkillId(), getDuration(), this, reflection);
                }
            } else {
                monster.applyMonsterBuff(stats, getSkillId(), getDuration(), this, reflection);
            }
        }
        if ((disease != null) && (player != null)) {
            if ((this.lt != null) && (this.rb != null) && (skill) && (monster != null)) {
                for (MapleCharacter chr : getPlayersInRange(monster, player)) {
                    chr.giveDebuff(disease, this);
                }
            } else {
                player.giveDebuff(disease, this);
            }
        }
        if (monster != null) {
            monster.setMp(monster.getMp() - getMpCon());
        }
    }

    public int getSkillId() {
        return this.skillId;
    }

    public int getSkillLevel() {
        return this.skillLevel;
    }

    public int getMpCon() {
        return this.mpCon;
    }

    public List<Integer> getSummons() {
        return Collections.unmodifiableList(this.toSummon);
    }

    public int getSpawnEffect() {
        return this.spawnEffect;
    }

    public int getHP() {
        return this.hp;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public long getDuration() {
        return this.duration;
    }

    public long getCoolTime() {
        return this.cooltime;
    }

    public Point getLt() {
        return this.lt;
    }

    public Point getRb() {
        return this.rb;
    }

    public int getLimit() {
        return this.limit;
    }

    public boolean makeChanceResult() {
        return (this.prop >= 1.0D) || (Math.random() < this.prop);
    }

    private Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft) {
        Point myrb;
        Point mylt;
        if (facingLeft) {
            mylt = new Point(this.lt.x + posFrom.x, this.lt.y + posFrom.y);
            myrb = new Point(this.rb.x + posFrom.x, this.rb.y + posFrom.y);
        } else {
            myrb = new Point(this.lt.x * -1 + posFrom.x, this.rb.y + posFrom.y);
            mylt = new Point(this.rb.x * -1 + posFrom.x, this.lt.y + posFrom.y);
        }
        Rectangle bounds = new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
        return bounds;
    }

    private List<MapleCharacter> getPlayersInRange(MapleMonster monster, MapleCharacter player) {
        Rectangle bounds = calculateBoundingBox(monster.getTruePosition(), monster.isFacingLeft());
        List players = new ArrayList();
        players.add(player);
        return monster.getMap().getPlayersInRectAndInList(bounds, players);
    }

    private List<MapleMapObject> getObjectsInRange(MapleMonster monster, MapleMapObjectType objectType) {
        Rectangle bounds = calculateBoundingBox(monster.getTruePosition(), monster.isFacingLeft());
        List objectTypes = new ArrayList();
        objectTypes.add(objectType);
        return monster.getMap().getMapObjectsInRect(bounds, objectTypes);
    }
}
