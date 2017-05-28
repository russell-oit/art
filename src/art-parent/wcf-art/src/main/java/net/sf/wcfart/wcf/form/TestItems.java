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
package net.sf.wcfart.wcf.form;

import org.w3c.dom.Element;

import net.sf.wcfart.wcf.component.FormListener;
import net.sf.wcfart.wcf.controller.RequestContext;
import net.sf.wcfart.wcf.ui.ListItem;
import net.sf.wcfart.wcf.ui.Select;
import net.sf.wcfart.wcf.utils.DomUtils;

/**
 * Example of dynamic list items
 * @author av
 */
public class TestItems extends NodeHandlerSupport implements FormListener {
  String id = DomUtils.randomId();
  /**
   * creates a few items
	 * @param context context
   */
  public void revert(RequestContext context) {
    Element list = getElement();
    Select.removeAllItems(list);
    for (int i = 0; i < 10; i++) {
      Element item = ListItem.addListItem(list);
      ListItem.setId(item, id + "." + i);
      ListItem.setValue(item, "" + i);
      ListItem.setLabel(item, "item " + (i + 1));
    }
  }


  /**
   * always true
	 * @param context context
   */
  public boolean validate(RequestContext context) {
    return true;
  }

  /**
   * @see net.sf.wcfart.wcf.form.NodeHandler#initialize(RequestContext, XmlComponent, Element)
   */
  public void initialize(RequestContext context, XmlComponent comp, Element element) throws Exception {
    super.initialize(context, comp, element);
    comp.addFormListener(this);
    revert(context);
  }

}
