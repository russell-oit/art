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
package net.sf.jpivotart.jpivot.olap.navi;

import net.sf.jpivotart.jpivot.core.Extension;

/**
 * suppresses the display of empty rows/columns on an axis.
 * @author av
 */
public interface NonEmpty extends Extension {
  /**
   * name of the Extension for lookup
   */
  public static final String ID = "nonEmpty";

  /**
   * @return true if non-empty rows are currently suppressed
   */
  boolean isNonEmpty();
  
  /**
   * change the visability of non-empty rows
   */
  void setNonEmpty(boolean nonEmpty);
}
