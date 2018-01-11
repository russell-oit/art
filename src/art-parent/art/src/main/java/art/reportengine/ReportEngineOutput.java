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

import art.output.StandardOutput;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import net.sf.reportengine.Report;
import net.sf.reportengine.ReportBuilder;
import net.sf.reportengine.components.CellProps;
import net.sf.reportengine.components.FlatTable;
import net.sf.reportengine.components.ParagraphProps;
import net.sf.reportengine.components.RowProps;
import net.sf.reportengine.config.DefaultDataColumn;
import net.sf.reportengine.config.HorizAlign;
import net.sf.reportengine.core.steps.ColumnHeaderOutputInitStep;
import net.sf.reportengine.core.steps.DataRowsOutputStep;
import net.sf.reportengine.in.ColumnMetadata;
import net.sf.reportengine.in.JdbcResultsetTableInput;
import net.sf.reportengine.out.AbstractReportOutput;
import net.sf.reportengine.out.OutputFormat;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Timothy Anyona
 */
public class ReportEngineOutput extends AbstractReportOutput {

	private StandardOutput so;
	private int headerCount = 0;
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

	public void generateReportEngineOutput(ResultSet rs) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		so.setTotalColumnCount(columnCount);

		JdbcResultsetTableInput rsInput = new JdbcResultsetTableInput(rs);
		ObjectFlatTableBuilder flatTableBuilder = new ObjectFlatTableBuilder(rsInput);
		rsInput.open();
		List<ColumnMetadata> columnMetadata = rsInput.getColumnMetadata();
		for (int i = 0; i < columnMetadata.size(); i++) {
			ColumnMetadata column = columnMetadata.get(i);
			flatTableBuilder.addDataColumn(new DefaultDataColumn.Builder(i)
					.header(column.getColumnLabel())
					.horizAlign(HorizAlign.LEFT)
					.build());
		}

		FlatTable flatTable = flatTableBuilder.build();
		Report report = new ReportBuilder(this)
				.add(flatTable)
				.build();

		//report execution    
		report.execute();
	}

}
