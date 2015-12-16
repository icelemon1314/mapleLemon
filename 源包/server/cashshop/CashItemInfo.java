package server.cashshop;

public class CashItemInfo {

    private final int itemId;
    private final int count;
    private final int price;
    private final int originalPrice;
    private final int sn;
    private final int period;
    private final int gender;
    private final boolean onSale;
    private final boolean bonus;
    private final boolean refundable;
    private final boolean discount;

    public CashItemInfo(int itemId, int count, int price, int originalPrice, int sn, int period, int gender, boolean sale, boolean bonus, boolean refundable, boolean discount) {
        this.itemId = itemId;
        this.count = count;
        this.price = price;
        this.originalPrice = originalPrice;
        this.sn = sn;
        this.period = period;
        this.gender = gender;
        this.onSale = sale;
        this.bonus = bonus;
        this.refundable = refundable;
        this.discount = discount;
    }

    public int getId() {
        return this.itemId;
    }

    public int getCount() {
        return this.count;
    }

    public int getPrice() {
        return Math.max(this.price, this.originalPrice);
    }

    public int getOriginalPrice() {
        return this.originalPrice;
    }

    public int getSN() {
        return this.sn;
    }

    public int getPeriod() {
        return this.period;
    }

    public int getGender() {
        return this.gender;
    }

    public boolean onSale() {
        return this.onSale;
    }

    public boolean genderEquals(int g) {
        return (g == this.gender) || (this.gender == 2);
    }

    public boolean isBonus() {
        return this.bonus;
    }

    public boolean isRefundable() {
        return this.refundable;
    }

    public boolean isDiscount() {
        return this.discount;
    }
}
