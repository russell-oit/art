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
import net.sf.jpivotart.jpivot.olap.model.Level;
import net.sf.jpivotart.jpivot.olap.model.Visitor;
import net.sf.jpivotart.jpivot.olap.query.MDXElement;
import net.sf.wcfart.tbutils.res.Resources;

/**
 * MondrianHierarchy is an adapter class for the Mondrian Hierarchy.  
 */
public class MondrianHierarchy implements Hierarchy, MDXElement {

   private net.sf.mondrianart.mondrian.olap.Hierarchy monHierarchy;
   private MondrianDimension dimension;
   private ArrayList<Level> aLevels;
   private MondrianModel model;
   private Resources resources;
   
   /**
    * Constructor
    * @param monHierarchy Mondrian Hierarchy
    * @param dimension parent
    */
   protected MondrianHierarchy( net.sf.mondrianart.mondrian.olap.Hierarchy monHierarchy,
                                 MondrianDimension dimension, MondrianModel model ) {
      this.monHierarchy = monHierarchy;
      this.dimension = dimension;
      this.model = model;
      this.resources = Resources.instance(model.getLocale(), MondrianHierarchy.class);
      aLevels = new ArrayList<>();
      dimension.addHierarchy(this);
   }
   
   /**
    * add level 
    * @param level MondrianLevel
    */
   protected void addLevel(MondrianLevel level) {
      aLevels.add(level);
   }


	/**
	 * @see net.sf.jpivotart.jpivot.olap.model.Hierarchy#getDimension()
	 */
	public Dimension getDimension() {
		return dimension;
	}

	/**
	 * @see net.sf.jpivotart.jpivot.olap.model.Hierarchy#getLevels()
	 */
	public Level[] getLevels() {
		return aLevels.toArray( new MondrianLevel[0] );
	}

   public String getLabel() {
     String label =  monHierarchy.getCaption();
     return resources.getOptionalString(label, label);
   }

  /**
   * @see net.sf.jpivotart.jpivot.olap.model.Visitable#accept(Visitor)
   */
  public void accept(Visitor visitor) {
    visitor.visitHierarchy(this);
  }

	/**
	 * Returns the monHierarchy.
	 * @return net.sf.mondrianart.mondrian.olap.Hierarchy
	 */
	public net.sf.mondrianart.mondrian.olap.Hierarchy getMonHierarchy() {
		return monHierarchy;
	}
  
  public Object getRootDecoree() {
    return this;
  }

	/**
   * @return the unique name
	 * @see net.sf.mondrianart.mondrian.olap.Hierarchy#getUniqueName()
	 */
	public String getUniqueName() {
		return monHierarchy.getUniqueName();
	}
  public boolean hasAll() {
     return monHierarchy.hasAll();
  }
}
