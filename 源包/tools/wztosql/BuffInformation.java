/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools.wztosql;

import java.awt.Point;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import provider.MapleCanvas;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.FileoutputUtil;
import tools.Pair;

/**
 *
 * @author Wubin
 */
public class BuffInformation {

    public static long start, end;
    public static List<Exception> exceptions = new LinkedList();
    public static List<Pair<Integer, Character>> a = new LinkedList();
    public static List<Character> b = new LinkedList();

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        FileoutputUtil.log("Would you like to export all skills' information? <y/n>");
        boolean export = input.next().equalsIgnoreCase("y");
        if (export) {
            dumpSkills();
            for (Pair c : a) {
                FileoutputUtil.log("special char " + c.getRight() + " skill " + c.getLeft());
            }
        }
        while (!export) {
            FileoutputUtil.log("Please insert skill id.");
            int skill;
            try {
                skill = input.nextInt();
            } catch (Exception ex) {
                if (!exceptions.contains(ex)) {
                    exceptions.add(ex);
                }
                FileoutputUtil.log("Could not parse skill id.");
                return;
            }
            FileoutputUtil.log(getSkillInformation(skill).toString());
        }
        if (exceptions.size() > 0) {
            FileoutputUtil.log("Show exceptions? <y/n>");
            boolean show = input.next().equalsIgnoreCase("y");
            if (show) {
                for (Exception ex : exceptions) {
                    FileoutputUtil.log(ex.toString());
                }
            }
        }
    }

    public static StringBuilder getSkillInformation(int skill) {
        StringBuilder sb = new StringBuilder();
        File wzfile = new File(System.getProperty("wzpath") + "/Skill.wz");
        MapleDataProvider prov = MapleDataProviderFactory.getDataProvider(wzfile);
        MapleData data = prov.getData(String.valueOf(skill / 10000) + ".img");
        for (MapleData sub : data.getChildren()) {
            if (!sub.getName().equals("skill")) {
                continue;
            }
            for (MapleData sub2 : sub.getChildren()) {
                if (!sub2.getName().equals(String.valueOf(skill))) {
                    continue;
                }
                System.out.println(skill / 10000);
                System.out.println(skill);
                for (MapleData sub3 : sub2.getChildren()) {
                    if (!sub3.getName().equals("common")) {
                        //continue;
                    }
                    for (MapleData sub4 : sub3.getChildren()) {
                        String tab = "";
                        String name = sub3.getName() + "." + sub4.getName();
                        name += ":";
                        for (int i = 3; i > name.length() / 8; i--) {
                            tab += "\t";
                        }
                        String info;
                        try {
                            info = MapleDataTool.getString(sub4, "0");
                        } catch (Exception ex) {
                            if (!exceptions.contains(ex)) {
                                exceptions.add(ex);
                            }
                            continue;
                        }
                        sb.append(name).append(tab);
                        sb.append(info).append("\r\n");
                    }
                }
            }
        }
        if (sb.toString().isEmpty()) {
            sb.append("No results were found.\r\n");
        } else {
            sb.append("\r\n");
            sb.append("\r\n");
            sb.append("Results:");
            sb.append("\r\n");
        }
        return sb;
    }

    public static void dumpSkills() {
        StringBuilder sb = new StringBuilder();
        File wzfile = new File(System.getProperty("wzpath") + "/Skill.wz");
        MapleDataProvider prov = MapleDataProviderFactory.getDataProvider(wzfile);
        MapleDataDirectoryEntry root = prov.getRoot();
        start = System.currentTimeMillis();
        for (MapleDataFileEntry data : root.getFiles()) {
            if (data.getName().length() > 8) {
                continue;
            }
            FileoutputUtil.log("Exporting job " + data.getName().replaceAll(".img", ""));
            for (MapleData sub : prov.getData(data.getName())) {
                if (!sub.getName().equals("skill")) {
                    continue;
                }
                for (MapleData sub2 : sub.getChildren()) {
                    for (MapleData sub3 : sub2.getChildren()) {
                        boolean found = false;
                        for (String s : opt) {
                            if (s.equals(sub3.getName())) {
                                found = true;
                            }
                        }
                        if (!found) {
                            FileoutputUtil.log("New information type found: " + sub3.getName());
                        }
                        for (MapleData sub4 : sub3.getChildren()) {
                            String tab = "";
                            String name = sub3.getName() + "/" + sub4.getName();
                            name += ":";
                            for (int i = 4; i > Integer.valueOf(name.length() / 8); i--) {
                                tab += "\t";
                            }
                            String info = "";
                            try {
                                if (sub4.getData() instanceof String
                                        || sub4.getData() instanceof Integer) {
                                    info = MapleDataTool.getString(sub4, "0");
                                    for (char c : info.toCharArray()) {
                                        if (Character.isAlphabetic(c) && name.contains("common")) {
                                            if (!b.contains(c)) {
                                                a.add(new Pair<>(Integer.parseInt(sub2.getName()), c));
                                                b.add(c);
                                            }
                                        }
                                    }
                                } else if (sub4.getData() instanceof Point) {
                                    info = MapleDataTool.getPoint(sub4).toString();
                                } else if (sub4.getData() instanceof MapleCanvas) {
                                    //info = MapleDataTool.getImage(sub4).toString();
                                    info = "image";
                                }
                            } catch (NumberFormatException ex) {
                                if (!exceptions.contains(ex)) {
                                    exceptions.add(ex);
                                }
                                continue;
                            }
                            if (!info.isEmpty()) {
                                sb.append(name).append(tab);
                                sb.append(info).append("\r\n");
                            }
                        }
                    }
                    if (!sb.toString().isEmpty()) {
                        try {
                            if (saveSkillFile(Integer.parseInt(sub2.getName()), sb)) {
                                saveReadme();
                            }
                        } catch (NumberFormatException ex) {
                            if (!exceptions.contains(ex)) {
                                exceptions.add(ex);
                            }
                            FileoutputUtil.log("Failed to get information of " + sub2.getName());
                        }
                        sb = new StringBuilder();
                    }
                }
            }
        }
        end = System.currentTimeMillis();
        long total = end - start;
        long minutes = (total / (60 * 1000));
        long seconds = (total % (60 * 1000) / 1000);
        long milliseconds = (total % 1000);
        FileoutputUtil.log("Total time: " + minutes + " minute(s), " + seconds
                + " second(s), " + milliseconds + " millisecond(s).");
    }

    public static boolean saveSkillFile(int skillid, StringBuilder sb) {
        boolean active = skillid % 10000 >= 1000;
        String cat = (active ? "active" : "passive");
        int job = skillid / 10000;
        File outfile = new File("SkillInformation/Skill/" + job + "/" + cat);
        outfile.mkdirs();
        String fileName = outfile + "/" + skillid + ".txt";
        try {
            FileOutputStream out = new FileOutputStream(fileName, false);
            out.write(sb.toString().getBytes());
        } catch (IOException ex) {
            if (!exceptions.contains(ex)) {
                exceptions.add(ex);
            }
            return false;
        }
        return true;
    }

    public static boolean saveReadme() {
        File outfile = new File("SkillInformation");
        outfile.mkdir();
        String fileName = outfile + "/ReadMe.txt";
        StringBuilder sb = new StringBuilder();
        sb.append("+---------------------------------------------------+\r\n");
        sb.append("|  Extra Information:                               |\r\n");
        sb.append("|                                                   |\r\n");
        sb.append("|   @Parameter x - Skill level                      |\r\n");
        sb.append("|   @Parameter y - Unknown (Affinity Heart I)       |\r\n");
        sb.append("|   @Parameter u - Math.floor to the higher number  |\r\n");
        sb.append("|   @Parameter d - Math.floor to the lower number   |\r\n");
        sb.append("+---------------------------------------------------+\r\n");
        try {
            FileOutputStream out = new FileOutputStream(fileName, false);
            out.write(sb.toString().getBytes());
        } catch (IOException ex) {
            if (!exceptions.contains(ex)) {
                exceptions.add(ex);
            }
            return false;
        }
        return true;
    }
    public static String[] opt = {"icon", "iconMouseOver", "iconDisabled",
        "invisible", "level", "effect", "affected", "info", "common", "disable",
        "action", "hit", "screen", "repeat", "effect0", "timeLimited",
        "notExtend", "special", "mobCode", "psd", "cDoor", "mDoor", "Frame",
        "PVPcommon", "elemAttr", "tile", "mob", "ball", "finalAttack", "summon",
        "masterLevel", "notRemoved", "affected0", "mob0", "skillTamingMob",
        "weather", "disableNextLevelInfo", "?????", "CharLevel", "req",
        "effect1", "effect2", "addAttack", "skillType", "weapon", "weapon2",
        "effect_1H", "state", "finish", "standBuffOn", "combatOrders",
        "specialActionFrame", "hyper", "reqLev", "psdSkill", "prepare",
        "prepare_1H", "keydown", "keydown_1H", "gaugemax", "gaugemax_1H",
        "attected", "afterimage", "special0", "effect3", "info2", "keydownend",
        "prepare0", "keydown0", "damage", "effectR", "pvpCommon", "number",
        "icon1", "icon2", "icon3", "icon4", "icon5", "effect4",
        "specialAffected", "subWeapon", "repeat2", "tile0", "keydownloop",
        "exceedInfo", "weapon ", "finish0", "back_effect0", "back_effect",
        "back", "back_finish", "sDoor", "oDoor", "eDoor", "ball0", "edgeEffect",
        "anotherModeInfo", "keydownend0", "mob1", "mob2", "fixLevel",
        "skillpet", "skillDelay", "icon6", "skillpetAction", "specialPre",
        "specialLoop", "specialEnd", "specialLoop0", "special1", "loop", "end",
        "screen0", "PVP", "specialAction", "type", "monkeyAction", "mob3",
        "mob4", "specialAffcted", "hit0", "hit1", "effect_ship", "normalGauge",
        "commandGauge", "effectCash1_", "effectCash1_1", "effectCash2_",
        "effectCash2_1", "effectCash3_", "effectCash3_1", "vehicleID",
        "reduceMoveTime", "setItemReason", "setItemPartsCount", "1",
        "petPassive", "maxLevel", "1932014", "pvp", "chainAttack"};
}
