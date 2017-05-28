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

import java.util.List;

import net.sf.jpivotart.jpivot.core.ExtensionSupport;
import net.sf.jpivotart.jpivot.olap.model.Hierarchy;
import net.sf.jpivotart.jpivot.olap.model.Member;
import net.sf.jpivotart.jpivot.olap.navi.MemberTree;

/**
 * Created on 22.10.2002
 * 
 * @author av
 */
public class TestMemberTree extends ExtensionSupport implements MemberTree {

  /**
   * @see net.sf.jpivotart.jpivot.olap.navi.MemberTree#getRootMembers(Hierarchy)
   */
  public Member[] getRootMembers(Hierarchy hier) {
    return ((TestHierarchy)hier).getRootMembers();
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.navi.MemberTree#hasChildren(Member)
   */
  public boolean hasChildren(Member member) {
    return ((TestMember)member).hasChildren();
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.navi.MemberTree#getChildren(Member)
   */
  public Member[] getChildren(Member member) {
    List list = ((TestMember)member).getChildMember();
    return (Member[])list.toArray(new TestMember[list.size()]);
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.navi.MemberTree#getParent(Member)
   */
  public Member getParent(Member member) {
    return ((TestMember)member).getParentMember();
  }

}
