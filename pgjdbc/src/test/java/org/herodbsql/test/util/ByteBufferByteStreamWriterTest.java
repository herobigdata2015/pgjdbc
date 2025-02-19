/*
 * Copyright (c) 2020, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.herodbsql.test.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.herodbsql.util.ByteBufferByteStreamWriter;
import org.herodbsql.util.ByteStreamWriter;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ByteBufferByteStreamWriterTest {

  private ByteArrayOutputStream targetStream;
  private byte[] data;
  private ByteBufferByteStreamWriter writer;

  @Before
  public void setUp() throws Exception {
    targetStream = new ByteArrayOutputStream();
    data = new byte[] { 1, 2, 3, 4 };
    ByteBuffer buffer = ByteBuffer.wrap(data);
    writer = new ByteBufferByteStreamWriter(buffer);
  }

  @Test
  public void testReportsLengthCorrectly() {
    assertEquals("Incorrect length reported", 4, writer.getLength());
  }

  @Test
  public void testCopiesDataCorrectly() throws IOException {
    writer.writeTo(target(targetStream));
    byte[] written = targetStream.toByteArray();
    assertArrayEquals("Incorrect data written to target stream", data, written);
  }

  @Test
  public void testPropagatesException() throws IOException {
    final IOException e = new IOException("oh no");
    OutputStream errorStream = new OutputStream() {
      @Override
      public void write(int b) throws IOException {
        throw e;
      }
    };
    try {
      writer.writeTo(target(errorStream));
      fail("No exception thrown");
    } catch (IOException caught) {
      assertEquals("Exception was thrown that wasn't the expected one", caught, e);
    }
  }

  private static ByteStreamWriter.ByteStreamTarget target(final OutputStream stream) {
    return new ByteStreamWriter.ByteStreamTarget() {
      @Override
      public OutputStream getOutputStream() {
        return stream;
      }
    };
  }
}
