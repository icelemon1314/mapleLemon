package server.life;

import handling.world.WorldBroadcastService;
import java.awt.Point;
import java.util.concurrent.atomic.AtomicBoolean;
import server.Randomizer;
import server.maps.MapleMap;
import tools.MaplePacketCreator;

public class SpawnPointAreaBoss extends Spawns {

    private final MapleMonsterStats monster;
    private final Point pos1;
    private final Point pos2;
    private final Point pos3;
    private long nextPossibleSpawn;
    private final int mobTime;
    private final int fh;
    private final int f;
    private final int id;
    private final AtomicBoolean spawned = new AtomicBoolean(false);
    private final String msg;
    private boolean sendWorldMsg = false;

    public SpawnPointAreaBoss(MapleMonster monster, Point pos1, Point pos2, Point pos3, int mobTime, String msg, boolean shouldSpawn, boolean sendWorldMsg) {
        this.monster = monster.getStats();
        this.id = monster.getId();
        this.fh = monster.getFh();
        this.f = monster.getF();
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.pos3 = pos3;
        this.mobTime = (mobTime < 0 ? -1 : mobTime * 1000);
        this.msg = msg;
        this.sendWorldMsg = ((msg != null) && (sendWorldMsg));
        this.nextPossibleSpawn = (System.currentTimeMillis() + (shouldSpawn ? 0 : this.mobTime));
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
    public MapleMonsterStats getMonster() {
        return this.monster;
    }

    @Override
    public byte getCarnivalTeam() {
        return -1;
    }

    @Override
    public int getCarnivalId() {
        return -1;
    }

    @Override
    public boolean shouldSpawn(long time) {
        if ((this.mobTime < 0) || (this.spawned.get())) {
            return false;
        }
        return this.nextPossibleSpawn <= time;
    }

    @Override
    public Point getPosition() {
        int rand = Randomizer.nextInt(3);
        return rand == 1 ? this.pos2 : rand == 0 ? this.pos1 : this.pos3;
    }

    @Override
    public MapleMonster spawnMonster(MapleMap map) {
        Point pos = getPosition();
        MapleMonster mob = new MapleMonster(this.id, this.monster);
        mob.setPosition(pos);
        mob.setCy(pos.y);
        mob.setRx0(pos.x - 50);
        mob.setRx1(pos.x + 50);
        mob.setFh(this.fh);
        mob.setF(this.f);
        this.spawned.set(true);
        mob.addListener(new MonsterListener() {
            @Override
            public void monsterKilled() {
                nextPossibleSpawn = System.currentTimeMillis();
       //   SpawnPointAreaBoss.access$002(SpawnPointAreaBoss.this, System.currentTimeMillis());

                if (SpawnPointAreaBoss.this.mobTime > 0) {
                    nextPossibleSpawn += mobTime;
                    //SpawnPointAreaBoss.access$014(SpawnPointAreaBoss.this, SpawnPointAreaBoss.this.mobTime);
                }
                SpawnPointAreaBoss.this.spawned.set(false);
            }
        });
        map.spawnMonster(mob, -2);

        if (this.msg != null) {
            if (this.sendWorldMsg) {
                WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.spouseMessage(20, "[系统提示] " + this.msg));
            } else {
                map.broadcastMessage(MaplePacketCreator.serverMessageNotice(this.msg));
            }
        }
        return mob;
    }

    @Override
    public int getMobTime() {
        return this.mobTime;
    }
}
