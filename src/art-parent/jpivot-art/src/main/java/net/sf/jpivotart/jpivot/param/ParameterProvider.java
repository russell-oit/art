/*
 * Created on 14.12.2004
 *
 */
package net.sf.jpivotart.jpivot.param;

import java.util.List;

import net.sf.jpivotart.jpivot.olap.model.Member;
import net.sf.jpivotart.jpivot.olap.model.OlapModel;


/**
 * creates a list of SessionParameter from a member.
 * @see net.sf.wcfart.wcf.param.SessionParam
 */
public interface ParameterProvider {
  public List createSessionParams(OlapModel model, Member member) throws Exception;
}