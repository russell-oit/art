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
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.apache.commons.lang.ArrayUtils;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to render bubble charts
 *
 * @author Timothy Anyona
 */
public class ArtBubbleChart implements ArtGraph, DatasetProducer, ChartPostProcessor,
		Serializable, XYToolTipGenerator, XYItemLinkGenerator {

	private static final long serialVersionUID = 1L;
	final static Logger logger = LoggerFactory.getLogger(ArtSpeedometer.class);
	String title = "Title";
	String xlabel = "X label";
	String ylabel = "Y label";
	String seriesName = "Series";
	ArrayList<String> hyperLinks;
	HashMap<String, String> drilldownLinks;
	int height = 300;
	int width = 500;
	String bgColor = "#FFFFFF";
	boolean useHyperLinks = false;
	boolean hasDrilldown = false;
	boolean hasTooltips = true;
	double minValue;
	double maxValue;
	String openDrilldownInNewWindow;
	DefaultXYZDataset dataset = new DefaultXYZDataset();
	Map<Integer, ArtQueryParam> displayParameters = null;
	boolean showGraphData = false;
	RowSetDynaClass graphData = null; //store graph data in disconnected, serializable object
	int columnCount; //resultset can have extra column with actual z value
	ArrayList<Double> actualZValues = new ArrayList<Double>();

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setXlabel(String xlabel) {
		this.xlabel = xlabel;
	}

	@Override
	public String getXlabel() {
		return xlabel;
	}

	@Override
	public void setYlabel(String ylabel) {
		this.ylabel = ylabel;
	}

	@Override
	public String getYlabel() {
		return ylabel;
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

	@Override
	public void prepareDataset(ResultSet rs, Map<Integer, DrilldownQuery> drilldownQueries, Map<String, String> inlineParams, Map<String, String[]> multiParams) throws SQLException {

		if (useHyperLinks) {
			hyperLinks = new ArrayList<String>(100);
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
		String key;

		//store parameter names so that parent parameters with the same name as in the drilldown query are omitted
		HashMap<String, String> params = new HashMap<String, String>();
		String paramLabel;
		String paramValue;

		double x, y, z;
		double actualZ=0; //may contain actual bubble value, in case z value is much larger than y value
		ArrayList<Double> xValues = new ArrayList<Double>();
		ArrayList<Double> yValues = new ArrayList<Double>();
		ArrayList<Double> zValues = new ArrayList<Double>();
		
		columnCount=rs.getMetaData().getColumnCount();

		while (rs.next()) {
			x = rs.getDouble(1);
			y = rs.getDouble(2);
			z = rs.getDouble(3); //bubble value may be normalized to the y axis values so that bubbles aren't too large
			xValues.add(new Double(rs.getDouble(1)));
			yValues.add(new Double(rs.getDouble(2)));
			zValues.add(new Double(rs.getDouble(3)));
			
			if(columnCount>=4){
				actualZ=rs.getDouble(4);
			}
			actualZValues.add(new Double(actualZ));
			
			if (useHyperLinks) {
				hyperLinks.add(rs.getString(5)); //if use LINKs, must have 5 columns - actualZ column, then link column
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
						//drill down on col 1 = data value (y value). drill down on col 2 = category (x value)
						//drill down on col 3 = series name. (only one series is possible)
						//drill down on col 4 = bubble value (z value). drill down on col 5 = actual bubble value (actual z value)
						paramLabel = param.getParamLabel();
						paramString = "&P_" + paramLabel + "=";
						if (param.getDrilldownColumn() == 1) {
							paramString = paramString + y;
						} else if (param.getDrilldownColumn() == 2) {
							paramString = paramString + x;
						} else if (param.getDrilldownColumn() == 3) {
							paramValue = seriesName;
							try {
								paramValue = URLEncoder.encode(paramValue, "UTF-8");
							} catch (UnsupportedEncodingException e) {
								logger.warn("UTF-8 encoding not supported", e);
							}
							paramString = paramString + paramValue;
						} else if (param.getDrilldownColumn() == 4) {
							paramString = paramString + z;
						} else if (param.getDrilldownColumn() == 5) {
							paramString = paramString + actualZ;
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

				//use y data value and x data value and z data value to identify url in hashmap. to ensure correct link will be returned in generatelink. 
				key = String.valueOf(y) + String.valueOf(x) + String.valueOf(z) + String.valueOf(actualZ);
				drilldownLinks.put(key, drilldownUrl);
			}
		}

		double[] xArray = ArrayUtils.toPrimitive(xValues.toArray(new Double[0]));
		double[] yArray = ArrayUtils.toPrimitive(yValues.toArray(new Double[0]));
		double[] zArray = ArrayUtils.toPrimitive(zValues.toArray(new Double[0]));
		double[][] data = new double[][]{xArray, yArray, zArray};
		dataset.addSeries(seriesName, data);

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

	@Override
	public void prepareDataset(ResultSet rs) throws SQLException {
		prepareDataset(rs, null, null, null);
	}

	@Override
	public boolean getHasDrilldown() {
		return hasDrilldown;
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
	public void setShowGraphData(boolean value) {
		showGraphData = value;
	}

	@Override
	public boolean isShowGraphData() {
		return showGraphData;
	}

	@Override
	public RowSetDynaClass getGraphData() {
		return graphData;
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
	public Object produceDataset(Map map) throws DatasetProduceException {
		return dataset;
	}

	@Override
	public boolean hasExpired(Map map, Date date) {
		return true;
	}

	@Override
	public String getProducerId() {
		return "BubbleDataProducer";
	}

	@Override
	public void processChart(Object chart, Map params) {
		XYPlot plot = (XYPlot) ((JFreeChart) chart).getPlot();

		//allow setting of y axis range
		if (params.get("from") != null && params.get("to") != null) {
			NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
			int from = Integer.parseInt((String) params.get("from"));
			int to = Integer.parseInt((String) params.get("to"));
			rangeAxis.setRange(from, to);
		}

		//set grid lines to light grey so that they are visible with a default plot background colour of white
		plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
		plot.setDomainGridlinePaint(Color.LIGHT_GRAY);

		// Output to file if required     	  
		String outputToFile = (String) params.get("outputToFile");
		String fileName = (String) params.get("fullFileName");
		if (outputToFile.equals("pdf")) {
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

	@Override
	public String generateToolTip(XYDataset xyd, int series, int index) {
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

		//format z value
		double zValue;
		String formattedZValue;
		
		if(columnCount==3){
			//use z value column
			zValue = dataset.getZValue(series, index);
		} else {
			//use actual z value column
			zValue=actualZValues.get(index);
		}
		formattedZValue = valueFormatter.format(zValue);

		//return final tooltip text	   
		return formattedXValue + ", " + formattedYValue + ", " + formattedZValue;
	}

	@Override
	public String generateLink(Object data, int series, int item) {
		String link = "";

		if (useHyperLinks) {
			link = hyperLinks.get(item);
		} else if (hasDrilldown) {
			double y;
			double x;
			double z;
			double actualZ;
			String key;

			XYZDataset tmpDataset = (XYZDataset) data; //or use dataset variable of the class
			y = tmpDataset.getYValue(series, item);
			x = tmpDataset.getXValue(series, item);
			z = tmpDataset.getZValue(series, item);
			
			actualZ=actualZValues.get(item).intValue();

			key = String.valueOf(y) + String.valueOf(x) + String.valueOf(z) + String.valueOf(actualZ);
			link = drilldownLinks.get(key);
		}

		return link;
	}
}
