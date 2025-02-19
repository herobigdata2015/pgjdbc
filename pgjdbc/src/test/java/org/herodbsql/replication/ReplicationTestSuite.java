/*
 * Copyright (c) 2016, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.herodbsql.replication;

import org.herodbsql.core.ServerVersion;
import org.herodbsql.test.TestUtil;

import org.junit.AssumptionViolatedException;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    CopyBothResponseTest.class,
    LogicalReplicationStatusTest.class,
    LogicalReplicationTest.class,
    LogSequenceNumberTest.class,
    PhysicalReplicationTest.class,
    ReplicationConnectionTest.class,
    ReplicationSlotTest.class,
})
public class ReplicationTestSuite {

  @BeforeClass
  public static void setUp() throws Exception {
    Connection connection = TestUtil.openDB();
    try {
      if (TestUtil.haveMinimumServerVersion(connection, ServerVersion.v9_0)) {
        assumeWalSenderEnabled(connection);
        assumeReplicationRole(connection);
      } else {
        throw new AssumptionViolatedException(
            "Skip replication test because current database version "
                + "too old and don't contain replication API"
        );
      }
    } finally {
      connection.close();
    }
  }

  private static void assumeWalSenderEnabled(Connection connection) throws SQLException {
    Statement stmt = connection.createStatement();
    ResultSet rs = stmt.executeQuery("SHOW max_wal_senders");
    rs.next();
    int maxWalSenders = rs.getInt(1);
    rs.close();
    stmt.close();

    if (maxWalSenders == 0) {
      throw new AssumptionViolatedException(
          "Skip replication test because max_wal_senders = 0");
    }
  }

  private static void assumeReplicationRole(Connection connection) throws SQLException {
    Statement stmt = connection.createStatement();
    ResultSet rs =
        stmt.executeQuery("SELECT usename, userepl FROM pg_user WHERE usename = current_user");
    rs.next();
    String userName = rs.getString(1);
    boolean replicationGrant = rs.getBoolean(2);
    rs.close();
    stmt.close();

    if (!replicationGrant) {
      throw new AssumptionViolatedException(
          "Skip replication test because user '" + userName + "' doesn't have replication role");
    }
  }
}
