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
package net.sf.wcfart.wcf.log;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.w3c.dom.Element;

import net.sf.wcfart.wcf.component.FormListener;
import net.sf.wcfart.wcf.controller.RequestContext;
import net.sf.wcfart.wcf.form.NodeHandlerSupport;
import net.sf.wcfart.wcf.form.XmlComponent;
import net.sf.wcfart.wcf.ui.ListItem;
import net.sf.wcfart.wcf.ui.Select;

public class LogItems extends NodeHandlerSupport implements FormListener {

  LogHandler handler;
  ResourceBundle res;

  /**
   * creates a few items
   */
  public void revert(RequestContext context) {
    Element list = getElement();
    Select.removeAllItems(list);

    String confs[] = handler.getConfigNames();

    for (int i = 0; i < confs.length; i++) {
      Element item = ListItem.addListItem(list);
      ListItem.setId(item, String.valueOf(confs[i].hashCode())/*DomUtils.randomId()*/);
      ListItem.setValue(item, confs[i]);

      String label = handler.getLabel(confs[i]);
      if (label == null) {
        try {
          label = res.getString(confs[i]);
        } catch (MissingResourceException e) {
          label = confs[i];
        }
      }

      ListItem.setLabel(item, label);
    }
  }

  /**
   * always true
   */
  public boolean validate(RequestContext context) {
    return true;
  }

  public void initialize(RequestContext context, XmlComponent comp, Element element)
      throws Exception {
    super.initialize(context, comp, element);

    res = ResourceBundle.getBundle("net.sf.wcfart.wcf.log.logging", context.getLocale());

    comp.addFormListener(this);

    LogForm form = (LogForm) comp;
    handler = form.getLogHandler();

    revert(context);
  }

}
