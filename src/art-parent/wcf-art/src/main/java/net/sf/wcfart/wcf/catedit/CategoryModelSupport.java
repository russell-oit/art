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
package net.sf.wcfart.wcf.catedit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created on 09.12.2002
 * 
 * @author av
 */
public class CategoryModelSupport implements CategoryModel {

  List<Category> categories = new ArrayList<>();
  ArrayList<CategoryModelChangeListener> listeners = new ArrayList<>();

  /**
   * Returns the categories.
   * @return List
   */
  public List<Category> getCategories() {
    return categories;
  }

  /**
   * Sets the categories.
   * @param categories The categories to set
   */
  public void setCategories(List<Category> categories) {
    this.categories = categories;
  }

  public void addCategoryModelChangeListener(CategoryModelChangeListener l) {
    listeners.add(l);
  }

  public void removeCategoryModelChangeListener(CategoryModelChangeListener l) {
    listeners.remove(l);
  }

  public void fireModelChanged() {
    if (listeners.size() > 0) {
      CategoryModelChangeEvent event = new CategoryModelChangeEvent(this);
      List<CategoryModelChangeListener> copy = new ArrayList<>(listeners);
      for (CategoryModelChangeListener listener : copy){
		  listener.categoryModelChanged(event);
	  }
    }
  }

}
