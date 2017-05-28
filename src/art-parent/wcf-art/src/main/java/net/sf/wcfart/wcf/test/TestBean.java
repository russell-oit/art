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
package net.sf.wcfart.wcf.test;

import net.sf.wcfart.wcf.controller.RequestContext;
import net.sf.wcfart.wcf.form.FormBean;
import net.sf.wcfart.wcf.form.FormComponent;
import net.sf.wcfart.wcf.selection.SelectionChangeEvent;
import net.sf.wcfart.wcf.selection.SelectionChangeListener;
import net.sf.wcfart.wcf.table.TableModel;
import net.sf.wcfart.wcf.table.TestModel;
import net.sf.wcfart.wcf.tree.TestTreeModel;
import net.sf.wcfart.wcf.tree.TreeModel;

public class TestBean implements FormBean {

  class MyTableModel extends TestModel implements SelectionChangeListener {
    public MyTableModel() {
      super.setTitle(null);
    }

    public void selectionChanged(SelectionChangeEvent event) {
      //System.out.println("table selection changed");
    }
  };

  class MyTreeModel extends TestTreeModel implements SelectionChangeListener {
    public void selectionChanged(SelectionChangeEvent event) {
      //System.out.println("tree selection changed");
    }
  };

  String stringValue = "some String";
  int intValue = 123;

  TableModel tableValue = new MyTableModel();
  TreeModel treeValue = new MyTreeModel();

  public int getIntValue() {
    return intValue;
  }

  public String getStringValue() {
    return stringValue;
  }

  public void setIntValue(int i) {
    intValue = i;
  }

  public void setStringValue(String string) {
    stringValue = string;
  }

  public TableModel getTableValue() {
    return tableValue;
  }

  public TreeModel getTreeValue() {
    return treeValue;
  }

  public void testAction(RequestContext context) throws Exception {
    System.out.println("testhandler called");
  }

  /**
   * implement FormBean - this allows us to access the ui, e.g.
   * hide some input elements or change the selectionmodel of an
   * embedded tree component.
   */
  public void setFormComponent(RequestContext context, FormComponent form) {
  }
}
