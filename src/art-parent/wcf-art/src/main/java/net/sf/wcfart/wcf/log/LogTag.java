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

import java.net.URL;

import javax.servlet.jsp.JspException;

import org.w3c.dom.Document;

import net.sf.wcfart.wcf.component.Component;
import net.sf.wcfart.wcf.component.ComponentTag;
import net.sf.wcfart.wcf.controller.RequestContext;
import net.sf.wcfart.wcf.form.FormDocument;
import net.sf.wcfart.wcf.utils.ResourceLocator;
import net.sf.wcfart.wcf.utils.XmlUtils;

public class LogTag extends ComponentTag {
	
	private static final long serialVersionUID = 1L;

  String xmlUri;
  String logDir;

  /**
   * loads a form from an xml file and registeres it with the controller.
   */
  public Component createComponent(RequestContext context) throws JspException {
    try {

      // parse the form xml
      URL url =
        ResourceLocator.getResource(context.getServletContext(), context.getLocale(), xmlUri);
      Document doc = XmlUtils.parse(url);

      //In replaceI18n(...) wird gepr�ft, ob "bundle"-Attribut vorhanden
      FormDocument.replaceI18n(context, doc, null);

      // create the component
      return new LogForm(id, null, doc, logDir);
    }
    catch (Exception e) {
      throw new JspException(e);
    }
  }

  public void setXmlUri(String xmlUri) {
    this.xmlUri = xmlUri;
  }

  /**
   * Sets the logDir.
   * @param logDir The logDir to set
   */
  public void setLogDir(String logDir) {
    this.logDir = logDir;
  }

}
