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

package art.reportparameter;

import art.parameter.Parameter;
import art.report.Report;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Class to represent report parameter
 * 
 * @author Timothy Anyona
 */
public class ReportParameter implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private int reportParameterId;
	private Report report;
	private Parameter parameter;
	private int position;
	private String[] passedParameterValues; //used for run report logic
	private Map<String, String> lovValues; //store value and label for lov parameters
	private Object actualParameterValues;

	/**
	 * Get the value of actualParameterValues
	 *
	 * @return the value of actualParameterValues
	 */
	public Object getActualParameterValues() {
		return actualParameterValues;
	}

	/**
	 * Set the value of actualParameterValues
	 *
	 * @param actualParameterValues new value of actualParameterValues
	 */
	public void setActualParameterValues(Object actualParameterValues) {
		this.actualParameterValues = actualParameterValues;
	}


	/**
	 * Get the value of lovValues
	 *
	 * @return the value of lovValues
	 */
	public Map<String, String> getLovValues() {
		return lovValues;
	}

	/**
	 * Set the value of lovValues
	 *
	 * @param lovValues new value of lovValues
	 */
	public void setLovValues(Map<String, String> lovValues) {
		this.lovValues = lovValues;
	}


	/**
	 * @return the reportParameterId
	 */
	public int getReportParameterId() {
		return reportParameterId;
	}

	/**
	 * @param reportParameterId the reportParameterId to set
	 */
	public void setReportParameterId(int reportParameterId) {
		this.reportParameterId = reportParameterId;
	}

	/**
	 * @return the report
	 */
	public Report getReport() {
		return report;
	}

	/**
	 * @param report the report to set
	 */
	public void setReport(Report report) {
		this.report = report;
	}

	/**
	 * @return the parameter
	 */
	public Parameter getParameter() {
		return parameter;
	}

	/**
	 * @param parameter the parameter to set
	 */
	public void setParameter(Parameter parameter) {
		this.parameter = parameter;
	}

	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * @param position the position to set
	 */
	public void setPosition(int position) {
		this.position = position;
	}

	/**
	 * @return the passedParameterValues
	 */
	public String[] getPassedParameterValues() {
		return passedParameterValues;
	}

	/**
	 * @param passedParameterValues the passedParameterValues to set
	 */
	public void setPassedParameterValues(String[] passedParameterValues) {
		this.passedParameterValues = passedParameterValues;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 23 * hash + this.reportParameterId;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ReportParameter other = (ReportParameter) obj;
		if (this.reportParameterId != other.reportParameterId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ReportParameter{" + "reportParameterId=" + reportParameterId + '}';
	}
	
}
