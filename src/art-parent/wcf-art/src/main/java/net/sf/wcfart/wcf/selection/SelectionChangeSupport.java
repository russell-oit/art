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
package net.sf.wcfart.wcf.selection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.wcfart.wcf.controller.RequestContext;

public class SelectionChangeSupport {

  ArrayList<SelectionChangeListener> listeners = new ArrayList<>();
  SelectionModel source;

  public SelectionChangeSupport(SelectionModel source) {
    this.source = source;
  }

  public void fireSelectionChanged(RequestContext context) {
    if (listeners.size() > 0) {
      SelectionChangeEvent event = new SelectionChangeEvent(context, source);
      List<SelectionChangeListener> copy = new ArrayList<>(listeners);
      for (SelectionChangeListener listener : copy){
		  listener.selectionChanged(event);
	  }
    }
  }

  public void addSelectionListener(SelectionChangeListener l) {
    listeners.add(l);
  }

  public void removeSelectionListener(SelectionChangeListener l) {
    listeners.remove(l);
  }

}
