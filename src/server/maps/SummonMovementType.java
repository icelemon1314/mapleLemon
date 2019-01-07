package server.maps;

public enum SummonMovementType {

    不会移动(0),
    飞行跟随(1),
    自由移动(2),
    跟随并且随机移动打怪(3),
    CIRCLE_STATIONARY(5),
    移动跟随(7),
    跟随移动跟随攻击(8);

    private final int val;

    private SummonMovementType(int val) {
        this.val = val;
    }

    public int getValue() {
        return this.val;
    }
}
