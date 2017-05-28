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

import java.util.ArrayList;

import net.sf.jpivotart.jpivot.olap.model.Hierarchy;
import net.sf.jpivotart.jpivot.olap.model.Level;
import net.sf.jpivotart.jpivot.olap.model.Visitor;
import net.sf.jpivotart.jpivot.olap.query.MDXElement;
import net.sf.jpivotart.jpivot.olap.query.MDXLevel;
import net.sf.wcfart.tbutils.res.Resources;

/**
 * Level implementation for Mondrian.
 * MondrianLevel is an adapter class for the Mondrian Level.
 */
public class MondrianLevel implements Level, MDXElement, MDXLevel {

  private net.sf.mondrianart.mondrian.olap.Level monLevel;
  private MondrianHierarchy hierarchy;
  private ArrayList<MondrianMember> aMembers;
  private MondrianModel model;
  private Resources resources;

  /**
   * Constructor
   * @param monLevel corresponding Mondrian Level
   * @param hierarchy parent object
   * @param model Model
   */
  protected MondrianLevel(
    net.sf.mondrianart.mondrian.olap.Level monLevel,
    MondrianHierarchy hierarchy,
    MondrianModel model) {
    this.monLevel = monLevel;
    this.hierarchy = hierarchy;
    this.model = model;
    this.resources = Resources.instance(model.getLocale(), MondrianLevel.class);
    aMembers = new ArrayList<>();
    hierarchy.addLevel(this);
  }

  /**
   * add member to level
   * @param member MondrianMember
   */
  protected void addMember(MondrianMember member) {
    aMembers.add(member);
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.model.Level#getHierarchy()
   */
  public Hierarchy getHierarchy() {
    return hierarchy;
  }

  public String getLabel() {
    String label = monLevel.getCaption();
    return resources.getOptionalString(label, label);
  }

  /**
   * @return the level's depth (root level = 0)
   * @see net.sf.jpivotart.jpivot.olap.query.MDXLevel#getDepth()
   */
  public int getDepth() {
    return monLevel.getDepth();
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.model.Visitable#accept(Visitor)
   */
  public void accept(Visitor visitor) {
    visitor.visitLevel(this);
  }

  public Object getRootDecoree() {
    return this;
  }

  /**
   * @return the assigned Mondrian Level
   */
  public net.sf.mondrianart.mondrian.olap.Level getMonLevel() {
    return monLevel;
  }

  /**
   * @return the level's unique name
   */
  public String getUniqueName() {
    return monLevel.getUniqueName();
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.query.MDXLevel#isAll()
   */
  public boolean isAll() {
    return monLevel.isAll();
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.query.MDXLevel#hasChildLevel()
   */
  public boolean hasChildLevel() {
    return (monLevel.getChildLevel() != null);
  }

} // MondrianLevel
