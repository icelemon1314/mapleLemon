package server.maps;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.Pair;
import tools.StringUtil;

public class MapleReactorFactory {

    private static final MapleDataProvider data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath", "wz") + "/Reactor.wz"));
    private static final Map<Integer, MapleReactorStats> reactorStats = new HashMap();

    public static MapleReactorStats getReactor(int rid) {
        MapleReactorStats stats = (MapleReactorStats) reactorStats.get(Integer.valueOf(rid));
        if (stats == null) {
            int infoId = rid;
            MapleData reactorData = data.getData(StringUtil.getLeftPaddedStr(Integer.toString(infoId) + ".img", '0', 11));
            MapleData link = reactorData.getChildByPath("info/link");
            if (link != null) {
                infoId = MapleDataTool.getIntConvert("info/link", reactorData);
                stats = (MapleReactorStats) reactorStats.get(infoId);
            }
            if (stats == null) {
                stats = new MapleReactorStats();
                reactorData = data.getData(StringUtil.getLeftPaddedStr(Integer.toString(infoId) + ".img", '0', 11));
                if (reactorData == null) {
                    return stats;
                }

                boolean canTouch = MapleDataTool.getInt("info/activateByTouch", reactorData, 0) > 0;
                boolean areaSet = false;
                boolean foundState = false;
                for (byte i = 0;; i = (byte) (i + 1)) {
                    MapleData reactorD = reactorData.getChildByPath(String.valueOf(i));
                    if (reactorD == null) {
                        break;
                    }
                    MapleData reactorInfoData_ = reactorD.getChildByPath("event");
                    if ((reactorInfoData_ != null) && (reactorInfoData_.getChildByPath("0") != null)) {
                        MapleData reactorInfoData = reactorInfoData_.getChildByPath("0");
                        Pair reactItem = null;
                        int type = MapleDataTool.getIntConvert("type", reactorInfoData);
                        if (type == 100) {
                            reactItem = new Pair(MapleDataTool.getIntConvert("0", reactorInfoData), MapleDataTool.getIntConvert("1", reactorInfoData, 1));
                            if (!areaSet) {
                                stats.setTL(MapleDataTool.getPoint("lt", reactorInfoData));
                                stats.setBR(MapleDataTool.getPoint("rb", reactorInfoData));
                                areaSet = true;
                            }
                        }
                        foundState = true;
                        stats.addState(i, type, reactItem, (byte) MapleDataTool.getIntConvert("state", reactorInfoData), MapleDataTool.getIntConvert("timeOut", reactorInfoData_, -1), (byte) ((MapleDataTool.getIntConvert("2", reactorInfoData, 0) > 0) || (reactorInfoData.getChildByPath("clickArea") != null) || (type == 9) ? 1 : canTouch ? 2 : 0));
                    } else {
                        stats.addState(i, 999, null, (byte) (foundState ? -1 : i + 1), 0, (byte) 0);
                    }
                }
                reactorStats.put(infoId, stats);
                if (rid != infoId) {
                    reactorStats.put(rid, stats);
                }
            } else {
                reactorStats.put(rid, stats);
            }
        }
        return stats;
    }
}
