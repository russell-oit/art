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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;

import javax.servlet.jsp.JspException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import net.sf.wcfart.wcf.component.Component;
import net.sf.wcfart.wcf.component.ComponentTag;
import net.sf.wcfart.wcf.controller.RequestContext;
import net.sf.wcfart.wcf.utils.ResourceLocator;
import net.sf.wcfart.wcf.utils.XmlUtils;
import net.sf.wcfart.wcf.wizard.WizardComponent;
import net.sf.wcfart.wcf.wizard.WizardComponentTag;

/**
 * creates a FormComponent
 *
 * @author av
 */
public class FormComponentTag extends ComponentTag {
	
	private static final long serialVersionUID = 1L;

  String xmlUri;
  String model;
  boolean bookmarkable = false;
  boolean finishButton = true;
  String bundle;
  private Locale locale;

  private static Logger logger = Logger.getLogger(FormComponentTag.class);

  /**
   * loads a form from an xml file and registeres it with the controller.
   */
  public Component createComponent(RequestContext context) throws JspException {
    try {
		
		if(locale!=null){
			context.setLocale(locale);
		}

      Document doc = parseDocument(context, getXmlUri());

      // find the bean model
      Object bean = null;
      if (model != null)
        bean = context.getModelReference(model);

      // create the component
      FormComponent fc = createFormComponent(context, id, doc, bean);
      fc.setBookmarkable(bookmarkable);
      fc.setFinishButton(finishButton);

      registerWithWizard(fc);
      return fc;

    } catch (MalformedURLException e) {
      logger.error(null, e);
      throw new JspException(e);
    }
  }

  /**
   * if this is used inside a wizard tag, then its registered with the wizard.
   */
  private void registerWithWizard(FormComponent fc) {
    WizardComponentTag wt = (WizardComponentTag) findAncestorWithClass(this,
        WizardComponentTag.class);
    if (wt == null)
      return;
    WizardComponent wc = (WizardComponent) wt.getComponent();
    wc.addPage(fc);
  }

  protected FormComponent createFormComponent(RequestContext context, String id, Document doc,
      Object bean) {
    return new FormComponent(id, null, doc, bean);
  }

  protected Document parseDocument(RequestContext context, String xmlUri)
      throws MalformedURLException, MissingResourceException {

    Locale loc = context.getLocale(); // Default: browser setting
    URL url = ResourceLocator.getResource(context.getServletContext(), loc, xmlUri);

    Document document = XmlUtils.parse(url); 
    //In replaceI18n(...) wird gepr�ft, ob "bundle"-Attribut vorhanden
    FormDocument.replaceI18n(context, document, getBundle());

    return document;
  }

  public String getXmlUri() {
    return xmlUri;
  }

  public void setXmlUri(String xmlUri) {
    this.xmlUri = xmlUri;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public void setBookmarkable(boolean b) {
    bookmarkable = b;
  }

  public void setBundle(String bundle) {
    this.bundle = bundle;
  }

  public boolean isFinishButton() {
    return finishButton;
  }

  public void setFinishButton(boolean finishButton) {
    this.finishButton = finishButton;
  }

  public String getBundle() {
    return bundle;
  }

	/**
	 * @return the locale
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @param locale the locale to set
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

}
