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

import java.util.HashSet;
import java.util.Iterator;

import net.sf.jpivotart.jpivot.olap.model.Member;
import net.sf.jpivotart.jpivot.olap.navi.DrillExpandMember;

/**
 * Created on 22.10.2002
 * 
 * @author av
 */
public class TestDrillExpandMember extends TestExtensionSupport implements DrillExpandMember {

  HashSet<Member> expanded = new HashSet<>();

  protected TestOlapModel model() {
    return (TestOlapModel) super.getModel();
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.navi.DrillExpandMember#canExpand(Member)
   */
  public boolean canExpand(Member member) {
    TestMember tm = (TestMember) member;
    return tm.hasChildren() && !expanded.contains(member);
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.navi.DrillExpandMember#canCollapse(Member)
   */
  public boolean canCollapse(Member member) {
    TestMember tm = (TestMember) member;
    return tm.hasChildren() && expanded.contains(member);
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.navi.DrillExpandMember#expand(Member)
   */

  public void expand(Member member) {
    expanded.add(member);
    TestMember tm = (TestMember) member;
    for (Iterator it = tm.getChildMember().iterator(); it.hasNext();)
       ((TestMember) it.next()).setVisible(true);
    TestOlapModelUtils.rebuildAxis(model(), tm);
    fireModelChanged();
  }

  public void collapse(Member member) {
    recurseCollapse((TestMember) member);
    TestOlapModelUtils.rebuildAxis(model(), (TestMember)member);
    fireModelChanged();
  }


  private void recurseCollapse(TestMember tm) {
    if (!expanded.contains(tm))
      return;
    expanded.remove(tm);
    for (Iterator it = tm.getChildMember().iterator(); it.hasNext();) {
      TestMember child = (TestMember) it.next();
      recurseCollapse(child);
      child.setVisible(false);
    }
  }


}
