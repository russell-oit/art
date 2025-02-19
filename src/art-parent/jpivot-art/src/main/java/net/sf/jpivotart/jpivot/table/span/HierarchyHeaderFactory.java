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
package net.sf.jpivotart.jpivot.table.span;

import net.sf.jpivotart.jpivot.olap.model.Dimension;
import net.sf.jpivotart.jpivot.olap.model.EmptyMember;
import net.sf.jpivotart.jpivot.olap.model.Hierarchy;
import net.sf.jpivotart.jpivot.olap.model.Level;
import net.sf.jpivotart.jpivot.olap.model.Member;
import net.sf.jpivotart.jpivot.olap.model.Property;
import net.sf.jpivotart.jpivot.olap.model.VisitorSupportStrict;

/**
 * returns a new span containing the hierarchy of the given span.
 * If the given span contains a dimension (which does not have
 * a single hierarchy) the new span contains the dimension.
 * @author av
 */
public class HierarchyHeaderFactory extends VisitorSupportStrict implements SpanVisitor, SpanHeaderFactory {
  Span header;

  /**
   * @see net.sf.jpivotart.jpivot.table.span.SpanHeaderFactory#create(Span)
   */
  public Span create(Span span) {
    header = (Span)span.clone();
    span.getObject().accept(this);
    return header;
  }


  public void visitDimension(Dimension v) {
    header.setObject(v);
  }

  public void visitHierarchy(Hierarchy v) {
    header.setObject(v);
  }

  public void visitLevel(Level v) {
    header.setObject(v.getHierarchy());
  }

  public void visitMember(Member v) {
    header.setObject(v.getLevel().getHierarchy());
  }

  public void visitProperty(Property v) {
    header.setObject(new PropertyHeading(v.getLabel()));
  }

  public void visitPropertyHeading(PropertyHeading heading) {
    header.setObject(new PropertyHeading(heading.getLabel()));
  }

  public void visitEmptyMember(EmptyMember v) {
    header.setObject(v);
  }


}


