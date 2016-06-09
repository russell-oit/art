/*
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates output using the freemarker library
 *
 * @author Timothy Anyona
 */
public class FreemarkerOutput {

	private static final Logger logger = LoggerFactory.getLogger(FreemarkerOutput.class);

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
