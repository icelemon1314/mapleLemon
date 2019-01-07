package client.inventory;

public enum PetFlag {

    PET_PICKUP_ITEM(1, 5190000, 5191000),
    PET_LONG_RANGE(2, 5190002, 5191002),
    PET_DROP_SWEEP(4, 5190003, 5191003),
    PET_IGNORE_PICKUP(8, 5190005, -1),
    PET_PICKUP_ALL(16, 5190004, 5191004),
    PET_CONSUME_HP(32, 5190001, 5191001),
    PET_CONSUME_MP(64, 5190006, -1),
    PET_RECALL(128, 5190007, -1),
    PET_AUTO_SPEAKING(256, 5190008, -1),
    PET_AUTO_BUFF(512, 5190010, -1),
    PET_SMART(2048, 5190011, -1);

    private final int i;
    private final int item;
    private final int remove;

    private PetFlag(int i, int item, int remove) {
        this.i = i;
        this.item = item;
        this.remove = remove;
    }

    public int getValue() {
        return this.i;
    }

    public boolean check(int flag) {
        return (flag & this.i) == this.i;
    }

    public static PetFlag getByAddId(int itemId) {
        for (PetFlag flag : values()) {
            if (flag.item == itemId) {
                return flag;
            }
        }
        return null;
    }

    public static PetFlag getByDelId(int itemId) {
        for (PetFlag flag : values()) {
            if (flag.remove == itemId) {
                return flag;
            }
        }
        return null;
    }
}
