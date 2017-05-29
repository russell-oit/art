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
package net.sf.jpivotart.jpivot.test.olap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sf.jpivotart.jpivot.olap.model.Displayable;
import net.sf.jpivotart.jpivot.olap.model.Expression;
import net.sf.jpivotart.jpivot.olap.navi.SetParameter;

/**
 * @author av
 */
public class TestSetParameter extends TestExtensionSupport implements SetParameter {
  Map<String, String> params = new HashMap<>();
  
  public void setParameter(String paramName, Expression paramValue) {
    String label = ((Displayable)paramValue).getLabel();
    System.out.println("setting Parameter " + paramName + " to " + label);
    params.put(paramName, label);
  }
  
  /** for scripting */
  public Map<String, String> getDisplayValues() {
    return params;
  }
  
  public String[] getParameterNames() {
    Set<String> keys = params.keySet();
    return keys.toArray(new String[keys.size()]);
  }

}
