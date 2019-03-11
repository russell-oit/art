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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import net.sf.jpivotart.jpivot.olap.model.Cell;
import net.sf.jpivotart.jpivot.olap.model.CellFormatter;
import net.sf.jpivotart.jpivot.olap.model.Property;
import net.sf.wcfart.tbutils.res.Resources;

public class FormatStringParser {
  private static Map<String, CellFormatter> cellFormatters = new HashMap<>();
  private RE regex1;
  private RE regex2;
  private Logger logger = Logger.getLogger(FormatStringParser.class);
  Resources resources;
  
  public FormatStringParser() {
    try {
      regex1 = new RE("\\s*([a-zA-Z][\\w\\.]*)\\s*=\\s*'([^']*)'");
      regex2 = new RE("\\s*([a-zA-Z][\\w\\.]*)\\s*=\\s*([^\\s]*)");
      resources = Resources.instance(FormatStringParser.class);
    } catch (RESyntaxException e) {
      logger.error(null, e);
    }
  }

  public static class Result {
    String formattedValue;
    List<Property> properties;

    private Result(String formattedValue, List<Property> properties) {
      this.formattedValue = formattedValue;
      this.properties = properties;
    }
    public String getFormattedValue() {
      return formattedValue;
    }
    public List<Property> getProperties() {
      return properties;
    }
  }

  public Result parse(Cell cell, String formattedValue) {
    if (formattedValue == null) {
      // SAP
      return new Result("", Collections.<Property>emptyList());
    }
    
    List<Property> properties = Collections.emptyList();
    if (formattedValue.startsWith("|")) {
      properties = new ArrayList<>();
      String[] strs = formattedValue.substring(1).split("\\|");
      formattedValue = strs[0]; // original value
      for (int i = 1; i < strs.length; i++) {
        String propName = null;
        String propValue = null;
        if (regex1.match(strs[i])) {
          propName = regex1.getParen(1); // property name
          propValue = regex1.getParen(2); // property value
        } else if (regex2.match(strs[i])) {
          propName = regex2.getParen(1); // property name
          propValue = regex2.getParen(2); // property value
        } else {
          // it is not a key=value pair
          // we add the String to the formadded value
          formattedValue += strs[i];
          continue;
        }

        // call user defined function, if property key is "exit"
        //  exit = xxx
        //  where xxx is assigned to a class in user.properties or
        // system.properties
        //  the class must implement the CellFormatter interface
        if (propName.equalsIgnoreCase("exit")) {
          // try to find the exit in the map
          CellFormatter cf = getCellFormatter(propValue);
          if (cf != null) {
            formattedValue = cf.formatCell(cell);
          }
        } else {
          PropertyImpl prop = new PropertyImpl();

          prop.setName(propName);
          prop.setLabel(propName);
          prop.setValue(propValue);

          properties.add(prop);
        }
      } // for

    } // if (formattedValue.startsWith("|"))
    return new Result(formattedValue, properties);
  }

  /**
   * Threadsafe access to cell formatter cache
   */
  private CellFormatter getCellFormatter(String propValue) {
    synchronized (cellFormatters) {
      CellFormatter cf = cellFormatters.get(propValue);
      if (cf == null) {
        // not there, create new
        try {
          //String className = resources.getString("cellfmt." + propValue);
		  String className = "net.sf.jpivotart.jpivot.olap.model.impl.CF_hhhmmss";
          Class<?> clazz = Class.forName(className);
          Constructor ctor = clazz.getConstructor(new Class[0]);
          cf = (CellFormatter) ctor.newInstance(new Object[0]);
        } catch (Exception e) {
          logger.error("could not instantiate cell formatter " + propValue, e);
        }
        cellFormatters.put(propValue, cf);
      } else {
        logger.error("Could not find a property definition for exit = " + propValue);
      }
      return cf;
    }
  }

}