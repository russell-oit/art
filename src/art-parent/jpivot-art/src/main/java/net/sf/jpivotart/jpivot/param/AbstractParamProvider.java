/*
 * Copyright (c) 1971-2003 TONBELLER AG, Bensheim.
 * All rights reserved.
 */
package net.sf.jpivotart.jpivot.param;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.jpivotart.jpivot.olap.model.Member;
import net.sf.jpivotart.jpivot.olap.model.OlapException;
import net.sf.jpivotart.jpivot.olap.model.OlapModel;
import net.sf.wcfart.wcf.param.SessionParam;

public abstract class AbstractParamProvider implements ParameterProvider {

  public List<SessionParam> createSessionParams(OlapModel model, Member member) throws OlapException {
    SqlAccess sa = (SqlAccess) model.getExtension(SqlAccess.ID);
    if (sa == null)
      return Collections.emptyList();
    List<SessionParam> list = new ArrayList<>();
    addMemberParams(list, sa, member);
    return list;
  }

  protected abstract void addMemberParams(List<SessionParam> list, SqlAccess sa, Member member);
}
