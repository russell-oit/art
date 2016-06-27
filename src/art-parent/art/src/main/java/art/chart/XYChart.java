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

import de.laures.cewolf.links.XYItemLinkGenerator;
import de.laures.cewolf.tooltips.XYToolTipGenerator;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Provides methods for working with xy charts
 *
 * @author Timothy Anyona
 */
public class XYChart extends Chart implements XYToolTipGenerator, XYItemLinkGenerator {

	private static final long serialVersionUID = 1L;

	public XYChart() {
		setType("xy");
		setHasTooltips(true);
	}

	@Override
	public void fillDataset(ResultSet rs) throws SQLException {
		Objects.requireNonNull(rs, "rs must not be null");

		XYSeriesCollection dataset = new XYSeriesCollection();

		//resultset structure
		//static series: xValue, yValue [, link]
		//dynamic series: xValue, yValue, seriesName [, link]
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();

		int optionsColumnCount = 0;
		for (int i = 0; i < columnCount; i++) {
			int columnIndex = i + 1;
			String columnName = rsmd.getColumnLabel(columnIndex);
			if (isOptionsColumn(columnName)) {
				optionsColumnCount++;
			}
		}

		boolean dynamicSeries = false;
		int dynamicSeriesColumnCount = 3 + optionsColumnCount; //xValue, yValue, seriesName
		if (isHasHyperLinks()) {
			if (columnCount == dynamicSeriesColumnCount + 1) { //+1 for hyperlink column
				dynamicSeries = true;
			}
		} else {
			if (columnCount == dynamicSeriesColumnCount) {
				dynamicSeries = true;
			}
		}

		setSeriesColorOptions(rsmd);

		int seriesCount = 0; //start series index at 0 as generateLink() uses zero-based indices to idenfity series
		Map<Integer, XYSeries> finalSeries = new HashMap<>(); //<series index, series>
		Map<String, Integer> seriesIndices = new HashMap<>(); //<series name, series index>
		Map<String, Integer> itemIndices = new HashMap<>(); //<series name, max item index>

		while (rs.next()) {
			double xValue = rs.getDouble(1);
			double yValue = rs.getDouble(2);

			String seriesName;
			if (dynamicSeries) {
				//series name is the contents of the third column
				seriesName = rs.getString(3);
			} else {
				//currently only one series supported
				//series name is the column alias of the second column
				//can optimize static series values out of the loop
				seriesName = rsmd.getColumnLabel(2);
			}

			//set series index
			int seriesIndex;
			if (seriesIndices.containsKey(seriesName)) {
				seriesIndex = seriesIndices.get(seriesName);
			} else {
				seriesIndex = seriesCount;
				seriesIndices.put(seriesName, seriesIndex);
				finalSeries.put(seriesIndex, new XYSeries(seriesName));
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

			//add dataset value
			finalSeries.get(seriesIndex).add(xValue, yValue);

			//use series index and item index to identify url in hashmap
			//to ensure correct link will be returned by the generatelink() method. 
			//use series index instead of name because the generateLink() method uses series indices
			String linkId = String.valueOf(seriesIndex) + String.valueOf(itemIndex);

			//add hyperlink if required
			addHyperLink(rs, linkId);

			//add drilldown link if required
			//drill down on col 1 = y value
			//drill down on col 2 = x value
			//drill down on col 3 = series name
			addDrilldownLink(linkId, yValue, xValue, seriesName);
		}

		//add series to dataset
		for (XYSeries series : finalSeries.values()) {
			dataset.addSeries(series);
		}

		setDataset(dataset);
	}

	@Override
	public String generateToolTip(XYDataset data, int series, int item) {
		//display formatted values
		NumberFormat nf = NumberFormat.getInstance(getLocale());

		//format y value
		double yValue = data.getYValue(series, item);
		String formattedYValue = nf.format(yValue);

		//format x value
		double xValue = data.getXValue(series, item);
		String formattedXValue = nf.format(xValue);

		//return final tooltip text	   
		return formattedXValue + ", " + formattedYValue;
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
