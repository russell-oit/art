/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.Map;

/**
 * Represents options for standard/jfreechart reports
 *
 * @author Timothy Anyona
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JFreeChartOptions implements Serializable {

	private static final long serialVersionUID = 1L;

	private Map<String, String> seriesColors;
	private String dateFormat;
	private boolean dynamicSeries;
	private double upperMargin;

	/**
	 * @return the upperMargin
	 */
	public double getUpperMargin() {
		return upperMargin;
	}

	/**
	 * @param upperMargin the upperMargin to set
	 */
	public void setUpperMargin(double upperMargin) {
		this.upperMargin = upperMargin;
	}

	/**
	 * @return the dynamicSeries
	 */
	public boolean isDynamicSeries() {
		return dynamicSeries;
	}

	/**
	 * @param dynamicSeries the dynamicSeries to set
	 */
	public void setDynamicSeries(boolean dynamicSeries) {
		this.dynamicSeries = dynamicSeries;
	}

	/**
	 * @return the seriesColors
	 */
	public Map<String, String> getSeriesColors() {
		return seriesColors;
	}

	/**
	 * @param seriesColors the seriesColors to set
	 */
	public void setSeriesColors(Map<String, String> seriesColors) {
		this.seriesColors = seriesColors;
	}

	/**
	 * @return the dateFormat
	 */
	public String getDateFormat() {
		return dateFormat;
	}

	/**
	 * @param dateFormat the dateFormat to set
	 */
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

}
