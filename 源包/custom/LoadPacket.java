/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package custom;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import tools.EncodingDetect;
import tools.FileoutputUtil;
import tools.HexTool;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 *
 * @author Itzik
 */
public class LoadPacket {

    public static byte[] getPacket() {
        Properties packetProps = new Properties();
        try {
            InputStream in = new FileInputStream("其他/文件封包.txt");
            BufferedReader bf = new BufferedReader(new InputStreamReader(in, EncodingDetect.getJavaEncode("其他/文件封包.txt")));
            packetProps.load(bf);
            bf.close();
        } catch (IOException ex) {
            FileoutputUtil.log("读取 文件封包.txt 失败" + ex);
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(HexTool.getByteArrayFromHexString(packetProps.getProperty("packet")));
        return mplew.getPacket();
    }
}
