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

import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import net.sf.jpivotart.jpivot.core.Model;
import net.sf.jpivotart.jpivot.core.ModelFactory;
import net.sf.jpivotart.jpivot.olap.model.OlapException;
import net.sf.jpivotart.jpivot.olap.model.OlapModel;
import net.sf.jpivotart.jpivot.tags.OlapModelTag;
import net.sf.wcfart.wcf.controller.RequestContext;

/**
 * JSP tag for XMLA Olap Model
 */
public class XMLA_OlapModelTag extends OlapModelTag {
  private static Logger logger = Logger.getLogger(XMLA_OlapModelTag.class);
 
  private String config = null;
  private String uri = null; //"http://TBNTSRV3/XML4A/msxisapi.dll";
  private String catalog = null; //"Foodmart 2000";
  private String dataSource = null; //"Provider=MSOLAP;Data Source=local";

  protected OlapModel getOlapModel(RequestContext context) throws SAXException, IOException, OlapException {

    URL url;
    if (config == null)
      url = getClass().getResource("config.xml");
    else
      url = pageContext.getServletContext().getResource(config);

    // let Digester create a model from config input
    // the config input stream MUST refer to the XMLA_Model class
    // <model class="com.tonbeller.bii.xmla.XMLA_Model"> is required
    Model model = ModelFactory.instance(url);

    if (!(model instanceof XMLA_Model))
      throw new OlapException("invalid class attribute for model tag, resource=" + config);

    XMLA_Model xm = (XMLA_Model) model;
    xm.setUri(uri);
    xm.setDataSource(dataSource);
    xm.setCatalog(catalog);
    xm.setMdxQuery(getBodyContent().getString());

    return xm;
  }

  /**
   * Returns the config.
   * @return String
   */
  public String getConfig() {
    return config;
  }

  /**
   * Sets the config.
   * @param config The config to set
   */
  public void setConfig(String config) {
    this.config = config;
  }

  /**
   * Returns the catalog.
   * @return String
   */
  public String getCatalog() {
    return catalog;
  }

  /**
   * Returns the dataSource.
   * @return String
   */
  public String getDataSource() {
    return dataSource;
  }

  /**
   * Returns the uri.
   * @return String
   */
  public String getUri() {
    return uri;
  }

  /**
   * Sets the catalog.
   * @param catalog The catalog to set
   */
  public void setCatalog(String catalog) {
    this.catalog = catalog;
  }

  /**
   * Sets the dataSource.
   * @param dataSource The dataSource to set
   */
  public void setDataSource(String dataSource) {
    this.dataSource = dataSource;
  }

  /**
   * Sets the uri.
   * @param uri The uri to set
   */
  public void setUri(String uri) {
    this.uri = uri;
  }

} // End XMLA_OlapModelTag
