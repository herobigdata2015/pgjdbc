package org.herodbx;

/**
 * 必要时，把oracle的jdbc url更改为herodb的url，以便herodb jdbc能解析和连接到herodb服务器。
 *
 * @author 顾法华
 */
public class HerodbJdbcUrlTool {

  public static final boolean mock_oracle = false;

  /**
   * 简单的将oracle的jdbc url替换为herodb的jdbc url。
   *
   * @param url
   * @return
   */
  public static String jdbcUrlAdjust(String url) {
    if (mock_oracle && url.startsWith("jdbc:oracle:thin:@")) {
      url = "jdbc:herodbsql:" + url.substring("jdbc:oracle:thin:@".length());
    }
    return url;
  }
}
