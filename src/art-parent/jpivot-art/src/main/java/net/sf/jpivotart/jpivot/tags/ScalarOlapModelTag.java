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
package net.sf.jpivotart.jpivot.tags;

import net.sf.jpivotart.jpivot.olap.model.OlapModel;
import net.sf.jpivotart.jpivot.olap.model.impl.ScalarOlapModel;
import net.sf.wcfart.wcf.controller.RequestContext;
import net.sf.wcfart.wcf.expr.ExprUtils;

/**
 * jsp tag that defines a scalar query
 */
public class ScalarOlapModelTag extends OlapModelTag {

  String value;
  String formattedValue;
  String caption;
  
  protected OlapModel getOlapModel(RequestContext context) throws Exception {
    ScalarOlapModel som = new ScalarOlapModel();
    som.setValue(evalNum(context, value));
    som.setFormattedValue(evalStr(context, formattedValue));
    som.setCaption(caption);
    return som;
  }
  
  private String evalStr(RequestContext context, String el) {
    Object obj = eval(context, el);
    if (obj == null)
      return null;
    return String.valueOf(obj);
  }

  private Number evalNum(RequestContext context, String el) {
    Object obj = eval(context, el);
    if (obj instanceof Number)
      return (Number)obj;
    return new Double(String.valueOf(obj));
  }

  private Object eval(RequestContext context, String el) {
    if (ExprUtils.isExpression(el))
      return context.getModelReference(el);
    return el;
  }

  public void setCaption(String caption) {
    this.caption = caption;
  }
  public void setFormattedValue(String formattedValue) {
    this.formattedValue = formattedValue;
  }
  public void setValue(String value) {
    this.value = value;
  }

}