package handling.channel.handler;

import client.MapleCharacterUtil;
import client.MapleClient;
import constants.ServerConstants;
import scripting.event.EventManager;
import scripting.npc.NPCScriptManager;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class UserInterfaceHandler {

    public static void CygnusSummon_NPCRequest(MapleClient c) {
        if (c.getPlayer().getJob() == 2000) {
            NPCScriptManager.getInstance().start(c, 1202000);
        } else if (c.getPlayer().getJob() == 1000) {
            NPCScriptManager.getInstance().start(c, 1101008);
        }
    }

    public static void InGame_Poll(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (ServerConstants.PollEnabled) {
            int selection = slea.readInt();
            if ((selection >= 0) && (selection <= ServerConstants.Poll_Answers.length)
                    && (MapleCharacterUtil.SetPoll(c.getAccID(), selection))) {
                c.getSession().write(MaplePacketCreator.getPollReply("Thank you."));
            }
        }
    }

    public static void ShipObjectRequest(int mapid, MapleClient c) {
        int effect = 3;
        EventManager em;
        switch (mapid) {
            case 101000300:
            case 200000111:
                em = c.getChannelServer().getEventSM().getEventManager("Boats");
                if ((em == null) || (!em.getProperty("docked").equals("true"))) {
                    break;
                }
                effect = 1;
                break;
            case 200000121:
            case 220000110:
                em = c.getChannelServer().getEventSM().getEventManager("Trains");
                if ((em == null) || (!em.getProperty("docked").equals("true"))) {
                    break;
                }
                effect = 1;
                break;
            case 200000151:
            case 260000100:
                em = c.getChannelServer().getEventSM().getEventManager("Geenie");
                if ((em == null) || (!em.getProperty("docked").equals("true"))) {
                    break;
                }
                effect = 1;
                break;
            case 200000131:
            case 240000110:
                em = c.getChannelServer().getEventSM().getEventManager("Flight");
                if ((em == null) || (!em.getProperty("docked").equals("true"))) {
                    break;
                }
                effect = 1;
                break;
            case 200090000:
            case 200090010:
                em = c.getChannelServer().getEventSM().getEventManager("Boats");
                if ((em != null) && (em.getProperty("haveBalrog").equals("true"))) {
                    effect = 1;
                } else {
                    return;
                }

            default:
                FileoutputUtil.log("Unhandled ship object, MapID : " + mapid);
        }

        c.getSession().write(MaplePacketCreator.boatPacket(effect==1?true:false));
    }
}
