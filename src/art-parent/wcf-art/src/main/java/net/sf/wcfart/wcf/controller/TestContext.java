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
package net.sf.wcfart.wcf.controller;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.apache.commons.fileupload.FileItem;

import net.sf.wcfart.tbutils.res.Resources;
import net.sf.wcfart.wcf.convert.Converter;
import net.sf.wcfart.wcf.convert.ConverterFactory;
import net.sf.wcfart.wcf.expr.ExprContext;
import net.sf.wcfart.wcf.expr.ExprUtils;
import net.sf.wcfart.wcf.format.Formatter;
import net.sf.wcfart.wcf.format.FormatterFactory;

/**
 * Created on 28.11.2002
 *
 * @author av
 */
public class TestContext extends RequestContext {

  Formatter formatter = FormatterFactory.instance(getLocale());
  Converter converter = ConverterFactory.instance(formatter);
  ExprContext exprContext;
  Map<String, String[]> parameters = new HashMap<>();
  HttpSession session = new TestSession();

  /**
   * @see net.sf.wcfart.wcf.controller.RequestContext#getRequest()
   */
  public HttpServletRequest getRequest() {
    return null;
  }

  /**
   * @see net.sf.wcfart.wcf.controller.RequestContext#getResponse()
   */
  public HttpServletResponse getResponse() {
    return null;
  }

  /**
   * @see net.sf.wcfart.wcf.controller.RequestContext#getServletContext()
   */
  public ServletContext getServletContext() {
    return null;
  }

  /**
   * @see net.sf.wcfart.wcf.controller.RequestContext#getSession()
   */
  public HttpSession getSession() {
    return session;
  }

  /**
   * @see net.sf.wcfart.wcf.controller.RequestContext#getConverter()
   */
  public Converter getConverter() {
    return converter;
  }

  /**
   * @see net.sf.wcfart.wcf.controller.RequestContext#getFormatter()
   */
  public Formatter getFormatter() {
    return formatter;
  }

  public Map<String, String[]> getParameters() {
    return parameters;
  }

  public void setParameters(Map<String, String[]> map) {
    this.parameters = map;
  }

  public String[] getParameters(String name) {
    return parameters.get(name);
  }

  public String getParameter(String name) {
    String[] values = getParameters(name);
    if (values != null && values.length > 0)
      return values[0];
    return null;
  }

  public Locale getLocale() {
    return Locale.US;
  }

  /**
   * Returns the exprContext.
   * @return ExprContext
   */
  public ExprContext getExprContext() {
    return exprContext;
  }

  /**
   * Sets the exprContext.
   * @param exprContext The exprContext to set
   */
  public void setExprContext(ExprContext exprContext) {
    this.exprContext = exprContext;
  }

  /**
   * @see net.sf.wcfart.wcf.controller.RequestContext#getModelReference(String)
   */
  public Object getModelReference(String expr) {
    return ExprUtils.getModelReference(exprContext, expr);
  }

  /**
   * @see net.sf.wcfart.wcf.controller.RequestContext#setModelReference(String, Object)
   */
  public void setModelReference(String expr, Object value) {
    ExprUtils.setModelReference(exprContext, expr, value);
  }

  public boolean isUserInRole(String roleExpr) {
    return false;
  }

  public Resources getResources() {
    return Resources.instance(getLocale());
  }

  public Resources getResources(String bundleName) {
    return Resources.instance(getLocale(), bundleName);
  }

  public Resources getResources(Class<?> clasz) {
    return Resources.instance(getLocale(), clasz);
  }

  public String getRemoteUser() {
    return "guest";
  }

  public String getRemoteDomain() {
    return null;
  }

  public boolean isAdmin() {
    return false;
  }
  public class TestSession implements HttpSession {
    HashMap<String, Object> attrs = new HashMap<>();

    public long getCreationTime() {
      return 0;
    }

    public String getId() {
      return "testSessionID";
    }

    public long getLastAccessedTime() {
      return 0;
    }

    public ServletContext getServletContext() {
      return null;
    }

    public void setMaxInactiveInterval(int arg0) {
    }

    public int getMaxInactiveInterval() {
      return 0;
    }

    /** @deprecated */
	@Deprecated
    public javax.servlet.http.HttpSessionContext getSessionContext() {
      return null;
    }

    public Object getAttribute(String id) {
      return attrs.get(id);
    }

    /** @deprecated */
	@Deprecated
    public Object getValue(String id) {
      return getAttribute(id);
    }

    public Enumeration<String> getAttributeNames() {
      Vector<String> v = new Vector<>();
      v.addAll(attrs.keySet());
      return v.elements();
    }

    /** @deprecated */
	@Deprecated
    public String[] getValueNames() {
      return attrs.keySet().toArray(new String[0]);
    }

    public void setAttribute(String id, Object att) {
      removeAttribute(id);
      attrs.put(id, att);
      if (att instanceof HttpSessionBindingListener) {
        HttpSessionBindingEvent e = new HttpSessionBindingEvent(this, id);
        ((HttpSessionBindingListener)att).valueBound(e);
      }
    }

    /** @deprecated */
	@Deprecated
    public void putValue(String id, Object attr) {
      setAttribute(id, attr);
    }

    public void removeAttribute(String id) {
      Object attr = attrs.get(id);
      if (attr instanceof HttpSessionBindingListener) {
        HttpSessionBindingEvent e = new HttpSessionBindingEvent(this, id);
        ((HttpSessionBindingListener)attr).valueUnbound(e);
      }
    }

    /** @deprecated */
	@Deprecated
    public void removeValue(String id) {
      removeAttribute(id);
    }

    public void invalidate() {
    }

    public boolean isNew() {
      return false;
    }
  }

  public Object findBean(String name) {
    return exprContext.findBean(name);
  }

  public void setBean(String name, Object bean) {
    exprContext.setBean(name, bean);
  }

  public void setLocale(Locale locale) {
  }

  public FileItem getFileItem(String name) {
    return null;
  }

  public Map<String, FileItem[]> getFileParameters() {
    return null;
  }
}
