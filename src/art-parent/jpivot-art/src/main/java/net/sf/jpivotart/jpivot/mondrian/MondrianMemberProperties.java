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

import net.sf.jpivotart.jpivot.core.ExtensionSupport;
import net.sf.jpivotart.jpivot.olap.model.Level;
import net.sf.jpivotart.jpivot.olap.model.Member;
import net.sf.jpivotart.jpivot.olap.model.MemberPropertyMeta;
import net.sf.jpivotart.jpivot.olap.navi.MemberProperties;

/**
 * get member properties from Mondrian
 */
public class MondrianMemberProperties extends ExtensionSupport implements MemberProperties {

  public MondrianMemberProperties() {
    super.setId(MemberProperties.ID);
  }

  /* get the property definitions for a certain level
   * @see net.sf.jpivotart.jpivot.olap.navi.MemberProperties#getMemberPropertyMetas(net.sf.jpivotart.jpivot.olap.model.Level)
   */
  public MemberPropertyMeta[] getMemberPropertyMetas(Level level) {
    net.sf.mondrianart.mondrian.olap.Level monLevel = ((MondrianLevel) level).getMonLevel();
    net.sf.mondrianart.mondrian.olap.Property[] monProps = monLevel.getProperties();
    if (monProps == null || monProps.length == 0)
      return new MemberPropertyMeta[0];

    String scope = getPropertyScope(monLevel);
    MemberPropertyMeta[] props = new MemberPropertyMeta[monProps.length];
    for (int i = 0; i < props.length; i++) {
      String name = monProps[i].getName();
      String label = monProps[i].getCaption();      
      if (label==null)
          label=name;
       props[i] = new MemberPropertyMeta(label, name, scope);
    }
    return props;
  }

  /**
   * returns the unique name of the level (if levelScope) or hierarchy (if !levelScope)
   * @param monLevel
   * @return the unique name of the level (if levelScope) or hierarchy (if !levelScope)
   */
  private String getPropertyScope(net.sf.mondrianart.mondrian.olap.Level monLevel) {
    return monLevel.getHierarchy().getUniqueName();
  }

  /**
   * @return false
   */
  public boolean isLevelScope() {
    return false;
  }

  public String getPropertyScope(Member m) {
    MondrianLevel level = (MondrianLevel) m.getLevel();
    return getPropertyScope(level.getMonLevel());
  }

  /**
   * sets the visible properties. Optimizing implementations of
   * PropertyHolder may only return these properties.
   * @see net.sf.jpivotart.jpivot.olap.model.PropertyHolder
   */
  public void setVisibleProperties(MemberPropertyMeta[] props) {
    // ignored
  }

} // End MondrianMemberProperties
