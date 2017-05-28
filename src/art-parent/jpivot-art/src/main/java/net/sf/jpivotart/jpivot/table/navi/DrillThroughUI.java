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
package net.sf.jpivotart.jpivot.table.navi;

import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import net.sf.jpivotart.jpivot.core.ModelChangeEvent;
import net.sf.jpivotart.jpivot.core.ModelChangeListener;
import net.sf.jpivotart.jpivot.olap.model.Cell;
import net.sf.jpivotart.jpivot.olap.model.OlapModel;
import net.sf.jpivotart.jpivot.olap.navi.DrillThrough;
import net.sf.jpivotart.jpivot.table.CellBuilder;
import net.sf.jpivotart.jpivot.table.CellBuilderDecorator;
import net.sf.jpivotart.jpivot.table.TableComponent;
import net.sf.jpivotart.jpivot.table.TableComponentExtensionSupport;
import net.sf.jpivotart.jpivot.mondrian.MondrianDrillThroughTableModel;
import net.sf.wcfart.wcf.component.RendererParameters;
import net.sf.wcfart.wcf.controller.Dispatcher;
import net.sf.wcfart.wcf.controller.DispatcherSupport;
import net.sf.wcfart.wcf.controller.RequestContext;
import net.sf.wcfart.wcf.controller.RequestListener;
import net.sf.wcfart.wcf.table.EditableTableComponent;
import net.sf.wcfart.wcf.table.EmptyTableModel;
import net.sf.wcfart.wcf.table.ITableComponent;
import net.sf.wcfart.wcf.table.TableColumn;
import net.sf.wcfart.wcf.table.TableModel;
import net.sf.wcfart.wcf.table.TableModelDecorator;
import net.sf.wcfart.wcf.utils.DomUtils;

/**
 *
 * @author Robin Bagot
 */
public class DrillThroughUI extends TableComponentExtensionSupport implements ModelChangeListener {

  boolean available;
  boolean renderActions;
  Dispatcher dispatcher = new DispatcherSupport();
  DrillThrough extension;

  TableModelDecorator tableModel = new TableModelDecorator(EmptyTableModel.instance());

  public static final String ID = "drillThrough";
  public String getId() {
    return ID;
  }

  public void initialize(RequestContext context, TableComponent table) throws Exception {
    super.initialize(context, table);
    table.getOlapModel().addModelChangeListener(this);

    // does the underlying data model support drill?
    if (!initializeExtension()) {
      available = false;
      return;
    }
    available = true;

    // extend the controller
    table.getDispatcher().addRequestListener(null, null, dispatcher);

    // add some decorators via table.get/setRenderer
    CellBuilder cb = table.getCellBuilder();
    DomDecorator cr = new DomDecorator(table.getCellBuilder());
    table.setCellBuilder(cr);

  }

  public void startBuild(RequestContext context) {
    super.startBuild(context);
    renderActions = RendererParameters.isRenderActions(context);
    if (renderActions)
      dispatcher.clear();
  }

  class DomDecorator extends CellBuilderDecorator {

    DomDecorator(CellBuilder delegate) {
      super(delegate);
    }

    public Element build(Cell cell, boolean even) {
      Element parent = super.build(cell, even);

      if (!enabled || !renderActions || extension == null)
        return parent;

      String id = DomUtils.randomId();
      if (canDrillThrough(cell) && (!cell.isNull())) {
        // add a drill through child node to cell element
        Element elem = table.insert("drill-through", parent);
        elem.setAttribute("id", id);
        elem.setAttribute("title", "Show source data");
        dispatcher.addRequestListener(id, null, new DrillThroughHandler(cell));
      } else {
        // dont add anything
      }

      return parent;
    }
  }

  class DrillThroughHandler implements RequestListener {
    Cell cell;
    DrillThroughHandler(Cell cell) {
      this.cell = cell;
    }
    public void request(RequestContext context) throws Exception {
      if (canDrillThrough(cell)) {
        HttpSession session = context.getSession();
        final String drillTableRef = table.getOlapModel().getID() + ".drillthroughtable";
        ITableComponent tc =
          (ITableComponent) session.getAttribute(drillTableRef);
        // get a new drill through table model
        TableModel tm = drillThrough(cell);
        tc.setModel(tm);
        tc.setVisible(true);
        TableColumn[] tableColumns = null;
        if (tc instanceof EditableTableComponent) {
          tableColumns =
              ((EditableTableComponent) tc).getTableComp().getTableColumns();
        } else if (tc instanceof net.sf.wcfart.wcf.table.TableComponent) {
          tableColumns = ((net.sf.wcfart.wcf.table.TableComponent) tc).getTableColumns();
        }
        if (tableColumns != null) {
          for (int i = 0; i < tableColumns.length; i++) {
            TableColumn tableColumn = tableColumns[i];
            tableColumn.setHidden(false);
          }
        }
      }
    }
  }

  /** @return true if extension is available */
  protected boolean initializeExtension() {
    OlapModel om = table.getOlapModel();
    extension = (DrillThrough) om.getExtension(DrillThrough.ID);
    return extension != null;
  }

  protected boolean canDrillThrough(Cell cell) {
    return extension.canDrillThrough((Cell) cell.getRootDecoree());
  }

  /**
   * returns a DrillThroughTableModel object for the drill through
   * @param cell
   * @return a DrillThroughTableModel object for the drill through
   */
  protected TableModel drillThrough(Cell cell) {
    return extension.drillThrough((Cell) cell.getRootDecoree());
  }

  public boolean isAvailable() {
    return available;
  }

  public void modelChanged(ModelChangeEvent e) {
  }

  public void structureChanged(ModelChangeEvent e) {
    initializeExtension();
    dispatcher.clear();
  }

  public TableModel getTableModel() {
    return tableModel;
  }
}
