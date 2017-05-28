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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import net.sf.wcfart.wcf.controller.RequestContext;

/**
 * default implementation of <code>Form</code>. Ensures that
 * two different <code>FormSupport</code> instances may listen to
 * each other without endless recursion.
 * @see Form
 * @author av
 */
public class FormSupport implements Form {
  Set<FormListener> listeners = new HashSet<>();
  boolean fireing = false;
  private static Logger logger = Logger.getLogger(FormSupport.class);

  public FormSupport() {
  }

  /**
   * calls validate() on all components
   */
  public boolean validate(RequestContext context) {
    logger.info("enter");
    // avoid endless recursion
    if (fireing)
      return true;
    fireing = true;
    try {
      boolean valid = true;
      for (FormListener listener : listeners) {
        // make sure to validate all components
        valid = listener.validate(context) && valid;
      }
      return valid;
    } finally {
      fireing = false;
    }
  }

  /**
   * calls revert() on all listeners. 
   */
  public void revert(RequestContext context) {
    logger.info("enter");
    if (fireing)
      return;
    fireing = true;
    try {
      for (FormListener listener : listeners) {
        listener.revert(context);
      }
    } finally {
      fireing = false;
    }
  }

  public void addFormListener(FormListener listener) {
    listeners.add(listener);
  }

  public void removeFormListener(FormListener listener) {
    listeners.remove(listener);
  }

}
