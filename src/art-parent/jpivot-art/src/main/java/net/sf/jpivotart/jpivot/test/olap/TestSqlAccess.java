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

import javax.sql.DataSource;

import net.sf.jpivotart.jpivot.core.ExtensionSupport;
import net.sf.jpivotart.jpivot.olap.model.Member;
import net.sf.jpivotart.jpivot.olap.model.Property;
import net.sf.jpivotart.jpivot.param.SqlAccess;
import net.sf.wcfart.wcf.param.SessionParam;

/**
 * @author av
 */
public class TestSqlAccess extends ExtensionSupport implements SqlAccess  {

  public DataSource getDataSource() {
    return null;
  }

  public SessionParam createParameter(Member m, String paramName) {
    SessionParam p = new SessionParam();
    p.setDisplayName(m.getLevel().getLabel());
    p.setDisplayValue(m.getLabel());
    p.setMdxValue(m.getLabel());
    p.setName(paramName);
    p.setSqlValue(m.getLabel());
    return p;
  }

  public SessionParam createParameter(Member m, String paramName, String propertyName) {
    SessionParam p = new SessionParam();
    p.setDisplayName(m.getLevel().getLabel());
    p.setDisplayValue(m.getLabel());
    p.setMdxValue(m.getLabel());
    p.setName(paramName);
    Property prop = m.getProperty(propertyName);
    p.setSqlValue(prop.getValue());
    return p;
  }

}
