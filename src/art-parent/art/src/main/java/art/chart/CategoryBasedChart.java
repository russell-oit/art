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
package art.chart;

import art.enums.ReportType;
import art.runreport.RunReportHelper;
import net.sf.cewolfart.links.CategoryItemLinkGenerator;
import net.sf.cewolfart.tooltips.CategoryToolTipGenerator;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides methods for working with category based charts. These include the
 * line chart and bar charts.
 *
 * @author Timothy Anyona
 */
public class CategoryBasedChart extends Chart implements CategoryToolTipGenerator, CategoryItemLinkGenerator {

	private static final Logger logger = LoggerFactory.getLogger(CategoryBasedChart.class);
	private static final long serialVersionUID = 1L;

	public CategoryBasedChart(ReportType reportType) {
		logger.debug("Entering CategoryBasedChart: reportType={}", reportType);

		Objects.requireNonNull(reportType, "reportType must not be null");

		switch (reportType) {
			case LineChart:
				type = "line";
				break;
			case HorizontalBar2DChart:
				type = "horizontalBar";
				break;
			case HorizontalBar3DChart:
				type = "horizontalBar3D";
				break;
			case VerticalBar2DChart:
				type = "verticalBar";
				break;
			case VerticalBar3DChart:
				type = "verticalBar3D";
				break;
			case StackedHorizontalBar2DChart:
				type = "stackedHorizontalBar";
				break;
			case StackedHorizontalBar3DChart:
				type = "stackedHorizontalBar3D";
				break;
			case StackedVerticalBar2DChart:
				type = "stackedVerticalBar";
				break;
			case StackedVerticalBar3DChart:
				type = "stackedVerticalBar3D";
				break;
			default:
				throw new IllegalArgumentException("Unsupported report type: " + reportType);
		}

		setHasTooltips(true);
	}

	@Override
	protected void fillDataset(ResultSet rs) throws SQLException {
		logger.debug("Entering fillDataset");

		Objects.requireNonNull(rs, "rs must not be null");

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		//resultset structure
		//static series: category name [, link], series 1 value [, series 2 value, ...]
		//dynamic series: category name [, link], seriesName, value
		int hop = 0;
		if (isHasHyperLinks()) {
			hop = 1;
		}

		boolean optionsDynamicSeries = extraOptions.isDynamicSeries();

		ResultSetMetaData rsmd = rs.getMetaData();
		boolean columnDynamicSeries = false;
		String secondColumnClassName = rsmd.getColumnClassName(2 + hop);
		if (StringUtils.equals(secondColumnClassName, "java.lang.String")) {
			columnDynamicSeries = true;
		}

		boolean dynamicSeries = false;
		if (optionsDynamicSeries || columnDynamicSeries) {
			dynamicSeries = true;
		}

		int seriesCount; //start series index at 0 as generateLink() uses zero-based indices to idenfity series
		Map<String, Integer> seriesIndices = new HashMap<>(); //<series name, series index>

		if (dynamicSeries) {
			seriesCount = 0;
		} else {
			//series values start from column 2. column 1 has the xValue (category name)
			seriesCount = rsmd.getColumnCount() - 1 - hop; //1 for xValue column
		}

		resultSetColumnNames = new ArrayList<>();
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			String columnName = rsmd.getColumnLabel(i);
			resultSetColumnNames.add(columnName);
		}

		resultSetData = new ArrayList<>();

		while (rs.next()) {
			resultSetRecordCount++;

			Map<String, Object> row = new LinkedHashMap<>();
			Map<Integer, Object> indexRow = new LinkedHashMap<>();
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				String columnName = rsmd.getColumnLabel(i);
				Object data = rs.getObject(i);
				row.put(columnName, data);
				indexRow.put(i, data);
			}
			
			if (includeDataInOutput) {
				resultSetData.add(row);
			}

			String categoryName = RunReportHelper.getStringRowValue(indexRow, 1);

			if (dynamicSeries) {
				//series name is the contents of the second column
				String seriesName = RunReportHelper.getStringRowValue(indexRow, 2 + hop);
				double yValue = RunReportHelper.getDoubleRowValue(indexRow, 3 + hop);

				//set series index
				int seriesIndex;
				if (seriesIndices.containsKey(seriesName)) {
					seriesIndex = seriesIndices.get(seriesName);
				} else {
					seriesIndex = seriesCount;
					seriesIndices.put(seriesName, seriesIndex);
					seriesCount++;
				}

				addData(row, dataset, seriesIndex, yValue, categoryName, seriesName);
			} else {
				for (int seriesIndex = 0; seriesIndex < seriesCount; seriesIndex++) {
					int columnIndex = seriesIndex + 2 + hop; //start from column 2
					String seriesName = resultSetColumnNames.get(columnIndex - 1);
					double yValue = RunReportHelper.getDoubleRowValue(indexRow, columnIndex);
					addData(row, dataset, seriesIndex, yValue, categoryName, seriesName);
				}
			}
		}

		setDataset(dataset);
	}

	@Override
	protected void fillDataset(List<? extends Object> data) {
		logger.debug("Entering fillDataset");

		Objects.requireNonNull(data, "data must not be null");

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		//resultset structure
		//static series: category name [, link], series 1 value [, series 2 value, ...]
		//dynamic series: category name [, link], seriesName, value
		int hop = 0;
		if (isHasHyperLinks()) {
			hop = 1;
		}

		boolean dynamicSeries = extraOptions.isDynamicSeries();

		int seriesCount; //start series index at 0 as generateLink() uses zero-based indices to idenfity series
		Map<String, Integer> seriesIndices = new HashMap<>(); //<series name, series index>

		if (dynamicSeries) {
			seriesCount = 0;
		} else {
			//series values start from column 2. column 1 has the xValue (category name)
			seriesCount = colCount - 1 - hop; //1 for xValue column
		}

		for (Object row : data) {
			String categoryName = RunReportHelper.getStringRowValue(row, 1, columnNames);

			if (dynamicSeries) {
				//series name is the contents of the second column
				String seriesName = RunReportHelper.getStringRowValue(row, 2 + hop, columnNames);
				double yValue = RunReportHelper.getDoubleRowValue(row, 3 + hop, columnNames);

				//set series index
				int seriesIndex;
				if (seriesIndices.containsKey(seriesName)) {
					seriesIndex = seriesIndices.get(seriesName);
				} else {
					seriesIndex = seriesCount;
					seriesIndices.put(seriesName, seriesIndex);
					seriesCount++;
				}

				addData(row, dataset, seriesIndex, yValue, categoryName, seriesName);
			} else {
				for (int seriesIndex = 0; seriesIndex < seriesCount; seriesIndex++) {
					int columnIndex = seriesIndex + 2 + hop; //start from column 2
					String seriesName = columnNames.get(columnIndex - 1);
					double yValue = RunReportHelper.getDoubleRowValue(row, columnIndex, columnNames);
					addData(row, dataset, seriesIndex, yValue, categoryName, seriesName);
				}
			}
		}

		setDataset(dataset);
	}

	/**
	 * Adds data to the dataset object
	 *
	 * @param row the current row of data
	 * @param dataset the dataset to populate
	 * @param seriesIndex the series index
	 * @param yValue the y value
	 * @param categoryName the category name
	 * @param seriesName the series name
	 * @throws SQLException
	 */
	private void addData(Object row, DefaultCategoryDataset dataset,
			int seriesIndex, double yValue, String categoryName, String seriesName) {

		//add dataset value
		if (swapAxes) {
			dataset.addValue(yValue, categoryName, seriesName);
		} else {
			dataset.addValue(yValue, seriesName, categoryName);
		}

		//use series index and category name to identify url in hashmap
		//to ensure correct link will be returned by the generatelink() method. 
		//use series index instead of name because the generateLink() method uses series indices
		String linkId = String.valueOf(seriesIndex) + categoryName;

		//add hyperlink if required
		addHyperLink(row, linkId);

		//add drilldown link if required
		//drill down on col 1 = y value (data value)
		//drill down on col 2 = x value (category name)
		//drill down on col 3 = series name
		addDrilldownLink(linkId, yValue, categoryName, seriesName);
	}

	@Override
	public String generateToolTip(CategoryDataset data, int series, int item) {
		//format data value
		double dataValue = data.getValue(series, item).doubleValue();

		NumberFormat nf = NumberFormat.getInstance(locale);
		String formattedValue = nf.format(dataValue);

		//in case one wishes to show category names
		/*
		 String mainCategory=String.valueOf(data.getColumnKey(item));
		 String subCategory=String.valueOf(data.getRowKey(series));		
		 */
		//return final tooltip text
		return formattedValue;
	}

	@Override
	public String generateLink(Object dataset, int series, Object category) {
		String link = "";

		String key = String.valueOf(series) + String.valueOf(category);

		if (getHyperLinks() != null) {
			link = getHyperLinks().get(key);
		} else if (getDrilldownLinks() != null) {
			link = getDrilldownLinks().get(key);
		}

		return link;
	}
}
