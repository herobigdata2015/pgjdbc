/*
 * Copyright (c) 2004, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.herodbsql.test.jdbc2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.herodbsql.core.Encoding;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.util.Locale;

/**
 * Tests for the Encoding class.
 */
public class EncodingTest {

  @Test
  public void testCreation() throws Exception {
    Encoding encoding = Encoding.getDatabaseEncoding("UTF8");
    assertEquals("UTF", encoding.name().substring(0, 3).toUpperCase(Locale.US));
    encoding = Encoding.getDatabaseEncoding("SQL_ASCII");
    assertTrue(encoding.name().toUpperCase(Locale.US).contains("ASCII"));
    assertEquals("When encoding is unknown the default encoding should be used",
        Encoding.defaultEncoding(), Encoding.getDatabaseEncoding("UNKNOWN"));
  }

  @Test
  public void testTransformations() throws Exception {
    Encoding encoding = Encoding.getDatabaseEncoding("UTF8");
    assertEquals("ab", encoding.decode(new byte[]{97, 98}));

    assertEquals(2, encoding.encode("ab").length);
    assertEquals(97, encoding.encode("a")[0]);
    assertEquals(98, encoding.encode("b")[0]);

    encoding = Encoding.defaultEncoding();
    assertEquals("a".getBytes()[0], encoding.encode("a")[0]);
    assertEquals(new String(new byte[]{97}), encoding.decode(new byte[]{97}));
  }

  @Test
  public void testReader() throws Exception {
    Encoding encoding = Encoding.getDatabaseEncoding("SQL_ASCII");
    InputStream stream = new ByteArrayInputStream(new byte[]{97, 98});
    Reader reader = encoding.getDecodingReader(stream);
    assertEquals(97, reader.read());
    assertEquals(98, reader.read());
    assertEquals(-1, reader.read());
  }
}
