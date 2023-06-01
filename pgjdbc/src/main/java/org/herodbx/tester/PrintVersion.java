package org.herodbx.tester;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * 显示jdbc的版本细节。
 *
 * @author 顾法华
 * @since 2020年6月8日
 */
public class PrintVersion {
  static public void main(String[] args) throws IOException {
    Properties p = new Properties();
    p.load(new InputStreamReader(PrintVersion.class.getResourceAsStream("/org/herodbsql/git.properties")));
    String v = p.getProperty("git.build.version", "");
    String g = p.getProperty("git.commit.id.abbrev", "");
    System.out.println("版本信息：");
    System.out.println("HeroDB-"+v + "-" + g);
  }
}

