/*
 * Copyright (C)   Enrico Liboni  - enrico@computer.org
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the LGPL License as published by
 *   the Free Software Foundation;
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. *  
 */
package art.graph;

import art.utils.ArtQueryParam;
import art.utils.DrilldownQuery;
import de.laures.cewolf.ChartPostProcessor;
import de.laures.cewolf.DatasetProducer;
import de.laures.cewolf.links.PieSectionLinkGenerator;
import de.laures.cewolf.tooltips.PieToolTipGenerator;
import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <b>ArtPie</b> is used to chart a pie. <br> See <i>ArtGraph</i> interface API
 * for description and usage example. <br> The <i> resultSet</i> is expected to
 * have the following layout: <ol> <li>the first column must be a character
 * datatype (String). The value represent the category name. </li> <li>the
 * second colums must be numeric. </li> <li>(optional) if setUseHyperLinks(true)
 * is used, the 3rd column must be a String (an hyperlink). </li> </ol>
 * <i>ResultSet Example:</i><br>
 * <code>select CATEGORY , NUMBER1 [, HyperLink]  from ...</code> <br>
 * <i>Note:</i><br> <ul> <li>the <i>X/Y labels</i> make no sense here. </li>
 * </ul>
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class ArtPie implements ArtGraph, DatasetProducer, PieToolTipGenerator, ChartPostProcessor, PieSectionLinkGenerator, Serializable {
	//classes implementing chartpostprocessor need to be serializable to use cewolf 1.1+

	private static final long serialVersionUID = 1L;
	final static Logger logger = LoggerFactory.getLogger(ArtPie.class);
	String title = "Title";
	String xAxisLabel = "x Label";
	String yAxisLabel = "y Label";
	String seriesName = "Series Name";
	java.util.HashMap<String, String> hyperLinks;
	int height = 300;
	int width = 500;
	String bgColor = "#FFFFFF";
	boolean useHyperLinks = false;
	boolean hasDrilldown = false;
	HashMap<String, String> drilldownLinks;
	boolean hasTooltips = true;
	String openDrilldownInNewWindow;
	DefaultPieDataset dataset = new DefaultPieDataset();
	boolean showGraphData = false;
	RowSetDynaClass graphData = null; //store graph data in disconnected, serializable object
	Map<Integer,ArtQueryParam> displayParameters=null; //to enable display of graph parameters in pdf output

	/**
	 * Constructor
	 */
	public ArtPie() {
	}
	
	@Override
	public void setQueryType(int queryType) {
		//not used
	}
	
	@Override
	public void setDisplayParameters(Map<Integer,ArtQueryParam> value){
		displayParameters=value;
	}
	
	@Override
	public Map<Integer,ArtQueryParam> getDisplayParameters(){
		return displayParameters;
	}
	
	@Override
	public RowSetDynaClass getGraphData(){
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
	public void prepareDataset(ResultSet rs, Map<Integer, DrilldownQuery> drilldownQueries,
			Map<String, String> inlineParams, Map<String, String[]> multiParams) throws SQLException {
		String[] seriesNames = new String[1];
		seriesNames[0] = seriesName;

		ResultSetMetaData rsmd = rs.getMetaData();
		useHyperLinks = (rsmd.getColumnCount() == 3);

		if (useHyperLinks) {
			hyperLinks = new HashMap<String, String>();
		}

		//add support for drill down queries
		DrilldownQuery drilldown = null;
		if (drilldownQueries != null && drilldownQueries.size() > 0) {
			hasDrilldown = true;
			drilldownLinks = new HashMap<String, String>();

			//only use the first drill down query
			Iterator it = drilldownQueries.entrySet().iterator();
			if (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				drilldown = (DrilldownQuery) entry.getValue();

				openDrilldownInNewWindow = drilldown.getOpenInNewWindow();
			}
		}

		String drilldownUrl;
		String outputFormat;
		int drilldownQueryId;
		List<ArtQueryParam> drilldownParams;
		String paramString;
		String category;

		//store parameter names so that parent parameters with the same name as in the drilldown query are omitted
		HashMap<String, String> params = new HashMap<String, String>();
		String paramLabel;
		String paramValue;

		//there is only one series, this is for reference
		for (int series = 0; series < seriesNames.length; series++) {
			while (rs.next()) {
				// Pie data set: setValue(category, value)

				category = rs.getString(1);
				dataset.setValue(category, rs.getDouble(2));
				if (useHyperLinks) {
					hyperLinks.put(category, rs.getString(3));
				}

				//set drill down hyperlinks
				if (drilldown != null) {
					drilldownQueryId = drilldown.getDrilldownQueryId();
					outputFormat = drilldown.getOutputFormat();
					if (outputFormat == null || outputFormat.toUpperCase().equals("ALL")) {
						drilldownUrl = "showParams.jsp?queryId=" + drilldownQueryId;
					} else {
						drilldownUrl = "ExecuteQuery?queryId=" + drilldownQueryId + "&viewMode=" + outputFormat;
					}

					drilldownParams = drilldown.getDrilldownParams();
					if (drilldownParams != null) {
						Iterator it2 = drilldownParams.iterator();
						while (it2.hasNext()) {
							ArtQueryParam param = (ArtQueryParam) it2.next();
							//drill down on col 1 = drill on data value. drill down on col 2 = category name
							paramLabel = param.getParamLabel();
							paramString = "&P_" + paramLabel + "=";
							if (param.getDrilldownColumn() == 1) {
								paramString = paramString + rs.getString(2);
							} else {
								paramValue = category;
								try {
									paramValue = URLEncoder.encode(paramValue, "UTF-8");
								} catch (UnsupportedEncodingException e) {
									logger.warn("UTF-8 encoding not supported", e);
								}
								paramString = paramString + paramValue;
							}
							drilldownUrl = drilldownUrl + paramString;
							params.put(paramLabel, paramLabel);
						}
					}

					//add parameters from parent query										
					if (inlineParams != null) {
						Iterator itInline = inlineParams.entrySet().iterator();
						while (itInline.hasNext()) {
							Map.Entry entryInline = (Map.Entry) itInline.next();
							paramLabel = (String) entryInline.getKey();
							paramValue = (String) entryInline.getValue();
							//add parameter only if one with a similar name doesn't already exist in the drill down parameters
							if (!params.containsKey(paramLabel)) {
								try {
									paramValue = URLEncoder.encode(paramValue, "UTF-8");
								} catch (UnsupportedEncodingException e) {
									logger.warn("UTF-8 encoding not supported", e);
								}
								paramString = "&P_" + paramLabel + "=" + paramValue;
								drilldownUrl = drilldownUrl + paramString;
							}
						}
					}

					if (multiParams != null) {
						String[] paramValues;
						Iterator itMulti = multiParams.entrySet().iterator();
						while (itMulti.hasNext()) {
							Map.Entry entryMulti = (Map.Entry) itMulti.next();
							paramLabel = (String) entryMulti.getKey();
							paramValues = (String[]) entryMulti.getValue();
							for (String param : paramValues) {
								try {
									param = URLEncoder.encode(param, "UTF-8");
								} catch (UnsupportedEncodingException e) {
									logger.warn("UTF-8 encoding not supported", e);
								}
								paramString = "&M_" + paramLabel + "=" + param;
								drilldownUrl = drilldownUrl + paramString;
							}
						}
					}

					drilldownLinks.put(category, drilldownUrl);
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
			graphData=null;
		}
	}

	/**
	 *
	 * @param params
	 * @return dataset to be used for rendering the chart
	 */
	@Override
	public Object produceDataset(Map params) {
		return dataset;
	}

	/**
	 *
	 * @return identifier for this producer class
	 */
	@Override
	public String getProducerId() {
		return "PieDataProducer";
	}

	/**
	 *
	 * @param params
	 * @param since
	 * @return
	 * <code>true</code> if the data for the chart has expired
	 */
	@Override
	public boolean hasExpired(Map params, java.util.Date since) {
		return true;
	}

	/**
	 *
	 * @param dataset
	 * @param section
	 * @param index
	 * @return tooltip text
	 */
	@Override
	public String generateToolTip(PieDataset dataset, Comparable section, int index) {
		double dataValue;
		DecimalFormat valueFormatter;
		String formattedValue;

		//get data value to be used as tooltip
		dataValue = dataset.getValue(index).doubleValue();

		//format value. use numberformat factory method to set formatting according to the default locale	   		
		NumberFormat nf = NumberFormat.getInstance();
		valueFormatter = (DecimalFormat) nf;

		formattedValue = valueFormatter.format(dataValue);

		/*
		 * //category name and value //return String.valueOf(section) + "=" +
		 * formattedValue;
		 */

		return formattedValue;
	}

	/**
	 *
	 * @param data
	 * @param category
	 * @return url of clickable link
	 */
	@Override
	public String generateLink(Object data, Object category) {
		String link = "";

		if (useHyperLinks) {
			link = hyperLinks.get(category.toString());
		} else if (hasDrilldown) {
			link = drilldownLinks.get(category.toString());
		}

		return link;
	}

	// get the plot object and set the label / default one is too big...
	/**
	 *
	 * @param chart
	 * @param params
	 */
	@Override
	public void processChart(Object chart, Map params) {
		PiePlot3D plot = (PiePlot3D) ((JFreeChart) chart).getPlot();

		// switch off labels
		String labelFormat = (String) params.get("labelFormat");
		if (labelFormat.equals("off")) {
			plot.setLabelGenerator(null);
		} else {
			plot.setLabelGenerator(new StandardPieSectionLabelGenerator(labelFormat));
		}

		// Output to file if required     	  
		String outputToFile = (String) params.get("outputToFile");
		String fileName = (String) params.get("fullFileName");
		if (outputToFile.equals("pdf")) {
			//allow show graph data below graph and show graph parameters above graph
			//PdfGraph.createPdf(chart, fileName, title);
			PdfGraph.createPdf(chart, fileName, title, graphData, displayParameters);
		} else if (outputToFile.equals("png")) {
			//save chart as png file									            
			try {
				ChartUtilities.saveChartAsPNG(new File(fileName), (JFreeChart) chart, width, height);
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}
	}
}
