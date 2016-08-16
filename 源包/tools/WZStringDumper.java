/*
	
 */
package tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;

/**
 *
 * @author Danny
 */
public class WZStringDumper {

    public static void main(String args[]) throws FileNotFoundException, IOException {
        File stringFile = MapleDataProviderFactory.fileInWZPath("wz\\string.wz");
        MapleDataProvider stringProvider = MapleDataProviderFactory.getDataProvider(stringFile);

        MapleData cash = stringProvider.getData("Cash.img");
        MapleData consume = stringProvider.getData("Consume.img");
        MapleData eqp = stringProvider.getData("Eqp.img").getChildByPath("Eqp");
        MapleData etc = stringProvider.getData("Etc.img").getChildByPath("Etc");
        MapleData ins = stringProvider.getData("Ins.img");
        MapleData pet = stringProvider.getData("Pet.img");
        MapleData map = stringProvider.getData("Map.img");
        MapleData mob = stringProvider.getData("Mob.img");
        MapleData skill = stringProvider.getData("Skill.img");
        MapleData npc = stringProvider.getData("Npc.img");

        String output = "wzdata";

        File outputDir = new File(output);
        File cashTxt = new File(output + "\\Cash.txt");
        File useTxt = new File(output + "\\Use.txt");
        File eqpDir = new File(output + "\\Equip");
        File etcTxt = new File(output + "\\Etc.txt");
        File insTxt = new File(output + "\\Setup.txt");
        File petTxt = new File(output + "\\Pet.txt");
        File mapTxt = new File(output + "\\Map.txt");
        File mobTxt = new File(output + "\\Mob.txt");
        File skillTxt = new File(output + "\\Skill.txt");
        File npcTxt = new File(output + "\\NPC.txt");
        outputDir.mkdir();
        cashTxt.createNewFile();
        useTxt.createNewFile();
        eqpDir.mkdir();
        etcTxt.createNewFile();
        insTxt.createNewFile();
        petTxt.createNewFile();
        mapTxt.createNewFile();
        mobTxt.createNewFile();
        skillTxt.createNewFile();
        npcTxt.createNewFile();

        FileoutputUtil.log("提取 Cash.img 数据...");
        PrintWriter writer = new PrintWriter(new FileOutputStream(cashTxt));
        for (MapleData child : cash.getChildren()) {
            MapleData nameData = child.getChildByPath("name");
            MapleData descData = child.getChildByPath("desc");
            String name = "";
            String desc = "(无描述)";
            if (nameData != null) {
                name = (String) nameData.getData();
            }
            if (descData != null) {
                desc = (String) descData.getData();
            }
            writer.println(child.getName() + " - " + name + " - " + desc);
        }
        writer.flush();
        writer.close();
        FileoutputUtil.log("Cash.img 提取完成.");

        FileoutputUtil.log("提取 Consume.img 数据...");
        writer = new PrintWriter(new FileOutputStream(useTxt));
        for (MapleData child : consume.getChildren()) {
            MapleData nameData = child.getChildByPath("name");
            MapleData descData = child.getChildByPath("desc");
            String name = "";
            String desc = "(无描述)";
            if (nameData != null) {
                name = (String) nameData.getData();
            }
            if (descData != null) {
                desc = (String) descData.getData();
            }
            writer.println(child.getName() + " - " + name + " - " + desc);
        }
        writer.flush();
        writer.close();
        FileoutputUtil.log("Consume.img 提取完成.");

        FileoutputUtil.log("提取 Eqp.img 数据...");
        for (MapleData child : eqp.getChildren()) {
            FileoutputUtil.log("提取 " + child.getName() + " 数据...");
            File eqpFile = new File(output + "\\Equip\\" + child.getName() + ".txt");
            eqpFile.createNewFile();
            PrintWriter eqpWriter = new PrintWriter(new FileOutputStream(eqpFile));
            for (MapleData child2 : child.getChildren()) {
                MapleData nameData = child2.getChildByPath("name");
                MapleData descData = child2.getChildByPath("desc");
                String name = "";
                String desc = "(无描述)";
                if (nameData != null) {
                    name = (String) nameData.getData();
                }
                if (descData != null) {
                    desc = (String) descData.getData();
                }
                eqpWriter.println(child2.getName() + " - " + name + " - " + desc);
            }
            eqpWriter.flush();
            eqpWriter.close();
            FileoutputUtil.log(child.getName() + " 提取完成.");
        }
        FileoutputUtil.log("Eqp.img 提取完成.");

        FileoutputUtil.log("提取 Etc.img 数据...");
        writer = new PrintWriter(new FileOutputStream(etcTxt));
        for (MapleData child : etc.getChildren()) {
            MapleData nameData = child.getChildByPath("name");
            MapleData descData = child.getChildByPath("desc");
            String name = "";
            String desc = "(无描述)";
            if (nameData != null) {
                name = (String) nameData.getData();
            }
            if (descData != null) {
                desc = (String) descData.getData();
            }
            writer.println(child.getName() + " - " + name + " - " + desc);
        }
        writer.flush();
        writer.close();
        FileoutputUtil.log("Etc.img 提取完成.");

        FileoutputUtil.log("提取 Ins.img 数据...");
        writer = new PrintWriter(new FileOutputStream(insTxt));
        for (MapleData child : ins.getChildren()) {
            MapleData nameData = child.getChildByPath("name");
            MapleData descData = child.getChildByPath("desc");
            String name = "";
            String desc = "(无描述)";
            if (nameData != null) {
                name = (String) nameData.getData();
            }
            if (descData != null) {
                desc = (String) descData.getData();
            }
            writer.println(child.getName() + " - " + name + " - " + desc);
        }
        writer.flush();
        writer.close();
        FileoutputUtil.log("Ins.img 提取完成.");

        FileoutputUtil.log("提取 Pet.img 数据...");
        writer = new PrintWriter(new FileOutputStream(petTxt));
        for (MapleData child : pet.getChildren()) {
            MapleData nameData = child.getChildByPath("name");
            MapleData descData = child.getChildByPath("desc");
            String name = "";
            String desc = "(无描述)";
            if (nameData != null) {
                name = (String) nameData.getData();
            }
            if (descData != null) {
                desc = (String) descData.getData();
            }
            writer.println(child.getName() + " - " + name + " - " + desc);
        }
        writer.flush();
        writer.close();
        FileoutputUtil.log("Pet.img 提取完成.");

        FileoutputUtil.log("提取 Map.img 数据...");
        writer = new PrintWriter(new FileOutputStream(mapTxt));
        for (MapleData child : map.getChildren()) {
            writer.println(child.getName());
            writer.println();
            for (MapleData child2 : child.getChildren()) {
                MapleData streetData = child2.getChildByPath("streetName");
                MapleData mapData = child2.getChildByPath("mapName");
                String streetName = "(无数据名)";
                String mapName = "(无地图名)";
                if (streetData != null) {
                    streetName = (String) streetData.getData();
                }
                if (mapData != null) {
                    mapName = (String) mapData.getData();
                }
                writer.println(child2.getName() + " - " + streetName + " - " + mapName);
            }
            writer.println();
        }
        writer.flush();
        writer.close();
        FileoutputUtil.log("Map.img 提取完成.");

        FileoutputUtil.log("提取 Mob.img 数据...");
        writer = new PrintWriter(new FileOutputStream(mobTxt));
        for (MapleData child : mob.getChildren()) {
            MapleData nameData = child.getChildByPath("name");
            String name = "";
            if (nameData != null) {
                name = (String) nameData.getData();
            }
            writer.println(child.getName() + " - " + name);
        }
        writer.flush();
        writer.close();
        FileoutputUtil.log("Mob.img 提取完成.");

        FileoutputUtil.log("提取 Skill.img 数据...");
        writer = new PrintWriter(new FileOutputStream(skillTxt));
        for (MapleData child : skill.getChildren()) {
            MapleData nameData = child.getChildByPath("name");
            MapleData descData = child.getChildByPath("desc");
            MapleData bookData = child.getChildByPath("bookName");
            MapleData detialData = child.getChildByPath("h");
            String name = "";
            String desc = "";
            String detial = "";
            if (nameData != null) {
                name = (String) nameData.getData();
            }
            if (descData != null) {
                desc = (String) descData.getData();
            }
            if (detialData != null) {
                detial = (String) detialData.getData();
            }
            if (bookData != null) {
                continue;
            } else {
                writer.println("[" + child.getName() + "] - " + name + " - " + desc);
                writer.println("详细信息：" + detial);
            }
        }
        writer.flush();
        writer.close();
        FileoutputUtil.log("Skill.img 提取完成.");

        FileoutputUtil.log("提取 Npc.img 数据...");
        writer = new PrintWriter(new FileOutputStream(npcTxt));
        for (MapleData child : npc.getChildren()) {
            MapleData nameData = child.getChildByPath("name");
            String name = "";
            if (nameData != null) {
                name = (String) nameData.getData();
            }
            writer.println(child.getName() + " - " + name);
        }
        writer.flush();
        writer.close();
        FileoutputUtil.log("Npc.img 提取完成.");
    }
}
