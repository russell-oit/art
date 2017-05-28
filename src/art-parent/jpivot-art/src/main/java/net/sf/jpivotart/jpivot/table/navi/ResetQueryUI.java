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

import net.sf.jpivotart.jpivot.olap.navi.ResetQuery;
import net.sf.jpivotart.jpivot.table.TableComponent;
import net.sf.jpivotart.jpivot.table.TableComponentExtensionSupport;
import net.sf.wcfart.wcf.controller.RequestContext;

/**
 * Created on 06.12.2002
 * 
 * @author av
 */
public class ResetQueryUI extends TableComponentExtensionSupport {
  public static final String ID = "resetQuery";

  public String getId() {
    return ID;
  }

  public void initialize(RequestContext context, TableComponent table) throws Exception {
    super.initialize(context, table);
  }

  public boolean isAvailable() {
    return getExtension() != null;
  }

  public boolean isButtonPressed() {
    return false;
  }

  public void setButtonPressed(boolean value) {
    if (value && getExtension() != null)
      getExtension().reset();
  }

  ResetQuery getExtension() {
    return (ResetQuery) table.getOlapModel().getExtension(ResetQuery.ID);
  }

}
