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
package net.sf.wcfart.wcf.tree;

import javax.servlet.jsp.JspException;

import net.sf.wcfart.wcf.component.Component;
import net.sf.wcfart.wcf.component.ComponentTag;
import net.sf.wcfart.wcf.controller.RequestContext;
import net.sf.wcfart.wcf.selection.SelectionModel;

/**
 * TreeComponent
 * @author av
 */
public class TreeComponentTag extends ComponentTag {
	
	private static final long serialVersionUID = 1L;
	
  String model, selectionModel;
  
  public Component createComponent(RequestContext context) throws JspException {
    // find the model
    if (model != null) {
      TreeModel tm = (TreeModel)context.getModelReference(model);
      if (tm == null)
        throw new JspException("model \"" + model + "\" not found");
      TreeComponent tc = new TreeComponent(getId(), null, tm);
      if (selectionModel != null) {
        SelectionModel sm = (SelectionModel)context.getModelReference(selectionModel);
        tc.setSelectionModel(sm);
      }
      return tc;
    }
    
    // create testdata
    TestTreeModel ttm = new TestTreeModel();
    MutableTreeModelDecorator mtmd = new MutableTreeModelDecorator(ttm);
    TreeComponent tc = new TreeComponent(id, null, mtmd);
    tc.setDeleteNodeModel(ttm.getDeleteNodeModel());
    return tc;
  }

  /**
   * Sets the model.
   * @param model The model to set
   */
  public void setModel(String model) {
    this.model = model;
  }

  /**
   * Sets the selectionModel.
   * @param selectionModel The selectionModel to set
   */
  public void setSelectionModel(String selectionModel) {
    this.selectionModel = selectionModel;
  }
}
