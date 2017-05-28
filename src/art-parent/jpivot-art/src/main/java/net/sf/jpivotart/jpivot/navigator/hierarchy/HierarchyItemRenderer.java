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
package net.sf.jpivotart.jpivot.navigator.hierarchy;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Iterator;
import net.sf.jpivotart.jpivot.olap.model.Member;
import net.sf.wcfart.wcf.catedit.Category;
import net.sf.wcfart.wcf.catedit.DefaultItemElementRenderer;
import net.sf.wcfart.wcf.catedit.Item;
import net.sf.wcfart.wcf.controller.RequestContext;
import net.sf.wcfart.wcf.utils.DomUtils;

/**
 * renders a Hierarchy 
 * @author av
 */
public class HierarchyItemRenderer extends DefaultItemElementRenderer {

  public Element render(RequestContext context, Document factory, Category cat, Item item) {
    Element elem = super.render(context, factory, cat, item);
    HierarchyItem hi = (HierarchyItem)item;
    if (hi.isClickable())
      elem.setAttribute("id", hi.getId());

    if (!hi.getSlicerSelection().isEmpty()) {
      // Manage the addition of slicer elements in loop before of the compound 
      // slicers.
      	
      List slicerSelection = hi.getSlicerSelection();
      if (slicerSelection != null) {
    	  Iterator iter = slicerSelection.iterator();
    	  while (iter.hasNext()) {
    		  Member m = (Member)iter.next();
		      Element e = DomUtils.appendElement(elem, "slicer-value");
		      e.setAttribute("label", m.getLabel());
		      e.setAttribute("level", m.getLevel().getLabel());
	      }
      }
    }
    
    return elem;
    
  }
}
