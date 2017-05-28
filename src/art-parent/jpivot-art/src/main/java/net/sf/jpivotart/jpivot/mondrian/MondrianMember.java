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

import net.sf.mondrianart.mondrian.olap.SchemaReader;

import net.sf.jpivotart.jpivot.olap.model.Alignable;
import net.sf.jpivotart.jpivot.olap.model.Level;
import net.sf.jpivotart.jpivot.olap.model.Member;
import net.sf.jpivotart.jpivot.olap.model.Property;
import net.sf.jpivotart.jpivot.olap.model.Visitor;
import net.sf.jpivotart.jpivot.olap.model.impl.PropertyImpl;
import net.sf.jpivotart.jpivot.olap.query.MDXMember;

/**
 * MondrianMember is an adapter class for the Mondrian Member.  
 */
public class MondrianMember implements Member, MDXMember {

  private net.sf.mondrianart.mondrian.olap.Member monMember;
  private MondrianLevel level;
  private MondrianModel model;
  private Property[] properties = null;

  /**
   * Constructor
   * @param monMember corresponding Mondrian Member
   * @param level Olap hierarchy parent object
   */
  protected MondrianMember(
    net.sf.mondrianart.mondrian.olap.Member monMember,
    MondrianLevel level,
    MondrianModel model) {   
    this.monMember = monMember;
    this.level = level;
    this.model = model;
    level.addMember(this);

    net.sf.mondrianart.mondrian.olap.Property[] props = monMember.getLevel().getProperties();
    if (props != null) {
      properties = new Property[props.length];
      for (int i = 0; i < props.length; i++) {
        MondrianProp prop = new MondrianProp();
        if (props[i].getType() == net.sf.mondrianart.mondrian.olap.Property.Datatype.TYPE_NUMERIC)
          prop.setAlignment(Alignable.Alignment.RIGHT);
        String propName = props[i].getName();
        prop.setName(propName);
        String caption = props[i].getCaption();
        if (caption != null && !caption.equals(propName)){
          // name and caption are different
          // we want to show caption instead of name
          prop.setLabel(caption);
          prop.setMondrianName(propName);
          // if the property has a separate Label, then it does not require normalization
          // since it is to be displayed as-is
          prop.setNormalizable(false);
        } else {
          prop.setLabel(propName);
        }
        String propValue = monMember.getPropertyFormattedValue(propName);
        prop.setValue(propValue);
        properties[i] = prop;
      }
    }

  }

  public String getLabel() {
    return monMember.getCaption();
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.model.Member#getRootDistance()
   */
  public int getRootDistance() {
      SchemaReader scr = model.getSchemaReader();
      net.sf.mondrianart.mondrian.olap.Member m = monMember;
      int rootDistance = 0;
      while (true) {
          m = scr.getMemberParent(m);
          if (m == null)
              return rootDistance;
          rootDistance += 1;
      }
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.model.Member#getLevel()
   */
  public Level getLevel() {
    return level;
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.model.PropertyHolder#getProperties()
   */
  public Property[] getProperties() {

    if (properties == null || properties.length == 0)
      return new Property[0]; // or null ???

    return properties;
  }

  /**
   * @return parent
   * @see net.sf.jpivotart.jpivot.olap.query.MDXMember#getParent()
   */
  public Member getParent() {
    net.sf.mondrianart.mondrian.olap.Member monParent = monMember.getParentMember();
    MondrianMember parent = model.addMember(monParent);
    return parent;
  }

  /** 
   * @return parent unique name
   */
  public String getParentUniqueName() {
    return monMember.getParentUniqueName();
  }

  /**
   * @return true, if it is an "All" member
   */
  public boolean isAll() {
    return monMember.isAll();
  }

  
  /**
   * @see net.sf.jpivotart.jpivot.olap.model.PropertyHolder#getProperty(String)
   */
  public Property getProperty(String name) {

    if (properties == null || properties.length == 0)
      return null;

    for (int i = 0; i < properties.length; i++) {
      if (name.equals(properties[i].getName()))
        return properties[i];
    }

    return null; // not found
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.model.Visitable#accept(Visitor)
   */
  public void accept(Visitor visitor) {
    visitor.visitMember(this);
  }

  /**
   * Returns the corresponding Mondrian Member.
   * @return net.sf.mondrianart.mondrian.olap.Member
   */
  public net.sf.mondrianart.mondrian.olap.Member getMonMember() {
    return monMember;
  }

  /**
   * @return the unique name
   */
  public String getUniqueName() {
    return monMember.getUniqueName();
  }
  /**
  * @return true,if the member is calculated
  */ 
  public boolean isCalculated() {
    return monMember.isCalculated();
  }
  
  public Object getRootDecoree() {
    return this;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj) {
    if (!(obj instanceof MondrianMember))
      return false;
    net.sf.mondrianart.mondrian.olap.Member mm = ((MondrianMember) obj).getMonMember();
    return monMember.equals(mm);
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return monMember.hashCode();
  }
  
  /**
   * a mondrian property can have a caption different from name 
   * we only show the caption
   */
  public static class MondrianProp extends PropertyImpl {
    String mondrianName = null; // only set if different from name
 
    /**
     * @return Returns the mondrianName.
     */
    public String getMondrianName() {
      return mondrianName;
    }

    /**
     * @param mondrianName The mondrianName to set.
     */
    public void setMondrianName(String mondrianName) {
      this.mondrianName = mondrianName;
    }
  } // MondrianProp


} // MondrianMember
