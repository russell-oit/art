/*
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.output;

import art.drilldown.Drilldown;
import art.enums.DisplayNull;
import art.enums.ReportFormat;
import art.enums.ColumnType;
import art.reportparameter.ReportParameter;
import art.servlets.Config;
import art.utils.ActionResult;
import art.utils.DrilldownLinkHelper;
import java.io.PrintWriter;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Timothy Anyona
 */
public abstract class StandardOutput {

	private static final Logger logger = LoggerFactory.getLogger(StandardOutput.class);

	protected PrintWriter out;
	protected int rowCount;
	protected int totalColumnCount; //resultset column count + drilldown column count
	protected DecimalFormat actualNumberFormatter;
	protected DecimalFormat sortNumberFormatter;
	protected String contextPath;
	protected Locale locale;
	protected boolean evenRow;

	/**
	 * @return the locale
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @param locale the locale to set
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * @return the writer
	 */
	public PrintWriter getWriter() {
		return out;
	}

	/**
	 * Set the output stream.
	 * <br>Use it to print something: <br>
	 * <code>o.println("Hello Word!"); </code><br>
	 * will print <i>Hello Word!</i> to the user browser
	 *
	 * @param writer
	 */
	public void setWriter(PrintWriter writer) {
		this.out = writer;
	}

	/**
	 * @return the rowCount
	 */
	public int getRowCount() {
		return rowCount;
	}

	/**
	 * @param rowCount the rowCount to set
	 */
	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	/**
	 * @return the maxRows
	 */
	public int getMaxRows() {
		return maxRows;
	}

	/**
	 * This method is invoked to set the maximum number of rows allowed to be
	 * output
	 *
	 * @param maxRows maximum number of rows to be output
	 */
	public void setMaxRows(int maxRows) {
		this.maxRows = maxRows;
	}

	/**
	 * @return the contextPath
	 */
	public String getContextPath() {
		return contextPath;
	}

	/**
	 * @param contextPath the contextPath to set
	 */
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	/**
	 * This method is invoked to state that the header begins. Output class
	 * should do initialization here
	 */
	public abstract void beginHeader();

	/**
	 * This method is invoked to set a column header name (from the result set
	 * meta data).
	 *
	 * @param value column header name
	 */
	public abstract void addHeaderCell(String value);

	/**
	 * Add a header cell whose text is left aligned
	 *
	 * @param value
	 */
	public void addHeaderCellLeftAligned(String value) {
		addHeaderCell(value);
	}

	/**
	 * Method invoked to state that the header finishes.
	 */
	public void endHeader() {

	}

	/**
	 * Method invoked to state that the result set rows begin.
	 */
	public void beginRows() {

	}

	/**
	 * Method invoked to add a String value in the current row.
	 *
	 * @param value value to output
	 */
	public abstract void addCellString(String value);

	/**
	 * Method invoked to add a numeric value in the current row.
	 *
	 * @param value value to output
	 */
	public abstract void addCellNumeric(Double value);

	/**
	 * Method invoked to add a Date value in the current row.
	 *
	 * @param value value to output
	 */
	public abstract void addCellDate(Date value);

	/**
	 * Method invoked to close the current row and open a new one.
	 * <br>This method should return true if the new row is allocatable, false
	 * if it is not possible to proceed (for example MaxRows reached or an error
	 * raised). If false is returned, the output generator will stop feeding the
	 * object, it will call endRows() and close the result set.
	 *
	 * @return <code>true</code> if can proceed to next record
	 */
	public abstract void newRow();

	/**
	 * This method is invoked when the last row has been flushed.
	 * <br> Usually, here the total number of rows are printed and open streams
	 * (files) are closed.
	 */
	public abstract void endRows();

	/**
	 * Output query results
	 *
	 * @return StandardOutputResult. if successful, rowCount contains the number
	 * of rows in the resultset. if not, message contains the i18n message
	 * indicating the problem
	 * @throws SQLException
	 */
	public StandardOutputResult generateTabularOutput(ResultSet rs, List<Drilldown> drilldowns,
			List<ReportParameter> reportParamsList, ReportFormat reportFormat) throws SQLException {

		StandardOutputResult result = new StandardOutputResult();

		//initialize number formatters
		actualNumberFormatter = (DecimalFormat) NumberFormat.getInstance(locale);
		actualNumberFormatter.applyPattern("#,##0.#");

		//specifically use english locale for sorting e.g.
		//in case user locale uses dot as thousands separator e.g. italian, german locale
		sortNumberFormatter = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
		sortNumberFormatter.applyPattern("#.#");
		//set minimum digits to ensure all numbers are pre-padded with zeros,
		//so that sorting works correctly
		sortNumberFormatter.setMinimumIntegerDigits(20);

		ResultSetMetaData rsmd = rs.getMetaData();
		int resultSetColumnCount = rsmd.getColumnCount();

		int drilldownCount = 0;
		if (drilldowns != null) {
			drilldownCount = drilldowns.size();
		}

		totalColumnCount = resultSetColumnCount + drilldownCount;

		//begin header output
		beginHeader();

		//output header columns for the result set columns
		for (int i = 0; i < resultSetColumnCount; i++) {
			addHeaderCell(rsmd.getColumnLabel(i + 1));
		}

		//output header columns for drill down reports
		if (drilldowns != null) {
			for (Drilldown drilldown : drilldowns) {
				String drilldownTitle = drilldown.getHeaderText();
				if (drilldownTitle == null || drilldownTitle.trim().length() == 0) {
					drilldownTitle = drilldown.getDrilldownReport().getName();
				}
				addHeaderCell(drilldownTitle);
			}
		}

		//end header output
		endHeader();

		//begin data output
		beginRows();

		int maxRows = Config.getMaxRows(reportFormat.getValue());
		DisplayNull displayNullSetting = Config.getSettings().getDisplayNull();
		List<ColumnType> columnTypes = getColumnTypes(rsmd);

		while (rs.next()) {
			rowCount++;

			if (rowCount % 2 == 0) {
				evenRow = true;
			} else {
				evenRow = false;
			}

			if (rowCount > maxRows) {
				//row limit exceeded
				for (int i = 0; i < totalColumnCount; i++) {
					addCellString("...");
				}
				
				endRows();
				
				result.setMessage("runReport.message.tooManyRows");
				result.setTooManyRows(true);
				return result;
			} else {
				List<Object> columnValues = outputResultSetColumns(columnTypes, rs, displayNullSetting);
				outputDrilldownColumns(drilldowns, reportParamsList, columnValues);
			}
		}

		endRows();

		result.setSuccess(true);
		result.setRowCount(rowCount);
		return result;
	}

	private List<ColumnType> getColumnTypes(ResultSetMetaData rsmd) throws SQLException {
		List<ColumnType> columnTypes = new ArrayList<>();
		for (int i = 0; i < rsmd.getColumnCount(); i++) {
			int sqlType = rsmd.getColumnType(i + 1);
			if (isNumeric(sqlType)) {
				columnTypes.add(ColumnType.Numeric);
			} else if (isDate(sqlType)) {
				columnTypes.add(ColumnType.Date);
			} else if (isClob(sqlType)) {
				columnTypes.add(ColumnType.Clob);
			} else {
				columnTypes.add(ColumnType.String);
			}
		}

		return columnTypes;
	}

	private List<Object> outputResultSetColumns(List<ColumnType> columnTypes, ResultSet rs, DisplayNull displayNullSetting) throws SQLException {
		//save column values for use in drill down columns.
		//for the jdbc-odbc bridge, you can only read
		//column values ONCE and in the ORDER they appear in the select
		List<Object> columnValues = new ArrayList<>();

		int columnIndex = 0;
		for (ColumnType columnType : columnTypes) {
			columnIndex++;
			Object value = null;

			switch (columnType) {
				case Numeric:
					value = rs.getDouble(columnIndex);
					if (rs.wasNull()) {
						value = null;
					}
					addNumeric(value, displayNullSetting);
					break;
				case Date:
					value = rs.getTimestamp(columnIndex);
					addCellDate((Date) value);
					break;
				case Clob:
					Clob clob = rs.getClob(columnIndex);
					if (clob != null) {
						value = clob.getSubString(1, (int) clob.length());
					}
					addString(value, displayNullSetting);
					break;
				default:
					value = rs.getString(columnIndex);
					addString(value, displayNullSetting);
			}

			columnValues.add(value);
		}

		return columnValues;
	}

	private void outputDrilldownColumns(List<Drilldown> drilldowns,
			List<ReportParameter> reportParamsList, List<Object> columnValues)
			throws SQLException {

		//output columns for drill down reports
		if (drilldowns != null) {
			for (Drilldown drilldown : drilldowns) {
				DrilldownLinkHelper drilldownLinkHelper = new DrilldownLinkHelper(drilldown, reportParamsList);
				String drilldownUrl = drilldownLinkHelper.getDrilldownLink(columnValues.toArray());

				String drilldownText = drilldown.getLinkText();
				if (StringUtils.isBlank(drilldownText)) {
					drilldownText = "Drill Down";
				}

				String drilldownTag;
				String targetAttribute = "";
				if (drilldown.isOpenInNewWindow()) {
					//open drill down in new window
					targetAttribute = "target='_blank'";
				}
				drilldownTag = "<a href='" + drilldownUrl + "' " + targetAttribute + ">" + drilldownText + "</a>";
				addCellString(drilldownTag);
			}
		}
	}

	private boolean isNumeric(int sqlType) {
		boolean numeric;

		switch (sqlType) {
			case Types.NUMERIC:
			case Types.DECIMAL:
			case Types.FLOAT:
			case Types.REAL:
			case Types.DOUBLE:
			case Types.INTEGER:
			case Types.TINYINT:
			case Types.SMALLINT:
			case Types.BIGINT:
				numeric = true;
				break;
			default:
				numeric = false;
		}

		return numeric;
	}

	private boolean isDate(int sqlType) {
		boolean date;

		switch (sqlType) {
			case Types.DATE:
			case Types.TIME:
			case Types.TIMESTAMP:
				date = true;
				break;
			default:
				date = false;
		}

		return date;
	}

	private boolean isClob(int sqlType) {
		boolean clob;

		switch (sqlType) {
			case Types.CLOB:
			case Types.NCLOB:
				clob = true;
				break;
			default:
				clob = false;
		}

		return clob;
	}

	private void addString(Object value, DisplayNull displayNullSetting) {
		if (value == null) {
			if (displayNullSetting == DisplayNull.Yes) {
				addCellString(null); //display nulls as "null"
			} else {
				addCellString(""); //display nulls as empty string
			}
		} else {
			addCellString((String) value);
		}
	}

	private void addNumeric(Object value, DisplayNull displayNullSetting) {
		if (value == null) {
			if (displayNullSetting == DisplayNull.NoNumbersAsBlank) {
				addCellString(""); //display nulls as empty string
			} else {
				addCellNumeric(0.0D); //display nulls as 0
			}
		} else {
			addCellNumeric((Double) value);
		}
	}
}
