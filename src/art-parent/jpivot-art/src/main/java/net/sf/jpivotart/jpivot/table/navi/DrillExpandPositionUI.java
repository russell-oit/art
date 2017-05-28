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
package net.sf.jpivotart.jpivot.table.navi;

import net.sf.jpivotart.jpivot.olap.model.Member;
import net.sf.jpivotart.jpivot.olap.model.OlapModel;
import net.sf.jpivotart.jpivot.olap.model.Position;
import net.sf.jpivotart.jpivot.olap.navi.DrillExpandPosition;
import net.sf.jpivotart.jpivot.table.span.Span;

/**
 * Created on 29.11.2002
 * 
 * @author av
 */
public class DrillExpandPositionUI extends DrillExpandUI {
  public static final String ID = "drillPosition";
  public String getId() {
    return ID;
  }

  DrillExpandPosition expandPosition;

  protected boolean initializeExtension() {
    OlapModel om = table.getOlapModel();
    expandPosition = (DrillExpandPosition) om.getExtension(DrillExpandPosition.ID);
    return expandPosition != null;
  }

  protected boolean canExpand(Span span) {
    if (!positionContainsMember(span))
      return false;
    return expandPosition.canExpand(
      (Position) span.getPosition().getRootDecoree(),
      (Member) span.getMember().getRootDecoree());
  }

  protected boolean canCollapse(Span span) {
    if (!positionContainsMember(span))
      return false;
    return expandPosition.canCollapse(
      (Position) span.getPosition().getRootDecoree(),
      (Member) span.getMember().getRootDecoree());
  }

  protected void expand(Span span) {
    expandPosition.expand(
      (Position) span.getPosition().getRootDecoree(),
      (Member) span.getMember().getRootDecoree());
  }

  protected void collapse(Span span) {
    expandPosition.collapse(
      (Position) span.getPosition().getRootDecoree(),
      (Member) span.getMember().getRootDecoree());
  }

  protected String getCollapseImage() {
    return "drill-position-collapse";
  }

  protected String getExpandImage() {
    return "drill-position-expand";
  }

  protected String getOtherImage() {
    return "drill-position-other";
  }

}
