package net.sf.wcfart.wcf.form;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.w3c.dom.Document;

import net.sf.wcfart.tbutils.res.Resources;
import net.sf.wcfart.wcf.controller.RequestContext;
import net.sf.wcfart.wcf.utils.I18nReplacer;
import net.sf.wcfart.wcf.utils.ResourceLocator;
import net.sf.wcfart.wcf.utils.XmlUtils;

/**
 * Loads an xml document and replaces locale dependant labels 
 */
public class FormDocument {

  /**
   * Shortcut for replaceI18N(parseDocument())
   */
  public static Document loadDocument(RequestContext context, String bundle, String name) throws MalformedURLException, MissingResourceException {
    Document doc = parseDocument(context, name);
    replaceI18n(context, doc, bundle);
    return doc;
  }
  
  /**
   * replaces attr value "fmt:xxx" with string for key "xxx" from resource bundle.
   */
  public static void replaceI18n(RequestContext context, Document dom, String bundle)
      throws MissingResourceException {

    String bundleAttr = dom.getDocumentElement().getAttribute("bundle");
    if (bundleAttr.length() > 0) {
      bundle = bundleAttr;
    }

    if (bundle != null) {
      Locale loc = context.getLocale();
      ResourceBundle resb = ResourceBundle.getBundle(bundle, loc);
      I18nReplacer replacer = I18nReplacer.instance(resb);
      replacer.replaceAll(dom);
    } else {
      Resources res = context.getResources();
      I18nReplacer replacer = I18nReplacer.instance(res);
      replacer.replaceAll(dom);
    }
  }

  public static Document parseDocument(RequestContext context, String name) throws MalformedURLException,
      MissingResourceException {

    Locale loc = context.getLocale(); // Default: browser setting
    URL url = ResourceLocator.getResource(context.getServletContext(), loc, name);
    return XmlUtils.parse(url);
  }

}