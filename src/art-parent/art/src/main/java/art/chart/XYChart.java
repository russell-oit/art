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

import art.parameter.Parameter;
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
 *
 * @author Timothy Anyona
 */
public class XYChart extends AbstractChart implements XYToolTipGenerator, XYItemLinkGenerator {

	private static final long serialVersionUID = 1L;

	public XYChart() {
		setType("xy");
		setHasTooltips(true);
	}

	@Override
	public void fillDataset(ResultSet rs) throws SQLException {
		Objects.requireNonNull(rs, "resultset must not be null");

		XYSeriesCollection dataset = new XYSeriesCollection();
		
		//resultset structure
		//static series: xValue, yValue [,link]
		//dynamic series: xValue, yValue, seriesName [,link]

		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();

		boolean dynamicSeries = false;
		final int dynamicSeriesColumnCount = 3; //xValue, yValue, seriesName
		if (isHasHyperLinks()) {
			if (columnCount == dynamicSeriesColumnCount + 1) { //+1 for hyperlink column
				dynamicSeries = true;
			}
		} else {
			if (columnCount == dynamicSeriesColumnCount) {
				dynamicSeries = true;
			}
		}

		int seriesCount = 0; //start series index at 0 as generateLink() uses zero-based indices to idenfity series
		Map<Integer,XYSeries> finalSeries = new HashMap<>(); //<series index, series>
		Map<String, Integer> existingSeries = new HashMap<>(); //<series name, series index>

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

			//has this series already appeared?
			int seriesIndex;
			if (existingSeries.containsKey(seriesName)) {
				seriesIndex = existingSeries.get(seriesName);
			} else {
				seriesIndex = seriesCount;
				existingSeries.put(seriesName, seriesIndex);
				finalSeries.put(seriesIndex,new XYSeries(seriesName));
				seriesCount++;
			}

			//add dataset value
			finalSeries.get(seriesIndex).add(xValue, yValue);

			//use series index, y data value and x data value to identify url in hashmap
			//to ensure correct link will be returned in generatelink. 
			//use series index instead of name because the generateLink() method uses series indices
			String linkId = seriesIndex + String.valueOf(yValue) + String.valueOf(xValue);

			//add hyperlink if required
			addHyperLink(rs, linkId);

			//add drilldown link if required
			addDrilldownLink(yValue, xValue, seriesName, linkId);
		}

		//add series to dataset
		for (XYSeries series : finalSeries.values()) {
			dataset.addSeries(series);
		}

		setDataset(dataset);
	}

	private void addDrilldownLink(double yValue, double xValue, String seriesName, String key) {
		//set drill down links
		if (getDrilldown() != null) {
			StringBuilder sb = new StringBuilder(200);

			//add base url
			addDrilldownBaseUrl(sb);

			//add drilldown parameters
			if (getDrilldownParams() != null) {
				for (Parameter drilldownParam : getDrilldownParams()) {
					//drill down on col 1 = y value (data value)
					//drill down on col 2 = x value (category)
					//drill down on col 3 = series name
					String paramName = drilldownParam.getName();
					String paramValue;
					if (drilldownParam.getDrilldownColumnIndex() == 1) {
						paramValue = String.valueOf(yValue);
					} else if (drilldownParam.getDrilldownColumnIndex() == 2) {
						paramValue = String.valueOf(xValue);
					} else {
						paramValue = seriesName;
					}
					addUrlParameter(paramName, paramValue, sb);
				}
			}

			//add parameters from parent report
			addParentParameters(sb);

			String drilldownUrl = sb.toString();
			getDrilldownLinks().put(key, drilldownUrl);
		}
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

		XYDataset xyDataset = (XYDataset) data;

		double yValue = xyDataset.getYValue(series, item);
		double xValue = xyDataset.getXValue(series, item);

		String key = String.valueOf(series) + String.valueOf(yValue) + String.valueOf(xValue);

		if (getHyperLinks() != null) {
			link = getHyperLinks().get(key);
		} else if (getDrilldownLinks() != null) {
			link = getDrilldownLinks().get(key);
		}

		return link;
	}

}
