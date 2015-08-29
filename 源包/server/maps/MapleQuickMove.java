package server.maps;

public enum MapleQuickMove {
    射手村(100000000, QuickMoveNPC.大陆移动码头.getValue() | QuickMoveNPC.出租车.getValue()) ,
    魔法密林(101000000, QuickMoveNPC.大陆移动码头.getValue() | QuickMoveNPC.出租车.getValue()),
    勇士部落(102000000, QuickMoveNPC.大陆移动码头.getValue() | QuickMoveNPC.出租车.getValue()),
    废弃都市(103000000, QuickMoveNPC.大陆移动码头.getValue() | QuickMoveNPC.出租车.getValue()),
    明珠港(104000000, QuickMoveNPC.大陆移动码头.getValue() | QuickMoveNPC.出租车.getValue()),
    林中之城(105000000, QuickMoveNPC.大陆移动码头.getValue() | QuickMoveNPC.出租车.getValue()),
    天空之城(200000000, QuickMoveNPC.大陆移动码头.getValue()),
    玩具城(220000000, QuickMoveNPC.大陆移动码头.getValue());
    private final int map, npc;
    private final int generalNpc = 
             QuickMoveNPC.自由市场.getValue()
            | QuickMoveNPC.皇家美发.getValue()
            | QuickMoveNPC.皇家整形.getValue();

    private MapleQuickMove(int map, int npc) {
        this.map = map;
        this.npc = npc | generalNpc;
    }

    public int getMap() {
        return map;
    }

    public int getNPCFlag() {
        return npc;
    }

    public enum QuickMoveNPC {

        大陆移动码头(5, true, 9000086, 0, "移动到距离当前位置最近的#c<大陆移动码头>#。"),
        自由市场(3, true, 9000087, 0, "移动到可以和其他玩家交易道具的#c<自由市场>#。"),
        出租车(6, true, 9000089, 0, "使用可以让角色移动到附近主要地区的#c<出租车>#。"),
        皇家美发(13, true, 9000123, 1, "在爱德华那里可以更换漂亮的发型。"),
        皇家整形(14, true, 9000124, 1, "在塑料罗伊那里可以接受整容。");
        private final int value, type, id, level;
        private final String desc;
        private final boolean show;

        private QuickMoveNPC(int type, boolean show, int id, int level, String desc) {
            this.value = (int) Math.pow(2, type);
            this.type = type;
            this.show = show;
            this.id = id;
            this.level = level;
            this.desc = desc;
        }

        public final int getValue() {
            return value;
        }

        public final boolean check(int flag) {
            return (flag & value) != 0;
        }

        public int getType() {
            return type;
        }

        public boolean show() {
            return show;
        }

        public int getId() {
            return id;
        }

        public int getLevel() {
            return level;
        }

        public String getDescription() {
            return desc;
        }
    }
}
