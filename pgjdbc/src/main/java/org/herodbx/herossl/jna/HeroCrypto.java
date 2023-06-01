package org.herodbx.herossl.jna;

import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;

/**
 * DLL动态库调用接口
 * @Description: 读取调用CDecl方式导出的DLL动态库方法
 * @author: qiaowy
 * @date: 2019年10月22日
 */
public interface HeroCrypto extends Library {
  // DLL文件默认路径为项目根目录，若DLL文件存放在项目外，
  // 请使用绝对路径。（此处：(Platform.isWindows()?"msvcrt":"c")指本地动态库msvcrt.dll）
  HeroCrypto INSTANCE = (HeroCrypto) Native.loadLibrary(Platform.isWindows() ? "libcrypto-1_1-x64" : "/opt/herolib/libcrypto-hero.so.1.1", HeroCrypto.class);

  /**
   * PIN码验证
   *
   * @param userid 用户id
   * @param userpin 设备的PIN码
   * @return 0 on success
   */
  int db_LoadClientEngine(String userid, String userpin);

//  int db_SetPinCode(char *pincode, int len);
  int db_SetPinCode(Pointer pincode, int len);

  /**
   *读取pin码 (从共享内存中读取PIN码, 在HeroProxy认证之后共享内存中才有PIN码,否则取不到PIN码)
   *
   * @param pincode ukey的pin码
   * @param len pin码长度
   * @return 0 on success
   */
  int db_GetPinCode(Pointer pincode, int len);

//  int db_FreePinCode();
  int db_FreePinCode();

  /**
   * 计算文件的杂凑值d
   *
   * @param filePath 文件路径
   * @param key 杂凑密钥
   * @param keylen 杂凑密钥长度
   * @param hashvalue 杂凑值字符串
   * @return 0 on success
   */
  int db_CalculateFileHash(String filePath, String key, int keylen, Pointer hashvalue);

  /**
   * 客户端编码
   *
   * @param in 输入缓冲区
   * @param inlen 输入缓存区的长度
   * @param newline 是否换行
   * @param out 输出缓存区
   * @param outlen 输出缓存区的长度
   * @return 0 on success
   */
  int db_Base64Encode(Pointer in, int inlen, boolean newline, Pointer out, IntByReference outlen);

  /**
   * 客户端用于挑战码签名
   *
   * @param chapcode 挑战码（base64编码）
   * @param userid 用户标识
   * @param chapsigncode 挑战码签名值（base64编码）
   * @param outlen 输出缓存区的长度
   * @return 0 on success
   */
//  int db_SignChapCode(const char *chapcode,char* userid,char* chapsigncode,int* outlen);
  int db_SignChapCode(String chapcode, String userid, Pointer chapsigncode, IntByReference outlen);

  /**
   * @Name: db_SignBufChapCode
   * @Description: 客户端挑战码签名
   * @Input:
   *  {chapcode:挑战码},
   *  {inlen:chapcode指向内存大小},
   *  {userid：用户标识}
   * @Output:
   *  {chapsigncode:挑战码签名值}
   *  {outlen：挑战码签名值的长度}
   * @Return: {0: 成功}，{非0: 错误码}
   * @Others:
   */
//  int db_SignBufChapCode(char *chapcode, int inlen, char *userid, char *chapsigncode, int *outlen);
  int db_SignBufChapCode(byte[] chapcode, int inlen, String userid, Pointer chapsigncode, IntByReference outlen);

  /**
   * @Name: db_VerifyChapCode
   * @Description: 挑战码验证
   * @Input:
   *  {chapcode:明态挑战码},
   *  {inlen:明态挑战码长度},
   *  {chapsigncode:客户端挑战码签名},
   *  {inlen: chapsigncode指向内存大小},
   *  {signcert:客户端签名证书}
   *  {certlen: 客户端签名证书长度}
   * @Output:
   * @Return: {true: 验签成功}，{false: 验签失败}
   * @Others:
   */
//  bool db_VerifyBufChapCode(char *chapcode, int inlen, char *chapsigncode, int sign_len, char *signcert, int certlen);
  boolean db_VerifyBufChapCode(byte[] chapcode, int inlen, byte[] chapsigncode, int sign_len, byte[] signcert, int certlen);

  /**
   * 获取错误信息
   *
   * @param ErrNo 错误码
   * @return 返回错误代码的描述
   */
  String db_GetShErrMsg(int ErrNo);

  /**
   * 获取错误信息
   *
   * @return 返回错误代码的描述
   */
  String db_GetLastErrMsg();

  /**
   * @Name: db_GetLastErrStackMsg
   * @Description: 获取错误号对应的错误信息
   * @Input:
   * {错误号}
   * @Output:
   * @Return: {错误描述}
   * @Others:
   */
//  char* db_GetLastErrStackMsg();
  String db_GetLastErrStackMsg();

  int db_EngineInit(String nullString);

  /**
   * 参数：
   *   content:令牌
   *   len：令牌长度
   *   返回值：
   *     0：成功
   *   非0：错误码
   * @param len
   * @return
   */
//  bool db_VerifyToken(unsigned char* token,int len)
  boolean db_VerifyToken(String token, int len);

  /**
   * 参数：
   * seed：种子，默认为0到F
   * buf：[OUT]缓冲器
   * len：长度
   * 返回值：
   * 0：成功
   * 非0：错误号
   *
   * @param len
   * @return
   */
//  int db_GetServerPinCode(char *seed,char* buf,int len)
  int db_GetServerPinCode(String seed, Pointer buf, int len);

  int db_LoadAutoClientEngine(String userid);

  /**
   * 从加密平台获得随机数
   *
   * @param len 随机数的字节数
   * @param out 随机数的输出缓冲区
   * @return 0表示成功，非0表示错误码
   */
//  int db_GenRandom(int len,char* out)
  int db_GenRandom(int len, Pointer out);

  /**
   * 调用加密平台的杂凑算法
   *
   * @param algname 算法名，主要支持 sm3（默认），sha256，shar512
   * @param key     杂凑密钥
   * @param keylen  杂凑密钥的长度
   * @param in      输入缓冲区
   * @param inlen   输入缓冲区的长度
   * @param out     输出缓冲区
   * @param outlen  输出缓冲区的长度（调用前是缓冲区的大小，调用后是结果的字节数）
   * @return 0成功，非0是错误码
   */
//  int db_CalcuteBlockHash(char*algname, char*key, int keylen, char* in,int inlen,char* out,int *outlen)
  int db_CalcuteBlockHash(String algname, byte[] key, int keylen, byte[] in, int inlen, Memory out, IntByReference outlen);

  /**
   * 读取用户证书
   *
   * @param userid 用户的id
   * @param userpin 用户pin
   * @param usercert 用户签名证书缓存，格式为PEM
   * @param len 返回证书字节长度
   * @return 0 on success
   */
  int db_ReadUserSignCert(String userid,String userpin, Pointer usercert, IntByReference len);

  /**
   * 关闭UKey
   *
   * @return 0 on success
   */
  int db_UnloadAllEngine();

//  int db_ReadUserAppkey(char *userid, char *appkey, int *len);
  int db_ReadUserAppkey(String userid, Memory appkey, IntByReference len);

//  int db_ReadUserMacCode(char *userid, char *maccode, int *len);
  int db_ReadUserMacCode(String userid, Memory maccode, IntByReference len);
}
