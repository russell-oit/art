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
import net.sf.jpivotart.jpivot.olap.model.Cell;
import net.sf.wcfart.wcf.table.TableModel;

/**
 * 
 * @author Robin Bagot
 */
public interface DrillThrough extends Extension {
  /**
   * name of the Extension for lookup
   */
  public static final String ID = "drillThrough";

  /**
   * drill through is possible if <code>cell</code> is stored and not calculated
   */
  boolean canDrillThrough(Cell cell);
  
  /**
   * retrieves the individual data points that made up the value of the Cell
   */
  TableModel drillThrough(Cell cell);
  
}
