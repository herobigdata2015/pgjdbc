package org.herodbx.herossl;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

public class HeroInputStream extends InputStream {


  // 我们用来实现skip()的静态虚拟数组
  private final static byte[] SKIP_ARRAY = new byte[1024];

  private HeroSSLSocket c;

  // 一个用于实现单字节read()方法的元素数组
  private final byte[] oneByte = new byte[1];

  HeroInputStream(HeroSSLSocket conn) {
    c = conn;
  }



  /**
   * 读取单个字节，在非故障EOF状态下返回-1。
   */
  @Override
  public synchronized int read() throws IOException {
    int n = read(oneByte, 0, 1);
    if (n <= 0) { // EOF
      return -1;
    }
    return oneByte[0] & 0xff;
  }

  /**
   * Reads into a byte array <i>b</i> at offset <i>off</i>,
   * <i>length</i> bytes of data.
   *
   * @param b the buffer into which the data is read
   * @param off the start offset of the data
   * @param len the maximum number of bytes read
   * @return the actual number of bytes read, -1 is
   *          returned when the end of the stream is reached.
   * @exception IOException If an I/O error has occurred.
   */
  @Override
  public synchronized int read(byte b[], int off, int len)
    throws IOException {
    if (b == null) {
      throw new NullPointerException();
    } else if (off < 0 || len < 0 || len > b.length - off) {
      throw new IndexOutOfBoundsException();
    } else if (len == 0) {
      return 0;
    }

    // 检查套接字是否无效(错误或关闭)
    if(c == null || !c.HeroSSL_Status()){
      throw new SocketException("SSLSocket handle is empty!");
    }
    try {
      return c.HeroSSL_Read(b, off, len);
    } catch (Exception e) {
      // 关闭并重新抛出(包装)异常
        c.close();
      // dummy for compiler
      return -1;
    }
  }


  /**
   * 跳过n个字节。
   * 这个实现的效率比可能的要低一些，
   * 但也不是很差(冗余副本)。
   * 我们重用read()代码以使事情更简单。
   * 注意，SKIP_ARRAY是静态的，
   * 可能会被并发使用打乱，
   * 但是我们对数据不感兴趣。
   */
  @Override
  public synchronized long skip(long n) throws IOException {
    long skipped = 0;
    while (n > 0) {
      int len = (int)Math.min(n, SKIP_ARRAY.length);
      int r = read(SKIP_ARRAY, 0, len);
      if (r <= 0) {
        break;
      }
      n -= r;
      skipped += r;
    }
    return skipped;
  }

  /*
   * 套接字关闭已经同步，不需要在这里阻塞。
   */
  @Override
  public void close() throws IOException {
    c.close();
  }

  // inherit default mark/reset behavior (throw Exceptions) from InputStream
}
