package net.sf.wcfart.wcf.param;

import java.util.ArrayList;
import java.util.List;

/**
 * @author av
 * @since 31.01.2005
 */
abstract class SqlExprWithOperands implements SqlExpr {
  ArrayList<SqlExpr> opds = new ArrayList<>();
  public void addOperand(SqlExpr exp) {
    opds.add(exp);
  }
  public List<SqlExpr> getOperands() {
    return opds;
  }
  public Object clone() throws CloneNotSupportedException {
    SqlExprWithOperands p = (SqlExprWithOperands) super.clone();
    //p.opds = (ArrayList)opds.clone();
	//https://stackoverflow.com/questions/20147420/warning-cloning-arraylist-in-java
	p.opds = new ArrayList<>(opds);
    return p;
  }
}
