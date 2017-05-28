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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.MissingResourceException;

import javax.servlet.jsp.JspException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import net.sf.wcfart.wcf.component.Component;
import net.sf.wcfart.wcf.component.ComponentTag;
import net.sf.wcfart.wcf.controller.RequestContext;
import net.sf.wcfart.wcf.selection.DefaultSelectionModel;
import net.sf.wcfart.wcf.selection.SelectionModel;
import net.sf.wcfart.wcf.utils.ResourceLocator;
import net.sf.wcfart.wcf.utils.XmlUtils;

/**
 * Created on 15.11.2002
 *
 * @author av
 */
public class TableComponentTag extends ComponentTag {
	
	private static final long serialVersionUID = 1L;
  
  private static Logger logger = Logger.getLogger(TableComponentTag.class);
  
  String  model    = null;
  String  selmode  = "multi";
  boolean closable = true;
  boolean pagable  = true;
  boolean sortable = true;
  int     pagesize = 10;
  String editForm  = null;
  boolean editable = false;
  boolean colHeaders = true;
  
 
  public Component createComponent(RequestContext context) throws JspException {
    try {
      TableModel tm = EmptyTableModel.instance();
      if (model != null) {
        tm = (TableModel)context.getModelReference(model);
        if (tm == null)
          throw new JspException("table " + model + " not found");
      }

      String tableId = editForm  == null ? id : id + ".table";
      TableComponent tableComp = createTable(tableId, tm);
      tableComp.setPageable(pagable);
      tableComp.setSortable(sortable);
      tableComp.setClosable(closable);
      tableComp.setColHeaders(colHeaders);
      
      DefaultSelectionModel dsm = new DefaultSelectionModel();
      if ("href".equals(selmode))
        dsm.setMode(SelectionModel.SINGLE_SELECTION_HREF);
      else if ("single".equals(selmode))
        dsm.setMode(SelectionModel.SINGLE_SELECTION);
      else if ("multi".equals(selmode))
        dsm.setMode(SelectionModel.MULTIPLE_SELECTION);
      else
        dsm.setMode(SelectionModel.NO_SELECTION);
      tableComp.setSelectionModel(dsm);
      tableComp.setPageSize(pagesize);
      
      if (editForm != null) {
        // @DEPRECATED - use editable instead
        URL url = ResourceLocator.getResource(context.getServletContext(), context.getLocale(), editForm);
        Document doc = XmlUtils.parse(url);
        TablePropertiesFormComponent formComp = new TablePropertiesFormComponent(id + ".form", null, doc, tableComp);
        formComp.setVisible(false);
        formComp.setCloseable(true);
        return new EditableTableComponent(id, null, tableComp, formComp);
      }
      else if (editable)
        return EditableTableComponent.instance(context, id, tableComp);
      return tableComp;

    } catch (MalformedURLException e) {
      logger.error(id, e);
      throw new JspException(e);
    } catch (MissingResourceException e) {
      logger.error(id, e);
      throw new JspException(e);
    }
  }

  protected TableComponent createTable(String tableId, TableModel tm) {
    return new TableComponent(tableId, null, tm);  
  }

  public void setModel(String model) {
    this.model = model;
  }

  public void setSelmode(String string) {
    selmode = string;
  }

  public void setClosable(boolean b) {
    closable = b;
  }

  public void setPagable(boolean b) {
    pagable = b;
  }

  public void setSortable(boolean b) {
    sortable = b;
  }

  public void setPagesize(int i) {
    pagesize = i;
  }

  public String getModel() {
    return model;
  }

  public void setEditForm(String string) {
    editForm = string;
  }

  public void setEditable(boolean editable) {
    this.editable = editable;
  }
  
  public boolean isColHeaders() {
    return colHeaders;
  }
  
  public void setColHeaders(boolean colHeaders) {
    this.colHeaders = colHeaders;
  }
}
