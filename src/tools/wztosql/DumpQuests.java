/*
 This file is part of the ZeroFusion MapleStory Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>
 ZeroFusion organized by "RMZero213" <RMZero213@hotmail.com>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tools.wztosql;

import database.DatabaseConnection;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.quest.MapleQuestActionType;
import server.quest.MapleQuestRequirementType;
import tools.FileoutputUtil;
import tools.Pair;

public class DumpQuests {

    private final MapleDataProvider quest;
    protected boolean hadError = false;
    protected boolean update = false;
    protected int id = 0;
    private final Connection con = DatabaseConnection.getConnection();

    public DumpQuests(boolean update) throws Exception {
        this.update = update;
        this.quest = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath", "wz") + "/Quest.wz"));
        if (quest == null) {
            hadError = true;
        }
    }

    public boolean isHadError() {
        return hadError;
    }

    public void dumpQuests() throws Exception {
        if (!hadError) {
            PreparedStatement psai = con.prepareStatement("INSERT INTO wz_questactitemdata(uniqueid, itemid, count, period, gender, job, jobEx, prop) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            PreparedStatement psaq = con.prepareStatement("INSERT INTO wz_questactquestdata(uniqueid, quest, state) VALUES (?, ?, ?)");
            PreparedStatement ps = con.prepareStatement("INSERT INTO wz_questdata(questid, name, autoStart, autoPreComplete, viewMedalItem, selectedSkillID, blocked, autoAccept, autoComplete) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
            PreparedStatement psr = con.prepareStatement("INSERT INTO wz_questreqdata(questid, type, name, stringStore, intStoresFirst, intStoresSecond) VALUES (?, ?, ?, ?, ?, ?)");
            PreparedStatement psq = con.prepareStatement("INSERT INTO wz_questpartydata(questid, rank, mode, property, value) VALUES(?,?,?,?,?)");
            PreparedStatement psa = con.prepareStatement("INSERT INTO wz_questactdata(questid, type, name, intStore, applicableJobs, uniqueid) VALUES (?, ?, ?, ?, ?, ?)");
            try {
                dumpQuests(psai, psaq, ps, psr, psq, psa);
            } catch (Exception e) {
                FileoutputUtil.log(id + " quest.");
                hadError = true;
            } finally {
                psai.executeBatch();
                psai.close();
                psaq.executeBatch();
                psaq.close();
                psa.executeBatch();
                psa.close();
                psr.executeBatch();
                psr.close();
                psq.executeBatch();
                psq.close();
                ps.executeBatch();
                ps.close();
            }
        }
    }

    public void delete(String sql) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    public boolean doesExist(String sql) throws Exception {
        boolean ret;
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            ret = rs.next();
        }
        return ret;
    }

    //kinda inefficient
    public void dumpQuests(PreparedStatement psai, PreparedStatement psaq, PreparedStatement ps, PreparedStatement psr, PreparedStatement psq, PreparedStatement psa) throws Exception {
        if (!update) {
            delete("DELETE FROM wz_questdata");
            delete("DELETE FROM wz_questactdata");
            delete("DELETE FROM wz_questactitemdata");
            delete("DELETE FROM wz_questactquestdata");
            delete("DELETE FROM wz_questreqdata");
            delete("DELETE FROM wz_questpartydata");
            FileoutputUtil.log("清除 wz_questdata 完成");
        }
        final MapleData checkz = quest.getData("Check.img");
        final MapleData actz = quest.getData("Act.img");
        final MapleData infoz = quest.getData("QuestInfo.img");
        FileoutputUtil.log("正在添加到数据库 wz_questdata.....");
        int uniqueid = 0;
        for (MapleData qz : checkz.getChildren()) { //requirements first
            this.id = Integer.parseInt(qz.getName());
            if (update && doesExist("SELECT * FROM wz_questdata WHERE questid = " + id)) {
                continue;
            }
            ps.setInt(1, id);
            for (int i = 0; i < 2; i++) {
                MapleData reqData = qz.getChildByPath(String.valueOf(i));
                if (reqData != null) {
                    psr.setInt(1, id);
                    psr.setInt(2, i); //0 = start
                    for (MapleData req : reqData.getChildren()) {
                        if (MapleQuestRequirementType.getByWZName(req.getName()) == MapleQuestRequirementType.UNDEFINED) {
                            continue; //un-needed
                        }
                        psr.setString(3, req.getName());
                        switch (req.getName()) {
                            case "fieldEnter":
                                //diff
                                psr.setString(4, String.valueOf(MapleDataTool.getIntConvert("0", req, 0)));
                                break;
                            case "end":
                            case "startscript":
                            case "endscript":
                                psr.setString(4, MapleDataTool.getString(req, ""));
                                break;
                            default:
                                psr.setString(4, String.valueOf(MapleDataTool.getInt(req, 0)));
                                break;
                        }
                        StringBuilder intStore1 = new StringBuilder();
                        StringBuilder intStore2 = new StringBuilder();
                        List<Pair<Integer, Integer>> dataStore = new LinkedList<>();
                        switch (req.getName()) {
                            case "job": {
                                final List<MapleData> child = req.getChildren();
                                for (MapleData child1 : child) {
                                    dataStore.add(new Pair<>(i, MapleDataTool.getInt(child1, -1)));
                                }
                                break;
                            }
                            case "skill": {
                                final List<MapleData> child = req.getChildren();
                                for (MapleData childdata : child) {
                                    if (childdata == null) {
                                        continue;
                                    }
                                    dataStore.add(new Pair<>(MapleDataTool.getInt(childdata.getChildByPath("id"), 0),
                                            MapleDataTool.getInt(childdata.getChildByPath("acquire"), 0)));
                                }
                                break;
                            }
                            case "quest": {
                                final List<MapleData> child = req.getChildren();
                                for (MapleData childdata : child) {
                                    if (childdata == null) {
                                        continue;
                                    }
                                    dataStore.add(new Pair<>(MapleDataTool.getInt(childdata.getChildByPath("id"), 0),
                                            MapleDataTool.getInt(childdata.getChildByPath("state"), 0)));
                                }
                                break;
                            }
                            case "item":
                            case "mob": {
                                final List<MapleData> child = req.getChildren();
                                for (MapleData childdata : child) {
                                    if (childdata == null) {
                                        continue;
                                    }
                                    dataStore.add(new Pair<>(MapleDataTool.getInt(childdata.getChildByPath("id"), 0),
                                            MapleDataTool.getInt(childdata.getChildByPath("count"), 0)));
                                }
                                break;
                            }
                            case "mbcard": {
                                final List<MapleData> child = req.getChildren();
                                for (MapleData childdata : child) {
                                    if (childdata == null) {
                                        continue;
                                    }
                                    dataStore.add(new Pair<>(MapleDataTool.getInt(childdata.getChildByPath("id"), 0),
                                            MapleDataTool.getInt(childdata.getChildByPath("min"), 0)));
                                }
                                break;
                            }
                            case "pet": {
                                final List<MapleData> child = req.getChildren();
                                for (MapleData childdata : child) {
                                    if (childdata == null) {
                                        continue;
                                    }
                                    dataStore.add(new Pair<>(i,
                                            MapleDataTool.getInt(childdata.getChildByPath("id"), 0)));
                                }
                                break;
                            }
                        }
                        for (Pair<Integer, Integer> data : dataStore) {
                            if (intStore1.length() > 0) {
                                intStore1.append(", ");
                                intStore2.append(", ");
                            }
                            intStore1.append(data.getLeft());
                            intStore2.append(data.getRight());
                        }
                        psr.setString(5, intStore1.toString());
                        psr.setString(6, intStore2.toString());
                        psr.addBatch();
                    }
                }
                MapleData actData = actz.getChildByPath(id + "/" + i);
                if (actData != null) {
                    psa.setInt(1, id);
                    psa.setInt(2, i); //0 = start
                    for (MapleData act : actData.getChildren()) {
                        if (MapleQuestActionType.getByWZName(act.getName()) == MapleQuestActionType.UNDEFINED) {
                            continue; //un-needed
                        }
                        psa.setString(3, act.getName());
                        if (act.getName().equals("sp")) {
                            psa.setInt(4, MapleDataTool.getIntConvert("0/sp_value", act, 0));
                        } else {
                            psa.setInt(4, MapleDataTool.getInt(act, 0));
                        }
                        StringBuilder applicableJobs = new StringBuilder();
                        if (act.getName().equals("sp") || act.getName().equals("skill")) {
                            int index = 0;
                            while (true) {
                                if (act.getChildByPath(index + "/job") != null) {
                                    for (MapleData d : act.getChildByPath(index + "/job")) {
                                        if (applicableJobs.length() > 0) {
                                            applicableJobs.append(", ");
                                        }
                                        applicableJobs.append(MapleDataTool.getInt(d, 0));
                                    }
                                    index++;
                                } else {
                                    break;
                                }
                            }
                        } else {
                            if (act.getChildByPath("job") != null) {
                                for (MapleData d : act.getChildByPath("job")) {
                                    if (applicableJobs.length() > 0) {
                                        applicableJobs.append(", ");
                                    }
                                    applicableJobs.append(MapleDataTool.getInt(d, 0));
                                }
                            }
                        }
                        psa.setString(5, applicableJobs.toString());
                        psa.setInt(6, -1);
                        switch (act.getName()) {
                            case "item":
                                //prop, job, gender, id, count
                                uniqueid++;
                                psa.setInt(6, uniqueid);
                                psai.setInt(1, uniqueid);
                                for (MapleData iEntry : act.getChildren()) {
                                    psai.setInt(2, MapleDataTool.getInt("id", iEntry, 0));
                                    psai.setInt(3, MapleDataTool.getInt("count", iEntry, 0));
                                    psai.setInt(4, MapleDataTool.getInt("period", iEntry, 0));
                                    psai.setInt(5, MapleDataTool.getInt("gender", iEntry, 2));
                                    psai.setInt(6, MapleDataTool.getInt("job", iEntry, -1));
                                    psai.setInt(7, MapleDataTool.getInt("jobEx", iEntry, -1));
                                    if (iEntry.getChildByPath("prop") == null) {
                                        psai.setInt(8, -2);
                                    } else {
                                        psai.setInt(8, MapleDataTool.getInt("prop", iEntry, -1));
                                    }
                                    psai.addBatch();
                                }
                                break;
                            case "quest":
                                uniqueid++;
                                psa.setInt(6, uniqueid);
                                psaq.setInt(1, uniqueid);
                                for (MapleData sEntry : act) {
                                    psaq.setInt(2, MapleDataTool.getInt("id", sEntry, 0));
                                    psaq.setInt(3, MapleDataTool.getInt("state", sEntry, 0));
                                    psaq.addBatch();
                                }
                                break;
                        }
                        psa.addBatch();
                    }
                }
            }
            MapleData infoData = infoz.getChildByPath(String.valueOf(id));
            if (infoData != null) {
                ps.setString(2, MapleDataTool.getString("name", infoData, ""));
                ps.setInt(3, MapleDataTool.getInt("autoStart", infoData, 0) > 0 ? 1 : 0);
                ps.setInt(4, MapleDataTool.getInt("autoPreComplete", infoData, 0) > 0 ? 1 : 0);
                ps.setInt(5, MapleDataTool.getInt("viewMedalItem", infoData, 0));
                ps.setInt(6, MapleDataTool.getInt("selectedSkillID", infoData, 0));
                ps.setInt(7, MapleDataTool.getInt("blocked", infoData, 0));
                ps.setInt(8, MapleDataTool.getInt("autoAccept", infoData, 0));
                ps.setInt(9, MapleDataTool.getInt("autoComplete", infoData, 0));
            } else {
                ps.setString(2, "");
                ps.setInt(3, 0);
                ps.setInt(4, 0);
                ps.setInt(5, 0);
                ps.setInt(6, 0);
                ps.setInt(7, 0);
                ps.setInt(8, 0);
                ps.setInt(9, 0);
            }
            ps.addBatch();
            FileoutputUtil.log("添加任务：" + id);
        }
        FileoutputUtil.log("正在储存至数据库 wz_questdata ，请勿关闭...");
    }

    public int currentId() {
        return id;
    }

    public static void main(String[] args) {
        boolean hadError = false;
        boolean update = false;
        long startTime = System.currentTimeMillis();
        for (String file : args) {
            if (file.equalsIgnoreCase("-update")) {
                update = true;
            }
        }
        int currentQuest = 0;
        try {
            final DumpQuests dq = new DumpQuests(update);
            FileoutputUtil.log("转存任务");
            dq.dumpQuests();
            hadError |= dq.isHadError();
            currentQuest = dq.currentId();
        } catch (Exception e) {
            hadError = true;
            System.out.println(e);
            FileoutputUtil.log(currentQuest + " quest.");
        }
        long endTime = System.currentTimeMillis();
        double elapsedSeconds = (endTime - startTime) / 1000.0;
        int elapsedSecs = (((int) elapsedSeconds) % 60);
        int elapsedMinutes = (int) (elapsedSeconds / 60.0);

        String withErrors = "";
        if (hadError) {
            withErrors = "(有错误)";
        }
        FileoutputUtil.log("完成" + withErrors + "耗时：" + elapsedMinutes + "分" + elapsedSecs + "秒");
    }
}
