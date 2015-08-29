package server.shop;

public enum MapleShopResponse {

    购买道具完成(0x00),
    背包空间不够(0x03),
    卖出道具完成(0x08),
    充值飞镖完成(0x00),
    充值金币不够(0x2),
    购买回购出错(0x1D);

    private final int value;

    private MapleShopResponse(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
