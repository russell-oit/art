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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Fires events to listeners depending on parameters in the HttpServletRequest.
 * <ul>
 * <li>If a listener is registered with name <em>and</em> value, it receives the event
 * if the request contains a paramter with that name and value.
 * <li>If a listener is registered with name only, it receives the event if the request
 * contains a parameter with that name and any value.
 * <li>If a listener is registered with value only, it receives the event if the request
 * contains a parameter with that value and any name.
 * <li>If a listener is registered with no name and no value, it receives every request.
 * </ul>
 */
public class DispatcherSupport implements Dispatcher {

  // key = listener, value = name/value pair separated by '='
  private HashMap<RequestListener, String> map = new HashMap<>();

  // key = name/value pair, value = List of listeners
  private HashMap<String, List<RequestListener>> inverseMap = new HashMap<>();

  /**
   * Adds a listener. A listener can only be registered once.
   * If its registered more than one, the last name/value
   * pair will be used.
   *
   * @param name name of the request parameter or null
   * @param value of the request parameter or null
   * @param listener the listener to register
   */
  public void addRequestListener(String name, String value, RequestListener listener) {
    removeRequestListener(listener);
    String key = getKey(name, value);
    map.put(listener, key);
    List<RequestListener> list = inverseMap.get(key);
    if (list == null) {
      list = new LinkedList<>();
      inverseMap.put(key, list);
    }
    list.add(listener);
  }

  /**
   * removes a listener.
   * @param listener the listener to remove
   */
  public void removeRequestListener(RequestListener listener) {
    String key = map.get(listener);
    if (key != null) {
      map.remove(listener);
      List<RequestListener> list = inverseMap.get(key);
      if (list != null)
        list.remove(listener);
    }
  }

  /**
   * removes all listeners
   */
  public void clear() {
    map.clear();
    inverseMap.clear();
  }

  /**
   * get key
   * @param name name
   * @param value value
   * @return key
   */
  String getKey(String name, String value) {
    if ((name != null) && (value != null)) {
      return name + "=" + value;
    } else if (name != null) {
      return name + "=";
    } else if (value != null) {
      return "=" + value;
    }
    return "=";
  }

  /**
   * finds all RequestListeners that match a name/value pair in requestParameters
   * 
   * @param requestParameters HttpServletRequest.getParameterMap()
   * @return List of RequestListeners
   */
  List<RequestListener> findAll(Map<String, String[]> requestParameters) {
    List<RequestListener> match = new LinkedList<>();
    Iterator<String> it = requestParameters.keySet().iterator();

    while (it.hasNext()) {
      String name = it.next();
      String[] values = requestParameters.get(name);

      if (values != null) {
        for (int i = 0; i < values.length; i++) {

          // empty string will be caught later, when we test for the name only
          if (values[i] == null || values[i].length() == 0)
            continue;

          // try name and value
          List<RequestListener> obj = inverseMap.get(name + "=" + values[i]);

          if (obj != null) {
            match.addAll(obj);
          }

          // try value only
          obj = inverseMap.get("=" + values[i]);

          if (obj != null) {
            match.addAll(obj);
          }
        }
      }

      // try name only
      List<RequestListener> obj = inverseMap.get(name + "=");
      if (obj != null)
        match.addAll(obj);

      // imagebutton support
      if (name.endsWith(".x")) {
        name = name.substring(0, name.length() - 2);
        obj = inverseMap.get(name + "=");
        if (obj != null)
          match.addAll(obj);
      }
    }

    // try default handler
    List<RequestListener> obj = inverseMap.get("=");

    if (obj != null) {
      match.addAll(obj);
    }

    return match;
  }

  /**
   * fires event to all matching listeners
   * @param context the current request
   * @throws Exception the exception from listeners
   */
  public void request(RequestContext context) throws Exception {
    Iterator<RequestListener> it = findAll(context.getRequest().getParameterMap()).iterator();

    while (it.hasNext()) {
      RequestListener listener = it.next();
      listener.request(context);
    }
  }

  /**
   * returns the leaf RequestListeners that would be invoked for a http request
   * containing <code>httpParams</code>
   */
  public List<RequestListener> findMatchingListeners(Map<String, String[]> httpParams) {
    List<RequestListener> candidates = findAll(httpParams);
    List<RequestListener> result = new ArrayList<>();
    for (Iterator<RequestListener> it = candidates.iterator(); it.hasNext();) {
      RequestListener obj = it.next();
      if (obj instanceof Dispatcher) {
        Dispatcher d = (Dispatcher) obj;
        result.addAll(d.findMatchingListeners(httpParams));
      } else {
        result.add(obj);
      }
    }
    return result;
  }

}