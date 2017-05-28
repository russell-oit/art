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
package net.sf.jpivotart.jpivot.xmla;

import net.sf.jpivotart.jpivot.core.ExtensionSupport;
import net.sf.jpivotart.jpivot.olap.model.OlapException;
import net.sf.jpivotart.jpivot.olap.navi.MdxQuery;
import net.sf.wcfart.wcf.format.FormatException;

/**
 * set user defined MDX Query String
 */
public class XMLA_MdxQuery extends ExtensionSupport implements MdxQuery {

  public XMLA_MdxQuery() {
    super.setId(MdxQuery.ID);
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.navi.MdxQuery#getMdxQuery()
   */
  public String getMdxQuery() {
    XMLA_Model m = (XMLA_Model) getModel();
    return m.getCurrentMdx();
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.navi.MdxQuery#setMdxQuery(String)
   */
  public void setMdxQuery(String mdxQuery) {
    try {
      XMLA_Model m = (XMLA_Model) getModel();
      if (mdxQuery.equals(m.getCurrentMdx()))
        return;
      m.setUserMdx(mdxQuery);
      m.fireModelChanged();
    } catch (OlapException e) {
      throw new FormatException(e.getMessage());
    }
  }

 
} // End XMLA_MdxQuery
