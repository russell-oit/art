/*
 * Created on 14.12.2004
 */
package net.sf.jpivotart.jpivot.param;

import java.util.List;

import net.sf.jpivotart.jpivot.olap.model.Member;
import net.sf.wcfart.wcf.param.SessionParam;

/**
 * creates a SessionParam from a member property
 * @see net.sf.jpivotart.jpivot.param.SqlAccess
 * @see net.sf.wcfart.wcf.param.SessionParam
 *
 */
public class PropertyParamProvider extends AbstractParamProvider {
  private String paramName;
  private String propertyName;

  /**
   * creates a SessionParam from a member property. The SQL Value will be the
   * value of the property, the MDX value will be the member.
   * @see net.sf.jpivotart.jpivot.param.SqlAccess
   * @see net.sf.wcfart.wcf.param.SessionParam
   *
   * @param paramName name of the parameter
   * @param propertyName name of the member property whose value will become the SQL value of the parameter
   */

  public PropertyParamProvider(String paramName, String propertyName) {
    this.paramName = paramName;
    this.propertyName = propertyName;
  }

  protected void addMemberParams(List<SessionParam> list, SqlAccess sa, Member member) {
    SessionParam param = sa.createParameter(member, paramName, propertyName);
    if (param != null) // !calculated, !all
      list.add(param);
  }

}