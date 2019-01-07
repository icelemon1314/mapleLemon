package scripting.map;

import client.MapleClient;
import client.MapleQuestStatus;
import java.awt.Point;
import scripting.AbstractPlayerInteraction;
import server.MapleItemInformationProvider;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.OverrideMonsterStats;
import server.maps.MapleMap;
import server.quest.MapleQuest;
import tools.MaplePacketCreator;
import tools.packet.UIPacket;

/**
 * 进入地图出发的剧情
 * @author 7
 */
public class MapScriptMethods extends AbstractPlayerInteraction {

    public MapScriptMethods(MapleClient c) {
        super(c);
    }

    public void displayAranIntro() {
        String data = null;
        switch (this.c.getPlayer().getMapId()) {
            case 914090010:
                data = "Effect/Direction1.img/aranTutorial/Scene0";
                break;
            case 914090011:
                data = new StringBuilder().append("Effect/Direction1.img/aranTutorial/Scene1").append(this.c.getPlayer().getGender() == 0 ? "0" : "1").toString();
                break;
            case 914090012:
                data = new StringBuilder().append("Effect/Direction1.img/aranTutorial/Scene2").append(this.c.getPlayer().getGender() == 0 ? "0" : "1").toString();
                break;
            case 914090013:
                data = "Effect/Direction1.img/aranTutorial/Scene3";
                break;
            case 914090100:
                data = new StringBuilder().append("Effect/Direction1.img/aranTutorial/HandedPoleArm").append(this.c.getPlayer().getGender() == 0 ? "0" : "1").toString();
                break;
            case 914090200:
                data = "Effect/Direction1.img/aranTutorial/Maha";
        }

        if (data != null) {
            showIntro(this.c, data);
        }
    }

    private void showIntro(MapleClient c, String data) {
        c.getSession().write(UIPacket.IntroDisableUI(true));
        c.getSession().write(UIPacket.IntroLock(true));
        c.getSession().write(UIPacket.ShowWZEffect(data));
    }

    public void startMapEffect(MapleClient c, String data, int itemId) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (!ii.itemExists(itemId)) {
            c.getPlayer().dropMessage(5, new StringBuilder().append("地图效果触发 道具: ").append(itemId).append(" 不存在.").toString());
            return;
        }
        if (!ii.isFloatCashItem(itemId)) {
            c.getPlayer().dropMessage(5, new StringBuilder().append("地图效果触发 道具: ").append(itemId).append(" 不具有漂浮公告的效果.").toString());
            return;
        }
        c.getPlayer().getMap().startMapEffect(data, itemId);
    }

    public void sendMapNameDisplay(boolean enabled) {
        if (enabled) {
            this.c.getSession().write(UIPacket.IntroDisableUI(false));
            this.c.getSession().write(UIPacket.IntroLock(false));
        }
        this.c.getSession().write(UIPacket.MapNameDisplay(this.c.getPlayer().getMapId()));
    }

    public void handlePinkBeanStart() {
        MapleMap map = this.c.getPlayer().getMap();
        map.resetFully();
        if (!map.containsNPC(2141000)) {
            map.spawnNpc(2141000, new Point(-190, -42));
        }
    }
}
