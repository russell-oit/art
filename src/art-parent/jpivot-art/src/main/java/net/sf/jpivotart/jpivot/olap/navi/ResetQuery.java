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
 * resets the query to its initial state. This undoes all navigations 
 * that the user has performed.
 * @author av
 */

public interface ResetQuery extends Extension {
  /**
   * name of the Extension for lookup
   */
  public static final String ID = "resetQuery";

  /**
   * reset the query
   */
  void reset();
}
