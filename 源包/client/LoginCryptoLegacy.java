package client;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import org.apache.log4j.Logger;

public class LoginCryptoLegacy {

    private static final Random rand = new Random();
    private static final char[] iota64 = new char[64];

    private static final Logger log = Logger.getLogger(LoginCryptoLegacy.class);

    public static String hashPassword(String password) {
        byte[] randomBytes = new byte[6];
        rand.setSeed(System.currentTimeMillis());
        rand.nextBytes(randomBytes);
        return myCrypt(password, genSalt(randomBytes));
    }

    public static boolean checkPassword(String password, String hash) {
        return myCrypt(password, hash).equals(hash);
    }

    public static boolean isLegacyPassword(String hash) {
        return hash.substring(0, 3).equals("$H$");
    }

    private static String myCrypt(String password, String seed) throws RuntimeException {
        String out = null;
        int count = 8;

        if (!seed.substring(0, 3).equals("$H$")) {
            byte[] randomBytes = new byte[6];
            rand.nextBytes(randomBytes);
            seed = genSalt(randomBytes);
        }

        String salt = seed.substring(4, 12);
        if (salt.length() < 8) {
            throw new RuntimeException("Error hashing password - Invalid seed.");
        }
        try {
            MessageDigest digester = MessageDigest.getInstance("SHA-1");

            digester.update((salt + password).getBytes("iso-8859-1"), 0, (salt + password).length());
            byte[] sha1Hash = digester.digest();
            do {
                byte[] CombinedBytes = new byte[sha1Hash.length + password.length()];
                System.arraycopy(sha1Hash, 0, CombinedBytes, 0, sha1Hash.length);
                System.arraycopy(password.getBytes("iso-8859-1"), 0, CombinedBytes, sha1Hash.length, password.getBytes("iso-8859-1").length);
                digester.update(CombinedBytes, 0, CombinedBytes.length);
                sha1Hash = digester.digest();
            } while (--count > 0);
            out = seed.substring(0, 12);
            out += encode64(sha1Hash);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException Ex) {
            System.err.println("Error hashing password." + Ex);
        }
        if (out == null) {
            throw new RuntimeException("Error hashing password - out = null");
        }
        return out;
    }

    private static String genSalt(byte[] Random) {
        StringBuilder Salt = new StringBuilder("$H$");
        Salt.append(iota64[30]);
        Salt.append(encode64(Random));
        return Salt.toString();
    }

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = data[i] >>> 4 & 0xF;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9)) {
                    buf.append((char) (48 + halfbyte));
                } else {
                    buf.append((char) (97 + (halfbyte - 10)));
                }
                halfbyte = data[i] & 0xF;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    public static String encodeSHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        return convertToHex(md.digest());
    }

    private static String encode64(byte[] Input) {
        int iLen = Input.length;
        int oDataLen = (iLen * 4 + 2) / 3;
        int oLen = (iLen + 2) / 3 * 4;

        char[] out = new char[oLen];
        int ip = 0;
        int op = 0;
        while (ip < iLen) {
            int i0 = Input[(ip++)] & 0xFF;
            int i1 = ip < iLen ? Input[(ip++)] & 0xFF : 0;
            int i2 = ip < iLen ? Input[(ip++)] & 0xFF : 0;
            int o0 = i0 >>> 2;
            int o1 = (i0 & 0x3) << 4 | i1 >>> 4;
            int o2 = (i1 & 0xF) << 2 | i2 >>> 6;
            int o3 = i2 & 0x3F;
            out[(op++)] = iota64[o0];
            out[(op++)] = iota64[o1];
            out[op] = (op < oDataLen ? iota64[o2] : '=');
            op++;
            out[op] = (op < oDataLen ? iota64[o3] : '=');
            op++;
        }
        return new String(out);
    }

    static {
        int i = 0;
        iota64[(i++)] = '.';
        iota64[(i++)] = '/';
        for (char c = 'A'; c <= 'Z'; c = (char) (c + '\001')) {
            iota64[(i++)] = c;
        }
        for (char c = 'a'; c <= 'z'; c = (char) (c + '\001')) {
            iota64[(i++)] = c;
        }
        for (char c = '0'; c <= '9'; c = (char) (c + '\001')) {
            iota64[(i++)] = c;
        }
    }
}
