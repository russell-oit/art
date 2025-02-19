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

import javax.servlet.jsp.JspException;

import net.sf.wcfart.wcf.component.Component;
import net.sf.wcfart.wcf.controller.RequestContext;

/**
 * Created on 06.01.2003
 * 
 * @author av
 */
public class FormButtonTag extends ToolButtonTag {
	private static final long serialVersionUID = 1L;
	
  String action;
  String forward;
  String form;
  
  /**
   * @see net.sf.wcfart.wcf.toolbar.ToolButtonTag#getToolButtonModel(RequestContext)
   */
  protected ToolButtonModel getToolButtonModel(RequestContext context) throws JspException {
  	Component f = (Component)context.getModelReference(form);
  	if (f == null)
  	  throw new JspException("could not find form " + form);
    FormButtonModel fm = new FormButtonModel(f);
    fm.setAction(action);
    fm.setForward(forward);
    return fm;
  }

  /**
   * Sets the action.
   * @param action The action to set
   */
  public void setAction(String action) {
    this.action = action;
  }

  /**
   * Sets the forward.
   * @param forward The forward to set
   */
  public void setForward(String forward) {
    this.forward = forward;
  }

  /**
   * @param string
   */
  public void setForm(String string) {
    form = string;
  }

}
