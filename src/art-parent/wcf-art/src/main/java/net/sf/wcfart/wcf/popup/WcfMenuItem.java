/*
 * Copyright (c) 1971-2003 TONBELLER AG, Bensheim.
 * All rights reserved.
 */
package net.sf.wcfart.wcf.popup;

import net.sf.wcfart.wcf.controller.Dispatcher;
import net.sf.wcfart.wcf.controller.RequestListener;
import net.sf.wcfart.wcf.utils.DomUtils;

/**
 * WcfMenuItem ist ein MenuItem, das sich bei der Konstruktion
 * an einem RequestDispatcher anmeldet.
 * 
 * @author av
 * @since Mar 14, 2006
 */
public abstract class WcfMenuItem extends MenuItemSupport implements RequestListener {

  public WcfMenuItem(Dispatcher d, String label) {
    this(d, label, null);
  }

  public WcfMenuItem(Dispatcher d, String label, String image) {
    super(null, label, image);
    String id = DomUtils.randomId();
    super.setHref("?" + id + "=x");
    d.addRequestListener(id, null, this);
  }
}
