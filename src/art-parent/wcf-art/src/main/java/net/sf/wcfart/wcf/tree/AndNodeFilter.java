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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * implements the AND function for NodeFilter
 */
public class AndNodeFilter extends ArrayList<NodeFilter> implements NodeFilter {
	private static final long serialVersionUID = 1L;
	
  public boolean accept(Object node) {
    for (Iterator<NodeFilter> it = iterator(); it.hasNext();)
      if (!(it.next()).accept(node))
        return false;
    return true;
  }
}
