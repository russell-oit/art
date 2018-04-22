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
package art.reportoptions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.List;

/**
 * Represents report options for c3.js reports
 *
 * @author Timothy Anyona
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class C3Options implements Serializable {

	private static final long serialVersionUID = 1L;
	private String cssFile;
	private List<String> chartTypes;
	private String x;
	private String type;
	private List<String> value;
	private String template;
	private boolean groupedTooltip = true;
	private boolean showLegend = true;
	private boolean rotatedAxis;
	private boolean showTooltip = true;
	private String legendPosition;
	private int width;
	private int height;

	/**
	 * @return the x
	 */
	public String getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(String x) {
		this.x = x;
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
	 * @return the value
	 */
	public List<String> getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(List<String> value) {
		this.value = value;
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
	 * @return the cssFile
	 */
	public String getCssFile() {
		return cssFile;
	}

	/**
	 * @param cssFile the cssFile to set
	 */
	public void setCssFile(String cssFile) {
		this.cssFile = cssFile;
	}

	/**
	 * @return the groupedTooltip
	 */
	public boolean isGroupedTooltip() {
		return groupedTooltip;
	}

	/**
	 * @param groupedTooltip the groupedTooltip to set
	 */
	public void setGroupedTooltip(boolean groupedTooltip) {
		this.groupedTooltip = groupedTooltip;
	}

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
	 * @return the rotatedAxis
	 */
	public boolean isRotatedAxis() {
		return rotatedAxis;
	}

	/**
	 * @param rotatedAxis the rotatedAxis to set
	 */
	public void setRotatedAxis(boolean rotatedAxis) {
		this.rotatedAxis = rotatedAxis;
	}

	/**
	 * @return the showTooltip
	 */
	public boolean isShowTooltip() {
		return showTooltip;
	}

	/**
	 * @param showTooltip the showTooltip to set
	 */
	public void setShowTooltip(boolean showTooltip) {
		this.showTooltip = showTooltip;
	}

	/**
	 * @return the legendPosition
	 */
	public String getLegendPosition() {
		return legendPosition;
	}

	/**
	 * @param legendPosition the legendPosition to set
	 */
	public void setLegendPosition(String legendPosition) {
		this.legendPosition = legendPosition;
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
}
