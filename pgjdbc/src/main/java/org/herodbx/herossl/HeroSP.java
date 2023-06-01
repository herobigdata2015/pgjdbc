package org.herodbx.herossl;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import org.herodbx.herossl.jna.HeroCrypto;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author 顾法华
 * @since 2020/9/16
 */
public class HeroSP {
  static private final Logger logger = Logger.getLogger(HeroSP.class.getName());
  /**
   * 校验token是否有效
   *
   * @param jwt
   * @return
   */
  static public boolean verifyToken(String jwt) {
    return HeroCrypto.INSTANCE.db_VerifyToken(jwt, jwt.length());
  }

  static public String GetServerPinCode(String userid) {
    Memory mem = new Memory(16);
    int ret = HeroCrypto.INSTANCE.db_GetServerPinCode(userid, mem, 16);
    if (0 != ret) {
      String errmsg = HeroCrypto.INSTANCE.db_GetShErrMsg(ret);
      throw new RuntimeException("计算 " + userid + " 的密码出错：" + errmsg);
    } else {
      String pin = mem.getString(0);
      return pin;
    }
  }

  static public void SetPinCode(String pincode) {
    if(pincode==null){
      pincode = "";
    }
    int length = pincode.length();
    Memory mem = new Memory(length+1);
    mem.setString(0,pincode);
    int ret = HeroCrypto.INSTANCE.db_SetPinCode(mem, length);
    if (0 != ret) {
      String errmsg = HeroCrypto.INSTANCE.db_GetShErrMsg(ret);
      throw new RuntimeException("SetPinCode出错了：" + errmsg);
    }
  }

  static public void FreePinCode() {
    int ret = HeroCrypto.INSTANCE.db_FreePinCode();
    if (0 != ret) {
      String errmsg = HeroCrypto.INSTANCE.db_GetShErrMsg(ret);
      throw new RuntimeException("FreePinCode出错了：" + errmsg);
    }
  }

  /**
   * 这个函数将会更改证书的口令
   * @param userid
   */
  static public void LoadAutoClientEngine(String userid) {
    int ret = HeroCrypto.INSTANCE.db_LoadAutoClientEngine(userid);
    if (0 != ret) {
      String errmsg = HeroCrypto.INSTANCE.db_GetShErrMsg(ret);
      throw new RuntimeException("用户 " + userid + " 自动登录出错：" + errmsg);
    } else {
      logger.fine("自动加载 加密平台 客户端成功。");
    }
  }

  static public void LoadClientEngine(String userid,String userpin){
    int ret = HeroCrypto.INSTANCE.db_LoadClientEngine(userid, userpin);
    if (0 != ret) {
      String errMsg = HeroCrypto.INSTANCE.db_GetLastErrMsg();
      throw new RuntimeException("加载加密平台（userid=" + userid + "）出错：" + errMsg);
    } else {
      logger.finest("登录到加密平台成功。");
    }
  }

  static public String genRandomPrintable(int len) {
    return genRandomPrintable(len, null);
  }

  static public String genRandomPrintable(int len, int... x) {
    x = (null == x) ? new int[0] : x;
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < len; ) {
      int y = HeroSP.GenRandom(1)[0];
      if (y < 0x21 || y > 0x7e) {//0x21=31,0x7e=126，32是空格，127是delete，这中间都是可正常打印的字符。顾法华，2020年10月16日09:17:41
        continue;
      }
      char newChar = (char) y;
      boolean accept = true;
      for (int ix = 0; ix < x.length; ix++) {
        if (newChar == x[ix]) {
          logger.fine("排除了字符：" + newChar);
          accept = false;
          break;
        }
      }
      if (accept) {
        sb.append(newChar);
        i++;
      }
    }
    return sb.toString();
  }

  static public byte[] GenRandom(int len) {
    Pointer buf = new Memory(len);
    int ret = HeroCrypto.INSTANCE.db_GenRandom(len, buf);
    if (0 != ret) {
      String errmsg = HeroCrypto.INSTANCE.db_GetShErrMsg(ret);
      throw new RuntimeException("请求 " + len + " 字节随机数出错：" + errmsg);
    } else {
      return buf.getByteArray(0, len);
    }
  }

  static public byte[] CalcuteBlockHash(byte[] key, byte[] plain) {
    Memory out = new Memory(2049);
    IntByReference outlenIR = new IntByReference((int) out.size());
    int ret = HeroCrypto.INSTANCE.db_CalcuteBlockHash(null, key, key.length, plain, plain.length, out, outlenIR);
    if (0 != ret) {
      String errmsg = HeroCrypto.INSTANCE.db_GetShErrMsg(ret);
      throw new RuntimeException("杂凑计算出错：" + errmsg);
    } else {
      int outlen = outlenIR.getValue();
      return out.getByteArray(0, outlen);
    }
  }

  static public String SignChapCode(String userid,String chapcode) {
    Pointer memory = new Memory(1024);
    IntByReference intByReference = new IntByReference();
    logger.log(Level.FINEST,"请求数据签名："+userid+","+chapcode);
    int ret = HeroCrypto.INSTANCE.db_SignChapCode(chapcode, userid, memory, intByReference);
    if(0 == ret){
      byte[] byteArray = memory.getByteArray(0, intByReference.getValue());
      String chap = new String(byteArray);
      logger.log(Level.FINEST,"数据签名成功："+chap);
      return chap;
    }else {
      String errmsg = HeroCrypto.INSTANCE.db_GetShErrMsg(ret);
      throw new RuntimeException("给挑战码签名出错：" + errmsg);
    }
  }

  static public byte[] SignBufChapCode(String userid,byte[] chapcode) {
    Pointer memory = new Memory(1024);
    IntByReference intByReference = new IntByReference();
//    logger.log(Level.FINEST,"请求签名："+userid+",["+ HeroCommonTool.getPrintable(chapcode) +"]");
    int ret = HeroCrypto.INSTANCE.db_SignBufChapCode(chapcode,chapcode.length, userid, memory, intByReference);
    if(0 == ret){
      byte[] byteArray = memory.getByteArray(0, intByReference.getValue());
//      logger.log(Level.FINEST,"签名成功："+byteArray.length+"B,["+ HeroCommonTool.getPrintable(byteArray)+"]");
      return byteArray;
    }else {
      String errmsg = HeroCrypto.INSTANCE.db_GetShErrMsg(ret);
      throw new RuntimeException("签名出错：" + errmsg);
    }
  }

  static public boolean VerifyBufChapCode(byte[] chapcode, byte[] chapcodeSign, byte[] cert){
    return HeroCrypto.INSTANCE.db_VerifyBufChapCode(chapcode,chapcode.length,chapcodeSign,chapcodeSign.length,cert,cert.length);
  }

  static public String getUserSignCert(String userid, String userpin){
    Pointer outData = new Memory(8192);
    IntByReference outDataLen = new IntByReference();
    int ret = HeroCrypto.INSTANCE.db_ReadUserSignCert(userid,userpin, outData, outDataLen);
    if(ret == 0){
      byte[] byteArray = outData.getByteArray(0, outDataLen.getValue());
      return new String(byteArray).trim();
    }else {
      String errmsg = HeroCrypto.INSTANCE.db_GetShErrMsg(ret);
      throw new RuntimeException("读取用户的签名证书出错：" + errmsg);
    }
  }

  static public void Hero_db_UnloadAllEngine(){
    int ret = HeroCrypto.INSTANCE.db_UnloadAllEngine();
    if(0!=ret){
      String errmsg = HeroCrypto.INSTANCE.db_GetShErrMsg(ret);
      throw new RuntimeException("卸载安全平台出错：" + errmsg);
    }
  }

  /**
   * 通过该函数获取用户的Appkey不需要手动登录加密平台。
   * @param userid
   * @return
   * @since 2022年3月1日
   */
  static public String Hero_db_ReadUserAppkey(String userid) {
    Memory outData = new Memory(4096);
    IntByReference outDataLen = new IntByReference();
    int ret = HeroCrypto.INSTANCE.db_ReadUserAppkey(userid,outData,outDataLen);
    if(ret == 0){
      byte[] byteArray = outData.getByteArray(0, outDataLen.getValue());
      return new String(byteArray).trim();
    }else {
      String errmsg = HeroCrypto.INSTANCE.db_GetShErrMsg(ret);
      throw new RuntimeException("读取用户的Appkey出错：" + errmsg);
    }
  }

  /**
   * 通过该函数获取用户的MacCode不需要手动登录加密平台。
   * @param userid
   * @return
   * @since 2022年3月1日
   */
  static public String Hero_db_ReadUserMacCode(String userid) {
    Memory outData = new Memory(4096);
    IntByReference outDataLen = new IntByReference();
    int ret = HeroCrypto.INSTANCE.db_ReadUserMacCode(userid,outData,outDataLen);
    if(ret == 0){
      byte[] byteArray = outData.getByteArray(0, outDataLen.getValue());
      return new String(byteArray).trim();
    }else {
      String errmsg = HeroCrypto.INSTANCE.db_GetShErrMsg(ret);
      throw new RuntimeException("读取用户的Appkey出错：" + errmsg);
    }
  }
}
