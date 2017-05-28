/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * Copyright (C) 2003-2004 TONBELLER AG.
 * All Rights Reserved.
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 *
 * 
 */
package net.sf.jpivotart.jpivot.olap.query;

import net.sf.jpivotart.jpivot.olap.model.Hierarchy;

/**
 * Wrapper for Set Object to be placed on a query axis
 */
public class SetExp {
  private int mode;
  private Object oExp;
  private Hierarchy hier;

  public SetExp(int mode, Object oExp, Hierarchy hier) {
    this.mode = mode;
    this.oExp = oExp;
    this.hier = hier;
  }

  /**
   * @return hier
   */
  public Hierarchy getHier() {
    return hier;
  }

  /**
   * @return mode
   */
  public int getMode() {
    return mode;
  }

  /**
   * @return oexp
   */
  public Object getOExp() {
    return oExp;
  }

} // SetExp
