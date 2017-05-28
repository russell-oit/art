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

import net.sf.jpivotart.jpivot.olap.model.Axis;
import net.sf.jpivotart.jpivot.olap.model.Hierarchy;
import net.sf.jpivotart.jpivot.olap.model.Member;
import net.sf.jpivotart.jpivot.olap.navi.PlaceHierarchiesOnAxes;

/**
 * Created on 09.12.2002
 * 
 * @author av
 */
public class TestPlaceHierarchiesOnAxes extends TestExtensionSupport implements PlaceHierarchiesOnAxes {

  /**
   * @see net.sf.jpivotart.jpivot.olap.navi.PlaceHierarchiesOnAxes#createMemberExpression(Hierarchy)
   */
  public Object createMemberExpression(Hierarchy hier) {
    return TestOlapModelUtils.createAxis((TestDimension)hier.getDimension());
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.navi.PlaceHierarchiesOnAxes#setQueryAxis(Axis, Object[])
   */
  public void setQueryAxis(Axis target, Object[] memberExpressions) {
    int index = model().indexOf(target);
    if (index < 0)
      throw new IllegalArgumentException("axis not found");
    TestAxis axis = (TestAxis)memberExpressions[0];
    for (int i = 1; i < memberExpressions.length; i++) {
      TestAxis x = (TestAxis)memberExpressions[i];
      axis = TestOlapModelUtils.crossJoin(axis, x);
    }
    model().setAxis(index, axis);
    fireModelChanged();
  }

  public void setSlicer(Member[] members) {
    TestAxis axis = TestOlapModelUtils.createAxis(members);
    model().setSlicer(axis);
    fireModelChanged();
  }

  public void setExpandAllMember(boolean expandAllMember) {
  }

  public boolean getExpandAllMember() {
    return false;
  }

}
