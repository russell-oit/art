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
import java.util.List;

import net.sf.jpivotart.jpivot.olap.model.Expression;
import net.sf.jpivotart.jpivot.olap.model.FunCallExpr;
import net.sf.jpivotart.jpivot.olap.model.PropertyExpr;
import net.sf.jpivotart.jpivot.olap.model.StringExpr;
import net.sf.jpivotart.jpivot.olap.model.Visitor;
import net.sf.jpivotart.jpivot.olap.model.VisitorSupportSloppy;

/**
 * PropertyExpr implementation
 */
public class PropertyExprImpl implements PropertyExpr {

  private Expression valueExpr;
  private String name;

  /**
   * c'tor
   * @param name
   */
  public PropertyExprImpl(String name, Expression valueExpr) {
    this.name = name;
    this.valueExpr = valueExpr;
  }
  
  /**
   * @return the value expression
   * @see net.sf.jpivotart.jpivot.olap.model.PropertyExpr#getValueExpr()
   */
  public Expression getValueExpr() {
    return valueExpr;
  }

  /**
   * @return the property name
   * @see net.sf.jpivotart.jpivot.olap.model.PropertyExpr#getName()
   */
  public String getName() {
    return name;
  }

  /**
   * walk expression tree and find possible choices
   * @see net.sf.jpivotart.jpivot.olap.model.PropertyExpr#getChoices()
   */
  public String[] getChoices() {

    final List choices = new ArrayList();

    this.accept(new VisitorSupportSloppy() {
      // ParameterExpr not supported
      public void visitStringExpr(StringExpr v) {
        choices.add(v.getValue());
      }

      public void visitFunCallExpr(FunCallExpr v) {
        Expression[] args = v.getArgs();
        for (int i = 0; i < args.length; i++) {
          args[i].accept(this);
        }
      }
      
      public void visitPropertyExpr(PropertyExpr v) {
        Expression exp = v.getValueExpr();
        exp.accept(this);
      }
      
    });

    return (String[]) choices.toArray(new String[0]);
  }

  /**
   * visitor implementation
   * @see net.sf.jpivotart.jpivot.olap.model.Visitable#accept
   */
  public void accept(Visitor visitor) {
    visitor.visitPropertyExpr(this);
  }

} // PropertyExpr
