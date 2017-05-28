package net.sf.wcfart.wcf.param;

/**
 * @author av
 * @since 31.01.2005
 */
public class SessionParam implements Cloneable {

  String displayName;
  String displayValue;
  String name;
  SqlExpr sqlExpr;
  String mdxValue;
  String textValue;
  
  /**
   * returns a text value as entered in the jsp 
	 * @return a text value as entered in the jsp 
   */
  public String getTextValue() {
    return textValue;
  }
  
  /**
   * sets a text value as entered in the jsp
	 * @param textValue text value
   */
  public void setTextValue(String textValue) {
    this.textValue = textValue;
  }

  /**
   * returns the value returned by the expression parser. 
   * For example the unique name like "[Customers].[Name].[Andreas Voss]"
	 * @return the value returned by the expression parser
   */
  public String getMdxValue() {
    return mdxValue;
  }

  /**
   * sets the value returned by the expression parser. 
   * For example the unique name like "[Customers].[Name].[Customer 287]"
	 * @param mdxValue mdx value
   */
  public void setMdxValue(String mdxValue) {
    this.mdxValue = mdxValue;
  }
  
  /**
   * get sql expr
   * @return sql expr
   */
  public SqlExpr getSqlExpr() {
    return sqlExpr;
  }
  
  /**
   * set sql expr
   * @param sqlExpr sql expr
   */
  public void setSqlExpr(SqlExpr sqlExpr) {
    this.sqlExpr = sqlExpr;
  }
  
  /**
   * shorthand for getting/setting the sqlValue of a SqlEqualExpr
	 * @return sql value
   */
  public Object getSqlValue() {
    if (sqlExpr == null)
      return null;
    if (!(sqlExpr instanceof SqlEqualExpr))
      throw new IllegalStateException("SqlEqualExpr required");
    return ((SqlEqualExpr)sqlExpr).getSqlValue();
  }
  
  /**
   * shorthand for getting/setting the sqlValue of a SqlEqualExpr
	 * @param sqlValue sql value
   */
  public void setSqlValue(Object sqlValue) {
    SqlEqualExpr expr = new SqlEqualExpr();
    expr.setSqlValue(sqlValue);
    setSqlExpr(expr);
  }

  /**
   * returns the name of the parameter for display to the user. 
   * For example "Customer"
	 * @return the name of the parameter for display to the user
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * sets the name of the parameter for display to the user
   * For example "Customer"
	 * @param displayName the name of the parameter for display to the user
   */
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }
  
  /**
   * returns the parameter value for display to the user
   * For example "Andreas Voss"
	 * @return the parameter value for display to the user
   */
  public String getDisplayValue() {
    return displayValue;
  }

  /**
   * sets the parameter value for display to the user
   * For example "Andreas Voss"
	 * @param displayValue
   */
  public void setDisplayValue(String displayValue) {
    this.displayValue = displayValue;
  }

  /**
   * returns the name that identifies this parameter within the {@link SessionParamPool}.
   * For example, the customer ID.
	 * @return the name that identifies this parameter within the {@link SessionParamPool}.
   */
  public String getName() {
    return name;
  }

  /**
   * sets the name that identifies this parameter within the {@link SessionParamPool}.
   * For example, the customer ID.
	 * @param name the name that identifies this parameter within the {@link SessionParamPool}.
   */
  public void setName(String name) {
    this.name = name;
  }
  
  public Object clone() throws CloneNotSupportedException {
    SessionParam p = (SessionParam) super.clone();
    SqlExpr x = (SqlExpr) sqlExpr.clone();
    p.setSqlExpr(x);
    return p;
  }

}
