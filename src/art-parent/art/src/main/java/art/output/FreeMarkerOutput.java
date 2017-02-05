/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.output;

import art.report.Report;
import art.reportparameter.ReportParameter;
import art.servlets.Config;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
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

/**
 * Generates output using the freemarker library
 *
 * @author Timothy Anyona
 */
public class FreeMarkerOutput {

	private static final Logger logger = LoggerFactory.getLogger(FreeMarkerOutput.class);

	/**
	 * Generates report output
	 *
	 * @param report the report to use, not null
	 * @param reportParams the report parameters
	 * @param resultSet the resultset containing report data, not null
	 * @param writer the writer to output to, not null
	 * @throws java.sql.SQLException
	 * @throws java.io.IOException
	 * @throws freemarker.template.TemplateException
	 */
	public void generateReport(Report report, List<ReportParameter> reportParams,
			ResultSet resultSet, Writer writer)
			throws SQLException, IOException, TemplateException {

		logger.debug("Entering generateReport");

		Objects.requireNonNull(report, "report must not be null");
		Objects.requireNonNull(resultSet, "resultset must not be null");
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

		Configuration cfg = Config.getFreemarkerConfig();
		Template template = cfg.getTemplate(templateFileName);

		//set objects to be passed to freemarker
		Map<String, Object> data = new HashMap<>();

		//pass report parameters
		if (reportParams != null) {
			for (ReportParameter reportParam : reportParams) {
				String paramName = reportParam.getParameter().getName();
				data.put(paramName, reportParam);
			}
			
			data.put("params", reportParams);
		}

		//pass report data
		boolean useLowerCaseProperties = false;
		boolean useColumnLabels = true;
		RowSetDynaClass rsdc = new RowSetDynaClass(resultSet, useLowerCaseProperties, useColumnLabels);
		data.put("results", rsdc.getRows());

		//create output
		template.process(data, writer);
		writer.flush();
	}
}
