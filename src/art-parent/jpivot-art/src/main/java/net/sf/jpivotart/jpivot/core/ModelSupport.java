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
package net.sf.jpivotart.jpivot.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Default implementation of a model
 * @author av
 */

public class ModelSupport implements Model {

  private Collection<ModelChangeListener> listeners = new ArrayList<>();
  private Map<String, Extension> extensionMap = new HashMap<>();
  private Locale locale;
  private Model decoratedModel;

  public ModelSupport() {
    decoratedModel = this;
  }
  
  /**
   * clears all references to extensioins (to avoid memory leaks) 
   */
  public void destroy() {
    listeners.clear();
    extensionMap.clear();
    locale = null;
    decoratedModel = null;
  }

  public Extension getExtension(String id) {
    return extensionMap.get(id);
  }

  /**
   * returns the extensions
   * @see Extension
   */
  public Map<String, Extension> getExtensions() {
    return extensionMap;
  }

  /**
   * adds a Feature to this model. Used by ModelFactory.
   */
  public void addExtension(Extension extension) {
    extensionMap.put(extension.getId(), extension);
    extension.setModel(this);
    decoratedModel = extension.decorate(decoratedModel);
  }

  /**
   * returns null
   */
  public Object retrieveBookmarkState(int levelOfDetail) {
    return null;
  }

  /**
   * does nothing
   */
  public void setBookmarkState(Object state) {
  }

  /**
   * @see net.sf.jpivotart.jpivot.core.Model#addModelChangeListener(ModelChangeListener)
   */
  public void addModelChangeListener(ModelChangeListener l) {
    listeners.add(l);
  }

  /**
   * @see net.sf.jpivotart.jpivot.core.Model#removeModelChangeListener(ModelChangeListener)
   */
  public void removeModelChangeListener(ModelChangeListener l) {
    listeners.remove(l);
  }

  public void fireModelChanged() {
    fireModelChanged(new ModelChangeEvent(this));
  }

  public void fireModelChanged(ModelChangeEvent e) {
	  for(ModelChangeListener listener : listeners){
		  listener.modelChanged(e);
	  }
  }

  public void fireStructureChanged() {
    fireStructureChanged(new ModelChangeEvent(this));
  }

  public void fireStructureChanged(ModelChangeEvent e) {
	   for(ModelChangeListener listener : listeners){
		  listener.structureChanged(e);
	  }
  }

  /**
   * Returns the current locale.
   * @return Locale
   */
  public Locale getLocale() {
    if (locale == null)
      return Locale.getDefault();
    return locale;
  }

  /**
   * Sets the current locale.
   * @param locale The locale to set
   */
  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  /**
   * Returns a decorated model if any of the extensions decorate this. Returns this otherwise.
   */
  public Model getTopDecorator() {
    return decoratedModel;
  }

  public Model getRootModel() {
    return this;
  }
}
