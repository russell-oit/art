/*
 * ART. A Reporting Tool.
 * Copyright (C) 2018 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.reportoptions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.List;

/**
 * Represents options for plotly.js reports
 *
 * @author Timothy Anyona
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlotlyOptions implements Serializable {

	private static final long serialVersionUID = 1L;
	private String template;
	private String barmode;
	private String xColumn;
	private List<String> yColumns;
	private String type;
	private String mode;
	private List<String> chartTypes;
	private String title;
	private String xAxisTitle;
	private String yAxisTitle;
	private boolean showLegend = true;
	private boolean showText;
	private int height;
	private int width;
	private double hole;
	private String bundle;
	private String textPosition;
	private String hoverInfo;
	private String orientation;

	/**
	 * @return the showLegend
	 */
	public boolean isShowLegend() {
		return showLegend;
	}

	/**
	 * @param showLegend the showLegend to set
	 */
	public void setShowLegend(boolean showLegend) {
		this.showLegend = showLegend;
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
	 * @return the xAxisTitle
	 */
	public String getxAxisTitle() {
		return xAxisTitle;
	}

	/**
	 * @param xAxisTitle the xAxisTitle to set
	 */
	public void setxAxisTitle(String xAxisTitle) {
		this.xAxisTitle = xAxisTitle;
	}

	/**
	 * @return the yAxisTitle
	 */
	public String getyAxisTitle() {
		return yAxisTitle;
	}

	/**
	 * @param yAxisTitle the yAxisTitle to set
	 */
	public void setyAxisTitle(String yAxisTitle) {
		this.yAxisTitle = yAxisTitle;
	}

	/**
	 * @return the chartTypes
	 */
	public List<String> getChartTypes() {
		return chartTypes;
	}

	/**
	 * @param chartTypes the chartTypes to set
	 */
	public void setChartTypes(List<String> chartTypes) {
		this.chartTypes = chartTypes;
	}

	/**
	 * @return the mode
	 */
	public String getMode() {
		return mode;
	}

	/**
	 * @param mode the mode to set
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}

	/**
	 * @return the template
	 */
	public String getTemplate() {
		return template;
	}

	/**
	 * @param template the template to set
	 */
	public void setTemplate(String template) {
		this.template = template;
	}

	/**
	 * @return the barmode
	 */
	public String getBarmode() {
		return barmode;
	}

	/**
	 * @param barmode the barmode to set
	 */
	public void setBarmode(String barmode) {
		this.barmode = barmode;
	}

	/**
	 * @return the xColumn
	 */
	public String getxColumn() {
		return xColumn;
	}

	/**
	 * @param xColumn the xColumn to set
	 */
	public void setxColumn(String xColumn) {
		this.xColumn = xColumn;
	}

	/**
	 * @return the yColumns
	 */
	public List<String> getyColumns() {
		return yColumns;
	}

	/**
	 * @param yColumns the yColumns to set
	 */
	public void setyColumns(List<String> yColumns) {
		this.yColumns = yColumns;
	}

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
	 * @return the showText
	 */
	public boolean isShowText() {
		return showText;
	}

	/**
	 * @param showText the showText to set
	 */
	public void setShowText(boolean showText) {
		this.showText = showText;
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
	 * @return the hole
	 */
	public double getHole() {
		return hole;
	}

	/**
	 * @param hole the hole to set
	 */
	public void setHole(double hole) {
		this.hole = hole;
	}

	/**
	 * @return the bundle
	 */
	public String getBundle() {
		return bundle;
	}

	/**
	 * @param bundle the bundle to set
	 */
	public void setBundle(String bundle) {
		this.bundle = bundle;
	}

	/**
	 * @return the textPosition
	 */
	public String getTextPosition() {
		return textPosition;
	}

	/**
	 * @param textPosition the textPosition to set
	 */
	public void setTextPosition(String textPosition) {
		this.textPosition = textPosition;
	}

	/**
	 * @return the hoverInfo
	 */
	public String getHoverInfo() {
		return hoverInfo;
	}

	/**
	 * @param hoverInfo the hoverInfo to set
	 */
	public void setHoverInfo(String hoverInfo) {
		this.hoverInfo = hoverInfo;
	}

	/**
	 * @return the orientation
	 */
	public String getOrientation() {
		return orientation;
	}

	/**
	 * @param orientation the orientation to set
	 */
	public void setOrientation(String orientation) {
		this.orientation = orientation;
	}

}
