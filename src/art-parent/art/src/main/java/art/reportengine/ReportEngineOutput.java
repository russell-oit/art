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
package art.reportengine;

import art.enums.ReportEngineCalculator;
import art.enums.ReportType;
import art.enums.SortOrder;
import art.output.StandardOutput;
import art.reportoptions.ReportEngineDataColumn;
import art.reportoptions.ReportEngineGroupColumn;
import art.reportoptions.ReportEngineOptions;
import art.servlets.Config;
import art.utils.ArtUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.sf.reportengine.Report;
import net.sf.reportengine.ReportBuilder;
import net.sf.reportengine.components.CellProps;
import net.sf.reportengine.components.FlatTable;
import net.sf.reportengine.components.FlatTableBuilder;
import net.sf.reportengine.components.ParagraphProps;
import net.sf.reportengine.components.PivotTable;
import net.sf.reportengine.components.PivotTableBuilder;
import net.sf.reportengine.components.RowProps;
import net.sf.reportengine.config.DefaultDataColumn;
import net.sf.reportengine.config.DefaultGroupColumn;
import net.sf.reportengine.config.DefaultPivotData;
import net.sf.reportengine.config.DefaultPivotHeaderRow;
import net.sf.reportengine.core.calc.AvgGroupCalculator;
import net.sf.reportengine.core.calc.CountGroupCalculator;
import net.sf.reportengine.core.calc.FirstGroupCalculator;
import net.sf.reportengine.core.calc.GroupCalculator;
import net.sf.reportengine.core.calc.LastGroupCalculator;
import net.sf.reportengine.core.calc.MaxGroupCalculator;
import net.sf.reportengine.core.calc.MinGroupCalculator;
import net.sf.reportengine.core.calc.SumGroupCalculator;
import net.sf.reportengine.core.steps.ColumnHeaderOutputInitStep;
import net.sf.reportengine.core.steps.DataRowsOutputStep;
import net.sf.reportengine.in.ColumnMetadata;
import net.sf.reportengine.in.JdbcResultsetTableInput;
import net.sf.reportengine.in.TableInput;
import net.sf.reportengine.in.TextTableInput;
import net.sf.reportengine.out.AbstractReportOutput;
import net.sf.reportengine.out.OutputFormat;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

/**
 * Generates output using the ReportEngine library
 *
 * @author Timothy Anyona
 */
public class ReportEngineOutput extends AbstractReportOutput {

	private static final Logger logger = LoggerFactory.getLogger(ReportEngineOutput.class);

	private StandardOutput so;
	private int bodyCount = 0;

	public ReportEngineOutput(StandardOutput so) {
		this(so, new GeneralOutputFormat());
	}

	public ReportEngineOutput(StandardOutput so, OutputFormat outputFormat) {
		super(outputFormat);

		this.so = so;
	}

	@Override
	public <T> void output(String templateName, T model) {
		if (model instanceof ParagraphProps) {
			so.newRow();

			ParagraphProps paragraphProps = (ParagraphProps) model;
			String text = paragraphProps.getText();

			so.addCellString(text);
		} else if (model instanceof RowProps) {
			if (StringUtils.equals(templateName, ColumnHeaderOutputInitStep.START_HEADER_ROW_TEMPLATE)) {
				so.beginHeader();
			} else if (StringUtils.equals(templateName, DataRowsOutputStep.START_DATA_ROW_TEMPLATE)) {
				if (bodyCount == 0) {
					so.beginRows();
					so.newRow();
				} else {
					so.newRow();
				}
				bodyCount++;
			} else {
				so.newRow();
			}
		} else if (model instanceof CellProps) {
			CellProps cellProps = (CellProps) model;
			Object value = cellProps.getValue();

			if (StringUtils.equals(templateName, ColumnHeaderOutputInitStep.HEADER_CELL_TEMPLATE)) {
				String text = String.valueOf(value);
				so.addHeaderCell(text);
			} else {
				addCellValue(value);
			}
		} else if (StringUtils.equals(templateName, ColumnHeaderOutputInitStep.END_HEADER_ROW_TEMPLATE)) {
			so.endHeader();
		} else if (StringUtils.equals(templateName, "endTable.ftl")) {
			if (bodyCount > 0) {
				so.endRow();
				so.endRows();
			}
		} else if (StringUtils.equals(templateName, "startReport.ftl")) {
			so.init();
			so.addTitle();
			so.initializeNumberFormatters();
		}
	}

	private void addCellValue(Object value) {
		if (value == null) {
			so.addCellString("");
		} else if (value instanceof Number) {
			Number number = (Number) value;
			so.addCellNumeric(number.doubleValue());
		} else if (value instanceof Date) {
			Date date = (Date) value;
			so.addCellDate(date);
		} else {
			so.addCellString(String.valueOf(value));
		}
	}

	@Override
	public void close() {
		try {
			so.endOutput();
		} finally {
			super.close();
		}
	}

	/**
	 * Generates tabular output
	 *
	 * @param rs the resultset that contains the data to output. May be null for
	 * reportengine file report type
	 * @param reportType the report type
	 * @throws SQLException
	 * @throws IOException
	 */
	public void generateTabularOutput(ResultSet rs, ReportType reportType)
			throws SQLException, IOException {

		Objects.requireNonNull(reportType, "reportType must not be null");

		MessageSource messageSource = so.getMessageSource();
		Locale locale = so.getLocale();

		art.report.Report artReport = so.getReport();
		String options = artReport.getOptions();
		ReportEngineOptions reportEngineOptions;
		if (StringUtils.isBlank(options)) {
			reportEngineOptions = new ReportEngineOptions();
		} else {
			reportEngineOptions = ArtUtils.jsonToObject(options, ReportEngineOptions.class);
		}

		List<ReportEngineGroupColumn> groupColumns = reportEngineOptions.getGroupColumns();
		List<ReportEngineDataColumn> dataColumns = reportEngineOptions.getDataColumns();

		if (reportType == ReportType.ReportEngine) {
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			so.setTotalColumnCount(columnCount);

			JdbcResultsetTableInput rsInput = new JdbcResultsetTableInput(rs);
			ObjectFlatTableBuilder flatTableBuilder = new ObjectFlatTableBuilder(rsInput);
			rsInput.open();

			List<ColumnMetadata> columnMetadata = rsInput.getColumnMetadata();
			for (int i = 0; i < columnMetadata.size(); i++) {
				ColumnMetadata column = columnMetadata.get(i);
				String columnLabel = column.getColumnLabel();

				boolean isGroupColumn = false;

				if (CollectionUtils.isNotEmpty(groupColumns)) {
					for (ReportEngineGroupColumn groupColumn : groupColumns) {
						String id = groupColumn.getId();
						if (StringUtils.equalsIgnoreCase(columnLabel, id)
								|| StringUtils.equals(String.valueOf(i + 1), id)) {
							isGroupColumn = true;
							DefaultGroupColumn.Builder groupColumnBuilder = new DefaultGroupColumn.Builder(i);
							groupColumnBuilder.header(columnLabel);
							prepareGroupColumnBuilder(groupColumnBuilder, groupColumn);
							flatTableBuilder.addGroupColumn(groupColumnBuilder.build());
							break;
						}
					}
				}

				if (!isGroupColumn) {
					DefaultDataColumn.Builder dataColumnBuilder = new DefaultDataColumn.Builder(i);
					dataColumnBuilder.header(columnLabel);
					if (CollectionUtils.isNotEmpty(dataColumns)) {
						for (ReportEngineDataColumn dataColumn : dataColumns) {
							String id = dataColumn.getId();
							if (StringUtils.equalsIgnoreCase(columnLabel, id)
									|| StringUtils.equals(String.valueOf(i + 1), id)) {
								prepareDataColumnBuilder(dataColumnBuilder, dataColumn, messageSource, locale);
								break;
							}
						}
					}
					flatTableBuilder.addDataColumn(dataColumnBuilder.build());
				}
			}

			if (reportEngineOptions.isSortValues()) {
				flatTableBuilder.sortValues();
			}
			Boolean showTotals = reportEngineOptions.getShowTotals();
			if (showTotals != null) {
				flatTableBuilder.showTotals(showTotals);
			}
			Boolean showGrandTotal = reportEngineOptions.getShowGrandTotal();
			if (showGrandTotal != null) {
				flatTableBuilder.showGrandTotal(showGrandTotal);
			}

			FlatTable flatTable = flatTableBuilder.build();
			Report report = new ReportBuilder(this)
					.add(flatTable)
					.build();

			//report execution    
			report.execute();
		} else if (reportType == ReportType.ReportEngineFile) {
			if (CollectionUtils.isEmpty(dataColumns)) {
				throw new IllegalStateException("dataColumns not specified");
			}
			int columnCount = dataColumns.size();
			if (CollectionUtils.isNotEmpty(groupColumns)) {
				columnCount += groupColumns.size();
			}
			so.setTotalColumnCount(columnCount);

			TableInput tableInput;
			String separator = reportEngineOptions.getSeparator();
			boolean firstLineIsHeader = reportEngineOptions.isFirstLineIsHeader();
			String urlString = reportEngineOptions.getUrl();
			InputStreamReader reader = null;
			try {
				if (StringUtils.isBlank(urlString)) {
					String templateFileName = artReport.getTemplate();
					String templatesPath = Config.getTemplatesPath();
					String fullTemplateFileName = templatesPath + templateFileName;

					logger.debug("templateFileName='{}'", templateFileName);

					//need to explicitly check if template file is empty string
					//otherwise file.exists() will return true because fullTemplateFileName will just have the directory name
					if (StringUtils.isBlank(templateFileName)) {
						throw new IllegalArgumentException("Data file not specified");
					}

					File templateFile = new File(fullTemplateFileName);
					if (!templateFile.exists()) {
						throw new IllegalStateException("Data file not found: " + templateFileName);
					}

					String encoding = "UTF-8";
					tableInput = new TextTableInput(fullTemplateFileName, separator, encoding, firstLineIsHeader);
				} else {
					URL url = new URL(urlString);
					reader = new InputStreamReader(url.openStream());
					tableInput = new TextTableInput(reader, separator, firstLineIsHeader);
				}

				FlatTableBuilder flatTableBuilder = new FlatTableBuilder(tableInput);

				if (CollectionUtils.isNotEmpty(groupColumns)) {
					for (int i = 0; i < groupColumns.size(); i++) {
						ReportEngineGroupColumn groupColumn = groupColumns.get(i);
						int finalIndex;
						Integer index = groupColumn.getIndex();
						if (index == null) {
							finalIndex = i;
						} else {
							finalIndex = index;
						}
						DefaultGroupColumn.Builder groupColumnBuilder = new DefaultGroupColumn.Builder(finalIndex);
						groupColumnBuilder.header(groupColumn.getId());
						prepareGroupColumnBuilder(groupColumnBuilder, groupColumn);
						flatTableBuilder.addGroupColumn(groupColumnBuilder.build());
					}
				}

				for (int i = 0; i < dataColumns.size(); i++) {
					ReportEngineDataColumn dataColumn = dataColumns.get(i);
					int finalIndex;
					Integer index = dataColumn.getIndex();
					if (index == null) {
						finalIndex = i;
					} else {
						finalIndex = index;
					}
					DefaultDataColumn.Builder dataColumnBuilder = new DefaultDataColumn.Builder(finalIndex);
					dataColumnBuilder.header(dataColumn.getId());
					prepareDataColumnBuilder(dataColumnBuilder, dataColumn, messageSource, locale);
					flatTableBuilder.addDataColumn(dataColumnBuilder.build());
				}

				if (reportEngineOptions.isSortValues()) {
					flatTableBuilder.sortValues();
				}
				Boolean showTotals = reportEngineOptions.getShowTotals();
				if (showTotals != null) {
					flatTableBuilder.showTotals(showTotals);
				}
				Boolean showGrandTotal = reportEngineOptions.getShowGrandTotal();
				if (showGrandTotal != null) {
					flatTableBuilder.showGrandTotal(showGrandTotal);
				}

				FlatTable flatTable = flatTableBuilder.build();
				Report report = new ReportBuilder(this)
						.add(flatTable)
						.build();

				//report execution    
				report.execute();
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException ex) {
						logger.error("Error", ex);
					}
				}
			}
		}
	}

	/**
	 * Generates pivot output
	 *
	 * @param rs the resultset that contains the data. May be null for
	 * reportengine file report type
	 * @param reportType the report type
	 * @throws SQLException
	 * @throws IOException
	 */
	public void generatePivotOutput(ResultSet rs, ReportType reportType)
			throws SQLException, IOException {

		MessageSource messageSource = so.getMessageSource();
		Locale locale = so.getLocale();

		art.report.Report artReport = so.getReport();
		String options = artReport.getOptions();
		ReportEngineOptions reportEngineOptions;
		if (StringUtils.isBlank(options)) {
			reportEngineOptions = new ReportEngineOptions();
		} else {
			reportEngineOptions = ArtUtils.jsonToObject(options, ReportEngineOptions.class);
		}

		List<ReportEngineGroupColumn> groupColumns = reportEngineOptions.getGroupColumns();
		List<ReportEngineDataColumn> dataColumns = reportEngineOptions.getDataColumns();
		List<String> pivotHeaderRows = reportEngineOptions.getPivotHeaderRows();
		ReportEngineDataColumn pivotData = reportEngineOptions.getPivotData();
		int optionsColumnCount = reportEngineOptions.getColumnCount();

		Objects.requireNonNull(pivotHeaderRows, "pivotHeaderRows must not be null");
		Objects.requireNonNull(pivotData, "pivotData must not be null");

		if (reportType == ReportType.ReportEngine) {
			int columnCount;
			if (optionsColumnCount > 0) {
				columnCount = optionsColumnCount;
			} else {
				ResultSetMetaData rsmd = rs.getMetaData();
				columnCount = rsmd.getColumnCount();
			}
			so.setTotalColumnCount(columnCount);

			JdbcResultsetTableInput rsInput = new JdbcResultsetTableInput(rs);
			PivotTableBuilder pivotTableBuilder = new PivotTableBuilder(rsInput);
			rsInput.open();

			List<ColumnMetadata> columnMetadata = rsInput.getColumnMetadata();
			for (int i = 0; i < columnMetadata.size(); i++) {
				ColumnMetadata column = columnMetadata.get(i);
				String columnLabel = column.getColumnLabel();

				boolean isGroupColumn = false;
				boolean isHeaderRow = false;
				boolean isPivotData = false;

				if (CollectionUtils.isNotEmpty(groupColumns)) {
					for (ReportEngineGroupColumn groupColumn : groupColumns) {
						String id = groupColumn.getId();
						if (StringUtils.equalsIgnoreCase(columnLabel, id)
								|| StringUtils.equals(String.valueOf(i + 1), id)) {
							isGroupColumn = true;
							DefaultGroupColumn.Builder groupColumnBuilder = new DefaultGroupColumn.Builder(i);
							groupColumnBuilder.header(columnLabel);
							prepareGroupColumnBuilder(groupColumnBuilder, groupColumn);
							pivotTableBuilder.addGroupColumn(groupColumnBuilder.build());
							break;
						}
					}
				}

				for (String headerRow : pivotHeaderRows) {
					if (StringUtils.equalsIgnoreCase(columnLabel, headerRow)
							|| StringUtils.equals(String.valueOf(i + 1), headerRow)) {
						isHeaderRow = true;
						pivotTableBuilder.addHeaderRow(new DefaultPivotHeaderRow(i));
						break;
					}
				}

				String pivotDataColumnId = pivotData.getId();
				if (StringUtils.equalsIgnoreCase(columnLabel, pivotDataColumnId)
						|| StringUtils.equals(String.valueOf(i + 1), pivotDataColumnId)) {
					isPivotData = true;
					DefaultPivotData.Builder pivotDataBuilder = new DefaultPivotData.Builder(i);
					@SuppressWarnings("rawtypes")
					GroupCalculator groupCalculator = getGroupCalculator(pivotData, messageSource, locale);
					if (groupCalculator != null) {
						String calculatorFormatter = pivotData.getCalculatorFormatter();
						if (StringUtils.isBlank(calculatorFormatter)) {
							pivotDataBuilder.useCalculator(groupCalculator);
						} else {
							pivotDataBuilder.useCalculator(groupCalculator, calculatorFormatter);
						}
					}

					String valuesFormatter = pivotData.getValuesFormatter();
					if (StringUtils.isNotBlank(valuesFormatter)) {
						pivotDataBuilder.valuesFormatter(valuesFormatter);
					}

					pivotTableBuilder.pivotData(pivotDataBuilder.build());
				}

				if (!isGroupColumn && !isHeaderRow && !isPivotData) {
					DefaultDataColumn.Builder dataColumnBuilder = new DefaultDataColumn.Builder(i);
					dataColumnBuilder.header(columnLabel);
					if (CollectionUtils.isNotEmpty(dataColumns)) {
						for (ReportEngineDataColumn dataColumn : dataColumns) {
							String id = dataColumn.getId();
							if (StringUtils.equalsIgnoreCase(columnLabel, id)
									|| StringUtils.equals(String.valueOf(i + 1), id)) {
								prepareDataColumnBuilder(dataColumnBuilder, dataColumn, messageSource, locale);
								break;
							}
						}
					}
					pivotTableBuilder.addDataColumn(dataColumnBuilder.build());
				}
			}

			if (reportEngineOptions.isSortValues()) {
				pivotTableBuilder.sortValues();
			}
			Boolean showTotals = reportEngineOptions.getShowTotals();
			if (showTotals != null) {
				pivotTableBuilder.showTotals(showTotals);
			}
			Boolean showGrandTotal = reportEngineOptions.getShowGrandTotal();
			if (showGrandTotal != null) {
				pivotTableBuilder.showGrandTotal(showGrandTotal);
			}

			PivotTable pivotTable = pivotTableBuilder.build();
			Report report = new ReportBuilder(this)
					.add(pivotTable)
					.build();

			//report execution    
			report.execute();
		} else if (reportType == ReportType.ReportEngineFile) {
			if (CollectionUtils.isEmpty(dataColumns)) {
				throw new IllegalStateException("dataColumns not specified");
			}

			int columnCount;
			if (optionsColumnCount > 0) {
				columnCount = optionsColumnCount;
			} else {
				columnCount = dataColumns.size();
				if (CollectionUtils.isNotEmpty(groupColumns)) {
					columnCount += groupColumns.size();
				}
			}
			so.setTotalColumnCount(columnCount);

			TableInput tableInput;
			String separator = reportEngineOptions.getSeparator();
			boolean firstLineIsHeader = reportEngineOptions.isFirstLineIsHeader();
			String urlString = reportEngineOptions.getUrl();
			InputStreamReader reader = null;
			try {
				if (StringUtils.isBlank(urlString)) {
					String templateFileName = artReport.getTemplate();
					String templatesPath = Config.getTemplatesPath();
					String fullTemplateFileName = templatesPath + templateFileName;

					logger.debug("templateFileName='{}'", templateFileName);

					//need to explicitly check if template file is empty string
					//otherwise file.exists() will return true because fullTemplateFileName will just have the directory name
					if (StringUtils.isBlank(templateFileName)) {
						throw new IllegalArgumentException("Data file not specified");
					}

					File templateFile = new File(fullTemplateFileName);
					if (!templateFile.exists()) {
						throw new IllegalStateException("Data file not found: " + templateFileName);
					}

					String encoding = "UTF-8";
					tableInput = new TextTableInput(fullTemplateFileName, separator, encoding, firstLineIsHeader);
				} else {
					URL url = new URL(urlString);
					reader = new InputStreamReader(url.openStream());
					tableInput = new TextTableInput(reader, separator, firstLineIsHeader);
				}

				PivotTableBuilder pivotTableBuilder = new PivotTableBuilder(tableInput);

				if (CollectionUtils.isNotEmpty(groupColumns)) {
					for (int i = 0; i < groupColumns.size(); i++) {
						ReportEngineGroupColumn groupColumn = groupColumns.get(i);
						int finalIndex;
						Integer index = groupColumn.getIndex();
						if (index == null) {
							finalIndex = i;
						} else {
							finalIndex = index;
						}
						DefaultGroupColumn.Builder groupColumnBuilder = new DefaultGroupColumn.Builder(finalIndex);
						groupColumnBuilder.header(groupColumn.getId());
						prepareGroupColumnBuilder(groupColumnBuilder, groupColumn);
						pivotTableBuilder.addGroupColumn(groupColumnBuilder.build());
					}
				}

				for (int i = 0; i < dataColumns.size(); i++) {
					ReportEngineDataColumn dataColumn = dataColumns.get(i);
					int finalIndex;
					Integer index = dataColumn.getIndex();
					if (index == null) {
						finalIndex = i;
					} else {
						finalIndex = index;
					}
					DefaultDataColumn.Builder dataColumnBuilder = new DefaultDataColumn.Builder(finalIndex);
					dataColumnBuilder.header(dataColumn.getId());
					prepareDataColumnBuilder(dataColumnBuilder, dataColumn, messageSource, locale);
					pivotTableBuilder.addDataColumn(dataColumnBuilder.build());
				}

				for (String headerRow : pivotHeaderRows) {
					int index = Integer.parseInt(headerRow);
					pivotTableBuilder.addHeaderRow(new DefaultPivotHeaderRow(index));
				}

				String pivotDataColumnId = pivotData.getId();
				int pivotDataIndex = Integer.parseInt(pivotDataColumnId);
				DefaultPivotData.Builder pivotDataBuilder = new DefaultPivotData.Builder(pivotDataIndex);
				@SuppressWarnings("rawtypes")
				GroupCalculator groupCalculator = getGroupCalculator(pivotData, messageSource, locale);
				if (groupCalculator != null) {
					String calculatorFormatter = pivotData.getCalculatorFormatter();
					if (StringUtils.isBlank(calculatorFormatter)) {
						pivotDataBuilder.useCalculator(groupCalculator);
					} else {
						pivotDataBuilder.useCalculator(groupCalculator, calculatorFormatter);
					}
				}

				String valuesFormatter = pivotData.getValuesFormatter();
				if (StringUtils.isNotBlank(valuesFormatter)) {
					pivotDataBuilder.valuesFormatter(valuesFormatter);
				}

				pivotTableBuilder.pivotData(pivotDataBuilder.build());

				if (reportEngineOptions.isSortValues()) {
					pivotTableBuilder.sortValues();
				}
				Boolean showTotals = reportEngineOptions.getShowTotals();
				if (showTotals != null) {
					pivotTableBuilder.showTotals(showTotals);
				}
				Boolean showGrandTotal = reportEngineOptions.getShowGrandTotal();
				if (showGrandTotal != null) {
					pivotTableBuilder.showGrandTotal(showGrandTotal);
				}

				PivotTable pivotTable = pivotTableBuilder.build();
				Report report = new ReportBuilder(this)
						.add(pivotTable)
						.build();

				//report execution    
				report.execute();
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException ex) {
						logger.error("Error", ex);
					}
				}
			}
		}
	}

	/**
	 * Returns a group calculator according to configuration provided in a data
	 * column
	 *
	 * @param dataColumn the data column
	 * @param messageSource the message source
	 * @param locale the locale
	 * @return a group calculator, null if none is defined
	 */
	@SuppressWarnings("rawtypes")
	public GroupCalculator getGroupCalculator(ReportEngineDataColumn dataColumn,
			MessageSource messageSource, Locale locale) {

		GroupCalculator groupCalculator = null;
		ReportEngineCalculator calculator = dataColumn.getCalculator();
		if (calculator != null) {
			switch (calculator) {
				case SUM:
					String totalString = messageSource.getMessage("reportengine.text.total", null, locale);
					groupCalculator = new SumGroupCalculator(totalString);
					break;
				case COUNT:
					String countString = messageSource.getMessage("reportengine.text.count", null, locale);
					groupCalculator = new CountGroupCalculator(countString);
					break;
				case AVG:
					String avgString = messageSource.getMessage("reportengine.text.average", null, locale);
					groupCalculator = new AvgGroupCalculator(avgString);
					break;
				case MIN:
					String minString = messageSource.getMessage("reportengine.text.minimum", null, locale);
					groupCalculator = new MinGroupCalculator(minString);
					break;
				case MAX:
					String maxString = messageSource.getMessage("reportengine.text.maximum", null, locale);
					groupCalculator = new MaxGroupCalculator(maxString);
					break;
				case FIRST:
					String firstString = messageSource.getMessage("reportengine.text.first", null, locale);
					groupCalculator = new FirstGroupCalculator<>(firstString);
					break;
				case LAST:
					String lastString = messageSource.getMessage("reportengine.text.last", null, locale);
					groupCalculator = new LastGroupCalculator<>(lastString);
					break;
				default:
					break;
			}
		}

		return groupCalculator;
	}

	/**
	 * Prepares a data column builder object using settings provided in a data
	 * column definition
	 *
	 * @param dataColumnBuilder the data column builder object
	 * @param dataColumn the data column definition
	 * @param messageSource the message source to use
	 * @param locale the locale to use
	 */
	public void prepareDataColumnBuilder(DefaultDataColumn.Builder dataColumnBuilder,
			ReportEngineDataColumn dataColumn, MessageSource messageSource, Locale locale) {

		@SuppressWarnings("rawtypes")
		GroupCalculator groupCalculator = getGroupCalculator(dataColumn, messageSource, locale);
		if (groupCalculator != null) {
			String calculatorFormatter = dataColumn.getCalculatorFormatter();
			if (StringUtils.isBlank(calculatorFormatter)) {
				dataColumnBuilder.useCalculator(groupCalculator);
			} else {
				dataColumnBuilder.useCalculator(groupCalculator, calculatorFormatter);
			}
		}

		SortOrder sortOrder = dataColumn.getSortOrder();
		if (sortOrder != null) {
			Integer sortOrderLevel = dataColumn.getSortOrderLevel();
			switch (sortOrder) {
				case Asc:
					if (sortOrderLevel == null) {
						dataColumnBuilder.sortAsc();
					} else {
						dataColumnBuilder.sortAsc(sortOrderLevel);
					}
					break;
				case Desc:
					if (sortOrderLevel == null) {
						dataColumnBuilder.sortDesc();
					} else {
						dataColumnBuilder.sortDesc(sortOrderLevel);
					}
					break;
				default:
					break;
			}
		}

		String valuesFormatter = dataColumn.getValuesFormatter();
		if (StringUtils.isNotBlank(valuesFormatter)) {
			dataColumnBuilder.valuesFormatter(valuesFormatter);
		}
	}

	/**
	 * Prepares a group column builder object by setting using settings provided
	 * in a group column definition
	 *
	 * @param groupColumnBuilder the group column builder
	 * @param groupColumn the group column definition
	 */
	private void prepareGroupColumnBuilder(DefaultGroupColumn.Builder groupColumnBuilder,
			ReportEngineGroupColumn groupColumn) {

		Integer level = groupColumn.getLevel();
		if (level != null) {
			groupColumnBuilder.level(level);
		}

		if (groupColumn.isShowDuplicateValues()) {
			groupColumnBuilder.showDuplicateValues();
		}

		SortOrder sortOrder = groupColumn.getSortOrder();
		if (sortOrder != null) {
			switch (sortOrder) {
				case Asc:
					groupColumnBuilder.sortAsc();
					break;
				case Desc:
					groupColumnBuilder.sortDesc();
					break;
				default:
					break;
			}
		}

		String valuesFormatter = groupColumn.getValuesFormatter();
		if (StringUtils.isNotBlank(valuesFormatter)) {
			groupColumnBuilder.valuesFormatter(valuesFormatter);
		}
	}

}
