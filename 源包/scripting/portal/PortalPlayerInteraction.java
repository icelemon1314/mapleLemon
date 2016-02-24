package scripting.portal;

import client.MapleClient;
import scripting.AbstractPlayerInteraction;
import server.MaplePortal;

public class PortalPlayerInteraction extends AbstractPlayerInteraction {

    private final MaplePortal portal;

    public PortalPlayerInteraction(MapleClient c, MaplePortal portal) {
        super(c, portal.getId(), String.valueOf(c.getPlayer().getMapId()));
        this.portal = portal;
    }

    public MaplePortal getPortal() {
        return this.portal;
    }

    public void inFreeMarket() {
        if (getMapId() != 910000000) {
            if (getPlayer().getLevel() > 10) {
                saveLocation("FREE_MARKET");
                playPortalSE();
                warp(910000000, "st00");
            } else {
                playerMessage(5, "你必须10级以上才能进入自由市场。");
            }
        }
    }

    @Override
    public void spawnMonster(int id) {
        spawnMonster(id, 1, this.portal.getPosition());
    }

    @Override
    public void spawnMonster(int id, int qty) {
        spawnMonster(id, qty, this.portal.getPosition());
    }
}
