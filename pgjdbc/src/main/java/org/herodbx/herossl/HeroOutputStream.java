package org.herodbx.herossl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

public class HeroOutputStream extends OutputStream {

  //SSL句柄
  private HeroSSLSocket heroSSLSocket;

  // 一个用于实现write(byte)方法的元素数组
  private final byte[] oneByte = new byte[1];

  // 创建
  HeroOutputStream(HeroSSLSocket heroSSLSocket) {
    this.heroSSLSocket = heroSSLSocket;
  }
  @Override
  synchronized public void write(int b) throws IOException {
    oneByte[0] = (byte)b;
    write(oneByte, 0, 1);
  }

  public void flush() throws IOException {

  }
  /**
   * 此方法调用herosslsocketimpl来处理
   *
   * @param      b     数据
   * @param      off   数据中的起始偏移量。
   * @param      len   要写入的字节数。
   * @exception  IOException 如果发生I/O错误。
   * 特别是，如果输出流关闭，
   * 则抛出<code>IOException</code>。
   */
  @Override
  synchronized public void write(byte b[], int off, int len)
    throws IOException {
    if (b == null) {
      throw new NullPointerException();
    } else if (off < 0 || len < 0 || len > b.length - off) {
      throw new IndexOutOfBoundsException();
    } else if (len == 0) {
      return;
    }

    // 检查套接字是否无效(错误或关闭)
    if(heroSSLSocket == null){
      throw new SocketException("SSLSocket handle is empty!");
    }
    // 把数据发送到后端
    try {
      heroSSLSocket.HeroSSL_Write(b, off, len);
    } catch (Exception e) {
      close();
      throw e;
    }
  }

  /*
   * 套接字关闭已经同步，不需要在这里阻塞。
   */
  @Override
  public void close() throws IOException {
    heroSSLSocket.close();
  }

}
