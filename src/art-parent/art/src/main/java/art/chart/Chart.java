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

import art.drilldown.Drilldown;
import art.enums.ReportFormat;
import art.enums.ReportType;
import art.output.PdfHelper;
import art.report.ChartOptions;
import art.report.Report;
import art.reportoptions.JFreeChartOptions;
import art.reportparameter.ReportParameter;
import art.drilldown.DrilldownLinkHelper;
import art.runreport.GroovyDataDetails;
import art.runreport.ReportOptions;
import art.runreport.RunReportHelper;
import net.sf.cewolfart.ChartPostProcessor;
import net.sf.cewolfart.ChartValidationException;
import net.sf.cewolfart.DatasetProduceException;
import net.sf.cewolfart.DatasetProducer;
import net.sf.cewolfart.PostProcessingException;
import net.sf.cewolfart.cpp.LineRendererProcessor;
import net.sf.cewolfart.cpp.RotatedAxisLabels;
import net.sf.cewolfart.taglib.AbstractChartDefinition;
import net.sf.cewolfart.taglib.CewolfChartFactory;
import java.awt.Color;
import java.io.File;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import net.sf.cewolfart.cpp.SeriesPaintProcessor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
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
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.TextAnchor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides methods for working with charts
 *
 * @author Timothy Anyona
 */
public abstract class Chart extends AbstractChartDefinition implements DatasetProducer, ChartPostProcessor {

	private static final Logger logger = LoggerFactory.getLogger(Chart.class);

	private static final long serialVersionUID = 1L;
	protected final String HYPERLINKS_COLUMN_NAME = "LINK";
	private Dataset dataset;
	private ChartOptions chartOptions;
	protected Locale locale;
	private Map<String, String> hyperLinks;
	private Map<String, String> drilldownLinks;
	private boolean openLinksInNewWindow;
	private boolean hasHyperLinks;
	private boolean hasTooltips; //if true class must implement a ToolTipGenerator (otherwise showChart.jsp will fail on the <cewolf:map> tag)
	private DrilldownLinkHelper drilldownLinkHelper;
	private List<ReportParameter> reportParamsList;
	private ReportType reportType;
	private List<Chart> secondaryCharts;
	protected boolean swapAxes;
	protected JFreeChartOptions extraOptions;
	protected int rowCount;
	protected int colCount;
	protected List<String> columnNames;
	protected List<String> resultSetColumnNames;
	protected List<Map<String, Object>> resultSetData;
	protected int resultSetRecordCount;
	protected boolean includeDataInOutput;
	private Map<String, String[]> reportRequestParameters;
	private ReportOptions reportOptions;

	/**
	 * @return the reportOptions
	 */
	public ReportOptions getReportOptions() {
		return reportOptions;
	}

	/**
	 * @param reportOptions the reportOptions to set
	 */
	public void setReportOptions(ReportOptions reportOptions) {
		this.reportOptions = reportOptions;
	}

	/**
	 * @return the reportRequestParameters
	 */
	public Map<String, String[]> getReportRequestParameters() {
		return reportRequestParameters;
	}

	/**
	 * @param reportRequestParameters the reportRequestParameters to set
	 */
	public void setReportRequestParameters(Map<String, String[]> reportRequestParameters) {
		this.reportRequestParameters = reportRequestParameters;
	}

	/**
	 * @return the includeDataInOutput
	 */
	public boolean isIncludeDataInOutput() {
		return includeDataInOutput;
	}

	/**
	 * @param includeDataInOutput the includeDataInOutput to set
	 */
	public void setIncludeDataInOutput(boolean includeDataInOutput) {
		this.includeDataInOutput = includeDataInOutput;
	}

	/**
	 * @return the resultSetRecordCount
	 */
	public int getResultSetRecordCount() {
		return resultSetRecordCount;
	}

	/**
	 * @param resultSetRecordCount the resultSetRecordCount to set
	 */
	public void setResultSetRecordCount(int resultSetRecordCount) {
		this.resultSetRecordCount = resultSetRecordCount;
	}

	/**
	 * @return the resultSetColumnNames
	 */
	public List<String> getResultSetColumnNames() {
		return resultSetColumnNames;
	}

	/**
	 * @param resultSetColumnNames the resultSetColumnNames to set
	 */
	public void setResultSetColumnNames(List<String> resultSetColumnNames) {
		this.resultSetColumnNames = resultSetColumnNames;
	}

	/**
	 * @return the resultSetData
	 */
	public List<Map<String, Object>> getResultSetData() {
		return resultSetData;
	}

	/**
	 * @param resultSetData the resultSetData to set
	 */
	public void setResultSetData(List<Map<String, Object>> resultSetData) {
		this.resultSetData = resultSetData;
	}

	/**
	 * @return the extraOptions
	 */
	public JFreeChartOptions getExtraOptions() {
		return extraOptions;
	}

	/**
	 * @param extraOptions the extraOptions to set
	 */
	public void setExtraOptions(JFreeChartOptions extraOptions) {
		this.extraOptions = extraOptions;
	}

	/**
	 * @return the swapAxes
	 */
	public boolean isSwapAxes() {
		return swapAxes;
	}

	/**
	 * @param swapAxes the swapAxes to set
	 */
	public void setSwapAxes(boolean swapAxes) {
		this.swapAxes = swapAxes;
	}

	/**
	 * @return the reportType
	 */
	public ReportType getReportType() {
		return reportType;
	}

	/**
	 * @param reportType the reportType to set
	 */
	public void setReportType(ReportType reportType) {
		this.reportType = reportType;
	}

	/**
	 * @return the secondaryCharts
	 */
	public List<Chart> getSecondaryCharts() {
		return secondaryCharts;
	}

	/**
	 * @param secondaryCharts the secondaryCharts to set
	 */
	public void setSecondaryCharts(List<Chart> secondaryCharts) {
		this.secondaryCharts = secondaryCharts;
	}

	/**
	 * @return the hasTooltips
	 */
	public boolean isHasTooltips() {
		return hasTooltips;
	}

	/**
	 * @param hasTooltips the hasTooltips to set
	 */
	public void setHasTooltips(boolean hasTooltips) {
		this.hasTooltips = hasTooltips;
	}

	/**
	 * @param hasHyperLinks the hasHyperLinks to set
	 */
	public void setHasHyperLinks(boolean hasHyperLinks) {
		this.hasHyperLinks = hasHyperLinks;
	}

	/**
	 * @return the hasHyperLinks
	 */
	public boolean isHasHyperLinks() {
		return hasHyperLinks;
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
	 * Generates the chart dataset based on the given resultset
	 *
	 * @param rs the resultset to use
	 * @throws SQLException
	 */
	protected abstract void fillDataset(ResultSet rs) throws SQLException;

	/**
	 * Generates the chart dataset based on the given data
	 *
	 * @param data the data to use
	 */
	protected abstract void fillDataset(List<? extends Object> data);

	/**
	 * Generates the chart dataset based on the given resultset
	 *
	 * @param rs the resultset to use
	 * @param drilldown the drilldown to use, if any
	 * @param reportParamsList the report parameters to use
	 * @throws SQLException
	 */
	public void prepareDataset(ResultSet rs, Drilldown drilldown,
			List<ReportParameter> reportParamsList) throws SQLException {

		logger.debug("Entering prepareDataset");

		Objects.requireNonNull(rs, "rs must not be null");

		this.reportParamsList = reportParamsList;

		prepareDrilldownDetails(drilldown);
		prepareHyperLinkDetails(rs);

		fillDataset(rs);
	}

	/**
	 * Generates the chart dataset based on the given data
	 *
	 * @param data the data to use
	 * @param drilldown the drilldown to use, if any
	 * @param reportParamsList the report parameters to use
	 * @throws Exception
	 */
	public void prepareDataset(Object data, Drilldown drilldown,
			List<ReportParameter> reportParamsList) throws Exception {

		logger.debug("Entering prepareDataset");

		Objects.requireNonNull(data, "data must not be null");

		this.reportParamsList = reportParamsList;

		GroovyDataDetails dataDetails = RunReportHelper.getGroovyDataDetails(data);
		rowCount = dataDetails.getRowCount();
		colCount = dataDetails.getColCount();
		columnNames = dataDetails.getColumnNames();
		List<? extends Object> dataList = dataDetails.getDataList();

		prepareDrilldownDetails(drilldown);
		prepareDataHyperLinkDetails();

		fillDataset(dataList);
	}

	private void prepareDrilldownDetails(Drilldown drilldown) throws SQLException {
		logger.debug("Entering prepareDrilldownDetails: drilldown={}", drilldown);

		if (drilldown == null) {
			return;
		}

		drilldownLinks = new HashMap<>();
		openLinksInNewWindow = drilldown.isOpenInNewWindow();
		drilldownLinkHelper = new DrilldownLinkHelper(drilldown, locale, reportRequestParameters);
	}

	private void prepareHyperLinkDetails(ResultSet rs) throws SQLException {
		logger.debug("Entering prepareHyperLinkDetails");

		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		String lastColumnName = rsmd.getColumnLabel(columnCount);
		String secondColumnName = rsmd.getColumnLabel(2);

		if (StringUtils.equals(lastColumnName, HYPERLINKS_COLUMN_NAME)
				|| StringUtils.equals(secondColumnName, HYPERLINKS_COLUMN_NAME)) {
			setHasHyperLinks(true);
			setHyperLinks(new HashMap<>());
		}

	}

	private void prepareDataHyperLinkDetails() throws SQLException {
		logger.debug("Entering prepareDataHyperLinkDetails");

		String lastColumnName = columnNames.get(colCount - 1);
		String secondColumnName = columnNames.get(1);

		if (StringUtils.equals(lastColumnName, HYPERLINKS_COLUMN_NAME)
				|| StringUtils.equals(secondColumnName, HYPERLINKS_COLUMN_NAME)) {
			setHasHyperLinks(true);
			setHyperLinks(new HashMap<>());
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
	public boolean hasExpired(Map<String, Object> params, Date since) {
		//https://sourceforge.net/p/cewolf/discussion/192228/thread/5cc8447e/
		//https://coderanch.com/t/500782/OutOfMemory-CeWolf-JFreeChart
		//https://coderanch.com/t/546785/Storage-Method-cewolf
		return false;
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
		logger.debug("Entering processChart");

		Objects.requireNonNull(chart, "chart must not be null");

		postProcessChart(chart);
	}

	/**
	 * Perform additional post processing on the chart
	 *
	 * @param chart the jfree chart
	 */
	protected void postProcessChart(JFreeChart chart) {
		logger.debug("Entering postProcessChart");

		processYAxisRange(chart);
		processLabels(chart);
		processXAxisLabelLines(chart);
		showPoints(chart);
		rotateLabels(chart);
		applySeriesColors(chart);
		addSecondaryCharts(chart);
	}

	/**
	 * Performs post processing action of setting the y axis range
	 *
	 * @param chart
	 */
	protected void processYAxisRange(JFreeChart chart) {
		logger.debug("Entering processYAxisRange");

		Plot plot = chart.getPlot();

		//set y axis range if required
		if (chartOptions.getyAxisMin() != 0 || chartOptions.getyAxisMax() != 0) {
			if (plot instanceof XYPlot) {
				XYPlot xyPlot = (XYPlot) plot;
				xyPlot.getRangeAxis().setRange(chartOptions.getyAxisMin(), chartOptions.getyAxisMax());
			} else if (plot instanceof CategoryPlot) {
				CategoryPlot categoryPlot = (CategoryPlot) plot;
				categoryPlot.getRangeAxis().setRange(chartOptions.getyAxisMin(), chartOptions.getyAxisMax());
			}
		}
	}

	/**
	 * Performs post processing action of setting maximum x axis label lines
	 *
	 * @param chart
	 */
	private void processXAxisLabelLines(JFreeChart chart) {
		logger.debug("Entering processXAxisLabelLines");

		Plot plot = chart.getPlot();

		if (plot instanceof CategoryPlot) {
			CategoryPlot categoryPlot = (CategoryPlot) plot;
			//for category based charts, make long x axis labels more readable by breaking them into separate lines
			final int MAX_LABEL_LINES = 5;
			categoryPlot.getDomainAxis().setMaximumCategoryLabelLines(MAX_LABEL_LINES);
		}
	}

	/**
	 * Performs post processing action of displaying labels
	 *
	 * @param chart
	 */
	private void processLabels(JFreeChart chart) {
		logger.debug("Entering processLabels");

		Plot plot = chart.getPlot();

		boolean showLabels = BooleanUtils.toBoolean(chartOptions.getShowLabels());
		String labelFormat = chartOptions.getLabelFormat(); //either "off" or a format string e.g. {2}

		if (plot instanceof PiePlot) {
			PiePlot piePlot = (PiePlot) plot;

			if (!showLabels || StringUtils.equalsIgnoreCase(labelFormat, "off")) {
				piePlot.setLabelGenerator(null);
			} else {
				piePlot.setLabelGenerator(new StandardPieSectionLabelGenerator(labelFormat));
			}
		} else if (plot instanceof CategoryPlot) {
			CategoryPlot categoryPlot = (CategoryPlot) plot;

			CategoryItemRenderer renderer = categoryPlot.getRenderer(); //could be a version of BarRenderer or LineAndShapeRenderer for line graphs
			if (!showLabels || StringUtils.equalsIgnoreCase(labelFormat, "off")) {
				renderer.setBaseItemLabelGenerator(null);
				renderer.setBaseItemLabelsVisible(false);
			} else {
				//display data values in the labels
				NumberFormat nf = NumberFormat.getInstance(locale);

				CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator(labelFormat, nf);
				renderer.setBaseItemLabelGenerator(generator);
				renderer.setBaseItemLabelsVisible(true);

				//https://stackoverflow.com/questions/23665260/bar-chart-with-exact-value-printed-on-top-of-each-bar
				//http://www.jfree.org/jfreechart/api/javadoc/org/jfree/chart/labels/ItemLabelAnchor.html
				//https://stackoverflow.com/questions/18456048/jfreechart-margin
				//http://www.jfree.org/jfreechart/api/javadoc/org/jfree/chart/axis/ValueAxis.html#setUpperMargin-double-
				//https://stackoverflow.com/questions/23864802/add-margin-and-chart-header-alignment-in-jfree-charts
				//http://www.jfree.org/jfreechart/api/javadoc/org/jfree/chart/JFreeChart.html#setPadding-org.jfree.chart.ui.RectangleInsets-
				//https://stackoverflow.com/questions/16386295/margin-plot-and-chart-in-jfreechart
				//https://stackoverflow.com/questions/22299659/how-can-i-make-a-jfreechart-bar-charts-plot-expand-to-show-vertical-labels-abov
				//https://sourceforge.net/p/jfreechart/bugs/695/
				//https://stackoverflow.com/questions/16091206/jfreechart-last-x-axis-label-cut-off
				//http://jfree.org/forum/viewtopic.php?t=22177
				//http://www.jfree.org/forum/viewtopic.php?t=23485
				//https://community.jaspersoft.com/questions/537007/labels-truncated-chart
				renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_CENTER));
				renderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_CENTER));

				if (reportType.isVerticalBar2DChart()) {
					double upperMargin = extraOptions.getUpperMargin();
					if (upperMargin > 0 && upperMargin < 100) {
						categoryPlot.getRangeAxis().setUpperMargin(upperMargin);
					}
				}
			}
		}
	}

	/**
	 * Produces the basic jfree chart, without any post processing. Called by
	 * getChart()
	 *
	 * @return the basic chart
	 * @throws DatasetProduceException
	 * @throws ChartValidationException
	 */
	@Override
	protected JFreeChart produceChart() throws DatasetProduceException, ChartValidationException {
		return CewolfChartFactory.getChartInstance(type, title, xAxisLabel, yAxisLabel, dataset, BooleanUtils.toBoolean(chartOptions.getShowLegend()));
	}

	/**
	 * Generates the chart and creates a png or pdf file with the image
	 *
	 * @param reportFormat the report format. Either png or pdf
	 * @param outputFileName the full path of the file name to use
	 * @param report the report for the chart
	 * @param pdfPageNumbers whether page numbers should be included in pdf
	 * output
	 * @param dynamicOpenPassword dynamic open password, for pdf output
	 * @param dynamicModifyPassword dynamic modify password, for pdf output
	 * @param groovyData the groovy data to be displayed with the chart. Null if
	 * show data is not required.
	 * @param showResultSetData whether resultset data should be displayed with
	 * the chart
	 * @throws Exception
	 */
	public void generateFile(ReportFormat reportFormat, String outputFileName,
			Report report, boolean pdfPageNumbers,
			String dynamicOpenPassword, String dynamicModifyPassword,
			Object groovyData, boolean showResultSetData) throws Exception {

		logger.debug("Entering generateFile: reportFormat={}, outputFileName='{}', "
				+ "report={}, pdfPageNumbers={}, showResultSetData={},",
				reportFormat, outputFileName, report, pdfPageNumbers, showResultSetData);

		Objects.requireNonNull(reportFormat, "reportFormat must not be null");
		Objects.requireNonNull(outputFileName, "outputFileName must not be null");

		JFreeChart chart = getFinalChart();

		switch (reportFormat) {
			case png:
				ChartUtilities.saveChartAsPNG(new File(outputFileName), chart, chartOptions.getWidth(), chartOptions.getHeight());
				break;
			case pdf:
				List<String> outputColumnNames = null;
				List<Map<String, Object>> outputData = null;
				if (showResultSetData) {
					outputColumnNames = resultSetColumnNames;
					outputData = resultSetData;
				}
				List<ReportParameter> finalParamsList = null;
				if (reportOptions.isShowSelectedParameters()) {
					finalParamsList = reportParamsList;
				}
				PdfChart.generatePdf(chart, outputFileName, title, finalParamsList, report, pdfPageNumbers, groovyData, outputColumnNames, outputData);
				PdfHelper pdfHelper = new PdfHelper();
				pdfHelper.addProtections(report, outputFileName, dynamicOpenPassword, dynamicModifyPassword);
				break;
			default:
				throw new IllegalArgumentException("Unsupported report format: " + reportFormat);
		}
	}

	/**
	 * Returns the final chart object after all processing is complete
	 *
	 * @return the final chart object
	 * @throws DatasetProduceException
	 * @throws ChartValidationException
	 * @throws PostProcessingException
	 */
	public JFreeChart getFinalChart() throws DatasetProduceException, ChartValidationException, PostProcessingException {
		logger.debug("Entering getFinalChart");

		//use cewolf to generate chart in order to achieve similar look as with interactive/browser display
		setBackgroundPaint(Color.decode(chartOptions.getBackgroundColor()));

		//use cewolf AbstractChartDefinition.getChart() to generate chart
		//with additional processing like antialising and running external post processors
		//in order to achieve similar look as with interactive/browser display using <cewolf> tags
		//alternative is to duplicate the code
		showLegend = BooleanUtils.toBoolean(chartOptions.getShowLegend());
		JFreeChart chart = getChart();

		//run internal post processor
		processChart(chart, null);

		return chart;
	}

	protected void addHyperLink(ResultSet rs, String key) throws SQLException {
		if (hasHyperLinks) {
			String hyperLink = rs.getString(HYPERLINKS_COLUMN_NAME);
			hyperLinks.put(key, hyperLink);
		}
	}

	protected void addHyperLink(Object row, String key) {
		if (hasHyperLinks) {
			String hyperLink = RunReportHelper.getStringRowValue(row, HYPERLINKS_COLUMN_NAME);
			hyperLinks.put(key, hyperLink);
		}
	}

	/**
	 * Performs post processing action of showing data points
	 *
	 * @param chart
	 */
	private void showPoints(JFreeChart chart) {
		logger.debug("Entering showPoints");

		LineRendererProcessor pointsProcessor = new LineRendererProcessor();
		Map<String, String> lineOptions = new HashMap<>();
		lineOptions.put("shapes", String.valueOf(BooleanUtils.toBoolean(chartOptions.getShowPoints())));
		pointsProcessor.processChart(chart, lineOptions);
	}

	/**
	 * Performs post processing action of rotating labels
	 *
	 * @param chart
	 */
	private void rotateLabels(JFreeChart chart) {
		logger.debug("Entering rotateLabels");

		//display x-axis labels vertically if too many categories present
		RotatedAxisLabels rotateProcessor = new RotatedAxisLabels();
		Map<String, String> rotateOptions = new HashMap<>();
		rotateOptions.put("rotate_at", String.valueOf(chartOptions.getRotateAt()));
		rotateOptions.put("remove_at", String.valueOf(chartOptions.getRemoveAt()));
		rotateProcessor.processChart(chart, rotateOptions);
	}

	protected void addDrilldownLink(String linkId, Object... paramValues) {
		//set drill down links
		if (drilldownLinkHelper != null) {
			String drilldownUrl = drilldownLinkHelper.getDrilldownLink(paramValues);
			getDrilldownLinks().put(linkId, drilldownUrl);
		}
	}

	/**
	 * Applies custom series colors if so configured
	 *
	 * @param chart the jfree chart to process
	 */
	private void applySeriesColors(JFreeChart chart) {
		logger.debug("Entering applySeriesColors");

		if (MapUtils.isEmpty(extraOptions.getSeriesColors())) {
			return;
		}

		SeriesPaintProcessor seriesPaintProcessor = new SeriesPaintProcessor();
		seriesPaintProcessor.processChart(chart, extraOptions.getSeriesColors());
	}

	/**
	 * Adds secondary charts, allowing for multiple axis charts
	 *
	 * @param chart the chart object of this chart (the primary chart)
	 */
	private void addSecondaryCharts(JFreeChart chart) {
		if (secondaryCharts == null || secondaryCharts.isEmpty()) {
			return;
		}

		//https://stackoverflow.com/questions/11005286/check-if-null-boolean-is-true-results-in-exception
		boolean showPoints = BooleanUtils.toBoolean(chartOptions.getShowPoints());

		int count = 0;
		for (Chart secondaryChart : secondaryCharts) {
			count++;
			Dataset secondaryDataset;
			try {
				secondaryDataset = secondaryChart.getDataset();
			} catch (DatasetProduceException ex) {
				logger.error("Error", ex);
				continue;
			}
			String secondaryYAxisLabel = secondaryChart.getyAxisLabel();
			NumberAxis axis = new NumberAxis(secondaryYAxisLabel);
			Plot plot = chart.getPlot();
			if (plot instanceof CategoryPlot) {
				CategoryPlot categoryPlot = (CategoryPlot) plot;
				categoryPlot.setRangeAxis(count, axis);
				CategoryDataset categoryDataset;
				if ((secondaryDataset instanceof CategoryDataset)) {
					categoryDataset = (CategoryDataset) secondaryDataset;
				} else {
					throw new RuntimeException("Invalid secondary chart: " + secondaryChart.getTitle());
				}
				categoryPlot.setDataset(count, categoryDataset);
				categoryPlot.mapDatasetToRangeAxis(count, count);
				CategoryItemRenderer categoryRenderer;
				ReportType secondaryReportType = secondaryChart.getReportType();
				if (secondaryReportType.isCategoryPlotChart()) {
					switch (secondaryChart.getReportType()) {
						case LineChart:
							LineAndShapeRenderer lineAndShapeRenderer = new LineAndShapeRenderer();
							lineAndShapeRenderer.setBaseShapesVisible(showPoints);
							categoryRenderer = lineAndShapeRenderer;
							break;
						default:
							categoryRenderer = new BarRenderer();
					}
				} else {
					throw new IllegalArgumentException("Invalid secondary chart: " + secondaryChart.getTitle());
				}
				categoryPlot.setRenderer(count, categoryRenderer);
			} else if (plot instanceof XYPlot) {
				XYPlot xyPlot = (XYPlot) plot;
				xyPlot.setRangeAxis(count, axis);
				XYDataset xyDateset;
				if ((secondaryDataset instanceof XYDataset)) {
					xyDateset = (XYDataset) secondaryDataset;
				} else {
					throw new RuntimeException("Invalid secondary chart: " + secondaryChart.getTitle());
				}
				xyPlot.setDataset(count, xyDateset);
				xyPlot.mapDatasetToRangeAxis(count, count);
				AbstractXYItemRenderer xyRenderer;
				ReportType secondaryReportType = secondaryChart.getReportType();
				if (secondaryReportType.isXYPlotChart()) {
					StandardXYItemRenderer standardXYItemRenderer = new StandardXYItemRenderer();
					standardXYItemRenderer.setBaseShapesVisible(showPoints);
					xyRenderer = standardXYItemRenderer;
				} else {
					throw new IllegalArgumentException("Invalid secondary chart: " + secondaryChart.getTitle());
				}
				xyPlot.setRenderer(count, xyRenderer);
			}
		}
	}

}
