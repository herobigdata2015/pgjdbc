package org.herodbx;

/**
 * @author 顾法华
 * @since 2021年1月25日
 */
public class HeroCommonTool {
  public static byte[] byteMerger(byte[] a, byte[] b){
    byte[] r = new byte[a.length+ b.length];
    System.arraycopy(a, 0, r, 0, a.length);
    System.arraycopy(b, 0, r, a.length, b.length);
    return r;
  }

  public static byte[] subByte(byte[] source, int offset, int length){
    byte[] b1 = new byte[length];
    System.arraycopy(source, offset, b1, 0, length);
    return b1;
  }

  public static String bytes2HexString(byte[] b) {
    StringBuffer result = new StringBuffer();
    for (int i = 0; i < b.length; i++) {
      result.append(String.format("%02X",b[i]));
    }
    return result.toString();
  }

  public static byte[] hexString2Bytes(String src) {
    if(null==src || src.length()%2!=0){
      throw new IllegalArgumentException("字符串长度不正确！");
    }
    int l = src.length() / 2;
    byte[] ret = new byte[l];
    for (int i = 0; i < l; i++) {
      ret[i] = Integer.valueOf(src.substring(i * 2, i * 2 + 2), 16).byteValue();
    }
    return ret;
  }

  static public String getPrintable(byte[] bytes){
    byte[] out = new byte[bytes.length];
    for(int i=0; i<bytes.length; i++){
      if(bytes[i]>=32 && bytes[i]<=126){
        out[i] = bytes[i];
      }else{
        out[i] = ' ';
      }
    }
    return new String(out);
  }
}
