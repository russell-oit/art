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
import net.sf.cewolfart.links.XYItemLinkGenerator;
import net.sf.cewolfart.tooltips.XYToolTipGenerator;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Day;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides methods for working with time series base charts. These include time
 * series and date series charts.
 *
 * @author Timothy Anyona
 */
public class TimeSeriesBasedChart extends Chart implements XYToolTipGenerator, XYItemLinkGenerator {

	//similar to xychart except for the type of dataset
	//use separate class because there's no generic addSeries() method for xyseriescolletion and timeseriescollection
	private static final Logger logger = LoggerFactory.getLogger(TimeSeriesBasedChart.class);
	private static final long serialVersionUID = 1L;
	private ReportType reportType;

	public TimeSeriesBasedChart(ReportType reportType) {
		logger.debug("Entering TimeSeriesBasedChart: reportType={}", reportType);

		Objects.requireNonNull(reportType, "reportType must not be null");

		switch (reportType) {
			case TimeSeriesChart:
			case DateSeriesChart:
				this.reportType = reportType;
				type = "timeseries";
				setHasTooltips(true);
				break;
			default:
				throw new IllegalArgumentException("Unsupported report type: " + reportType);

		}
	}

	@Override
	public void fillDataset(ResultSet rs) throws SQLException {
		logger.debug("Entering fillDataset");

		Objects.requireNonNull(rs, "rs must not be null");

		TimeSeriesCollection dataset = new TimeSeriesCollection();

		//resultset structure
		//static series: date [, link], series 1 value [, series 2 value, ...]
		//dynamic series: date [, link], seriesName, value
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
		Map<Integer, TimeSeries> finalSeries = new HashMap<>(); //<series index, series>
		Map<String, Integer> seriesIndices = new HashMap<>(); //<series name, series index>
		Map<String, Integer> itemIndices = new HashMap<>(); //<series name, max item index>

		if (dynamicSeries) {
			seriesCount = 0;
		} else {
			//series values start from column 2. column 1 has the xValue (date)
			seriesCount = rsmd.getColumnCount() - 1 - hop; //1 for xValue column

			for (int seriesIndex = 0; seriesIndex < seriesCount; seriesIndex++) {
				int columnIndex = seriesIndex + 2 + hop; //start from column 2
				String seriesName = rsmd.getColumnLabel(columnIndex);
				finalSeries.put(seriesIndex, new TimeSeries(seriesName));
			}
		}

		int recordCount = 0;
		while (rs.next()) {
			recordCount++;

			Date date;
			switch (reportType) {
				case TimeSeriesChart:
					date = rs.getTimestamp(1);
					break;
				case DateSeriesChart:
					date = rs.getDate(1);
					break;
				default:
					throw new IllegalArgumentException("Unexpected report type: " + reportType);
			}

			if (dynamicSeries) {
				//series name is the contents of the second column
				String seriesName = rs.getString(2 + hop);
				double yValue = rs.getDouble(3 + hop);

				//set series name
				int seriesIndex;
				if (seriesIndices.containsKey(seriesName)) {
					seriesIndex = seriesIndices.get(seriesName);
				} else {
					seriesIndex = seriesCount;
					seriesIndices.put(seriesName, seriesIndex);
					finalSeries.put(seriesIndex, new TimeSeries(seriesName));
					seriesCount++;
				}

				//set item index
				int itemIndex;
				if (itemIndices.containsKey(seriesName)) {
					int maxItemIndex = itemIndices.get(seriesName);
					itemIndex = maxItemIndex + 1;
				} else {
					//first item in this series. use zero-based indices
					itemIndex = 0;
				}
				itemIndices.put(seriesName, itemIndex);

				addData(rs, finalSeries, seriesIndex, itemIndex, yValue, date, seriesName);
			} else {
				int itemIndex = recordCount - 1; //-1 to get zero-based index

				for (int seriesIndex = 0; seriesIndex < seriesCount; seriesIndex++) {
					int columnIndex = seriesIndex + 2 + hop; //start from column 2
					String seriesName = rsmd.getColumnLabel(columnIndex);
					double yValue = rs.getDouble(columnIndex);
					addData(rs, finalSeries, seriesIndex, itemIndex, yValue, date, seriesName);
				}
			}
		}

		//add series to dataset
		for (TimeSeries series : finalSeries.values()) {
			dataset.addSeries(series);
		}

		setDataset(dataset);
	}

	@Override
	public void fillDataset(List<? extends Object> data) {
		logger.debug("Entering fillDataset");

		Objects.requireNonNull(data, "data must not be null");

		TimeSeriesCollection dataset = new TimeSeriesCollection();

		//resultset structure
		//static series: date [, link], series 1 value [, series 2 value, ...]
		//dynamic series: date [, link], seriesName, value
		int hop = 0;
		if (isHasHyperLinks()) {
			hop = 1;
		}

		boolean dynamicSeries = extraOptions.isDynamicSeries();

		int seriesCount; //start series index at 0 as generateLink() uses zero-based indices to idenfity series
		Map<Integer, TimeSeries> finalSeries = new HashMap<>(); //<series index, series>
		Map<String, Integer> seriesIndices = new HashMap<>(); //<series name, series index>
		Map<String, Integer> itemIndices = new HashMap<>(); //<series name, max item index>

		if (dynamicSeries) {
			seriesCount = 0;
		} else {
			//series values start from column 2. column 1 has the xValue (date)
			seriesCount = colCount - 1 - hop; //1 for xValue column

			for (int seriesIndex = 0; seriesIndex < seriesCount; seriesIndex++) {
				int columnIndex = seriesIndex + 2 + hop; //start from column 2
				String seriesName = columnNames.get(columnIndex - 1);
				finalSeries.put(seriesIndex, new TimeSeries(seriesName));
			}
		}

		int recordCount = 0;
		for (Object row : data) {
			recordCount++;

			Date date;
			switch (reportType) {
				case TimeSeriesChart:
					date = RunReportHelper.getDateTimeRowValue(row, 1 - 1, columnNames);
					break;
				case DateSeriesChart:
					date = RunReportHelper.getDateRowValue(row, 1 - 1, columnNames);
					break;
				default:
					throw new IllegalArgumentException("Unexpected report type: " + reportType);
			}

			if (dynamicSeries) {
				//series name is the contents of the second column
				String seriesName = RunReportHelper.getStringRowValue(row, 2 + hop - 1, columnNames);
				double yValue = RunReportHelper.getDoubleRowValue(row, 3 + hop - 1, columnNames);

				//set series name
				int seriesIndex;
				if (seriesIndices.containsKey(seriesName)) {
					seriesIndex = seriesIndices.get(seriesName);
				} else {
					seriesIndex = seriesCount;
					seriesIndices.put(seriesName, seriesIndex);
					finalSeries.put(seriesIndex, new TimeSeries(seriesName));
					seriesCount++;
				}

				//set item index
				int itemIndex;
				if (itemIndices.containsKey(seriesName)) {
					int maxItemIndex = itemIndices.get(seriesName);
					itemIndex = maxItemIndex + 1;
				} else {
					//first item in this series. use zero-based indices
					itemIndex = 0;
				}
				itemIndices.put(seriesName, itemIndex);

				addData(row, finalSeries, seriesIndex, itemIndex, yValue, date, seriesName);
			} else {
				int itemIndex = recordCount - 1; //-1 to get zero-based index

				for (int seriesIndex = 0; seriesIndex < seriesCount; seriesIndex++) {
					int columnIndex = seriesIndex + 2 + hop; //start from column 2
					String seriesName = columnNames.get(columnIndex - 1);
					double yValue = RunReportHelper.getDoubleRowValue(row, columnIndex - 1, columnNames);
					addData(row, finalSeries, seriesIndex, itemIndex, yValue, date, seriesName);
				}
			}
		}

		//add series to dataset
		for (TimeSeries series : finalSeries.values()) {
			dataset.addSeries(series);
		}

		setDataset(dataset);
	}

	/**
	 * Adds data to the dataset
	 *
	 * @param rs the resultset with the current row of data
	 * @param finalSeries the dataset to populate
	 * @param seriesIndex the series index
	 * @param itemIndex the item index
	 * @param yValue the y value
	 * @param date the date
	 * @param seriesName the series name
	 * @throws SQLException
	 */
	private void addData(ResultSet rs, Map<Integer, TimeSeries> finalSeries,
			int seriesIndex, int itemIndex, double yValue, Date date, String seriesName) throws SQLException {

		//add dataset value
		switch (reportType) {
			case TimeSeriesChart:
				finalSeries.get(seriesIndex).add(new Millisecond(date), yValue);
				break;
			case DateSeriesChart:
				finalSeries.get(seriesIndex).add(new Day(date), yValue);
				break;
			default:
				throw new IllegalArgumentException("Unexpected report type: " + reportType);
		}

		//use series index and item index to identify url in hashmap
		//to ensure correct link will be returned by the generatelink() method. 
		//use series index instead of name because the generateLink() method uses series indices
		String linkId = String.valueOf(seriesIndex) + String.valueOf(itemIndex);

		//add hyperlink if required
		addHyperLink(rs, linkId);

		//add drilldown link if required
		//drill down on col 1 = y value (data value)
		//drill down on col 2 = x value (date)
		//drill down on col 3 = series name
		addDrilldownLink(linkId, yValue, date, seriesName);
	}

	/**
	 * Adds data to the dataset
	 *
	 * @param row the current row of data
	 * @param finalSeries the dataset to populate
	 * @param seriesIndex the series index
	 * @param itemIndex the item index
	 * @param yValue the y value
	 * @param date the date
	 * @param seriesName the series name
	 * @throws SQLException
	 */
	private void addData(Object row, Map<Integer, TimeSeries> finalSeries,
			int seriesIndex, int itemIndex, double yValue, Date date, String seriesName) {

		//add dataset value
		switch (reportType) {
			case TimeSeriesChart:
				finalSeries.get(seriesIndex).add(new Millisecond(date), yValue);
				break;
			case DateSeriesChart:
				finalSeries.get(seriesIndex).add(new Day(date), yValue);
				break;
			default:
				throw new IllegalArgumentException("Unexpected report type: " + reportType);
		}

		//use series index and item index to identify url in hashmap
		//to ensure correct link will be returned by the generatelink() method. 
		//use series index instead of name because the generateLink() method uses series indices
		String linkId = String.valueOf(seriesIndex) + String.valueOf(itemIndex);

		//add hyperlink if required
		addHyperLink(row, linkId);

		//add drilldown link if required
		//drill down on col 1 = y value (data value)
		//drill down on col 2 = x value (date)
		//drill down on col 3 = series name
		addDrilldownLink(linkId, yValue, date, seriesName);
	}

	@Override
	public String generateToolTip(XYDataset data, int series, int item) {
		//format y value
		double yValue = data.getYValue(series, item);

		NumberFormat nf = NumberFormat.getInstance(locale);
		String formattedYValue = nf.format(yValue);

		//format x value (date)
		long xValue = (long) data.getXValue(series, item);
		Date date = new Date(xValue);

		SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM-dd-yyyy", locale);
		String formattedDate = dateFormatter.format(date);

		//return final tooltip text	   
		return formattedYValue + ", " + formattedDate;
	}

	@Override
	public String generateLink(Object data, int series, int item) {
		String link = "";

		String key = String.valueOf(series) + String.valueOf(item);

		if (getHyperLinks() != null) {
			link = getHyperLinks().get(key);
		} else if (getDrilldownLinks() != null) {
			link = getDrilldownLinks().get(key);
		}

		return link;
	}

	@Override
	public void processChart(JFreeChart chart, Map<String, String> params) {
		postProcessChart(chart);

		//set custom date format if applicable
		if (StringUtils.isNotBlank(extraOptions.getDateFormat())) {
			XYPlot plot = (XYPlot) chart.getPlot();
			DateAxis axis = (DateAxis) plot.getDomainAxis();
			axis.setDateFormatOverride(new SimpleDateFormat(extraOptions.getDateFormat(), locale));
		}

	}

}
