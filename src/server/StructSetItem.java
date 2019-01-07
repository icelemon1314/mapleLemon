package server;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StructSetItem {

    public int setItemID;
    public byte completeCount;
    public String setItemName;
    public Map<Integer, StructSetItemStat> setItemStat = new LinkedHashMap();
    public List<Integer> itemIDs = new ArrayList();

    public Map<Integer, StructSetItemStat> getSetItemStats() {
        return new LinkedHashMap(this.setItemStat);
    }
}
