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
package net.sf.jpivotart.jpivot.olap.mdxparse;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * can be any MDX object
 */
public class CompoundId implements Exp {

  private ArrayList<NamePart> names = new ArrayList<>();

  /**
   * c'tor
   * @param name
   * @param isKey
   */
  public CompoundId(String name, boolean isKey) {
    names.add(new NamePart(name, isKey));
  }

  public CompoundId(String name) {
    this(name, false);
  }

  public void append(String name, boolean isKey) {
    names.add(new NamePart(name, isKey));
  }

  public void append(String name) {
    names.add(new NamePart(name, false));
  }

  public String[] toStringArray() {
    String[] ret = new String[names.size()];
    int i = 0;
    for (NamePart np : names) {
      ret[i++] = np.name;
    }

    return ret;
  }

  private class NamePart {
    private String name;
    private boolean isKey;
    protected NamePart(String name, boolean isKey) {
      this.name = name;
      this.isKey = isKey;
    }
  }

  /**
   * format to MDX
   * @see Exp
   */ 
  public String toMdx() {
    String str = "";
    boolean isFollow = false;
    for (NamePart np : names) {
      if (isFollow)
        str += ".";
      isFollow = true;
      str += np.name;
    }

    return str;
  }
  
  private CompoundId(){
    // default
  }
  
  /**
   * 
   * @see java.lang.Object#clone()
   */
  public Object clone() {
    CompoundId cloned = new CompoundId();
    for (NamePart np : names) {
      cloned.append(np.name, np.isKey);
    } 
    return cloned;
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.mdxparse.Exp#accept
   */
  public void accept(ExpVisitor visitor) {
    visitor.visitCompoundId(this);
  }

} // End CompoundId

