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
package net.sf.jpivotart.jpivot.test.olap;

import net.sf.jpivotart.jpivot.olap.navi.NonEmpty;

/**
 * Created on 06.12.2002
 * 
 * @author av
 */
public class TestNonEmpty extends TestExtensionSupport implements NonEmpty {
  boolean value;
  
  /**
   * @see net.sf.jpivotart.jpivot.olap.navi.NonEmpty#isNonEmpty()
   */
  public boolean isNonEmpty() {
    return value;
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.navi.NonEmpty#setNonEmpty(boolean)
   */
  public void setNonEmpty(boolean nonEmpty) {
    value = nonEmpty;
    fireModelChanged();
  }

}
