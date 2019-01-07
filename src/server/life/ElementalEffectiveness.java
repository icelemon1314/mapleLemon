package server.life;

public enum ElementalEffectiveness {

    NORMAL(1.0D), 
    IMMUNE(0.0D), 
    STRONG(0.5D), 
    WEAK(1.5D);

    private final double value;

    private ElementalEffectiveness(double val) {
        this.value = val;
    }

    public double getValue() {
        return this.value;
    }

    public static ElementalEffectiveness getByNumber(int num) {
        switch (num) {
            case 1:
                return IMMUNE;
            case 2:
                return STRONG;
            case 3:
                return WEAK;
        }
        throw new IllegalArgumentException("Unkown effectiveness: " + num);
    }
}
