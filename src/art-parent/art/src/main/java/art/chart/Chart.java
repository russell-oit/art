/*
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
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
import art.report.ChartOptions;
import art.reportparameter.ReportParameter;
import art.utils.DrilldownLinkHelper;
import de.laures.cewolf.ChartPostProcessor;
import de.laures.cewolf.ChartValidationException;
import de.laures.cewolf.DatasetProduceException;
import de.laures.cewolf.DatasetProducer;
import de.laures.cewolf.PostProcessingException;
import de.laures.cewolf.cpp.LineRendererProcessor;
import de.laures.cewolf.cpp.RotatedAxisLabels;
import de.laures.cewolf.taglib.AbstractChartDefinition;
import de.laures.cewolf.taglib.CewolfChartFactory;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
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
	protected final String WHITE_HEX_COLOR_CODE = "#FFFFFF";
	protected final String HYPERLINKS_COLUMN_NAME = "LINK";
	private String backgroundColor = WHITE_HEX_COLOR_CODE;
	private Dataset dataset;
	private ChartOptions chartOptions;
	private Locale locale;
	private Map<String, String> hyperLinks;
	private Map<String, String> drilldownLinks;
	private boolean openLinksInNewWindow;
	private boolean hasHyperLinks;
	private boolean hasTooltips; //if true class must implement a ToolTipGenerator (otherwise showChart.jsp will fail on the <cewolf:map> tag)
	private DrilldownLinkHelper drilldownLinkHelper;
	private List<ReportParameter> reportParamsList;
	protected Map<String, String> seriesColors;

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

		this.reportParamsList = reportParamsList;

		prepareDrilldownDetails(drilldown);
		prepareHyperLinkDetails(rs);

		fillDataset(rs);
	}

	private void prepareDrilldownDetails(Drilldown drilldown) throws SQLException {
		logger.debug("Entering prepareDrilldownDetails: drilldown={}", drilldown);

		if (drilldown == null) {
			return;
		}

		drilldownLinks = new HashMap<>();
		openLinksInNewWindow = drilldown.isOpenInNewWindow();
		drilldownLinkHelper = new DrilldownLinkHelper(drilldown, reportParamsList);
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
		logger.debug("Entering processChart");

		Objects.requireNonNull(chart, "chart must not be null");

		//perform chart post processing
		processYAxisRange(chart);
		processLabels(chart);
		processXAxisLabelLines(chart);
		showPoints(chart);
		rotateLabels(chart);
		setSeriesColors(chart);
	}

	/**
	 * Performs post processing action of setting the y axis range
	 *
	 * @param chart
	 */
	protected void processYAxisRange(JFreeChart chart) {
		logger.debug("Entering processYAxisRange");

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
			} else if (plot instanceof CategoryPlot) {
				CategoryPlot categoryPlot = (CategoryPlot) plot;
				NumberAxis rangeAxis = (NumberAxis) categoryPlot.getRangeAxis();
				rangeAxis.setRange(chartOptions.getyAxisMin(), chartOptions.getyAxisMax());
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
					renderer.setBaseItemLabelsVisible(false);
				} else {
					//display data values in the labels
					NumberFormat nf = NumberFormat.getInstance(locale);

					CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator(labelFormat, nf);
					renderer.setBaseItemLabelGenerator(generator);
					renderer.setBaseItemLabelsVisible(true);

					renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_CENTER));
					renderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_CENTER));
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
		return CewolfChartFactory.getChartInstance(type, title, xAxisLabel, yAxisLabel, dataset, chartOptions.isShowLegend());
	}

	/**
	 * Generates the chart and creates a png or pdf file with the image
	 *
	 * @param reportFormat the report format. Either png or pdf
	 * @param outputFileName the full path of the file name to use
	 * @param data for pdf format, if it is required to show the chart data
	 * together with the image. Null if show data is not required.
	 * @throws IOException
	 * @throws DatasetProduceException
	 * @throws ChartValidationException
	 * @throws PostProcessingException
	 */
	public void generateFile(ReportFormat reportFormat, String outputFileName, RowSetDynaClass data)
			throws IOException, DatasetProduceException, ChartValidationException, PostProcessingException {

		logger.debug("Entering generateFile: reportFormat={}, outputFileName='{}'", reportFormat, outputFileName);

		Objects.requireNonNull(reportFormat, "reportFormat must not be null");
		Objects.requireNonNull(outputFileName, "outputFileName must not be null");

		JFreeChart chart = getFinalChart();

		switch (reportFormat) {
			case png:
				ChartUtilities.saveChartAsPNG(new File(outputFileName), chart, chartOptions.getWidth(), chartOptions.getHeight());
				break;
			case pdf:
				PdfChart.createPdf(chart, outputFileName, title, data, reportParamsList);
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
		setBackgroundPaint(Color.decode(backgroundColor));

		//use cewolf AbstractChartDefinition.getChart() to generate chart
		//with additional processing like antialising and running external post processors
		//in order to achieve similar look as with interactive/browser display using <cewolf> tags
		//alternative is to duplicate the code
		JFreeChart chart = getChart();

		//run internal post processor
		processChart(chart, null);

		return chart;
	}

	protected void addHyperLink(ResultSet rs, String key) throws SQLException {
		if (isHasHyperLinks()) {
			String hyperLink = rs.getString(HYPERLINKS_COLUMN_NAME);
			getHyperLinks().put(key, hyperLink);
		}
	}

	/**
	 * Performs post processing action of showing data points
	 *
	 * @param chart
	 */
	private void showPoints(JFreeChart chart) {
		logger.debug("Entering showPoints");

		if (chartOptions == null) {
			return;
		}

		LineRendererProcessor pointsProcessor = new LineRendererProcessor();
		Map<String, String> lineOptions = new HashMap<>();
		lineOptions.put("shapes", String.valueOf(chartOptions.isShowPoints()));
		pointsProcessor.processChart(chart, lineOptions);
	}

	/**
	 * Performs post processing action of rotating labels
	 *
	 * @param chart
	 */
	private void rotateLabels(JFreeChart chart) {
		logger.debug("Entering rotateLabels");

		if (chartOptions == null) {
			return;
		}

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
	 * Sets custom series colors if so configured
	 *
	 * @param chart the jfree chart to process
	 */
	private void setSeriesColors(JFreeChart chart) {
		logger.debug("Entering setSeriesColors");

		if (seriesColors == null || seriesColors.isEmpty()) {
			return;
		}

		ArtSeriesPaintProcessor seriesPaintProcessor = new ArtSeriesPaintProcessor();
		seriesPaintProcessor.processChart(chart, seriesColors);
	}

	/**
	 * Sets series color options as specified in the query sql
	 *
	 * @param rsmd the rsmd object for the chart resultset
	 * @throws SQLException
	 */
	protected void setSeriesColorOptions(ResultSetMetaData rsmd) throws SQLException {
		logger.debug("Entering setSeriesColorOptions");

		seriesColors = new HashMap<>();
		for (int i = 0; i < rsmd.getColumnCount(); i++) {
			int columnIndex = i + 1;
			String columnName = rsmd.getColumnLabel(columnIndex);
			if (StringUtils.startsWithIgnoreCase(columnName, "seriesColor:")) {
				String[] colorDetails = StringUtils.split(columnName, ":");
				String seriesId = colorDetails[1];
				String hexColorCode = colorDetails[2];
				seriesColors.put(seriesId, hexColorCode);
			}
		}
	}

	/**
	 * Returns <code>true</code> if the resultset column with the given name is
	 * an options definition column
	 *
	 * @param columnName the column name
	 * @return <code>true</code> if the resultset column with the given name is
	 * an options definition column
	 */
	protected boolean isOptionsColumn(String columnName) {
		if (StringUtils.startsWithIgnoreCase(columnName, "seriesColor:")) {
			return true;
		} else {
			return false;
		}
	}

}
