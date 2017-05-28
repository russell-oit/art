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
package net.sf.wcfart.wcf.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import net.sf.wcfart.wcf.component.FormListener;
import net.sf.wcfart.wcf.controller.RequestContext;
import net.sf.wcfart.wcf.convert.ConvertException;
import net.sf.wcfart.wcf.ui.ListItem;
import net.sf.wcfart.wcf.utils.DomUtils;
import net.sf.wcfart.wcf.utils.SoftException;

/**
 * THIS IS CRAP - DONT USE!
 * <p/>
 * stateful listbox.
 *
 * initialize():
 * <pre>
 *  Element parent = ...
 *  // erzeuge Listbox Element
 *  Element lb = ListBox1.addListBox1(parent);
 *  ListBox1.setId(lb, DomUtils.randomId());
    ListBox1.setLabel(lb, p.getLabel());

    // create MappedListBox
    MappedListBox mlb = new MappedListBox(lb);

    // add Items
    Object[] objs = ...
    for (int i = 0; i &lt; objs.length; i++) {
      Object o = objs[i];
      MappedListBox.Item item = new MappedListBox.Item(hier.getLabel(), o);
      mlb.getEntries().add(item);
      if (some condition)
        item.setSelected(true);
    }
    mlb.sortByLabel();

    // create DOM Elements
    mlb.revert();
 * </pre>
 *
 * validate():
 * <pre>
 *  mlb.validate(context);
    MappedListBox.Item item = mlb.getSingleSelection();
    if (item != null)
      ... // work with selection
 * </pre>
 *
 * revert():
 * <pre>
 *   mlb.revert();
 * </pre>
 *
 * @author av
 */
public class MappedListBox implements FormListener {
  List<Item> entries = new ArrayList<>();
  Element listBox;
  private static Logger logger = Logger.getLogger(MappedListBox.class);

  public MappedListBox(Element listBox) {
    this.listBox = listBox;
  }

  public void revert(RequestContext context) {
    DomUtils.removeChildElements(listBox);
    for (Item item : entries) {
      Element elem = ListItem.addListItem(listBox);
      item.setElement(elem);
      ListItem.setId(elem, item.getId());
      ListItem.setLabel(elem, item.getLabel());
      if (item.isSelected())
        ListItem.setSelected(elem, true);
    }
  }

  public boolean validate(RequestContext context) {
    try {
      context.getConverter().validate(context.getParameters(), context.getFileParameters(), listBox, null);
      for (Item item : entries) {
        item.setSelected(ListItem.isSelected(item.getElement()));
      }
    } catch (ConvertException e) {
      logger.error("exception caught", e);
      throw new SoftException(e);
    }
    return true;
  }

  public Item getSingleSelection() {
    for (Item item : entries) {
      if (item.isSelected())
        return item;
    }
    return null;
  }

  public void sortByLabel() {
    Collections.sort(entries, new Comparator<Item>(){
      public int compare(Item o1, Item o2) {
        String l1 = o1.getLabel();
        String l2 = o2.getLabel();
        return l1.compareTo(l2);
      }
    });
  }

  /**
   * listbox entry
   */
  public static class Item {
    String label;
    String id;
    Object value;
    boolean selected;
    Element element;

    public Item(String label, Object value) {
      this.label = label;
      this.value = value;
      this.id = DomUtils.randomId();
    }

    /**
     * Returns the label.
     * @return String
     */
    public String getLabel() {
      return label;
    }

    /**
     * Returns the value.
     * @return Object
     */
    public Object getValue() {
      return value;
    }

    /**
     * Sets the label.
     * @param label The label to set
     */
    public void setLabel(String label) {
      this.label = label;
    }

    /**
     * Sets the value.
     * @param value The value to set
     */
    public void setValue(Object value) {
      this.value = value;
    }
    /**
     * Returns the id.
     * @return String
     */
    public String getId() {
      return id;
    }

    /**
     * Sets the id.
     * @param id The id to set
     */
    public void setId(String id) {
      this.id = id;
    }

    /**
     * Returns the selected.
     * @return boolean
     */
    public boolean isSelected() {
      return selected;
    }

    /**
     * Sets the selected.
     * @param selected The selected to set
     */
    public void setSelected(boolean selected) {
      this.selected = selected;
    }

    /**
     * Returns the element.
     * @return Element
     */
    public Element getElement() {
      return element;
    }

    /**
     * Sets the element.
     * @param element The element to set
     */
    public void setElement(Element element) {
      this.element = element;
    }

  }

  /**
   * Returns the entries.
   * @return List
   */
  public List<Item> getEntries() {
    return entries;
  }

  /**
   * Sets the entries.
   * @param entries The entries to set
   */
  public void setEntries(List<Item> entries) {
    this.entries = entries;
  }

}
