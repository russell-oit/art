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
package art.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents c3.js chart types
 *
 * @author Timothy Anyona
 */
public enum C3ChartType {
	//http://c3js.org/reference.html#data-type
	Line("line"), Spline("spline"), Step("step"), Area("area"),
	AreaSpline("areaSpline"), AreaStep("areaStep"), Bar("bar"),
	Scatter("scatter"), Pie("pie"), Donut("donut"), Gauge("gauge");

	private final String value;

	private C3ChartType(String value) {
		this.value = value;
	}

	/**
	 * Returns the c3 chart type string
	 *
	 * @return the c3 chart type string
	 */
	public String getC3Type() {
		switch (this) {
			case AreaSpline:
				return "area-spline";
			case AreaStep:
				return "area-step";
			default:
				return value;
		}
	}

	/**
	 * Returns the plotly chart type string
	 *
	 * @return the plotly chart type string
	 */
	public String getPlotlyType() {
		switch (this) {
			case Line:
				return "scatter";
			case Donut:
				return "pie";
			default:
				return value;
		}
	}

	/**
	 * Returns the plotly chart mode string
	 *
	 * @return the plotly chart mode string
	 */
	public String getPlotlyMode() {
		switch (this) {
			case Line:
				return "lines+markers";
			case Scatter:
				return "markers";
			default:
				return "";
		}
	}

	/**
	 * Returns this enum option's value
	 *
	 * @return this enum option's value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Returns all enum options
	 *
	 * @return all enum options
	 */
	public static List<C3ChartType> list() {
		//use a new list as Arrays.asList() returns a fixed-size list. can't add or remove from it
		List<C3ChartType> items = new ArrayList<>();
		items.addAll(Arrays.asList(values()));
		return items;
	}

	/**
	 * Returns chart types applicable for plotly charts
	 *
	 * @return chart types applicable for plotly charts
	 */
	public static List<C3ChartType> getPlotlyChartTypes() {
		List<C3ChartType> items = new ArrayList<>();
		items.add(Line);
		items.add(Bar);
		items.add(Scatter);
		items.add(Pie);
		items.add(Donut);
		return items;
	}

	/**
	 * Convert a value to an enum. If the conversion fails, an exception is
	 * thrown
	 *
	 * @param value the value to convert
	 * @return the enum option that corresponds to the value
	 */
	public static C3ChartType toEnum(String value) {
		for (C3ChartType v : values()) {
			if (v.value.equalsIgnoreCase(value)) {
				return v;
			}
		}
		throw new IllegalArgumentException("Invalid chart type: " + value);
	}

	/**
	 * Returns this enum option's description
	 *
	 * @return enum option description
	 */
	public String getDescription() {
		return value;
	}

	/**
	 * Returns this enum option's i18n message string for use in the user
	 * interface
	 *
	 * @return this enum option's i18n message string
	 */
	public String getLocalizedDescription() {
		return "c3.chartType." + value;
	}
}
