/**
 * Copyright (C) 2006 Dragos Balan (dragos.balan@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.reportengine;

import java.util.List;

import net.sf.reportengine.components.ReportComponent;
import net.sf.reportengine.out.AbstractReportOutput;
import net.sf.reportengine.out.ReportProps;

/**
 * abstract helper report
 *
 * @author dragos balan (dragos dot balan at gmail dot com)
 *
 */
abstract class AbstractReport<T extends AbstractReportOutput> implements Report {

	/**
	 * the default start report template
	 */
	public final static String START_REPORT_TEMPLATE = "startReport.ftl";

	/**
	 * default end report template
	 */
	public final static String END_REPORT_TEMPLATE = "endReport.ftl";

	/**
	 * the list of the components of this report
	 */
	private final List<ReportComponent> components;

	/**
	 * the report output
	 */
	private final T reportOutput;

	private String startReportTemplate = START_REPORT_TEMPLATE;
	private String endReportTemplate = END_REPORT_TEMPLATE;

	/**
	 * report constructor
	 *
	 * @param reportOutput the output of this report
	 * @param components	the report components
	 */
	AbstractReport(T reportOutput, List<ReportComponent> components) {
		this.reportOutput = reportOutput;
		this.components = components;
	}

	/**
	 * returns the components of this report
	 */
	protected List<ReportComponent> getComponents() {
		return components;
	}

	/**
	 * returns the output of this report
	 */
	protected T getReportOutput() {
		return reportOutput;
	}

	/**
	 * @return the startReportTemplate
	 */
	public String getStartReportTemplate() {
		return startReportTemplate;
	}

	/**
	 * @param startReportTemplate the startReportTemplate to set
	 */
	public void setStartReportTemplate(String startReportTemplate) {
		this.startReportTemplate = startReportTemplate;
	}

	/**
	 * @return the endReportTemplate
	 */
	public String getEndReportTemplate() {
		return endReportTemplate;
	}

	/**
	 * @param endReportTemplate the endReportTemplate to set
	 */
	public void setEndReportTemplate(String endReportTemplate) {
		this.endReportTemplate = endReportTemplate;
	}

	/**
	 * outputs each component of the report
	 *
	 * @param reportOutput	the output of this report
	 */
	protected void outputFO(T reportOutput) {
		reportOutput.output(startReportTemplate, new ReportProps(reportOutput.getFormat()));
		for (ReportComponent reportComponent : getComponents()) {
			reportComponent.output(reportOutput);
		}
		reportOutput.output(endReportTemplate);
	}
}
