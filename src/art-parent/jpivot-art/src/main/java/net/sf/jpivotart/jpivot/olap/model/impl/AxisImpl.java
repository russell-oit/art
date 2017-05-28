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
package net.sf.jpivotart.jpivot.olap.model.impl;

import java.util.ArrayList;
import java.util.List;

import net.sf.jpivotart.jpivot.olap.model.Axis;
import net.sf.jpivotart.jpivot.olap.model.Hierarchy;
import net.sf.jpivotart.jpivot.olap.model.Member;
import net.sf.jpivotart.jpivot.olap.model.Position;
import net.sf.jpivotart.jpivot.olap.model.Visitor;

/**
 * implements axis
 * @author av
 */
public class AxisImpl implements Axis {

  List<Position> positions = new ArrayList<>();

  /**
   * @return the hierarchies of the members of the first position.
   * If there are no positions (i.e. axis is empty), an empty array (non null)
   * is returned.
   * @see net.sf.jpivotart.jpivot.olap.model.Axis#getHierarchies()
   */
  public Hierarchy[] getHierarchies() {
    if (positions.size() > 0) {
      Position pos = positions.get(0);
      Member[] members = pos.getMembers();
      Hierarchy[] hiers = new Hierarchy[members.length];
      for (int i = 0; i < members.length; i++)
        hiers[i] = members[i].getLevel().getHierarchy();
      return hiers;
    }
    return new Hierarchy[0];
  }

  /**
   * Returns the positions.
   * @return List
   */
  public List<Position> getPositions() {
    return positions;
  }

  /**
   * Sets the positions.
   * @param positions The positions to set
   */
  public void setPositions(List<Position> positions) {
    this.positions = positions;
  }

  public void addPosition(Position pos) {
    positions.add(pos);
  }

  public void accept(Visitor visitor) {
    visitor.visitAxis(this);
  }
  
  public Object getRootDecoree() {
    return this;
  }

}
