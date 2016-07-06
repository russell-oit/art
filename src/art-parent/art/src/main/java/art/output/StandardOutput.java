/*
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
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
import art.enums.ReportFormat;
import art.enums.ColumnType;
import art.job.Job;
import art.reportparameter.ReportParameter;
import art.servlets.Config;
import art.utils.DrilldownLinkHelper;
import art.utils.FilenameHelper;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates standard/tabular output
 *
 * @author Timothy Anyona
 */
public abstract class StandardOutput {

	private static final Logger logger = LoggerFactory.getLogger(StandardOutput.class);

	protected PrintWriter out;
	protected int rowCount;
	protected int resultSetColumnCount;
	protected int totalColumnCount; //resultset column count + drilldown column count
	protected DecimalFormat actualNumberFormatter;
	protected DecimalFormat sortNumberFormatter;
	protected String contextPath;
	protected Locale locale;
	protected boolean evenRow;
	private List<Drilldown> drilldowns;
	protected String reportName;
	private List<ReportParameter> reportParamsList;
	protected String fullOutputFileName;
	private boolean showSelectedParameters;

	/**
	 * @return the showSelectedParameters
	 */
	public boolean isShowSelectedParameters() {
		return showSelectedParameters;
	}

	/**
	 * @param showSelectedParameters the showSelectedParameters to set
	 */
	public void setShowSelectedParameters(boolean showSelectedParameters) {
		this.showSelectedParameters = showSelectedParameters;
	}

	/**
	 * @return the fullOutputFileName
	 */
	public String getFullOutputFileName() {
		return fullOutputFileName;
	}

	/**
	 * @param fullOutputFileName the fullOutputFileName to set
	 */
	public void setFullOutputFileName(String fullOutputFileName) {
		this.fullOutputFileName = fullOutputFileName;
	}

	/**
	 * @return the reportParamsList
	 */
	public List<ReportParameter> getReportParamsList() {
		return reportParamsList;
	}

	/**
	 * @param reportParamsList the reportParamsList to set
	 */
	public void setReportParamsList(List<ReportParameter> reportParamsList) {
		this.reportParamsList = reportParamsList;
	}

	/**
	 * @return the drilldowns
	 */
	public List<Drilldown> getDrilldowns() {
		return drilldowns;
	}

	/**
	 * @param drilldowns the drilldowns to set
	 */
	public void setDrilldowns(List<Drilldown> drilldowns) {
		this.drilldowns = drilldowns;
	}

	/**
	 * @return the reportName
	 */
	public String getReportName() {
		return reportName;
	}

	/**
	 * @param reportName the reportName to set
	 */
	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

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
	 * Sets the output stream
	 *
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

	public String getContentType() {
		return "text/html;charset=utf-8";
	}

	/**
	 * Returns <code>true</code> if page header and footer should be output also
	 *
	 * @return
	 */
	public boolean outputHeaderandFooter() {
		return true;
	}

	/**
	 * Performs any initialization required by the output generator
	 */
	public void init() {

	}

	/**
	 * Outputs the report title
	 */
	public void addTitle() {

	}

	/**
	 * Outputs report parameters
	 *
	 * @param reportParamsList the selected report parameters
	 */
	public void addSelectedParameters(List<ReportParameter> reportParamsList) {

	}

	/**
	 * Performs initialization required before outputting the header
	 */
	public void beginHeader() {

	}

	/**
	 * Outputs a value to the header
	 *
	 * @param value the value to output
	 */
	public abstract void addHeaderCell(String value);

	/**
	 * Outputs a value to the header whose text is left aligned
	 *
	 * @param value the value to output
	 */
	public void addHeaderCellAlignLeft(String value) {
		addHeaderCell(value);
	}

	/**
	 * Performs any cleanup after outputting of the header
	 */
	public void endHeader() {

	}

	/**
	 * Performs any initialization before resultset output begins
	 */
	public void beginRows() {

	}

	/**
	 * Outputs a String value to the current row
	 *
	 * @param value the value to output
	 */
	public abstract void addCellString(String value);

	/**
	 * Outputs numeric value to the current row
	 *
	 * @param value the value to output
	 */
	public abstract void addCellNumeric(Double value);

	/**
	 * Outputs a Date value to the current row
	 *
	 * @param value the value to output
	 */
	public abstract void addCellDate(Date value);

	/**
	 * Closes the current row and opens a new one.
	 */
	public abstract void newRow();

	/**
	 * Closes report output. Any final cleanup should be done here.
	 */
	public abstract void endRows();

	/**
	 * Formats a numberic value for display
	 *
	 * @param value the value to format
	 * @return the string representation to display
	 */
	public String formatNumbericValue(Double value) {
		String formattedValue;

		if (value == null) {
			formattedValue = "0";
		} else {
			formattedValue = actualNumberFormatter.format(value);
		}

		return formattedValue;
	}

	/**
	 * Formats a date value for display
	 *
	 * @param value the value to format
	 * @return the string representation to display
	 */
	public String formatDateValue(Date value) {
		String formattedValue;

		if (value == null) {
			formattedValue = "";
		} else {
			formattedValue = Config.getDateDisplayString(value);
		}

		return formattedValue;
	}

	/**
	 * Returns a value to use to sort date columns
	 *
	 * @param value the actual date
	 * @return the sort value for the date
	 */
	public long getDateSortValue(Date value) {
		long sortValue;

		if (value == null) {
			sortValue = 0;
		} else {
			sortValue = value.getTime();
		}

		return sortValue;
	}

	/**
	 * Generates a tabular report
	 *
	 * @param rs the resultset to use
	 * @param reportFormat the report format to use
	 * @return StandardOutputResult. if successful, rowCount contains the number
	 * of rows in the resultset. if not, message contains the i18n message
	 * indicating the problem
	 * @throws SQLException
	 */
	public StandardOutputResult generateTabularOutput(ResultSet rs, ReportFormat reportFormat)
			throws SQLException {

		logger.debug("Entering generateTabularOutput");

		StandardOutputResult result = new StandardOutputResult();

		//initialize number formatters
		initializeNumberFormatters();

		ResultSetMetaData rsmd = rs.getMetaData();
		resultSetColumnCount = rsmd.getColumnCount();

		int drilldownCount = 0;
		if (drilldowns != null) {
			drilldownCount = drilldowns.size();
		}

		totalColumnCount = resultSetColumnCount + drilldownCount;

		//perform any required output initialization
		init();

		addTitle();

		if (showSelectedParameters) {
			addSelectedParameters(reportParamsList);
		}

		//begin header output
		beginHeader();

		//output header columns for the result set columns
		for (int i = 0; i < resultSetColumnCount; i++) {
			addHeaderCell(rsmd.getColumnLabel(i + 1));
		}

		//output header columns for drill down reports
		//only output drilldown columns for html reports
		if (reportFormat.isHtml() && drilldowns != null) {
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
		List<ColumnType> columnTypes = getColumnTypes(rsmd);

		while (rs.next()) {
			rowCount++;

			newRow();

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
				List<Object> columnValues = outputResultSetColumns(columnTypes, rs);
				outputDrilldownColumns(drilldowns, reportParamsList, columnValues);
			}
		}

		//end data output
		endRows();

		result.setSuccess(true);
		result.setRowCount(rowCount);
		return result;
	}

	/**
	 * Generates burst output
	 *
	 * @param rs the resultset to use
	 * @param reportFormat the report format to use
	 * @param job the job that is generating the burst output
	 * @throws SQLException
	 * @throws java.io.IOException
	 */
	public void generateBurstOutput(ResultSet rs, ReportFormat reportFormat, Job job)
			throws SQLException, IOException {

		logger.debug("Entering generateBurstOutput");

		initializeNumberFormatters();

		ResultSetMetaData rsmd = rs.getMetaData();
		resultSetColumnCount = rsmd.getColumnCount();

		totalColumnCount = resultSetColumnCount;

		int maxRows = Config.getMaxRows(reportFormat.getValue());
		List<ColumnType> columnTypes = getColumnTypes(rsmd);

		String previousBurstId = null;
		FileOutputStream fos = null;

		try {
			while (rs.next()) {
				rowCount++;

				String currentBurstId = rs.getString(1);

				boolean generateNewFile;
				if (rowCount == 1) {
					generateNewFile = true;
				} else if (StringUtils.equals(previousBurstId, currentBurstId)) {
					generateNewFile = false;
				} else {
					generateNewFile = true;
				}

				if (generateNewFile) {
					if (rowCount > 1) {
						//close previous file
						fos = endBurstOutput(fos);
					}

					rowCount = 1;
					previousBurstId = currentBurstId;

					String fileNameBurstId;
					if (currentBurstId == null) {
						fileNameBurstId = "null";
					} else {
						fileNameBurstId = currentBurstId;
					}

					//generate file name to use
					FilenameHelper filenameHelper = new FilenameHelper();
					String baseFileName = filenameHelper.getFileName(job, fileNameBurstId);
					String exportPath = Config.getJobsExportPath();
					String extension;

					extension = reportFormat.getFilenameExtension();

					String fileName = baseFileName + "." + extension;
					fullOutputFileName = exportPath + fileName;

					//create html file to output to as required
					if (reportFormat.isHtml() || reportFormat == ReportFormat.xml
							|| reportFormat == ReportFormat.rss20) {
						fos = new FileOutputStream(fullOutputFileName);
						out = new PrintWriter(new OutputStreamWriter(fos, "UTF-8")); // make sure we make a utf-8 encoded text
					}

					initializeOutput(rsmd);
				}

				newRow();

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

					fos = endBurstOutput(fos);
					previousBurstId = null;
				} else {
					outputResultSetColumns(columnTypes, rs);
				}
			}

			fos = endBurstOutput(fos);
		} finally {
			if (out != null) {
				out.close();
			}

			if (fos != null) {
				fos.close();
			}
		}

	}

	/**
	 * Finalizes burst output
	 *
	 * @param fos the file output stream used, that will be closed
	 * @return the file output stream used
	 * @throws IOException
	 */
	private FileOutputStream endBurstOutput(FileOutputStream fos) throws IOException {
		endRows();

		if (out != null) {
			out.close();
			out = null;
		}

		if (fos != null) {
			fos.close();
			fos = null;
		}

		return fos;
	}

	/**
	 * Initializes actual and sort number formatters
	 */
	private void initializeNumberFormatters() {
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
	}

	/**
	 * Starts output for burst output generation
	 *
	 * @param rsmd the resultset metadata object
	 * @throws SQLException
	 */
	private void initializeOutput(ResultSetMetaData rsmd) throws SQLException {
		//perform any required output initialization
		init();

		addTitle();

		if (showSelectedParameters) {
			addSelectedParameters(reportParamsList);
		}

		//begin header output
		beginHeader();

		//output header columns for the result set columns
		for (int i = 0; i < resultSetColumnCount; i++) {
			addHeaderCell(rsmd.getColumnLabel(i + 1));
		}

		//end header output
		endHeader();

		//begin data output
		beginRows();
	}

	/**
	 * Returns the column types corresponding to the given resultset metadata
	 *
	 * @param rsmd the resultset metadata
	 * @return the column types corresponding to the given resultset metadata
	 * @throws SQLException
	 */
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
			} else if (sqlType == Types.OTHER) {
				columnTypes.add(ColumnType.Other);
			} else {
				columnTypes.add(ColumnType.String);
			}
		}

		return columnTypes;
	}

	/**
	 * Outputs one row for the resultset data
	 *
	 * @param columnTypes the column types for the records
	 * @param rs the resultset with the data to output
	 * @return data for the output row
	 * @throws SQLException
	 */
	private List<Object> outputResultSetColumns(List<ColumnType> columnTypes,
			ResultSet rs) throws SQLException {
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
					addNumeric(value);
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
					addString(value);
					break;
				case Other:
					value = rs.getObject(columnIndex);
					if (value != null) {
						value = value.toString();
					}
					addString(value);
					break;
				default:
					value = rs.getString(columnIndex);
					addString(value);
			}

			columnValues.add(value);
		}

		return columnValues;
	}

	/**
	 * Outputs values for drilldown columns
	 *
	 * @param drilldowns the drilldowns to use, may be null
	 * @param reportParamsList the report parameters
	 * @param columnValues the values from the resultset data
	 * @throws SQLException
	 */
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

	/**
	 * Returns <code>true</code> if the given sql type is a numeric one
	 *
	 * @param sqlType the sql/jdbc type
	 * @return <code>true</code> if the given sql type is a numeric one
	 */
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

	/**
	 * Returns <code>true</code> if the given sql type is a date one
	 *
	 * @param sqlType the sql/jdbc type
	 * @return <code>true</code> if the given sql type is a date one
	 */
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

	/**
	 * Returns <code>true</code> if the given sql type is a clob one
	 *
	 * @param sqlType the sql/jdbc type
	 * @return <code>true</code> if the given sql type is a clob one
	 */
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

	/**
	 * Outputs a string value
	 *
	 * @param value the value to output
	 */
	private void addString(Object value) {
		if (value == null) {
			addCellString(""); //display nulls as empty string
		} else {
			addCellString((String) value);
		}
	}

	/**
	 * Outputs a numeric value
	 *
	 * @param value the value to output
	 */
	private void addNumeric(Object value) {
		if (value == null) {
			//either way, default to displaying empty string or 0 respectively
			//http://sourceforge.net/p/art/discussion/352129/thread/85b90969/
			addCellNumeric(0D); //display nulls as 0
		} else {
			addCellNumeric((Double) value);
		}
	}

	/**
	 * Generates crosstab output
	 *
	 * @param rs the resultset to use
	 * @param reportFormat the report format to use
	 * @return output result
	 * @throws SQLException
	 */
	public StandardOutputResult generateCrosstabOutput(ResultSet rs,
			ReportFormat reportFormat) throws SQLException {

		/*
		 * input
		 */ 		     	 /*
		 * input
		 */
		// A Jan 14			     	  A 1 Jan 1 14
		// A Feb 24			     	  A 1 Feb 2 24
		// A Mar 34			     	  A 1 Mar 3 34
		// B Jan 14			     	  B 2 Jan 1 14
		// B Feb 24			     	  B 2 Feb 2 24
		// C Jan 04			     	  C 3 Jan 1 04
		// C Mar 44			     	  C 3 Mar 3 44
		//				     	    ^-----^------Used to sort the x/y axis

		/*
		 * output
		 */		     	 /*
		 * output
		 */
		//         y-axis		     	 	  y-axis	      
		//           |		     	 	    |				 
		//  x-axis - _   Feb Jan Mar     	   x-axis - _  Jan Feb Mar
		//           A    24  14  34      	 	    A	14  24  34   
		//           B    24  14  -      	 	    B	14  24   -   
		//           C    -   04  44      	 	    C	04   -  44   
		//                   ^--- Jan comes after Feb!			     	 

		StandardOutputResult result = new StandardOutputResult();

		ResultSetMetaData rsmd = rs.getMetaData();
		resultSetColumnCount = rsmd.getColumnCount();

		if (resultSetColumnCount != 3 && resultSetColumnCount != 5) {
			result.setMessage("reports.message.invalidCrosstab");
			return result;
		}

		int maxRows = Config.getMaxRows(reportFormat.getValue());

		boolean alternateSort;

		if (resultSetColumnCount > 3) {
			alternateSort = true;
		} else {
			alternateSort = false;
		}

		HashMap<String, Object> values = new HashMap<>();
		Object[] xa;
		Object[] ya;
		if (alternateSort) { // name1, altSort1, name2, altSort2, value
			TreeMap<Object, Object> x = new TreeMap<>(); // allows a sorted toArray (or Iterator())
			TreeMap<Object, Object> y = new TreeMap<>();

			// Scroll resultset and feed data structures
			// to read it as a crosstab (pivot)
			while (rs.next()) {
				Object DyVal = rs.getObject(1);
				Object Dy = rs.getObject(2);
				Object DxVal = rs.getObject(3);
				Object Dx = rs.getObject(4);
				x.put(Dx, DxVal);
				y.put(Dy, DyVal);
				addValue(Dy.toString() + "-" + Dx.toString(), values, rs, 5, ColumnType.String);
			}

			xa = x.keySet().toArray();
			ya = y.keySet().toArray();

			totalColumnCount = xa.length + 1;

			//perform any required output initialization
			init();

			addTitle();

			if (showSelectedParameters) {
				addSelectedParameters(reportParamsList);
			}

			//begin header output
			beginHeader();

			addHeaderCell(rsmd.getColumnLabel(5) + " (" + rsmd.getColumnLabel(1) + " / " + rsmd.getColumnLabel(3) + ")");
			int i, j;
			for (i = 0; i < xa.length; i++) {
				addHeaderCell(x.get(xa[i]).toString());
			}
			endHeader();
			beginRows();

			//  _ Jan Feb Mar
			for (j = 0; j < ya.length; j++) {
				rowCount++;

				newRow();

				if (rowCount % 2 == 0) {
					evenRow = true;
				} else {
					evenRow = false;
				}

				if (rowCount > maxRows) {
					//row limit exceeded
					for (int k = 0; k < totalColumnCount; k++) {
						addCellString("...");
					}

					endRows();

					result.setMessage("runReport.message.tooManyRows");
					result.setTooManyRows(true);
					return result;
				} else {
					Object Dy = ya[j];
					addHeaderCellAlignLeft(y.get(Dy).toString()); //column 1 data displayed as a header
					for (i = 0; i < xa.length; i++) {
						Object value = values.get(Dy.toString() + "-" + xa[i].toString());
						addString(value);
					}
				}
			}

		} else {
			TreeSet<Object> x = new TreeSet<>(); // allows a sorted toArray (or Iterator())
			TreeSet<Object> y = new TreeSet<>();

			// Scroll resultset and feed data structures
			// to read it as a crosstab (pivot)
			while (rs.next()) {
				Object Dy = rs.getObject(1);
				Object Dx = rs.getObject(2);
				x.add(Dx);
				y.add(Dy);
				addValue(Dy.toString() + "-" + Dx.toString(), values, rs, 3, ColumnType.String);
			}

			xa = x.toArray();
			ya = y.toArray();

			totalColumnCount = xa.length + 1;

			//perform any required output initialization
			init();

			addTitle();

			if (showSelectedParameters) {
				addSelectedParameters(reportParamsList);
			}

			//begin header output
			beginHeader();
			addHeaderCell(rsmd.getColumnLabel(3) + " (" + rsmd.getColumnLabel(1) + " / " + rsmd.getColumnLabel(2) + ")");
			int i, j;
			for (i = 0; i < xa.length; i++) {
				addHeaderCell(xa[i].toString());
			}

			endHeader();
			beginRows();

			//  _ Jan Feb Mar
			for (j = 0; j < ya.length; j++) {
				rowCount++;

				newRow();

				if (rowCount % 2 == 0) {
					evenRow = true;
				} else {
					evenRow = false;
				}

				if (rowCount > maxRows) {
					//row limit exceeded
					for (int k = 0; k < totalColumnCount; k++) {
						addCellString("...");
					}

					endRows();

					result.setMessage("runReport.message.tooManyRows");
					result.setTooManyRows(true);
					return result;
				} else {
					Object Dy = ya[j];
					//o.addHeaderCell(Dy.toString()); //column 1 data displayed as a header
					addHeaderCellAlignLeft(Dy.toString()); //column 1 data displayed as a header
					for (i = 0; i < xa.length; i++) {
						Object value = values.get(Dy.toString() + "-" + xa[i].toString());
						addString(value);
					}
				}
			}
		}

		endRows();

		result.setSuccess(true);
		result.setRowCount(rowCount);
		return result;
	}

	/**
	 * Stores the right object type in the Hashmap used by
	 * generateCrosstabOutput to cache sorted values
	 */
	private static void addValue(String key, Map<String, Object> values,
			ResultSet rs, int columnIndex, ColumnType columnType) throws SQLException {

		Object value = null;

		switch (columnType) {
			case Numeric:
				value = rs.getDouble(columnIndex);
				if (rs.wasNull()) {
					value = null;
				}
				break;
			case Date:
				value = rs.getTimestamp(columnIndex);
				break;
			case Clob:
				Clob clob = rs.getClob(columnIndex);
				if (clob != null) {
					value = clob.getSubString(1, (int) clob.length());
				}
				break;
			default:
				value = rs.getString(columnIndex);
		}

		values.put(key, value);
	}
}
