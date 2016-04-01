/*
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Enum for report types
 *
 * @author Timothy Anyona
 */
public enum ReportType {

	Tabular(0), Group(1), TabularHtml(103),
	Update(100), Crosstab(101), CrosstabHtml(102),
	Dashboard(110), Text(111), Mondrian(112), MondrianXmla(113), SqlServerXmla(114),
	JasperReportsTemplate(115), JasperReportsArt(116), JxlsTemplate(117), JxlsArt(118),
	LovDynamic(119), LovStatic(120), JobRecipients(121),
	XYChart(-1), Pie3DChart(-2), HorizontalBar3DChart(-3), VerticalBar3DChart(-4),
	LineChart(-5), TimeSeriesChart(-6), DateSeriesChart(-7), StackedVerticalBar3DChart(-8),
	StackedHorizontalBar3DChart(-9), SpeedometerChart(-10), BubbleChart(-11),
	HeatmapChart(-12), Pie2DChart(-13), VerticalBar2DChart(-14), StackedVerticalBar2DChart(-15),
	HorizontalBar2DChart(-16), StackedHorizontalBar2DChart(-17);

	private final int value;

	private ReportType(int value) {
		this.value = value;
	}

	/**
	 * Determine if this report type uses sql queries
	 *
	 * @return
	 */
	public boolean usesSql() {
		if (this == Dashboard || this == Text || isOlap()
				|| this == LovStatic) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Determine if this is an lov report type
	 *
	 * @return
	 */
	public boolean isLov() {
		switch (this) {
			case LovDynamic:
			case LovStatic:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Determine if this is a jasper reports type
	 *
	 * @return
	 */
	public boolean isJasperReports() {
		if (this == JasperReportsArt || this == JasperReportsTemplate) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Determine if this is a jxls report type
	 *
	 * @return
	 */
	public boolean isJxls() {
		switch (this) {
			case JxlsArt:
			case JxlsTemplate:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns true if this is an olap report type
	 *
	 * @return
	 */
	public boolean isOlap() {
		switch (this) {
			case Mondrian:
			case MondrianXmla:
			case SqlServerXmla:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Determine if this is a crosstab type
	 *
	 * @return
	 */
	public boolean isCrosstab() {
		if (this == Crosstab || this == CrosstabHtml) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Determine if this is a chart report
	 *
	 * @return
	 */
	public boolean isChart() {
		if (value < 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Determine if this is a direct output report
	 *
	 * @return
	 */
	public boolean isStandardOutput() {
		switch (this) {
			case Tabular:
			case TabularHtml:
			case Crosstab:
			case CrosstabHtml:
			case LovDynamic:
			case LovStatic:
			case JobRecipients:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Get enum value
	 *
	 * @return
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Get a list of all enum values
	 *
	 * @return
	 */
	public static List<ReportType> list() {
		//use a new list as Arrays.asList() returns a fixed-size list. can't add or remove from it
		List<ReportType> items = new ArrayList<>();
		items.addAll(Arrays.asList(values()));
		return items;
	}

	/**
	 * Convert a value to an enum. If the conversion fails, Tabular is returned
	 *
	 * @param value
	 * @return
	 */
	public static ReportType toEnum(int value) {
		return toEnum(value, Tabular);
	}

	/**
	 * Convert a value to an enum. If the conversion fails, the specified
	 * default is returned
	 *
	 * @param value
	 * @param defaultEnum
	 * @return
	 */
	public static ReportType toEnum(int value, ReportType defaultEnum) {
		for (ReportType v : values()) {
			if (v.value == value) {
				return v;
			}
		}

		return defaultEnum;
	}

	/**
	 * Get enum description. In case description needs to be different from
	 * internal value
	 *
	 * @return
	 */
	public String getDescription() {
		switch (this) {
			case Tabular:
				return "Tabular";
			case Update:
				return "Update Statement";
			case Crosstab:
				return "Crosstab";
			case CrosstabHtml:
				return "Crosstab (html only)";
			case TabularHtml:
				return "Tabular (html only)";
			case Dashboard:
				return "Dashboard";
			case Text:
				return "Text";
			case Mondrian:
				return "Pivot Table: Mondrian";
			case MondrianXmla:
				return "Pivot Table: Mondrian XMLA";
			case SqlServerXmla:
				return "Pivot Table: SQL Server XMLA";
			case JasperReportsTemplate:
				return "JasperReports: Template Query";
			case JasperReportsArt:
				return "JasperReports: ART Query";
			case JxlsTemplate:
				return "jXLS: Template Query";
			case JxlsArt:
				return "jXLS: ART Query";
			case LovDynamic:
				return "LOV: Dynamic";
			case LovStatic:
				return "LOV: Static";
			case JobRecipients:
				return "Dynamic Job Recipients";
			case XYChart:
				return "Chart: XY";
			case Pie3DChart:
				return "Chart: Pie 3D";
			case HorizontalBar3DChart:
				return "Chart: Horizontal Bar 3D";
			case VerticalBar3DChart:
				return "Chart: Vertical Bar 3D";
			case LineChart:
				return "Chart: Line";
			case TimeSeriesChart:
				return "Chart: Time Series";
			case DateSeriesChart:
				return "Chart: Date Series";
			case StackedVerticalBar3DChart:
				return "Chart: Stacked Vertical Bar 3D";
			case StackedHorizontalBar3DChart:
				return "Chart: Stacked Horizontal Bar 3D";
			case SpeedometerChart:
				return "Chart: Speedometer";
			case BubbleChart:
				return "Chart: Bubble Chart";
			case HeatmapChart:
				return "Chart: Heat Map";
			case Pie2DChart:
				return "Chart: Pie 2D";
			case VerticalBar2DChart:
				return "Chart: Vertical Bar 2D";
			case StackedVerticalBar2DChart:
				return "Chart: Stacked Vertical Bar 2D";
			case HorizontalBar2DChart:
				return "Chart: Horizontal Bar 2D";
			case StackedHorizontalBar2DChart:
				return "Chart: Stacked Horizontal Bar 2D";
			case Group:
				return "Group";
			default:
				return this.name();
		}
	}

}
