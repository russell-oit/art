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
package art.output;

import art.drilldown.Drilldown;
import art.enums.ReportFormat;
import art.enums.ColumnType;
import art.job.Job;
import art.report.Report;
import art.reportoptions.GeneralReportOptions;
import art.reportoptions.Reporti18nOptions;
import art.reportparameter.ReportParameter;
import art.servlets.Config;
import art.utils.ArtUtils;
import art.drilldown.DrilldownLinkHelper;
import art.reportoptions.StandardOutputOptions;
import art.runreport.GroovyDataDetails;
import art.runreport.ReportOptions;
import art.runreport.RunReportHelper;
import art.utils.ExpressionHelper;
import art.utils.FilenameHelper;
import art.utils.FinalFilenameValidator;
import groovy.sql.GroovyRowResult;
import java.io.File;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

/**
 * Generates standard/tabular output
 *
 * @author Timothy Anyona
 */
public abstract class StandardOutput {

	private static final Logger logger = LoggerFactory.getLogger(StandardOutput.class);

	protected PrintWriter out;
	protected int rowCount;
	private int resultSetColumnCount;
	protected int totalColumnCount; //resultset column count + drilldown column count - hidden column count
	protected DecimalFormat actualNumberFormatter;
	protected DecimalFormat sortNumberFormatter;
	protected DecimalFormat plainNumberFormatter;
	protected String contextPath;
	protected Locale locale;
	protected boolean evenRow;
	private List<Drilldown> drilldowns;
	protected String reportName;
	private List<ReportParameter> reportParamsList;
	protected String fullOutputFileName;
	private boolean showSelectedParameters;
	private Map<Integer, Double> columnTotals;
	private SimpleDateFormat globalDateFormatter;
	private SimpleDateFormat globalTimeFormatter;
	private Map<Integer, Object> columnFormatters;
	private DecimalFormat globalNumericFormatter;
	protected MessageSource messageSource;
	private String requestBaseUrl;
	protected boolean isJob;
	protected Report report;
	protected boolean pdfPageNumbers = true;
	protected boolean ajax;
	protected String dynamicOpenPassword;
	protected String dynamicModifyPassword;
	protected boolean pageHeaderLoaded;
	private Map<String, String[]> reportRequestParameters;
	protected ReportOptions reportOptions;

	/**
	 * @return the reportOptions
	 */
	public ReportOptions getReportOptions() {
		return reportOptions;
	}

	/**
	 * @param reportOptions the reportOptions to set
	 */
	public void setReportOptions(ReportOptions reportOptions) {
		this.reportOptions = reportOptions;
	}

	/**
	 * @return the reportRequestParameters
	 */
	public Map<String, String[]> getReportRequestParameters() {
		return reportRequestParameters;
	}

	/**
	 * @param reportRequestParameters the reportRequestParameters to set
	 */
	public void setReportRequestParameters(Map<String, String[]> reportRequestParameters) {
		this.reportRequestParameters = reportRequestParameters;
	}

	/**
	 * @return the pageHeaderLoaded
	 */
	public boolean isPageHeaderLoaded() {
		return pageHeaderLoaded;
	}

	/**
	 * @param pageHeaderLoaded the pageHeaderLoaded to set
	 */
	public void setPageHeaderLoaded(boolean pageHeaderLoaded) {
		this.pageHeaderLoaded = pageHeaderLoaded;
	}

	/**
	 * @return the dynamicOpenPassword
	 */
	public String getDynamicOpenPassword() {
		return dynamicOpenPassword;
	}

	/**
	 * @param dynamicOpenPassword the dynamicOpenPassword to set
	 */
	public void setDynamicOpenPassword(String dynamicOpenPassword) {
		this.dynamicOpenPassword = dynamicOpenPassword;
	}

	/**
	 * @return the dynamicModifyPassword
	 */
	public String getDynamicModifyPassword() {
		return dynamicModifyPassword;
	}

	/**
	 * @param dynamicModifyPassword the dynamicModifyPassword to set
	 */
	public void setDynamicModifyPassword(String dynamicModifyPassword) {
		this.dynamicModifyPassword = dynamicModifyPassword;
	}

	/**
	 * @return the totalColumnCount
	 */
	public int getTotalColumnCount() {
		return totalColumnCount;
	}

	/**
	 * @param totalColumnCount the totalColumnCount to set
	 */
	public void setTotalColumnCount(int totalColumnCount) {
		this.totalColumnCount = totalColumnCount;
	}

	/**
	 * @return the ajax
	 */
	public boolean isAjax() {
		return ajax;
	}

	/**
	 * @param ajax the ajax to set
	 */
	public void setAjax(boolean ajax) {
		this.ajax = ajax;
	}

	/**
	 * @return the pdfPageNumbers
	 */
	public boolean isPdfPageNumbers() {
		return pdfPageNumbers;
	}

	/**
	 * @param pdfPageNumbers the pdfPageNumbers to set
	 */
	public void setPdfPageNumbers(boolean pdfPageNumbers) {
		this.pdfPageNumbers = pdfPageNumbers;
	}

	/**
	 * @return the report
	 */
	public Report getReport() {
		return report;
	}

	/**
	 * @param report the report to set
	 */
	public void setReport(Report report) {
		this.report = report;
	}

	/**
	 * @param isJob the isJob to set
	 */
	public void setIsJob(boolean isJob) {
		this.isJob = isJob;
	}

	/**
	 * @return the requestBaseUrl
	 */
	public String getRequestBaseUrl() {
		return requestBaseUrl;
	}

	/**
	 * @param requestBaseUrl the requestBaseUrl to set
	 */
	public void setRequestBaseUrl(String requestBaseUrl) {
		this.requestBaseUrl = requestBaseUrl;
	}

	/**
	 * @return the messageSource
	 */
	public MessageSource getMessageSource() {
		return messageSource;
	}

	/**
	 * @param messageSource the messageSource to set
	 */
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

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
	 * @return <code>true</code> if page header and footer should be output also
	 */
	public boolean outputHeaderAndFooter() {
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
	protected void addSelectedParameters(List<ReportParameter> reportParamsList) {

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
	protected void addHeaderCellAlignLeft(String value) {
		addHeaderCell(value);
	}

	/**
	 * Outputs a value to the header whose text is left aligned
	 *
	 * @param value the value to output
	 * @param sortValue the sort value to use
	 */
	protected void addHeaderCellAlignLeft(String value, String sortValue) {
		addHeaderCellAlignLeft(value);
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
	 * For html output, the implementing class should perform escaping on the
	 * value given
	 *
	 * @param value
	 */
	protected void addCellStringUnsafe(String value) {
		addCellString(value);
	}

	/**
	 * Outputs numeric value to the current row
	 *
	 * @param value the value to output
	 */
	public abstract void addCellNumeric(Double value);

	/**
	 * Outputs numeric value to the current row
	 *
	 * @param numericValue the numeric value
	 * @param formattedValue the formatted string for the numeric value
	 * @param sortValue the sort value to use
	 */
	protected void addCellNumeric(Double numericValue, String formattedValue, String sortValue) {
		addCellNumeric(numericValue);
	}

	/**
	 * Outputs a Date value to the current row
	 *
	 * @param value the value to output
	 */
	public abstract void addCellDate(Date value);

	/**
	 * Outputs a date value to the current row
	 *
	 * @param dateValue the date value
	 * @param formattedValue the formatted string for the date value
	 * @param sortValue the sort value to use
	 */
	protected void addCellDate(Date dateValue, String formattedValue, long sortValue) {
		addCellDate(dateValue);
	}
	
	/**
	 * Outputs a time value to the current row
	 *
	 * @param value the value to output
	 */
	public abstract void addCellTime(Date value);
	
	/**
	 * Outputs a time value to the current row
	 *
	 * @param timeValue the time value
	 * @param formattedValue the formatted string for the time value
	 * @param sortValue the sort value to use
	 */
	protected void addCellTime(Date timeValue, String formattedValue, long sortValue) {
		addCellTime(timeValue);
	}

	/**
	 * Outputs an image to the current row
	 *
	 * @param binaryData the binary data for the image
	 */
	protected void addCellImage(byte[] binaryData) {
		addCellString("");
	}

	/**
	 * Closes the current row and opens a new one.
	 */
	public abstract void newRow();

	/**
	 * Closes the current row
	 */
	public void endRow() {

	}

	/**
	 * Finalizes data output
	 */
	public void endRows() {

	}

	/**
	 * Begins output for the total row
	 */
	protected void beginTotalRow() {
		newRow();
	}

	/**
	 * Outputs a total value
	 *
	 * @param value the value to output
	 */
	protected void addCellTotal(Double value) {
		addCellNumeric(value);
	}

	/**
	 * Outputs a total value
	 *
	 * @param totalValue the total value
	 * @param formattedValue the formatted string value
	 * @param sortValue the sort value
	 */
	protected void addCellTotal(Double totalValue, String formattedValue, String sortValue) {
		addCellNumeric(totalValue, formattedValue, sortValue);
	}

	/**
	 * Finalizes the total row
	 */
	protected void endTotalRow() {
		endRow();
	}

	/**
	 * Closes report output. Any final cleanup should be done here.
	 */
	public abstract void endOutput();

	/**
	 * Formats a numberic value for display
	 *
	 * @param value the value to format
	 * @return the string representation to display
	 */
	protected String formatNumericValue(Double value) {
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
	protected String formatDateValue(Date value) {
		String formattedValue;

		if (value == null) {
			formattedValue = "";
		} else {
			formattedValue = Config.getDateDisplayString(value);
		}

		return formattedValue;
	}
	
	/**
	 * Formats a time value for display
	 *
	 * @param value the value to format
	 * @return the string representation to display
	 */
	protected String formatTimeValue(Date value) {
		String formattedValue;

		if (value == null) {
			formattedValue = "";
		} else {
			formattedValue = Config.getTimeDisplayString(value);
		}

		return formattedValue;
	}

	/**
	 * Returns a value to use to sort date columns
	 *
	 * @param value the actual date
	 * @return the sort value for the date
	 */
	protected long getDateSortValue(Date value) {
		long sortValue;

		if (value == null) {
			sortValue = 0;
		} else {
			sortValue = value.getTime();
		}

		return sortValue;
	}

	/**
	 * Returns a value to use to sort numeric columns
	 *
	 * @param value the actual number
	 * @return the sort value for the number
	 */
	protected String getNumericSortValue(Double value) {
		String sortValue;

		if (value == null) {
			sortValue = null;
		} else {
			sortValue = sortNumberFormatter.format(value);
		}

		return sortValue;
	}

	/**
	 * Returns the value to be used for tabular heatmaps
	 *
	 * @param value the original value
	 * @return the value to be used for tabular heatmaps
	 */
	protected double getHeatmapValue(Double value) {
		double heatmapValue;

		if (value == null) {
			heatmapValue = Double.MIN_VALUE;
		} else {
			heatmapValue = value;
		}

		return heatmapValue;
	}

	/**
	 * Generates a tabular report
	 *
	 * @param rs the resultset to use, not null
	 * @param reportFormat the report format to use, not null
	 * @param report the report that is being run, not null
	 * @return StandardOutputResult. if successful, rowCount contains the number
	 * of rows in the resultset. if not, message contains the i18n message
	 * indicating the problem
	 * @throws SQLException
	 * @throws java.io.IOException
	 */
	public StandardOutputResult generateTabularOutput(ResultSet rs, ReportFormat reportFormat,
			Report report) throws SQLException, IOException {

		logger.debug("Entering generateTabularOutput");

		Objects.requireNonNull(rs, "rs must not be null");
		Objects.requireNonNull(reportFormat, "reportFormat must not be null");
		Objects.requireNonNull(report, "report must not be null");

		this.report = report;

		StandardOutputResult result = new StandardOutputResult();

		initializeNumberFormatters();

		ResultSetMetaData rsmd = rs.getMetaData();
		resultSetColumnCount = rsmd.getColumnCount();

		int drilldownCount = 0;
		if (drilldowns != null) {
			drilldownCount = drilldowns.size();
		}

		setTotalColumnCount(resultSetColumnCount + drilldownCount);

		List<String> hiddenColumns = getHiddenColumnsList(report);

		int hiddenColumnCount = 0;
		if (hiddenColumns != null) {
			hiddenColumnCount = hiddenColumns.size();
		}

		setTotalColumnCount(totalColumnCount - hiddenColumnCount);

		//perform any required output initialization
		init();

		addTitle();

		if (showSelectedParameters) {
			addSelectedParameters(reportParamsList);
		}

		//begin header output
		beginHeader();

		List<String> localizedColumnNames = getLocalizedColumnNames(rsmd, report);

		//output header columns for the result set columns
		for (int i = 1; i <= resultSetColumnCount; i++) {
			if (shouldOutputColumn(i, hiddenColumns, rsmd)) {
				addHeaderCell(localizedColumnNames.get(i));
			}
		}

		//output header columns for drill down reports
		//only output drilldown columns for html reports
		if (reportFormat.isHtml() && CollectionUtils.isNotEmpty(drilldowns)) {
			for (Drilldown drilldown : drilldowns) {
				String drilldownTitle = drilldown.getHeaderText();
				if (drilldownTitle == null || drilldownTitle.trim().length() == 0) {
					drilldownTitle = drilldown.getDrilldownReport().getLocalizedName(locale);
				}
				addHeaderCell(drilldownTitle);
			}
		}

		//end header output
		endHeader();

		//begin data output
		beginRows();

		int maxRows = Config.getMaxRows(reportFormat.getValue());
		Map<Integer, ColumnTypeDefinition> columnTypes = getColumnTypes(rsmd);

		initializeColumnFormatters(report, rsmd, columnTypes);

		String nullNumberDisplay = report.getNullNumberDisplay();
		if (nullNumberDisplay == null) {
			nullNumberDisplay = "";
		}

		String nullStringDisplay = report.getNullStringDisplay();
		if (nullStringDisplay == null) {
			nullStringDisplay = "";
		}

		List<String> totalColumns = getTotalColumnsList(report);
		if (!totalColumns.isEmpty()) {
			columnTotals = new HashMap<>();
		}

		rowCount = 0;
		while (rs.next()) {
			rowCount++;

			newRow();

			if (rowCount % 2 == 0) {
				evenRow = true;
			} else {
				evenRow = false;
			}

			if (rowCount > maxRows) {
				logger.debug("rowCount={}, maxRows={}, reportFormat={}", rowCount, maxRows, reportFormat);
				//row limit exceeded
				for (int i = 0; i < totalColumnCount; i++) {
					addCellString("...");
				}

				endRows();

				endOutput();

				result.setMessage("runReport.message.tooManyRows");
				result.setTooManyRows(true);
				return result;
			} else {
				List<Object> columnValues = outputResultSetColumns(columnTypes, rs, hiddenColumns, nullNumberDisplay, nullStringDisplay, reportFormat);
				outputDrilldownColumns(drilldowns, columnValues);
			}
		}

		finalizeOutput(hiddenColumns, rsmd, drilldownCount, totalColumns);

		result.setSuccess(true);
		result.setRowCount(rowCount);

		return result;
	}

	/**
	 * Generates a tabular report
	 *
	 * @param data the data to use, not null.
	 * @param reportFormat the report format to use, not null
	 * @param report the report that is being run, not null
	 * @return StandardOutputResult. if successful, rowCount contains the number
	 * of rows in the resultset. if not, message contains the i18n message
	 * indicating the problem
	 * @throws java.lang.Exception
	 */
	public StandardOutputResult generateTabularOutput(Object data,
			ReportFormat reportFormat, Report report) throws Exception {

		logger.debug("Entering generateTabularOutput");

		Objects.requireNonNull(data, "data must not be null");
		Objects.requireNonNull(reportFormat, "reportFormat must not be null");
		Objects.requireNonNull(report, "report must not be null");

		this.report = report;

		StandardOutputResult result = new StandardOutputResult();

		initializeNumberFormatters();

		GroovyDataDetails dataDetails = RunReportHelper.getGroovyDataDetails(data, report);
		resultSetColumnCount = dataDetails.getColCount();
		List<String> columnNames = dataDetails.getColumnNames();
		List<? extends Object> dataList = dataDetails.getDataList();
		Map<Integer, ColumnTypeDefinition> columnTypes = dataDetails.getColumnTypes();
		List<String> columnLabels = dataDetails.getColumnLabels();

		int drilldownCount = 0;
		if (drilldowns != null) {
			drilldownCount = drilldowns.size();
		}

		setTotalColumnCount(resultSetColumnCount + drilldownCount);

		List<String> hiddenColumns = getHiddenColumnsList(report);

		int hiddenColumnCount = 0;
		if (hiddenColumns != null) {
			hiddenColumnCount = hiddenColumns.size();
		}

		setTotalColumnCount(totalColumnCount - hiddenColumnCount);

		//perform any required output initialization
		init();

		addTitle();

		if (showSelectedParameters) {
			addSelectedParameters(reportParamsList);
		}

		//begin header output
		beginHeader();

		List<String> localizedColumnNames = getLocalizedColumnNames(columnLabels, report);

		//output header columns for the result set columns
		for (int i = 1; i <= resultSetColumnCount; i++) {
			String columnName = columnNames.get(i - 1);
			if (shouldOutputColumn(i, hiddenColumns, columnName)) {
				addHeaderCell(localizedColumnNames.get(i));
			}
		}

		//output header columns for drill down reports
		//only output drilldown columns for html reports
		if (reportFormat.isHtml() && CollectionUtils.isNotEmpty(drilldowns)) {
			for (Drilldown drilldown : drilldowns) {
				String drilldownTitle = drilldown.getHeaderText();
				if (drilldownTitle == null || drilldownTitle.trim().length() == 0) {
					drilldownTitle = drilldown.getDrilldownReport().getLocalizedName(locale);
				}
				addHeaderCell(drilldownTitle);
			}
		}

		//end header output
		endHeader();

		//begin data output
		beginRows();

		int maxRows = Config.getMaxRows(reportFormat.getValue());

		initializeColumnFormatters(report, columnNames, columnTypes);

		String nullNumberDisplay = report.getNullNumberDisplay();
		if (nullNumberDisplay == null) {
			nullNumberDisplay = "";
		}

		String nullStringDisplay = report.getNullStringDisplay();
		if (nullStringDisplay == null) {
			nullStringDisplay = "";
		}

		List<String> totalColumns = getTotalColumnsList(report);
		if (!totalColumns.isEmpty()) {
			columnTotals = new HashMap<>();
		}

		rowCount = 0;
		for (Object row : dataList) {
			rowCount++;

			newRow();

			if (rowCount % 2 == 0) {
				evenRow = true;
			} else {
				evenRow = false;
			}

			if (rowCount > maxRows) {
				logger.debug("rowCount={}, maxRows={}, reportFormat={}", rowCount, maxRows, reportFormat);
				//row limit exceeded
				for (int i = 0; i < totalColumnCount; i++) {
					addCellString("...");
				}

				endRows();

				endOutput();

				result.setMessage("runReport.message.tooManyRows");
				result.setTooManyRows(true);
				return result;
			} else {
				List<Object> columnValues = outputDataColumns(columnTypes, row, columnNames, hiddenColumns, nullNumberDisplay, nullStringDisplay, reportFormat);
				outputDrilldownColumns(drilldowns, columnValues);
			}
		}

		finalizeOutput(hiddenColumns, columnNames, drilldownCount, totalColumns);

		result.setSuccess(true);
		result.setRowCount(rowCount);

		return result;
	}

	/**
	 * Initialize date and number formatters to be used for formatting column
	 * values
	 *
	 * @param report the report being run
	 * @param rsmd the resultset metadata object
	 * @param columnTypes the column types of the resultset
	 * @throws RuntimeException
	 * @throws SQLException
	 */
	private void initializeColumnFormatters(Report report, ResultSetMetaData rsmd,
			Map<Integer, ColumnTypeDefinition> columnTypes)
			throws RuntimeException, SQLException {

		List<String> columnNames = getColumnNames(rsmd);
		initializeColumnFormatters(report, columnNames, columnTypes);
	}

	/**
	 * Initialize date and number formatters to be used for formatting column
	 * values
	 *
	 * @param report the report being run
	 * @param columnNames the column names
	 * @param columnTypes the column types
	 * @throws RuntimeException
	 */
	private void initializeColumnFormatters(Report report, List<String> columnNames,
			Map<Integer, ColumnTypeDefinition> columnTypes)
			throws RuntimeException {

		Locale columnFormatLocale;
		String reportLocale = report.getLocale();
		if (StringUtils.isBlank(reportLocale)) {
			columnFormatLocale = locale;
		} else {
			columnFormatLocale = ArtUtils.getLocaleFromString(reportLocale);
		}

		String globalDateFormat = report.getDateFormat();
		if (StringUtils.isNotBlank(globalDateFormat)) {
			globalDateFormatter = new SimpleDateFormat(globalDateFormat, columnFormatLocale);
		}
		
		String globalTimeFormat = report.getTimeFormat();
		if (StringUtils.isNotBlank(globalTimeFormat)) {
			globalTimeFormatter = new SimpleDateFormat(globalTimeFormat, columnFormatLocale);
		}

		String globalNumberFormat = report.getNumberFormat();
		if (StringUtils.isNotBlank(globalNumberFormat)) {
			globalNumericFormatter = (DecimalFormat) NumberFormat.getInstance(columnFormatLocale);
			globalNumericFormatter.applyPattern(globalNumberFormat);
		}

		String columnFormatsSetting = report.getColumnFormats();
		if (StringUtils.isNotBlank(columnFormatsSetting)) {
			columnFormatters = new HashMap<>();
			String columnFormatsArray[] = columnFormatsSetting.split("\\r?\\n");
			List<String> columnFormatIds = new ArrayList<>();
			Map<String, String> columnFormatDetails = new HashMap<>();
			for (String columnFormat : columnFormatsArray) {
				String id = StringUtils.substringBefore(columnFormat, ":");
				id = StringUtils.strip(id);
				String format = StringUtils.substringAfter(columnFormat, ":");
				format = StringUtils.strip(format);
				columnFormatIds.add(id);
				columnFormatDetails.put(id, format);
			}

			List<String> columns = getOneBasedColumnNames(columnNames);

			for (int i = 1; i <= resultSetColumnCount; i++) {
				String columnName = columns.get(i);
				if (columnFormatIds.contains(String.valueOf(i))
						|| (StringUtils.isNotBlank(columnName) && ArtUtils.containsIgnoreCase(columnFormatIds, columnName))) {
					String format = columnFormatDetails.get(String.valueOf(i));
					if (format == null) {
						format = columnFormatDetails.get(columnName);
					}
					ColumnTypeDefinition columnTypeDefinition = columnTypes.get(i);
					ColumnType columnType = columnTypeDefinition.getColumnType();
					switch (columnType) {
						case Date:
							SimpleDateFormat dateFormatter = new SimpleDateFormat(format, columnFormatLocale);
							columnFormatters.put(i, dateFormatter);
							break;
						case Time:
							SimpleDateFormat timeFormatter = new SimpleDateFormat(format, columnFormatLocale);
							columnFormatters.put(i, timeFormatter);
							break;
						case Numeric:
							DecimalFormat numberFormatter = (DecimalFormat) NumberFormat.getInstance(columnFormatLocale);
							numberFormatter.applyPattern(format);
							columnFormatters.put(i, numberFormatter);
							break;
						default:
							throw new RuntimeException("Formatting not supported for column: " + i + " or " + columnName);
					}
				}
			}
		}
	}

	/**
	 * Finalizes tabular output
	 *
	 * @param hiddenColumns the list of columns to be hidden
	 * @param rsmd the resultset metadata object
	 * @param drilldownCount the number of drilldowns in use
	 * @param totalColumns the list of columns to be totalled
	 * @throws SQLException
	 */
	private void finalizeOutput(List<String> hiddenColumns, ResultSetMetaData rsmd,
			int drilldownCount, List<String> totalColumns) throws SQLException {

		List<String> columnNames = getColumnNames(rsmd);
		finalizeOutput(hiddenColumns, columnNames, drilldownCount, totalColumns);
	}

	/**
	 * Finalizes tabular output
	 *
	 * @param hiddenColumns the list of columns to be hidden
	 * @param columnNames the column names
	 * @param drilldownCount the number of drilldowns in use
	 * @param totalColumns the list of columns to be totalled
	 */
	private void finalizeOutput(List<String> hiddenColumns, List<String> columnNames,
			int drilldownCount, List<String> totalColumns) {

		if (rowCount > 0) {
			endRow();
		}

		endRows();

		//output total row
		if (columnTotals != null) {
			outputTotals(hiddenColumns, columnNames, drilldownCount, totalColumns);
		}

		//end data output
		endOutput();
	}

	/**
	 * Output totals
	 *
	 * @param hiddenColumns the list of columns to be hidden
	 * @param columnNames the column names
	 * @param drilldownCount the number of drilldowns being displayed
	 * @param totalColumns the list of columns to be totalled
	 */
	private void outputTotals(List<String> hiddenColumns, List<String> columnNames,
			int drilldownCount, List<String> totalColumns) {

		beginTotalRow();

		List<String> columns = getOneBasedColumnNames(columnNames);

		for (int i = 1; i <= resultSetColumnCount; i++) {
			String columnName = columns.get(i);
			if (shouldOutputColumn(i, hiddenColumns, columnName)) {
				Double columnTotal = columnTotals.get(i);
				if (columnTotal == null) {
					addCellString("");
				} else {
					if (shouldTotalColumn(i, totalColumns, columnName)) {
						String sortValue = getNumericSortValue(columnTotal);
						String columnFormattedValue = null;

						if (columnFormatters != null) {
							DecimalFormat columnFormatter = (DecimalFormat) columnFormatters.get(i);
							if (columnFormatter != null) {
								columnFormattedValue = columnFormatter.format(columnTotal);
							}
						}

						if (columnFormattedValue != null) {
							addCellTotal(columnTotal, columnFormattedValue, sortValue);
						} else {
							String formattedValue;
							if (globalNumericFormatter != null) {
								formattedValue = globalNumericFormatter.format(columnTotal);
							} else {
								formattedValue = formatNumericValue(columnTotal);
							}

							addCellTotal(columnTotal, formattedValue, sortValue);
						}
					} else {
						addCellString("");
					}
				}
			}
		}

		//output total columns for drilldowns
		for (int i = 0; i < drilldownCount; i++) {
			addCellString("");
		}

		endTotalRow();
	}

	/**
	 * Returns a list of column indexes or column names that should not be
	 * included in the output
	 *
	 * @param report the report that is being run
	 * @return a list of column indexes or column names that should not be
	 * included in the output
	 */
	private List<String> getHiddenColumnsList(Report report) {
		String hiddenColumnsSetting = report.getHiddenColumns();
		String[] hiddenColumnsArray = StringUtils.split(hiddenColumnsSetting, ",");

		List<String> hiddenColumns;
		if (hiddenColumnsArray == null) {
			hiddenColumns = Collections.emptyList();
		} else {
			hiddenColumnsArray = StringUtils.stripAll(hiddenColumnsArray, " ");
			hiddenColumns = Arrays.asList(hiddenColumnsArray);
		}

		return hiddenColumns;
	}

	/**
	 * Returns a list of column indexes or column names that should be totalled
	 * in the output
	 *
	 * @param report the report that is being run
	 * @return a list of column indexes or column names that should be totalled
	 * in the output
	 */
	private List<String> getTotalColumnsList(Report report) {
		String totalColumnsSetting = report.getTotalColumns();
		String[] totalColumnsArray = StringUtils.split(totalColumnsSetting, ",");

		List<String> totalColumns;
		if (totalColumnsArray == null) {
			totalColumns = Collections.emptyList();
		} else {
			totalColumnsArray = StringUtils.stripAll(totalColumnsArray, " ");
			totalColumns = Arrays.asList(totalColumnsArray);
		}

		return totalColumns;
	}

	/**
	 * Generates burst output
	 *
	 * @param rs the resultset to use
	 * @param reportFormat the report format to use
	 * @param job the job that is generating the burst output
	 * @param reportParamsMap report parameters
	 * @param username the username of the user who is running the job
	 * @throws SQLException
	 * @throws java.io.IOException
	 * @throws java.text.ParseException
	 */
	public void generateBurstOutput(ResultSet rs, ReportFormat reportFormat,
			Job job, Map<String, ReportParameter> reportParamsMap,
			String username) throws SQLException, IOException, ParseException {

		logger.debug("Entering generateBurstOutput: reportFormat={}, job={},"
				+ " username='{}'", reportFormat, job, username);

		this.report = job.getReport();

		initializeNumberFormatters();

		ResultSetMetaData rsmd = rs.getMetaData();
		resultSetColumnCount = rsmd.getColumnCount();

		setTotalColumnCount(resultSetColumnCount);

		List<String> hiddenColumns = getHiddenColumnsList(report);

		int hiddenColumnCount = 0;
		if (hiddenColumns != null) {
			hiddenColumnCount = hiddenColumns.size();
		}

		setTotalColumnCount(totalColumnCount - hiddenColumnCount);

		int maxRows = Config.getMaxRows(reportFormat.getValue());
		Map<Integer, ColumnTypeDefinition> columnTypes = getColumnTypes(rsmd);

		initializeColumnFormatters(report, rsmd, columnTypes);

		String nullNumberDisplay = report.getNullNumberDisplay();
		if (nullNumberDisplay == null) {
			nullNumberDisplay = "";
		}

		String nullStringDisplay = report.getNullStringDisplay();
		if (nullStringDisplay == null) {
			nullStringDisplay = "";
		}

		List<String> totalColumns = getTotalColumnsList(report);

		String previousBurstId = null;
		FileOutputStream fos = null;

		try {
			rowCount = 0;
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
						fos = endBurstOutput(fos, hiddenColumns, rsmd, totalColumns);
					}

					if (!totalColumns.isEmpty()) {
						columnTotals = null;
						columnTotals = new HashMap<>();
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
					String exportPath = Config.getJobsExportPath();
					String fileName;

					FilenameHelper filenameHelper = new FilenameHelper();

					String fixedFileName = job.getFixedFileName();
					if (StringUtils.isNotBlank(fixedFileName)) {
						ExpressionHelper expressionHelper = new ExpressionHelper();
						fixedFileName = expressionHelper.processString(fixedFileName, reportParamsMap, username);

						fixedFileName = fixedFileName + "-BurstId-" + fileNameBurstId;
						fixedFileName = ArtUtils.cleanBaseFilename(fixedFileName);

						String extension = filenameHelper.getFilenameExtension(report, reportFormat);

						fileName = fixedFileName + "." + extension;

						if (!FinalFilenameValidator.isValidTwoDot(fileName)) {
							throw new IllegalArgumentException("Invalid burst file name - " + fileName);
						}

						String fullFixedFileName = exportPath + fileName;
						File fixedFile = new File(fullFixedFileName);
						if (fixedFile.exists()) {
							boolean fileDeleted = fixedFile.delete();
							if (!fileDeleted) {
								logger.warn("Could not delete fixed file: '{}'", fullFixedFileName);
							}
						}
					} else {
						fileName = filenameHelper.getFilename(job, fileNameBurstId, locale, reportFormat, reportParamsMap);

						if (!FinalFilenameValidator.isValidTwoDot(fileName)) {
							throw new IllegalArgumentException("Invalid file name - " + fileName);
						}
					}

					fullOutputFileName = exportPath + fileName;

					//create html file to output to as required
					if (reportFormat.isHtml() || reportFormat == ReportFormat.xml
							|| reportFormat == ReportFormat.rss20) {
						fos = new FileOutputStream(fullOutputFileName);
						out = new PrintWriter(new OutputStreamWriter(fos, "UTF-8")); // make sure we make a utf-8 encoded text
					}

					reportName = report.getLocalizedName(locale) + " - " + currentBurstId;

					initializeBurstOutput(rsmd, hiddenColumns, report);
				}

				newRow();

				if (rowCount % 2 == 0) {
					evenRow = true;
				} else {
					evenRow = false;
				}

				if (rowCount > maxRows) {
					logger.debug("rowCount={}, maxRows={}, reportFormat={}", rowCount, maxRows, reportFormat);
					//row limit exceeded
					for (int i = 0; i < totalColumnCount; i++) {
						addCellString("...");
					}

					fos = endBurstOutput(fos, hiddenColumns, rsmd, totalColumns);
					previousBurstId = null;
				} else {
					outputResultSetColumns(columnTypes, rs, hiddenColumns, nullNumberDisplay, nullStringDisplay, reportFormat);
				}
			}

			fos = endBurstOutput(fos, hiddenColumns, rsmd, totalColumns);
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
	 * @param hiddenColumns the list of columns to be hidden
	 * @param rsmd the resultset metadata object
	 * @param totalColumns the list of columns to be totalled
	 * @return the file output stream used
	 * @throws IOException
	 */
	private FileOutputStream endBurstOutput(FileOutputStream fos,
			List<String> hiddenColumns, ResultSetMetaData rsmd,
			List<String> totalColumns) throws IOException, SQLException {

		int drilldownCount = 0;
		finalizeOutput(hiddenColumns, rsmd, drilldownCount, totalColumns);

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
	public void initializeNumberFormatters() {
		//initialize number formatters
		final int OPTIONAL_DECIMAL_COUNT = 10;
		String optionalDecimals = StringUtils.repeat("#", OPTIONAL_DECIMAL_COUNT);
		String pattern;
		actualNumberFormatter = (DecimalFormat) NumberFormat.getInstance(locale);
		pattern = "#,##0." + optionalDecimals;
		actualNumberFormatter.applyPattern(pattern);

		plainNumberFormatter = (DecimalFormat) NumberFormat.getInstance(locale);
		pattern = "#." + optionalDecimals;
		plainNumberFormatter.applyPattern(pattern);

		//specifically use english locale for sorting e.g.
		//in case user locale uses dot as thousands separator e.g. italian, german
		sortNumberFormatter = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
		pattern = "#." + optionalDecimals;
		sortNumberFormatter.applyPattern(pattern);
		//set minimum digits to ensure all numbers are pre-padded with zeros,
		//so that sorting works correctly
		sortNumberFormatter.setMinimumIntegerDigits(20);
	}

	/**
	 * Starts output for burst output generation
	 *
	 * @param rsmd the resultset metadata object
	 * @param hiddenColumns column ids or column names of resultset columns that
	 * should not be included in the output
	 * @throws SQLException
	 */
	private void initializeBurstOutput(ResultSetMetaData rsmd, List<String> hiddenColumns,
			Report report) throws SQLException {

		//perform any required output initialization
		init();

		addTitle();

		if (showSelectedParameters) {
			addSelectedParameters(reportParamsList);
		}

		//begin header output
		beginHeader();

		List<String> localizedColumnNames = getLocalizedColumnNames(rsmd, report);

		//output header columns for the result set columns
		for (int i = 1; i <= resultSetColumnCount; i++) {
			if (shouldOutputColumn(i, hiddenColumns, rsmd)) {
				addHeaderCell(localizedColumnNames.get(i));
			}
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
	 * [column index, column type definition]
	 * @throws SQLException
	 */
	private Map<Integer, ColumnTypeDefinition> getColumnTypes(ResultSetMetaData rsmd) throws SQLException {
		Map<Integer, ColumnTypeDefinition> columnTypes = new LinkedHashMap<>();

		int colCount = rsmd.getColumnCount();
		for (int i = 1; i <= colCount; i++) {
			ColumnTypeDefinition columnTypeDefinition = getColumnTypeDefinition(rsmd, i);
			columnTypes.put(i, columnTypeDefinition);
		}

		return columnTypes;
	}

	/**
	 * Returns the column type definition for a given column
	 *
	 * @param rsmd the resultset metadata
	 * @param columnIndex the column index
	 * @return the column type definition
	 * @throws SQLException
	 */
	private ColumnTypeDefinition getColumnTypeDefinition(ResultSetMetaData rsmd,
			int columnIndex) throws SQLException {

		int sqlType = rsmd.getColumnType(columnIndex);

		ColumnTypeDefinition columnTypeDefinition = new ColumnTypeDefinition();
		columnTypeDefinition.setSqlType(sqlType);

		ColumnType columnType = getColumnType(sqlType);
		columnTypeDefinition.setColumnType(columnType);

		return columnTypeDefinition;
	}

	/**
	 * Returns the art column type given a particular sql type
	 *
	 * @param sqlType the sql type
	 * @return the art column type
	 */
	private ColumnType getColumnType(int sqlType) {
		ColumnType columnType;

		if (isNumeric(sqlType)) {
			columnType = ColumnType.Numeric;
		} else if (isDate(sqlType)) {
			columnType = ColumnType.Date;
		} else if (isClob(sqlType)) {
			columnType = ColumnType.Clob;
		} else if (sqlType == Types.OTHER) {
			columnType = ColumnType.Other;
		} else if(isTime(sqlType)){
			columnType = ColumnType.Time;
		} else if (isBinary(sqlType)) {
			columnType = ColumnType.Binary;
		} else {
			columnType = ColumnType.String;
		}

		return columnType;
	}

	/**
	 * Returns <code>true</code> if the resultset column with the given index
	 * should be included in the output
	 *
	 * @param columnIndex the column's index
	 * @param hiddenColumns the list of columns to be hidden
	 * @param rsmd the resultset metadata object
	 * @return <code>true</code> if the resultset column with the given index
	 * should be included in the output
	 * @throws SQLException
	 */
	private boolean shouldOutputColumn(int columnIndex, List<String> hiddenColumns,
			ResultSetMetaData rsmd) throws SQLException {

		String columnName = rsmd.getColumnLabel(columnIndex);
		return shouldOutputColumn(columnIndex, hiddenColumns, columnName);
	}

	/**
	 * Returns <code>true</code> if the resultset column with the given index
	 * should be included in the output
	 *
	 * @param columnIndex the column's index
	 * @param hiddenColumns the list of columns to be hidden
	 * @param columnName the column name
	 * @return <code>true</code> if the resultset column with the given index
	 * should be included in the output
	 */
	private boolean shouldOutputColumn(int columnIndex, List<String> hiddenColumns,
			String columnName) {

		if (hiddenColumns == null || hiddenColumns.isEmpty()) {
			return true;
		}

		boolean displayColumn;

		if (hiddenColumns.contains(String.valueOf(columnIndex))
				|| ArtUtils.containsIgnoreCase(hiddenColumns, columnName)) {
			displayColumn = false;
		} else {
			displayColumn = true;
		}

		return displayColumn;
	}

	/**
	 * Returns <code>true</code> if the resultset column with the given index
	 * should be output as an image
	 *
	 * @param columnIndex the column's index
	 * @param imageColumns the list of image columns
	 * @param rsmd the resultset metadata object
	 * @return <code>true</code> if the resultset column with the given index
	 * should be output as an image
	 * @throws SQLException
	 */
	private boolean isImageColumn(int columnIndex, List<String> imageColumns,
			ResultSetMetaData rsmd) throws SQLException {

		if (CollectionUtils.isEmpty(imageColumns)) {
			return false;
		}

		boolean imageColumn;

		String columnName = rsmd.getColumnLabel(columnIndex);

		if (imageColumns.contains(String.valueOf(columnIndex))
				|| ArtUtils.containsIgnoreCase(imageColumns, columnName)) {
			imageColumn = true;
		} else {
			imageColumn = false;
		}

		return imageColumn;
	}

	/**
	 * Returns <code>true</code> if the resultset column with the given index
	 * should be totalled
	 *
	 * @param columnIndex the column's index
	 * @param totalColumns the list of columns to be totalled
	 * @param columnName the columnName
	 * @return <code>true</code> if the resultset column with the given index
	 * should be totalled
	 */
	private boolean shouldTotalColumn(int columnIndex, List<String> totalColumns,
			String columnName) {

		if (totalColumns == null || totalColumns.isEmpty()) {
			return false;
		}

		boolean totalColumn;

		if (totalColumns.contains(String.valueOf(columnIndex))
				|| ArtUtils.containsIgnoreCase(totalColumns, columnName)) {
			totalColumn = true;
		} else {
			totalColumn = false;
		}

		return totalColumn;
	}

	/**
	 * Outputs one row for the resultset data
	 *
	 * @param columnTypes the column types for the records
	 * @param rs the resultset with the data to output
	 * @param hiddenColumns column ids or column names of resultset columns that
	 * should not be included in the output
	 * @param nullNumberDisplay the string to display for null numeric values
	 * @param nullStringDisplay the string to display for null string values
	 * @param reportFormat the report format being used
	 * @return data for the output row
	 * @throws Exception
	 */
	private List<Object> outputDataColumns(Map<Integer, ColumnTypeDefinition> columnTypes,
			Object row, List<String> columnNames, List<String> hiddenColumns,
			String nullNumberDisplay, String nullStringDisplay,
			ReportFormat reportFormat) throws Exception {
		//save column values for use in drill down columns.
		//for the jdbc-odbc bridge, you can only read
		//column values ONCE and in the ORDER they appear in the select
		List<Object> columnValues = new ArrayList<>();

		List<String> columns = getOneBasedColumnNames(columnNames);

		for (Entry<Integer, ColumnTypeDefinition> entry : columnTypes.entrySet()) {
			int columnIndex = entry.getKey();
			String columnName = columns.get(columnIndex);
			if (!shouldOutputColumn(columnIndex, hiddenColumns, columnName)) {
				continue;
			}

			ColumnTypeDefinition columnTypeDefinition = entry.getValue();
			ColumnType columnType = columnTypeDefinition.getColumnType();

			Object value;
			if (row instanceof GroovyRowResult) {
				GroovyRowResult rowResult = (GroovyRowResult) row;
				value = rowResult.get(columnName);
			} else if (row instanceof DynaBean) {
				DynaBean rowBean = (DynaBean) row;
				value = rowBean.get(columnName);
			} else if (row instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, ? extends Object> rowMap = (Map<String, ? extends Object>) row;
				value = rowMap.get(columnName);
			} else if (row instanceof String) {
				value = row;
			} else {
				value = PropertyUtils.getProperty(row, columnName);
			}

			switch (columnType) {
				case Numeric:
					Double numericValue;
					if (value == null) {
						numericValue = null;
					} else {
						numericValue = ((Number) value).doubleValue();
					}

					if (reportFormat.isUseColumnFormatting()) {
						if (numericValue == null) {
							String sortValue = "null";
							addCellNumeric(numericValue, nullNumberDisplay, sortValue);
						} else {
							String sortValue = getNumericSortValue(numericValue);
							String columnFormattedValue = null;

							if (columnFormatters != null) {
								DecimalFormat columnFormatter = (DecimalFormat) columnFormatters.get(columnIndex);
								if (columnFormatter != null) {
									columnFormattedValue = columnFormatter.format(numericValue);
								}
							}

							if (columnFormattedValue != null) {
								addCellNumeric(numericValue, columnFormattedValue, sortValue);
							} else {
								String formattedValue;
								if (globalNumericFormatter != null) {
									formattedValue = globalNumericFormatter.format(numericValue);
								} else {
									formattedValue = formatNumericValue(numericValue);
								}

								addCellNumeric(numericValue, formattedValue, sortValue);
							}
						}
					} else {
						addCellNumeric(numericValue);
					}

					if (columnTotals != null) {
						Double currentValue;

						if (value == null) {
							currentValue = 0D;
						} else {
							currentValue = numericValue;
						}

						Double newTotal;
						Double currentTotal = columnTotals.get(columnIndex);
						if (currentTotal == null) {
							newTotal = currentValue;
						} else {
							newTotal = currentTotal + currentValue;
						}

						columnTotals.put(columnIndex, newTotal);
					}
					break;
				case Date:
					Date dateValue = (Date) value;

					if (reportFormat.isUseColumnFormatting()) {
						if (dateValue == null) {
							addCellDate(dateValue);
						} else {
							long sortValue = getDateSortValue(dateValue);
							String columnFormattedValue = null;

							if (columnFormatters != null) {
								SimpleDateFormat columnFormatter = (SimpleDateFormat) columnFormatters.get(columnIndex);
								if (columnFormatter != null) {
									columnFormattedValue = columnFormatter.format(dateValue);
								}
							}

							if (columnFormattedValue != null) {
								addCellDate(dateValue, columnFormattedValue, sortValue);
							} else {
								String formattedValue;
								if (globalDateFormatter != null) {
									formattedValue = globalDateFormatter.format(dateValue);
								} else {
									formattedValue = Config.getDateDisplayString(dateValue);
								}

								addCellDate(dateValue, formattedValue, sortValue);
							}
						}
					} else {
						addCellDate(dateValue);
					}
					break;
				case Time:
					Date timeValue = (Date) value;

					if (reportFormat.isUseColumnFormatting()) {
						if (timeValue == null) {
							addCellTime(timeValue);
						} else {
							long sortValue = getDateSortValue(timeValue);
							String columnFormattedValue = null;

							if (columnFormatters != null) {
								SimpleDateFormat columnFormatter = (SimpleDateFormat) columnFormatters.get(columnIndex);
								if (columnFormatter != null) {
									columnFormattedValue = columnFormatter.format(timeValue);
								}
							}

							if (columnFormattedValue != null) {
								addCellTime(timeValue, columnFormattedValue, sortValue);
							} else {
								String formattedValue;
								if (globalTimeFormatter != null) {
									formattedValue = globalTimeFormatter.format(timeValue);
								} else {
									formattedValue = Config.getTimeDisplayString(timeValue);
								}

								addCellTime(timeValue, formattedValue, sortValue);
							}
						}
					} else {
						addCellTime(timeValue);
					}
					break;
				default:
					addString(value, nullStringDisplay);
			}

			columnValues.add(value);
		}

		return columnValues;
	}

	/**
	 * Outputs one row for the resultset data
	 *
	 * @param columnTypes the column types for the records
	 * @param rs the resultset with the data to output
	 * @param hiddenColumns column ids or column names of resultset columns that
	 * should not be included in the output
	 * @param nullNumberDisplay the string to display for null numeric values
	 * @param nullStringDisplay the string to display for null string values
	 * @param reportFormat the report format being used
	 * @return data for the output row
	 * @throws SQLException
	 */
	private List<Object> outputResultSetColumns(Map<Integer, ColumnTypeDefinition> columnTypes,
			ResultSet rs, List<String> hiddenColumns, String nullNumberDisplay,
			String nullStringDisplay, ReportFormat reportFormat) throws SQLException, IOException {
		//save column values for use in drill down columns.
		//for the jdbc-odbc bridge, you can only read
		//column values ONCE and in the ORDER they appear in the select
		List<Object> columnValues = new ArrayList<>();

		StandardOutputOptions standardOutputOptions;
		String options = report.getOptions();
		if (StringUtils.isBlank(options)) {
			standardOutputOptions = new StandardOutputOptions();
		} else {
			standardOutputOptions = ArtUtils.jsonToObject(options, StandardOutputOptions.class);
		}

		List<String> imageColumns = standardOutputOptions.getImageColumns();

		ResultSetMetaData rsmd = rs.getMetaData();

		for (Entry<Integer, ColumnTypeDefinition> entry : columnTypes.entrySet()) {
			int columnIndex = entry.getKey();
			if (!shouldOutputColumn(columnIndex, hiddenColumns, rsmd)) {
				continue;
			}

			ColumnTypeDefinition columnTypeDefinition = entry.getValue();
			ColumnType columnType = columnTypeDefinition.getColumnType();

			Object value = null;

			switch (columnType) {
				case Numeric:
					value = getColumnValue(rs, columnIndex, columnTypeDefinition);
					Double numericValue;
					if (value == null) {
						numericValue = null;
					} else {
						numericValue = (Double) value;
					}

					if (reportFormat.isUseColumnFormatting()) {
						if (numericValue == null) {
							String sortValue = "null";
							addCellNumeric(numericValue, nullNumberDisplay, sortValue);
						} else {
							String sortValue = getNumericSortValue(numericValue);
							String columnFormattedValue = null;

							if (columnFormatters != null) {
								DecimalFormat columnFormatter = (DecimalFormat) columnFormatters.get(columnIndex);
								if (columnFormatter != null) {
									columnFormattedValue = columnFormatter.format(numericValue);
								}
							}

							if (columnFormattedValue != null) {
								addCellNumeric(numericValue, columnFormattedValue, sortValue);
							} else {
								String formattedValue;
								if (globalNumericFormatter != null) {
									formattedValue = globalNumericFormatter.format(numericValue);
								} else {
									formattedValue = formatNumericValue(numericValue);
								}

								addCellNumeric(numericValue, formattedValue, sortValue);
							}
						}
					} else {
						addCellNumeric(numericValue);
					}

					if (columnTotals != null) {
						Double currentValue;

						if (value == null) {
							currentValue = 0D;
						} else {
							currentValue = numericValue;
						}

						Double newTotal;
						Double currentTotal = columnTotals.get(columnIndex);
						if (currentTotal == null) {
							newTotal = currentValue;
						} else {
							newTotal = currentTotal + currentValue;
						}

						columnTotals.put(columnIndex, newTotal);
					}
					break;
				case Date:
					value = rs.getTimestamp(columnIndex);
					Date dateValue = (Date) value;

					if (reportFormat.isUseColumnFormatting()) {
						if (dateValue == null) {
							addCellDate(dateValue);
						} else {
							long sortValue = getDateSortValue(dateValue);
							String columnFormattedValue = null;

							if (columnFormatters != null) {
								SimpleDateFormat columnFormatter = (SimpleDateFormat) columnFormatters.get(columnIndex);
								if (columnFormatter != null) {
									columnFormattedValue = columnFormatter.format(dateValue);
								}
							}

							if (columnFormattedValue != null) {
								addCellDate(dateValue, columnFormattedValue, sortValue);
							} else {
								String formattedValue;
								if (globalDateFormatter != null) {
									formattedValue = globalDateFormatter.format(dateValue);
								} else {
									formattedValue = Config.getDateDisplayString(dateValue);
								}

								addCellDate(dateValue, formattedValue, sortValue);
							}
						}
					} else {
						addCellDate(dateValue);
					}
					break;
				case Time:
					value = rs.getTime(columnIndex);
					Date timeValue = (Date) value;

					if (reportFormat.isUseColumnFormatting()) {
						if (timeValue == null) {
							addCellTime(timeValue);
						} else {
							long sortValue = getDateSortValue(timeValue);
							String columnFormattedValue = null;

							if (columnFormatters != null) {
								SimpleDateFormat columnFormatter = (SimpleDateFormat) columnFormatters.get(columnIndex);
								if (columnFormatter != null) {
									columnFormattedValue = columnFormatter.format(timeValue);
								}
							}

							if (columnFormattedValue != null) {
								addCellTime(timeValue, columnFormattedValue, sortValue);
							} else {
								String formattedValue;
								if (globalTimeFormatter != null) {
									formattedValue = globalTimeFormatter.format(timeValue);
								} else {
									formattedValue = Config.getTimeDisplayString(timeValue);
								}

								addCellTime(timeValue, formattedValue, sortValue);
							}
						}
					} else {
						addCellTime(timeValue);
					}
					break;
				case Clob:
					value = getColumnValue(rs, columnIndex, columnTypeDefinition);
					addString(value, nullStringDisplay);
					break;
				case Other:
					//ms-access (ucanaccess driver) data type
					value = rs.getObject(columnIndex);
					if (value != null) {
						value = value.toString();
					}
					addString(value, nullStringDisplay);
					break;
				case Binary:
					//e.g. _id column of mongodb collections querying with drill gives a varbinary sql type
					//https://stackoverflow.com/questions/14013534/jdbctemplate-accessing-mysql-varbinary-field-as-string
					//https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
					byte[] bytes = rs.getBytes(columnIndex);
					if (isImageColumn(columnIndex, imageColumns, rsmd)) {
						addCellImage(bytes);
					} else {
						if (bytes != null) {
							value = Hex.encodeHexString(bytes);
						}
						addString(value, nullStringDisplay);
					}
					break;
				default:
					value = rs.getString(columnIndex);
					addString(value, nullStringDisplay);
			}

			columnValues.add(value);
		}

		return columnValues;
	}

	/**
	 * Outputs values for drilldown columns
	 *
	 * @param drilldowns the drilldowns to use, may be null
	 * @param columnValues the values from the resultset data
	 * @throws SQLException
	 */
	private void outputDrilldownColumns(List<Drilldown> drilldowns,
			List<Object> columnValues) throws SQLException {

		if (CollectionUtils.isEmpty(drilldowns)) {
			return;
		}

		for (Drilldown drilldown : drilldowns) {
			DrilldownLinkHelper drilldownLinkHelper = new DrilldownLinkHelper(drilldown, locale, reportRequestParameters);
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

			//clean to be a custom setting? cleanHtmlReportOutput?
			addCellString(drilldownTag);
//				if (requestBaseUrl != null) {
//					String cleanedDrilldownTag = Jsoup.clean(drilldownTag, requestBaseUrl, Whitelist.relaxed().preserveRelativeLinks(true));
//					addCellStringAsIs(cleanedDrilldownTag);
//				} else {
//					addCellString(drilldownTag);
//				}
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
			case Types.TIMESTAMP:
				date = true;
				break;
			default:
				date = false;
		}

		return date;
	}
	
	/**
	 * Returns <code>true</code> if the given sql type is a time one
	 *
	 * @param sqlType the sql/jdbc type
	 * @return <code>true</code> if the given sql type is a time one
	 */
	private boolean isTime(int sqlType) {
		boolean time;

		switch (sqlType) {
			case Types.TIME:
				time = true;
				break;
			default:
				time = false;
		}

		return time;
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
	 * Returns <code>true</code> if the given sql type is a binary one
	 *
	 * @param sqlType the sql/jdbc type
	 * @return <code>true</code> if the given sql type is a binary one
	 */
	private boolean isBinary(int sqlType) {
		boolean binary;

		switch (sqlType) {
			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:
			case Types.BLOB:
				binary = true;
				break;
			default:
				binary = false;
		}

		return binary;
	}

	/**
	 * Outputs a string value
	 *
	 * @param value the value to output
	 * @param nullStringDisplay the string to output if the value is null
	 */
	private void addString(Object value, String nullStringDisplay) {
		if (value == null) {
//			if (requestBaseUrl != null) {
//				String cleanedValue = Jsoup.clean(nullStringDisplay, requestBaseUrl, Whitelist.relaxed().preserveRelativeLinks(true));
//				addCellStringAsIs(cleanedValue);
//			} else {
//				addCellString(nullStringDisplay);
//			}

			addCellString(nullStringDisplay);
		} else {
//			if (requestBaseUrl != null) {
//				String cleanedValue = Jsoup.clean((String) value, requestBaseUrl, Whitelist.relaxed().preserveRelativeLinks(true));
//				addCellStringAsIs(cleanedValue);
//			} else {
//				addCellString((String) value);
//			}

			//https://stackoverflow.com/questions/16815279/difference-between-casting-to-string-and-string-valueof
			//use String.valueOf() instead of cast in case value is not a string
			addCellString(String.valueOf(value));
		}
	}

	/**
	 * Generates crosstab output
	 *
	 * @param rs the resultset to use
	 * @param reportFormat the report format to use
	 * @param report the report that is being run
	 * @return output result
	 * @throws SQLException
	 */
	public StandardOutputResult generateCrosstabOutput(ResultSet rs,
			ReportFormat reportFormat, Report report) throws SQLException {

		logger.debug("Entering generateCrosstabOutput: reportFormat={},"
				+ " report={}", reportFormat, report);

		Objects.requireNonNull(rs, "rs must not be null");
		Objects.requireNonNull(reportFormat, "reportFormat must not be null");
		Objects.requireNonNull(report, "report must not be null");

		/*
		 * input
		 */ /*
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
		 */ /*
		 * output
		 */
		//         y-axis		     	 	  y-axis	      
		//           |		     	 	    |				 
		//  x-axis - _   Feb Jan Mar     	   x-axis - _  Jan Feb Mar
		//           A    24  14  34      	 	    A	14  24  34   
		//           B    24  14  -      	 	    B	14  24   -   
		//           C    -   04  44      	 	    C	04   -  44   
		//                   ^--- Jan comes after Feb!			     	 

		this.report = report;

		StandardOutputResult result = new StandardOutputResult();

		initializeNumberFormatters();

		ResultSetMetaData rsmd = rs.getMetaData();
		resultSetColumnCount = rsmd.getColumnCount();

		if (resultSetColumnCount != 3 && resultSetColumnCount != 5) {
			result.setMessage("reports.message.invalidCrosstab");
			return result;
		}

		int maxRows = Config.getMaxRows(reportFormat.getValue());

		Map<Integer, ColumnTypeDefinition> columnTypes = getColumnTypes(rsmd);

		initializeColumnFormatters(report, rsmd, columnTypes);

		// Check the data type of the value (last column)
		ColumnTypeDefinition valueColumnTypeDefinition = getColumnTypeDefinition(rsmd, resultSetColumnCount);

		String nullNumberDisplay = report.getNullNumberDisplay();
		if (nullNumberDisplay == null) {
			nullNumberDisplay = "";
		}

		String nullStringDisplay = report.getNullStringDisplay();
		if (nullStringDisplay == null) {
			nullStringDisplay = "";
		}

		boolean alternateSort;

		if (resultSetColumnCount > 3) {
			alternateSort = true;
		} else {
			alternateSort = false;
		}

		List<String> localizedColumnNames = getLocalizedColumnNames(rsmd, report);

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
				addValue(Dy.toString() + "-" + Dx.toString(), values, rs, 5, valueColumnTypeDefinition);
			}

			xa = x.keySet().toArray();
			ya = y.keySet().toArray();

			setTotalColumnCount(xa.length + 1);

			//perform any required output initialization
			init();

			addTitle();

			if (showSelectedParameters) {
				addSelectedParameters(reportParamsList);
			}

			//begin header output
			beginHeader();

			addHeaderCell(localizedColumnNames.get(5) + " (" + localizedColumnNames.get(1) + " / " + localizedColumnNames.get(3) + ")");
			int i, j;
			for (i = 0; i < xa.length; i++) {
				addHeaderCell(x.get(xa[i]).toString());
			}
			endHeader();
			beginRows();

			//  _ Jan Feb Mar
			rowCount = 0;
			for (j = 0; j < ya.length; j++) {
				rowCount++;

				newRow();

				if (rowCount % 2 == 0) {
					evenRow = true;
				} else {
					evenRow = false;
				}

				if (rowCount > maxRows) {
					logger.debug("rowCount={}, maxRows={}, reportFormat={}", rowCount, maxRows, reportFormat);
					//row limit exceeded
					for (int k = 0; k < totalColumnCount; k++) {
						addCellString("...");
					}

					endOutput();

					result.setMessage("runReport.message.tooManyRows");
					result.setTooManyRows(true);
					return result;
				} else {
					Object Dy = ya[j];
					addHeaderCellAlignLeft(y.get(Dy).toString(), String.valueOf(Dy)); //column 1 data displayed as a header
					for (i = 0; i < xa.length; i++) {
						Object value = values.get(Dy.toString() + "-" + xa[i].toString());
						outputCrosstabValue(value, valueColumnTypeDefinition, nullNumberDisplay, nullStringDisplay);
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
				addValue(Dy.toString() + "-" + Dx.toString(), values, rs, 3, valueColumnTypeDefinition);
			}

			xa = x.toArray();
			ya = y.toArray();

			setTotalColumnCount(xa.length + 1);

			//perform any required output initialization
			init();

			addTitle();

			if (showSelectedParameters) {
				addSelectedParameters(reportParamsList);
			}

			//begin header output
			beginHeader();
			addHeaderCell(localizedColumnNames.get(3) + " (" + localizedColumnNames.get(1) + " / " + localizedColumnNames.get(2) + ")");
			int i, j;
			for (i = 0; i < xa.length; i++) {
				addHeaderCell(xa[i].toString());
			}

			endHeader();
			beginRows();

			//  _ Jan Feb Mar
			rowCount = 0;
			for (j = 0; j < ya.length; j++) {
				rowCount++;

				newRow();

				if (rowCount % 2 == 0) {
					evenRow = true;
				} else {
					evenRow = false;
				}

				if (rowCount > maxRows) {
					logger.debug("rowCount={}, maxRows={}, reportFormat={}", rowCount, maxRows, reportFormat);
					//row limit exceeded
					for (int k = 0; k < totalColumnCount; k++) {
						addCellString("...");
					}

					endOutput();

					result.setMessage("runReport.message.tooManyRows");
					result.setTooManyRows(true);
					return result;
				} else {
					Object Dy = ya[j];
					//o.addHeaderCell(Dy.toString()); //column 1 data displayed as a header
					addHeaderCellAlignLeft(Dy.toString()); //column 1 data displayed as a header
					for (i = 0; i < xa.length; i++) {
						Object value = values.get(Dy.toString() + "-" + xa[i].toString());
						outputCrosstabValue(value, valueColumnTypeDefinition, nullNumberDisplay, nullStringDisplay);
					}
				}
			}
		}

		endOutput();

		result.setSuccess(true);
		result.setRowCount(rowCount);

		return result;
	}

	/**
	 * Generates crosstab output
	 *
	 * @param data the data to use
	 * @param reportFormat the report format to use
	 * @param report the report that is being run
	 * @return output result
	 * @throws java.lang.Exception
	 */
	public StandardOutputResult generateCrosstabOutput(Object data,
			ReportFormat reportFormat, Report report) throws Exception {

		logger.debug("Entering generateCrosstabOutput: reportFormat={},"
				+ " report={}", reportFormat, report);

		Objects.requireNonNull(data, "data must not be null");
		Objects.requireNonNull(reportFormat, "reportFormat must not be null");
		Objects.requireNonNull(report, "report must not be null");

		this.report = report;

		StandardOutputResult result = new StandardOutputResult();

		initializeNumberFormatters();

		GroovyDataDetails dataDetails = RunReportHelper.getGroovyDataDetails(data, report);
		resultSetColumnCount = dataDetails.getColCount();
		List<String> columnNames = dataDetails.getColumnNames();
		List<? extends Object> dataList = dataDetails.getDataList();
		Map<Integer, ColumnTypeDefinition> columnTypes = dataDetails.getColumnTypes();
		List<String> columnLabels = dataDetails.getColumnLabels();

		if (resultSetColumnCount != 3 && resultSetColumnCount != 5) {
			result.setMessage("reports.message.invalidCrosstab");
			return result;
		}

		int maxRows = Config.getMaxRows(reportFormat.getValue());

		initializeColumnFormatters(report, columnNames, columnTypes);

		// Check the data type of the value (last column)
		ColumnTypeDefinition valueColumnTypeDefinition = columnTypes.get(resultSetColumnCount);

		String nullNumberDisplay = report.getNullNumberDisplay();
		if (nullNumberDisplay == null) {
			nullNumberDisplay = "";
		}

		String nullStringDisplay = report.getNullStringDisplay();
		if (nullStringDisplay == null) {
			nullStringDisplay = "";
		}

		boolean alternateSort;

		if (resultSetColumnCount > 3) {
			alternateSort = true;
		} else {
			alternateSort = false;
		}

		List<String> localizedColumnNames = getLocalizedColumnNames(columnLabels, report);

		HashMap<String, Object> values = new HashMap<>();
		Object[] xa;
		Object[] ya;
		if (alternateSort) { // name1, altSort1, name2, altSort2, value
			TreeMap<Object, Object> x = new TreeMap<>(); // allows a sorted toArray (or Iterator())
			TreeMap<Object, Object> y = new TreeMap<>();

			// Scroll resultset and feed data structures
			// to read it as a crosstab (pivot)
			for (Object row : dataList) {
				Object DyVal = RunReportHelper.getRowValue(row, 1, columnNames);
				Object Dy = RunReportHelper.getRowValue(row, 2, columnNames);
				Object DxVal = RunReportHelper.getRowValue(row, 3, columnNames);
				Object Dx = RunReportHelper.getRowValue(row, 4, columnNames);
				x.put(Dx, DxVal);
				y.put(Dy, DyVal);
				addValue(Dy.toString() + "-" + Dx.toString(), values, row, 5, columnNames);
			}

			xa = x.keySet().toArray();
			ya = y.keySet().toArray();

			setTotalColumnCount(xa.length + 1);

			//perform any required output initialization
			init();

			addTitle();

			if (showSelectedParameters) {
				addSelectedParameters(reportParamsList);
			}

			//begin header output
			beginHeader();

			addHeaderCell(localizedColumnNames.get(5) + " (" + localizedColumnNames.get(1) + " / " + localizedColumnNames.get(3) + ")");
			int i, j;
			for (i = 0; i < xa.length; i++) {
				addHeaderCell(x.get(xa[i]).toString());
			}
			endHeader();
			beginRows();

			//  _ Jan Feb Mar
			rowCount = 0;
			for (j = 0; j < ya.length; j++) {
				rowCount++;

				newRow();

				if (rowCount % 2 == 0) {
					evenRow = true;
				} else {
					evenRow = false;
				}

				if (rowCount > maxRows) {
					logger.debug("rowCount={}, maxRows={}, reportFormat={}", rowCount, maxRows, reportFormat);
					//row limit exceeded
					for (int k = 0; k < totalColumnCount; k++) {
						addCellString("...");
					}

					endOutput();

					result.setMessage("runReport.message.tooManyRows");
					result.setTooManyRows(true);
					return result;
				} else {
					Object Dy = ya[j];
					addHeaderCellAlignLeft(y.get(Dy).toString(), String.valueOf(Dy)); //column 1 data displayed as a header
					for (i = 0; i < xa.length; i++) {
						Object value = values.get(Dy.toString() + "-" + xa[i].toString());
						outputCrosstabValue(value, valueColumnTypeDefinition, nullNumberDisplay, nullStringDisplay);
					}
				}
			}
		} else {
			TreeSet<Object> x = new TreeSet<>(); // allows a sorted toArray (or Iterator())
			TreeSet<Object> y = new TreeSet<>();

			// Scroll resultset and feed data structures
			// to read it as a crosstab (pivot)
			for (Object row : dataList) {
				Object Dy = RunReportHelper.getRowValue(row, 1, columnNames);
				Object Dx = RunReportHelper.getRowValue(row, 2, columnNames);
				x.add(Dx);
				y.add(Dy);
				addValue(Dy.toString() + "-" + Dx.toString(), values, row, 3, columnNames);
			}

			xa = x.toArray();
			ya = y.toArray();

			setTotalColumnCount(xa.length + 1);

			//perform any required output initialization
			init();

			addTitle();

			if (showSelectedParameters) {
				addSelectedParameters(reportParamsList);
			}

			//begin header output
			beginHeader();
			addHeaderCell(localizedColumnNames.get(3) + " (" + localizedColumnNames.get(1) + " / " + localizedColumnNames.get(2) + ")");
			int i, j;
			for (i = 0; i < xa.length; i++) {
				addHeaderCell(xa[i].toString());
			}

			endHeader();
			beginRows();

			//  _ Jan Feb Mar
			rowCount = 0;
			for (j = 0; j < ya.length; j++) {
				rowCount++;

				newRow();

				if (rowCount % 2 == 0) {
					evenRow = true;
				} else {
					evenRow = false;
				}

				if (rowCount > maxRows) {
					logger.debug("rowCount={}, maxRows={}, reportFormat={}", rowCount, maxRows, reportFormat);
					//row limit exceeded
					for (int k = 0; k < totalColumnCount; k++) {
						addCellString("...");
					}

					endOutput();

					result.setMessage("runReport.message.tooManyRows");
					result.setTooManyRows(true);
					return result;
				} else {
					Object Dy = ya[j];
					//o.addHeaderCell(Dy.toString()); //column 1 data displayed as a header
					addHeaderCellAlignLeft(Dy.toString()); //column 1 data displayed as a header
					for (i = 0; i < xa.length; i++) {
						Object value = values.get(Dy.toString() + "-" + xa[i].toString());
						outputCrosstabValue(value, valueColumnTypeDefinition, nullNumberDisplay, nullStringDisplay);
					}
				}
			}
		}

		endOutput();

		result.setSuccess(true);
		result.setRowCount(rowCount);

		return result;
	}

	/**
	 * Stores the right object type in the Hashmap used by
	 * generateCrosstabOutput to cache sorted values
	 *
	 * @param key
	 * @param values
	 * @param rs the resultset at the row with the value to extract
	 * @param columnIndex the one-based column index
	 * @param columnTypeDefinition
	 * @throws SQLException
	 */
	private void addValue(String key, Map<String, Object> values,
			ResultSet rs, int columnIndex, ColumnTypeDefinition columnTypeDefinition)
			throws SQLException {

		Object value = getColumnValue(rs, columnIndex, columnTypeDefinition);
		values.put(key, value);
	}

	/**
	 * Stores the right object type in the Hashmap used by
	 * generateCrosstabOutput to cache sorted values
	 *
	 * @param key
	 * @param values
	 * @param row the current row of data
	 * @param columnIndex the one-based column index
	 * @param columnNames the column names
	 * @throws SQLException
	 */
	private void addValue(String key, Map<String, Object> values,
			Object row, int columnIndex, List<String> columnNames) {

		Object value = RunReportHelper.getRowValue(row, columnIndex, columnNames);
		values.put(key, value);
	}

	/**
	 * Returns the value for a given resultset column
	 *
	 * @param rs the resultset
	 * @param columnIndex the column index
	 * @param columnTypeDefinition the column type definition
	 * @return the value for a given resultset column
	 * @throws SQLException
	 */
	private Object getColumnValue(ResultSet rs, int columnIndex,
			ColumnTypeDefinition columnTypeDefinition) throws SQLException {

		ColumnType columnType = columnTypeDefinition.getColumnType();
		int sqlType = columnTypeDefinition.getSqlType();

		Object value = null;

		switch (columnType) {
			case Numeric:
				//https://www.cis.upenn.edu/~bcpierce/courses/629/jdkdocs/guide/jdbc/getstart/mapping.doc.html
				//http://docs.datastax.com/en/cql/3.3/cql/cql_reference/cql_data_types_c.html
				//cassandra has issues using getDouble() when a column is defined as int
				switch (sqlType) {
					case Types.TINYINT:
					case Types.SMALLINT:
						value = rs.getShort(columnIndex);
						break;
					case Types.INTEGER:
						value = rs.getInt(columnIndex);
						break;
					case Types.BIGINT:
						value = rs.getLong(columnIndex);
						break;
					default:
						value = rs.getDouble(columnIndex);
				}

				if (rs.wasNull()) {
					value = null;
				} else {
					//https://stackoverflow.com/questions/7503877/java-correct-way-convert-cast-object-to-double
					//https://stackoverflow.com/questions/2465096/casting-a-primitive-int-to-a-number
					value = ((Number) value).doubleValue();
				}
				break;
			case Date:
				value = rs.getTimestamp(columnIndex);
				break;
			case Time:
				value = rs.getTime(columnIndex);
				break;
			case Clob:
				Clob clob = rs.getClob(columnIndex);
				if (clob != null) {
					value = clob.getSubString(1, (int) clob.length());
				}
				break;
			case Other:
				//ms-access (ucanaccess driver) data type
				value = rs.getObject(columnIndex);
				if (value != null) {
					value = value.toString();
				}
				break;
			case Binary:
				//e.g. _id column of mongodb collections querying with drill gives a varbinary sql type
				//https://stackoverflow.com/questions/14013534/jdbctemplate-accessing-mysql-varbinary-field-as-string
				//https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
				byte[] bytes = rs.getBytes(columnIndex);
				if (bytes != null) {
					value = Hex.encodeHexString(bytes);
				}
				break;
			default:
				value = rs.getString(columnIndex);
		}

		return value;
	}

	/**
	 * Outputs a value for a crosstab report
	 *
	 * @param value the value to output
	 * @param columnTypeDefinition the column type definition that determines
	 * what type of value it is and how it is to be output
	 * @param nullNumberDisplay the string to display for null numbers
	 * @param nullStringDisplay the string to display for null strings
	 */
	private void outputCrosstabValue(Object value, ColumnTypeDefinition columnTypeDefinition,
			String nullNumberDisplay, String nullStringDisplay) {
		ColumnType columnType = columnTypeDefinition.getColumnType();

		switch (columnType) {
			case Numeric:
				Double numericValue;
				if (value == null) {
					numericValue = null;
				} else {
					numericValue = ((Number) value).doubleValue();
				}

				if (numericValue == null) {
					String sortValue = "null";
					addCellNumeric(numericValue, nullNumberDisplay, sortValue);
				} else {
					String sortValue = getNumericSortValue(numericValue);
					String formattedValue;
					if (globalNumericFormatter != null) {
						formattedValue = globalNumericFormatter.format(numericValue);
					} else {
						formattedValue = formatNumericValue(numericValue);
					}

					addCellNumeric(numericValue, formattedValue, sortValue);
				}
				break;
			case Date:
				Date dateValue = (Date) value;
				if (dateValue == null) {
					addCellDate(dateValue);
				} else {
					long sortValue = getDateSortValue(dateValue);
					String formattedValue;
					if (globalDateFormatter != null) {
						formattedValue = globalDateFormatter.format(dateValue);
					} else {
						formattedValue = Config.getDateDisplayString(dateValue);
					}

					addCellDate(dateValue, formattedValue, sortValue);
				}
				break;
			case Time:
				Date timeValue = (Date) value;
				if (timeValue == null) {
					addCellTime(timeValue);
				} else {
					long sortValue = getDateSortValue(timeValue);
					String formattedValue;
					if (globalTimeFormatter != null) {
						formattedValue = globalTimeFormatter.format(timeValue);
					} else {
						formattedValue = Config.getTimeDisplayString(timeValue);
					}

					addCellTime(timeValue, formattedValue, sortValue);
				}
				break;
			case Clob:
				addString(value, nullStringDisplay);
				break;
			case Other:
				//ms-access (ucanaccess driver) data type
				addString(value, nullStringDisplay);
				break;
			case Binary:
				//e.g. _id column of mongodb collections querying with drill gives a varbinary sql type
				//https://stackoverflow.com/questions/14013534/jdbctemplate-accessing-mysql-varbinary-field-as-string
				//https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
				addString(value, nullStringDisplay);
				break;
			default:
				addString(value, nullStringDisplay);
		}
	}

	/**
	 * Returns the column names to use in report output, considering the current
	 * locale and report i18n options
	 *
	 * @param rsmd the resultset metadata object for the data result set
	 * @param report the report being run
	 * @return the localized column names to use
	 * @throws SQLException
	 */
	private List<String> getLocalizedColumnNames(ResultSetMetaData rsmd,
			Report report) throws SQLException {

		List<String> columnNames = getColumnNames(rsmd);
		return getLocalizedColumnNames(columnNames, report);
	}

	/**
	 * Returns the column names to use in report output, considering the current
	 * locale and report i18n options
	 *
	 * @param rsmd the resultset metadata object for the data result set
	 * @param report the report being run
	 * @return the localized column names to use
	 */
	private List<String> getLocalizedColumnNames(List<String> columnNames,
			Report report) {

		//set defaults
		//add dummy item in index 0. to allow retrieving of column names using 1-based index like with rsmd
		//without it, the index based add() or set() fails with an empty list
		List<String> localizedColumnNames = getOneBasedColumnNames(columnNames);

		//set localized column names if configured
		GeneralReportOptions generalReportOptions = report.getGeneralOptions();
		Reporti18nOptions i18nOptions = generalReportOptions.getI18n();
		if (i18nOptions != null) {
			List<Map<String, List<Map<String, String>>>> i18nColumnNamesSettings = i18nOptions.getColumnNames();

			if (CollectionUtils.isNotEmpty(i18nColumnNamesSettings)) {
				//use case insensitive map so that column names specified in i18n options are not case sensitive
				Map<String, String> localizedColumnNamesMap = new CaseInsensitiveMap<>();

				for (Map<String, List<Map<String, String>>> i18nColumnNameSetting : i18nColumnNamesSettings) {
					//Get the first entry that the iterator returns
					Entry<String, List<Map<String, String>>> entry = i18nColumnNameSetting.entrySet().iterator().next();
					String i18nColumnName = entry.getKey();
					List<Map<String, String>> i18nColumnNameOptions = entry.getValue();
					String localizedColumnName = ArtUtils.getLocalizedValue(locale, i18nColumnNameOptions);
					//https://stackoverflow.com/questions/15091148/hashmaps-and-null-values
					localizedColumnNamesMap.put(i18nColumnName, localizedColumnName);
				}

				//set final column names
				for (int i = 1; i < localizedColumnNames.size(); i++) {
					String columnName = localizedColumnNames.get(i);
					String columnIndexString = String.valueOf(i);
					//try see if localized column index defined
					String localizedColumnName = localizedColumnNamesMap.get(columnIndexString);
					if (localizedColumnName == null) {
						//try search using column name
						localizedColumnName = localizedColumnNamesMap.get(columnName);
					}

					if (localizedColumnName != null) {
						localizedColumnNames.set(i, localizedColumnName);
					}
				}
			}
		}

		return localizedColumnNames;
	}

	/**
	 * Returns the open password to use for a report's output file
	 *
	 * @return the open password to use for a report's output file
	 */
	protected String getEffectiveOpenPassword() {
		RunReportHelper runReportHelper = new RunReportHelper();
		return runReportHelper.getEffectiveOpenPassword(report, dynamicOpenPassword);
	}

	/**
	 * Returns the column names for a resultset
	 *
	 * @param rsmd the resultset metadata object
	 * @return the column names for a resultset
	 * @throws SQLException
	 */
	private List<String> getColumnNames(ResultSetMetaData rsmd) throws SQLException {
		List<String> columnNames = new ArrayList<>();
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			columnNames.add(rsmd.getColumnLabel(i));
		}

		return columnNames;
	}

	/**
	 * Returns column names where retrieval uses a 1-based index
	 *
	 * @param columnNames the original column names
	 * @return column names where retrieval uses a 1-based index
	 */
	private List<String> getOneBasedColumnNames(List<String> columnNames) {
		List<String> columns = new ArrayList<>();
		columns.add("dummy");

		for (String column : columnNames) {
			columns.add(column);
		}

		return columns;
	}
}
