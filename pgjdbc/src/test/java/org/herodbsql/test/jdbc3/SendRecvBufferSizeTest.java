/*
 * Copyright (c) 2004, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.herodbsql.test.jdbc3;

import org.herodbsql.test.TestUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SendRecvBufferSizeTest {

  private Connection conn;

  @Before
  public void setUp() throws Exception {
    System.setProperty("sendBufferSize", "1024");
    System.setProperty("receiveBufferSize", "1024");

    conn = TestUtil.openDB();
    Statement stmt = conn.createStatement();
    stmt.execute("CREATE TEMP TABLE hold(a int)");
    stmt.execute("INSERT INTO hold VALUES (1)");
    stmt.execute("INSERT INTO hold VALUES (2)");
    stmt.close();
  }

  @After
  public void tearDown() throws SQLException {
    Statement stmt = conn.createStatement();
    stmt.execute("DROP TABLE hold");
    stmt.close();
    TestUtil.closeDB(conn);
  }

  // dummy test
  @Test
  public void testSelect() throws SQLException {
    Statement stmt = conn.createStatement();
    stmt.execute("select * from hold");
    stmt.close();
  }
}
