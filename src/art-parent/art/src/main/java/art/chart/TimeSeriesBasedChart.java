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
import net.sf.cewolfart.links.XYItemLinkGenerator;
import net.sf.cewolfart.tooltips.XYToolTipGenerator;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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
	private String dateFormat;

	public TimeSeriesBasedChart(ReportType reportType) {
		logger.debug("Entering TimeSeriesBasedChart: reportType={}", reportType);

		Objects.requireNonNull(reportType, "reportType must not be null");

		switch (reportType) {
			case TimeSeriesChart:
			case DateSeriesChart:
				this.reportType = reportType;
				setType("timeseries");
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

		//use single report type for date and time series?
		//use different report type or a report option or first column name to indicate static/dynamic series?
		ResultSetMetaData rsmd = rs.getMetaData();
		boolean dynamicSeries = false;
		String secondColumnClassName = rsmd.getColumnClassName(2 + hop);
		if (StringUtils.equals(secondColumnClassName, "java.lang.String")) {
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
				if (isOptionsColumn(seriesName)) {
					continue;
				}
				finalSeries.put(seriesIndex, new TimeSeries(seriesName));
			}
		}

		setSeriesColorOptions(rsmd);

		setDateFormat(rsmd);

		int rowCount = 0;

		while (rs.next()) {
			rowCount++;

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
				int itemIndex = rowCount - 1; //-1 to get zero-based index

				for (int seriesIndex = 0; seriesIndex < seriesCount; seriesIndex++) {
					int columnIndex = seriesIndex + 2 + hop; //start from column 2
					String seriesName = rsmd.getColumnLabel(columnIndex);
					if (isOptionsColumn(seriesName)) {
						continue;
					}
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

	/**
	 * Sets the optional date format property that defines a fixed date format
	 * to use in the chart
	 *
	 * @param rsmd the resultset metadata
	 * @throws SQLException
	 */
	private void setDateFormat(ResultSetMetaData rsmd) throws SQLException {
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			String columnName = rsmd.getColumnLabel(i);
			if (StringUtils.startsWithIgnoreCase(columnName, "dateFormat:")) {
				dateFormat = StringUtils.substringAfter(columnName, ":");
				break;
			}
		}
	}

	/**
	 * Adds data from the resultset to the dataset
	 *
	 * @param rs the resultset to use
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
		switch(reportType){
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

	@Override
	public String generateToolTip(XYDataset data, int series, int item) {
		//format y value
		double yValue = data.getYValue(series, item);

		NumberFormat nf = NumberFormat.getInstance(getLocale());
		String formattedYValue = nf.format(yValue);

		//format x value (date)
		long xValue = (long) data.getXValue(series, item);
		Date date = new Date(xValue);

		SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM-dd-yyyy");
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
		if (StringUtils.isNotBlank(dateFormat)) {
			XYPlot plot = (XYPlot) chart.getPlot();
			DateAxis axis = (DateAxis) plot.getDomainAxis();
			axis.setDateFormatOverride(new SimpleDateFormat(dateFormat));
		}

	}
}
