package scripting.reactor;

import client.MapleClient;
import database.DatabaseConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import scripting.AbstractScriptManager;
import server.maps.MapleReactor;
import server.maps.ReactorDropEntry;
import tools.FileoutputUtil;

public class ReactorScriptManager extends AbstractScriptManager {

    private static final ReactorScriptManager instance = new ReactorScriptManager();
    private final Map<Integer, List<ReactorDropEntry>> drops = new HashMap();

    public static final ReactorScriptManager getInstance() {
        return instance;
    }

    public void act(MapleClient c, MapleReactor reactor) {
        try {
            Invocable iv = getInvocable("reactors/" + reactor.getReactorId() + ".js", c);
            if (iv == null) {
                if (c.getPlayer().isShowPacket()) {
                    c.getPlayer().dropMessage(5, "未找到 反应堆 文件中的 " + reactor.getReactorId() + ".js 文件.");
                }
                FileoutputUtil.log(FileoutputUtil.Reactor_ScriptEx_Log, "未找到 反应堆 文件中的 " + reactor.getReactorId() + ".js 文件.");
                return;
            }
            ScriptEngine scriptengine = (ScriptEngine) iv;
            ReactorActionManager rm = new ReactorActionManager(c, reactor);

            scriptengine.put("rm", rm);
            iv.invokeFunction("act", new Object[0]);
        } catch (ScriptException | NoSuchMethodException e) {
            System.err.println("执行反应堆文件出错 反应堆ID: " + reactor.getReactorId() + ", 反应堆名称: " + reactor.getName() + " 错误信息: " + e);
            FileoutputUtil.log(FileoutputUtil.Reactor_ScriptEx_Log, "执行反应堆文件出错 反应堆ID: " + reactor.getReactorId() + ", 反应堆名称: " + reactor.getName() + " 错误信息: " + e);
        }
    }

    public List<ReactorDropEntry> getDrops(int reactorId) {
        List ret = (List) this.drops.get(Integer.valueOf(reactorId));
        if (ret != null) {
            return ret;
        }
        ret = new LinkedList();
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM reactordrops WHERE reactorid = ?")) {
                ps.setInt(1, reactorId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ret.add(new ReactorDropEntry(rs.getInt("itemid"), rs.getInt("chance"), rs.getInt("questid")));
                    }
                }
                ps.close();
            }
        } catch (SQLException e) {
            System.err.println("从SQL中读取箱子的爆率出错.箱子的ID: " + reactorId + " 错误信息: " + e);
            FileoutputUtil.log(FileoutputUtil.Reactor_ScriptEx_Log, "从SQL中读取箱子的爆率出错.箱子的ID: " + reactorId + " 错误信息: " + e);
        }
        this.drops.put(reactorId, ret);
        return ret;
    }

    public void clearDrops() {
        this.drops.clear();
    }
}
