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
package net.sf.wcfart.wcf.convert;

import java.util.Map;

import org.w3c.dom.Node;

import net.sf.wcfart.wcf.format.FormatException;
import net.sf.wcfart.wcf.format.Formatter;
import org.apache.commons.fileupload.FileItem;

/**
 * converts bean property values to DOM ELements representing HTML forms.
 *
 * @author av
 */
public interface Converter {

  /**
   * converts request parameters to DOM element attributes and bean properties
   */
  void validate(Map<String, String[]> httpParamSource, Map<String, FileItem[]> fileParamSource, Node domTarget, Object beanTarget) throws ConvertException, FormatException;

  /**
   * converts bean properties to DOM element attributes
   */
  void revert(Object beanSource, Node domTarget) throws ConvertException;

  /**
   * sets the formatter to use for conversions
   */
  void setFormatter(Formatter formatter);

  /**
   * returns the formatter used for conversions. This may
   * be used to add new handlers for application specific
   * data types.
   */
  Formatter getFormatter();
}
