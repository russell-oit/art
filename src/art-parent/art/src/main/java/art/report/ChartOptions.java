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
package art.report;

import java.io.Serializable;

/**
 * Represents chart options
 *
 * @author Timothy Anyona
 */
public class ChartOptions implements Serializable {

	private static final long serialVersionUID = 1L;
	private Boolean showLegend; //use object wrapper instead of primitive to differentiate 0 and "not set"
	private Boolean showLabels;
	private Boolean showPoints;
	private Boolean showData;
	private Integer rotateAt;
	private Integer removeAt;
	private Integer width;
	private Integer height;
	private Double yAxisMin;
	private Double yAxisMax;
	private String backgroundColor;
	private String labelFormat;

	/**
	 * @return the labelFormat
	 */
	public String getLabelFormat() {
		return labelFormat;
	}

	/**
	 * @param labelFormat the labelFormat to set
	 */
	public void setLabelFormat(String labelFormat) {
		this.labelFormat = labelFormat;
	}

	/**
	 * @return the showLegend
	 */
	public Boolean getShowLegend() {
		//must use get in method name instead of is if using Boolean object and not boolean primitive
		//otherwise you'll have errors in jsp pages
		return showLegend;
	}

	/**
	 * @param showLegend the showLegend to set
	 */
	public void setShowLegend(Boolean showLegend) {
		this.showLegend = showLegend;
	}

	/**
	 * @return the showLabels
	 */
	public Boolean getShowLabels() {
		return showLabels;
	}

	/**
	 * @param showLabels the showLabels to set
	 */
	public void setShowLabels(Boolean showLabels) {
		this.showLabels = showLabels;
	}

	/**
	 * @return the showPoints
	 */
	public Boolean getShowPoints() {
		return showPoints;
	}

	/**
	 * @param showPoints the showPoints to set
	 */
	public void setShowPoints(Boolean showPoints) {
		this.showPoints = showPoints;
	}

	/**
	 * @return the showData
	 */
	public Boolean getShowData() {
		return showData;
	}

	/**
	 * @param showData the showData to set
	 */
	public void setShowData(Boolean showData) {
		this.showData = showData;
	}

	/**
	 * @return the rotateAt
	 */
	public Integer getRotateAt() {
		return rotateAt;
	}

	/**
	 * @param rotateAt the rotateAt to set
	 */
	public void setRotateAt(Integer rotateAt) {
		this.rotateAt = rotateAt;
	}

	/**
	 * @return the removeAt
	 */
	public Integer getRemoveAt() {
		return removeAt;
	}

	/**
	 * @param removeAt the removeAt to set
	 */
	public void setRemoveAt(Integer removeAt) {
		this.removeAt = removeAt;
	}

	/**
	 * @return the width
	 */
	public Integer getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(Integer width) {
		this.width = width;
	}

	/**
	 * @return the height
	 */
	public Integer getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(Integer height) {
		this.height = height;
	}

	/**
	 * @return the yAxisMin
	 */
	public Double getyAxisMin() {
		return yAxisMin;
	}

	/**
	 * @param yAxisMin the yAxisMin to set
	 */
	public void setyAxisMin(Double yAxisMin) {
		this.yAxisMin = yAxisMin;
	}

	/**
	 * @return the yAxisMax
	 */
	public Double getyAxisMax() {
		return yAxisMax;
	}

	/**
	 * @param yAxisMax the yAxisMax to set
	 */
	public void setyAxisMax(Double yAxisMax) {
		this.yAxisMax = yAxisMax;
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
}
