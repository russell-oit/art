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

import net.sf.wcfart.wcf.selection.SelectionChangeEvent;
import net.sf.wcfart.wcf.selection.SelectionChangeListener;

public class TestBean {
  class MyTreeModel extends TestTreeModel implements SelectionChangeListener {
    public void selectionChanged(SelectionChangeEvent event) {
      System.out.println("selection changed");
    }
  };
  
  String stringValue = "some String";
  int intValue = 123;
  
  TreeModel treeValue = new MyTreeModel();
  /**
   * @return int value
   */
  public int getIntValue() {
    return intValue;
  }

  /**
   * @return string value
   */
  public String getStringValue() {
    return stringValue;
  }

  /**
   * @return tree value
   */
  public TreeModel getTreeValue() {
    return treeValue;
  }

  /**
   * @param i
   */
  public void setIntValue(int i) {
    intValue = i;
  }

  /**
   * @param string
   */
  public void setStringValue(String string) {
    stringValue = string;
  }

  /**
   * @param model
   */
  public void setTreeValue(TreeModel model) {
    treeValue = model;
  }

}
