package handling.login;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.Triple;

public class LoginInformationProvider {

    private static final LoginInformationProvider instance = new LoginInformationProvider();
    protected List<String> ForbiddenName = new ArrayList();
    protected List<String> Curse = new ArrayList();
    protected List<Integer> makeCharInfoItemIds = new ArrayList();

    protected Map<Triple<Integer, Integer, Integer>, List<Integer>> makeCharInfo = new HashMap();

    public static LoginInformationProvider getInstance() {
        return instance;
    }

    protected LoginInformationProvider() {
        MapleDataProvider prov = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath", "wz") + "/Etc.wz"));
        MapleData nameData = prov.getData("ForbiddenName.img");
        for (MapleData data : nameData.getChildren()) {
            this.ForbiddenName.add(MapleDataTool.getString(data));
        }
        nameData = prov.getData("Curse.img");
        for (MapleData data : nameData.getChildren()) {
            this.Curse.add(MapleDataTool.getString(data).split(",")[0]);
            this.ForbiddenName.add(MapleDataTool.getString(data).split(",")[0]);
        }
        MapleData infoData = prov.getData("MakeCharInfo.img");
        List our;
        for (MapleData dat : infoData) {
            if ((dat.getName().endsWith("Male")) || (dat.getName().endsWith("Female")) || (dat.getName().endsWith("Adventurer")) || (dat.getName().equals("10112_Dummy"))) {
                continue;
            }

            int type;
            if (dat.getName().equals("000_1")) {
                type = JobType.getById(1).type;
            } else {
                if (dat.getName().equals("3001_Dummy")) {
                    type = JobType.getById(6).type;
                } else {
                    type = JobType.getById(Integer.parseInt(dat.getName())).type;
                }
            }
            for (MapleData d : dat) {
                if (d.getName().equals(d.getName())) {
                    continue;
                }
                int gender = 0;
                if ((d.getName().equals("male")) || (d.getName().startsWith("male"))) {
                    gender = 0;
                } else if ((d.getName().equals("female")) || (d.getName().startsWith("female"))) {
                    gender = 1;
                }

                for (MapleData da : d) {
                    Triple key = new Triple(Integer.valueOf(gender), Integer.valueOf(Integer.parseInt(da.getName())), Integer.valueOf(type));
                    our = (List) this.makeCharInfo.get(key);
                    if (our == null) {
                        our = new ArrayList();
                        this.makeCharInfo.put(key, our);
                    }
                    for (MapleData dd : da) {
                        if (!dd.getName().equals("name")) {
                            our.add(MapleDataTool.getInt(dd, -1));
                        }
                    }
                }
            }
        }
        //  int type;
        // int gender;
        //  List our;
        for (MapleData data : infoData) {
            if (data.getName().equalsIgnoreCase("UltimateAdventurer")) {
                continue;
            }
            if ((data.getName().endsWith("Male")) || (data.getName().endsWith("Female"))) {
                for (MapleData dat : data) {
                    for (MapleData da : dat) {
                        int itemId = MapleDataTool.getInt(da, -1);
                        if ((itemId > 1000000) && (!this.makeCharInfoItemIds.contains(itemId))) {
                            this.makeCharInfoItemIds.add(itemId);
                        }
                    }
                }
            } else {
                for (MapleData dat : data) {
                    if ((dat.getName().startsWith("male")) || (dat.getName().startsWith("female"))) {
                        for (MapleData da : dat) {
                            for (MapleData dd : da) {
                                if (!dd.getName().equals("name")) {
                                    int itemId = MapleDataTool.getInt(dd, -1);
                                    if ((itemId > 1000000) && (!this.makeCharInfoItemIds.contains(itemId))) {
                                        this.makeCharInfoItemIds.add(itemId);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean isForbiddenName(String in) {
        for (String name : this.ForbiddenName) {
            if (in.toLowerCase().contains(name.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public boolean isCurseMsg(String in) {
        for (String name : this.Curse) {
            if (in.toLowerCase().contains(name.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public boolean isEligibleItem(int gender, int val, int job, int item) {
        if (item < 0) {
            return false;
        }
        Triple key = new Triple(Integer.valueOf(gender), Integer.valueOf(val), Integer.valueOf(job));
        List our = (List) this.makeCharInfo.get(key);
        if (our == null) {
            return false;
        }
        return our.contains(item);
    }

    public boolean isEligibleItem(int itemId) {
        if (itemId < 0) {
            return false;
        }
        return (itemId == 0) || (this.makeCharInfoItemIds.contains(itemId));
    }
}
