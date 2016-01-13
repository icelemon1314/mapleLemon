package constants;

import server.ServerProperties;

public class WorldConstants {

    public static int EXP_RATE = 1;
    public static int MESO_RATE = 1;
    public static int DROP_RATE = 1;
    public static byte FLAG = 3;
    public static int CHANNEL_COUNT = 5;
    public static String WORLD_TIP = "怀旧冒×岛";
    public static final int gmserver = -1; // -1 = 无GM服务器
    public static final byte recommended = (byte) getMainWorld().getWorld(); //-1 = no recommended
    public static final String recommendedmsg = recommended < 0 ? "" : "        Join " + getById(recommended).name() + ",       the newest world! (If youhave friends who play, consider joining their worldinstead. Characters can`t move between worlds.)";

    private static int channels = 0;

    public static interface Option {

        public int getWorld();

        public int getExp();

        public int getMeso();

        public int getDrop();

        public byte getFlag();

        public boolean show();

        public boolean isAvailable();

        public int getChannelCount();

        public String getWorldTip();

        public void setExp(int info);

        public void setMeso(int info);

        public void setDrop(int info);

        public void setFlag(byte info);

        public void setShow(boolean info);

        public void setAvailable(boolean info);

        public void setChannelCount(int info);

        public void setWorldTip(String info);

        public String name();
    }

    /**
     *
     * @Warning: World will be duplicated if it's the same as the gm server
     */
    public static enum WorldOption implements Option {

        蓝蜗牛(0, true),
        蘑菇仔(1),
        绿水灵(2),
        漂漂猪(3),
        小青蛇(4),
        红螃蟹(5),
        大海龟(6),
        章鱼怪(7),
        顽皮猴(8),
        星精灵(9),
        胖企鹅(10);
        private final int world, exp, meso, drop, channels;
        private final byte flag;//1 事件, 2 新, 3 热
        public final boolean show, available;
        private final String worldtip;

        WorldOption(int world) {
            this.world = world;
            this.exp = 0;
            this.meso = 0;
            this.drop = 0;
            this.flag = -1;
            this.show = false;
            this.available = false;
            this.channels = 0;
            this.worldtip = null;
        }

        WorldOption(int world, boolean show) {
            this.world = world;
            this.exp = 0;
            this.meso = 0;
            this.drop = 0;
            this.flag = -1;
            this.show = show;
            this.available = show;
            this.channels = 0;
            this.worldtip = null;
        }

        @Override
        public int getWorld() {
            return world;
        }

        @Override
        public int getExp() {
            int info = ServerProperties.getProperty("expWorld" + world, exp);
            return info > 0 ? info : EXP_RATE;
        }

        @Override
        public int getMeso() {
            int info = ServerProperties.getProperty("mesoWorld" + world, meso);
            return info > 0 ? info : MESO_RATE;
        }

        @Override
        public int getDrop() {
            int info = ServerProperties.getProperty("dropWorld" + world, drop);
            return info > 0 ? info : DROP_RATE;
        }

        @Override
        public byte getFlag() {
            byte info = ServerProperties.getProperty("flagWorld" + world, flag);
            return info >= 0 ? info : FLAG;
        }

        @Override
        public boolean show() {
            return ServerProperties.getProperty("showWorld" + world, show);
        }

        @Override
        public boolean isAvailable() {
            return ServerProperties.getProperty("availableWorld" + world, available);
        }

        @Override
        public int getChannelCount() {
            int info = ServerProperties.getProperty("channelWorld" + world, channels);
            return info > 0 ? info : CHANNEL_COUNT;
        }

        @Override
        public String getWorldTip() {
            String info = ServerProperties.getProperty("tipWorld" + world, worldtip);
            return info != null ? info : WORLD_TIP;
        }

        @Override
        public void setExp(int info) {
            if (info == exp) {
                ServerProperties.removeProperty("expWorld" + world);
                return;
            }
            ServerProperties.setProperty("expWorld" + world, info);
        }

        @Override
        public void setMeso(int info) {
            if (info == meso) {
                ServerProperties.removeProperty("mesoWorld" + world);
                return;
            }
            ServerProperties.setProperty("mesoWorld" + world, info);
        }

        @Override
        public void setDrop(int info) {
            if (info == drop) {
                ServerProperties.removeProperty("dropWorld" + world);
                return;
            }
            ServerProperties.setProperty("dropWorld" + world, info);
        }

        @Override
        public void setFlag(byte info) {
            if (info == flag) {
                ServerProperties.removeProperty("flagWorld" + world);
                return;
            }
            ServerProperties.setProperty("flagWorld" + world, info);
        }

        @Override
        public void setShow(boolean info) {
            if (info == show) {
                ServerProperties.removeProperty("showWorld" + world);
                return;
            }
            ServerProperties.setProperty("showWorld" + world, info);
        }

        @Override
        public void setAvailable(boolean info) {
            if (info == available) {
                ServerProperties.removeProperty("availableWorld" + world);
                return;
            }
            ServerProperties.setProperty("availableWorld" + world, info);
        }

        @Override
        public void setChannelCount(int info) {
            if (info == channels) {
                ServerProperties.removeProperty("channelWorld" + world);
                return;
            }
            ServerProperties.setProperty("channelWorld" + world, info);
        }

        @Override
        public void setWorldTip(String info) {
            if (info == worldtip) {
                ServerProperties.removeProperty("tipWorld" + world);
                return;
            }
            ServerProperties.setProperty("tipWorld" + world, info);
        }
    }

    public static enum TespiaWorldOption implements Option {

        測試機("t0", true);
        private final int world, exp, meso, drop, channels;
        private final byte flag;
        private final boolean show, available;
        private final String worldName, worldtip;

        TespiaWorldOption(String world) {
            this.world = Integer.parseInt(world.replaceAll("t", ""));
            this.worldName = world;
            this.exp = 0;
            this.meso = 0;
            this.drop = 0;
            this.flag = -1;
            this.show = false;
            this.available = false;
            this.channels = 0;
            this.worldtip = null;
        }

        TespiaWorldOption(String world, boolean show) {
            this.world = Integer.parseInt(world.replaceAll("t", ""));
            this.worldName = world;
            this.exp = 0;
            this.meso = 0;
            this.drop = 0;
            this.flag = -1;
            this.show = show;
            this.available = show;
            this.channels = 0;
            this.worldtip = null;
        }

        @Override
        public int getWorld() {
            return world;
        }

        @Override
        public int getExp() {
            int info = ServerProperties.getProperty("expWorld" + worldName, exp);
            return info > 0 ? info : EXP_RATE;
        }

        @Override
        public int getMeso() {
            int info = ServerProperties.getProperty("mesoWorld" + worldName, meso);
            return info > 0 ? info : MESO_RATE;
        }

        @Override
        public int getDrop() {
            int info = ServerProperties.getProperty("dropWorld" + worldName, drop);
            return info > 0 ? info : DROP_RATE;
        }

        @Override
        public byte getFlag() {
            byte info = ServerProperties.getProperty("flagWorld" + worldName, flag);
            return info >= 0 ? info : FLAG;
        }

        @Override
        public boolean show() {
            return ServerProperties.getProperty("showWorld" + worldName, show);
        }

        @Override
        public boolean isAvailable() {
            return ServerProperties.getProperty("availableWorld" + worldName, available);
        }

        @Override
        public int getChannelCount() {
            int info = ServerProperties.getProperty("channelWorld" + worldName, channels);
            return info > 0 ? info : CHANNEL_COUNT;
        }

        @Override
        public String getWorldTip() {
            String info = ServerProperties.getProperty("tipWorld" + worldName, worldtip);
            return info != null ? info : WORLD_TIP;
        }

        @Override
        public void setExp(int info) {
            if (info == exp) {
                ServerProperties.removeProperty("expWorld" + worldName);
                return;
            }
            ServerProperties.setProperty("expWorld" + worldName, info);
        }

        @Override
        public void setMeso(int info) {
            if (info == meso) {
                ServerProperties.removeProperty("mesoWorld" + worldName);
                return;
            }
            ServerProperties.setProperty("mesoWorld" + worldName, info);
        }

        @Override
        public void setDrop(int info) {
            if (info == drop) {
                ServerProperties.removeProperty("dropWorld" + worldName);
                return;
            }
            ServerProperties.setProperty("dropWorld" + worldName, info);
        }

        @Override
        public void setFlag(byte info) {
            if (info == flag) {
                ServerProperties.removeProperty("flagWorld" + worldName);
                return;
            }
            ServerProperties.setProperty("flagWorld" + worldName, info);
        }

        @Override
        public void setShow(boolean info) {
            if (info == show) {
                ServerProperties.removeProperty("showWorld" + worldName);
                return;
            }
            ServerProperties.setProperty("showWorld" + worldName, info);
        }

        @Override
        public void setAvailable(boolean info) {
            if (info == available) {
                ServerProperties.removeProperty("availableWorld" + worldName);
                return;
            }
            ServerProperties.setProperty("availableWorld" + worldName, info);
        }

        @Override
        public void setChannelCount(int info) {
            if (info == channels) {
                ServerProperties.removeProperty("channelWorld" + worldName);
                return;
            }
            ServerProperties.setProperty("channelWorld" + worldName, info);
        }

        @Override
        public void setWorldTip(String info) {
            if (info == worldtip) {
                ServerProperties.removeProperty("tipWorld" + worldName);
                return;
            }
            ServerProperties.setProperty("tipWorld" + worldName, info);
        }
    }

    public static Option[] values() {
        return ServerConstants.TESPIA ? TespiaWorldOption.values() : WorldOption.values();
    }

    public static Option valueOf(String name) {
        return ServerConstants.TESPIA ? TespiaWorldOption.valueOf(name) : WorldOption.valueOf(name);
    }

    public static Option getById(int g) {
        for (Option e : values()) {
            if (e.getWorld() == g) {
                return e;
            }
        }
        return null;
    }

    public static Option getMainWorld() {
        for (Option e : values()) {
            if (e.show() == true) {
                return e;
            }
        }
        return null;
    }

    public static boolean isExists(int id) {
        return getById(id) != null;
    }

    public static String getNameById(int serverid) {
        if (getById(serverid) == null) {
            System.err.println("World doesn't exists exception. ID: " + serverid);
            return "";
        }
        return getById(serverid).name();
    }

    public static int getChannelCount() {
        if (channels <= 0) {
            for (Option e : values()) {
                if (e.getChannelCount() > channels) {
                    channels = e.getChannelCount();
                }
            }
        }
        return channels;
    }

    public static void loadSetting() {
        FLAG = ServerProperties.getProperty("FLAG", FLAG);
        EXP_RATE = ServerProperties.getProperty("EXP_RATE", EXP_RATE);
        MESO_RATE = ServerProperties.getProperty("MESO_RATE", MESO_RATE);
        DROP_RATE = ServerProperties.getProperty("DROP_RATE", DROP_RATE);
        WORLD_TIP = ServerProperties.getProperty("WORLD_TIP", WORLD_TIP);
        CHANNEL_COUNT = ServerProperties.getProperty("CHANNEL_COUNT", CHANNEL_COUNT);
    }

    static {
        loadSetting();
    }
}
