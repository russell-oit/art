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
import art.utils.ArtUtils;
import java.awt.Color;
import net.sf.cewolfart.cpp.HeatmapEnhancer;
import net.sf.cewolfart.links.XYItemLinkGenerator;
import net.sf.cewolfart.tooltips.XYToolTipGenerator;
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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;

/**
 * Provides methods for working with xyz based charts. These include bubble
 * charts and heat maps.
 *
 * @author Timothy Anyona
 */
public class XYZBasedChart extends Chart implements XYToolTipGenerator, XYItemLinkGenerator {

	private static final long serialVersionUID = 1L;

	private final ReportType reportType;
	private Map<String, Double> actualZValues = new HashMap<>();
	private Map<String, String> heatmapOptions = new HashMap<>(); // options used by cewolf heatmap postprocessor

	public XYZBasedChart(ReportType reportType) {
		Objects.requireNonNull(reportType, "reportType must not be null");

		switch (reportType) {
			case BubbleChart:
				//set type directly instead of calling setType()
				//because setType is overridable and overridable methods shouldn't be called in constructors
				//http://www.javaworld.com/article/2074669/core-java/java-netbeans--overridable-method-call-in-constructor.html
				//https://stackoverflow.com/questions/3404301/whats-wrong-with-overridable-method-calls-in-constructors
				type = "bubble";
				break;
			case HeatmapChart:
				type = "heatmap";
				break;
			default:
				throw new IllegalArgumentException("Unsupported report type: " + reportType);
		}

		this.reportType = reportType;
		setHasTooltips(true);
	}

	@Override
	protected void fillDataset(ResultSet rs) throws SQLException {
		Objects.requireNonNull(rs, "rs must not be null");

		DefaultXYZDataset dataset = new DefaultXYZDataset();

		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();

		String seriesName = rsmd.getColumnLabel(2);

		List<Double> xValuesList = new ArrayList<>();
		List<Double> yValuesList = new ArrayList<>();
		List<Double> zValuesList = new ArrayList<>();

		int seriesIndex = 0; //zero-based series index. only one series is supported
		int itemIndex = 0; //zero-based index for a particular item within a series

		resultSetColumnNames = new ArrayList<>();
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			String columnName = rsmd.getColumnLabel(i);
			resultSetColumnNames.add(columnName);
		}

		resultSetData = new ArrayList<>();

		int recordCount = 0;
		while (rs.next()) {
			resultSetRecordCount++;

			Map<String, Object> row = new LinkedHashMap<>();
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				String columnName = rsmd.getColumnLabel(i);
				Object data = rs.getObject(i);
				row.put(columnName, data);
			}

			if (includeDataInOutput) {
				resultSetData.add(row);
			}

			recordCount++;

			prepareRow(row, resultSetColumnNames, columnCount, recordCount, xValuesList, yValuesList, zValuesList, seriesIndex, itemIndex, seriesName);

			itemIndex++;
		}

		//add series to dataset
		double[] xValuesArray = ArrayUtils.toPrimitive(xValuesList.toArray(new Double[xValuesList.size()]));
		double[] yValuesArray = ArrayUtils.toPrimitive(yValuesList.toArray(new Double[yValuesList.size()]));
		double[] zValuesArray = ArrayUtils.toPrimitive(zValuesList.toArray(new Double[zValuesList.size()]));
		double[][] data = new double[][]{xValuesArray, yValuesArray, zValuesArray};

		dataset.addSeries(seriesName, data);

		setDataset(dataset);
	}

	@Override
	protected void fillDataset(List<? extends Object> data) {
		Objects.requireNonNull(data, "data must not be null");

		DefaultXYZDataset dataset = new DefaultXYZDataset();

		String seriesName = columnNames.get(2 - 1);

		List<Double> xValuesList = new ArrayList<>();
		List<Double> yValuesList = new ArrayList<>();
		List<Double> zValuesList = new ArrayList<>();

		int seriesIndex = 0; //zero-based series index. only one series is supported
		int itemIndex = 0; //zero-based index for a particular item within a series

		int recordCount = 0;
		for (Object row : data) {
			recordCount++;

			prepareRow(row, columnNames, colCount, recordCount, xValuesList, yValuesList, zValuesList, seriesIndex, itemIndex, seriesName);

			itemIndex++;
		}

		//add series to dataset
		double[] xValuesArray = ArrayUtils.toPrimitive(xValuesList.toArray(new Double[xValuesList.size()]));
		double[] yValuesArray = ArrayUtils.toPrimitive(yValuesList.toArray(new Double[yValuesList.size()]));
		double[] zValuesArray = ArrayUtils.toPrimitive(zValuesList.toArray(new Double[zValuesList.size()]));
		double[][] seriesData = new double[][]{xValuesArray, yValuesArray, zValuesArray};

		dataset.addSeries(seriesName, seriesData);

		setDataset(dataset);
	}

	/**
	 * Prepares a row of data
	 *
	 * @param row the row of data
	 * @param dataColumnNames the data column names
	 * @param dataColumnCount the column count
	 * @param recordCount the current record index
	 * @param xValuesList the x values list
	 * @param yValuesList the y values list
	 * @param zValuesList the z values list
	 * @param seriesIndex the seris index
	 * @param itemIndex the item index
	 * @param seriesName the series name
	 */
	private void prepareRow(Object row, List<String> dataColumnNames,
			int dataColumnCount, int recordCount, List<Double> xValuesList,
			List<Double> yValuesList, List<Double> zValuesList,
			int seriesIndex, int itemIndex, String seriesName) {

		double xValue = RunReportHelper.getDoubleRowValue(row, 1, dataColumnNames);
		double yValue = RunReportHelper.getDoubleRowValue(row, 2, dataColumnNames);
		double actualZValue = RunReportHelper.getDoubleRowValue(row, 3, dataColumnNames);

		double zValue;
		if (reportType == ReportType.BubbleChart && dataColumnCount >= 4) {
			//use normalized z value to plot
			//bubble value normalized to the y axis values so that bubbles aren't too large,
			//in case z value is much larger than y value
			zValue = RunReportHelper.getDoubleRowValue(row, 4, dataColumnNames);
		} else {
			//use actual z value to plot
			zValue = actualZValue;
		}

		// set values
		xValuesList.add(xValue);
		yValuesList.add(yValue);
		zValuesList.add(zValue);

		// set heat map options
		if (reportType == ReportType.HeatmapChart && recordCount == 1) {
			for (int i = 4; i <= dataColumnCount; i++) {
				String optionSpec = RunReportHelper.getStringRowValue(row, i, dataColumnNames);
				if (optionSpec != null) {
					String[] optionDetails = StringUtils.split(optionSpec, "=");
					if (optionDetails.length == 2) {
						heatmapOptions.put(optionDetails[0], optionDetails[1]);
					}
				}
			}

			// allow specifying only the upper colour, for 2 colour schemes. set lower colour to white
			if (heatmapOptions.containsKey("upperColor") && !heatmapOptions.containsKey("lowerColor")) {
				heatmapOptions.put("lowerColor", ArtUtils.WHITE_HEX_COLOR_CODE);
			}
		}

		//use series index and item index to identify url in hashmap
		//to ensure correct link will be returned by the generatelink() method. 
		//use series index instead of name because the generateLink() method uses series indices
		String linkId = String.valueOf(seriesIndex) + String.valueOf(itemIndex);

		//add actual z values
		actualZValues.put(linkId, actualZValue);

		//add hyperlink if required
		addHyperLink(row, linkId);

		//add drilldown link if required
		//drill down on col 1 = y value
		//drill down on col 2 = x value
		//drill down on col 3 = series name
		//drill down on col 4 = actual z/bubble value
		addDrilldownLink(linkId, yValue, xValue, seriesName, actualZValue);
	}

	@Override
	public String generateToolTip(XYDataset data, int series, int item) {
		//display formatted values
		NumberFormat nf = NumberFormat.getInstance(locale);

		XYZDataset dataset = (XYZDataset) data;

		//format y value
		double yValue = dataset.getYValue(series, item);
		String formattedYValue = nf.format(yValue);

		//format x value
		double xValue = dataset.getXValue(series, item);
		String formattedXValue = nf.format(xValue);

		//format z value
		//use actual z value instead of z value in dataset
		//for bubble charts, z value in dataset may contain a normalized value
		String key = String.valueOf(series) + String.valueOf(item);
		double actualZValue = actualZValues.get(key);

		String formattedZValue = nf.format(actualZValue);

		// return final tooltip text
		return formattedXValue + ", " + formattedYValue + ", " + formattedZValue;
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
		Objects.requireNonNull(chart, "chart must not be null");

		processYAxisRange(chart);

		//set grid lines to light grey so that they are visible with a default plot background colour of white
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
		plot.setDomainGridlinePaint(Color.LIGHT_GRAY);

		if (reportType == ReportType.HeatmapChart) {
			HeatmapEnhancer heatmapPP = new HeatmapEnhancer();
			heatmapPP.processChart(chart, heatmapOptions);
		}
	}
}
