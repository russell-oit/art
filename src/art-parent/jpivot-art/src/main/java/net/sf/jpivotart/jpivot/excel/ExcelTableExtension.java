/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * Copyright (C) 2003-2004 TONBELLER AG.
 * All Rights Reserved.
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package net.sf.jpivotart.jpivot.excel;

import net.sf.jpivotart.jpivot.table.TableComponent;
import net.sf.jpivotart.jpivot.table.TableComponentExtensionSupport;
import net.sf.wcfart.wcf.controller.RequestContext;

/**
 */
public class ExcelTableExtension extends TableComponentExtensionSupport {
  public static final String ID = "excel";

  public String getId() {
    return ID;
  }

  public void initialize(RequestContext context, TableComponent table) throws Exception {
    super.initialize(context, table);
    ExcelCellBuilderDecorator cbd = new ExcelCellBuilderDecorator(table.getCellBuilder());
    table.setCellBuilder(cbd);
  }
  
}