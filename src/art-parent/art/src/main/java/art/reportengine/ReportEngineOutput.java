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
import art.output.StandardOutput;
import art.reportoptions.ReportEngineGroupColumn;
import art.reportoptions.ReportEngineOptions;
import art.utils.ArtUtils;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.sf.reportengine.Report;
import net.sf.reportengine.ReportBuilder;
import net.sf.reportengine.components.CellProps;
import net.sf.reportengine.components.FlatTable;
import net.sf.reportengine.components.ParagraphProps;
import net.sf.reportengine.components.RowProps;
import net.sf.reportengine.config.DefaultDataColumn;
import net.sf.reportengine.config.DefaultGroupColumn;
import net.sf.reportengine.core.calc.AvgGroupCalculator;
import net.sf.reportengine.core.calc.CountGroupCalculator;
import net.sf.reportengine.core.calc.FirstGroupCalculator;
import net.sf.reportengine.core.calc.LastGroupCalculator;
import net.sf.reportengine.core.calc.MaxGroupCalculator;
import net.sf.reportengine.core.calc.MinGroupCalculator;
import net.sf.reportengine.core.calc.SumGroupCalculator;
import net.sf.reportengine.core.steps.ColumnHeaderOutputInitStep;
import net.sf.reportengine.core.steps.DataRowsOutputStep;
import net.sf.reportengine.in.ColumnMetadata;
import net.sf.reportengine.in.JdbcResultsetTableInput;
import net.sf.reportengine.out.AbstractReportOutput;
import net.sf.reportengine.out.OutputFormat;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;

/**
 *
 * @author Timothy Anyona
 */
public class ReportEngineOutput extends AbstractReportOutput {

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

	public void generateReportEngineOutput(ResultSet rs) throws SQLException, IOException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		so.setTotalColumnCount(columnCount);

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

		JdbcResultsetTableInput rsInput = new JdbcResultsetTableInput(rs);
		ObjectFlatTableBuilder flatTableBuilder = new ObjectFlatTableBuilder(rsInput);
		rsInput.open();

		List<ReportEngineGroupColumn> groupColumns = reportEngineOptions.getGroupColumns();
		List<Map<String, ReportEngineCalculator>> calculators = reportEngineOptions.getCalculators();

		List<ColumnMetadata> columnMetadata = rsInput.getColumnMetadata();
		for (int i = 0; i < columnMetadata.size(); i++) {
			ColumnMetadata column = columnMetadata.get(i);
			String columnLabel = column.getColumnLabel();

			boolean isGroupColumn = false;

			if (CollectionUtils.isNotEmpty(groupColumns)) {
				for (ReportEngineGroupColumn groupColumn : groupColumns) {
					String id = groupColumn.getId();
					if (StringUtils.equalsIgnoreCase(columnLabel, id)
							|| StringUtils.equals(String.valueOf(i), id)) {
						isGroupColumn = true;
						DefaultGroupColumn.Builder groupColumnBuilder = new DefaultGroupColumn.Builder(i);
						groupColumnBuilder.header(columnLabel);
						Integer level = groupColumn.getLevel();
						if (level != null) {
							groupColumnBuilder.level(level);
						}
						flatTableBuilder.addGroupColumn(groupColumnBuilder.build());
						break;
					}
				}
			}

			if (!isGroupColumn) {
				DefaultDataColumn.Builder dataColumnBuilder = new DefaultDataColumn.Builder(i);
				dataColumnBuilder.header(columnLabel);
				if (CollectionUtils.isNotEmpty(calculators)) {
					for (Map<String, ReportEngineCalculator> calculatorDefinition : calculators) {
						// Get the first entry that the iterator returns
						Entry<String, ReportEngineCalculator> entry = calculatorDefinition.entrySet().iterator().next();
						String id = entry.getKey();
						ReportEngineCalculator reportEngineCalculator = entry.getValue();

						if ((StringUtils.equalsIgnoreCase(columnLabel, id)
								|| StringUtils.equals(String.valueOf(i), id))
								&& reportEngineCalculator != null) {
							switch (reportEngineCalculator) {
								case SUM:
									String totalString = messageSource.getMessage("reportengine.text.total", null, locale);
									dataColumnBuilder.useCalculator(new SumGroupCalculator(totalString));
									break;
								case COUNT:
									String countString = messageSource.getMessage("reportengine.text.count", null, locale);
									dataColumnBuilder.useCalculator(new CountGroupCalculator(countString));
									break;
								case AVG:
									String avgString = messageSource.getMessage("reportengine.text.average", null, locale);
									dataColumnBuilder.useCalculator(new AvgGroupCalculator(avgString));
									break;
								case MIN:
									String minString = messageSource.getMessage("reportengine.text.minimum", null, locale);
									dataColumnBuilder.useCalculator(new MinGroupCalculator(minString));
									break;
								case MAX:
									String maxString = messageSource.getMessage("reportengine.text.maximum", null, locale);
									dataColumnBuilder.useCalculator(new MaxGroupCalculator(maxString));
									break;
								case FIRST:
									String firstString = messageSource.getMessage("reportengine.text.first", null, locale);
									dataColumnBuilder.useCalculator(new FirstGroupCalculator<>(firstString));
									break;
								case LAST:
									String lastString = messageSource.getMessage("reportengine.text.last", null, locale);
									dataColumnBuilder.useCalculator(new LastGroupCalculator<>(lastString));
									break;
								default:
								//do nothing
							}
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
	}

}
