package tools;

public class BitTools {

    public static int getShort(byte[] array, int index) {
        int ret = array[index];
        ret &= 255;
        ret |= array[(index + 1)] << 8 & 0xFF00;
        return ret;
    }

    public static String getString(byte[] array, int index, int length) {
        char[] cret = new char[length];
        for (int x = 0; x < length; x++) {
            cret[x] = (char) array[(x + index)];
        }
        return String.valueOf(cret);
    }

    public static String getMapleString(byte[] array, int index) {
        int length = array[index] & 0xFF | array[(index + 1)] << 8 & 0xFF00;
        return getString(array, index + 2, length);
    }

    public static byte rollLeft(byte in, int count) {
        int tmp = in & 0xFF;

        tmp <<= count % 8;
        return (byte) (tmp & 0xFF | tmp >> 8);
    }

    public static byte rollRight(byte in, int count) {
        int tmp = in & 0xFF;
        tmp = tmp << 8 >>> count % 8;

        return (byte) (tmp & 0xFF | tmp >>> 8);
    }

    public static byte[] multiplyBytes(byte[] in, int count, int mul) {
        byte[] ret = new byte[count * mul];
        for (int x = 0; x < count * mul; x++) {
            ret[x] = in[(x % count)];
        }
        return ret;
    }

    public static int doubleToShortBits(double d) {
        long l = Double.doubleToLongBits(d);
        return (int) (l >> 48);
    }
}
