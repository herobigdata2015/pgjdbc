/*
 * Copyright (c) 2004, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.herodbsql.test.jdbc2;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.herodbsql.test.TestUtil;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectTimeoutTest {
  // The IP below is non-routable (see http://stackoverflow.com/a/904609/1261287)
  private static final String UNREACHABLE_HOST = "10.255.255.1";
  private static final String UNREACHABLE_URL = "jdbc:herodbsql://" + UNREACHABLE_HOST + ":5432/test";
  private static final int CONNECT_TIMEOUT = 5;

  @Before
  public void setUp() throws Exception {
    TestUtil.initDriver();
  }

  @Test
  public void testTimeout() {
    final Properties props = new Properties();
    props.setProperty("user", "test");
    props.setProperty("password", "test");
    // with 0 (default value) it hangs for about 60 seconds (platform dependent)
    props.setProperty("connectTimeout", Integer.toString(CONNECT_TIMEOUT));

    final long startTime = System.currentTimeMillis();
    try {
      DriverManager.getConnection(UNREACHABLE_URL, props);
    } catch (SQLException e) {
      final long interval = System.currentTimeMillis() - startTime;
      final long connectTimeoutMillis = CONNECT_TIMEOUT * 1000;
      final long maxDeviation = connectTimeoutMillis / 10;

      /*
       * If the platform fast-fails the unroutable address connection then this
       * test may not time out, instead throwing
       * java.net.NoRouteToHostException. The test has failed in that the connection
       * attempt did not time out.
       *
       * We treat this as a skipped test, as the test didn't really "succeed"
       * in testing the original behaviour, but it didn't fail either.
       */
      Assume.assumeTrue("Host fast-failed connection to unreachable address "
                        + UNREACHABLE_HOST + " after " + interval + " ms, "
                        + " before timeout should have triggered.",
                        e.getCause() instanceof NoRouteToHostException
                        && interval < connectTimeoutMillis );

      assertTrue("Unexpected " + e.toString() + " with cause " + e.getCause(),
          e.getCause() instanceof SocketTimeoutException);
      // check that it was not a default system timeout, an approximate value is used
      assertTrue(Math.abs(interval - connectTimeoutMillis) < maxDeviation);
      return;
    }
    fail("SQLException expected");
  }
}
