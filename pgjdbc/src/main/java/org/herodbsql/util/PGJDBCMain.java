/*
 * Copyright (c) 2004, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.herodbsql.util;

import org.herodbsql.Driver;

public class PGJDBCMain {

  public static void main(String[] args) {

    java.net.URL url = Driver.class.getResource("/org/herodbsql/Driver.class");
    System.out.printf("%n%s%n", org.herodbsql.util.DriverInfo.DRIVER_FULL_NAME);
    System.out.printf("Found in: %s%n%n", url);

    System.out.printf("The PgJDBC driver is not an executable Java program.%n%n"
                       + "You must install it according to the JDBC driver installation "
                       + "instructions for your application / container / appserver, "
                       + "then use it by specifying a JDBC URL of the form %n    jdbc:herodbsql://%n"
                       + "or using an application specific method.%n%n"
                       + "See the PgJDBC documentation: http://jdbc.postgresql.org/documentation/head/index.html%n%n"
                       + "This command has had no effect.%n");

    System.exit(1);
  }
}
