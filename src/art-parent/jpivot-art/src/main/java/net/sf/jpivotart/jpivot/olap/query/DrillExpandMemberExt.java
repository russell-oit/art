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

import net.sf.jpivotart.jpivot.core.ExtensionSupport;
import net.sf.jpivotart.jpivot.olap.model.Member;
import net.sf.jpivotart.jpivot.olap.navi.DrillExpandMember;

/**
 * @author hh
 *
 */
public class DrillExpandMemberExt extends ExtensionSupport implements DrillExpandMember {
  /**
    * Constructor sets ID
    */
  public DrillExpandMemberExt() {
    super.setId(DrillExpandMember.ID);
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.navi.DrillExpandMember#canExpand(Member)
   * @param member the membber to be checked for potential expansion
   * @return true if the member can be expanded
   */
  public boolean canExpand(Member member) {
    QueryAdapter.QueryAdapterHolder model = (QueryAdapter.QueryAdapterHolder) getModel();
    return model.getQueryAdapter().canExpand(member);
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.navi.DrillExpandMember#canCollapse(Member)
   * @param member member to be expanded
   * @return true if the member can be collapsed
   */
  public boolean canCollapse(Member member) {
    QueryAdapter.QueryAdapterHolder model = (QueryAdapter.QueryAdapterHolder) getModel();
    return model.getQueryAdapter().canCollapse(member);
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.navi.DrillExpandMember#expand(Member)
   * @param member member to be expanded
   */
  public void expand(Member member) {
    QueryAdapter.QueryAdapterHolder model = (QueryAdapter.QueryAdapterHolder) getModel();
    model.getQueryAdapter().expand(member);
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.navi.DrillExpandMember#collapse(Member)
   * @param member member to be collapsed
   */
  public void collapse(Member member) {
    QueryAdapter.QueryAdapterHolder model = (QueryAdapter.QueryAdapterHolder) getModel();
    model.getQueryAdapter().collapse(member);
  }

} // End DrillExpandMemberExt
