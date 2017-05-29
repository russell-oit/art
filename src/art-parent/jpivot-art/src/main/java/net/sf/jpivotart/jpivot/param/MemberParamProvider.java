/*
 * Created on 14.12.2004
 */
package net.sf.jpivotart.jpivot.param;

import java.util.List;

import net.sf.jpivotart.jpivot.olap.model.Member;
import net.sf.wcfart.wcf.param.SessionParam;

/**
 * creates a SessionParam from a member
 * @see net.sf.jpivotart.jpivot.param.SqlAccess
 * @see net.sf.wcfart.wcf.param.SessionParam
 *
 */
public class MemberParamProvider extends AbstractParamProvider {
  String paramName;

  /**
   * creates a SessionParam from a member
   * @see net.sf.jpivotart.jpivot.param.SqlAccess
   * @see net.sf.wcfart.wcf.param.SessionParam
   *
   * @param paramName name of the parameter
   */

  public MemberParamProvider(String paramName) {
    this.paramName = paramName;
  }

  protected void addMemberParams(List<SessionParam> list, SqlAccess sa, Member member) {
    SessionParam param = sa.createParameter(member, paramName);
    if (param != null) // !calculated, !all
      list.add(param);
  }

}