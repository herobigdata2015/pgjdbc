/*
 * Copyright (c) 2016, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.herodbsql.test.jdbc4.jdbc41;

import org.herodbsql.Driver;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.jiderhamn.classloader.PackagesLoadedOutsideClassLoader;
import se.jiderhamn.classloader.leak.JUnitClassloaderRunner;
import se.jiderhamn.classloader.leak.Leaks;

/**
 * <p>Test case that verifies that the use of {@link org.herodbsql.util.SharedTimer} within
 * {@link org.herodbsql.Driver} does not cause ClassLoader leaks.</p>
 *
 * <p>The class is placed in {@code jdbc41} package so it won't be tested in JRE6 build.
 * {@link JUnitClassloaderRunner} does not support JRE6, so we have to skip the test there.</p>
 *
 * @author Mattias Jiderhamn
 */
@RunWith(JUnitClassloaderRunner.class)
@PackagesLoadedOutsideClassLoader(packages = "org.herodbsql", addToDefaults = true)
public class SharedTimerClassLoaderLeakTest {

  /** Starting a {@link org.herodbsql.util.SharedTimer} should not cause ClassLoader leaks. */
  @Leaks(false)
  @Test
  public void sharedTimerDoesNotCauseLeak() {
    Driver.getSharedTimer().getTimer(); // Start timer
  }

  @After
  public void tearDown() {
    Driver.getSharedTimer().releaseTimer();
  }
}
