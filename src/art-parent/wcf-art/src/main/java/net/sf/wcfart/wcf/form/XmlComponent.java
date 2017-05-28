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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.jaxen.JaxenException;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import net.sf.wcfart.wcf.component.Component;
import net.sf.wcfart.wcf.component.ComponentSupport;
import net.sf.wcfart.wcf.controller.RequestContext;
import net.sf.wcfart.wcf.ui.XoplonCtrl;
import net.sf.wcfart.wcf.utils.DomUtils;
import net.sf.wcfart.wcf.utils.NoWarn;
import net.sf.wcfart.wcf.utils.SoftException;

/**
 * Implementation of a Comonent with support for NodeHandlers. The DOM Document
 * is kept in memory.
 * <p>
 * The DOM may contain Elements with a handler attribute that contain the class
 * names of NodeHandlers. Example
 * <pre>
 *   &lt;myelem&gt;
 *     &lt;my-other-elem handler="my.pkg.ClassName"/&gt;
 *   &lt;/myelem&gt;
 * </pre>
 * XmlComponent will create an instance of my.pkg.ClassName and call its initialize() 
 * method with the my-other-elem Element as parameter. The NodeHandler may then choose
 * to register itself as FormListener or RequestListener with the Environment.
 * 
 * @author av
 */
public class XmlComponent extends ComponentSupport {
  private static Logger logger = Logger.getLogger(XmlComponent.class);
  Document document;
  Map<Node, NodeHandler> elementHandlerMap;
  Map<String, NodeHandler> idHandlerMap;
  Map<NodeHandler, Element> handlerElementMap;

  /**
   * creates a Component
	 * @param id id
	 * @param parent parent
   * @param document the document to use. 
   */
  public XmlComponent(String id, Component parent, Document document) {
    super(id, parent);
    this.document = document;
  }

  public void initialize(RequestContext context) throws Exception {
    super.initialize(context);
    hideSecuredElements(context, document);
    installNodeHandlers(context, document);
  }

  public void destroy(HttpSession session) throws Exception {
    for (NodeHandler nh : elementHandlerMap.values()) {
      nh.destroy(session);
    }
    super.destroy(session);
  }

  /**
   * @see net.sf.wcfart.wcf.component.Component#render(RequestContext)
   */
  public Document render(RequestContext context) throws Exception {
    for (NodeHandler nh : elementHandlerMap.values()) {
      nh.render(context);
    }
    return document;
  }

  /**
   * an object that is attached to a DOM Element.
   * <pre>
   *   &lt;myelem&gt;
   *     &lt;my-other-elem handler="my.pkg.ClassName"/&gt;
   *   &lt;/myelem&gt;
   * </pre>
   * install() will create an instance of my.pkg.ClassName and call its initialize() 
   * method with the my-other-elem Element as parameter.
   * <p />
   * 
   * @return Map maps the Elements to the created NodeHandlers (i.e. map.get(Element) returns NodeHandler)
   */
  void installNodeHandlers(RequestContext context, Document root) throws Exception {
    try {
      elementHandlerMap = new HashMap<>();
      handlerElementMap = new HashMap<>();
      idHandlerMap = new HashMap<>();
      List<Element> elements = findHandlerElements(root);
      for (Element elem : elements) {
        String clazz = elem.getAttribute("handler");
        NodeHandler nh = (NodeHandler) Class.forName(clazz).newInstance();
        nh.initialize(context, this, elem);
        elementHandlerMap.put(elem, nh);
        handlerElementMap.put(nh, elem);
        idHandlerMap.put(elem.getAttribute("id"), nh);
      }
    } catch (JaxenException e) {
      logger.error(null, e);
      throw new SoftException(e);
    } catch (InstantiationException e) {
      logger.error(null, e);
      throw new SoftException(e);
    } catch (IllegalAccessException e) {
      logger.error(null, e);
      throw new SoftException(e);
    } catch (ClassNotFoundException e) {
      logger.error(null, e);
      throw new SoftException(e);
    }
  }

  private List<Element> findHandlerElements(Document root) throws JaxenException {
    installDefaultHandler(root, "//xtree", "net.sf.wcfart.wcf.tree.TreeHandler");
    installDefaultHandler(root, "//xtable", "net.sf.wcfart.wcf.table.TableHandler");
    installDefaultHandler(root, "//xtabbed", "net.sf.wcfart.wcf.tabbed.TabbedHandler");
    installDefaultHandler(root, "//button", "net.sf.wcfart.wcf.form.ButtonHandler");
    installDefaultHandler(root, "//imgButton", "net.sf.wcfart.wcf.form.ButtonHandler");
    
    DOMXPath xp = new DOMXPath("//*[@handler]");
	
	List<Element> elements = NoWarn.castList(xp.selectNodes(root));
    return elements;
  }

  private void installDefaultHandler(Document root, String xpath, String clazz) throws JaxenException {
    DOMXPath xp = new DOMXPath(xpath);
    List<Element> list = NoWarn.castList(xp.selectNodes(root));
    for (Element e : list) {
      if (e.getAttribute("handler").length() == 0 && e.getAttribute("actionReference").length() == 0)
        e.setAttribute("handler", clazz);
    }
  }
  
  /**
   * sets the hidden attribute of elements that have a role attribute.
   */
  void hideSecuredElements(RequestContext context, Document root) throws Exception {
    try {
      DOMXPath xp = new DOMXPath("//*[@role]");
      List<Element> elements = NoWarn.castList(xp.selectNodes(root));
      for (Element elem : elements) {
        String role = elem.getAttribute("role");
        if (context.isUserInRole(role))
          XoplonCtrl.setHidden(elem, false); // DomUtils.removeAttribute(elem, "hidden");
        else
          XoplonCtrl.setHidden(elem, true); // elem.setAttribute("hidden", "true");
      }
    } catch (JaxenException e) {
      logger.error(null, e);
      throw new SoftException(e);
    }
  }

  /**
   * returns NodeHandler of the parent DOM Element
	 * @param handler handler
	 * @return NodeHandler of the parent DOM Element
   */
  public NodeHandler getParent(NodeHandler handler) {
    Element elem = handlerElementMap.get(handler);
    Node node = elem.getParentNode();
    while (node.getNodeType() == Node.ELEMENT_NODE) {
      NodeHandler h = elementHandlerMap.get(node);
      if (h != null)
        return h;
      node = node.getParentNode();
    }
    return null;
  }

  List<NodeHandler> getHandlers(NodeHandler rootHandler, String xpathExpr) {
    try {
      List<NodeHandler> handlerList = new ArrayList<>();
      DOMXPath xp = new DOMXPath(xpathExpr);
      Element rootElem = handlerElementMap.get(rootHandler);
      List<Element> elements = NoWarn.castList(xp.selectNodes(rootElem));
      for (Element element : elements) {
        NodeHandler handler = elementHandlerMap.get(element);
        if (handler != null)
          handlerList.add(handler);
      }
      return handlerList;
    } catch (JaxenException e) {
      logger.error(null, e);
      throw new SoftException(e);
    }
  }

  /**
   * get children
   * 
   * @param handler handler
   * @return children
   */
  public List<NodeHandler> getChildren(NodeHandler handler) {
    return getHandlers(handler, ".//*[@handler]");
  }

  /**
   * returns the current DOM
	 * @return the current DOM
   */
  public Document getDocument() {
    return document;
  }

  /**
   * return the NodeHandler associated with element or null, if there is no such handler
   * @param element element
   * @return NodeHandler the NodeHandler associated with element or null, if there is no such handler
   */
  public NodeHandler getHandler(Element element) {
    return elementHandlerMap.get(element);
  }

  /**
   * return the NodeHandler whose element contains an Attribute "id"
   * @param id value of the id attribute
   * @return NodeHandler associated with that element or null
   */
  public NodeHandler getHandler(String id) {
    return idHandlerMap.get(id);
  }

  /**
   * return the Element that NodeHandler associated with, or null if there is no such element
   * @param handler handler
   * @return NodeHandler
   */
  public Element getElement(NodeHandler handler) {
    return handlerElementMap.get(handler);
  }

  /**
   * get element
   * @param id id
   * @return element
   */
  public Element getElement(String id) {
    return DomUtils.findElementWithId(id, document.getDocumentElement());
  }

}
