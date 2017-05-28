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
package net.sf.jpivotart.jpivot.olap.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import net.sf.wcfart.tbutils.res.Resources;

/**
 * Misc helpers
 * @author av
 */
public class OlapUtils {

  static final Set<String> infix = new HashSet<>();
  static final Set<String> prefix = new HashSet<>();
  static {
    String[] s = new String[] { "and", "or", "xor", "*", "/", "+", "-", "%", "<", ">", "<=", ">=",
        "<>", "="};
    for (int i = 0; i < s.length; i++)
      infix.add(s[i]);

    prefix.add("-");
    prefix.add("not");
  };

  static Resources resources = Resources.instance(OlapUtils.class);
  static final Set<String> singleRecordLevelNames = new HashSet<>();
  static {
    String s = resources.getString("single.record.level");
    StringTokenizer st = new StringTokenizer(s);
    while (st.hasMoreTokens())
      singleRecordLevelNames.add(st.nextToken());
  }

  private OlapUtils() {
  }

  /**
   * returns a matrix[rows][columns] of cells
   * @param result - a 0, 1, or 2 dimensional Result
   * @return a matrix containing the result cells
   * @see #getCellList
   */
  public static Cell[][] getCellMatrix(Result result) {
    int rows, cols;
    switch (result.getAxes().length) {
    case 0:
      cols = 1;
      rows = 1;
      break;
    case 1:
      cols = result.getAxes()[0].getPositions().size();
      rows = 1;
      break;
    case 2:
      cols = result.getAxes()[0].getPositions().size();
      rows = result.getAxes()[1].getPositions().size();
      break;
    default:
      throw new IllegalArgumentException("result must be 0,1 or 2 dimensional");
    }
    Iterator it = result.getCells().iterator();
    Cell[][] matrix = new Cell[rows][cols];
    for (int row = 0; row < rows; row++)
      for (int col = 0; col < cols; col++)
        matrix[row][col] = (Cell) it.next();
    return matrix;
  }

  /**
   * creates a list of cells out of a matrix
   * @see #getCellMatrix
   */
  public static List<Cell> getCellList(Cell[][] matrix) {
    if (matrix.length == 0)
      return Collections.emptyList();
    int rows = matrix.length;
    int cols = matrix[0].length;
    List<Cell> list = new ArrayList<>(rows * cols);
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        list.add(matrix[row][col]);
      }
    }
    return list;
  }

  public static Cell[][] transposeCellMatrix(Cell[][] oldCells) {
    int rows = oldCells.length;
    int cols = oldCells[0].length;
    Cell[][] newCells = new Cell[cols][rows];
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        newCells[col][row] = oldCells[row][col];
      }
    }
    return newCells;
  }

  /**
   * returns the number of members on each position
   */
  public static int countHierarchies(Axis axis) {
    List positions = axis.getPositions();
    if (positions.size() == 0)
      return 0;
    return ((Position) positions.get(0)).getMembers().length;
  }

  public static Level getParentLevel(Level level) {
    Hierarchy hier = level.getHierarchy();
    Level[] levels = hier.getLevels();
    for (int i = 1; i < levels.length; i++)
      if (level.equals(levels[i]))
        return levels[i - 1];
    return null;
  }

  public static Level getChildLevel(Level level) {
    Hierarchy hier = level.getHierarchy();
    Level[] levels = hier.getLevels();
    for (int i = 0; i < levels.length - 1; i++)
      if (level.equals(levels[i]))
        return levels[i + 1];
    return null;
  }

  /**
   * compare member array
   * @param aMem1
   * @param aMem2
   * @return true if member arrays compare
   */
  public static boolean compareMembers(Member[] aMem1, Member[] aMem2) {
    if (aMem1.length != aMem2.length)
      return false;
    for (int i = 0; i < aMem1.length; i++) {
      // any null does not compare
      if (aMem1[i] == null)
        return false;
      if (!aMem1[i].equals(aMem2[i]))
        return false;
    }
    return true;
  }

  /**
   * return the dimensions that are displayed on a visible axis
   */
  public static Set<Dimension> getVisibleDimensions(OlapModel model) throws OlapException {
    Set<Dimension> visible = new HashSet<>();
    Axis[] axes = model.getResult().getAxes();
    for (int i = 0; i < axes.length; i++) {
      Hierarchy[] hiers = axes[i].getHierarchies();
      for (int j = 0; j < hiers.length; j++)
        visible.add(hiers[j].getDimension());
    }
    return visible;
  }

  /**
   * return the dimensions that are on the slicer axis (all that are not visible)
   */
  public static Set<Dimension> getSlicerDimensions(OlapModel model) throws OlapException {
    Set<Dimension> visible = getVisibleDimensions(model);
    Set<Dimension> slicer = new HashSet<>();
    Dimension[] dims = model.getDimensions();
    for (int i = 0; i < dims.length; i++) {
      if (!visible.contains(dims[i]))
        slicer.add(dims[i]);
    }
    return slicer;
  }

  /**
   * check, whether a function name matches an infis function like "+" or "-" 
   * @param fuName
   * @return whether a function name matches an infis function like "+" or "-" 
   */
  public static boolean isInfixFunction(String fuName) {
    return infix.contains(fuName);
  }

  /**
   * check, whether a function name matches an prefix function like "not" or "unary -" 
   * @param fuName
   * @return whether a function name matches an prefix function like "not" or "unary -" 
   */
  public static boolean isPrefixFunction(String fuName) {
    return prefix.contains(fuName);
  }

  /** 
   * true if for every member of the lowest level of <code>hier</code>
   * there is a corresponding row in the fact table. If so, the
   * Hierarchy is handled differently in the GUI.
   */
  public static boolean isSingleRecord(Hierarchy hier) {
    Level lowest = getLowestLevel(hier);
    return singleRecordLevelNames.contains(lowest.getLabel());
  }

  /** 
   * true if for every member of the lowest level of any hierarchy in <code>dim</code>
   * there is a corresponding row in the fact table. If so, the
   * Hierarchy is handled differently in the GUI.
   */
  public static boolean isSingleRecord(Dimension dim) {
    Hierarchy[] hiers = dim.getHierarchies();
    for (int i = 0; i < hiers.length; i++)
      if (isSingleRecord(hiers[i]))
        return true;
    return false;
  }

  public static Level getLowestLevel(Hierarchy hier) {
    Level[] levels = hier.getLevels();
    return levels[levels.length - 1];
  }

  /**
   * Return a list of active dimensions on the slicer 
   */
  public static Set<Dimension> getActiveSlicerDimensions(OlapModel model) throws OlapException {
    Set<Dimension> active = new HashSet<>();
    Axis slicer = model.getResult().getSlicer();
    Hierarchy[] hiers = slicer.getHierarchies();
    for (int j = 0; j < hiers.length; j++) {
            active.add(hiers[j].getDimension());
    }
    return active;
  }

  /**
   * Return a list of active dimensions on the slicer 
   */
  public static Set<Hierarchy> getActiveSlicerHierarchies(OlapModel model) throws OlapException {
    Set<Hierarchy> active = new HashSet<>();
    Axis slicer = model.getResult().getSlicer();
    Hierarchy[] hiers = slicer.getHierarchies();
    for (int j = 0; j < hiers.length; j++)
      active.add(hiers[j]);
    return active;
  }

  /**
   * return the hierarchies that are on the slicer axis 
   (all that are not visible)
   */
  public static Set getSlicerHierarchies(OlapModel model) throws OlapException {
    Set visible = getVisibleDimensions(model);
           
    /* Get dimensions and hierarchies on the slicer */
    
    Axis slicer = model.getResult().getSlicer();
    Set<Dimension> selectedSlicerDims = new HashSet<>();
    Set<Hierarchy> selectedSlicerHiers = new HashSet<>();
    
    List positions = slicer.getPositions();

    for (Iterator iter = positions.iterator(); iter.hasNext();) {
      Position pos = (Position) iter.next();
      Member[] posMembers = pos.getMembers();
      for (int i = 0; i < posMembers.length; i++) {
          Hierarchy hier = posMembers[i].getLevel().getHierarchy();
          Dimension dim = hier.getDimension();
          
          if (!selectedSlicerHiers.contains(hier))
              selectedSlicerHiers.add(hier);
          
          if (!selectedSlicerDims.contains(dim))
              selectedSlicerDims.add(dim);          
      }
    }
    /* Return hierarchies that are not on the rows or columns and for the selected
     * members, return the selected hierarchy
     */
    Set<Hierarchy> slicerHiers = new HashSet<>();
    Dimension[] dims = model.getDimensions();
    for (int i = 0; i < dims.length; i++) {
      if (!visible.contains(dims[i])) {
        if (!selectedSlicerDims.contains(dims[i])) {
            slicerHiers.add(dims[i].getHierarchies()[0]);
        } else {
          for (Hierarchy hier : selectedSlicerHiers) {
            Dimension dim = hier.getDimension();
            if (dim.equals(dims[i])) {
                slicerHiers.add(hier);
              break;
            }
          }
        }

      }
    }
    
    return slicerHiers;
  }

}

 	  	 
