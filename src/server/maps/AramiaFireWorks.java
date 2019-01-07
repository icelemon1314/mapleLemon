package server.maps;

import client.MapleCharacter;
import handling.world.WorldBroadcastService;
import java.awt.Point;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.Timer.EventTimer;
import server.life.MapleLifeFactory;
import tools.MaplePacketCreator;

public class AramiaFireWorks {

    public static final int KEG_ID = 4001128;
    public static final int SUN_ID = 4001246;
    public static final int DEC_ID = 4001473;
    public static final int MAX_KEGS = 2400;
    public static final int MAX_SUN = 3000;
    public static final int MAX_DEC = 3600;
    private short kegs = 400;
    private short sunshines = 500;
    private short decorations = 600;
    private static final int[] arrayMob = {9500168, 9500169, 9500170, 9500171, 9500173, 9500174, 9500175, 9500176, 9500170, 9500171, 9500172, 9500173, 9500174, 9500175, 9400569};
    private static final int[] arrayX = {2100, 2605, 1800, 2600, 3120, 2700, 2320, 2062, 2800, 3100, 2300, 2840, 2700, 2320, 1950};
    private static final int[] arrayY = {574, 364, 574, 316, 574, 574, 403, 364, 574, 574, 403, 574, 574, 403, 574};
    private static final int[] array_X = {720, 180, 630, 270, 360, 540, 450, 142, 142, 218, 772, 810, 848, 232, 308, 142};
    private static final int[] array_Y = {1234, 1234, 1174, 1234, 1174, 1174, 1174, 1260, 1234, 1234, 1234, 1234, 1234, 1114, 1114, 1140};
    private static final int flake_Y = 149;

    public void giveKegs(MapleCharacter c, int kegs) {
        this.kegs = (short) (this.kegs + kegs);
        if (this.kegs >= 2400) {
            this.kegs = 0;
            broadcastEvent(c);
        }
    }

    private void broadcastServer(MapleCharacter c, int itemid) {
        WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.serverNotice(6, itemid, "<频道 " + c.getClient().getChannel() + "> " + c.getMap().getMapName() + " : The amount of {" + MapleItemInformationProvider.getInstance().getName(itemid) + "} has reached the limit!"));
    }

    public short getKegsPercentage() {
        return (short) (this.kegs / 2400 * 10000);
    }

    private void broadcastEvent(final MapleCharacter c) {
        broadcastServer(c, 4001128);

        EventTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                AramiaFireWorks.this.startEvent(c.getClient().getChannelServer().getMapFactory().getMap(100000200));
            }
        }, 10000L);
    }

    private void startEvent(final MapleMap map) {
        map.startMapEffect("Who's going crazy with the fireworks?", 5121010);

        EventTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                AramiaFireWorks.this.spawnMonster(map);
            }
        }, 5000L);
    }

    private void spawnMonster(MapleMap map) {
        for (int i = 0; i < arrayMob.length; i++) {
            Point pos = new Point(arrayX[i], arrayY[i]);
            map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(arrayMob[i]), pos);
        }
    }

    public void giveSuns(MapleCharacter c, int kegs) {
        this.sunshines = (short) (this.sunshines + kegs);

        MapleMap map = c.getClient().getChannelServer().getMapFactory().getMap(970010000);
        MapleReactor reactor = map.getReactorByName("mapleTree");
        for (int gogo = kegs + 500; gogo > 0; gogo -= 500) {
            switch (reactor.getState()) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                    if (this.sunshines < 500 * (2 + reactor.getState())) {
                        continue;
                    }
                    reactor.setState((byte) (reactor.getState() + 1));
                    reactor.setTimerActive(false);
                    map.broadcastMessage(MaplePacketCreator.triggerReactor(reactor, reactor.getState()));
                    break;
                default:
                    if (this.sunshines < 500) {
                        continue;
                    }
                    map.resetReactors();
            }

        }

        if (this.sunshines >= 3000) {
            this.sunshines = 0;
            broadcastSun(c);
        }
    }

    public short getSunsPercentage() {
        return (short) (this.sunshines / 3000 * 10000);
    }

    private void broadcastSun(final MapleCharacter c) {
        broadcastServer(c, 4001246);

        EventTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                AramiaFireWorks.this.startSun(c.getClient().getChannelServer().getMapFactory().getMap(970010000));
            }
        }, 10000L);
    }

    private void startSun(final MapleMap map) {
        map.startMapEffect("The tree is bursting with sunshine!", 5121010);
        for (int i = 0; i < 3; i++) {
            EventTimer.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    AramiaFireWorks.this.spawnItem(map);
                }
            }, 5000 + i * 10000);
        }
    }

    private void spawnItem(MapleMap map) {
        for (int i = 0; i < Randomizer.nextInt(5) + 10; i++) {
            Point pos = new Point(array_X[i], array_Y[i]);
            int itemId = 4001246;
            switch (Randomizer.nextInt(15)) {
                case 0:
                case 1:
                    itemId = 3010141;
                    break;
                case 2:
                    itemId = 3010146;
                    break;
                case 3:
                case 4:
                    itemId = 3010025;
            }

            map.spawnAutoDrop(itemId, pos);
        }
    }

    public void giveDecs(MapleCharacter c, int kegs) {
        this.decorations = (short) (this.decorations + kegs);

        MapleMap map = c.getClient().getChannelServer().getMapFactory().getMap(555000000);
        MapleReactor reactor = map.getReactorByName("XmasTree");
        for (int gogo = kegs + 600; gogo > 0; gogo -= 600) {
            switch (reactor.getState()) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                    if (this.decorations < 600 * (2 + reactor.getState())) {
                        continue;
                    }
                    reactor.setState((byte) (reactor.getState() + 1));
                    reactor.setTimerActive(false);
                    map.broadcastMessage(MaplePacketCreator.triggerReactor(reactor, reactor.getState()));
                    break;
                default:
                    if (this.decorations < 600) {
                        continue;
                    }
                    map.resetReactors();
            }

        }

        if (this.decorations >= 3600) {
            this.decorations = 0;
            broadcastDec(c);
        }
    }

    public short getDecsPercentage() {
        return (short) (this.decorations / 3600 * 10000);
    }

    private void broadcastDec(final MapleCharacter c) {
        broadcastServer(c, 4001473);
        EventTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                AramiaFireWorks.this.startDec(c.getClient().getChannelServer().getMapFactory().getMap(555000000));
            }
        }, 10000L);
    }

    private void startDec(final MapleMap map) {
        map.startMapEffect("The tree is bursting with snow!", 5120000);
        for (int i = 0; i < 3; i++) {
            EventTimer.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    AramiaFireWorks.this.spawnDec(map);
                }
            }, 5000 + i * 10000);
        }
    }

    private void spawnDec(MapleMap map) {
        for (int i = 0; i < Randomizer.nextInt(10) + 40; i++) {
            Point pos = new Point(Randomizer.nextInt(800) - 400, 149);
            map.spawnAutoDrop(Randomizer.nextInt(15) == 1 ? 4310012 : 4310011, pos);
        }
    }
}
