/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.output;

import art.report.Report;
import art.reportoptions.TemplateResultOptions;
import art.reportparameter.ReportParameter;
import art.servlets.Config;
import art.utils.ArtUtils;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates output using velocity templates
 *
 * @author Timothy Anyona
 */
public class VelocityOutput {

	private static final Logger logger = LoggerFactory.getLogger(VelocityOutput.class);

	private String contextPath;
	private Locale locale;

	/**
	 * @return the locale
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @param locale the locale to set
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * @return the contextPath
	 */
	public String getContextPath() {
		return contextPath;
	}

	/**
	 * @param contextPath the contextPath to set
	 */
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	/**
	 * Generates output, updating the writer with the final output
	 *
	 * @param report the report to use, not null
	 * @param writer the writer to output to, not null
	 * @param rs the resultset containing report data, not null
	 * @param reportParams the report parameters
	 * @throws java.sql.SQLException
	 * @throws java.io.IOException
	 */
	public void generateOutput(Report report, Writer writer, ResultSet rs,
			List<ReportParameter> reportParams) throws SQLException, IOException {

		Objects.requireNonNull(rs, "resultset must not be null");

		//set variables to be passed to velocity
		Map<String, Object> variables = new HashMap<>();

		//pass report parameters
		if (reportParams != null) {
			for (ReportParameter reportParam : reportParams) {
				String paramName = reportParam.getParameter().getName();
				variables.put(paramName, reportParam);
			}

			variables.put("params", reportParams);
		}

		TemplateResultOptions templateResultOptions;
		String options = report.getOptions();
		if (StringUtils.isBlank(options)) {
			templateResultOptions = new TemplateResultOptions();
		} else {
			templateResultOptions = ArtUtils.jsonToObject(options, TemplateResultOptions.class);
		}

		//pass report data
		boolean useLowerCaseProperties = templateResultOptions.isUseLowerCaseProperties();
		boolean useColumnLabels = templateResultOptions.isUseColumnLabels();
		RowSetDynaClass rsdc = new RowSetDynaClass(rs, useLowerCaseProperties, useColumnLabels);
		variables.put("results", rsdc.getRows());

		generateOutput(report, writer, variables);
	}

	/**
	 * Generates output, updating the writer with the final output
	 *
	 * @param report the report to use, not null
	 * @param writer the writer to output to, not null
	 * @param data the data to be passed to the template
	 * @throws java.io.IOException
	 */
	public void generateOutput(Report report, Writer writer,
			Map<String, Object> data) throws IOException {

		logger.debug("Entering generateOutput: report={}", report);

		Objects.requireNonNull(report, "report must not be null");
		Objects.requireNonNull(writer, "writer must not be null");

		if (data == null) {
			data = new HashMap<>();
		}

		data.put("contextPath", contextPath);
		String artBaseUrl = Config.getSettings().getArtBaseUrl();
		data.put("artBaseUrl", artBaseUrl);
		data.put("locale", locale);

		String templateFileName = report.getTemplate();
		String templatesPath = Config.getTemplatesPath();
		String fullTemplateFileName = templatesPath + templateFileName;

		logger.debug("templateFileName='{}'", templateFileName);

		//need to explicitly check if template file is empty string
		//otherwise file.exists() will return true because fullTemplateFileName will just have the directory name
		if (StringUtils.isBlank(templateFileName)) {
			throw new IllegalArgumentException("Template file not specified");
		}

		//check if template file exists
		File templateFile = new File(fullTemplateFileName);
		if (!templateFile.exists()) {
			throw new IllegalStateException("Template file not found: " + templateFileName);
		}

		//create output
		VelocityContext ctx = new VelocityContext(data);

		VelocityEngine velocityEngine = Config.getVelocityEngine();
		Template template = velocityEngine.getTemplate(fullTemplateFileName, "UTF-8");
		template.merge(ctx, writer);
		writer.flush();
	}

}
