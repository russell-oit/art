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
	LovDynamic(119), LovStatic(120), JobRecipients(121),
	FreeMarker(122), Velocity(153), Thymeleaf(131),
	FixedWidth(141), CSV(152),
	ReactPivot(130), PivotTableJs(132), PivotTableJsCsvLocal(133), PivotTableJsCsvServer(134),
	XDocReportFreeMarkerDocx(123), XDocReportVelocityDocx(124),
	XDocReportFreeMarkerOdt(125), XDocReportVelocityOdt(126),
	XDocReportFreeMarkerPptx(127), XDocReportVelocityPptx(128), MongoDB(151),
	XYChart(-1), Pie3DChart(-2), HorizontalBar3DChart(-3), VerticalBar3DChart(-4),
	LineChart(-5), TimeSeriesChart(-6), DateSeriesChart(-7), StackedVerticalBar3DChart(-8),
	StackedHorizontalBar3DChart(-9), SpeedometerChart(-10), BubbleChart(-11),
	HeatmapChart(-12), Pie2DChart(-13), VerticalBar2DChart(-14), StackedVerticalBar2DChart(-15),
	HorizontalBar2DChart(-16), StackedHorizontalBar2DChart(-17),
	Dygraphs(135), DygraphsCsvLocal(136), DygraphsCsvServer(137),
	DataTables(138), DataTablesCsvLocal(139), DataTablesCsvServer(140),
	C3(142), Plotly(160), ChartJs(143), Datamaps(144), DatamapsFile(145),
	Leaflet(146), OpenLayers(147),
	OrgChartDatabase(154), OrgChartJson(155), OrgChartList(156), OrgChartAjax(157),
	ReportEngine(158), ReportEngineFile(159), View(161), File(162), Link(163),
	AwesomeChartJs(164), ApexChartsJs(165);

	private final int value;

	private ReportType(int value) {
		this.value = value;
	}

	/**
	 * Returns <code>true</code> if this report type uses jdbc that will be
	 * executed directly by the art application
	 *
	 * @return <code>true</code> if this report type uses jdbc that will be run
	 * by art
	 */
	public boolean isJdbcRunnableByArt() {
		switch (this) {
			case JasperReportsTemplate:
			case JxlsTemplate:
			case JPivotMondrian:
			case JPivotMondrianXmla:
			case JPivotSqlServerXmla:
			case LovStatic:
			case PivotTableJsCsvLocal:
			case PivotTableJsCsvServer:
			case DygraphsCsvLocal:
			case DygraphsCsvServer:
			case DataTablesCsvLocal:
			case DataTablesCsvServer:
			case DatamapsFile:
			case SaikuReport:
			case MongoDB:
			case OrgChartJson:
			case OrgChartList:
			case OrgChartAjax:
			case ReportEngineFile:
			case Dashboard:
			case GridstackDashboard:
				return false;
			default:
				return true;
		}
	}

	/**
	 * Returns <code>true</code> if this is a vertical bar or stacked vertical
	 * bar 2D chart report
	 *
	 * @return <code>true</code> if this is a vertical bar or stacked vertical
	 * bar 2D chart report
	 */
	public boolean isVerticalBar2DChart() {
		switch (this) {
			case VerticalBar2DChart:
			case StackedVerticalBar2DChart:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns <code>true</code> if this report type requires a scrollable
	 * resultset in order to run properly
	 *
	 * @return <code>true</code> if this report type requires a scrollable
	 * resultset in order to run properly
	 */
	public boolean requiresScrollableResultSet() {
		switch (this) {
			case Group:
			case ReportEngine:
			case ReportEngineFile:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns <code>true</code> if this report type stores templates in the
	 * js-templates directory
	 *
	 * @return <code>true</code> if this report type stores templates in the
	 * js-templates directory
	 */
	public boolean isUseJsTemplatesPath() {
		switch (this) {
			case ReactPivot:
			case PivotTableJs:
			case PivotTableJsCsvLocal:
			case PivotTableJsCsvServer: //can specify .js template and .csv data file
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
			case OrgChartDatabase:
			case OrgChartJson:
			case OrgChartList:
			case OrgChartAjax:
			case Plotly:
			case ApexChartsJs:
				return true;
			default:
				return false;
		}
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
			case MongoDB:
			case OrgChartDatabase:
			case OrgChartJson:
			case OrgChartList:
			case OrgChartAjax:
			case Plotly:
			case View:
			case Link:
			case AwesomeChartJs:
			case ApexChartsJs:
				return false;
			default:
				return true;
		}
	}

	/**
	 * Returns <code>true</code> if this is a reportengine report
	 *
	 * @return <code>true</code> if this is a reportengine report
	 */
	public boolean isReportEngine() {
		switch (this) {
			case ReportEngine:
			case ReportEngineFile:
				return true;
			default:
				return false;
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
	 * Returns <code>true</code> if this is an org chart report type
	 *
	 * @return <code>true</code> if this is an org chart report type
	 */
	public boolean isOrgChart() {
		switch (this) {
			case OrgChartDatabase:
			case OrgChartJson:
			case OrgChartList:
			case OrgChartAjax:
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
			case View:
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
			case Update:
				return "Update Statement";
			case CrosstabHtml:
				return "Crosstab (html only)";
			case TabularHtml:
				return "Tabular (html only)";
			case GridstackDashboard:
				return "Dashboard: Gridstack";
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
				return "Jxls: Template Query";
			case JxlsArt:
				return "Jxls: ART Query";
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
			case PivotTableJs:
				return "PivotTable.js";
			case PivotTableJsCsvLocal:
				return "PivotTable.js: CSV Local";
			case PivotTableJsCsvServer:
				return "PivotTable.js: CSV Server";
			case DygraphsCsvLocal:
				return "Dygraphs: CSV Local";
			case DygraphsCsvServer:
				return "Dygraphs: CSV Server";
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
			case DatamapsFile:
				return "Datamaps: File";
			case TabularHeatmap:
				return "Tabular: Heatmap";
			case SaikuReport:
				return "Saiku: Report";
			case SaikuConnection:
				return "Saiku: Connection";
			case OrgChartDatabase:
				return "OrgChart: Database";
			case OrgChartJson:
				return "OrgChart: JSON";
			case OrgChartList:
				return "OrgChart: List";
			case OrgChartAjax:
				return "OrgChart: Ajax";
			case ReportEngineFile:
				return "ReportEngine: File";
			case Plotly:
				return "Plotly.js";
			case ApexChartsJs:
				return "ApexCharts.js";
			default:
				return this.name();
		}
	}
}
