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
package net.sf.jpivotart.jpivot.mondrian.script;

import net.sf.mondrianart.mondrian.olap.Util.PropertyList;
import net.sf.mondrianart.mondrian.rolap.RolapConnectionProperties;

import net.sf.jpivotart.jpivot.core.ExtensionSupport;
import net.sf.jpivotart.jpivot.mondrian.MondrianCell;
import net.sf.jpivotart.jpivot.mondrian.MondrianModel;
import net.sf.jpivotart.jpivot.olap.model.Cell;
import net.sf.jpivotart.jpivot.olap.navi.DrillThrough;
import net.sf.wcfart.wcf.table.TableModel;
/**
 * @author Engineering Ingegneria Informatica S.p.A. - Luca Barozzi
 *
 * Implementation of the DrillExpand Extension for Mondrian Data Source.
 */
public class ScriptableMondrianDrillThrough extends ExtensionSupport implements DrillThrough {

  private boolean extendedContext = true;

  /**
   * Constructor sets ID
   */
  public ScriptableMondrianDrillThrough() {
    super.setId(DrillThrough.ID);
  }

  /**
   * drill through is possible if <code>member</code> is not calculated
   */
  public boolean canDrillThrough(Cell cell) {
    return ((MondrianCell) cell).getMonCell().canDrillThrough();
    //String sql = ((MondrianCell) cell).getMonCell().getDrillThroughSQL(extendedContext);
    //return sql != null;
  }

  /**
   * does a drill through, retrieves data that makes up the selected Cell
   */
  public TableModel drillThrough(Cell cell) {
    String sql = ((MondrianCell) cell).getMonCell().getDrillThroughSQL(extendedContext);
    if (sql == null) { throw new NullPointerException("DrillThroughSQL returned null"); }
    ScriptableMondrianDrillThroughTableModel dtm = new ScriptableMondrianDrillThroughTableModel();
    dtm.setSql(sql);
    String connectString = getConnection().getConnectString();
    PropertyList connectInfo = net.sf.mondrianart.mondrian.olap.Util.parseConnectString(connectString);
    String jdbcUrl = connectInfo.get(RolapConnectionProperties.Jdbc.name());
    dtm.setJdbcUrl(jdbcUrl);
    String jdbcUser = connectInfo.get(RolapConnectionProperties.JdbcUser.name());
    dtm.setJdbcUser(jdbcUser);
    String jdbcPassword = connectInfo.get(RolapConnectionProperties.JdbcPassword.name());
    dtm.setJdbcPassword(jdbcPassword);
    String dataSourceName = connectInfo.get(RolapConnectionProperties.DataSource.name());
    dtm.setDataSourceName(dataSourceName);
    String catalog = connectInfo.get(RolapConnectionProperties.Catalog.name());
    String catalogExtension = catalog.replaceFirst(".*/", "").replaceFirst("\\.xml$", ".ext.xml");
    dtm.setCatalogExtension(catalogExtension);
    return dtm;
  }

  /**
   * gets the mondrian connection
   * @return the mondrian connection
   */
  public net.sf.mondrianart.mondrian.olap.Connection getConnection() {
    MondrianModel model = (MondrianModel) getModel();
    return model.getConnection();
  }

  public boolean isExtendedContext() {
    return extendedContext;
  }

  public void setExtendedContext(boolean extendedContext) {
    this.extendedContext = extendedContext;
  }

}
