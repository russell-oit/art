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
package art.reportoptions;

import art.utils.ArtUtils;

/**
 * Represents report options for datatables reports
 *
 * @author Timothy Anyona
 */
public class DataTablesOptions extends CsvServerOptions {

	private boolean showColumnFilters;
	private String inputDateFormat = ArtUtils.ISO_DATE_FORMAT;
	private String outputDateFormat = ""; //if blank, no formatting will be done
	private String inputDateTimeFormat = ArtUtils.ISO_DATE_TIME_MILLISECONDS_FORMAT;
	private String outputDateTimeFormat = ""; //if blank, no formatting will be done

	/**
	 * @return the showColumnFilters
	 */
	public boolean isShowColumnFilters() {
		return showColumnFilters;
	}

	/**
	 * @param showColumnFilters the showColumnFilters to set
	 */
	public void setShowColumnFilters(boolean showColumnFilters) {
		this.showColumnFilters = showColumnFilters;
	}

	/**
	 * @return the inputDateFormat
	 */
	public String getInputDateFormat() {
		return inputDateFormat;
	}

	/**
	 * @param inputDateFormat the inputDateFormat to set
	 */
	public void setInputDateFormat(String inputDateFormat) {
		this.inputDateFormat = inputDateFormat;
	}

	/**
	 * @return the outputDateFormat
	 */
	public String getOutputDateFormat() {
		return outputDateFormat;
	}

	/**
	 * @param outputDateFormat the outputDateFormat to set
	 */
	public void setOutputDateFormat(String outputDateFormat) {
		this.outputDateFormat = outputDateFormat;
	}

	/**
	 * @return the inputDateTimeFormat
	 */
	public String getInputDateTimeFormat() {
		return inputDateTimeFormat;
	}

	/**
	 * @param inputDateTimeFormat the inputDateTimeFormat to set
	 */
	public void setInputDateTimeFormat(String inputDateTimeFormat) {
		this.inputDateTimeFormat = inputDateTimeFormat;
	}

	/**
	 * @return the outputDateTimeFormat
	 */
	public String getOutputDateTimeFormat() {
		return outputDateTimeFormat;
	}

	/**
	 * @param outputDateTimeFormat the outputDateTimeFormat to set
	 */
	public void setOutputDateTimeFormat(String outputDateTimeFormat) {
		this.outputDateTimeFormat = outputDateTimeFormat;
	}

}
