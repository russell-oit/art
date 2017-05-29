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

import java.util.List;

import net.sf.jpivotart.jpivot.olap.model.Member;
import net.sf.jpivotart.jpivot.olap.model.Position;
import net.sf.jpivotart.jpivot.olap.model.Visitor;

/**
 * base class for both Mondrian and XMLA Position
 */
public class PositionBase implements Position {

  protected Member[] members;

  // cellList, parent and number are temp variables used by hierarchize sort
  public List<Object> cellList = null;
  public PositionBase parent = null;
  public int number; 

  /* 
   * @return array of members
   * @see net.sf.jpivotart.jpivot.olap.model.Position#getMembers()
   */
  public Member[] getMembers() {
    return members;
  }
  

  /* (non-Javadoc)
   * @see net.sf.jpivotart.jpivot.olap.model.Visitable#accept
   */
  public void accept(Visitor visitor) {
    visitor.visitPosition(this);
  }

  /*
   * @see net.sf.jpivotart.jpivot.olap.model.Decorator#getRootDecoree()
   */
  public Object getRootDecoree() {
    return this;
  }

} // PositionBase
