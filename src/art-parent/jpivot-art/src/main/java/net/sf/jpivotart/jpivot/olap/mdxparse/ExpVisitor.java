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

package net.sf.jpivotart.jpivot.olap.mdxparse;

import net.sf.jpivotart.jpivot.olap.model.Dimension;
import net.sf.jpivotart.jpivot.olap.model.Hierarchy;
import net.sf.jpivotart.jpivot.olap.model.Level;
import net.sf.jpivotart.jpivot.olap.model.Member;

/**
 * Visitor for MDX parse expressions
 */
public interface ExpVisitor {

  void visitCompoundId(CompoundId visio);
  void visitFormula(Formula visio);
  void visitFunCall(FunCall visio);
  void visitLiteral(Literal visio);
  void visitMemberProperty(MemberProperty visio);
  void visitParsedQuery(ParsedQuery visio);
  void visitQueryAxis(QueryAxis visio);
  void visitDimension(Dimension visio);
  void visitHierarchy(Hierarchy visio);
  void visitLevel(Level visio);
  void visitMember(Member visio);

} // ExpVisitor
