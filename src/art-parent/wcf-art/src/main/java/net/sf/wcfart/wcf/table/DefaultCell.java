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
package net.sf.wcfart.wcf.table;

/**
 * default cells that are rendered by the DefaultCellRenderer.
 */

public class DefaultCell implements Comparable<DefaultCell> {
  private String URL;
  private Object value;
  private String target;
  private String image;
  private String onClick;

  public DefaultCell(String URL, Object value) {
    this.URL = URL;
    this.value = value;
    this.image = null;
  }

  /**
   * creates a clickable
   */
  public DefaultCell(String URL, Object value, String imageUrl) {
    this.URL = URL;
    this.value = value;
    this.image = imageUrl;
  }

  /** the href=".." attribute of the &lt;a ...&gt; element */
  public String getURL() {
    return URL;
  }
  /** the href=".." attribute of the &lt;a ...&gt; element */
  public void setURL(String newURL) {
    URL = newURL;
  }
  public void setValue(Object newValue) {
    value = newValue;
  }
  public Object getValue() {
    return value;
  }
  /** the target=".." attribute of the &lt;a ...&gt; element */
  public void setTarget(String newTarget) {
    target = newTarget;
  }
  /** the target=".." attribute of the &lt;a ...&gt; element */
  public String getTarget() {
    return target;
  }
  /** the onClick=".." attribute of the &lt;a ...&gt; element */
  public void setOnClick(String newOnClick) {
    onClick = newOnClick;
  }
  /** the onClick=".." attribute of the &lt;a ...&gt; element */
  public String getOnClick() {
    return onClick;
  }
  public int compareTo(DefaultCell other) {
    if (value != null){
		@SuppressWarnings("unchecked")
		int result = ((Comparable<Object>)value).compareTo(other.value);
      return result;
	}
    if (image != null)
      return image.compareTo(other.image);
    return URL.compareTo(other.URL);
  }
  public void setImage(String newImage) {
    image = newImage;
  }
  public String getImage() {
    return image;
  }
}