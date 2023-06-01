package org.herodbx.herossl;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import org.herodbx.herossl.jna.HeroSSLAPI;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/*实现socket中的方法,*/
public class HeroSSLSocket extends Socket {

  private static final Logger LOGGER = Logger.getLogger(HeroSSLSocket.class.getName());
  //SSL句柄
  private PointerByReference handle;

  // 关闭状态
  private boolean closeState = true;

  // 输入流
  private HeroInputStream in;

  //输出流
  private HeroOutputStream out;

  public HeroSSLSocket(int socketFd, String PIN, String user) {
      //调用c的ssl协议进行socket初始化
    PointerByReference handle = new PointerByReference(Pointer.NULL);

    LOGGER.finest("准备调用c进行ssl握手");

    int ret;

    //经测试，先load加密平台，后init ssl，不采用同步保护，是有问题的，所以还是采用同步保护的方案。顾法华，2020年6月1日 22点18分
//    ret = HeroCrypto.INSTANCE.db_LoadClientEngine(user,PIN);
//    if(0==ret){
//      LOGGER.finest("load加密平台成功");
//    }else{
//      LOGGER.log(Level.SEVERE,"load加密平台失败，错误号："+ret);
//      getErrMsg(ret);
//    }

    // 调用c进行ssl的握手
    //如果这里不同步，还是有较大概率把jvm搞死，所以以功能和稳定性优先。顾法华，2020年6月2日
    synchronized (HeroSSLSocket.class){
      LOGGER.log(Level.FINEST,"有同步的，socketFd="+socketFd);
      ret = HeroSSLAPI.INSTANCE.HeroSSL_Client_Init(socketFd, user, PIN, handle);
    }
    if(ret == 0){
      this.handle = handle;
      LOGGER.finest("新的socket fd为："+handle.getValue());
    }else {
      LOGGER.log(Level.SEVERE,"调用c进行ssl握手失败，错误号："+ret);
      getErrMsg(ret);
    }

    // 初始化socket 输入输出流
    in = new HeroInputStream(this);
    out = new HeroOutputStream(this);
//Java程序这边不需要考虑 free ssl context，所以不需要这个hook。顾法华，2020年6月5日 14点15分
//    Runtime.getRuntime().addShutdownHook(new Thread(()->{
//      close_or_free(true);
//    }));
  }

  //调用c的方法,把数据写入
  public void HeroSSL_Write(byte b[], int off, int len) throws IOException {
    if(handle == null){
      throw new SocketException("SSLSocket handle is empty!");
    }
    int ret = HeroSSLAPI.INSTANCE.HeroSSL_Write(handle.getValue(), b, off, len);
    if(ret == 0){// 发送成功!
      LOGGER.log(Level.SEVERE,"socket句柄已关闭！");
    } else if (-1==ret) {// 发送失败!
      getErrMsg(ret);
    } else {
      LOGGER.finest("发送字节数："+ret);
    }
  }

  //调用c的方法,读取数据

  // wrap native call to allow instrumentation
  /**
   * Reads into an array of bytes at the specified offset using
   * the received socket primitive.
   * @param b the buffer into which the data is read
   * @param len the maximum number of bytes read
   * @return the actual number of bytes read, -1 is
   *          returned when the end of the stream is reached.
   * @exception IOException If an I/O error has occurred.
   */
  public int HeroSSL_Read(byte b[], int off, int len) throws IOException {
    if(handle == null){
      throw new SocketException("SSLSocket handle is empty!");
    }

    int ret = HeroSSLAPI.INSTANCE.HeroSSL_Read(handle.getValue(), b, off, len);
    if(ret > 0){// 读取成功!
      return ret;
    }
    else {
      // 读取失败!
      getErrMsg(ret);
    }
    return -1;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    HeroSSL_Status();
    return in;
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    HeroSSL_Status();
    return out;
  }

  /**
   *关闭当前的socket
   */
  public void close(){
    close_or_free(false);
  }

  public void close_or_free(boolean free) {
    if(closeState){
      LOGGER.finest("调用c代码关闭socket");
      int ret;
      if(free){
        LOGGER.finest("jvm将关闭");
        ret = HeroSSLAPI.INSTANCE.HeroSSL_Free(handle.getValue());
      }else{
        LOGGER.finest("jvm不关闭");
        ret = HeroSSLAPI.INSTANCE.HeroSSL_Close(handle.getValue());
      }
      if(ret == 0){
        closeState = false;
        LOGGER.info("close SSLSocket success!");
      }else {
        try {
          throw new SocketException("SSLSocket closed Failed! ret code:"+ret);
        } catch (SocketException e) {
          LOGGER.log(Level.SEVERE,"关闭socket出错："+e.getMessage(),e);
        }
      }
    }else{
      LOGGER.finest("调用socket close，已经执行过！");
    }
  }
  /**
   *查看socket的状态
   */
  public boolean HeroSSL_Status() {
    return true;
  }

  /**
   *根据错误码获取错误详情
   *
   * @param errno 错误编号
   * @return 0 on success
   */
  public static void getErrMsg(int errno){
    String s = null;
    LOGGER.severe("错误代码为: " + errno);
    s = HeroSSLAPI.INSTANCE.HeroSSL_GetErrMsg(errno);
    LOGGER.warning("An error occurred while obtaining the error details!");
    try {
      throw new SocketException(s);
    } catch (SocketException e) {
//      e.printStackTrace();
      LOGGER.log(Level.SEVERE,"错误代码为："+errno+"，错误信息："+s,e);
    }
  }

  public PointerByReference getHandle() {
    return handle;
  }
}
