/*
 * ART. A Reporting Tool.
 * Copyright (C) 2018 Enrico Liboni <eliboni@users.sf.net>
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
package art.reportoptions;

import art.enums.ReportEngineCalculator;
import art.enums.SortOrder;
import java.io.Serializable;

/**
 * Reportengine options data column definition
 * 
 * @author Timothy Anyona
 */
public class ReportEngineDataColumn implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String id;
	private ReportEngineCalculator calculator;
	private String calculatorFormatter;
	private SortOrder sortOrder;
	private Integer sortOrderLevel;
	private String valuesFormatter;
	private Integer index;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the calculator
	 */
	public ReportEngineCalculator getCalculator() {
		return calculator;
	}

	/**
	 * @param calculator the calculator to set
	 */
	public void setCalculator(ReportEngineCalculator calculator) {
		this.calculator = calculator;
	}

	/**
	 * @return the calculatorFormatter
	 */
	public String getCalculatorFormatter() {
		return calculatorFormatter;
	}

	/**
	 * @param calculatorFormatter the calculatorFormatter to set
	 */
	public void setCalculatorFormatter(String calculatorFormatter) {
		this.calculatorFormatter = calculatorFormatter;
	}

	/**
	 * @return the sortOrder
	 */
	public SortOrder getSortOrder() {
		return sortOrder;
	}

	/**
	 * @param sortOrder the sortOrder to set
	 */
	public void setSortOrder(SortOrder sortOrder) {
		this.sortOrder = sortOrder;
	}

	/**
	 * @return the sortOrderLevel
	 */
	public Integer getSortOrderLevel() {
		return sortOrderLevel;
	}

	/**
	 * @param sortOrderLevel the sortOrderLevel to set
	 */
	public void setSortOrderLevel(Integer sortOrderLevel) {
		this.sortOrderLevel = sortOrderLevel;
	}

	/**
	 * @return the valuesFormatter
	 */
	public String getValuesFormatter() {
		return valuesFormatter;
	}

	/**
	 * @param valuesFormatter the valuesFormatter to set
	 */
	public void setValuesFormatter(String valuesFormatter) {
		this.valuesFormatter = valuesFormatter;
	}

	/**
	 * @return the index
	 */
	public Integer getIndex() {
		return index;
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(Integer index) {
		this.index = index;
	}
	
}
