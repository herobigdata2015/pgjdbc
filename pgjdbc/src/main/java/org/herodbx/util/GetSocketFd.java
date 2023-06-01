package org.herodbx.util;

import com.sun.jna.Platform;

import java.net.Socket;

/**
 * @author 顾法华
 * @since 2021年3月18日
 */
public class GetSocketFd {
  static {
    if(Platform.isWindows()){
      System.loadLibrary("libherojni");
    }else{
      System.setProperty("java.library.path","/opt/herolib/:"+System.getProperty("java.library.path"));
      System.loadLibrary("herojni");
    }
  }
  public native int getSocketFd(Socket socket);
}
