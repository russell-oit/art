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

import net.sf.mondrianart.mondrian.olap.Exp;
import net.sf.mondrianart.mondrian.olap.Literal;
import net.sf.mondrianart.mondrian.olap.Syntax;
import net.sf.mondrianart.mondrian.mdx.MemberExpr;
import net.sf.mondrianart.mondrian.mdx.UnresolvedFunCall;

import org.apache.log4j.Logger;

import net.sf.jpivotart.jpivot.olap.navi.SortRank;
import net.sf.jpivotart.jpivot.olap.query.QuaxChangeListener;
import net.sf.jpivotart.jpivot.olap.query.SortRankBase;

/**
 * @author hh
 *
 * Implementation of the Sort Extension for Mondrian Data Source.
 */
public class MondrianSortRank extends SortRankBase implements SortRank, QuaxChangeListener {

  static Logger logger = Logger.getLogger(MondrianSortRank.class);

  public MondrianSortRank() {
    super.setId(SortRank.ID);
  }

  /**
   * apply sort to mondrian query
   */
  public void addSortToQuery() {
    if (sorting && sortPosMembers != null) {
      MondrianModel model = (MondrianModel) getModel();
      net.sf.mondrianart.mondrian.olap.Query monQuery = ((MondrianQueryAdapter)model.getQueryAdapter()).getMonQuery();

      switch (sortMode) {
        case SortRank.ASC :
        case SortRank.DESC :
        case SortRank.BASC :
        case SortRank.BDESC :
          // call sort
          orderAxis(monQuery, sortMode);
          break;
        case SortRank.TOPCOUNT :
          topBottomAxis(monQuery, "TopCount");
          break;
        case SortRank.BOTTOMCOUNT :
          topBottomAxis(monQuery, "BottomCount");
          break;
        default :
          return; // do nothing
      }
    }
  }

  /**
  * Convert sort mode ordinal to sort mode name
  * @param sortMode mode
  * @return name of sort mode
  */
  static private String sortModeName(int sortMode) {
    switch (sortMode) {
      case SortRank.ASC :
        return "ASC";
      case SortRank.DESC :
        return "DESC";
      case SortRank.BASC :
        return "BASC";
      case SortRank.BDESC :
        return "BDESC";
      default :
        return null;
    }
  }

  /**
   * add Order Funcall to QueryAxis
   * @param monQuery
   * @param sortMode
   */
  private void orderAxis(net.sf.mondrianart.mondrian.olap.Query monQuery, int sortMode) {
    // Order(TopCount) is allowed, Order(Order) is not permitted
    //removeTopLevelSort(monQuery, new String[] { "Order" });
    net.sf.mondrianart.mondrian.olap.QueryAxis monAx = monQuery.getAxes()[quaxToSort.getOrdinal()];
    Exp setForAx = monAx.getSet();
    Exp memToSort;
    if (sortPosMembers.length == 1) {
      memToSort = new MemberExpr(((MondrianMember) sortPosMembers[0]).getMonMember());
    } else {
      MemberExpr[] memberExprs = new MemberExpr[sortPosMembers.length];
      for (int i = 0; i < memberExprs.length; i++) {
        memberExprs[i] = new MemberExpr(((MondrianMember) sortPosMembers[i]).getMonMember());
      }
      memToSort = new UnresolvedFunCall("()", Syntax.Parentheses, memberExprs);
    }
    String sDirection = sortModeName(sortMode);
    UnresolvedFunCall funOrder =
      new UnresolvedFunCall("Order", new Exp[] { setForAx, memToSort, Literal.createSymbol(sDirection)});
    monAx.setSet(funOrder);
  }

  /**
   * add Top/BottomCount Funcall to QueryAxis
   */
  private void topBottomAxis(net.sf.mondrianart.mondrian.olap.Query monQuery, String function) {
    // TopCount(TopCount) and TopCount(Order) is not permitted
    //removeTopLevelSort(monQuery, new String[] { "TopCount", "BottomCount", "Order" });
    net.sf.mondrianart.mondrian.olap.QueryAxis monAx = monQuery.getAxes()[quaxToSort.getOrdinal()];
    Exp setForAx = monAx.getSet();
    Exp memToSort;
    if (sortPosMembers.length > 1) {
      MemberExpr[] memberExprs = new MemberExpr[sortPosMembers.length];
      for (int i = 0; i < memberExprs.length; i++) {
        memberExprs[i] = new MemberExpr(((MondrianMember) sortPosMembers[i]).getMonMember());
      }
      memToSort = new UnresolvedFunCall("()", Syntax.Parentheses, memberExprs);
    } else {
      memToSort = new MemberExpr(((MondrianMember) sortPosMembers[0]).getMonMember());
    }
    UnresolvedFunCall funOrder =
      new UnresolvedFunCall(
        function,
        new Exp[] { setForAx, Literal.create(new Integer(topBottomCount)), memToSort });
    monAx.setSet(funOrder);
  }

  /**
   * remove top level sort functions from query axis
   * @param monAx
   * @param functions
   */
  /* not neeeded as of MDX generation version 3
   private void removeTopLevelSort(net.sf.mondrianart.mondrian.olap.Query monQuery, String[] functions) {
     // if the original MDX starts with a sort function, remove it from current query
     Exp originalSet = quaxToSort.getOriginalSet();
     if (!(originalSet instanceof FunCall))
       return;
     FunCall topF = (FunCall) originalSet;
     String fuName = topF.getFunName();
     boolean found = false;
     for (int i = 0; i < functions.length; i++) {
       if (functions[i].equalsIgnoreCase(fuName)) {
         found = true;
         break;
       }
     }
     if (!found)
       return;

     // remove sort function from current set
     net.sf.mondrianart.mondrian.olap.QueryAxis monAx = monQuery.axes[quaxToSort.getOrdinal()];
     Exp setForAx = monAx.set;
     Exp exp = setForAx;
     FunCall parent = null;
     while (exp instanceof FunCall) {
       // we skip over Hierarchize and Union, which was added by navigation
       FunCall f = (FunCall) exp;
       String currentName = f.getFunName();
       if (currentName.equalsIgnoreCase("Hierarchize") || currentName.equalsIgnoreCase("Union")) {
         // skip over
         parent = f;
         exp = f.args[0]; // first arg leads to original set
       } else if (currentName.equalsIgnoreCase(fuName)) {
         // remove it
         if (parent == null)
           monAx.set = f.args[0];
         else
           parent.args[0] = f.args[0];
         return;
       } else
         return; // should never get here
     }
   }
  */

} // End MondrianSortRank
