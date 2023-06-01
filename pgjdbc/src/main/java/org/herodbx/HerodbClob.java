package org.herodbx;

import java.io.*;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * 这是仅用于jdbc客户端的clob对象，仅用于上传文件用。
 *
 * @author 顾法华
 */
public class HerodbClob extends CharArrayWriter implements NClob, Clob {

  private static final Logger LOGGER = Logger.getLogger(HerodbClob.class.getName());

  @Override
  public long length() throws SQLException {
    return this.size();
  }

  @Override
  public String getSubString(long pos, int length) throws SQLException {
    pos = pos - 1;

    if ((pos + 1) > buf.length) {
      throw new SQLException("超出数组长度");
    }

    int len = (pos + length) <= buf.length ? length : (buf.length - (int) pos);
    if (len == 0) {
      return "";
    }

    char[] result = new char[len];
    System.arraycopy(buf, (int) pos, result, 0, len);
    return new String(result);
  }

  @Override
  public Reader getCharacterStream() throws SQLException {
    return new CharArrayReader(this.toCharArray());
  }

  @Override
  public InputStream getAsciiStream() throws SQLException {
    return new ByteArrayInputStream(this.toString().getBytes());
  }

  @Override
  public long position(String searchstr, long start) throws SQLException {
    return this.toString().indexOf(searchstr, (int) start);
  }

  @Override
  public long position(Clob searchstr, long start) throws SQLException {
    throw org.herodbsql.Driver.notImplemented(this.getClass(), "position()");
//    return 0;
  }

  @Override
  public int setString(long pos, String str) throws SQLException {
    pos = pos - 1;

    char[] t = str.toCharArray();

    char[] tmp = this.toCharArray();
    if ((pos + t.length) > tmp.length) {
      char[] tmp2 = new char[(int) pos + t.length];
      System.arraycopy(tmp, 0, tmp2, 0, tmp.length);
      tmp = tmp2;
    }

    System.arraycopy(t, 0, tmp, (int) pos, t.length);

    this.reset();
    try {
      this.write(tmp);
    } catch (IOException e) {
      throw new SQLException(e);
    }
    return t.length;
  }

  @Override
  public int setString(long pos, String str, int offset, int len) throws SQLException {
    char[] bytes = str.toCharArray();

    len = Math.min((bytes.length - offset), len);
    byte[] tmp = new byte[len];
    System.arraycopy(bytes, offset, tmp, 0, len);
    return this.setString(pos, new String(tmp));
  }

  @Override
  public OutputStream setAsciiStream(long pos) throws SQLException {
    final HerodbClob hc = this;
    return new OutputStream() {
      long pos2 = pos;
      ByteArrayOutputStream tt = new ByteArrayOutputStream();

      @Override
      public void write(int b) throws IOException {
        tt.write(b);
      }

      @Override
      public void flush() {
        try {
          tt.flush();
          String str = tt.toString();
          tt.reset();

          hc.setString(pos2, str);
          pos2 += str.length();
        } catch (IOException | SQLException e) {
          e.printStackTrace();
        }
      }
    };
  }

  @Override
  public Writer setCharacterStream(long pos) throws SQLException {
    this.setString(pos, "");
    return this;
  }

  @Override
  public void truncate(long len) throws SQLException {
    char[] tmp = this.toCharArray();
    if (tmp.length > len) {
      char[] tmp2 = new char[(int) len];
      System.arraycopy(tmp, 0, tmp2, 0, (int) len);
      this.buf = tmp2;
      this.count = tmp2.length;
    }
  }

  @Override
  public void free() throws SQLException {
  }

  @Override
  public Reader getCharacterStream(long pos, long length) throws SQLException {
    String tmp = getSubString(pos, (int) length);
    return new CharArrayReader(tmp.toCharArray());
  }
}
