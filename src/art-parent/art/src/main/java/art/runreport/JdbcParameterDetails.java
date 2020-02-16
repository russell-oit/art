/*
 * ART. A Reporting Tool.
 * Copyright (C) 2020 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.runreport;

import art.reportparameter.ReportParameter;
import java.util.List;

/**
 * Represents details of jdbc parameters
 * 
 * @author Timothy Anyona
 */
public class JdbcParameterDetails {

	private ReportParameter reportParam;
	private List<Object> finalValues;
	private boolean xParameter;
	
	public JdbcParameterDetails(){
		
	}

	public JdbcParameterDetails(ReportParameter reportParam) {
		this.reportParam = reportParam;
	}

	/**
	 * @return the reportParam
	 */
	public ReportParameter getReportParam() {
		return reportParam;
	}

	/**
	 * @param reportParam the reportParam to set
	 */
	public void setReportParam(ReportParameter reportParam) {
		this.reportParam = reportParam;
	}

	/**
	 * @return the finalValues
	 */
	public List<Object> getFinalValues() {
		return finalValues;
	}

	/**
	 * @param finalValues the finalValues to set
	 */
	public void setFinalValues(List<Object> finalValues) {
		this.finalValues = finalValues;
	}

	/**
	 * @return the xParameter
	 */
	public boolean isxParameter() {
		return xParameter;
	}

	/**
	 * @param xParameter the xParameter to set
	 */
	public void setxParameter(boolean xParameter) {
		this.xParameter = xParameter;
	}

}
