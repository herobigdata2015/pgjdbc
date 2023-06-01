package org.herodbx.util;

import com.sun.jna.Memory;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import org.herodbx.herossl.jna.HeroCrypto;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

public class HeroUtils {
  private static final Logger LOGGER = Logger.getLogger(HeroUtils.class.getName());

  private static String macAddressStr = null;
  private static final String[] windowsCommand = { "ipconfig", "/all" };
  private static final String[] linuxCommand = { "/sbin/ifconfig", "-a" };
  private static final Pattern macPattern = Pattern.compile(".*(([0-9a-f]{2}[-:]){5}[0-9a-f]{2}).*", Pattern.CASE_INSENSITIVE);

  /**
   * 获取多个网卡地址
   *
   * @return
   * @throws IOException
   */
  private final static List<String> getMacAddressList() throws IOException {
    final ArrayList<String> macAddressList = new ArrayList<String>();
    final String os = System.getProperty("os.name");
    final String command[];

    if (os.startsWith("Windows")) {
      command = windowsCommand;
    } else if (os.startsWith("Linux")) {
      command = linuxCommand;
    } else {
      LOGGER.log(Level.SEVERE,"Unknow operating system:" + os);
      throw new IOException("Unknow operating system:" + os);
    }
    // 执行命令
    final Process process = Runtime.getRuntime().exec(command);

    BufferedReader bufReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    for (String line = null; (line = bufReader.readLine()) != null;) {
      Matcher matcher = macPattern.matcher(line);
      if (matcher.matches()) {
        macAddressList.add(matcher.group(1).replace("-",":").toUpperCase());
      }
    }

    process.destroy();
    bufReader.close();
    return macAddressList;
  }

  /**
   * 获取一个网卡地址（多个网卡时从中获取一个）
   *
   * @return
   */
  public static String getMacAddress() {
    if (macAddressStr == null || macAddressStr.equals("")) {
      try {
        List<String> macList = getMacAddressList();
        //先用set去重
        Set<String> set = new HashSet<>();
        for(String mac:macList){
          set.add(mac);
        }

        StringBuffer sb = new StringBuffer();
        for(String mac:set){
          sb.append(","+mac);
        }
        String tmp = sb.toString();
        if(tmp.startsWith(",")){
          tmp = tmp.substring(1);
        }
        macAddressStr = tmp;
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE,"获取本机MAC地址出错",e);
      }
    }
    LOGGER.info("MAC地址列表："+macAddressStr);
    return macAddressStr;
  }

  /**
   *判断是linux还是windows系统
   **/
  public static boolean isOSLinux() {
    String os = System.getProperty("os.name");
    if (os != null && os.toLowerCase().indexOf("linux") > -1)
      return true;
    else
      return false;
  };

  /**
   * 获取jdbc驱动的jar包的水印值
   * 打包为jar包时，返回jar文件的水印值；未打包时，返回本calss文件的水印值（仅用于测试时）
   * @return 返回的是32字节的水印字节数组进行base64编码后的字符串的字节数组，共44字节。
   * @author 顾法华
   * @since 2020年6月1日
   */
  public static final byte[] getWaterMark(){
    //获取当前项目包的路径
    String dataPath = getAppPath();
    LOGGER.log(INFO, "projectPath:{0}", dataPath);

    Pointer memory = new Memory(32);
    // 调用动态库生成软件水印
    int ret = HeroCrypto.INSTANCE.db_CalculateFileHash(dataPath, "herodb", 6, memory);
    if(ret != 0){
      String msg = HeroCrypto.INSTANCE.db_GetLastErrMsg();
      LOGGER.log(SEVERE,"调用c计算文件的hash出错："+msg);
    }else{
      LOGGER.finest("计算水印成功:"+HeroUtils.toHexString(memory.getByteArray(0, 32)));
    }

    // 调用base64编码
    Pointer out = new Memory(1024);
    IntByReference intByReference = new IntByReference();
    ret = HeroCrypto.INSTANCE.db_Base64Encode(memory, 32, false, out, intByReference);
    if(ret != 0){
      String msg = HeroCrypto.INSTANCE.db_GetLastErrMsg();
      LOGGER.log(SEVERE,"计算base64编码出错："+msg);
    }else{
      LOGGER.finest("计算base64成功，结果中有字符 "+intByReference.getValue()+" 个（字节）");
    }

    // 获取编码之后的软件水印
    byte[] byteArray = out.getByteArray(0, intByReference.getValue());

    String base64 = new String(byteArray);
    LOGGER.finest("水印字符串: "+ base64);

    return byteArray;
  }

  /**
   * 获取jar的相对路径，当jar被解开执行时，返回本类的文件路径。
   * @return String
   */
  public static String getAppPath(){
    URL location = HeroUtils.class.getProtectionDomain().getCodeSource().getLocation();
    LOGGER.finest("loc: "+location.toString());

    URL u2 = HeroUtils.class.getResource("/org/herodbx/util/HeroUtils.class");
    String targetPath = u2.toString();
    LOGGER.finest("targetPath: "+targetPath);

    int pos = targetPath.indexOf(".jar!");
    if(-1!=pos){
      LOGGER.finest("jar包形式");
      targetPath = targetPath.substring(0,pos+4);
    }else{
      //jar包被解开
      LOGGER.finest("classes形式");
    }
    LOGGER.finest("targetPath(2): "+targetPath);

    targetPath = targetPath.substring(targetPath.indexOf("file:")+5);
    LOGGER.finest("targetPath(3): \""+targetPath+"\"");

    if(Platform.isWindows() && targetPath.startsWith("/")){
      targetPath = targetPath.substring(1);
      LOGGER.finest("targetPath(4): \""+targetPath+"\"");
    }

    return targetPath;
  }

  /**
   * 把一个文件转化为byte字节数组。
   *
   * @return
   */
  public static byte[] fileConvertToByteArray(File file) {
    byte[] data = null;

    try {
      FileInputStream fis = new FileInputStream(file);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      int len;
      byte[] buffer = new byte[1024];
      while ((len = fis.read(buffer)) != -1) {
        baos.write(buffer, 0, len);
      }

      data = baos.toByteArray();

      fis.close();
      baos.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return data;
  }

  public static String toHexString(byte[] byteArray) {
    if (byteArray == null || byteArray.length <= 0){
      return "";
    }

    final StringBuilder hexString = new StringBuilder();
    for (int i = 0; i < byteArray.length; i++) {
      int v = byteArray[i] & 0xFF;
      String hv = Integer.toHexString(v);
      if (hv.length() < 2) {
        hexString.append(0);
      }
      hexString.append(hv);
      hexString.append(" ");
    }
    return hexString.toString().trim().toUpperCase();
  }

}
