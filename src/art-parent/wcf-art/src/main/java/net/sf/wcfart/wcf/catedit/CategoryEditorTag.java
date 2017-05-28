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
package net.sf.wcfart.wcf.catedit;

import javax.servlet.jsp.JspException;

import net.sf.wcfart.wcf.component.Component;
import net.sf.wcfart.wcf.component.ComponentTag;
import net.sf.wcfart.wcf.controller.RequestContext;

/**
 * Created on 20.12.2002
 * 
 * @author av
 */
public class CategoryEditorTag extends ComponentTag {
	
	private static final long serialVersionUID = 1L;
	
  String model;

  
  public Component createComponent(RequestContext context) throws JspException {
    CategoryModel cm;
    if (model != null)
      cm = (CategoryModel) context.getModelReference(getModel());
    else
      cm = new TestCategoryModel();
    return new CategoryEditor(getId(), null, cm);
  }

  /**
   * Returns the model.
   * @return String
   */
  public String getModel() {
    return model;
  }

  /**
   * Sets the model.
   * @param model The model to set
   */
  public void setModel(String model) {
    this.model = model;
  }

}
