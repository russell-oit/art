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

import art.enums.ReportFormat;
import art.utils.ArtQueryParam;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.Dataset;

/**
 *
 * @author Timothy Anyona
 */
public abstract class AbstractChart extends AbstractChartDefinition implements DatasetProducer, ChartPostProcessor {

	private static final long serialVersionUID = 1L;
	private final String WHITE_HEX_COLOR_CODE = "#FFFFFF";

	private String seriesName;
	private int height = 300;
	private int width = 500;
	private String backgroundColor = WHITE_HEX_COLOR_CODE;
	private boolean hasLinks; //if true class must implement a de.laures.cewolf.links.LinkGenerator and a de.laures.cewolf.tooltips.ToolTipGenerator
	private boolean hasDrilldown; //if true class must implement a LinkGenerator and a ToolTipGenerator
	private Map<Integer, ArtQueryParam> displayParameters;
	private Dataset dataset;
	private Map<String, String> internalPostProcessorParams;
	private boolean hasTooltips; //if true class must implement a ToolTipGenerator (otherwise showChart.jsp will fail on the <cewolf:map> tag)
	private boolean openDrilldownInNewWindow = true;

	/**
	 * @return the seriesName
	 */
	public String getSeriesName() {
		return seriesName;
	}

	/**
	 * @param seriesName the seriesName to set
	 */
	public void setSeriesName(String seriesName) {
		this.seriesName = seriesName;
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
	 * @return the hasLinks
	 */
	public boolean isHasLinks() {
		return hasLinks;
	}

	/**
	 * @param hasLinks the hasLinks to set
	 */
	public void setHasLinks(boolean hasLinks) {
		this.hasLinks = hasLinks;
	}

	/**
	 * @return the hasDrilldown
	 */
	public boolean isHasDrilldown() {
		return hasDrilldown;
	}

	/**
	 * @param hasDrilldown the hasDrilldown to set
	 */
	public void setHasDrilldown(boolean hasDrilldown) {
		this.hasDrilldown = hasDrilldown;
	}

	/**
	 * @return the openDrilldownInNewWindow
	 */
	public boolean isOpenDrilldownInNewWindow() {
		return openDrilldownInNewWindow;
	}

	/**
	 * @param openDrilldownInNewWindow the openDrilldownInNewWindow to set
	 */
	public void setOpenDrilldownInNewWindow(boolean openDrilldownInNewWindow) {
		this.openDrilldownInNewWindow = openDrilldownInNewWindow;
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

	/**
	 * @return the displayParameters
	 */
	public Map<Integer, ArtQueryParam> getDisplayParameters() {
		return displayParameters;
	}

	/**
	 * @param displayParameters the displayParameters to set
	 */
	public void setDisplayParameters(Map<Integer, ArtQueryParam> displayParameters) {
		this.displayParameters = displayParameters;
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
	public void fillDataset(ResultSet rs) throws SQLException {
		//provide default implementation in case dataset is created in another way
		//do nothing by default. 
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
	@SuppressWarnings("rawtypes") //remove if and when cewolf changes the interface
	public boolean hasExpired(Map params, Date since) {
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
		Plot plot = chart.getPlot();

		if (plot instanceof XYPlot) {
			XYPlot xyPlot = (XYPlot) plot;

			//set y axis range if required
			if (StringUtils.isNotBlank(params.get("from")) && StringUtils.isNotBlank(params.get("to"))) {
				Double from = Double.valueOf(params.get("from"));
				Double to = Double.valueOf(params.get("to"));
				NumberAxis rangeAxis = (NumberAxis) xyPlot.getRangeAxis();
				rangeAxis.setRange(from, to);
			}

			//set grid lines to light grey so that they are visible with a default plot background colour of white
			xyPlot.setRangeGridlinePaint(Color.LIGHT_GRAY);
			xyPlot.setDomainGridlinePaint(Color.LIGHT_GRAY);
		}
	}

	//produces the basic jfree chart. called by getChart()
	@Override
	protected JFreeChart produceChart() throws DatasetProduceException, ChartValidationException {
		return CewolfChartFactory.getChartInstance(type, title, xAxisLabel, yAxisLabel, dataset, showLegend);
	}

	public void generateFile(ReportFormat reportFormat, String outputFileName)
			throws IOException, DatasetProduceException, ChartValidationException, PostProcessingException {

		//use cewolf to generate chart in order to achieve similar look as with interactive/browser display
		//<cewolf:chart tag doesn't allow expressions for the plotbackgroundcolor attribute
		//so use the same color/constant here as in the showChart.jsp page
		setPlotBackgroundPaint(Color.WHITE);
		setBackgroundPaint(Color.decode(backgroundColor));

		//use cewolf AbstractChartDefinition.getChart() to generate chart
		//with additional processing like antialising and running external post processors
		//in order to achieve similar look as with interactive/browser display using <cewolf> tags
		//alternative is to duplicate the code
		JFreeChart chart = getChart();

		//run internal post processor
		processChart(chart, internalPostProcessorParams);

		if (reportFormat == ReportFormat.png) {
			ChartUtilities.saveChartAsPNG(new File(outputFileName), chart, width, height);
		} else if (reportFormat == ReportFormat.pdf) {

		} else {
			throw new IllegalArgumentException("Unsupported report format: " + reportFormat);
		}
	}

}
