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

import art.utils.ArtQueryParam;
import de.laures.cewolf.ChartPostProcessor;
import de.laures.cewolf.DatasetProduceException;
import de.laures.cewolf.DatasetProducer;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Dataset;

/**
 *
 * @author Timothy Anyona
 */
public abstract class AbstractChart implements Serializable, DatasetProducer, ChartPostProcessor {

	private static final long serialVersionUID = 1L;
	private String title;
	private String xAxisLabel;
	private String yAxisLabel;
	private String seriesName;
	private int height = 300;
	private int width = 500;
	private String bgColor = "#FFFFFF";
	private boolean useHyperLinks;
	private boolean hasDrilldown;
	private Map<Integer, ArtQueryParam> displayParameters;
	private Dataset dataset;
	private String type;

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @param dataset the dataset to set
	 */
	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	/**
	 * @return the dataset
	 */
	public Dataset getDataset() {
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

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
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
		this.xAxisLabel = xAxisLabel;
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
		this.yAxisLabel = yAxisLabel;
	}

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
	 * @return the bgColor
	 */
	public String getBgColor() {
		return bgColor;
	}

	/**
	 * @param bgColor the bgColor to set
	 */
	public void setBgColor(String bgColor) {
		this.bgColor = bgColor;
	}

	/**
	 * @return the useHyperLinks
	 */
	public boolean isUseHyperLinks() {
		return useHyperLinks;
	}

	/**
	 * @param useHyperLinks the useHyperLinks to set
	 */
	public void setUseHyperLinks(boolean useHyperLinks) {
		this.useHyperLinks = useHyperLinks;
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

	public abstract void fillDataset(ResultSet rs) throws SQLException;

	//returns the dataset to be used for rendering the chart
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
	@Override
	public abstract String getProducerId();

	//performs post processing on the generated chart using the given parameters
	@Override
	public abstract void processChart(JFreeChart chart, Map<String, String> params);

}
