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
package net.sf.jpivotart.jpivot.mondrian;

import net.sf.jpivotart.jpivot.core.ExtensionSupport;
import net.sf.jpivotart.jpivot.olap.model.OlapException;
import net.sf.jpivotart.jpivot.olap.navi.MdxQuery;
import net.sf.wcfart.wcf.format.FormatException;

/**
 * Created on 19.12.2002
 * 
 * @author av
 */
public class MondrianMdxQuery extends ExtensionSupport implements MdxQuery {

  public MondrianMdxQuery() {
    super.setId(MdxQuery.ID);
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.navi.MdxQuery#getMdxQuery()
   */
  public String getMdxQuery() {
    MondrianModel m = (MondrianModel) getModel();
    return m.getCurrentMdx();
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.navi.MdxQuery#setMdxQuery(String)
   */
  public void setMdxQuery(String mdxQuery) {
    try {
      MondrianModel m = (MondrianModel) getModel();
      if (m.setUserMdx(mdxQuery))
        m.fireModelChanged(); // only if changed
    } catch (OlapException e) {
      throw new FormatException(e.getMessage());
    }
  }

} // End MondrianMdxQuery
