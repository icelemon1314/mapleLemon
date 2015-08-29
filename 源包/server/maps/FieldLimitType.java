package server.maps;

public enum FieldLimitType {

    Jump(1),
    MovementSkills(2),
    SummoningBag(4),
    MysticDoor(8),
    ChannelSwitch(16),
    RegularExpLoss(32),
    VipRock(64),
    Minigames(128),
    Mount(512),
    PotionUse(4096),
    Event(8192),
    Pet(32768),
    Event2(65536),
    DropDown(131072);

    private final int i;

    private FieldLimitType(int i) {
        this.i = i;
    }

    public int getValue() {
        return this.i;
    }

    public boolean check(int fieldlimit) {
        return (fieldlimit & this.i) == this.i;
    }
}
