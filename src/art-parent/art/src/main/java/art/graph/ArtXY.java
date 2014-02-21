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
import de.laures.cewolf.links.XYItemLinkGenerator;
import de.laures.cewolf.tooltips.XYToolTipGenerator;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <b>ArtXY</b> is used to plot a xy chart. <br> See <i>ArtGraph</i> interface
 * API for description and usage example.
 *
 * <i>artXY</i> supports PostProcessor to focus on a Y vale range. <br> The <i>
 * resultSet</i> is expected to have the following layout: <ol> <li>the first
 * column must be a numeric datatype. The value represent the x value. </li>
 * <li>the second column must be a numeric datatype. The value represent the y
 * value. </li> <li>(optional) if setUseHyperLinks(true) is used, the third
 * column must be a String (an hyperlink). </li> </ol> <i>ResultSet
 * Example:</i><br>
 * <code>select NUMBER1 , NUMBER2 [, HyperLink]  from ...</code> <br>
 * <i>Note:</i><br> <ul> <li>the <i>setSeriesName</i> method sets the name of
 * the plotted line </li> </ul>
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class ArtXY implements ArtGraph, DatasetProducer, XYItemLinkGenerator, ChartPostProcessor,
		XYToolTipGenerator, Serializable {
	//classes implementing chartpostprocessor need to be serializable to use cewolf 1.1+

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(ArtXY.class);
	String title = "Title";
	String xAxisLabel = "x Label";
	String yAxisLabel = "y Label";
	String seriesName = "Series Name";
	ArrayList<String> hyperLinks;
	int height = 300;
	int width = 500;
	String bgColor = "#FFFFFF";
	boolean useHyperLinks = false;
	boolean hasDrilldown = false;
	HashMap<String, String> drilldownLinks;
	boolean hasTooltips = true;
	String openDrilldownInNewWindow;
	XYSeriesCollection dataset;
	boolean showGraphData = false;
	RowSetDynaClass graphData = null; //store graph data in disconnected, serializable object
	Map<Integer, ArtQueryParam> displayParameters = null; //to enable display of graph parameters in pdf output

	/**
	 * Constructor
	 */
	public ArtXY() {
	}

	@Override
	public void setQueryType(int queryType) {
		//not used
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
		return graphData;
	}

	@Override
	public void setShowGraphData(boolean value) {
		showGraphData = value;
	}

	@Override
	public boolean isShowGraphData() {
		return showGraphData;
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
	public void setYAxisLabel(String ylabel) {
		this.yAxisLabel = ylabel;
	}

	@Override
	public String getYAxisLabel() {
		return yAxisLabel;
	}

	@Override
	public void setSeriesName(String seriesName) {
		this.seriesName = seriesName;
	}

	@Override
	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void setBgColor(String bgColor) {
		this.bgColor = bgColor;
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

	/**
	 *
	 * @param dataset
	 * @param series
	 * @param index
	 * @return tooltip text
	 */
	@Override
	public String generateToolTip(XYDataset dataset, int series, int index) {
		//display formatted values

		//format x value
		double xValue;
		DecimalFormat valueFormatter;
		String formattedXValue;

		//get data value to be used as tooltip
		xValue = dataset.getXValue(series, index);

		//format value. use numberformat factory method to set formatting according to the default locale	   		
		NumberFormat nf = NumberFormat.getInstance();
		valueFormatter = (DecimalFormat) nf;

		formattedXValue = valueFormatter.format(xValue);

		//format y value
		double yValue;
		String formattedYValue;

		yValue = dataset.getYValue(series, index);

		formattedYValue = valueFormatter.format(yValue);

		//return final tooltip text	   
		return formattedXValue + ", " + formattedYValue;
	}

	//overload used by exportgraph class. no drill down for scheduled charts
	@Override
	public void prepareDataset(ResultSet rs) throws SQLException {
		prepareDataset(rs, null, null, null);
	}

	//prepare graph data structures with query results
	@Override
	public void prepareDataset(ResultSet rs, Map<Integer, DrilldownQuery> drilldownQueries, Map<String, String> inlineParams, Map<String, String[]> multiParams) throws SQLException {

		if (useHyperLinks) {
			hyperLinks = new ArrayList<String>(10);
		}

		//add support for drill down queries
		DrilldownQuery drilldown = null;
		if (drilldownQueries != null && !drilldownQueries.isEmpty()) {
			hasDrilldown = true;
			drilldownLinks = new HashMap<String, String>();

			//only use the first drill down query
			Iterator<Map.Entry<Integer, DrilldownQuery>> it = drilldownQueries.entrySet().iterator();
			if (it.hasNext()) {
				Map.Entry<Integer, DrilldownQuery> entry = it.next();
				drilldown = entry.getValue();

				openDrilldownInNewWindow = drilldown.getOpenInNewWindow();
			}
		}

		int columnCount = rs.getMetaData().getColumnCount();

		String drilldownUrl;
		String outputFormat;
		int drilldownQueryId;
		List<ArtQueryParam> drilldownParams;
		String key;

		//store parameter names so that parent parameters with the same name as in the drilldown query are omitted
		HashMap<String, String> params = new HashMap<String, String>();
		String paramLabel;
		String paramValue;
		double x, y;

		if (columnCount == 2 || (columnCount == 3 && useHyperLinks)) {
			//one series

			XYSeries xys = new XYSeries(seriesName);

			while (rs.next()) {
				x = rs.getDouble(1);
				y = rs.getDouble(2);
				xys.add(x, y);
				if (useHyperLinks) {
					hyperLinks.add(rs.getString("LINK"));
				}

				//set drill down hyperlinks
				StringBuilder sb = new StringBuilder(200);
				if (drilldown != null) {
					drilldownQueryId = drilldown.getDrilldownQueryId();
					outputFormat = drilldown.getOutputFormat();
					if (outputFormat == null || outputFormat.equalsIgnoreCase("ALL")) {
						sb.append("showParams.jsp?queryId=").append(drilldownQueryId);
					} else {
						sb.append("ExecuteQuery?queryId=").append(drilldownQueryId)
								.append("&viewMode=").append(outputFormat);
					}

					drilldownParams = drilldown.getDrilldownParams();
					if (drilldownParams != null) {
						for (ArtQueryParam param : drilldownParams) {
							//drill down on col 1 = data value (y value). drill down on col 2 = category (x value). drill down on col 3 = series name. (only one series is possible)
							paramLabel = param.getParamLabel();
							if (param.getDrilldownColumn() == 1) {
								paramValue = String.valueOf(y);
							} else if (param.getDrilldownColumn() == 2) {
								paramValue = String.valueOf(x);
							} else {
								paramValue = seriesName;
								if (paramValue != null) {
									try {
										paramValue = URLEncoder.encode(paramValue, "UTF-8");
									} catch (UnsupportedEncodingException e) {
										logger.warn("Error while encoding. Parameter={}, Value={}", new Object[]{paramLabel, paramValue, e});
									}
								}
							}
							sb.append("&P_").append(paramLabel).append("=").append(paramValue);
							params.put(paramLabel, paramLabel);
						}
					}

					//add parameters from parent query										
					if (inlineParams != null) {
						for (Map.Entry<String, String> entry : inlineParams.entrySet()) {
							paramLabel = entry.getKey();
							paramValue = entry.getValue();
							//add parameter only if one with a similar name doesn't already exist in the drill down parameters
							if (!params.containsKey(paramLabel)) {
								if (paramValue != null) {
									try {
										paramValue = URLEncoder.encode(paramValue, "UTF-8");
									} catch (UnsupportedEncodingException e) {
										logger.warn("Error while encoding. Parameter={}, Value={}", new Object[]{paramLabel, paramValue, e});
									}
								}
								sb.append("&P_").append(paramLabel).append("=").append(paramValue);
							}
						}
					}

					if (multiParams != null) {
						String[] paramValues;
						for (Map.Entry<String, String[]> entry : multiParams.entrySet()) {
							paramLabel = entry.getKey();
							paramValues = entry.getValue();
							for (String param : paramValues) {
								if (param != null) {
									try {
										param = URLEncoder.encode(param, "UTF-8");
									} catch (UnsupportedEncodingException e) {
										logger.warn("Error while encoding. Parameter={}, Value={}", new Object[]{paramLabel, param, e});
									}
								}
								sb.append("&M_").append(paramLabel).append("=").append(param);
							}
						}
					}

					drilldownUrl = sb.toString();
					//use series, y data value and x data value to identify url in hashmap. to ensure correct link will be returned in generatelink. 
					key = "1" + String.valueOf(y) + String.valueOf(x);
					drilldownLinks.put(key, drilldownUrl);
				}
			}

			dataset = new XYSeriesCollection(xys);
		} else {
			//multiple series

			int series = 0; // number of available series is dynamic
			int seriesId;
			String tmpSeriesName;

			XYSeries s[] = new XYSeries[64]; // max of 64 series,
			// this number is reasonable and
			// we do not want to scroll all the result set...

			HashMap<String, Integer> hm = new HashMap<String, Integer>(); // stores series name and id

			while (rs.next()) {
				x = rs.getDouble(1);
				y = rs.getDouble(2);
				tmpSeriesName = rs.getString(3); // series name is in the 3rd column

				//has this series already appeared?
				if (hm.containsKey(tmpSeriesName)) { // item already available
					seriesId = (hm.get(tmpSeriesName)).intValue();
				} else { // new series 
					seriesId = series;
					hm.put(tmpSeriesName, Integer.valueOf(seriesId)); // map series name to array id
					s[seriesId] = new XYSeries(tmpSeriesName); // initialize new timeseries
					//insert value
					series++;
				}

				// insert value
				s[seriesId].add(x, y);
				if (useHyperLinks) {
					hyperLinks.add(rs.getString("LINK"));
				}

				//set drill down hyperlinks
				StringBuilder sb = new StringBuilder(200);
				if (drilldown != null) {
					drilldownQueryId = drilldown.getDrilldownQueryId();
					outputFormat = drilldown.getOutputFormat();
					if (outputFormat == null || outputFormat.equalsIgnoreCase("ALL")) {
						sb.append("showParams.jsp?queryId=").append(drilldownQueryId);
					} else {
						sb.append("ExecuteQuery?queryId=").append(drilldownQueryId)
								.append("&viewMode=").append(outputFormat);
					}

					drilldownParams = drilldown.getDrilldownParams();
					if (drilldownParams != null) {
						for (ArtQueryParam param : drilldownParams) {
							//drill down on col 1 = data value (y value). drill down on col 2 = category (x value). drill down on col 3 = series name. 
							paramLabel = param.getParamLabel();
							if (param.getDrilldownColumn() == 1) {
								paramValue = String.valueOf(y);
							} else if (param.getDrilldownColumn() == 2) {
								paramValue = String.valueOf(x);
							} else {
								paramValue = tmpSeriesName;
								if (paramValue != null) {
									try {
										paramValue = URLEncoder.encode(paramValue, "UTF-8");
									} catch (UnsupportedEncodingException e) {
										logger.warn("Error while encoding. Parameter={}, Value={}", new Object[]{paramLabel, paramValue, e});
									}
								}
							}
							sb.append("&P_").append(paramLabel).append("=").append(paramValue);
							params.put(paramLabel, paramLabel);
						}
					}

					//add parameters from parent query										
					if (inlineParams != null) {
						for (Map.Entry<String, String> entry : inlineParams.entrySet()) {
							paramLabel = entry.getKey();
							paramValue = entry.getValue();
							//add parameter only if one with a similar name doesn't already exist in the drill down parameters
							if (!params.containsKey(paramLabel)) {
								if (paramValue != null) {
									try {
										paramValue = URLEncoder.encode(paramValue, "UTF-8");
									} catch (UnsupportedEncodingException e) {
										logger.warn("Error while encoding. Parameter={}, Value={}", new Object[]{paramLabel, paramValue, e});
									}
								}
								sb.append("&P_").append(paramLabel).append("=").append(paramValue);
							}
						}
					}

					if (multiParams != null) {
						String[] paramValues;
						for (Map.Entry<String, String[]> entry : multiParams.entrySet()) {
							paramLabel = entry.getKey();
							paramValues = entry.getValue();
							for (String param : paramValues) {
								if (param != null) {
									try {
										param = URLEncoder.encode(param, "UTF-8");
									} catch (UnsupportedEncodingException e) {
										logger.warn("Error while encoding. Parameter={}, Value={}", new Object[]{paramLabel, param, e});
									}
								}
								sb.append("&M_").append(paramLabel).append("=").append(param);
							}
						}
					}

					drilldownUrl = sb.toString();
					//use series, y data value and x data value to identify url in hashmap. to ensure correct link will be returned in generatelink. 
					key = String.valueOf(seriesId) + String.valueOf(y) + String.valueOf(x);
					drilldownLinks.put(key, drilldownUrl);
				}
			}

			dataset = new XYSeriesCollection();
			for (int i = 0; i < series; i++) {
				dataset.addSeries(s[i]);
			}
		}

		//store data for potential use in pdf output
		if (showGraphData) {
			int rsType = rs.getType();
			if (rsType == ResultSet.TYPE_SCROLL_INSENSITIVE || rsType == ResultSet.TYPE_SCROLL_SENSITIVE) {
				rs.beforeFirst();
			}
			graphData = new RowSetDynaClass(rs, false, true);
		} else {
			graphData = null;
		}
	}

	/**
	 *
	 * @param params
	 * @return dataset to be used for rendering the chart
	 * @throws DatasetProduceException
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
		return "XYDataProducer";
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
	 * @param data
	 * @param series
	 * @param item
	 * @return url of clickable link
	 */
	@Override
	public String generateLink(Object data, int series, int item) {
		String link = "";
		XYDataset tmpDataset;
		double yValue;
		double xValue;
		String key;

		if (hyperLinks != null) {
			link = hyperLinks.get(item);
		} else if (drilldownLinks != null) {
			tmpDataset = (XYDataset) data;
			yValue = tmpDataset.getYValue(series, item);
			xValue = tmpDataset.getXValue(series, item);

			key = String.valueOf(series) + String.valueOf(yValue) + String.valueOf(xValue);
			link = drilldownLinks.get(key);
		}

		return link;
	}

	/**
	 *
	 * @param chart
	 * @param params
	 */
	@Override
	public void processChart (JFreeChart chart, Map<String,String> params) {
		XYPlot plot = (XYPlot) chart.getPlot();

		//set y axis range if required
		if (StringUtils.isNotBlank(params.get("from")) && StringUtils.isNotBlank(params.get("to"))) {
			Double from = Double.valueOf(params.get("from"));
			Double to = Double.valueOf(params.get("to"));
			NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
			rangeAxis.setRange(from, to);
		}

		//set grid lines to light grey so that they are visible with a default plot background colour of white
		plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
		plot.setDomainGridlinePaint(Color.LIGHT_GRAY);

		// Output to file if required     	  
		String outputToFile = params.get("outputToFile");
		String fileName = params.get("fullFileName");
		if (StringUtils.equals(outputToFile,"pdf")) {
			PdfGraph.createPdf(chart, fileName, title, graphData, displayParameters);
		} else if (StringUtils.equals(outputToFile,"png")) {
			//save chart as png file									            
			try {
				ChartUtilities.saveChartAsPNG(new File(fileName), chart, width, height);
			} catch (IOException e) {
				logger.error("Error", e);
			}
		}
	}
}
