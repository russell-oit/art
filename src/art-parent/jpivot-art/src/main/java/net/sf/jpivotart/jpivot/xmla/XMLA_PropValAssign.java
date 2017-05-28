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
package net.sf.jpivotart.jpivot.xmla;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Bean
 * assign a property values to binary coded integers
 */
public class XMLA_PropValAssign {

  private Map<String, List<ValAssign>> propMap = new HashMap<>();
  private int firstBit = 0;
  static private int LASTBIT = 15; // use FONTSIZE, < 2**16

  static Logger logger = Logger.getLogger(XMLA_PropValAssign.class);

  /**
   * @param values - possible value assignments
   */
  public void addProp(String prop, List<String> values) {
    // how many bits do we need ? log(2, #values)
    int nValues = values.size();
    if (nValues == 0)
      return;
    int nBits = 1;
    int n = (nValues - 1) / 2;
    while (n > 0) {
      ++nBits;
      n = n / 2;
    }

    int mask = 1 << nBits;
    mask = mask - 1; // 2**nBits -1
    mask = mask << firstBit;

    if (firstBit + nBits > LASTBIT) {
      // not enough bits to encode the property values (FONT_SIZE) 
      logger.error("could not encode property values " + prop + " #" + nValues);
      return;
    }
    
    List<ValAssign> vAssignList = new ArrayList<>();
    int iBitVal = 0;
    for (String val : values) {
      ValAssign vAssign = new ValAssign();
      vAssign.setVal(val);
      vAssign.setBitMask(mask);
      int bitVal = iBitVal << firstBit;
      vAssign.setBitVal(bitVal);
      vAssignList.add(vAssign);
      ++iBitVal;
    }
    propMap.put(prop, vAssignList);
    firstBit += nBits;
  }

  /**
   * @param prop
   * @return the value assignment list
   */
  public List<ValAssign> getValAssignList(String prop) {
    return propMap.get(prop);
  }

  /**
   * @return prop map
   */
  public Map<String, List<ValAssign>> getPropMap() {
    return propMap;
  }

  /**
   * @param map
   */
  public void setPropMap(Map<String, List<ValAssign>> map) {
    propMap = map;
  }
  
  /**
   * @return first bit
   */
  public int getFirstBit() {
    return firstBit;
  }

  /**
   * @param i
   */
  public void setFirstBit(int i) {
    firstBit = i;
  }

  /**
   * Bean - assigned property value
   */
  public static class ValAssign {
    private String val;
    private int bitMask;
    private int bitVal;

    /**
     * @return val
     */
    public String getVal() {
      return val;
    }

    /**
     * @param string
     */
    public void setVal(String string) {
      val = string;
    }

    /**
     * @return bit mask
     */
    public int getBitMask() {
      return bitMask;
    }

    /**
     * @param bitMask
     */
    public void setBitMask(int bitMask) {
      this.bitMask = bitMask;
    }

    /**
     * @return bit val
     */
    public int getBitVal() {
      return bitVal;
    }

    /**
     * @param i
     */
    public void setBitVal(int i) {
      bitVal = i;
    }

  } // ValAssign


} // XMLA_PropValAssign
