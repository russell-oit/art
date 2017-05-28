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
package net.sf.wcfart.wcf.bookmarks;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

/**
 * @author av
 */
public class BookmarkManager {
  private HttpSession session;
  private Map<String, Object> state2 = Collections.emptyMap();
  private static final String WEBKEY = BookmarkManager.class.getName();

  /** session singleton */
  BookmarkManager(HttpSession session) {
    this.session = session;
  }

  public static BookmarkManager instance(HttpSession session) {
    BookmarkManager bm = (BookmarkManager) session.getAttribute(WEBKEY);
    if (bm == null) {
      bm = new BookmarkManager(session);
      session.setAttribute(WEBKEY, bm);
    }
    return bm;
  }

  /**
   * collects the state of all Bookmarkable Session Attributes
   * @return memento object
   */
  public Object collectSessionState(int levelOfDetail) {
    Map<String, Object> map = new HashMap<>();
    for (Enumeration<String> en = session.getAttributeNames(); en.hasMoreElements();) {
      String name = en.nextElement();
      Object attr = session.getAttribute(name);
      if (attr instanceof Bookmarkable) {
        Object value = ((Bookmarkable) attr).retrieveBookmarkState(levelOfDetail);
        if (value != null)
          map.put(name, value);
      }
    }
    return map;
  }

  /**
   * restores the State of all Bookmarkable Session Atributes
   * @param memento created by collectBookmarkState()
   * @see #collectSessionState
   */
  	@SuppressWarnings("unchecked")
  public void restoreSessionState(Object memento) {
    // no bookmark state?
    if (memento == null) {
      state2 = Collections.emptyMap();
      return;
    }
	
	//https://stackoverflow.com/questions/39366263/does-the-placement-of-suppresswarningsunchecked-matter
    state2 = (Map<String, Object>) memento;
    for (String name : state2.keySet()) {
      restoreAttributeState(name);
    }
  }

  /**
   * restores bookmark state for a single session attribute.
   * This is used
   * for objects that were created after restoreSessionState() was called.
   * (e.g. via jsp tags on different jsp pages).
   * @param name name of the session attribute
   */
  public void restoreAttributeState(String name) {
    Object attr = session.getAttribute(name);
    if (attr instanceof Bookmarkable) {
      Object value = state2.get(name);
      if (value != null)
         ((Bookmarkable) attr).setBookmarkState(value);
    }
  }
}
