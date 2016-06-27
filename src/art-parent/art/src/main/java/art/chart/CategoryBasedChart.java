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
package art.chart;

import art.enums.ReportType;
import de.laures.cewolf.links.CategoryItemLinkGenerator;
import de.laures.cewolf.tooltips.CategoryToolTipGenerator;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.HashMap;
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
				setType("line");
				break;
			case HorizontalBar2DChart:
				setType("horizontalBar");
				break;
			case HorizontalBar3DChart:
				setType("horizontalBar3D");
				break;
			case VerticalBar2DChart:
				setType("verticalBar");
				break;
			case VerticalBar3DChart:
				setType("verticalBar3D");
				break;
			case StackedHorizontalBar2DChart:
				setType("stackedHorizontalBar");
				break;
			case StackedHorizontalBar3DChart:
				setType("stackedHorizontalBar3D");
				break;
			case StackedVerticalBar2DChart:
				setType("stackedVerticalBar");
				break;
			case StackedVerticalBar3DChart:
				setType("stackedVerticalBar3D");
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

		//use different report type or a report option or first column name to indicate static/dynamic series?
		ResultSetMetaData rsmd = rs.getMetaData();
		boolean dynamicSeries = false;
		String secondColumnClassName = rsmd.getColumnClassName(2 + hop);
		if (StringUtils.equals(secondColumnClassName, "java.lang.String")) {
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

		setSeriesColorOptions(rsmd);

		while (rs.next()) {
			String categoryName = rs.getString(1);

			if (dynamicSeries) {
				//series name is the contents of the second column
				String seriesName = rs.getString(2 + hop);
				double yValue = rs.getDouble(3 + hop);

				//set series index
				int seriesIndex;
				if (seriesIndices.containsKey(seriesName)) {
					seriesIndex = seriesIndices.get(seriesName);
				} else {
					seriesIndex = seriesCount;
					seriesIndices.put(seriesName, seriesIndex);
					seriesCount++;
				}

				addData(rs, dataset, seriesIndex, yValue, categoryName, seriesName);
			} else {
				for (int seriesIndex = 0; seriesIndex < seriesCount; seriesIndex++) {
					int columnIndex = seriesIndex + 2 + hop; //start from column 2
					String seriesName = rsmd.getColumnLabel(columnIndex);
					if (isOptionsColumn(seriesName)) {
						continue;
					}
					double yValue = rs.getDouble(columnIndex);
					addData(rs, dataset, seriesIndex, yValue, categoryName, seriesName);
				}
			}
		}

		setDataset(dataset);
	}

	/**
	 * Adds data from the resultset to the dataset object
	 *
	 * @param rs the sesultset to use
	 * @param dataset the dataset to populate
	 * @param seriesIndex the series index
	 * @param yValue the y value
	 * @param categoryName the category name
	 * @param seriesName the series name
	 * @throws SQLException
	 */
	private void addData(ResultSet rs, DefaultCategoryDataset dataset,
			int seriesIndex, double yValue, String categoryName, String seriesName) throws SQLException {

		//add dataset value
		dataset.addValue(yValue, seriesName, categoryName);

		//use series index and category name to identify url in hashmap
		//to ensure correct link will be returned by the generatelink() method. 
		//use series index instead of name because the generateLink() method uses series indices
		String linkId = String.valueOf(seriesIndex) + categoryName;

		//add hyperlink if required
		addHyperLink(rs, linkId);

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

		NumberFormat nf = NumberFormat.getInstance(getLocale());
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
