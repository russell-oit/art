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

import art.drilldown.Drilldown;
import art.enums.ReportFormat;
import art.parameter.Parameter;
import art.parameter.ParameterService;
import art.report.ChartOptions;
import art.reportparameter.ReportParameter;
import de.laures.cewolf.ChartPostProcessor;
import de.laures.cewolf.ChartValidationException;
import de.laures.cewolf.DatasetProduceException;
import de.laures.cewolf.DatasetProducer;
import de.laures.cewolf.PostProcessingException;
import de.laures.cewolf.taglib.AbstractChartDefinition;
import de.laures.cewolf.taglib.CewolfChartFactory;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.general.Dataset;
import org.jfree.ui.TextAnchor;

/**
 *
 * @author Timothy Anyona
 */
public abstract class AbstractChart extends AbstractChartDefinition implements DatasetProducer, ChartPostProcessor {

	private static final long serialVersionUID = 1L;
	private final String WHITE_HEX_COLOR_CODE = "#FFFFFF";
	private final String HYPERLINKS_COLUMN_NAME = "LINK";

	private int height = 300;
	private int width = 500;
	private String backgroundColor = WHITE_HEX_COLOR_CODE;
	private Dataset dataset;
	private Map<String, String> internalPostProcessorParams;
	private ChartOptions chartOptions;
	private Locale locale;
	private Map<String, String> hyperLinks;
	private Map<String, String> drilldownLinks;
	private Map<String, ReportParameter> reportParams;
	private Drilldown drilldown;
	private List<Parameter> drilldownParams;
	private Set<String> drilldownParamNames;
	private boolean openLinksInNewWindow;
	private boolean hasHyperLinks;

	/**
	 * @return the hasHyperLinks
	 */
	public boolean isHasHyperLinks() {
		return hasHyperLinks;
	}

	/**
	 * @return the drilldownParams
	 */
	public List<Parameter> getDrilldownParams() {
		return drilldownParams;
	}

	/**
	 * @param drilldownParams the drilldownParams to set
	 */
	public void setDrilldownParams(List<Parameter> drilldownParams) {
		this.drilldownParams = drilldownParams;
	}

	/**
	 * @return the openLinksInNewWindow
	 */
	public boolean isOpenLinksInNewWindow() {
		return openLinksInNewWindow;
	}

	/**
	 * @param openLinksInNewWindow the openLinksInNewWindow to set
	 */
	public void setOpenLinksInNewWindow(boolean openLinksInNewWindow) {
		this.openLinksInNewWindow = openLinksInNewWindow;
	}

	/**
	 * @return the drilldown
	 */
	public Drilldown getDrilldown() {
		return drilldown;
	}

	/**
	 * @param drilldown the drilldown to set
	 */
	public void setDrilldown(Drilldown drilldown) {
		this.drilldown = drilldown;
	}

	/**
	 * @return the reportParams
	 */
	public Map<String, ReportParameter> getReportParams() {
		return reportParams;
	}

	/**
	 * @param reportParams the reportParams to set
	 */
	public void setReportParams(Map<String, ReportParameter> reportParams) {
		this.reportParams = reportParams;
	}

	/**
	 * @return the hyperLinks
	 */
	public Map<String, String> getHyperLinks() {
		return hyperLinks;
	}

	/**
	 * @param hyperLinks the hyperLinks to set
	 */
	public void setHyperLinks(Map<String, String> hyperLinks) {
		this.hyperLinks = hyperLinks;
	}

	/**
	 * @return the drilldownLinks
	 */
	public Map<String, String> getDrilldownLinks() {
		return drilldownLinks;
	}

	/**
	 * @param drilldownLinks the drilldownLinks to set
	 */
	public void setDrilldownLinks(Map<String, String> drilldownLinks) {
		this.drilldownLinks = drilldownLinks;
	}

	/**
	 * @return the locale
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @param locale the locale to set
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * @return the chartOptions
	 */
	public ChartOptions getChartOptions() {
		return chartOptions;
	}

	/**
	 * @param chartOptions the chartOptions to set
	 */
	public void setChartOptions(ChartOptions chartOptions) {
		this.chartOptions = chartOptions;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the backgroundColor
	 */
	public String getBackgroundColor() {
		return backgroundColor;
	}

	/**
	 * @param backgroundColor the backgroundColor to set
	 */
	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	/**
	 * @return the internalPostProcessorParams
	 */
	public Map<String, String> getInternalPostProcessorParams() {
		return internalPostProcessorParams;
	}

	/**
	 * @param internalPostProcessorParams the internalPostProcessorParams to set
	 */
	public void setInternalPostProcessorParams(Map<String, String> internalPostProcessorParams) {
		this.internalPostProcessorParams = internalPostProcessorParams;
	}

	/**
	 * @param dataset the dataset to set
	 */
	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	@Override
	public Dataset getDataset() throws DatasetProduceException {
		return dataset;
	}

	//missing getters/setters for fields defined in AbstractChartDefinition
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return the showLegend
	 */
	public boolean isShowLegend() {
		return showLegend;
	}

	/**
	 * @return the xAxisLabel
	 */
	public String getxAxisLabel() {
		return xAxisLabel;
	}

	/**
	 * @param xAxisLabel the xAxisLabel to set
	 */
	public void setxAxisLabel(String xAxisLabel) {
		setXAxisLabel(xAxisLabel);
	}

	/**
	 * @return the yAxisLabel
	 */
	public String getyAxisLabel() {
		return yAxisLabel;
	}

	/**
	 * @param yAxisLabel the yAxisLabel to set
	 */
	public void setyAxisLabel(String yAxisLabel) {
		setYAxisLabel(yAxisLabel);
	}

	/**
	 * Produces the chart dataset based on the given resultset
	 *
	 * @param rs
	 * @throws SQLException
	 */
	protected void fillDataset(ResultSet rs) throws SQLException {
		//provide default implementation in case dataset is created in another way
		//do nothing by default. 
	}

	public void prepareDataset(ResultSet rs) throws SQLException {
		prepareDrilldown();
		prepareHyperLinks(rs);

		fillDataset(rs);
	}

	private void prepareDrilldown() throws SQLException {
		if (drilldown != null) {
			drilldownLinks = new HashMap<>();

			openLinksInNewWindow = drilldown.isOpenInNewWindow();

			ParameterService parameterService = new ParameterService();
			int drilldownReportId = drilldown.getDrilldownReport().getReportId();
			setDrilldownParams(parameterService.getDrilldownParameters(drilldownReportId));

			//store parameter names so that parent parameters with the same name
			//as in the drilldown report are omitted
			//use hashset for fast searching using contains
			//https://stackoverflow.com/questions/3307549/fastest-way-to-check-if-a-liststring-contains-a-unique-string
			drilldownParamNames = new HashSet<>();
			for (Parameter drilldownParam : getDrilldownParams()) {
				String paramName = drilldownParam.getName();
				drilldownParamNames.add(paramName);
			}
		}
	}

	private void prepareHyperLinks(ResultSet rs) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		String lastColumnName = rsmd.getColumnLabel(columnCount);

		if (StringUtils.equals(lastColumnName, HYPERLINKS_COLUMN_NAME)) {
			hasHyperLinks = true;
			setHyperLinks(new HashMap<String, String>());
		}

	}

	//returns the dataset to be used for rendering the chart
	//required for use with <cewolf:data> tag (implementing DatasetProducer interface)
	//the fillDataset() method is used to generate the dataset so this method and it's
	//parameters are not really relevant
	//separate method needed because dataset is produced from an sql resultset,
	//which can't be used as a parameter to this method because resultset isn't serializable
	//alternative is to generate a rowsetdynaclass from the resultset and pass that? what of resultset metadata?
	@Override
	public Object produceDataset(Map<String, Object> params) throws DatasetProduceException {
		//not currently using producer parameters - equivalent to the <cewolf:producer> tag
		return dataset;
	}

	//returns true if the data for the chart has expired
	@Override
	public boolean hasExpired(@SuppressWarnings("rawtypes") Map params, Date since) {
		return true;
	}

	//returns a unique identifier for the class
	//producers with the same ID are supposed to produce the same data when called with the same parameters.
	//provide default implementation as implementations currently doesn't use any parameters with the produceDataset() method
	@Override
	public String getProducerId() {
		return "AbstractDataProducer";
	}

	//performs internal post processing on the generated chart using the given parameters
	//need internal post processor in order to make changes to the chart based on
	//object state e.g. speedometer post processing needs maps that contain range information
	//<cewolf:chartpostprocessor> tag only allows passing of string parameters
	@Override
	public void processChart(JFreeChart chart, Map<String, String> params) {
		//perform default processing
		prepareYAxisRange(chart);
		prepareLabels(chart);
	}

	private void prepareYAxisRange(JFreeChart chart) {
		if (chartOptions == null) {
			return;
		}

		Plot plot = chart.getPlot();

		//set y axis range if required
		if (chartOptions.getyAxisMin() != 0 && chartOptions.getyAxisMax() != 0) {
			if (plot instanceof XYPlot) {
				XYPlot xyPlot = (XYPlot) plot;
				NumberAxis rangeAxis = (NumberAxis) xyPlot.getRangeAxis();
				rangeAxis.setRange(chartOptions.getyAxisMin(), chartOptions.getyAxisMax());

				//TODO test if needed
				//set grid lines to light grey so that they are visible with a default plot background colour of white
//			xyPlot.setRangeGridlinePaint(Color.LIGHT_GRAY);
//			xyPlot.setDomainGridlinePaint(Color.LIGHT_GRAY);
			} else if (plot instanceof CategoryPlot) {
				CategoryPlot categoryPlot = (CategoryPlot) plot;
				NumberAxis rangeAxis = (NumberAxis) categoryPlot.getRangeAxis();
				rangeAxis.setRange(chartOptions.getyAxisMin(), chartOptions.getyAxisMax());

				//set grid lines to light grey so that they are visible with a default plot background colour of white
//			xyPlot.setRangeGridlinePaint(Color.LIGHT_GRAY);
//			xyPlot.setDomainGridlinePaint(Color.LIGHT_GRAY);
			}
		}
	}

	private void prepareLabels(JFreeChart chart) {
		if (chartOptions == null) {
			return;
		}

		Plot plot = chart.getPlot();

		String labelFormat = chartOptions.getLabelFormat(); //either "off" or a format string e.g. {2}
		if (chartOptions.isShowLabels() && labelFormat != null) {
			if (plot instanceof PiePlot) {
				PiePlot piePlot = (PiePlot) plot;

				if (StringUtils.equalsIgnoreCase(labelFormat, "off")) {
					piePlot.setLabelGenerator(null);
				} else {
					piePlot.setLabelGenerator(new StandardPieSectionLabelGenerator(labelFormat));
				}
			} else if (plot instanceof CategoryPlot) {
				CategoryPlot categoryPlot = (CategoryPlot) plot;

				CategoryItemRenderer renderer = categoryPlot.getRenderer(); //could be a version of BarRenderer or LineAndShapeRenderer for line graphs
				if (StringUtils.equalsIgnoreCase(labelFormat, "off")) {
					renderer.setBaseItemLabelGenerator(null);
					renderer.setBaseItemLabelsVisible(false); //TODO need both null and false?
				} else {
					//display data values in the labels
					NumberFormat nf = NumberFormat.getInstance(locale);

					CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator(labelFormat, nf);
					renderer.setBaseItemLabelGenerator(generator);
					renderer.setBaseItemLabelsVisible(true); //TODO test if needed

					renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.TOP_CENTER));
					renderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.TOP_CENTER));
				}
			}

		}

	}

	//produces the basic jfree chart. called by getChart()
	@Override
	protected JFreeChart produceChart() throws DatasetProduceException, ChartValidationException {
		return CewolfChartFactory.getChartInstance(type, title, xAxisLabel, yAxisLabel, dataset, showLegend);
	}

	public void generateFile(ReportFormat reportFormat, String outputFileName, RowSetDynaClass data)
			throws IOException, DatasetProduceException, ChartValidationException, PostProcessingException {

		JFreeChart chart = getFinalChart();

		if (reportFormat == ReportFormat.png) {
			ChartUtilities.saveChartAsPNG(new File(outputFileName), chart, width, height);
		} else if (reportFormat == ReportFormat.pdf) {
			//TODO pdf output
		} else {
			throw new IllegalArgumentException("Unsupported report format: " + reportFormat);
		}
	}

	public JFreeChart getFinalChart() throws DatasetProduceException, ChartValidationException, PostProcessingException {
		//use cewolf to generate chart in order to achieve similar look as with interactive/browser display
		//<cewolf:chart tag doesn't allow expressions for the plotbackgroundcolor attribute
		//so use the same color/constant here as in the showChart.jsp page
//		setPlotBackgroundPaint(Color.WHITE);
		setBackgroundPaint(Color.decode(backgroundColor));

		//use cewolf AbstractChartDefinition.getChart() to generate chart
		//with additional processing like antialising and running external post processors
		//in order to achieve similar look as with interactive/browser display using <cewolf> tags
		//alternative is to duplicate the code
		JFreeChart chart = getChart();

		//run internal post processor
		processChart(chart, internalPostProcessorParams);

		return chart;
	}

	//add parameters from parent query										
	protected void addParentParameters(StringBuilder sb) {
		if (reportParams == null) {
			return;
		}

		for (Map.Entry<String, ReportParameter> entry : reportParams.entrySet()) {
			String paramName = entry.getKey();
			ReportParameter reportParam = entry.getValue();
			String[] paramValues = reportParam.getPassedParameterValues();

			//add parameter only if one with a similar name doesn't already
			//exist in the drill down parameters
			if (drilldownParamNames == null || !drilldownParamNames.contains(paramName)) {
				if (paramValues != null) {
					for (String paramValue : paramValues) {
						addUrlParameter(paramName, paramValue, sb);
					}
				}
			}
		}

	}

	protected void addUrlParameter(String paramName, String paramValue, StringBuilder sb) {
		if (paramName == null || paramValue == null || sb == null) {
			return;
		}

		try {
			String encodedParamValue = URLEncoder.encode(paramValue, "UTF-8");
			sb.append("&p-").append(paramName).append("=").append(encodedParamValue);
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}

	protected void addDrilldownBaseUrl(StringBuilder sb) {
		if (drilldown != null) {
			int drilldownReportId = drilldown.getDrilldownReport().getReportId();
			String drilldownReportFormat = drilldown.getReportFormat();
			if (drilldownReportFormat == null || drilldownReportFormat.equalsIgnoreCase("all")) {
				sb.append("showReport.do?reportId=").append(drilldownReportId);
			} else {
				sb.append("runReport.do?reportId=").append(drilldownReportId)
						.append("&reportFormat=").append(drilldownReportFormat);
			}
		}
	}

	protected void addHyperLink(ResultSet rs, String key) throws SQLException {
		if (isHasHyperLinks()) {
			String hyperLink = rs.getString(HYPERLINKS_COLUMN_NAME);
			getHyperLinks().put(key, hyperLink);
		}
	}

}
