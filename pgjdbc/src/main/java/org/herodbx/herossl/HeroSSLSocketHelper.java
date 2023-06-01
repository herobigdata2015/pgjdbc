package org.herodbx.herossl;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import org.herodbx.util.GetSocketFd;
import org.herodbsql.core.PGStream;
import org.herodbx.herossl.jna.HeroCrypto;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.SocketImpl;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author 顾法华
 * @since 2020年12月21日
 */
public class HeroSSLSocketHelper {
  private static final Logger LOGGER = Logger.getLogger(HeroSSLSocketHelper.class.getName());

  static public synchronized void switchToHeroSSLSocket(PGStream stream, String user) throws IOException {
    //default Pincode
    String PIN = "12345678";
    Pointer memory = new Memory(32);
    int size = 32;

    //pincode use to connect ukey and other cipher device
    //heroproxy has windows to input pincode(save to shmem,use db_GetPinCode to read it)
    int ret = HeroCrypto.INSTANCE.db_GetPinCode(memory, size);
    if (ret == 0) {
      PIN = memory.getString(0);
      LOGGER.finest("取得已登录用户的PIN码成功");
    } else {
      LOGGER.severe("读取已登录用户的PIN码出错了");
      throw new RuntimeException("读取已登录用户的PIN码出错了");
    }

    //get socket fd
    Socket socket = stream.getSocket();
    int getfd = new GetSocketFd().getSocketFd(socket);//getFD(socket);
    LOGGER.finest("socket的fd：" + getfd);
    if( getfd<=0){
      LOGGER.warning("socket的fd有问题：" + getfd);
    }
//    LOGGER.finest("pin: \""+PIN+"\"");
    LOGGER.finest("pin有值？ " + (PIN.length() > 0));
    LOGGER.finest("user: \"" + user + "\"");
    if (null == PIN || PIN.isEmpty()) {
      LOGGER.severe("PIN码为空！");
    }
    if (null == user || user.isEmpty()) {
      LOGGER.severe("user为空！");
    }

    //create herosslsocket and use it
    HeroSSLSocket newConnection = new HeroSSLSocket(getfd, PIN, user);
    LOGGER.finest("newConnection:" + newConnection);
    //change orginal connect to herosslsocket, switch to ssl connection
    stream.changeSocket(newConnection);
  }

  @Deprecated
  public static int getFD_Deprecated(Socket ss) {
    //因为通过反射获得Java对象的私有域的值需要特殊权限，所以本函数使用受限，不再推荐。已改成jni方案。顾法华，2021年3月18日

    LOGGER.finest("准备获取socket " + ss.toString() + " 的fd");
    try {
      Class<? extends Socket> aClass = ss.getClass();
      LOGGER.finest("aClass:" + aClass.getCanonicalName());
      Field $impl = aClass.getDeclaredField("impl");
      LOGGER.finest("$impl:" + $impl.getClass().getCanonicalName() + "," + $impl.toString());
      $impl.setAccessible(true);
      Object $implObject = $impl.get(ss);
      LOGGER.finest("$implObject：" + $implObject.getClass().getCanonicalName() + "," + $implObject);

      if ($implObject instanceof SocketImpl) {
        LOGGER.finest("可以转换");
      } else {
        LOGGER.log(Level.SEVERE, "socket对象 类型不兼容，不可以转换！");
        return 0;
      }
      SocketImpl socketImpl = (SocketImpl) $implObject;
      Method $getFileDescriptor = SocketImpl.class.getDeclaredMethod("getFileDescriptor");
      $getFileDescriptor.setAccessible(true);
      Object invoke = $getFileDescriptor.invoke(socketImpl);
      LOGGER.finest("invoke返回值：" + invoke);

      FileDescriptor fd = (FileDescriptor) invoke;
      Field $fd = fd.getClass().getDeclaredField("fd");
      $fd.setAccessible(true);
      return (Integer) $fd.get(fd);
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "获取socket文件句柄出错：" + e.getMessage(), e);
      return 0;
    } finally {
      LOGGER.finest("从方法 getFD 返回");
    }
  }
}
