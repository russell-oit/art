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
package net.sf.wcfart.wcf.component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.sf.wcfart.wcf.controller.RequestContext;

/**
 * keeps track of stylesheet parameters for the RendererTag. 
 * For the scopes 'request' (default), 
 * 'session' and 'application' keeps a Map of name/value pairs.
 * <p>
 * The special name "mode" may have values null, "excel", "print" and may
 * be queried by isExcelMode() and isPrintMode().
 */
public class RendererParameters {
  private static final String WEBKEY = RendererParameters.class.getName();

  public static final String MODE = "mode";
  public static final String EXCEL = "excel";
  public static final String PRINT = "print";

  /** navigation buttons are not rendered in excel/print modes */
  public static boolean isRenderActions(RequestContext context) {
    return isRenderActions(context.getRequest());
  }

  public static boolean isExcelMode(RequestContext context) {
    return isExcelMode(context.getRequest());
  }

  public static boolean isPrintMode(RequestContext context) {
    return isPrintMode(context.getRequest());
  }

  /** navigation buttons are not rendered in excel/print modes */
  public static boolean isRenderActions(HttpServletRequest request) {
    return getParameterMap(request).get(MODE) == null;
  }

  public static boolean isExcelMode(HttpServletRequest request) {
    return EXCEL.equals(getParameterMap(request).get(MODE));
  }

  public static boolean isPrintMode(HttpServletRequest request) {
    return PRINT.equals(getParameterMap(request).get(MODE));
  }
  
  public static void setParameter(HttpServletRequest req, String name, Object value, String scope) {
    Map<String, Object> map = getMap(req, scope, true);
    map.put(name, value);
  }

  public static void removeParameter(HttpServletRequest req, String name, Object value, String scope) {
    Map<String, Object> map = getMap(req, scope, false);
    if (map != null)
      map.remove(name);
  }

  private static Map<String, Object> getMap(HttpServletRequest req, String scope, boolean create) {
    if ("request".equals(scope))
      return getRequestMap(req, create);
    if ("session".equals(scope))
      return getSessionMap(req, create);
    if ("application".equals(scope))
      return getContextMap(req, create);
    throw new IllegalArgumentException("scope must be one of 'request', 'session', 'application'");

  }

  private static Map<String, Object> getRequestMap(HttpServletRequest req, boolean create) {
	  @SuppressWarnings("unchecked")
    Map<String, Object> map = (Map<String, Object>) req.getAttribute(WEBKEY);
    if (map != null || !create)
      return map;
    map = new HashMap<>();
    req.setAttribute(WEBKEY, map);
    return map;
  }

  private static Map<String, Object> getSessionMap(HttpServletRequest req, boolean create) {
    HttpSession session = req.getSession(true);
	@SuppressWarnings("unchecked")
    Map<String, Object> map = (Map<String, Object>) session.getAttribute(WEBKEY);
    if (map != null || !create)
      return map;
    map = new HashMap<>();
    session.setAttribute(WEBKEY, map);
    return map;
  }

  private static Map<String, Object> getContextMap(HttpServletRequest req, boolean create) {
    ServletContext context = req.getSession(true).getServletContext();
	@SuppressWarnings("unchecked")
    Map<String, Object> map = (Map<String, Object>) context.getAttribute(WEBKEY);
    if (map != null || !create)
      return map;
    map = new HashMap<>();
    context.setAttribute(WEBKEY, map);
    return map;
  }

  /**
   * returns a read-only map of all current parameters
   */
  @SuppressWarnings("unchecked")
  public static Map<String, Object> getParameterMap(HttpServletRequest request) {
    HttpSession session = request.getSession(true);
    ServletContext context = session.getServletContext();

    Map<String, Object> map = new HashMap<>();
    Map<String, Object> m = (Map<String, Object>) context.getAttribute(WEBKEY);
    if (m != null)
      map.putAll(m);
    m = (Map<String, Object>) session.getAttribute(WEBKEY);
    if (m != null)
      map.putAll(m);
    m = (Map<String, Object>) request.getAttribute(WEBKEY);
    if (m != null)
      map.putAll(m);
    return Collections.unmodifiableMap(map);
  }

}