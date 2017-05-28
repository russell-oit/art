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
package net.sf.jpivotart.jpivot.navigator;

import java.util.Locale;
import javax.servlet.jsp.JspException;

import net.sf.jpivotart.jpivot.olap.model.OlapModel;
import net.sf.wcfart.wcf.component.Component;
import net.sf.wcfart.wcf.component.ComponentTag;
import net.sf.wcfart.wcf.controller.RequestContext;
import org.apache.commons.lang3.LocaleUtils;

/**
 * Created on 09.12.2002
 * 
 * @author av
 */
public class NavigatorTag extends ComponentTag {

  String query;
  private String localeString;

  /**
   * creates the navigator component
   */
  public Component createComponent(RequestContext context) throws JspException {
	  if(localeString!=null){
		  Locale locale=LocaleUtils.toLocale(localeString);
		  context.setLocale(locale);
	  }
    OlapModel olapModel = (OlapModel) context.getModelReference(query);
    if (olapModel == null)
      throw new JspException("query \"" + query + "\" not found");
    return new Navigator(getId(), null, olapModel);
  }

  /**
   * Returns the query.
   * @return String
   */
  public String getQuery() {
    return query;
  }

  /**
   * Sets the query.
   * @param query The query to set
   */
  public void setQuery(String query) {
    this.query = query;
  }

	/**
	 * @return the localeString
	 */
	public String getLocaleString() {
		return localeString;
	}

	/**
	 * @param localeString the localeString to set
	 */
	public void setLocaleString(String localeString) {
		this.localeString = localeString;
	}

}
