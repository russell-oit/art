/*
 * ART. A Reporting Tool.
 * Copyright (C) 2019 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.selfservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.itfsw.query.builder.support.model.JsonRule;
import java.io.Serializable;
import java.util.List;

/**
 * Represents self service options
 * 
 * @author Timothy Anyona
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SelfServiceOptions implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private List<String> columns;
	private JsonRule rule;
	private String columnsString;

	/**
	 * @return the columnsString
	 */
	public String getColumnsString() {
		return columnsString;
	}

	/**
	 * @param columnsString the columnsString to set
	 */
	public void setColumnsString(String columnsString) {
		this.columnsString = columnsString;
	}

	/**
	 * @return the rule
	 */
	public JsonRule getRule() {
		return rule;
	}

	/**
	 * @param rule the rule to set
	 */
	public void setRule(JsonRule rule) {
		this.rule = rule;
	}

	/**
	 * @return the columns
	 */
	public List<String> getColumns() {
		return columns;
	}

	/**
	 * @param columns the columns to set
	 */
	public void setColumns(List<String> columns) {
		this.columns = columns;
	}
	
}
