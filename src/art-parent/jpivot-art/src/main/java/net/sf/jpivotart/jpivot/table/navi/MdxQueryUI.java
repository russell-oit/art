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

import net.sf.jpivotart.jpivot.olap.navi.MdxQuery;
import net.sf.jpivotart.jpivot.table.TableComponentExtensionSupport;
import net.sf.wcfart.wcf.format.FormatException;

/**
 * @author av
 */
public class MdxQueryUI extends TableComponentExtensionSupport {
  public static final String ID = "mdxQuery";

  public String getId() {
    return ID;
  }

  public boolean isAvailable() {
    return getExtension() != null;
  }

  MdxQuery getExtension() {
    return (MdxQuery) table.getOlapModel().getExtension(MdxQuery.ID);
  }

  public String getMdxQuery() {
    MdxQuery ext = getExtension();
    if (ext == null)
      return "";
    return ext.getMdxQuery();
  }

  public void setMdxQuery(String mdxQuery) throws FormatException {
    MdxQuery ext = getExtension();
    if (ext == null)
      return;
    ext.setMdxQuery(mdxQuery);
  }

}
