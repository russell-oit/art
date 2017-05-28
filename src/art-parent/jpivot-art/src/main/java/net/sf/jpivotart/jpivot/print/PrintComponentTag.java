/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * Copyright (C) 2003-2004 TONBELLER AG.
 * All Rights Reserved.
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package net.sf.jpivotart.jpivot.print;

import net.sf.wcfart.wcf.component.Component;
import net.sf.wcfart.wcf.component.ComponentTag;
import net.sf.wcfart.wcf.controller.RequestContext;


/**
 * creates a ChartComponent
 * @author Robin Bagot
 */
public class PrintComponentTag extends ComponentTag {
	String query;

  /**
   * creates a Print Component
   */
  public Component createComponent(RequestContext context) throws Exception {
	return new PrintComponent(id, context);
  }
  
}
