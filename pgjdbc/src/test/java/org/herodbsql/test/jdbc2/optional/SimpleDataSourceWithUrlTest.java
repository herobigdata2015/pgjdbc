/*
 * Copyright (c) 2004, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.herodbsql.test.jdbc2.optional;

import org.herodbsql.jdbc2.optional.SimpleDataSource;
import org.herodbsql.test.TestUtil;

/**
 * Performs the basic tests defined in the superclass. Just adds the configuration logic.
 *
 * @author Aaron Mulder (ammulder@chariotsolutions.com)
 */
public class SimpleDataSourceWithUrlTest extends BaseDataSourceTest {
  /**
   * Creates and configures a new SimpleDataSource.
   */
  @Override
  protected void initializeDataSource() {
    if (bds == null) {
      bds = new SimpleDataSource();
      bds.setUrl("jdbc:herodbsql://" + TestUtil.getServer() + ":" + TestUtil.getPort() + "/"
          + TestUtil.getDatabase() + "?prepareThreshold=" + TestUtil.getPrepareThreshold()
          + "&logLevel=" + TestUtil.getLogLevel());
      bds.setUser(TestUtil.getUser());
      bds.setPassword(TestUtil.getPassword());
      bds.setProtocolVersion(TestUtil.getProtocolVersion());
    }
  }
}
