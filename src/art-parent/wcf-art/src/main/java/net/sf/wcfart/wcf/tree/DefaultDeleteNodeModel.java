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
package net.sf.wcfart.wcf.tree;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author av
 */
public class DefaultDeleteNodeModel implements DeleteNodeModel {
  Set<Object> deleted;
  NodeFilter deletableFilter;
  
 /**
  * creates a NodeDeleter that uses a HashSet for the deleted nodes 
  */ 
  public DefaultDeleteNodeModel() {
    deleted = new HashSet<>();
  }

  /**
   * creates a NodeDeleter that uses a TreeSet with 
   * <code>comp</code> for the deleted nodes 
   */
  public DefaultDeleteNodeModel(Comparator<? super Object> comp) {
    deleted = new TreeSet<>(comp);
  }

  public boolean isDeletable(Object node) {
    return deletableFilter != null && deletableFilter.accept(node);
  }

  public void delete(Object node) {
    deleted.add(node);
  }

  public Set<Object> getDeleted() {
    return deleted;
  }

  /**
   * gets the filter for isDeletable()
   */
  public NodeFilter getDeletableFilter() {
    return deletableFilter;
  }

  /**
   * sets the filter for isDeletable()
   */
  public void setDeletableFilter(NodeFilter filter) {
    deletableFilter = filter;
  }

}
