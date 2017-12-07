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
package art.output;

import art.report.Report;
import art.reportparameter.ReportParameter;
import art.servlets.Config;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Generates output using the thymeleaf library
 *
 * @author Timothy Anyona
 */
public class ThymeleafOutput {

	private static final Logger logger = LoggerFactory.getLogger(ThymeleafOutput.class);

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

		//set variables to be passed to thymeleaf
		Map<String, Object> variables = new HashMap<>();

		//pass report parameters
		if (reportParams != null) {
			for (ReportParameter reportParam : reportParams) {
				String paramName = reportParam.getParameter().getName();
				variables.put(paramName, reportParam);
			}

			variables.put("params", reportParams);
		}

		//pass report data
		boolean useLowerCaseProperties = false;
		boolean useColumnLabels = true;
		RowSetDynaClass rsdc = new RowSetDynaClass(rs, useLowerCaseProperties, useColumnLabels);
		variables.put("results", rsdc.getRows());

		generateOutput(report, writer, variables);
	}
	
		/**
	 * Generates output, updating the writer with the final output
	 *
	 * @param report the report to use, not null
	 * @param writer the writer to output to, not null
	 * @param variables the variables to be passed to the template
	 * @throws java.io.IOException
	 */
	public void generateOutput(Report report, Writer writer,
			Map<String, Object> variables) throws IOException {
		
		logger.debug("Entering generateOutput: report={}", report);

		Objects.requireNonNull(report, "report must not be null");
		Objects.requireNonNull(writer, "writer must not be null");
		
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
		Context ctx = new Context();
		ctx.setVariables(variables);

		TemplateEngine templateEngine = Config.getThymeleafReportTemplateEngine();
		templateEngine.process(templateFileName, ctx, writer);
		writer.flush();
	}
}
