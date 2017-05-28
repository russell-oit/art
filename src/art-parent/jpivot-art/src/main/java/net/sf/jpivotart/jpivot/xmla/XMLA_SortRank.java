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

import net.sf.jpivotart.jpivot.olap.mdxparse.Exp;
import net.sf.jpivotart.jpivot.olap.mdxparse.FunCall;
import net.sf.jpivotart.jpivot.olap.mdxparse.Literal;
import net.sf.jpivotart.jpivot.olap.mdxparse.ParsedQuery;
import net.sf.jpivotart.jpivot.olap.mdxparse.QueryAxis;
import net.sf.jpivotart.jpivot.olap.model.Member;
import net.sf.jpivotart.jpivot.olap.model.Position;
import net.sf.jpivotart.jpivot.olap.navi.SortRank;
import net.sf.jpivotart.jpivot.olap.query.SortRankBase;

/**
 * SortRank Implementation XMLA
 */
public class XMLA_SortRank extends SortRankBase implements SortRank {

  /**
  * returns true, if one of the members is a measure
  * @param position the position to check for sortability
  * @return true, if the position is sortable
  * @see net.sf.jpivotart.jpivot.olap.navi.SortRank#isSortable(Position)
  */
  public boolean isSortable(Position position) {
    Member[] members = position.getMembers();
    for (int i = 0; i < members.length; i++)
      if (members[i].getLevel().getHierarchy().getDimension().isMeasure())
        return true;
    return false;
  }

  /**
   * apply sort to query
   */
  public void addSortToQuery() {
    if (sorting && sortPosMembers != null) {
      XMLA_Model model = (XMLA_Model) getModel();
      ParsedQuery pq = ((XMLA_QueryAdapter)model.getQueryAdapter()).getParsedQuery();

      switch (sortMode) {
        case SortRank.ASC :
        case SortRank.DESC :
        case SortRank.BASC :
        case SortRank.BDESC :
          // call sort
          orderAxis(pq);
          break;
        case SortRank.TOPCOUNT :
          topBottomAxis(pq, "TopCount");
          break;
        case SortRank.BOTTOMCOUNT :
          topBottomAxis(pq, "BottomCount");
          break;
        default :
          return; // do nothing
      }
    }
  }

  /**
   * add Order Funcall to QueryAxis
   * @param monAx
   * @param monSortMode
   */
  private void orderAxis(ParsedQuery pq) {
    // Order(TopCount) is allowed, Order(Order) is not permitted
    QueryAxis[] queryAxes = pq.getAxes();
    QueryAxis qa = queryAxes[quaxToSort.getOrdinal()];
    Exp setForAx = qa.getExp();

    // setForAx is the top level Exp of the axis
    // put an Order FunCall around 
    Exp[] args = new Exp[3];
    args[0] = setForAx; // the set to be sorted is the set representing the query axis
    // if we got more than 1 position member, generate a tuple for the 2.arg
    Exp sortExp;
    if (sortPosMembers.length > 1) {
      sortExp = new FunCall("()", (XMLA_Member[]) sortPosMembers, FunCall.TypeParentheses);
    } else {
      sortExp = (XMLA_Member) sortPosMembers[0];
    }
    args[1] = sortExp;
    args[2] = Literal.createString(sortMode2String(sortMode));
    FunCall order = new FunCall("Order", args, FunCall.TypeFunction);
    qa.setExp(order);
  }

  /**
   * add Top/BottomCount Funcall to QueryAxis
   * @param monAx
   * @param nShow
   */
  private void topBottomAxis(ParsedQuery pq, String function) {
    // TopCount(TopCount) and TopCount(Order) is not permitted

    QueryAxis[] queryAxes = pq.getAxes();
    QueryAxis qa = queryAxes[quaxToSort.getOrdinal()];
    Exp setForAx = qa.getExp();
    Exp sortExp;
    // if we got more than 1 position member, generate a tuple
     if (sortPosMembers.length > 1) {
       sortExp = new FunCall("()", (XMLA_Member[]) sortPosMembers, FunCall.TypeParentheses);
     } else {
       sortExp = (XMLA_Member) sortPosMembers[0];
     }
     
    Exp[] args = new Exp[3];
    args[0] = setForAx; // the set representing the query axis
    args[1] = Literal.create(new Integer(topBottomCount));
    args[2] = sortExp;
    FunCall topbottom = new FunCall(function, args, FunCall.TypeFunction);
    qa.setExp(topbottom);     
  }


  /**
  * @param sort mode according to JPivot
  * @return sort mode String according to MDX
  */
  static private String sortMode2String(int sortMode) {
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
        return ""; // should not happen
    }
  }

} // End XMLA_SortRank
