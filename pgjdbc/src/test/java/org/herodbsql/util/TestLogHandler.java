/*
 * Copyright (c) 2020, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.herodbsql.util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;

public class TestLogHandler extends Handler {
  public List<LogRecord> records = new ArrayList<LogRecord>();

  @Override
  public void publish(LogRecord record) {
    records.add(record);
  }

  @Override
  public void flush() {
  }

  @Override
  public void close() throws SecurityException {
  }

  public List<LogRecord> getRecordsMatching(Pattern messagePattern) {
    ArrayList<LogRecord> matches = new ArrayList<LogRecord>();
    for (LogRecord r: this.records) {
      if (messagePattern.matcher(r.getMessage()).find()) {
        matches.add(r);
      }
    }
    return matches;
  }
}
