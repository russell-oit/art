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
package art.chart;

import art.enums.ReportType;
import de.laures.cewolf.links.XYItemLinkGenerator;
import de.laures.cewolf.tooltips.XYToolTipGenerator;
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
import org.jfree.data.time.Day;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

/**
 *
 * @author Timothy Anyona
 */
public class TimeSeriesBasedChart extends Chart implements XYToolTipGenerator, XYItemLinkGenerator {

	//similar to xychart except for the type of dataset
	//use separate class because there's no generic addSeries() method for xyseriescolletion and timeseriescollection
	private static final long serialVersionUID = 1L;
	private ReportType reportType;

	public TimeSeriesBasedChart(ReportType reportType) {
		if (reportType != ReportType.TimeSeriesChart
				&& reportType != ReportType.DateSeriesChart) {
			throw new IllegalArgumentException("Unsupported report type: " + reportType);
		}

		this.reportType = reportType;

		setType("timeseries");
		setHasTooltips(true);
	}

	@Override
	public void fillDataset(ResultSet rs) throws SQLException {
		Objects.requireNonNull(rs, "resultset must not be null");

		TimeSeriesCollection dataset = new TimeSeriesCollection();

		//resultset structure
		//static series: date [, link], series 1 value [, series 2 value, ...]
		//dynamic series: date [, link], seriesName, value
		int hop = 0;
		if (isHasHyperLinks()) {
			hop = 1;
		}

		//TODO use single report type for date and time series?
		//TODO use different report type or a report option or first column name to indicate static/dynamic series?
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
				finalSeries.put(seriesIndex, new TimeSeries(seriesName));
			}
		}

		int rowCount = 0;

		while (rs.next()) {
			rowCount++;

			Date date;
			if (reportType == ReportType.TimeSeriesChart) {
				date = rs.getTimestamp(1);
			} else {
				date = rs.getDate(1);
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

	private void addData(ResultSet rs, Map<Integer, TimeSeries> finalSeries,
			int seriesIndex, int itemIndex, double yValue, Date date, String seriesName) throws SQLException {

		//add dataset value
		if (reportType == ReportType.TimeSeriesChart) {
			finalSeries.get(seriesIndex).add(new Millisecond(date), yValue);
		} else {
			finalSeries.get(seriesIndex).add(new Day(date), yValue);
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

}
