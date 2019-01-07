package handling.login.handler;

import client.MapleCharacter;
import client.MapleClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

public class ViewCharHandler {

    public static void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        Map<Byte, List> worlds = new HashMap();
        List<MapleCharacter> chars = c.loadCharacters(0);
        c.getSession().write(LoginPacket.showAllCharacter(chars.size()));
        for (MapleCharacter chr : chars) {
            if (chr != null) {
                ArrayList chrr;
                if (!worlds.containsKey(chr.getWorld())) {
                    chrr = new ArrayList();
                    worlds.put(chr.getWorld(), chrr);
                } else {
                    chrr = (ArrayList) worlds.get(chr.getWorld());
                }
                chrr.add(chr);
            }
        }
        for (Map.Entry w : worlds.entrySet()) {
            c.getSession().write(LoginPacket.showAllCharacterInfo(((Byte) w.getKey()), (List) w.getValue(), c.getSecondPassword()));
        }
    }
}
