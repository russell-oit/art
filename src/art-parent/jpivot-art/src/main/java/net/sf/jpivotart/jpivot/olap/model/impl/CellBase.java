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

package net.sf.jpivotart.jpivot.olap.model.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import net.sf.jpivotart.jpivot.olap.model.Cell;
import net.sf.jpivotart.jpivot.olap.model.NumberFormat;
import net.sf.jpivotart.jpivot.olap.model.Property;
import net.sf.jpivotart.jpivot.olap.model.Visitor;

/**
 * Cell base for both XMLA and Mondrian
 */
public abstract class CellBase implements Cell {

  static Logger logger = Logger.getLogger(CellBase.class);

  protected String formattedValue;
  private List<Property> properties = null;

  /**
   * @see net.sf.jpivotart.jpivot.olap.model.Cell#getValue()
   */
  public abstract Object getValue();

  /**
   * @see net.sf.jpivotart.jpivot.olap.model.Cell#getFormat()
   */
  public abstract NumberFormat getFormat();

  /**
   * @return the formatted value String
   * @see net.sf.jpivotart.jpivot.olap.model.Cell#getFormattedValue()
   */
  public String getFormattedValue() {
    return formattedValue;
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.model.Cell#isNull()
   */
  public abstract boolean isNull();

  /**
   * @see net.sf.jpivotart.jpivot.olap.model.PropertyHolder#getProperties()
   */
  public Property[] getProperties() {
    if (properties == null)
      return new Property[0];
    else
      return properties.toArray(new Property[0]);
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.model.PropertyHolder#getProperty(String)
   */
  public Property getProperty(String name) {

    if (properties == null)
      return null;

    for (Property prop : properties) {
      if (prop.getName().equalsIgnoreCase(name))
        return prop;

    }
    return null;
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.model.Visitable#accept(Visitor)
   */
  public void accept(Visitor visitor) {
    visitor.visitCell(this);
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.model.Decorator#getRootDecoree()
   */
  public Object getRootDecoree() {
    return this;
  }

  /**
   * @param string
   */
  public void setFormattedValue(String string, FormatStringParser parser) {
    FormatStringParser.Result res = parser.parse(this, string);
    formattedValue = res.getFormattedValue();
    if (res.getProperties().size() > 0) {
      if (properties == null)
        properties = new ArrayList<>();
      properties.addAll(res.getProperties());
    }
  }

  /**
   * add property to cell
   * 
   * @param prop
   * @param value
   */
  public void addProperty(String prop, String value) {
    Property p = this.getProperty(prop);
    if (p != null) {
      ((PropertyImpl) p).setValue(value);
    } else {
      PropertyImpl pi = new PropertyImpl();
      pi.setName(prop);
      pi.setLabel(prop);
      pi.setValue(value);
      if (properties == null)
        properties = new ArrayList<>();
      properties.add(pi);
    }
  }

} // CellBase
