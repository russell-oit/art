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
import de.laures.cewolf.links.CategoryItemLinkGenerator;
import de.laures.cewolf.tooltips.CategoryToolTipGenerator;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.apache.commons.lang.StringUtils;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.TextAnchor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <b>ArtCategorySeries</b> is used to chart (line or bar) over a pool of
 * categories. <br> See <i>ArtGraph</i> interface API for description and usage
 * example. In the x-axis are the categories, while the y-axis stores the
 * values. <br> <i>artCategories</i> supports multiple series and PostProcessor.
 * <br> There are two supported <i> resultSet</i> <br><b>Static Series</b> <ol>
 * <li>the first column must be a character datatype (String). The value
 * represent the category name. </li> <li>(optional) if setUseHyperLinks(true)
 * is used, the second column must be a String (an hyperlink) named "LINK" </li>
 * <li>following colums must be numeric, the column names will appear as the
 * series names. </li> </ol> <i>ResultSet Example:</i><br>
 * <code>select CATEGORY [, HyperLink LINK] , NUMBER1 [, NUMBER2 ...] from
 * ...</code>
 *
 * <br><b>Dynamic Series</b> <ol> <li>the first column must be a character
 * datatype (String). The value represent the category name. </li>
 * <li>(optional) if setUseHyperLinks(true) is used, the second column must be a
 * String (an hyperlink) named "LINK" </li> <li>the following colum is the
 * series name </li> <li>the last column the series name value </li> </ol>
 * <i>ResultSet Example:</i><br>
 * <code>select CATEGORY [, HyperLink LINK] , SERIES_NAME, NUMBER1 from
 * ...</code> <br>
 *
 * <br> <i>Note:</i><br> <ul> <li>the <i>setSeriesName</i> method does nothing
 * as the series labels are set to the column names </li> </ul>
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class ArtCategorySeries implements ArtGraph, DatasetProducer, CategoryItemLinkGenerator, CategoryToolTipGenerator, ChartPostProcessor, Serializable {
	//classes implementing chartpostprocessor need to be serializable to use cewolf 1.1+

	private static final long serialVersionUID = 1L;
	final static Logger logger = LoggerFactory.getLogger(ArtCategorySeries.class);
	String title = "Title";
	String xAxisLabel = "x Label";
	String yAxisLabel = "y Label";
	String seriesName = "Not Used";
	HashMap<String, String> hyperLinks;
	int height = 300;
	int width = 500;
	String bgColor = "#FFFFFF";
	boolean useHyperLinks = false;
	boolean hasDrilldown = false;
	HashMap<String, String> drilldownLinks;
	boolean hasTooltips = true;
	String openDrilldownInNewWindow;
	DefaultCategoryDataset dataset = new DefaultCategoryDataset();
	boolean showGraphData = false;
	RowSetDynaClass graphData = null; //store graph data in disconnected, serializable object
	Map<Integer, ArtQueryParam> displayParameters = null; //to enable display of graph parameters in pdf output

	/**
	 * Constructor
	 */
	public ArtCategorySeries() {
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

	//overload used by exportgraph class. no drill down for scheduled charts
	@Override
	public void prepareDataset(ResultSet rs) throws SQLException {
		prepareDataset(rs, null, null, null);
	}

	//prepare graph data structures with query results
	@Override
	public void prepareDataset(ResultSet rs, Map<Integer, DrilldownQuery> drilldownQueries, Map<String, String> inlineParams, Map<String, String[]> multiParams) throws SQLException {
		int hop = 0;
		if (useHyperLinks) {
			hyperLinks = new HashMap<String, String>();
			hop = 1;
		}

		/* discover which resultset type we have 
		 if areSeriesOnColumn is true the resultset is expected to have as many value columns
		 as many series to plot, otherwise the series name is expected to be 
		 in the 2nd (+hop) column (the latter allows dynamic series) 
		 */
		boolean areSeriesOnColumn;
		ResultSetMetaData rsmd = rs.getMetaData();
		switch (rsmd.getColumnType(2 + hop)) {
			case Types.NUMERIC:
			case Types.DECIMAL:
			case Types.FLOAT:
			case Types.REAL:
			case Types.DOUBLE:
			case Types.INTEGER:
			case Types.TINYINT:
			case Types.SMALLINT:
			case Types.BIGINT:
				areSeriesOnColumn = true;
				break;
			default:
				areSeriesOnColumn = false;
		}

		//cater for drill down queries
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

		String drilldownUrl;
		String outputFormat;
		int drilldownQueryId;
		List<ArtQueryParam> drilldownParams;

		String category;
		double value;
		String key;

		//store parameter names so that parent parameters with the same name as in the drilldown query are omitted
		HashMap<String, String> params = new HashMap<String, String>();
		String paramLabel;
		String paramValue;

		if (areSeriesOnColumn) {
			int series = rsmd.getColumnCount() - 1 - hop;
			String[] seriesNames = new String[series];

			for (int i = 0; i < series; i++) {
				seriesNames[i] = rsmd.getColumnLabel(i + 2 + hop); // seriesName is the column header				
			}

			while (rs.next()) {
				//Category data set: addValue(value,series,category)
				category = rs.getString(1);
				for (series = 0; series < seriesNames.length; series++) {
					value = rs.getDouble(series + hop + 2);
					dataset.addValue(value, seriesNames[series], category);

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
								//drill down on col 1 = drill on data value. drill down on col 2 = category name. drill down on col 3 = series name

								paramLabel = param.getParamLabel();
								if (param.getDrilldownColumn() == 1) {
									paramValue = String.valueOf(value);
								} else if (param.getDrilldownColumn() == 2) {
									paramValue = category;
									if (paramValue != null) {
										try {
											paramValue = URLEncoder.encode(paramValue, "UTF-8");
										} catch (UnsupportedEncodingException e) {
											logger.warn("Error while encoding. Parameter={}, Value={}", new Object[]{paramLabel, paramValue, e});
										}
									}
								} else {
									paramValue = seriesNames[series];
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
						//unique item to identify data in hashmap will be combination of category and series
						key = category + String.valueOf(series);
						drilldownLinks.put(key, drilldownUrl);
					}
				}

				if (hyperLinks != null) {
					hyperLinks.put(category, rs.getString("LINK"));
				}
			}
		} else {
			String tmpSeriesName;

			int series = 0; // counter. number of available series is dynamic. 
			int seriesId; //actual series number assigned. starts from 0. a new series number is assigned only for newly encountered series names
			HashMap<String, Integer> seriesList = new HashMap<String, Integer>(); // stores series name and id

			while (rs.next()) {
				category = rs.getString(1);
				tmpSeriesName = rs.getString(2 + hop); // series name is in the 2nd column value
				// insert value	
				value = rs.getDouble(hop + 3);
				dataset.addValue(value, tmpSeriesName, category);

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
							//drill down on col 1 = drill on data value. drill down on col 2 = category name. drill down on col 3 = series name							
							paramLabel = param.getParamLabel();
							if (param.getDrilldownColumn() == 1) {
								paramValue = String.valueOf(value);
							} else if (param.getDrilldownColumn() == 2) {
								paramValue = category;
								if (paramValue != null) {
									try {
										paramValue = URLEncoder.encode(paramValue, "UTF-8");
									} catch (UnsupportedEncodingException e) {
										logger.warn("Error while encoding. Parameter={}, Value={}", new Object[]{paramLabel, paramValue, e});
									}
								}
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

					//unique item to identify data in hashmap will be combination of category and series						
					if (seriesList.containsKey(tmpSeriesName)) {
						// series name has been encountered before. get the series id assigned
						seriesId = (seriesList.get(tmpSeriesName)).intValue();
					} else {
						// new series .
						seriesId = series;
						seriesList.put(tmpSeriesName, Integer.valueOf(seriesId)); // map series name to array id
						series++;
					}

					drilldownUrl = sb.toString();
					key = category + String.valueOf(seriesId);
					drilldownLinks.put(key, drilldownUrl);
				}

				if (hyperLinks != null) {
					hyperLinks.put(category, rs.getString("LINK"));
				}
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
		return "CategoryDataProducer";
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
	 * @param dataset
	 * @param series
	 * @param category
	 * @return url of clickable link
	 */
	@Override
	public String generateLink(Object dataset, int series, Object category) {
		String link = "";
		String key;

		if (hyperLinks != null) {
			link = hyperLinks.get(category.toString());
		} else if (drilldownLinks != null) {
			key = category.toString() + String.valueOf(series);
			link = drilldownLinks.get(key);
		}

		return link;
	}

	/**
	 *
	 * @param dataset
	 * @param series
	 * @param index
	 * @return tooltip text
	 */
	@Override
	public String generateToolTip(CategoryDataset dataset, int series, int index) {
		double dataValue;
		DecimalFormat valueFormatter;
		String formattedValue;

		//get data value to be used as tooltip
		dataValue = dataset.getValue(series, index).doubleValue();

		//format value. use numberformat factory method so that formatting is according to the default locale	   		
		NumberFormat nf = NumberFormat.getInstance();
		valueFormatter = (DecimalFormat) nf;

		formattedValue = valueFormatter.format(dataValue);

		//in case one wishes to show category names
		/*
		 String mainCategory=String.valueOf(dataset.getColumnKey(index));
		 String subCategory=String.valueOf(dataset.getRowKey(series));		
		 */

		//return final tooltip text	   
		return formattedValue;
	}

	/**
	 *
	 * @param chart
	 * @param params
	 */
	@Override
	public void processChart (JFreeChart chart, Map<String,String> params) {
		CategoryPlot plot = (CategoryPlot) chart.getPlot();

		//set y axis range if required
		if (StringUtils.isNotBlank(params.get("from")) && StringUtils.isNotBlank(params.get("to"))) {
			Double from = Double.valueOf(params.get("from"));
			Double to = Double.valueOf(params.get("to"));
			NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
			rangeAxis.setRange(from, to);
		}

		//make long axis labels more readable by breaking them into 5 lines
		plot.getDomainAxis().setMaximumCategoryLabelLines(5);

		// turn on or off data labels. by default labels are not displayed
		String labelFormat = params.get("labelFormat");
		if (!StringUtils.equals(labelFormat,"off")) {
			//display labels with data values

			DecimalFormat valueFormatter;
			NumberFormat nf = NumberFormat.getInstance();
			valueFormatter = (DecimalFormat) nf;

			CategoryItemRenderer renderer = plot.getRenderer(); //could be a version of BarRenderer or LineAndShapeRenderer for line graphs
			CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator(labelFormat, valueFormatter);
			renderer.setBaseItemLabelGenerator(generator);
			renderer.setBaseItemLabelsVisible(true);

			renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.TOP_CENTER));
			renderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.TOP_CENTER));
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

		/*
		 rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
		 // set colors
		 for (int i = 0; i < params.size(); i++) {
		 String colorStr = (String) params.get(String.valueOf(i));
		 plot.getRenderer().setSeriesPaint(i, java.awt.Color.decode(colorStr));
		 }
		 */

		/*
		 <cewolf:chartpostprocessor id="dataColor">
		 <cewolf:param name="0" value='<%= "#FFFFAA" %>'/>
		 <cewolf:param name="1" value='<%= "#AAFFAA" %>'/>
		 <cewolf:param name="2" value='<%= "#FFAAFF" %>'/>
		 <cewolf:param name="3" value='<%= "#FFAAAA" %>'/>
		 </cewolf:chartpostprocessor>
        
        
		 */
	}
}
