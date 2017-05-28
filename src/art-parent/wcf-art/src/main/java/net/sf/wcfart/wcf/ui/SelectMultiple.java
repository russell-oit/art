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
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Created on 07.11.2002
 * 
 * @author av
 */
public abstract class SelectMultiple extends Select {

  /** get idx of selected list items */
  public static int[] getSelectedItemsIdx(Element element) {
    List<Integer> vector = new ArrayList<>();
    List<Node> lis = getItems(element);
    for (int i = 0; i < lis.size(); ++i) {
      if (Item.isSelected((Element) lis.get(i))) {
        vector.add(new Integer(i));
      }
    }
    // convert vector to int[]
    int[] retVal = new int[vector.size()];
    for (int i = 0; i < vector.size(); ++i) {
      retVal[i] = vector.get(i);
    }
    return retVal;
  }

  /** get selected list items */
  public static List<Node> getSelectedItems(Element element) {
    List<Node> retVal = new ArrayList<>();
    List<Node> lis = getItems(element);
    for (int i = 0; i < lis.size(); ++i) {
      if (Item.isSelected((Element) lis.get(i))) {
        retVal.add(lis.get(i));
      }
    }
    return retVal;
  }

}
