package org.herodbx.herossl.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;


/**
 * DLL动态库调用接口
 * @Description: 读取调用CDecl方式导出的DLL动态库方法
 * @author: qiaowy
 * @date: 2019年10月22日 2020/3/25 modified by xia
 */
public interface HeroSSLAPI extends Library {
  // DLL文件默认路径为项目根目录，若DLL文件存放在项目外，
  // 请使用绝对路径。（此处：(Platform.isWindows()?"msvcrt":"c")指本地动态库msvcrt.dll）
  //HeroSSLAPI INSTANCE = (HeroSSLAPI) Native.loadLibrary((Platform.isWindows() ? "D:\\idea_workspace\\pgjdbc\\libherossl.dll" : "linuxLibSSL"),HeroSSLAPI.class);
  HeroSSLAPI INSTANCE = (HeroSSLAPI) Native.loadLibrary((Platform.isWindows() ? "libssl-1_1-x64" : "/opt/herolib/libssl-hero.so.1.1"), HeroSSLAPI.class);

  /**
   * 此方法主要调用c语言的SSL协议
   * (c实现了调用GMSSL SKF 硬件密码设备功能来进行TLS握手)
   * 进行初始化,与服务端协商一个socket的句柄返回给java
   *
   * @param socket socket文件描述符
   * @param user 登录的用户名称
   * @param accesscode 加密设备的PIN码
   * @param handle SSL句柄
   * @return 0 on success
   */
  int  HeroSSL_Client_Init(int socket, String user, String accesscode, PointerByReference handle);

  /**
   * 调用c读取数据
   *
   * @param handle SSL句柄
   * @param buf 读取数据缓冲区
   * @param off 读取数据写入缓冲区的起始位置
   * @param len 读取数据长度
   * @return >0 读取长度,<=0 表示错误(例如网络reset)
   */
  int HeroSSL_Read(Pointer handle, byte[] buf, int off, int len);

  /**
   * 调用c写入数据
   *
   * @param handle SSL句柄
   * @param buf 待写入缓冲区
   * @param off 缓冲区写入的起始位置
   * @param len 数据长度
   * @return 0 on success
   */
  int HeroSSL_Write(Pointer handle, byte[] buf, int off, int len);

  /**
   * 仅关闭SSL，不释放SSL的上下文，适用于多线程的环境
   *
   * @param handle SSL句柄
   * @return 0 on success
   */
  int HeroSSL_Close(Pointer handle);

  /**
   * 释放ssl的连接
   *
   * @param handle SSL句柄
   * @return 0 on success
   */
  int HeroSSL_Free(Pointer handle);

  /**
   * 根据错误号获取相应的连接
   *
   * @param ErrNo 错误代码
   * @return 返回错误代码的描述
   */
  String  HeroSSL_GetErrMsg(int ErrNo);


}
