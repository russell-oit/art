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

import net.sf.jpivotart.jpivot.olap.model.Dimension;
import net.sf.jpivotart.jpivot.olap.model.Hierarchy;
import net.sf.jpivotart.jpivot.olap.model.Visitor;
import net.sf.jpivotart.jpivot.olap.query.MDXElement;
import net.sf.wcfart.tbutils.res.Resources;

/**
 * MondrianDimension is an adapter class for the Mondrian Dimension.
 */
public class MondrianDimension implements Dimension, MDXElement {

  private net.sf.mondrianart.mondrian.olap.Dimension monDimension = null;
  private ArrayList aHierarchies;
  MondrianModel model;
  Resources resources;

  protected MondrianDimension(net.sf.mondrianart.mondrian.olap.Dimension monDimension, MondrianModel model) {
    this.monDimension = monDimension;
    this.model = model;
    aHierarchies = new ArrayList();
    resources = Resources.instance(model.getLocale(), MondrianDimension.class);
  }

  /**
   * add Hierarchy
   * @param  hierarchy MondrianHierarchy to be stored
   */
  protected void addHierarchy(MondrianHierarchy hierarchy) {
    aHierarchies.add(hierarchy);
  }

  /**
   * @see net.sf.mondrianart.mondrian.olap.Dimension#getHierarchies()
   */
  public Hierarchy[] getHierarchies() {
    return (Hierarchy[]) aHierarchies.toArray(new MondrianHierarchy[0]);
  }

  public boolean isTime() {
    return monDimension.getDimensionType() == net.sf.mondrianart.mondrian.olap.DimensionType.TimeDimension;
  }

  public boolean isMeasure() {
    return monDimension.isMeasures();
  }

  public String getLabel() {
  	String label = monDimension.getCaption();
    return resources.getOptionalString(label, label);
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.model.Visitable#accept(Visitor)
   */
  public void accept(Visitor visitor) {
    visitor.visitDimension(this);
  }

  public Object getRootDecoree() {
    return this;
  }

	/**
   * @return the unique name
	 * @see net.sf.mondrianart.mondrian.olap.Dimension#getUniqueName()
	 */
	public String getUniqueName() {
		return monDimension.getUniqueName();
	}

	/**
	 * @return the corresponding Mondrian dimension
	 */
	public net.sf.mondrianart.mondrian.olap.Dimension getMonDimension() {
		return monDimension;
	}

} // MondrianDimension
