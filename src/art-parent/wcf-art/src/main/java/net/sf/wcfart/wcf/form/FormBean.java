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

import net.sf.wcfart.wcf.controller.RequestContext;


/**
 * allows the Bean of a FormBean to access its form.
 */
public interface FormBean {
  void setFormComponent(RequestContext context, FormComponent form);
}
