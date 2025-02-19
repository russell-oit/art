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
import net.sf.jpivotart.jpivot.olap.model.Member;

/**
 * allows to place selected members on the slicer axis.
 * @author av
 */
public interface ChangeSlicer extends Extension {
  
  public static final String ID = "changeSlicer";
  
  /**
   * @return the current slicer. 
   * @see net.sf.jpivotart.jpivot.olap.model.Result#getSlicer
   */
  Member[] getSlicer();

  /**
   * sets the slicer
   */
  void setSlicer(Member[] members);
}
