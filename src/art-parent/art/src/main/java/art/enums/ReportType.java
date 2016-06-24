/*
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
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
 * Represents report types
 *
 * @author Timothy Anyona
 */
public enum ReportType {

	Tabular(0), Group(1), TabularHtml(103),
	Update(100), Crosstab(101), CrosstabHtml(102),
	Dashboard(110), Text(111), Mondrian(112), MondrianXmla(113), SqlServerXmla(114),
	JasperReportsTemplate(115), JasperReportsArt(116), JxlsTemplate(117), JxlsArt(118),
	LovDynamic(119), LovStatic(120), JobRecipients(121), FreeMarker(122),
	XDocReportFreeMarkerDocx(123), XDocReportVelocityDocx(124),
	XDocReportFreeMarkerOdt(125), XDocReportVelocityOdt(126),
	XDocReportFreeMarkerPptx(127), XDocReportVelocityPptx(128),
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
	 * Returns <code>true</code> if this is a lov dynamic or lov static report
	 * type
	 *
	 * @return <code>true</code> if this is a lov dynamic or lov static report
	 * type
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
	 * Returns <code>true</code> if this is a xdocreport freemarker or
	 * xdocreport velocity report
	 *
	 * @return <code>true</code> if this is a xdocreport freemarker or
	 * xdocreport velocity report
	 */
	public boolean isXDocReport() {
		switch (this) {
			case XDocReportFreeMarkerDocx:
			case XDocReportVelocityDocx:
			case XDocReportFreeMarkerOdt:
			case XDocReportVelocityOdt:
			case XDocReportFreeMarkerPptx:
			case XDocReportVelocityPptx:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns <code>true</code> if this is a xdocreport report that produces
	 * docx output
	 *
	 * @return <code>true</code> if this is a xdocreport report that produces
	 * docx output
	 */
	public boolean isXDocReportDocx() {
		switch (this) {
			case XDocReportFreeMarkerDocx:
			case XDocReportVelocityDocx:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns <code>true</code> if this is a xdocreport report that produces
	 * odt output
	 *
	 * @return <code>true</code> if this is a xdocreport report that produces
	 * odt output
	 */
	public boolean isXDocReportOdt() {
		switch (this) {
			case XDocReportFreeMarkerOdt:
			case XDocReportVelocityOdt:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns <code>true</code> if this is a xdocreport report that produces
	 * pptx output
	 *
	 * @return <code>true</code> if this is a xdocreport report that produces
	 * pptx output
	 */
	public boolean isXDocReportPptx() {
		switch (this) {
			case XDocReportFreeMarkerPptx:
			case XDocReportVelocityPptx:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns <code>true</code> if this is a xdocreport report that uses the
	 * freemarker engine
	 *
	 * @return <code>true</code> if this is a xdocreport report that uses the
	 * freemarker engine
	 */
	public boolean isXDocReportFreeMarker() {
		switch (this) {
			case XDocReportFreeMarkerDocx:
			case XDocReportFreeMarkerOdt:
			case XDocReportFreeMarkerPptx:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns <code>true</code> if this is a xdocreport report that uses the
	 * velocity engine
	 *
	 * @return <code>true</code> if this is a xdocreport report that uses the
	 * velocity engine
	 */
	public boolean isXDocReportVelocity() {
		switch (this) {
			case XDocReportVelocityDocx:
			case XDocReportVelocityOdt:
			case XDocReportVelocityPptx:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns <code>true</code> if this is a jasper reports art or jasper
	 * reports template report type
	 *
	 * @return <code>true</code> if this is a jasper reports art or jasper
	 * reports template report type
	 */
	public boolean isJasperReports() {
		switch (this) {
			case JasperReportsArt:
			case JasperReportsTemplate:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns <code>true</code> if this is a jxls art or jxls template report
	 * type
	 *
	 * @return <code>true</code> if this is a jxls art or jxls template report
	 * type
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
	 * Returns <code>true</code> if this is a mondrian, mondrian xmla or sql
	 * server xmla report type
	 *
	 * @return <code>true</code> if this is a mondrian, mondrian xmla or sql
	 * server xmla report type
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
	 * Returns <code>true</code> if this is a crosstab or crosstab html report
	 * type
	 *
	 * @return <code>true</code> if this is a crosstab or crosstab html report
	 * type
	 */
	public boolean isCrosstab() {
		switch (this) {
			case Crosstab:
			case CrosstabHtml:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns <code>true</code> if this is a chart report type
	 *
	 * @return <code>true</code> if this is a chart report type
	 */
	public boolean isChart() {
		if (value < 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns <code>true</code> if this is a tabular, crosstab, lov, job
	 * recipients report type
	 *
	 * @return <code>true</code> if this is a tabular, crosstab, lov, job
	 * recipients report type
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
	 * Returns this enum option's value
	 *
	 * @return this enum option's value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Returns all enum options
	 *
	 * @return all enum options
	 */
	public static List<ReportType> list() {
		//use a new list as Arrays.asList() returns a fixed-size list. can't add or remove from it
		List<ReportType> items = new ArrayList<>();
		items.addAll(Arrays.asList(values()));
		return items;
	}

	/**
	 * Converts a value to an enum. If the conversion fails, Tabular is returned
	 *
	 * @param value the value to convert
	 * @return the enum option that corresponds to the value
	 */
	public static ReportType toEnum(int value) {
		return toEnum(value, Tabular);
	}

	/**
	 * Converts a value to an enum. If the conversion fails, the specified
	 * default is returned
	 *
	 * @param value the value to convert
	 * @param defaultEnum the default enum option to use
	 * @return the enum option that corresponds to the value
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
	 * Returns this enum option's description
	 *
	 * @return this enum option's description
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
			case FreeMarker:
				return "FreeMarker";
			case XDocReportFreeMarkerDocx:
				return "XDocReport: Freemarker engine - Docx";
			case XDocReportVelocityDocx:
				return "XDocReport: Velocity engine - Docx";
			case XDocReportFreeMarkerOdt:
				return "XDocReport: Freemarker engine - ODT";
			case XDocReportVelocityOdt:
				return "XDocReport: Velocity engine - ODT";
			case XDocReportFreeMarkerPptx:
				return "XDocReport: Freemarker engine - PPTX";
			case XDocReportVelocityPptx:
				return "XDocReport: Velocity engine - PPTX";
			default:
				return this.name();
		}
	}
}
