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
package net.sf.wcfart.wcf.toolbar;

import java.util.Locale;
import javax.servlet.jsp.JspException;

import net.sf.wcfart.tbutils.res.Resources;
import net.sf.wcfart.wcf.component.Component;
import net.sf.wcfart.wcf.component.ComponentTag;
import net.sf.wcfart.wcf.controller.RequestContext;

/**
 * @author av
 */
public class ToolBarTag extends ComponentTag {
	
	private static final long serialVersionUID = 1L;
	
  String bundle;
  boolean globalButtonIds = false;
  private Locale locale;

  public void release() {
    globalButtonIds = false;
    bundle = null;
	locale = null;
    super.release();
  }  
  
  public ToolBar getToolBar() {
    return (ToolBar) super.getComponent();
  }

  public Component createComponent(RequestContext context) throws JspException {
    ToolBar tb = new ToolBar(getId(), null);
    tb.setGlobalButtonIds(globalButtonIds);
	if(locale!=null){
			context.setLocale(locale);
		}
    if (bundle != null) {
      Resources resb = context.getResources(bundle);
      tb.setBundle(resb);
    }
    return tb;
  }
  

  /**
   * @return bundle
   */
  public String getBundle() {
    return bundle;
  }

  /**
   * @param string
   */
  public void setBundle(String string) {
    bundle = string;
  }

  public void setGlobalButtonIds(boolean globalButtonIds) {
    this.globalButtonIds = globalButtonIds;
  }

	/**
	 * @return the locale
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @param locale the locale to set
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
}
