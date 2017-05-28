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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.sf.wcfart.wcf.component.RenderListener;
import net.sf.wcfart.wcf.controller.RequestContext;
import net.sf.wcfart.wcf.controller.RequestListener;

/**
 * creates the item DOM Element node
 * 
 * @author av
 */
public interface ItemElementRenderer extends RequestListener, CategoryModelChangeListener, RenderListener {
  Element render(RequestContext context, Document factory, Category cat, Item item);
}
