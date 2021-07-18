/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.datatables;

import java.io.Serializable;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Timothy Anyona
 */
public class DataTablesRequest implements Serializable {
	//https://stackoverflow.com/questions/28197817/spring-mvc-datatables-1-10-parameters-binding
	//https://github.com/darrachequesne/spring-data-jpa-datatables#5-fix-the-serialization--deserialization-of-the-query-parameters
	//https://github.com/davioooh/datatables-pagination/blob/master/datatables-pagination/src/main/java/com/davioooh/datatablespagination/model/TablePage.java
	//https://datatables.net/manual/server-side
	//https://datatables.net/examples/server_side/
	//https://www.opencodez.com/java/datatable-with-spring-boot.htm
	//http://www.studywithdemo.com/2014/12/datatable-server-side-processing-using-java.html
	//https://github.com/DataTables/DataTablesSrc/blob/master/examples/server_side/scripts/ssp.class.php
	//https://makitweb.com/how-to-add-custom-filter-in-datatable-ajax-and-php/
	//https://www.webslesson.info/2018/09/add-server-side-datatables-custom-filter-using-php-with-ajax.html

	private static final long serialVersionUID = 1L;

	private int draw;
	private int start;
	private int length;
	private Search search;
	private List<Order> order;
	private List<Column> columns;

	/**
	 * @return the draw
	 */
	public int getDraw() {
		return draw;
	}

	/**
	 * @param draw the draw to set
	 */
	public void setDraw(int draw) {
		this.draw = draw;
	}

	/**
	 * @return the start
	 */
	public int getStart() {
		return start;
	}

	/**
	 * @param start the start to set
	 */
	public void setStart(int start) {
		this.start = start;
	}

	/**
	 * @return the length
	 */
	public int getLength() {
		return length;
	}

	/**
	 * @param length the length to set
	 */
	public void setLength(int length) {
		this.length = length;
	}

	/**
	 * @return the search
	 */
	public Search getSearch() {
		return search;
	}

	/**
	 * @param search the search to set
	 */
	public void setSearch(Search search) {
		this.search = search;
	}

	/**
	 * @return the order
	 */
	public List<Order> getOrder() {
		return order;
	}

	/**
	 * @param order the order to set
	 */
	public void setOrder(List<Order> order) {
		this.order = order;
	}

	/**
	 * @return the columns
	 */
	public List<Column> getColumns() {
		return columns;
	}

	/**
	 * @param columns the columns to set
	 */
	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}

	/**
	 * Returns true if the request has a search value
	 * 
	 * @return true if the request has a search value
	 */
	public boolean hasSearchValue() {
		boolean hasSearchValue = false;

		if (search != null && StringUtils.isNotBlank(search.getValue())) {
			hasSearchValue = true;
		}

		if (!hasSearchValue && columns != null) {
			for (Column column : columns) {
				if (column.isSearchable() && column.getSearch() != null
						&& StringUtils.isNotBlank(column.getSearch().getValue())) {
					hasSearchValue = true;
					break;
				}
			}
		}

		return hasSearchValue;
	}

}
