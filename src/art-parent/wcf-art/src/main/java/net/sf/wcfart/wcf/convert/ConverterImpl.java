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
package net.sf.wcfart.wcf.convert;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import net.sf.wcfart.wcf.format.FormatException;
import net.sf.wcfart.wcf.format.Formatter;
import net.sf.wcfart.wcf.ui.XoplonCtrl;
import net.sf.wcfart.wcf.utils.DomUtils;
import net.sf.wcfart.wcf.utils.SoftException;
import net.sf.wcfart.wcf.utils.XmlUtils;
import net.sf.wcfart.wcf.utils.XoplonNS;
import org.apache.commons.fileupload.FileItem;

/**
 * implements the converter
 *
 * @author av
 */
public class ConverterImpl implements Converter {

  HashMap<String, NodeConverter> handlers = new HashMap<>();
  Formatter formatter;
  FormatException formatException = null;
  private static Logger logger = Logger.getLogger(ConverterImpl.class);


  public Formatter getFormatter() {
    return formatter;
  }

  public void setFormatter(Formatter formatter) {
    this.formatter = formatter;
  }

  public void addHandler(NodeConverter nc) {
    handlers.put(nc.getElementName(), nc);
  }

  /* ---------------------- convert from request to DOM Tree and bean ----------------------- */

  public void validate(Map<String, String[]> params, Map<String, FileItem[]> fileParams, Node node, Object bean) throws ConvertException, FormatException {
    try {
      formatException = null;
      Document root = XmlUtils.getDocument(node);
      traverse(node, params, fileParams, bean);
      if (formatException != null)
        throw formatException;
    } catch (IllegalAccessException e) {
      logger.error("?", e);
      throw new SoftException(e);
    } catch (NoSuchMethodException e) {
      logger.error("?", e);
      throw new SoftException(e);
    }
  }

  void traverse(Node node, Map<String, String[]> params, Map<String, FileItem[]> fileParams, Object bean)
    throws ConvertException, IllegalAccessException, NoSuchMethodException {
    if (node.getNodeType() == Node.ELEMENT_NODE) {
      String name = node.getNodeName();
      NodeConverter handler = handlers.get(name);
      Element elem = (Element)node;
      if (ignoreAll(elem))
        return;

      if (handler != null) {
        try {
          handler.convert(formatter, params, fileParams, elem, bean);
        } catch (FormatException e) {
          formatException = e;
          XoplonNS.setAttribute(elem, "error", formatException.getMessage());
        } catch (InvocationTargetException e) {
          Throwable te = e.getTargetException();
          if (te instanceof FormatException) {
            formatException = (FormatException) te;
            XoplonNS.setAttribute(elem, "error", formatException.getMessage());
          } else if (te instanceof IllegalArgumentException) {
            formatException = new FormatException(te.getMessage());
            XoplonNS.setAttribute(elem, "error", formatException.getMessage());
          }
          else {
            throw new SoftException(te);
          }
        }
      }
    }

    List<Node> children = DomUtils.getChildElements(node);
    for (Iterator<Node> it = children.iterator(); it.hasNext();) {
      Element child = (Element) it.next();
      traverse(child, params, fileParams, bean);
    }
  }

  private boolean ignoreAll(Element elem) {
    if (XoplonCtrl.isHidden(elem))
      return true;
    if ("true".equals(elem.getAttribute("children-hidden")))
      return true;
    return false;
  }

  /* ---------------------- convert from bean to DOM Tree ----------------------- */

  public void revert(Object bean, Node node) throws ConvertException {
    try {
      formatException = null;
      traverse(node, bean);
      if (formatException != null)
        throw formatException;
    } catch (IllegalAccessException e) {
      throw new SoftException(e);
    } catch (NoSuchMethodException e) {
      throw new SoftException(e);
    } catch (InvocationTargetException e) {
      throw new SoftException(e);
    }
  }

  void traverse(Node node, Object bean)
    throws ConvertException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    if (node.getNodeType() == Node.ELEMENT_NODE) {
      String name = node.getNodeName();
      Element elem = (Element)node;
      if (ignoreAll(elem))
        return;

      NodeConverter handler = handlers.get(name);
      if (handler != null) {
        DomUtils.removeAttribute(elem, "error");
        try {
          handler.convert(formatter, bean, elem);
        } catch (FormatException e) {
          formatException = e;
        }
      }
    }

    List<Node> children = DomUtils.getChildElements(node);
    for (Iterator<Node> it = children.iterator(); it.hasNext();) {
      Element child = (Element) it.next();
      traverse(child, bean);
    }

  }

}
