package server.life;

import client.SkillFactory;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import java.awt.Point;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import server.MapleCarnivalFactory;
import server.MapleStatEffect;
import server.maps.MapleMap;
import server.maps.MapleReactor;
import server.maps.MapleSummon;
import tools.MaplePacketCreator;

public class SpawnPoint extends Spawns {

    private final MapleMonsterStats monster;
    private final Point pos;
    private long nextPossibleSpawn;
    private final int mobTime;
    private int carnival = -1;
    private final int fh;
    private final int f;
    private final int id;
    private int level = -1;
    private final AtomicInteger spawnedMonsters = new AtomicInteger(0);
    private final String msg;
    private final byte carnivalTeam;

    public SpawnPoint(MapleMonster monster, Point pos, int mobTime, byte carnivalTeam, String msg) {
        this.monster = monster.getStats();
        this.pos = pos;
        this.id = monster.getId();
        this.fh = monster.getFh();
        this.f = monster.getF();
        this.mobTime = (mobTime < 0 ? -1 : mobTime * 1000);
        this.carnivalTeam = carnivalTeam;
        this.msg = msg;
        this.nextPossibleSpawn = System.currentTimeMillis();
    }

    public void setCarnival(int c) {
        this.carnival = c;
    }

    public void setLevel(int c) {
        this.level = c;
    }

    @Override
    public int getF() {
        return this.f;
    }

    @Override
    public int getFh() {
        return this.fh;
    }

    @Override
    public Point getPosition() {
        return this.pos;
    }

    @Override
    public MapleMonsterStats getMonster() {
        return this.monster;
    }

    @Override
    public byte getCarnivalTeam() {
        return this.carnivalTeam;
    }

    @Override
    public int getCarnivalId() {
        return this.carnival;
    }

    @Override
    public boolean shouldSpawn(long time) {
        if (this.mobTime < 0) {
            return false;
        }

        if (((mobTime != 0 || !monster.isMobile()) && spawnedMonsters.get() > 0) || spawnedMonsters.get() > 1) {
            return false;
        }
        return this.nextPossibleSpawn <= time;
    }

    @Override
    public MapleMonster spawnMonster(MapleMap map) {
        MapleMonster mob = new MapleMonster(this.id, this.monster);
        mob.setPosition(this.pos);
        mob.setCy(this.pos.y);
        mob.setRx0(this.pos.x - 50);
        mob.setRx1(this.pos.x + 50);
        mob.setFh(this.fh);
        mob.setF(this.f);
        mob.setCarnivalTeam(this.carnivalTeam);
        if (this.level > -1) {
            mob.changeLevel(this.level);
        }
        this.spawnedMonsters.incrementAndGet();
        mob.addListener(new MonsterListener() {
            @Override
            public void monsterKilled() {
                nextPossibleSpawn = System.currentTimeMillis();

                if (SpawnPoint.this.mobTime > 0) {
                    nextPossibleSpawn += mobTime;

                }
                spawnedMonsters.decrementAndGet();
            }
        });
        map.spawnMonster(mob, -2);
        if (this.carnivalTeam > -1) {
            for (MapleReactor r : map.getAllReactorsThreadsafe()) {
                if ((r.getName().startsWith(String.valueOf(this.carnivalTeam))) && (r.getReactorId() == 9980000 + this.carnivalTeam) && (r.getState() < 5)) {
                    int num = Integer.parseInt(r.getName().substring(1, 2));
                    MapleCarnivalFactory.MCSkill skil = MapleCarnivalFactory.getInstance().getGuardian(num);
                    if (skil != null) {
                        skil.getSkill().applyEffect(null, mob, false);
                    }
                }
            }
        }
        for (MapleSummon s : map.getAllSummonsThreadsafe()) {
            if (s.getSkillId() == 35111005) {
                MapleStatEffect effect = SkillFactory.getSkill(s.getSkillId()).getEffect(s.getSkillLevel());
                for (Map.Entry stat : effect.getMonsterStati().entrySet()) {
                    mob.applyStatus(s.getOwner(), new MonsterStatusEffect((MonsterStatus) stat.getKey(), (Integer) stat.getValue(), s.getSkillId(), null, false), false, effect.getDuration(), true, effect);
                }
                break;
            }
        }
        if (this.msg != null) {
            map.broadcastMessage(MaplePacketCreator.serverMessageNotice(this.msg));
        }
        return mob;
    }

    @Override
    public int getMobTime() {
        return this.mobTime;
    }
}


