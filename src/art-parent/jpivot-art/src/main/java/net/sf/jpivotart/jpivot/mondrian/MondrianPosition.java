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
package net.sf.jpivotart.jpivot.mondrian;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.sf.jpivotart.jpivot.olap.query.PositionBase;

/**
 * MondrianPosition is an adapter class for the Mondrian Position.  
 */
public class MondrianPosition extends PositionBase {

  net.sf.mondrianart.mondrian.olap.Position monPosition;
  MondrianModel model;
  private int iAxis; // Axis ordinal for result axis

  /**
   * Constructor
   * create the array of members
   * @param monPosition corresponding Mondrian Position
   * @param model MondrianModel
   */
  MondrianPosition(net.sf.mondrianart.mondrian.olap.Position monPosition, int iAxis, MondrianModel model) {
    super();
    this.monPosition = monPosition;
    this.model = model;
    this.iAxis = iAxis;
    // extract the members
    List<net.sf.jpivotart.jpivot.olap.model.Member> l = new ArrayList<>();
    Iterator mit = monPosition.iterator();
    while (mit.hasNext()) {
      net.sf.mondrianart.mondrian.olap.Member monMember = (net.sf.mondrianart.mondrian.olap.Member) mit.next();
      l.add(model.lookupMemberByUName(monMember.getUniqueName()));
    }
    members = (MondrianMember[]) l.toArray(new MondrianMember[l.size()]);
  }

  /**
   * get the Mondrian Members for this Axis Position
   * @return Array of Mondrian members
  net.sf.mondrianart.mondrian.olap.Member[] getMonMembers() {
  this is not used anywhere
    return monPosition.getMembers();
  }
   */

  /**
   * Returns the iAxis.
   * @return int
   */
  int getAxis() {
    return iAxis;
  }

} // MondrianPosition
