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
package net.sf.jpivotart.jpivot.mondrian;

import java.text.NumberFormat;
import java.text.ParseException;

import net.sf.mondrianart.mondrian.olap.Query;
import net.sf.mondrianart.mondrian.olap.OlapElement;
import net.sf.mondrianart.mondrian.olap.Util;

import org.apache.log4j.Logger;

import net.sf.jpivotart.jpivot.core.ExtensionSupport;
import net.sf.jpivotart.jpivot.olap.model.Dimension;
import net.sf.jpivotart.jpivot.olap.model.DoubleExpr;
import net.sf.jpivotart.jpivot.olap.model.Expression;
import net.sf.jpivotart.jpivot.olap.model.Hierarchy;
import net.sf.jpivotart.jpivot.olap.model.IntegerExpr;
import net.sf.jpivotart.jpivot.olap.model.Level;
import net.sf.jpivotart.jpivot.olap.model.Member;
import net.sf.jpivotart.jpivot.olap.model.impl.DoubleExprImpl;
import net.sf.jpivotart.jpivot.olap.model.impl.IntegerExprImpl;
import net.sf.jpivotart.jpivot.olap.model.impl.StringExprImpl;
import net.sf.jpivotart.jpivot.olap.navi.ExpressionParser;

/**
 * let Mondrian parse a String expression
 */
public class MondrianExpressionParser extends ExtensionSupport implements ExpressionParser {

  static NumberFormat nf = NumberFormat.getInstance();
  static Logger logger = Logger.getLogger(MondrianSetParameter.class);

  /**
   */
  public MondrianExpressionParser() {
    super.setId(ExpressionParser.ID);
  }

  /**
   * @see ExpressionParser#unparse(net.sf.jpivotart.jpivot.olap.model.Expression)
   */
  public String unparse(Expression expr) {
    if (expr instanceof DoubleExpr) {
      double d = ((DoubleExpr) expr).getValue();
      return nf.format(d);
    } else if (expr instanceof IntegerExpr) {
      int ii = ((IntegerExpr) expr).getValue();
      return String.valueOf(ii);
    } else if (expr instanceof MondrianMember) {
      MondrianMember m = (MondrianMember) expr;
      net.sf.mondrianart.mondrian.olap.Member monMember = m.getMonMember();
      return monMember.getUniqueName();
    }
    else if (expr instanceof MondrianLevel) {
      return ((MondrianLevel)expr).getUniqueName();
    }
    else if (expr instanceof MondrianHierarchy) {
      return ((MondrianHierarchy)expr).getUniqueName();
    }
    else if (expr instanceof MondrianDimension) {
      return ((MondrianDimension)expr).getUniqueName();
    }
    return null;
  }

  /**
   *
   * parse a String
   * currently we cannot handle anything different from
   * - string
   * - number
   * - member.
   * @see net.sf.jpivotart.jpivot.olap.navi.ExpressionParser#parse(java.lang.String)
   */
  public Expression parse(String expr) throws InvalidSyntaxException {

    // is it a String (enclose in double or single quotes ?
    String trimmed = expr.trim();
    int len = trimmed.length();
    if (trimmed.charAt(0) == '"' && trimmed.charAt(len - 1) == '"')
      return new StringExprImpl(trimmed.substring(1, trimmed.length() - 1));
    if (trimmed.charAt(0) == '\'' && trimmed.charAt(len - 1) == '\'')
      return new StringExprImpl(trimmed.substring(1, trimmed.length() - 1));

    // is it a Number ?
    Number number = null;
    try {
      number = nf.parse(trimmed);
    } catch (ParseException pex) {
      // nothing to do, should be member
    }
    if (number != null) {
      if (number instanceof Double) {
        return new DoubleExprImpl(number.doubleValue());
      } else {
        return new IntegerExprImpl(number.intValue());
      }
    }

    MondrianModel model = (MondrianModel) getModel();
    Query query = ((MondrianQueryAdapter)model.getQueryAdapter()).getMonQuery();

    // assume member,dimension,hierarchy,level
    OlapElement element;
    try {
      element = Util.lookup(query, Util.parseIdentifier(trimmed));
    } catch (Exception e) {
      logger.info(e);
      throw new InvalidSyntaxException(trimmed);
    }
    if (element instanceof net.sf.mondrianart.mondrian.olap.Member) {
      final net.sf.mondrianart.mondrian.olap.Member monMember = (net.sf.mondrianart.mondrian.olap.Member) element;
      Member member = model.lookupMemberByUName(monMember.getUniqueName());
      return member;
    } else if (element instanceof net.sf.mondrianart.mondrian.olap.Level) {
      net.sf.mondrianart.mondrian.olap.Level monLevel = (net.sf.mondrianart.mondrian.olap.Level) element;
      MondrianLevel level = model.lookupLevel(monLevel.getUniqueName());
      return level;
    } else if (element instanceof net.sf.mondrianart.mondrian.olap.Hierarchy) {
      net.sf.mondrianart.mondrian.olap.Hierarchy monHier = (net.sf.mondrianart.mondrian.olap.Hierarchy) element;
      MondrianHierarchy hier = model.lookupHierarchy(monHier.getUniqueName());
      return hier;
    } else if (element instanceof net.sf.mondrianart.mondrian.olap.Dimension) {
      net.sf.mondrianart.mondrian.olap.Dimension monDim = (net.sf.mondrianart.mondrian.olap.Dimension) element;
      MondrianDimension dim = model.lookupDimension(monDim.getUniqueName());
      return dim;
    }

    throw new InvalidSyntaxException("could not resolve expression " + trimmed);
  }

  public Member lookupMember(String uniqueName) throws InvalidSyntaxException {
    MondrianModel model = (MondrianModel) getModel();
    return model.lookupMemberByUName(uniqueName);
  }

  public Level lookupLevel(String uniqueName) throws InvalidSyntaxException {
    MondrianModel model = (MondrianModel) getModel();
    return model.lookupLevel(uniqueName);
  }

  public Hierarchy lookupHierarchy(String uniqueName) throws InvalidSyntaxException {
    MondrianModel model = (MondrianModel) getModel();
    return model.lookupHierarchy(uniqueName);
  }

  public Dimension lookupDimension(String uniqueName) throws InvalidSyntaxException {
    MondrianModel model = (MondrianModel) getModel();
    return model.lookupDimension(uniqueName);
  }

} // MondrianExpressionParser
