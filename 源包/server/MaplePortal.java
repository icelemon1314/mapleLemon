package server;

import client.MapleClient;
import handling.channel.ChannelServer;
import java.awt.Point;
import scripting.portal.PortalScriptManager;
import server.maps.MapleMap;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;

public class MaplePortal {

    public static int MAP_PORTAL = 2;
    public static int DOOR_PORTAL = 6;
    private String name;
    private String target;
    private String scriptName;
    private Point position;
    private int targetmap;
    private final int type; // portal类型
    private int id;
    private boolean portalState = true;

    public MaplePortal(int type) {
        this.type = type;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Point getPosition() {
        return this.position;
    }

    public String getTarget() {
        return this.target;
    }

    public int getTargetMapId() {
        return this.targetmap;
    }

    public int getType() {
        return this.type;
    }

    public String getScriptName() {
        return this.scriptName;
    }

    /**
     * 传送的脚本名字
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    public void setPosition(Point position) {
        this.position = position;
    }


    /**
     * 目标地图脚本名字
     * @param target
     */
    public void setTarget(String target) {
        this.target = target;
    }

    public void setTargetMapId(int targetmapid) {
        this.targetmap = targetmapid;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public void enterPortal(MapleClient c) {
        if ((getPosition().distanceSq(c.getPlayer().getPosition()) > 40000.0D) && (!c.getPlayer().isGM()) && c.getPlayer().getMapId() != 4000010) {
            FileoutputUtil.log("玩家离传送口过远，传送口位置："+getPosition().getX()+","+getPosition().getY()+"。玩家位置："+c.getPlayer().getPosition().toString());
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        MapleMap currentmap = c.getPlayer().getMap();
        FileoutputUtil.log("传送地图："+getTargetMapId());
        if ((!c.getPlayer().hasBlockedInventory()) && ((this.portalState) || (c.getPlayer().isGM()))) {
            if (getScriptName() != null) {
                FileoutputUtil.log("传送地图脚本："+getTargetMapId());
                try {
                    PortalScriptManager.getInstance().executePortalScript(this, c);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (getTargetMapId() != 999999999) {
                MapleMap to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(getTargetMapId());
                if (to == null) {
                    FileoutputUtil.log("找不到地图："+getTargetMapId());
                    c.getPlayer().dropMessage(-1, "找不到地图："+getTargetMapId());
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }
                if ((!c.getPlayer().isGM())&& (to.getLevelLimit() > 0) && (to.getLevelLimit() > c.getPlayer().getLevel())) {
                    c.getPlayer().dropMessage(-1, "You are too low of a level to enter this place.");
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }
                FileoutputUtil.log("传送地图正常："+getTarget());
                c.getPlayer().changeMapPortal(to, to.getPortal(getTarget()) == null ? to.getPortal(0) : to.getPortal(getTarget()));
            }
        }
        if ((c != null) && (c.getPlayer() != null) && (c.getPlayer().getMap() == currentmap)) {
            c.getSession().write(MaplePacketCreator.enableActions());
        }
    }

    public boolean getPortalState() {
        return this.portalState;
    }

    public void setPortalState(boolean ps) {
        this.portalState = ps;
    }
}
