/*
 * Copyright (c) 2003, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.herodbsql.test.osgi;

import org.herodbsql.jdbc2.optional.ConnectionPool;
import org.herodbsql.jdbc2.optional.PoolingDataSource;
import org.herodbsql.jdbc2.optional.SimpleDataSource;
import org.herodbsql.osgi.PGDataSourceFactory;
import org.herodbsql.xa.PGXADataSource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.jdbc.DataSourceFactory;

import java.sql.Driver;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

public class PGDataSourceFactoryTest {

  private DataSourceFactory dataSourceFactory;

  @Before
  public void createFactory() {
    dataSourceFactory = new PGDataSourceFactory();
  }

  @Test
  public void testCreateDriverDefault() throws Exception {
    Driver driver = dataSourceFactory.createDriver(null);
    Assert.assertTrue(driver instanceof org.herodbsql.Driver);
  }

  @Test
  public void testCreateDataSourceDefault() throws Exception {
    DataSource dataSource = dataSourceFactory.createDataSource(null);
    Assert.assertNotNull(dataSource);
  }

  @Test
  public void testCreateDataSourceSimple() throws Exception {
    Properties properties = new Properties();
    properties.put(DataSourceFactory.JDBC_DATABASE_NAME, "db");
    properties.put("currentSchema", "schema");
    DataSource dataSource = dataSourceFactory.createDataSource(properties);
    Assert.assertNotNull(dataSource);
    Assert.assertTrue(dataSource instanceof SimpleDataSource);
    SimpleDataSource simpleDataSource = (SimpleDataSource) dataSource;
    Assert.assertEquals("db", simpleDataSource.getDatabaseName());
    Assert.assertEquals("schema", simpleDataSource.getCurrentSchema());
  }

  @Test
  public void testCreateDataSourcePooling() throws Exception {
    Properties properties = new Properties();
    properties.put(DataSourceFactory.JDBC_DATABASE_NAME, "db");
    properties.put(DataSourceFactory.JDBC_INITIAL_POOL_SIZE, "5");
    properties.put(DataSourceFactory.JDBC_MAX_POOL_SIZE, "10");
    DataSource dataSource = dataSourceFactory.createDataSource(properties);
    Assert.assertNotNull(dataSource);
    Assert.assertTrue(dataSource instanceof PoolingDataSource);
    PoolingDataSource poolingDataSource = (PoolingDataSource) dataSource;
    Assert.assertEquals("db", poolingDataSource.getDatabaseName());
    Assert.assertEquals(5, poolingDataSource.getInitialConnections());
    Assert.assertEquals(10, poolingDataSource.getMaxConnections());
  }

  @Test
  public void testCreateConnectionPoolDataSourceDefault() throws Exception {
    ConnectionPoolDataSource dataSource = dataSourceFactory.createConnectionPoolDataSource(null);
    Assert.assertNotNull(dataSource);
  }

  @Test
  public void testCreateConnectionPoolDataSourceConfigured() throws Exception {
    Properties properties = new Properties();
    properties.put(DataSourceFactory.JDBC_DATABASE_NAME, "db");
    ConnectionPoolDataSource dataSource =
        dataSourceFactory.createConnectionPoolDataSource(properties);
    Assert.assertNotNull(dataSource);
    Assert.assertTrue(dataSource instanceof ConnectionPool);
    ConnectionPool connectionPoolDataSource = (ConnectionPool) dataSource;
    Assert.assertEquals("db", connectionPoolDataSource.getDatabaseName());
  }

  @Test
  public void testCreateXADataSourceDefault() throws Exception {
    XADataSource dataSource = dataSourceFactory.createXADataSource(null);
    Assert.assertNotNull(dataSource);
  }

  @Test
  public void testCreateXADataSourceConfigured() throws Exception {
    Properties properties = new Properties();
    properties.put(DataSourceFactory.JDBC_DATABASE_NAME, "db");
    XADataSource dataSource = dataSourceFactory.createXADataSource(properties);
    Assert.assertNotNull(dataSource);
    Assert.assertTrue(dataSource instanceof PGXADataSource);
    PGXADataSource xaDataSource = (PGXADataSource) dataSource;
    Assert.assertEquals("db", xaDataSource.getDatabaseName());
  }
}
