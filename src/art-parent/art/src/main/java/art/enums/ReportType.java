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

	//items will be displayed in editReport.jsp in the order they appear here
	Tabular(0), TabularHtml(103), Crosstab(101), CrosstabHtml(102),
	TabularHeatmap(148), Group(1), Update(100),
	Dashboard(110), GridstackDashboard(129), Text(111), JPivotMondrian(112),
	JPivotMondrianXmla(113), JPivotSqlServerXmla(114),
	SaikuConnection(150), SaikuReport(149),
	JasperReportsTemplate(115), JasperReportsArt(116), JxlsTemplate(117), JxlsArt(118),
	LovDynamic(119), LovStatic(120), JobRecipients(121), FreeMarker(122), Thymeleaf(131),
	ReactPivot(130), PivotTableJs(132), PivotTableJsCsvLocal(133), PivotTableJsCsvServer(134),
	XDocReportFreeMarkerDocx(123), XDocReportVelocityDocx(124),
	XDocReportFreeMarkerOdt(125), XDocReportVelocityOdt(126),
	XDocReportFreeMarkerPptx(127), XDocReportVelocityPptx(128),
	XYChart(-1), Pie3DChart(-2), HorizontalBar3DChart(-3), VerticalBar3DChart(-4),
	LineChart(-5), TimeSeriesChart(-6), DateSeriesChart(-7), StackedVerticalBar3DChart(-8),
	StackedHorizontalBar3DChart(-9), SpeedometerChart(-10), BubbleChart(-11),
	HeatmapChart(-12), Pie2DChart(-13), VerticalBar2DChart(-14), StackedVerticalBar2DChart(-15),
	HorizontalBar2DChart(-16), StackedHorizontalBar2DChart(-17),
	Dygraphs(135), DygraphsCsvLocal(136), DygraphsCsvServer(137),
	DataTables(138), DataTablesCsvLocal(139), DataTablesCsvServer(140),
	FixedWidth(141), C3(142), ChartJs(143), Datamaps(144), DatamapsFile(145),
	Leaflet(146), OpenLayers(147);

	private final int value;

	private ReportType(int value) {
		this.value = value;
	}

	/**
	 * Returns <code>true</code> if this report type can be scheduled
	 *
	 * @return <code>true</code> if this report type can be scheduled
	 */
	public boolean canSchedule() {
		switch (this) {
			case JPivotMondrian:
			case JPivotMondrianXmla:
			case JPivotSqlServerXmla:
			case Text:
			case LovStatic:
			case ReactPivot:
			case PivotTableJs:
			case PivotTableJsCsvLocal:
			case PivotTableJsCsvServer:
			case Dygraphs:
			case DygraphsCsvLocal:
			case DygraphsCsvServer:
			case DataTables:
			case DataTablesCsvLocal:
			case DataTablesCsvServer:
			case C3:
			case ChartJs:
			case Datamaps:
			case DatamapsFile:
			case Leaflet:
			case OpenLayers:
			case TabularHeatmap:
			case SaikuReport:
			case SaikuConnection:
				return false;
			default:
				return true;
		}
	}

	/**
	 * Returns <code>true</code> if this is a leaflet or openlayers report
	 *
	 * @return <code>true</code> if this is a leaflet or openlayers report
	 */
	public boolean isWebMap() {
		switch (this) {
			case Leaflet:
			case OpenLayers:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns <code>true</code> if this is a database or file datamaps report
	 *
	 * @return <code>true</code> if this is a database or file datamaps report
	 */
	public boolean isDatamaps() {
		switch (this) {
			case Datamaps:
			case DatamapsFile:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns <code>true</code> if this is a regular or csv datatables report
	 *
	 * @return <code>true</code> if this is a regular or csv datatables report
	 */
	public boolean isDataTables() {
		switch (this) {
			case DataTables:
			case DataTablesCsvLocal:
			case DataTablesCsvServer:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns <code>true</code> if this is a regular or csv dygraphs report
	 *
	 * @return <code>true</code> if this is a regular or csv dygraphs report
	 */
	public boolean isDygraphs() {
		switch (this) {
			case Dygraphs:
			case DygraphsCsvLocal:
			case DygraphsCsvServer:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns <code>true</code> if this is a regular or csv pivottable.js
	 * report
	 *
	 * @return <code>true</code> if this is a regular or csv pivottable.js
	 * report
	 */
	public boolean isPivotTableJs() {
		switch (this) {
			case PivotTableJs:
			case PivotTableJsCsvLocal:
			case PivotTableJsCsvServer:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns <code>true</code> if this is a regular or gridstack dashboard
	 *
	 * @return <code>true</code> if this is a regular or gridstack dashboard
	 */
	public boolean isDashboard() {
		switch (this) {
			case Dashboard:
			case GridstackDashboard:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns <code>true</code> if this report type is a chart that uses an xy
	 * plot
	 *
	 * @return <code>true</code> if this report type is a chart that uses an xy
	 * plot
	 */
	public boolean isXYPlotChart() {
		switch (this) {
			case XYChart:
			case TimeSeriesChart:
			case DateSeriesChart:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns <code>true</code> if this report type is a chart that uses a
	 * category plot
	 *
	 * @return <code>true</code> if this report type is a chart that uses a
	 * category plot
	 */
	public boolean isCategoryPlotChart() {
		switch (this) {
			case LineChart:
			case HorizontalBar2DChart:
			case HorizontalBar3DChart:
			case VerticalBar2DChart:
			case VerticalBar3DChart:
			case StackedHorizontalBar2DChart:
			case StackedHorizontalBar3DChart:
			case StackedVerticalBar2DChart:
			case StackedVerticalBar3DChart:
				return true;
			default:
				return false;
		}
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
	 * Returns <code>true</code> if this is a jpivot mondrian, mondrian xmla or
	 * sql server xmla report type
	 *
	 * @return <code>true</code> if this is a jpivot mondrian, mondrian xmla or
	 * sql server xmla report type
	 */
	public boolean isJPivot() {
		switch (this) {
			case JPivotMondrian:
			case JPivotMondrianXmla:
			case JPivotSqlServerXmla:
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
	 * Returns <code>true</code> if this is a tabular, crosstab, dynamic lov,
	 * job recipients, tabular heatmap report
	 *
	 * @return <code>true</code> if this is a tabular, crosstab, dynamic lov,
	 * job recipients, tabular heatmap report
	 */
	public boolean isStandardOutput() {
		switch (this) {
			case Tabular:
			case TabularHtml:
			case Crosstab:
			case CrosstabHtml:
			case LovDynamic:
			case JobRecipients:
			case TabularHeatmap:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns <code>true</code> if this is a tabular or tabular html report
	 *
	 * @return <code>true</code> if this is a tabular or tabular html report
	 */
	public boolean isTabular() {
		switch (this) {
			case Tabular:
			case TabularHtml:
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
			case GridstackDashboard:
				return "Dashboard: Gridstack";
			case Text:
				return "Text";
			case JPivotMondrian:
				return "JPivot: Mondrian";
			case JPivotMondrianXmla:
				return "JPivot: Mondrian XMLA";
			case JPivotSqlServerXmla:
				return "JPivot: SQL Server XMLA";
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
			case ReactPivot:
				return "ReactPivot";
			case Thymeleaf:
				return "Thymeleaf";
			case PivotTableJs:
				return "PivotTable.js";
			case PivotTableJsCsvLocal:
				return "PivotTable.js: CSV Local";
			case PivotTableJsCsvServer:
				return "PivotTable.js: CSV Server";
			case Dygraphs:
				return "Dygraphs";
			case DygraphsCsvLocal:
				return "Dygraphs: CSV Local";
			case DygraphsCsvServer:
				return "Dygraphs: CSV Server";
			case DataTables:
				return "DataTables";
			case DataTablesCsvLocal:
				return "DataTables: CSV Local";
			case DataTablesCsvServer:
				return "DataTables: CSV Server";
			case FixedWidth:
				return "Fixed Width";
			case C3:
				return "C3.js";
			case ChartJs:
				return "Chart.js";
			case Datamaps:
				return "Datamaps";
			case DatamapsFile:
				return "Datamaps: File";
			case Leaflet:
				return "Leaflet";
			case OpenLayers:
				return "OpenLayers";
			case TabularHeatmap:
				return "Tabular: Heatmap";
			case SaikuReport:
				return "Saiku: Report";
			case SaikuConnection:
				return "Saiku: Connection";
			default:
				return this.name();
		}
	}
}
