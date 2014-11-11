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

package art.runreport;

import art.report.ChartOptions;
import art.reportparameter.ReportParameter;
import java.util.List;
import java.util.Map;

/**
 * Represents the results of parameter processing
 * 
 * @author Timothy Anyona
 */
public class ParameterProcessorResult {
	private List<ReportParameter> reportParamsList;
	private Map<String, ReportParameter> reportParamsMap;
	private ReportOptions reportOptions;
	private ChartOptions chartOptions;

	/**
	 * @return the chartOptions
	 */
	public ChartOptions getChartOptions() {
		return chartOptions;
	}

	/**
	 * @param chartOptions the chartOptions to set
	 */
	public void setChartOptions(ChartOptions chartOptions) {
		this.chartOptions = chartOptions;
	}

	/**
	 * @return the reportParamsList
	 */
	public List<ReportParameter> getReportParamsList() {
		return reportParamsList;
	}

	/**
	 * @param reportParamsList the reportParamsList to set
	 */
	public void setReportParamsList(List<ReportParameter> reportParamsList) {
		this.reportParamsList = reportParamsList;
	}

	/**
	 * @return the reportParamsMap
	 */
	public Map<String, ReportParameter> getReportParamsMap() {
		return reportParamsMap;
	}

	/**
	 * @param reportParamsMap the reportParamsMap to set
	 */
	public void setReportParamsMap(Map<String, ReportParameter> reportParamsMap) {
		this.reportParamsMap = reportParamsMap;
	}

	/**
	 * @return the reportOptions
	 */
	public ReportOptions getReportOptions() {
		return reportOptions;
	}

	/**
	 * @param reportOptions the reportOptions to set
	 */
	public void setReportOptions(ReportOptions reportOptions) {
		this.reportOptions = reportOptions;
	}
}
