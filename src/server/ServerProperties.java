package server;

import constants.WorldConstants;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import tools.EncodingDetect;
import tools.FileoutputUtil;

public class ServerProperties {

    private static final Properties props = new Properties();

    public static boolean showPacket = false;
    private static final Map<String, Boolean> blockedOpcodes = new HashMap();
    private static boolean blockDefault;
    public static int maxHp = 99999;
    public static int maxMp = 99999;
    public static int maxLevel = 200;
    public static int maxCygnusLevel = 120;
    public static long maxMeso = 2147483647;

    public static String getPath() {
        return System.getProperty("path", "") + "config.ini";
    }

    public static void loadProperties() {
        try {
            InputStream in = new FileInputStream(getPath());
            BufferedReader bf = new BufferedReader(new InputStreamReader(in, EncodingDetect.getJavaEncode(getPath())));
            props.load(bf);
            bf.close();
        } catch (IOException ex) {
            FileoutputUtil.log("加载 " + getPath() + " 配置出错 " + ex);
        }
        showPacket = Boolean.parseBoolean(ServerProperties.getProperty("ShowPacket", String.valueOf(showPacket)));
        maxHp = Integer.parseInt(ServerProperties.getProperty("maxHp", String.valueOf(maxHp)));
        maxMp = Integer.parseInt(ServerProperties.getProperty("maxMp", String.valueOf(maxMp)));
        maxMeso = Long.parseLong(ServerProperties.getProperty("maxMeso", String.valueOf(maxMeso)));
        maxLevel = Integer.parseInt(ServerProperties.getProperty("maxLevel", String.valueOf(maxLevel)));
    }

    public static void saveProperties() {
        File outputFile = new File(getPath());
        if (outputFile.exists()) {
            outputFile.delete();
        }
        ArrayList<String> setting = new ArrayList();
        Map<String, ArrayList<String>> world = new HashMap();
        Map<String, ArrayList<String>> tespia = new HashMap();
        for (WorldConstants.WorldOption e : WorldConstants.WorldOption.values()) {
            world.put(e.name(), new ArrayList());
        }
        for (WorldConstants.TespiaWorldOption e : WorldConstants.TespiaWorldOption.values()) {
            tespia.put(e.name(), new ArrayList());
        }

        for (Map.Entry i : props.entrySet()) {
            String info = i.getKey() + " = " + i.getValue() + "\r\n";
            if (((String) i.getKey()).contains("World")) {
                int worldId = Integer.parseInt(((String) i.getKey()).substring(((String) i.getKey()).lastIndexOf('d') + 1));
                world.get(WorldConstants.getNameById(worldId)).add(info);
            } else if (((String) i.getKey()).contains("Worldt")) {
                int worldId = Integer.parseInt(((String) i.getKey()).substring(((String) i.getKey()).lastIndexOf('t') + 1));
                tespia.get(WorldConstants.getNameById(worldId)).add(info);
            } else {
                setting.add(info);
            }
        }

        FileoutputUtil.logToFile(getPath(), "# [配置]\r\n");
        for (String s:setting) {
            FileoutputUtil.logToFile(getPath(), s);
        }
//        setting.forEach((s) -> FileoutputUtil.logToFile(getPath(), s));

        FileoutputUtil.logToFile(getPath(), "\r\n# [服务器]\r\n");
        for (Map.Entry <String, ArrayList<String>> i:world.entrySet()) {
            if (i.getValue().isEmpty()) {
                return;
            }
            FileoutputUtil.logToFile(getPath(), "# " + i.getKey() + "\r\n");
            for (String s : i.getValue()) {
                FileoutputUtil.logToFile(getPath(), s);
            }
//            i.getValue().forEach((s) -> FileoutputUtil.logToFile(getPath(), s));
        }
        /*
        world.entrySet().forEach((i) -> {
            if (i.getValue().isEmpty()) {
                return;
            }
            FileoutputUtil.logToFile(getPath(), "# " + i.getKey() + "\r\n");
            i.getValue().forEach((s) -> FileoutputUtil.logToFile(getPath(), s));
        });*/

        FileoutputUtil.logToFile(getPath(), "\r\n# [测试机]\r\n");
        for (Map.Entry <String, ArrayList<String>> i:tespia.entrySet()) {
            if (i.getValue().isEmpty()) {
                return;
            }
            FileoutputUtil.logToFile(getPath(), "# " + i.getKey() + "\r\n");
            for (String s : i.getValue()) {
                FileoutputUtil.logToFile(getPath(), s);
            }
//            i.getValue().forEach((s) -> FileoutputUtil.logToFile(getPath(), s));
        }
        /*
        tespia.entrySet().forEach((i) -> {
            if (i.getValue().isEmpty()) {
                return;
            }
            FileoutputUtil.logToFile(getPath(), "# " + i.getKey() + "\r\n");
            i.getValue().forEach((s) -> FileoutputUtil.logToFile(getPath(), s));
        });*/
    }

    public static void setProperty(String prop, String newInf) {
        props.setProperty(prop, newInf);
    }

    public static void setProperty(String prop, boolean newInf) {
        props.setProperty(prop, String.valueOf(newInf));
    }

    public static void setProperty(String prop, byte newInf) {
        props.setProperty(prop, String.valueOf(newInf));
    }

//    public static String getProperty(String s) {
//        return props.getProperty(s);
//    }

    public static void setProperty(String prop, short newInf) {
        props.setProperty(prop, String.valueOf(newInf));
    }

    public static void setProperty(String prop, int newInf) {
        props.setProperty(prop, String.valueOf(newInf));
    }

    public static void setProperty(String prop, long newInf) {
        props.setProperty(prop, String.valueOf(newInf));
    }

    public static void removeProperty(String prop) {
        props.remove(prop);
    }

    public static String getProperty(String s, String def) {
        return props.getProperty(s, def);
    }

    public static boolean getProperty(String s, boolean def) {
        return getProperty(s, def ? "true" : "false").equalsIgnoreCase("true");
    }

    public static byte getProperty(String s, byte def) {
        String property = props.getProperty(s);
        if (property != null) {
            return Byte.parseByte(property);
        }
        return def;
    }

    public static short getProperty(String s, short def) {
        String property = props.getProperty(s);
        if (property != null) {
            return Short.parseShort(property);
        }
        return def;
    }

    public static int getProperty(String s, int def) {
        String property = props.getProperty(s);
        if (property != null) {
            return Integer.parseInt(property);
        }
        return def;
    }

    public static long getProperty(String s, long def) {
        String property = props.getProperty(s);
        if (property != null) {
            return Long.parseLong(property);
        }
        return def;
    }

    public static boolean ShowPacket() {
        return showPacket;
    }

    public static boolean SendPacket(String op, String pHeaderStr) {
//        if (op.equals("UNKNOWN")) {
//            return blockedOpcodes.containsKey("S_" + pHeaderStr) ? (blockedOpcodes.get("S_" + pHeaderStr)) : blockDefault;
//        }
        return blockedOpcodes.containsKey("S_" + op) ? (blockedOpcodes.get("S_" + op)) : blockDefault;
    }

    public static boolean RecvPacket(String op, String pHeaderStr) {
//        if (op.equals("UNKNOWN")) {
//            return blockedOpcodes.containsKey("R_" + pHeaderStr) ? (blockedOpcodes.get("R_" + pHeaderStr)) : blockDefault;
//        }
        return blockedOpcodes.containsKey("R_" + op) ? (blockedOpcodes.get("R_" + op)) : blockDefault;
    }

    public static int getMaxHp() {
        if ((maxHp < 99999) || (maxHp > 500000)) {
            maxHp = 99999;
        }
        return maxHp;
    }

    public static int getMaxMp() {
        if ((maxMp < 99999) || (maxMp > 500000)) {
            maxMp = 99999;
        }
        return maxMp;
    }

    public static long getMaxMeso() {
        if (maxMeso < 2147483647L) {
            maxMeso = 2147483647L;
        }
        return maxMeso;
    }

    public static int getMaxLevel() {
        if ((maxLevel < 200) || (maxLevel > 250)) {
            maxLevel = 200;
        }
        return maxLevel;
    }

    static {
        loadProperties();
    }
}
