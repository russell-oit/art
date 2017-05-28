/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * Copyright (C) 2003-2004 TONBELLER AG.
 * All Rights Reserved.
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package net.sf.jpivotart.jpivot.mondrian;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.mondrianart.mondrian.olap.AxisOrdinal.StandardAxisOrdinal;

import net.sf.jpivotart.jpivot.olap.model.Axis;
import net.sf.jpivotart.jpivot.olap.model.Hierarchy;
import net.sf.jpivotart.jpivot.olap.model.Visitor;

/**
 * MondrianAxis is an adapter class for the Result Mondrian Axis.
 */
public class MondrianAxis implements Axis {

  private net.sf.mondrianart.mondrian.olap.Axis monAxis = null;
  private MondrianModel model = null;
  private ArrayList<net.sf.jpivotart.jpivot.olap.model.Position> aPositions = null;
  private MondrianHierarchy[] hierarchies = null;
  private int ordinal; // -1 for slicer

  /**
   * Constructor
   * @param monAxis Axis as defined in Mondrian
   */
  public MondrianAxis(int iOrdinal, net.sf.mondrianart.mondrian.olap.Axis monAxis, MondrianModel model) {
    this.ordinal = iOrdinal;
    this.monAxis = monAxis;
    this.model = model;

    aPositions = new ArrayList<>();
    boolean foundQueryHierarchies = true;
    if (iOrdinal >= 0) {
      // it is not the slicer
      // get hierarchies from mondrian query, rather than from result, which can be empty

      MondrianQueryAdapter adapter = (MondrianQueryAdapter) model.getQueryAdapter();
      net.sf.mondrianart.mondrian.olap.Hierarchy[] monHiers = adapter.getMonQuery().getMdxHierarchiesOnAxis(
   		  StandardAxisOrdinal.forLogicalOrdinal(iOrdinal));
      hierarchies = new MondrianHierarchy[monHiers.length];
      for (int j = 0; j < hierarchies.length; j++) {
        // if the axis expr is a function like 
        // {StrToMember('[Time].[1997]')} then Mondrian
        // returns null because the hierarchy is not known
        if (monHiers[j] == null) {
          foundQueryHierarchies = false;
        } else {
          hierarchies[j] = model.lookupHierarchy(monHiers[j].getUniqueName());
        }
      }
    }

    List monPositions = monAxis.getPositions();
    Iterator pit = monPositions.iterator();
    int i = 0;
    while (pit.hasNext()) {
      net.sf.mondrianart.mondrian.olap.Position monPosition = (net.sf.mondrianart.mondrian.olap.Position) pit.next();
      MondrianPosition position = new MondrianPosition(monPosition, iOrdinal, model);
      aPositions.add(position);
      if (iOrdinal == -1 || !foundQueryHierarchies) {
        // for the slicer,  extract the hierarchies from the members
        
        if (i == 0) { 
          // first position only, as all positions have same hierarchies
          // create the hierarchies array
          List<MondrianHierarchy> l = new ArrayList<>();
          Iterator<net.sf.mondrianart.mondrian.olap.Member> mit = monPosition.iterator();
          while (mit.hasNext()) {
            net.sf.mondrianart.mondrian.olap.Member monMember = mit.next();
            l.add(model.lookupHierarchy(monMember.getHierarchy().getUniqueName()));
          }
          hierarchies = (MondrianHierarchy[]) 
                l.toArray(new MondrianHierarchy[l.size()]);
        }
      }
      i++;
    }

  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.model.Axis#getPositions()
   */
  public List<net.sf.jpivotart.jpivot.olap.model.Position> getPositions() {
    return aPositions;
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.model.Axis#getHierarchies()
   */
  public Hierarchy[] getHierarchies() {
    return hierarchies;
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.model.Visitable#accept(Visitor)
   */
  public void accept(Visitor visitor) {
    visitor.visitAxis(this);
  }

  public Object getRootDecoree() {
    return this;
  }

  /**
   * Returns the ordinal.
   * @return int
   */
  public int getOrdinal() {
    return ordinal;
  }

} // MondrianAxis
