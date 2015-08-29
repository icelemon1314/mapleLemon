package server.life;

import java.awt.Point;

public class MobAttackInfo {

    private boolean isDeadlyAttack;
    private int mpBurn;
    private int mpCon;
    private int diseaseSkill;
    private int diseaseLevel;
    public int PADamage;
    public int MADamage;
    public int attackAfter;
    public int range = 0;
    public Point lt = null;
    public Point rb = null;
    public boolean magic = false;

    public void setDeadlyAttack(boolean isDeadlyAttack) {
        this.isDeadlyAttack = isDeadlyAttack;
    }

    public boolean isDeadlyAttack() {
        return this.isDeadlyAttack;
    }

    public void setMpBurn(int mpBurn) {
        this.mpBurn = mpBurn;
    }

    public int getMpBurn() {
        return this.mpBurn;
    }

    public void setDiseaseSkill(int diseaseSkill) {
        this.diseaseSkill = diseaseSkill;
    }

    public int getDiseaseSkill() {
        return this.diseaseSkill;
    }

    public void setDiseaseLevel(int diseaseLevel) {
        this.diseaseLevel = diseaseLevel;
    }

    public int getDiseaseLevel() {
        return this.diseaseLevel;
    }

    public void setMpCon(int mpCon) {
        this.mpCon = mpCon;
    }

    public int getMpCon() {
        return this.mpCon;
    }

    public int getRange() {
        int maxX = Math.max(Math.abs(this.lt == null ? 0 : this.lt.x), Math.abs(this.rb == null ? 0 : this.rb.x));
        int maxY = Math.max(Math.abs(this.lt == null ? 0 : this.lt.y), Math.abs(this.rb == null ? 0 : this.rb.y));
        return Math.max(maxX * maxX + maxY * maxY, this.range);
    }
}
