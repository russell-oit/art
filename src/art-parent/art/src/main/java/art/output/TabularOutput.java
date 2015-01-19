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
import art.reportparameter.ReportParameter;
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
public abstract class TabularOutput {

	private static final Logger logger = LoggerFactory.getLogger(TabularOutput.class);

	protected PrintWriter out;
	protected int rowCount;
	protected int totalColumnCount; //resultset column count + drilldown column count
	protected int maxRows;
	protected DecimalFormat actualNumberFormatter;
	protected DecimalFormat sortNumberFormatter;
	protected String contextPath;
	protected Locale locale;

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
	public abstract void addHeaderCellLeftAligned(String value);

	/**
	 * Method invoked to state that the header finishes.
	 */
	public abstract void endHeader();

	/**
	 * Method invoked to state that the result set rows begin.
	 */
	public abstract void beginRows();

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
	public abstract boolean newRow();

	/**
	 * This method is invoked when the last row has been flushed.
	 * <br> Usually, here the total number of rows are printed and open streams
	 * (files) are closed.
	 */
	public abstract void endRows();

	/**
	 * Output query results
	 *
	 * @return ActionResult. if successful, data contains the number of rows in
	 * the resultset. if not, message contains the i18n message indicating the
	 * problem
	 * @throws SQLException
	 */
	public ActionResult generateTabularOutput(ResultSet rs, List<Drilldown> drilldowns,
			List<ReportParameter> reportParamsList) throws SQLException {

		ActionResult result = new ActionResult();

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

		while (rs.next()) {
			rowCount++;

			if (!newRow()) {
				//couldn't create new line. row limit exceeded
				//for xlsx, it's also possible that an error occurred.
				//just show one message. if error occurred, it will be logged
				result.setMessage("tooManyRows");
				return result;
			}

			//save column values for use in drill down columns.
			//for the jdbc-odbc bridge, you can only read
			//column values ONCE and in the ORDER they appear in the select
			List<Object> columnValues = new ArrayList<>();
			for (int i = 0; i < resultSetColumnCount; i++) {
				int sqlType = rsmd.getColumnType(i + 1);
				Object value = null;
				if (isNumeric(sqlType)) {
					value = rs.getDouble(i + 1);
					if (rs.wasNull()) {
						value = null;
					}
					addCellNumeric((Double)value);
				} else if (isDate(sqlType)) {
					value = rs.getTimestamp(i + 1);
					addCellDate((Date)value);
				} else if (isClob(sqlType)) {
					Clob clob = rs.getClob(i + 1);
					if (clob != null) {
						try {
							value = clob.getSubString(1, (int) clob.length());
						} catch (SQLException ex) {
							logger.error("Error", ex);
							value = "Error getting CLOB data: " + ex;
						}
					}
					addCellString((String)value);
				} else {
					//treat as string
					value = rs.getString(i + 1);
					addCellString((String)value);
				}
				columnValues.add(value);
			}

			//output columns for drill down reports			
			if (drilldowns != null) {
				for (Drilldown drilldown : drilldowns) {
					DrilldownLinkHelper drilldownLinkHelper=new DrilldownLinkHelper(drilldown, reportParamsList);
					String drilldownUrl=drilldownLinkHelper.getDrilldownLink(columnValues.toArray());
					
					String drilldownText = drilldown.getLinkText();
					if (StringUtils.isBlank(drilldownText)) {
						drilldownText = "Drill Down";
					}
					
					String drilldownTag;
					if (drilldown.isOpenInNewWindow()) {
						//open drill down in new window
						drilldownTag="<a href='" + drilldownUrl + "' target='_blank'>" + drilldownText + "</a>";
					} else {
						//open in same window
						drilldownTag="<a href='" + drilldownUrl + "'>" + drilldownText + "</a>";
					}
					addCellString(drilldownTag);
				}
			}

		}

		endRows();

		result.setSuccess(true);
		result.setData(rowCount);
		return result;
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
}
