package server.life;

public class OverrideMonsterStats {

    public long hp;
    public int exp;
    public int mp;

    public OverrideMonsterStats() {
        this.hp = 1L;
        this.exp = 0;
        this.mp = 0;
    }

    public OverrideMonsterStats(long hp, int mp, int exp, boolean change) {
        this.hp = hp;
        this.mp = mp;
        this.exp = exp;
    }

    public OverrideMonsterStats(long hp, int mp, int exp) {
        this(hp, mp, exp, true);
    }

    public int getExp() {
        return this.exp;
    }

    public void setOExp(int exp) {
        this.exp = exp;
    }

    public long getHp() {
        return this.hp;
    }

    public void setOHp(long hp) {
        this.hp = hp;
    }

    public int getMp() {
        return this.mp;
    }

    public void setOMp(int mp) {
        this.mp = mp;
    }
}
