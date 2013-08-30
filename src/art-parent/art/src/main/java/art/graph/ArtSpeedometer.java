/**
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.graph;

import art.utils.ArtQueryParam;
import art.utils.DrilldownQuery;
import de.laures.cewolf.ChartPostProcessor;
import de.laures.cewolf.DatasetProduceException;
import de.laures.cewolf.DatasetProducer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.apache.commons.lang.StringUtils;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.MeterInterval;
import org.jfree.chart.plot.MeterPlot;
import org.jfree.data.Range;
import org.jfree.data.general.DefaultValueDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to generate speedometer chart. Query should be in the form
 * <p>
 * 
 * <code>select dataValue, minValue, maxValue, unitsDescription, ranges </code> <br>
 * <br>
 * <b>ranges</b> optional columns and each range has 3 values separated by : i.e. rangeUpperValue:rangeColour:rangeDescription
 * </p>
 * 
 * <b>Example</b>
 * 
 * <pre>
 * <code>
 * select 45, 0, 100, "Units",
 * 50,"#00FF00","Normal",
 * 80,"#FFFF00","Warning",
 * 100,"#FF0000","Critical"
 * </code>
 * </pre>
 * 
 * @author Timothy Anyona
 */
public class ArtSpeedometer implements ArtGraph, DatasetProducer, ChartPostProcessor, Serializable {

	private static final long serialVersionUID = 1L;
	final static Logger logger = LoggerFactory.getLogger(ArtSpeedometer.class);
	String title = "Title";
	String xAxisLabel = "Not Used";
	String yAxisLabel = "Not Used";
	String seriesName = "Not Used";
	int height = 300;
	int width = 500;
	String bgColor = "#FFFFFF";
	boolean useHyperLinks = false;
	boolean hasDrilldown = false;
	boolean hasTooltips = false;
	double minValue;
	double maxValue;
	String unitsDescription;
	TreeMap<Integer, Double> rangeValues;
	TreeMap<Integer, String> rangeColors;
	TreeMap<Integer, String> rangeDescriptions;
	TreeMap<Integer, Range> rangeRanges;
	int rangeCount;
	String openDrilldownInNewWindow;
	DefaultValueDataset dataset = new DefaultValueDataset();
	Map<Integer, ArtQueryParam> displayParameters = null; // to enable display of graph parameters in pdf output

	/**
	 * Constructor
	 */
	public ArtSpeedometer() {
	}

	@Override
	public void setQueryType(int queryType) {
		// not used
	}

	@Override
	public void setDisplayParameters(Map<Integer, ArtQueryParam> value) {
		displayParameters = value;
	}

	@Override
	public Map<Integer, ArtQueryParam> getDisplayParameters() {
		return displayParameters;
	}

	@Override
	public RowSetDynaClass getGraphData() {
		return null; // never show data for speedometer chart
	}

	@Override
	public void setShowGraphData(boolean value) {
		// do nothing. for speedometer never show data. doesn't make sense
	}

	@Override
	public boolean isShowGraphData() {
		return false; // for speedometer never show data. doesn't make sense
	}

	@Override
	public String getOpenDrilldownInNewWindow() {
		return openDrilldownInNewWindow;
	}

	@Override
	public boolean getHasTooltips() {
		return hasTooltips;
	}

	@Override
	public boolean getHasDrilldown() {
		return hasDrilldown;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setXAxisLabel(String xlabel) {
		this.xAxisLabel = xlabel;
	}

	@Override
	public String getXAxisLabel() {
		return xAxisLabel;
	}

	@Override
	public void setYAxisLabel(String value) {
		this.yAxisLabel = value;
	}

	@Override
	public String getYAxisLabel() {
		return yAxisLabel;
	}

	@Override
	public void setSeriesName(String value) {
		this.seriesName = value;
	}

	@Override
	public void setWidth(int value) {
		this.width = value;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public void setHeight(int value) {
		this.height = value;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void setBgColor(String value) {
		this.bgColor = value;
	}

	@Override
	public String getBgColor() {
		return bgColor;
	}

	@Override
	public void setUseHyperLinks(boolean b) {
		this.useHyperLinks = b;
	}

	@Override
	public boolean getUseHyperLinks() {
		return useHyperLinks;
	}

	// overload used by exportgraph class. no drill down for scheduled charts
	@Override
	public void prepareDataset(ResultSet rs) throws SQLException {
		prepareDataset(rs, null, null, null);
	}

	// prepare graph data structures with query results
	@Override
	public void prepareDataset(ResultSet rs, Map<Integer, DrilldownQuery> drilldownQueries, Map<String, String> inlineParams, Map<String, String[]> multiParams) throws SQLException {

		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();

		if (rs.next()) {
			dataset.setValue(rs.getDouble(1));
			minValue = rs.getDouble(2);
			maxValue = rs.getDouble(3);
			unitsDescription = rs.getString(4);

			if (columnCount > 4) {
				// ranges have been specified
				rangeValues = new TreeMap<Integer, Double>();
				rangeColors = new TreeMap<Integer, String>();
				rangeDescriptions = new TreeMap<Integer, String>();
				rangeRanges = new TreeMap<Integer, Range>();
				Integer key;
				rangeCount = 0;
				for (int i = 5; i <= columnCount; i++) {
					String rangeSpec = rs.getString(i);
					if (rangeSpec != null) {
						String[] rangeDetails = StringUtils.split(rangeSpec, ":");
						if (rangeDetails.length == 3) {
							rangeCount++;
							key = Integer.valueOf(rangeCount);
							String valuePart = rangeDetails[0];
							double rangeValue;
							if (valuePart.contains("%")) {
								rangeValue = Double.parseDouble(valuePart.replace("%", ""));
								rangeValue = minValue + (maxValue - minValue) * rangeValue / 100.0D;
							} else {
								rangeValue = Double.parseDouble(valuePart);
							}

							rangeValues.put(key, Double.valueOf(rangeValue));
							rangeColors.put(key, rangeDetails[1]);
							rangeDescriptions.put(key, rangeDetails[2]);
						}
					}
				}

				// build chart ranges
				double rangeMin;
				double rangeMax;
				for (int i = 1; i <= rangeCount; i++) {
					key = Integer.valueOf(i);
					if (i == 1) {
						rangeMin = minValue;
						rangeMax = rangeValues.get(key);
					} else {
						rangeMin = rangeValues.get(key - 1);
						rangeMax = rangeValues.get(key);
					}
					Range range = new Range(rangeMin, rangeMax);
					rangeRanges.put(key, range);
				}
			}
		}
	}

	/**
	 * 
	 * @param params
	 * @return dataset to be used for rendering the chart
	 */
	@Override
	public Object produceDataset(Map<String,Object> params) throws DatasetProduceException {
		return dataset;
	}

	/**
	 * 
	 * @return identifier for this producer class
	 */
	@Override
	public String getProducerId() {
		return "SpeedometerDataProducer";
	}

	/**
	 * 
	 * @param params
	 * @param since
	 * @return <code>true</code> if the data for the chart has expired
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean hasExpired(Map params, java.util.Date since) {
		return true;
	}

	/**
	 * 
	 * @param ch
	 * @param params
	 */
	@Override
	public void processChart (JFreeChart chart, Map<String,String> params) {
		MeterPlot plot = (MeterPlot) chart.getPlot();

		finalizePlot(plot);

		boolean showLegend = Boolean.valueOf(params.get("showLegend"));
		if (!showLegend) {
			chart.removeLegend();
		}

		// Output to file if required
		String outputToFile = params.get("outputToFile");
		String fileName = params.get("fullFileName");
		if (StringUtils.equals(outputToFile,"pdf")) {
			PdfGraph.createPdf(chart, fileName, title, null, displayParameters);
		} else if (StringUtils.equals(outputToFile,"png")) {
			// save chart as png file
			try {
				ChartUtilities.saveChartAsPNG(new File(fileName), chart, width, height);
			} catch (IOException e) {
				logger.error("Error", e);
			}
		}
	}

	// finalize the plot including adding ranges, units description and custom formatting
	// put code in a method so that it can be used from exportgraph
	/**
	 * 
	 * @param plot
	 */
	public void finalizePlot(MeterPlot plot) {
		plot.setRange(new Range(minValue, maxValue));
		plot.setUnits(unitsDescription);

		plot.setBackgroundPaint(Color.lightGray);
		plot.setNeedlePaint(Color.darkGray);

		// set ranges
		int i;
		String description;
		Color rangeColor;
		for (i = 1; i <= rangeCount; i++) {
			description = rangeDescriptions.get(i);
			rangeColor = Color.decode(rangeColors.get(i));
			MeterInterval interval = new MeterInterval(description, rangeRanges.get(i), rangeColor, new BasicStroke(2.0F), null);
			plot.addInterval(interval);
		}

		// set tick interval. display interval every 10 percent. by default ticks are displayed every 10 units. can be too many with large values
		double tickInterval = (maxValue - minValue) / 10.0;
		plot.setTickSize(tickInterval);
	}
}