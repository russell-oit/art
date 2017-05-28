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
package net.sf.jpivotart.jpivot.navigator.hierarchy;

import javax.servlet.jsp.JspException;

import net.sf.jpivotart.jpivot.table.TableComponent;
import net.sf.wcfart.wcf.component.Component;
import net.sf.wcfart.wcf.component.ComponentTag;
import net.sf.wcfart.wcf.controller.RequestContext;

/**
 * creates a SelectPropertiesTag
 *
 * @author wawan
 */

public class SelectPropertiesTag extends ComponentTag {
	String table;

	/**
	 * creates the select properties component
	 */
	public Component createComponent(RequestContext context) throws JspException {
		TableComponent tableComponent = (TableComponent) context.getModelReference(table);
		if (tableComponent == null)
			throw new JspException("table \"" + table + "\" not found");
		return new SelectProperties(getId(), null, tableComponent);
	}

	/**
	 * Returns the query.
	 *
	 * @return String
	 */
	public String getTable() {
		return table;
	}

	/**
	 * Sets the query.
	 *
	 * @param query
	 *            The query to set
	 */
	public void setTable(String query) {
		this.table = query;
	}

}
