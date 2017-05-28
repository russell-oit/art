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
package net.sf.wcfart.wcf.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.sf.wcfart.wcf.utils.XoplonNS;

/**
 * A Select (ListBox, RadioButtons, etc) may have a type and modelReference. If
 * both are present, the modelReference will be updated to contain the value of 
 * the selected items.
 * 
 * @author av
 */
public class Select extends TypedCtrl {


  public static void removeAllItems(Element element) {
    List<Node> lis = getItems(element);
    Iterator<Node> en = lis.iterator();
    while (en.hasNext()) {
      Element item = (Element) en.next();
      element.removeChild(item);
    }
  }


  /** sets selection. 
   */
  public static void setSelection(Element element, Element item) {
    Iterator<Node> it = getItems(element).iterator();
    while (it.hasNext())
      Item.setSelected((Element)it.next(), false);
    Item.setSelected(item, true);
  }

  /** sets selection. 
   * @param items a list of Elements which must be children of element
   */
  public static void setSelectedItems(Element element, List<Node> items) {
    Iterator<Node> it = getItems(element).iterator();
    while (it.hasNext())
      Item.setSelected((Element)it.next(), false);
    
    it = items.iterator();
    while (it.hasNext())
      Item.setSelected((Element)it.next(), true);
  }


  /** removes item from the selection
   * @param item the item to remove from selection
   */
  public static void removeSelection(Element element, Element item) {
    Item.setSelected(item, false);
  }

  /** add list item  */
  public static void addItem(Element element, Element li) {
    element.appendChild(li);
  }

  /** remove list item */
  public static void removeItem(Element element, Element li) {
    element.removeChild(li);
  }

  /** return the items */
  public static List<Node> getItems(Element element) {
    List<Node> lis = new ArrayList<>();

    // iterate children
    NodeList children = element.getChildNodes();
    for (int i = 0; i < children.getLength(); ++i) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        lis.add(child);
      }
    } // iterate children

    return lis;
  }

  /**
   * returns the selected items separated by ';'. Counting starts at 1.
   * This is suitable for the WAP &lt;select&gt; elements iname/ivalue
   * attribute.
   */
  public static void getIValue(Element element) {
    XoplonNS.getAttribute(element, "ivalue");
  }

  /**
   * sets the ivalue attribute for WAP browsers.
   * @see #getIValue
   */
  public static void setIValue(Element element) {
    List<Node> list = getItems(element);
    Iterator<Node> en = list.iterator();
    int index = 1;
    StringBuffer sb = new StringBuffer();
    boolean first = true;
    while (en.hasNext()) {
      Element item = (Element) en.next();
      if (ListItem.isSelected(item)) {
        if (first)
          first = false;
        else
          sb.append(';');
        sb.append(index);
      }
      ++index;
    }
    XoplonNS.setAttribute(element, "ivalue", sb.toString());
  }



}
