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

import net.sf.jpivotart.jpivot.olap.model.Member;
import net.sf.jpivotart.jpivot.olap.model.OlapException;

/**
 * MDX Member
 */
public interface MDXMember extends Member, MDXElement {

  /**
   * get parent member
   */
  Member getParent() throws OlapException;

  /**
   * get parent member unique name 
   */
  String getParentUniqueName();

} // MDXMember
