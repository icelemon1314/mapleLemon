package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class RegisterAccountRecvVO extends MaplePacketRecvVO {

    String accountName;
    String password;
    String realName;
    String birthDay;
    String homeNo;
    String questionOne;
    String answerOne;
    String questionTwo;
    String answerTwo;
    String email;
    String IDCard;
    String telNo;
    Byte sex;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        accountName = slea.readMapleAsciiString();
        password = slea.readMapleAsciiString();
        realName = slea.readMapleAsciiString();
        birthDay = slea.readMapleAsciiString();
        homeNo = slea.readMapleAsciiString();
        questionOne = slea.readMapleAsciiString();
        answerOne = slea.readMapleAsciiString();
        questionTwo = slea.readMapleAsciiString();
        answerTwo = slea.readMapleAsciiString();
        email = slea.readMapleAsciiString();
        IDCard = slea.readMapleAsciiString();
        telNo = slea.readMapleAsciiString();
        sex = slea.readByte();
    }

    public String getAccountName() {
        return accountName;
    }

    public String getPassword() {
        return password;
    }

    public String getRealName() {
        return realName;
    }

    public String getBirthDay() {
        return birthDay;
    }

    public String getHomeNo() {
        return homeNo;
    }

    public String getQuestionOne() {
        return questionOne;
    }

    public String getAnswerOne() {
        return answerOne;
    }

    public String getQuestionTwo() {
        return questionTwo;
    }

    public String getAnswerTwo() {
        return answerTwo;
    }

    public String getEmail() {
        return email;
    }

    public String getIDCard() {
        return IDCard;
    }

    public String getTelNo() {
        return telNo;
    }

    public Byte getSex() {
        return sex;
    }
}
