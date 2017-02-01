/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
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
	private boolean showLegend;
	private boolean showLabels;
	private boolean showPoints;
	private boolean showData;
	private int rotateAt;
	private int removeAt;
	private Integer width;
	private Integer height;
	private double yAxisMin;
	private double yAxisMax;
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
	 * @return the showLabels
	 */
	public boolean isShowLabels() {
		return showLabels;
	}

	/**
	 * @param showLabels the showLabels to set
	 */
	public void setShowLabels(boolean showLabels) {
		this.showLabels = showLabels;
	}

	/**
	 * @return the showPoints
	 */
	public boolean isShowPoints() {
		return showPoints;
	}

	/**
	 * @param showPoints the showPoints to set
	 */
	public void setShowPoints(boolean showPoints) {
		this.showPoints = showPoints;
	}

	/**
	 * @return the showData
	 */
	public boolean isShowData() {
		return showData;
	}

	/**
	 * @param showData the showData to set
	 */
	public void setShowData(boolean showData) {
		this.showData = showData;
	}

	/**
	 * @return the rotateAt
	 */
	public int getRotateAt() {
		return rotateAt;
	}

	/**
	 * @param rotateAt the rotateAt to set
	 */
	public void setRotateAt(int rotateAt) {
		this.rotateAt = rotateAt;
	}

	/**
	 * @return the removeAt
	 */
	public int getRemoveAt() {
		return removeAt;
	}

	/**
	 * @param removeAt the removeAt to set
	 */
	public void setRemoveAt(int removeAt) {
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
	public double getyAxisMin() {
		return yAxisMin;
	}

	/**
	 * @param yAxisMin the yAxisMin to set
	 */
	public void setyAxisMin(double yAxisMin) {
		this.yAxisMin = yAxisMin;
	}

	/**
	 * @return the yAxisMax
	 */
	public double getyAxisMax() {
		return yAxisMax;
	}

	/**
	 * @param yAxisMax the yAxisMax to set
	 */
	public void setyAxisMax(double yAxisMax) {
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
