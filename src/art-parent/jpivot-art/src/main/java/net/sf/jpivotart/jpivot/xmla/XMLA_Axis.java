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
package net.sf.jpivotart.jpivot.xmla;

import java.util.ArrayList;
import java.util.List;

import net.sf.jpivotart.jpivot.olap.model.Axis;
import net.sf.jpivotart.jpivot.olap.model.Hierarchy;
import net.sf.jpivotart.jpivot.olap.model.Position;
import net.sf.jpivotart.jpivot.olap.model.Visitor;

/**
 * Result Axis XMLA
 */
public class XMLA_Axis implements Axis {

  private String name;

  private int ordinal;
  private int nHier = 0;

  private List<Hierarchy> aHiers = new ArrayList<>();
  private List<Position> aPositions = new ArrayList<>();

  /**
   * c'tor
   * @param name
   */
  XMLA_Axis(int ordinal, String name) {
    this.ordinal = ordinal;
    this.name = name;
  }

  void addHier(XMLA_Hierarchy hier) {
    aHiers.add(hier);
    ++nHier;
  }

  /**
   * Returns the name.
   * @return String
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the nHier.
   * @return int
   */
  public int getNHier() {
    return nHier;
  }

  /**
   * add position
   * @param pos
   */
  void addPosition(XMLA_Position pos) {
    aPositions.add(pos);
  }
  /**
   * @see net.sf.jpivotart.jpivot.olap.model.Axis#getPositions()
   */
  public List<Position> getPositions() {
    return aPositions;
  }
  /**
   * @see net.sf.jpivotart.jpivot.olap.model.Axis#getHierarchies()
   */
  public Hierarchy[] getHierarchies() {
    return aHiers.toArray(new Hierarchy[0]);
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
   * @return the ordinal of the axis , slicer = -1
   */
  public int getOrdinal() {
    return ordinal;
  }

} // End XMLA_Axis
