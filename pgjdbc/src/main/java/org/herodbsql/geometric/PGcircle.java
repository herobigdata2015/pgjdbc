/*
 * Copyright (c) 2003, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.herodbsql.geometric;

import org.herodbsql.util.GT;
import org.herodbsql.util.PGobject;
import org.herodbsql.util.PGtokenizer;
import org.herodbsql.util.PSQLException;
import org.herodbsql.util.PSQLState;

import java.io.Serializable;
import java.sql.SQLException;

/**
 * This represents org.herodbsql's circle datatype, consisting of a point and a radius.
 */
public class PGcircle extends PGobject implements Serializable, Cloneable {
  /**
   * This is the center point.
   */
  public PGpoint center;

  /**
   * This is the radius.
   */
  public double radius;

  /**
   * @param x coordinate of center
   * @param y coordinate of center
   * @param r radius of circle
   */
  public PGcircle(double x, double y, double r) {
    this(new PGpoint(x, y), r);
  }

  /**
   * @param c PGpoint describing the circle's center
   * @param r radius of circle
   */
  public PGcircle(PGpoint c, double r) {
    this();
    this.center = c;
    this.radius = r;
  }

  /**
   * @param s definition of the circle in PostgreSQL's syntax.
   * @throws SQLException on conversion failure
   */
  public PGcircle(String s) throws SQLException {
    this();
    setValue(s);
  }

  /**
   * This constructor is used by the driver.
   */
  public PGcircle() {
    setType("circle");
  }

  /**
   * @param s definition of the circle in PostgreSQL's syntax.
   * @throws SQLException on conversion failure
   */
  @Override
  public void setValue(String s) throws SQLException {
    PGtokenizer t = new PGtokenizer(PGtokenizer.removeAngle(s), ',');
    if (t.getSize() != 2) {
      throw new PSQLException(GT.tr("Conversion to type {0} failed: {1}.", type, s),
          PSQLState.DATA_TYPE_MISMATCH);
    }

    try {
      center = new PGpoint(t.getToken(0));
      radius = Double.parseDouble(t.getToken(1));
    } catch (NumberFormatException e) {
      throw new PSQLException(GT.tr("Conversion to type {0} failed: {1}.", type, s),
          PSQLState.DATA_TYPE_MISMATCH, e);
    }
  }

  /**
   * @param obj Object to compare with
   * @return true if the two circles are identical
   */
  public boolean equals(Object obj) {
    if (obj instanceof PGcircle) {
      PGcircle p = (PGcircle) obj;
      return p.center.equals(center) && p.radius == radius;
    }
    return false;
  }

  public int hashCode() {
    long v = Double.doubleToLongBits(radius);
    return (int) (center.hashCode() ^ v ^ (v >>> 32));
  }

  public Object clone() throws CloneNotSupportedException {
    PGcircle newPGcircle = (PGcircle) super.clone();
    if (newPGcircle.center != null) {
      newPGcircle.center = (PGpoint) newPGcircle.center.clone();
    }
    return newPGcircle;
  }

  /**
   * @return the PGcircle in the syntax expected by org.herodbsql
   */
  public String getValue() {
    return "<" + center + "," + radius + ">";
  }
}
