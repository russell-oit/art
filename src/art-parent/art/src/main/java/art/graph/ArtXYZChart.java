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
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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
import org.apache.commons.lang.StringUtils;
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
 * Class to render bubble charts and heat map charts (both use XYZDataset)
 * 
 * @author Timothy Anyona
 */
public class ArtXYZChart implements ArtGraph, DatasetProducer, ChartPostProcessor, Serializable, XYToolTipGenerator, XYItemLinkGenerator {

	private static final long serialVersionUID = 1L;
	final static Logger logger = LoggerFactory.getLogger(ArtSpeedometer.class);
	String title = "Title";
	String xAxisLabel = "X label";
	String yAxisLabel = "Y label";
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
	RowSetDynaClass graphData = null; // store graph data in disconnected, serializable object
	int columnCount; // resultset can have extra column with actual z value
	ArrayList<Double> actualZValues = new ArrayList<Double>();
	int queryType;
	Map<String, String> heatmapOptions = new HashMap<String, String>(); // options used by cewolf heatmap postprocessor

	public ArtXYZChart() {
	}

	public Map<String, String> getHeatmapOptions() {
		return heatmapOptions;
	}

	@Override
	public void setQueryType(int queryType) {
		this.queryType = queryType;
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

	@Override
	public void prepareDataset(ResultSet rs, Map<Integer, DrilldownQuery> drilldownQueries, Map<String, String> inlineParams, Map<String, String[]> multiParams) throws SQLException {

		if (useHyperLinks) {
			hyperLinks = new ArrayList<String>(10);
		}

		// add support for drill down queries
		DrilldownQuery drilldown = null;
		if (drilldownQueries != null && drilldownQueries.size() > 0) {
			hasDrilldown = true;
			drilldownLinks = new HashMap<String, String>();

			// only use the first drill down query
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
		String key;

		// store parameter names so that parent parameters with the same name as in the drilldown query are omitted
		HashMap<String, String> params = new HashMap<String, String>();
		String paramLabel;
		String paramValue;

		double x, y, z;
		double actualZ; // may contain real world data bubble/z value, in case z value is much larger than y value
		ArrayList<Double> xValues = new ArrayList<Double>();
		ArrayList<Double> yValues = new ArrayList<Double>();
		ArrayList<Double> zValues = new ArrayList<Double>();

		ResultSetMetaData rsmd = rs.getMetaData();
		columnCount = rsmd.getColumnCount();
		int rowCount = 0;

		while (rs.next()) {
			rowCount++;

			x = rs.getDouble(1);
			y = rs.getDouble(2);
			z = rs.getDouble(3);
			actualZ = z;

			if (queryType == -11) {
				// bubble chart
				if (columnCount >= 4) {
					z = rs.getDouble(4); // bubble value may be normalized to the y axis values so that bubbles aren't too large
				}

				if (useHyperLinks) {
					hyperLinks.add(rs.getString(5)); // if use LINKs, must have 5 columns - actualZ column, then link column
				}
			}

			// set values
			xValues.add(Double.valueOf(x));
			yValues.add(Double.valueOf(y));
			zValues.add(Double.valueOf(z));
			actualZValues.add(Double.valueOf(actualZ));

			// set heat map options
			if (queryType == -12) {
				if (rowCount == 1) {
					for (int i = 4; i <= columnCount; i++) {
						String optionSpec = rs.getString(i);
						if (optionSpec != null) {
							String[] optionDetails = StringUtils.split(optionSpec, "=");
							if (optionDetails.length == 2) {
								heatmapOptions.put(optionDetails[0], optionDetails[1]);
							}
						}
					}

					// allow specifying only the upper colour, for 2 colour schemes. set lower colour to white
					if (heatmapOptions.containsKey("upperColor") && !heatmapOptions.containsKey("lowerColor")) {
						heatmapOptions.put("lowerColor", "#FFFFFF");
					}
				}
			}

			// set drill down hyperlinks
			StringBuilder sb = new StringBuilder(200);
			if (drilldown != null) {
				drilldownQueryId = drilldown.getDrilldownQueryId();
				outputFormat = drilldown.getOutputFormat();
				if (outputFormat == null || outputFormat.toUpperCase().equals("ALL")) {
					sb.append("showParams.jsp?queryId=").append(drilldownQueryId);
				} else {
					sb.append("ExecuteQuery?queryId=").append(drilldownQueryId).append("&viewMode=").append(outputFormat);
				}

				drilldownParams = drilldown.getDrilldownParams();
				if (drilldownParams != null) {
					for (ArtQueryParam param : drilldownParams) {
						// drill down on col 1 = data value (y value). drill down on col 2 = category (x value)
						// drill down on col 3 = series name. (only one series is possible)
						// drill down on col 4 = actual bubble value (actual z value)
						paramLabel = param.getParamLabel();
						if (param.getDrilldownColumn() == 1) {
							paramValue = String.valueOf(y);
						} else if (param.getDrilldownColumn() == 2) {
							paramValue = String.valueOf(x);
						} else if (param.getDrilldownColumn() == 3) {
							paramValue = seriesName;
							if (paramValue != null) {
								try {
									paramValue = URLEncoder.encode(paramValue, "UTF-8");
								} catch (Exception e) {
									logger.warn("Error while encoding. Parameter={}, Value={}", new Object[] { paramLabel, paramValue, e });
								}
							}
						} else {
							paramValue = String.valueOf(actualZ);
						}
						sb.append("&P_").append(paramLabel).append("=").append(paramValue);
						params.put(paramLabel, paramLabel);
					}
				}

				// add parameters from parent query
				if (inlineParams != null) {
					for (Map.Entry<String, String> entry : inlineParams.entrySet()) {
						paramLabel = entry.getKey();
						paramValue = entry.getValue();
						// add parameter only if one with a similar name doesn't already exist in the drill down parameters
						if (!params.containsKey(paramLabel)) {
							if (paramValue != null) {
								try {
									paramValue = URLEncoder.encode(paramValue, "UTF-8");
								} catch (Exception e) {
									logger.warn("Error while encoding. Parameter={}, Value={}", new Object[] { paramLabel, paramValue, e });
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
								} catch (Exception e) {
									logger.warn("Error while encoding. Parameter={}, Value={}", new Object[] { paramLabel, param, e });
								}
							}
							sb.append("&M_").append(paramLabel).append("=").append(param);
						}
					}
				}

				drilldownUrl = sb.toString();
				// use y data value and x data value and z data value to identify url in hashmap. to ensure correct link will be returned in generatelink.
				key = String.valueOf(y) + String.valueOf(x) + String.valueOf(actualZ);
				drilldownLinks.put(key, drilldownUrl);
			}
		}

		double[] xArray = ArrayUtils.toPrimitive(xValues.toArray(new Double[xValues.size()]));
		double[] yArray = ArrayUtils.toPrimitive(yValues.toArray(new Double[yValues.size()]));
		double[] zArray = ArrayUtils.toPrimitive(zValues.toArray(new Double[zValues.size()]));
		double[][] data = new double[][] { xArray, yArray, zArray };
		dataset.addSeries(seriesName, data);

		// store data for potential use in pdf output
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
		return "XYZDataProducer";
	}

	@Override
	public void processChart(Object chart, Map params) {
		XYPlot plot = (XYPlot) ((JFreeChart) chart).getPlot();

		// set y axis range if required
		if (params.get("from") != null && params.get("to") != null) {
			Double from = (Double) params.get("from");
			Double to = (Double) params.get("to");
			NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
			rangeAxis.setRange(from, to);
		}

		// set grid lines to light grey so that they are visible with a default plot background colour of white
		plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
		plot.setDomainGridlinePaint(Color.LIGHT_GRAY);

		// Output to file if required
		String outputToFile = (String) params.get("outputToFile");
		String fileName = (String) params.get("fullFileName");
		if (outputToFile.equals("pdf")) {
			PdfGraph.createPdf(chart, fileName, title, graphData, displayParameters);
		} else if (outputToFile.equals("png")) {
			// save chart as png file
			try {
				ChartUtilities.saveChartAsPNG(new File(fileName), (JFreeChart) chart, width, height);
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}
	}

	@Override
	public String generateToolTip(XYDataset xyd, int series, int index) {
		// display formatted values

		// format x value
		double xValue;
		DecimalFormat valueFormatter;
		String formattedXValue;

		// get data value to be used as tooltip
		xValue = dataset.getXValue(series, index);

		// format value. use numberformat factory method to set formatting according to the default locale
		NumberFormat nf = NumberFormat.getInstance();
		valueFormatter = (DecimalFormat) nf;

		formattedXValue = valueFormatter.format(xValue);

		// format y value
		double yValue;
		String formattedYValue;
		yValue = dataset.getYValue(series, index);
		formattedYValue = valueFormatter.format(yValue);

		// format z value
		double zValue;
		String formattedZValue;

		zValue = dataset.getZValue(series, index);

		if (queryType == -11) {
			// bubble chart
			if (columnCount >= 4) {
				// use actual z value (z value in dataset contains a normalised value)
				zValue = actualZValues.get(index);
			}
		}
		formattedZValue = valueFormatter.format(zValue);

		// return final tooltip text
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

			XYZDataset tmpDataset = (XYZDataset) data; // or use dataset variable of the class
			y = tmpDataset.getYValue(series, item);
			x = tmpDataset.getXValue(series, item);
			z = tmpDataset.getZValue(series, item);
			actualZ = z;

			if (queryType == -11) {
				// bubble chart
				actualZ = actualZValues.get(item).intValue();
			}

			key = String.valueOf(y) + String.valueOf(x) + String.valueOf(actualZ);
			link = drilldownLinks.get(key);
		}

		return link;
	}
}
