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
package net.sf.wcfart.wcf.wizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sf.wcfart.wcf.controller.RequestContext;

/**
 * Support class for building WizardPage implementations. 
 * 
 * @author av
 */
public class WizardPageSupport {
  WizardPage source;
  ArrayList<PageListener> listeners = new ArrayList<>();

  public WizardPageSupport(WizardPage source) {
    this.source = source;
  }

  public void fireNext(RequestContext context) throws Exception {
    for (Iterator<PageListener> it = iterator(); it.hasNext();)
      (it.next()).onNext(context);
  }

  public void fireBack(RequestContext context) throws Exception {
    for (Iterator<PageListener> it = iterator(); it.hasNext();)
      (it.next()).onBack(context);
  }

  public void fireFinish(RequestContext context) throws Exception {
    for (Iterator<PageListener> it = iterator(); it.hasNext();)
      (it.next()).onFinish(context);
  }

  public void fireCancel(RequestContext context) throws Exception {
    for (Iterator<PageListener> it = iterator(); it.hasNext();)
      (it.next()).onCancel(context);
  }

  private Iterator<PageListener> iterator() {
    if (listeners.isEmpty())
		//https://stackoverflow.com/questions/306713/collections-emptylist-returns-a-listobject
		//https://www.leveluplunch.com/java/examples/return-empty-list-iterator-instead-of-null/
      return Collections.emptyIterator();
    List<PageListener> copy = new ArrayList<>(listeners);
    return copy.iterator();
  }

  public void addPageListener(PageListener l) {
    listeners.add(l);
  }

  public void removePageListener(PageListener l) {
    listeners.remove(l);
  }

  public void fireWizardButton(RequestContext context, String methodName) throws Exception {
    if ("onNext".equals(methodName))
      fireNext(context);
    else if ("onBack".equals(methodName))
      fireBack(context);
    else if ("onCancel".equals(methodName))
      fireCancel(context);
    else if ("onFinish".equals(methodName))
      fireFinish(context);
  }

}