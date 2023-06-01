package org.herodbx;

import java.io.*;
import java.sql.*;
import java.util.logging.Logger;

/**
 * 这是仅用于jdbc客户端的blob对象，仅用于上传文件用。
 *
 * @author 顾法华
 */
public class HerodbBlob implements java.sql.Blob {

  private static final Logger LOGGER = Logger.getLogger(HerodbBlob.class.getName());

  private ByteArrayOutputStream baos = new ByteArrayOutputStream();
  private byte[] buf;

  @Override
  public long length() throws SQLException {
    this.buf = this.baos.toByteArray();
    return this.buf.length;
  }

  @Override
  public byte[] getBytes(long pos, int length) throws SQLException {
    pos = pos - 1;
    buf = this.baos.toByteArray();
    if ((pos + 1) > buf.length) {
      throw new SQLException("超出数组长度");
    }

    int len = (pos + length) <= buf.length ? length : (buf.length - (int) pos);
    if (len == 0) {
      return new byte[0];
    }

    byte[] result = new byte[len];
    System.arraycopy(buf, (int) pos, result, 0, len);
    return result;
  }

  @Override
  public InputStream getBinaryStream() throws SQLException {
    this.buf = this.baos.toByteArray();
    return new ByteArrayInputStream(this.buf);
  }

  @Override
  public long position(byte[] pattern, long start) throws SQLException {
    throw org.herodbsql.Driver.notImplemented(this.getClass(), "position()");
//    return 0;
  }

  @Override
  public long position(Blob pattern, long start) throws SQLException {
    throw org.herodbsql.Driver.notImplemented(this.getClass(), "position()");
//    return 0;
  }

  @Override
  public int setBytes(long pos, byte[] bytes) throws SQLException {
    pos = pos - 1;

    byte[] tmp = this.baos.toByteArray();
    if ((pos + bytes.length) > tmp.length) {
      byte[] tmp2 = new byte[(int) pos + bytes.length];
      System.arraycopy(tmp, 0, tmp2, 0, tmp.length);
      tmp = tmp2;
    }
    System.arraycopy(bytes, 0, tmp, (int) pos, bytes.length);
    this.buf = tmp;
    this.baos.reset();
    try {
      this.baos.write(this.buf);
    } catch (IOException e) {
      throw new SQLException(e);
    }
    return bytes.length;
  }

  @Override
  public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
    len = Math.min((bytes.length - offset), len);
    byte[] tmp = new byte[len];
    System.arraycopy(bytes, offset, tmp, 0, len);
    return this.setBytes(pos, tmp);
  }

  @Override
  public OutputStream setBinaryStream(long pos) throws SQLException {
    this.setBytes(pos, new byte[0]);
    return this.baos;
  }

  @Override
  public void truncate(long len) throws SQLException {
    byte[] tmp = this.baos.toByteArray();
    if (tmp.length > len) {
      byte[] tmp2 = new byte[(int) len];
      System.arraycopy(tmp, 0, tmp2, 0, (int) len);
      this.buf = tmp2;
    }
  }

  @Override
  public void free() throws SQLException {
  }

  @Override
  public InputStream getBinaryStream(long pos, long length) throws SQLException {
    byte[] tmp = getBytes(pos, (int) length);
    return new ByteArrayInputStream(tmp);
  }
}
