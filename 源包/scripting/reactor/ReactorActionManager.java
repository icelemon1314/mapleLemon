package scripting.reactor;

import client.MapleClient;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.ItemConstants;
import handling.channel.ChannelServer;
import java.awt.Point;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import scripting.AbstractPlayerInteraction;
import server.MapleCarnivalFactory;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.maps.MapleReactor;
import server.maps.ReactorDropEntry;
import tools.MaplePacketCreator;

public class ReactorActionManager extends AbstractPlayerInteraction {

    private final MapleReactor reactor;
    private static final int[] 碎片 = {4001513, 4001515, 4001521};

    public ReactorActionManager(MapleClient c, MapleReactor reactor) {
        super(c, reactor.getReactorId(), String.valueOf( c.getPlayer().getMapId()));
        this.reactor = reactor;
    }

    public void dropItems() {
        dropItems(false, 0, 0, 0, 0, 0, false);
    }

    public void dropItems(int minDrops, int maxDrops) {
        dropItems(false, 0, 0, 0, minDrops, maxDrops, true);
    }

    public void dropItems(boolean meso, int mesoChance, int minMeso, int maxMeso) {
        dropItems(meso, mesoChance, minMeso, maxMeso, 0, 0, false);
    }

    public void dropItems(boolean meso, int mesoChance, int minMeso, int maxMeso, int minDrops, int maxDrops, boolean profession) {
        List chances = ReactorScriptManager.getInstance().getDrops(this.reactor.getReactorId());
        List<ReactorDropEntry> toDrop = new LinkedList();
        if ((meso) && (Math.random() < 1.0D / mesoChance)) {
            toDrop.add(new ReactorDropEntry(0, mesoChance, -1));
        }

        Iterator iter = chances.iterator();

        while (iter.hasNext()) {
            ReactorDropEntry d = (ReactorDropEntry) iter.next();
            if ((Math.random() < 1.0D / d.chance) && ((d.questid <= 0) || (getPlayer().getQuestStatus(d.questid) == 1))) {
                toDrop.add(d);
            }

        }

        while (toDrop.size() < minDrops) {
            if ((profession) && (!toDrop.isEmpty())) {
                for (int i = 0; i < toDrop.size(); i++) {
                    ReactorDropEntry fix = (ReactorDropEntry) toDrop.get(i);
                    if ((fix != null) && (toDrop.size() < minDrops)) {
                        toDrop.add(fix);
                    }
                }
                continue;
            }

            toDrop.add(new ReactorDropEntry(0, mesoChance, -1));
        }

        if ((profession) && (toDrop.size() > maxDrops)) {
            toDrop = toDrop.subList(0, maxDrops);
        }
        Collections.shuffle(toDrop);
        Point dropPos = this.reactor.getPosition();
        dropPos.x -= 12 * toDrop.size();

        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        for (ReactorDropEntry de : toDrop) {
            if (de.itemId == 0) {
                int range = maxMeso - minMeso;
                int mesoDrop = Randomizer.nextInt(range) + minMeso * ChannelServer.getInstance(getClient().getChannel()).getMesoRate(getPlayer().getWorld());
                if (mesoDrop > 0) {
                    this.reactor.getMap().spawnMesoDrop(mesoDrop, dropPos, this.reactor, getPlayer(), false, (byte) 0);
                }
            } else {
                Item drop;
                if (ItemConstants.getInventoryType(de.itemId) != MapleInventoryType.EQUIP) {
                    drop = new Item(de.itemId, (byte) 0, (byte) 1, (byte) 0);
                } else {
                    drop = ii.randomizeStats((Equip) ii.getEquipById(de.itemId));
                }
                drop.setGMLog("从箱子爆出 " + this.reactor.getReactorId() + " 在地图 " + getPlayer().getMapId());
                this.reactor.getMap().spawnItemDrop(this.reactor, getPlayer(), drop, dropPos, false, false);
            }
            dropPos.x += 25;
        }
    }

    @Override
    public void spawnNpc(int npcId) {
        spawnNpc(npcId, getPosition());
    }

    public Point getPosition() {
        Point pos = this.reactor.getPosition();
        pos.y -= 10;
        return pos;
    }

    public MapleReactor getReactor() {
        return this.reactor;
    }

    public void spawnZakum() {
        this.reactor.getMap().spawnZakum(getPosition().x, getPosition().y);
    }

    public void spawnFakeMonster(int id) {
        spawnFakeMonster(id, 1, getPosition());
    }

    public void spawnFakeMonster(int id, int x, int y) {
        spawnFakeMonster(id, 1, new Point(x, y));
    }

    public void spawnFakeMonster(int id, int qty) {
        spawnFakeMonster(id, qty, getPosition());
    }

    public void spawnFakeMonster(int id, int qty, int x, int y) {
        spawnFakeMonster(id, qty, new Point(x, y));
    }

    private void spawnFakeMonster(int id, int qty, Point pos) {
        for (int i = 0; i < qty; i++) {
            this.reactor.getMap().spawnFakeMonsterOnGroundBelow(MapleLifeFactory.getMonster(id), pos);
        }
    }

    public void killAll() {
        this.reactor.getMap().killAllMonsters(true);
    }

    public void killMonster(int monsId) {
        this.reactor.getMap().killMonster(monsId);
    }

    @Override
    public void spawnMonster(int id) {
        spawnMonster(id, 1, getPosition());
    }

    @Override
    public void spawnMonster(int id, int qty) {
        spawnMonster(id, qty, getPosition());
    }

    public void dispelAllMonsters(int num) {
        MapleCarnivalFactory.MCSkill skil = MapleCarnivalFactory.getInstance().getGuardian(num);
        if (skil != null) {
            for (MapleMonster mons : getMap().getAllMonstersThreadsafe()) {
                mons.dispelSkill(skil.getSkill());
            }
        }
    }
}
