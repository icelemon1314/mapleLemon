package client.inventory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.Pair;

public class PetDataFactory {

    private static final MapleDataProvider dataRoot = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath", "wz") + "/Item.wz"));
    private static final Map<Pair<Integer, Integer>, PetCommand> petCommands = new HashMap();
    private static final Map<Integer, Integer> petHunger = new HashMap();

    public static PetCommand getPetCommand(int petId, int command) {
        PetCommand ret = (PetCommand) petCommands.get(new Pair(Integer.valueOf(petId), Integer.valueOf(command)));
        if (ret != null) {
            return ret;
        }
        MapleData petData = dataRoot.getData("Pet/" + petId + ".img");
        int prob = 0;
        int inc = 0;
        if (petData != null) {
            prob = MapleDataTool.getInt("interact/" + command + "/prob", petData, 0);
            inc = MapleDataTool.getInt("interact/" + command + "/inc", petData, 0);
        }
        ret = new PetCommand(petId, command, prob, inc);
        petCommands.put(new Pair(petId, command), ret);
        return ret;
    }

    public static int getHunger(int petId) {
        Integer ret = petHunger.get(Integer.valueOf(petId));
        if (ret != null) {
            return ret;
        }
        MapleData hungerData = dataRoot.getData("Pet/" + petId + ".img").getChildByPath("info/hungry");
        ret = MapleDataTool.getInt(hungerData, 1);
        petHunger.put(petId, ret);
        return ret;
    }
}
