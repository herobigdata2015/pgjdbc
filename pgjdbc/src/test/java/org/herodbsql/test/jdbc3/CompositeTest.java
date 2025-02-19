/*
 * Copyright (c) 2007, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.herodbsql.test.jdbc3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.herodbsql.PGConnection;
import org.herodbsql.core.ServerVersion;
import org.herodbsql.jdbc.PreferQueryMode;
import org.herodbsql.test.TestUtil;
import org.herodbsql.util.PGobject;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CompositeTest {

  private Connection conn;

  @BeforeClass
  public static void beforeClass() throws Exception {
    Connection conn = TestUtil.openDB();
    try {
      Assume.assumeTrue("uuid requires PostgreSQL 8.3+", TestUtil.haveMinimumServerVersion(conn, ServerVersion.v8_3));
    } finally {
      conn.close();
    }
  }

  @Before
  public void setUp() throws Exception {
    conn = TestUtil.openDB();
    TestUtil.createSchema(conn, "\"Composites\"");
    TestUtil.createCompositeType(conn, "simplecompositetest", "i int, d decimal, u uuid");
    TestUtil.createCompositeType(conn, "nestedcompositetest", "t text, s simplecompositetest");
    TestUtil.createCompositeType(conn, "\"Composites\".\"ComplexCompositeTest\"",
        "l bigint[], n nestedcompositetest[], s simplecompositetest");
    TestUtil.createTable(conn, "compositetabletest",
        "s simplecompositetest, cc \"Composites\".\"ComplexCompositeTest\"[]");
    TestUtil.createTable(conn, "\"Composites\".\"Table\"",
        "s simplecompositetest, cc \"Composites\".\"ComplexCompositeTest\"[]");
  }

  @After
  public void tearDown() throws SQLException {
    TestUtil.dropTable(conn, "\"Composites\".\"Table\"");
    TestUtil.dropTable(conn, "compositetabletest");
    TestUtil.dropType(conn, "\"Composites\".\"ComplexCompositeTest\"");
    TestUtil.dropType(conn, "nestedcompositetest");
    TestUtil.dropType(conn, "simplecompositetest");
    TestUtil.dropSchema(conn, "\"Composites\"");
    TestUtil.closeDB(conn);
  }

  @Test
  public void testSimpleSelect() throws SQLException {
    PreparedStatement pstmt = conn.prepareStatement("SELECT '(1,2.2,)'::simplecompositetest");
    ResultSet rs = pstmt.executeQuery();
    assertTrue(rs.next());
    PGobject pgo = (PGobject) rs.getObject(1);
    assertEquals("simplecompositetest", pgo.getType());
    assertEquals("(1,2.2,)", pgo.getValue());
  }

  @Test
  public void testComplexSelect() throws SQLException {
    PreparedStatement pstmt = conn.prepareStatement(
        "SELECT '(\"{1,2}\",{},\"(1,2.2,)\")'::\"Composites\".\"ComplexCompositeTest\"");
    ResultSet rs = pstmt.executeQuery();
    assertTrue(rs.next());
    PGobject pgo = (PGobject) rs.getObject(1);
    assertEquals("\"Composites\".\"ComplexCompositeTest\"", pgo.getType());
    assertEquals("(\"{1,2}\",{},\"(1,2.2,)\")", pgo.getValue());
  }

  @Test
  public void testSimpleArgumentSelect() throws SQLException {
    Assume.assumeTrue("Skip if running in simple query mode", conn.unwrap(PGConnection.class).getPreferQueryMode() != PreferQueryMode.SIMPLE);
    PreparedStatement pstmt = conn.prepareStatement("SELECT ?");
    PGobject pgo = new PGobject();
    pgo.setType("simplecompositetest");
    pgo.setValue("(1,2.2,)");
    pstmt.setObject(1, pgo);
    ResultSet rs = pstmt.executeQuery();
    assertTrue(rs.next());
    PGobject pgo2 = (PGobject) rs.getObject(1);
    assertEquals(pgo, pgo2);
  }

  @Test
  public void testComplexArgumentSelect() throws SQLException {
    Assume.assumeTrue("Skip if running in simple query mode", conn.unwrap(PGConnection.class).getPreferQueryMode() != PreferQueryMode.SIMPLE);
    PreparedStatement pstmt = conn.prepareStatement("SELECT ?");
    PGobject pgo = new PGobject();
    pgo.setType("\"Composites\".\"ComplexCompositeTest\"");
    pgo.setValue("(\"{1,2}\",{},\"(1,2.2,)\")");
    pstmt.setObject(1, pgo);
    ResultSet rs = pstmt.executeQuery();
    assertTrue(rs.next());
    PGobject pgo2 = (PGobject) rs.getObject(1);
    assertEquals(pgo, pgo2);
  }

  @Test
  public void testCompositeFromTable() throws SQLException {
    PreparedStatement pstmt = conn.prepareStatement("INSERT INTO compositetabletest VALUES(?, ?)");
    PGobject pgo1 = new PGobject();
    pgo1.setType("public.simplecompositetest");
    pgo1.setValue("(1,2.2,)");
    pstmt.setObject(1, pgo1);
    String[] ctArr = new String[1];
    ctArr[0] = "(\"{1,2}\",{},\"(1,2.2,)\")";
    Array pgarr1 = conn.createArrayOf("\"Composites\".\"ComplexCompositeTest\"", ctArr);
    pstmt.setArray(2, pgarr1);
    int res = pstmt.executeUpdate();
    assertEquals(1, res);
    pstmt = conn.prepareStatement("SELECT * FROM compositetabletest");
    ResultSet rs = pstmt.executeQuery();
    assertTrue(rs.next());
    PGobject pgo2 = (PGobject) rs.getObject(1);
    Array pgarr2 = (Array) rs.getObject(2);
    assertEquals("simplecompositetest", pgo2.getType());
    assertEquals("\"Composites\".\"ComplexCompositeTest\"", pgarr2.getBaseTypeName());
    Object[] pgobjarr2 = (Object[]) pgarr2.getArray();
    assertEquals(1, pgobjarr2.length);
    PGobject arr2Elem = (PGobject) pgobjarr2[0];
    assertEquals("\"Composites\".\"ComplexCompositeTest\"", arr2Elem.getType());
    assertEquals("(\"{1,2}\",{},\"(1,2.2,)\")", arr2Elem.getValue());
    rs.close();
    pstmt = conn.prepareStatement("SELECT c FROM compositetabletest c");
    rs = pstmt.executeQuery();
    assertTrue(rs.next());
    PGobject pgo3 = (PGobject) rs.getObject(1);
    assertEquals("compositetabletest", pgo3.getType());
    assertEquals("(\"(1,2.2,)\",\"{\"\"(\\\\\"\"{1,2}\\\\\"\",{},\\\\\"\"(1,2.2,)\\\\\"\")\"\"}\")",
        pgo3.getValue());
  }

  @Test
  public void testNullArrayElement() throws SQLException {
    PreparedStatement pstmt =
        conn.prepareStatement("SELECT array[NULL, NULL]::compositetabletest[]");
    ResultSet rs = pstmt.executeQuery();
    assertTrue(rs.next());
    Array arr = rs.getArray(1);
    assertEquals("compositetabletest", arr.getBaseTypeName());
    Object[] items = (Object[]) arr.getArray();
    assertEquals(2, items.length);
    assertNull(items[0]);
    assertNull(items[1]);
  }

  @Test
  public void testTableMetadata() throws SQLException {
    PreparedStatement pstmt = conn.prepareStatement("INSERT INTO compositetabletest VALUES(?, ?)");
    PGobject pgo1 = new PGobject();
    pgo1.setType("public.simplecompositetest");
    pgo1.setValue("(1,2.2,)");
    pstmt.setObject(1, pgo1);
    String[] ctArr = new String[1];
    ctArr[0] = "(\"{1,2}\",{},\"(1,2.2,)\")";
    Array pgarr1 = conn.createArrayOf("\"Composites\".\"ComplexCompositeTest\"", ctArr);
    pstmt.setArray(2, pgarr1);
    int res = pstmt.executeUpdate();
    assertEquals(1, res);
    pstmt = conn.prepareStatement("SELECT t FROM compositetabletest t");
    ResultSet rs = pstmt.executeQuery();
    assertTrue(rs.next());
    String name = rs.getMetaData().getColumnTypeName(1);
    assertEquals("compositetabletest", name);
  }

  @Test
  public void testComplexTableNameMetadata() throws SQLException {
    PreparedStatement pstmt = conn.prepareStatement("INSERT INTO \"Composites\".\"Table\" VALUES(?, ?)");
    PGobject pgo1 = new PGobject();
    pgo1.setType("public.simplecompositetest");
    pgo1.setValue("(1,2.2,)");
    pstmt.setObject(1, pgo1);
    String[] ctArr = new String[1];
    ctArr[0] = "(\"{1,2}\",{},\"(1,2.2,)\")";
    Array pgarr1 = conn.createArrayOf("\"Composites\".\"ComplexCompositeTest\"", ctArr);
    pstmt.setArray(2, pgarr1);
    int res = pstmt.executeUpdate();
    assertEquals(1, res);
    pstmt = conn.prepareStatement("SELECT t FROM \"Composites\".\"Table\" t");
    ResultSet rs = pstmt.executeQuery();
    assertTrue(rs.next());
    String name = rs.getMetaData().getColumnTypeName(1);
    assertEquals("\"Composites\".\"Table\"", name);
  }
}
