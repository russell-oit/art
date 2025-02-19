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

import net.sf.jpivotart.jpivot.olap.model.NumberFormat;
import net.sf.jpivotart.jpivot.olap.model.impl.CellBase;
import net.sf.jpivotart.jpivot.util.NumSeparators;

/**
 * Cell Implementation for XMLA
 */
public class XMLA_Cell extends CellBase {

  private Object value = null;
  private int ordinal;
  private XMLA_Model model;

  // dsf add model to the constructor
  public XMLA_Cell(int ordinal,XMLA_Model model) {
  	this.model = model;
    this.ordinal = ordinal;
    formattedValue = "(null)";
  }
 
  /**
   * @see net.sf.jpivotart.jpivot.olap.model.Cell#getValue()
   */
  public Object getValue() {
    return value;
  }

  /**
   * Sets the value.
   * @param value The value to set
   */
  public void setValue(Object value) {
    this.value = value;
  }

  /**
   * Returns the ordinal.
   * @return int
   */
  public int getOrdinal() {
    return ordinal;
  }

  /**
    * @see net.sf.jpivotart.jpivot.olap.model.Cell#isNull()
    */
  public boolean isNull() {
    return (value == null);
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.model.Cell#getFormat()
   */
  public NumberFormat getFormat() {
      // NumberFormat retVal = null;
      
      if (this.getValue()==null)
          return null;

        if (this.getValue() instanceof Number) {
          // continue
        } else
          return null;

        boolean isPercent = formattedValue.indexOf('%') >= 0;
        boolean isGrouping = false;
        NumSeparators sep = NumSeparators.instance(model.getLocale());

        int fractionDigits = 0;
        if (formattedValue.indexOf(sep.thouSep) >= 0)
          isGrouping = true;
        int i = formattedValue.indexOf(sep.decimalSep);
        if (i > 0) {
          while (++i < formattedValue.length() && Character.isDigit(formattedValue.charAt(i)))
            ++fractionDigits;
        }
        
        return new NumFmt(isGrouping, fractionDigits, isPercent);
        
  }

/**
 * @return Returns the model.
 */
public XMLA_Model getModel() {
	return model;
}
/**
 * @param model The model to set.
 */
public void setModel(XMLA_Model model) {
	this.model = model;
}
} // XMLA_Cell

/**
 * 
 * TODO Should make public accessors and use this instead: com.tonbeller.olap.model.impl.NumberFormatImpl? 
 */
class NumFmt implements NumberFormat{
    
    private boolean isGrouping;
    private int fractionDigits;
    private boolean isPercent;
    public NumFmt(boolean isGrouping, int fractionDigits, boolean isPercent) {
        this.isGrouping = isGrouping;
        this.fractionDigits = fractionDigits;
        this.isPercent = isPercent;
    }


    public int getFractionDigits() {
        return fractionDigits;
    }

    public void setFractionDigits(int fractionDigits) {
        this.fractionDigits = fractionDigits;
    }

    public boolean isGrouping() {
        return isGrouping;
    }

    public void setGrouping(boolean isGrouping) {
        this.isGrouping = isGrouping;
    }

    public boolean isPercent() {
        return isPercent;
    }

    public void setPercent(boolean isPercent) {
        this.isPercent = isPercent;
    }
}