package org.herodbx.core.v3;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import org.herodbx.herossl.HeroSP;
import org.herodbx.HeroCommonTool;
import org.herodbsql.PGProperty;
import org.herodbsql.core.PGStream;
import org.herodbx.herossl.jna.HeroCrypto;
import org.herodbsql.util.GT;
import org.herodbx.util.HeroUtils;
import org.herodbsql.util.PSQLException;
import org.herodbsql.util.PSQLState;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author 顾法华
 * @since 2021年1月11日
 */
final public class ConnectionFactoryImplHelper {
  private static final Logger LOGGER = Logger.getLogger(ConnectionFactoryImplHelper.class.getName());

  static public void doWithMac(PGStream pgStream) throws PSQLException, IOException {
    LOGGER.log(Level.FINEST, "AuthenticationMAC");
    // 获取mac地址
    String macAddress = HeroUtils.getMacAddress();
    if (macAddress == null) {
      throw new PSQLException(
        GT.tr(
          "The server requested mac-based authentication, but no mac was provided."),
        PSQLState.CONNECTION_REJECTED);
    }
    byte[] encodedMAC = macAddress.getBytes("UTF-8");
    pgStream.sendChar('p');
    pgStream.sendInteger4(4 + encodedMAC.length + 1);
    pgStream.send(encodedMAC);
    pgStream.sendChar(0);
    pgStream.flush();
  }

  static public void doWithWatermark(PGStream pgStream) throws IOException {
    LOGGER.log(Level.FINEST, "AuthenticationWatermark");
    // 获取编码之后的软件水印
    byte[] byteArray = HeroUtils.getWaterMark();
    pgStream.sendChar('p');
    pgStream.sendInteger4(4 + byteArray.length + 1);
    pgStream.send(byteArray);
    pgStream.sendChar(0);
    pgStream.flush();
  }

  static public void doWithAppkey(PGStream pgStream,String userid) throws IOException {
    LOGGER.log(Level.FINEST, "AuthenticationAppkey,user=["+userid+"]");
    // 获取appkey
    String appkey = HeroSP.Hero_db_ReadUserAppkey(userid);
//    LOGGER.log(Level.FINEST,"appkey=["+appkey+"]");
    byte[] byteArray = appkey.getBytes();
    pgStream.sendChar('p');
    pgStream.sendInteger4(4 + byteArray.length + 1);
    pgStream.send(byteArray);
    pgStream.sendChar(0);
    pgStream.flush();
  }

  static public void doWithMacCode(PGStream pgStream,String userid) throws IOException {
    LOGGER.log(Level.FINEST, "AuthenticationMacCode,user=["+userid+"]");
    // 获取maccode
    String maccode = HeroSP.Hero_db_ReadUserMacCode(userid);
//    LOGGER.log(Level.FINEST,"maccode=["+maccode+"]");
    byte[] byteArray = maccode.getBytes();
    pgStream.sendChar('p');
    pgStream.sendInteger4(4 + byteArray.length + 1);
    pgStream.send(byteArray);
    pgStream.sendChar(0);
    pgStream.flush();
  }

  /**
   * 返回0时需要continue，返回1时需要break。
   *
   * @param pgStream
   * @return
   */
  static public int doWithChallenges(PGStream pgStream, Properties info, int msgLen, String user) throws IOException, PSQLException {
    LOGGER.log(Level.FINEST, "AuthenticationChallenges");
    // 判断是否延迟认证
    String certDelay = PGProperty.CERTDELAY.get(info);
//                if (StringUtils.isEmpty(certDelay)) {
//                  throw new PSQLException(
//                    GT.tr(
//                      "The certDelay property not found!"),
//                    PSQLState.CONNECTION_REJECTED);
//                }
    if ("on".equalsIgnoreCase(certDelay)) {
      LOGGER.finest("获取到的certdelay: on");
//      continue;
//      return 0;//对应continue
    } else {
      LOGGER.finest("获取到的certdelay: off");
    }

    // 接收数据库发来的挑战码
    int len = msgLen - 4 - 4;//数据包总长度-4字节的数据包长度信息-4字节的验证类型信息，剩下是挑战码。顾法华，2020年5月29日 18点14分
    LOGGER.finest("挑战码长度：" + len);
    byte[] rbText1 = pgStream.receive(len);
    LOGGER.finest("rbText1="+ HeroCommonTool.bytes2HexString(rbText1));
    LOGGER.finest("rbText1=["+ HeroCommonTool.getPrintable(rbText1)+"]");

    if ("on".equalsIgnoreCase(certDelay)) {
      LOGGER.finest("获取到的certdelay: on");
//      continue;
      return 0;//对应continue
    } else {
      LOGGER.finest("获取到的certdelay: off");
    }

    byte[] rb = HeroCommonTool.subByte(rbText1,0,32);
    String text1 = new String(HeroCommonTool.subByte(rbText1,32,6));
//    LOGGER.finest("rb="+ HeroCommonTool.bytes2HexString(rb));
//    LOGGER.finest("rb=["+ HeroCommonTool.getPrintable(rb)+"]");
//    LOGGER.finest("text1=["+text1+"]");

    String userid = PGProperty.USER.get(info);
//    LOGGER.finest("取得需要连接的user：\"" + user + "\",userid=\""+userid+"\"");

    Pointer m = new Memory(64);
    int ret = HeroCrypto.INSTANCE.db_GetPinCode(m, 64);
    if (0 != ret) {
      String retMsg = HeroCrypto.INSTANCE.db_GetShErrMsg(ret);
      LOGGER.severe("获取PIN码出错：" + retMsg);
    }
    String pin = m.getString(0);
//                LOGGER.finest("取得PIN: \""+pin+"\"");

    //尝试登录到加密平台
    ret = HeroCrypto.INSTANCE.db_LoadClientEngine(userid, pin);
    if (0 != ret) {
      String retMsg = HeroCrypto.INSTANCE.db_GetShErrMsg(ret);
      LOGGER.severe("登录到加密平台出错：" + retMsg);

      throw new PSQLException(
        GT.tr(
          "登录到加密平台失败:" + retMsg),
        PSQLState.CONNECTION_REJECTED);
    } else {
      LOGGER.finest("登录到加密平台成功。");
    }

    byte[] ra = HeroSP.GenRandom(32);
//    LOGGER.finest("Ra="+ HeroCommonTool.bytes2HexString(ra));
//    LOGGER.finest("Ra=["+ HeroCommonTool.getPrintable(ra)+"]");
    String text2 = "herodb";
    String text3 = "client";

    byte[] RaRb = HeroCommonTool.byteMerger(ra, rb);
//    LOGGER.finest("RaRb.len="+RaRb.length+",RaRb=["+ HeroCommonTool.getPrintable(RaRb)+"]");
    byte[] RaRbText2 = HeroCommonTool.byteMerger(RaRb,text2.getBytes());
//    LOGGER.finest("RaRbText2="+RaRbText2.length+"B,["+ HeroCommonTool.getPrintable(RaRbText2)+"]");
    byte[] signedRaRbText2 = HeroSP.SignBufChapCode(user,RaRbText2);
//    LOGGER.finest("signedRaRbText2=["+ HeroCommonTool.getPrintable(signedRaRbText2)+"]");
//    LOGGER.finest("signedRaRbText2=["+ HeroCommonTool.bytes2HexString(signedRaRbText2)+"]");

    byte[] RaRbText3 = HeroCommonTool.byteMerger(RaRb,text3.getBytes());
//    LOGGER.finest("RaRbText3=["+ HeroCommonTool.getPrintable(RaRbText3)+"]");
    byte[] reponse = HeroCommonTool.byteMerger(RaRbText3,signedRaRbText2);
//    LOGGER.finest("reponse.len="+reponse.length+",reponse=["+ HeroCommonTool.getPrintable(reponse)+"]");

//    boolean b = HeroSP.VerifyBufChapCode(RaRbText2,HeroCommonTool.subByte(reponse,70,signedRaRbText2.length),HeroSP.getUserSignCert(user,pin).getBytes());
//    LOGGER.finest("本地验签结果："+b);

    pgStream.sendChar('p');
//    pgStream.sendInteger4(4 + reponse.length + 1);
    pgStream.sendInteger4(4 + reponse.length);
    pgStream.send(reponse);
//    pgStream.sendChar(0);//经过与服务器一侧的数据比对，新协议不再附加这个字节。顾法华，2021年1月26日 17点33分
    pgStream.flush();

    return 1;//对应break
  }

}


