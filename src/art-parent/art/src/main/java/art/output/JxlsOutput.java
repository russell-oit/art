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

import art.dbutils.DatabaseUtils;
import art.enums.ReportType;
import art.report.Report;
import art.reportoptions.JxlsOptions;
import art.reportoptions.TemplateResultOptions;
import art.reportparameter.ReportParameter;
import art.runreport.RunReportHelper;
import art.servlets.Config;
import art.utils.ArtUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jxls.area.Area;
import org.jxls.builder.AreaBuilder;
import org.jxls.builder.xml.XmlAreaBuilder;
import org.jxls.common.Context;
import org.jxls.transform.Transformer;
import org.jxls.util.JxlsHelper;
import org.jxls.util.TransformerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates formatted excel workbooks using the jxls library
 *
 * @author Timothy Anyona
 */
public class JxlsOutput {

	private static final Logger logger = LoggerFactory.getLogger(JxlsOutput.class);

	private ResultSet resultSet;
	private Locale locale;
	private String dynamicOpenPassword;
	private String dynamicModifyPassword;
	private Object data;

	/**
	 * @return the data
	 */
	public Object getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(Object data) {
		this.data = data;
	}

	/**
	 * @return the dynamicModifyPassword
	 */
	public String getDynamicModifyPassword() {
		return dynamicModifyPassword;
	}

	/**
	 * @param dynamicModifyPassword the dynamicModifyPassword to set
	 */
	public void setDynamicModifyPassword(String dynamicModifyPassword) {
		this.dynamicModifyPassword = dynamicModifyPassword;
	}

	/**
	 * @return the dynamicOpenPassword
	 */
	public String getDynamicOpenPassword() {
		return dynamicOpenPassword;
	}

	/**
	 * @param dynamicOpenPassword the dynamicOpenPassword to set
	 */
	public void setDynamicOpenPassword(String dynamicOpenPassword) {
		this.dynamicOpenPassword = dynamicOpenPassword;
	}

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
	 * @param resultSet the resultSet to set
	 */
	public void setResultSet(ResultSet resultSet) {
		this.resultSet = resultSet;
	}

	/**
	 * Generates report output
	 *
	 * @param report the report to use, not null
	 * @param reportParams the report parameters, not null
	 * @param outputFileName the full path of the output file, not null
	 * @throws java.sql.SQLException
	 * @throws java.io.IOException
	 * @throws org.apache.poi.openxml4j.exceptions.InvalidFormatException
	 * @throws java.security.GeneralSecurityException
	 */
	public void generateReport(Report report, List<ReportParameter> reportParams,
			String outputFileName) throws SQLException, IOException,
			InvalidFormatException, GeneralSecurityException {

		logger.debug("Entering generateReport");

		Objects.requireNonNull(report, "report must not be null");
		Objects.requireNonNull(reportParams, "reportParams must not be null");
		Objects.requireNonNull(outputFileName, "outputFileName must not be null");

		Connection conn = null;

		try {
			String templateFileName = report.getTemplate();
			String templatesPath = Config.getTemplatesPath();
			String fullTemplateFileName = templatesPath + templateFileName;

			logger.debug("templateFileName='{}'", templateFileName);

			//need to explicitly check if template file is empty string
			//otherwise file.exists() will return true because fullTemplateFileName will just have the directory name
			if (StringUtils.isBlank(templateFileName)) {
				throw new IllegalArgumentException("Template file not specified");
			}

			File templateFile = new File(fullTemplateFileName);
			if (!templateFile.exists()) {
				throw new IllegalStateException("Template file not found: " + fullTemplateFileName);
			}

			//set objects to be passed to jxls
			Context context = new Context();

			//pass report parameters
			for (ReportParameter reportParam : reportParams) {
				String paramName = reportParam.getParameter().getName();
				context.putVar(paramName, reportParam);
			}

			context.putVar("params", reportParams);
			context.putVar("locale", locale);

			String options = report.getOptions();

			JxlsOptions jxlsOptions;
			if (StringUtils.isBlank(options)) {
				jxlsOptions = new JxlsOptions();
			} else {
				jxlsOptions = ArtUtils.jsonToObject(options, JxlsOptions.class);
			}

			String areaConfigFilename = jxlsOptions.getAreaConfigFile();
			String fullAreaConfigFilename = null;

			if (StringUtils.isNotBlank(areaConfigFilename)) {
				fullAreaConfigFilename = templatesPath + areaConfigFilename;
				File areaConfigFile = new File(fullAreaConfigFilename);
				if (!areaConfigFile.exists()) {
					throw new IllegalStateException("Area config file not found: " + fullAreaConfigFilename);
				}
			}

			TemplateResultOptions templateResultOptions;
			if (StringUtils.isBlank(options)) {
				templateResultOptions = new TemplateResultOptions();
			} else {
				templateResultOptions = ArtUtils.jsonToObject(options, TemplateResultOptions.class);
			}

			RunReportHelper runReportHelper = new RunReportHelper();
			ReportType reportType = report.getReportType();
			if (reportType == ReportType.JxlsTemplate) {
				conn = runReportHelper.getEffectiveReportDatasource(report, reportParams);
				ArtJxlsJdbcHelper jdbcHelper = new ArtJxlsJdbcHelper(conn, templateResultOptions);
				context.putVar("jdbc", jdbcHelper);
			} else {
				if (data == null) {
					boolean useLowerCaseProperties = templateResultOptions.isUseLowerCaseProperties();
					boolean useColumnLabels = templateResultOptions.isUseColumnLabels();
					RowSetDynaClass rsdc = new RowSetDynaClass(resultSet, useLowerCaseProperties, useColumnLabels);
					context.putVar("results", rsdc.getRows());
				} else {
					context.putVar("results", data);
				}
			}

			process(fullTemplateFileName, outputFileName, context, fullAreaConfigFilename, jxlsOptions);

			String extension = FilenameUtils.getExtension(templateFileName);
			if (!StringUtils.equalsIgnoreCase(extension, "xls")) {
				//set modify password
				String modifyPassword = runReportHelper.getEffectiveModifyPassword(report, dynamicModifyPassword);

				if (StringUtils.isNotEmpty(modifyPassword)) {
					//https://poi.apache.org/spreadsheet/quick-guide.html#ReadWriteWorkbook
					//https://stackoverflow.com/questions/17556108/open-existing-xls-in-apache-poi
					//https://stackoverflow.com/questions/20340915/how-to-go-though-the-sheets-of-a-workbook-with-apache-poi
					try (InputStream is = new FileInputStream(outputFileName)) {
						//use inputstream instead of File. Using File results in an exception on wb.write()
						Workbook wb = WorkbookFactory.create(is);
						for (int i = 0; i < wb.getNumberOfSheets(); i++) {
							Sheet sheet = wb.getSheetAt(i);
							sheet.protectSheet(modifyPassword);
						}

						try (FileOutputStream fout = new FileOutputStream(outputFileName)) {
							wb.write(fout);
						}
					}
				}

				//set open password
				String openPassword = runReportHelper.getEffectiveOpenPassword(report, dynamicOpenPassword);

				if (StringUtils.isNotEmpty(openPassword)) {
					PoiUtils.addOpenPassword(openPassword, outputFileName);
				}
			}
		} finally {
			DatabaseUtils.close(conn);
		}
	}

	/**
	 * Processes the jxls template
	 *
	 * @param fullTemplateFileName the path of the template file
	 * @param outputFileName the path of the output file
	 * @param context the context
	 * @param fullAreaConfigFilename the full path of the area config file. null
	 * if not using xml config
	 * @param options jxls options
	 * @throws IOException
	 */
	private void process(String fullTemplateFileName, String outputFileName,
			Context context, String fullAreaConfigFilename,
			JxlsOptions options) throws IOException {

		try (InputStream is = new FileInputStream(fullTemplateFileName)) {
			try (OutputStream os = new FileOutputStream(outputFileName)) {
				if (StringUtils.isBlank(fullAreaConfigFilename)) {
					//http://jxls.sourceforge.net/samples/multi_sheet_markup_demo.html
					JxlsHelper jxlsHelper = JxlsHelper.getInstance();
//					if (options.isUseStandardFormulaProcessor()) {
//						jxlsHelper.setUseFastFormulaProcessor(false);
//					}
					jxlsHelper.processTemplate(is, os, context);
				} else {
					processUsingXmlConfig(fullAreaConfigFilename, is, os, context, options);
				}
			}
		}
	}

	/**
	 * Processes template using xml config to define areas
	 *
	 * @param fullAreaConfigFilename full file name of config file, including
	 * path
	 * @param is source template input stream
	 * @param os target file output stream
	 * @param context context
	 * @param options jxls options
	 * @throws IOException
	 */
	private void processUsingXmlConfig(String fullAreaConfigFilename,
			final InputStream is, final OutputStream os, Context context,
			JxlsOptions options) throws IOException {

		//http://jxls.sourceforge.net/samples/object_collection_xmlbuilder.html
		Transformer transformer = TransformerFactory.createTransformer(is, os);
		try (InputStream configInputStream = new FileInputStream(fullAreaConfigFilename)) {
			AreaBuilder areaBuilder = new XmlAreaBuilder(configInputStream, transformer);
			List<Area> xlsAreaList = areaBuilder.build();
			for (Area xlsArea : xlsAreaList) {
				xlsArea.applyAt(xlsArea.getStartCellRef(), context);
				//http://jxls.sourceforge.net/reference/formulas.html
//				if (options.isUseStandardFormulaProcessor()) {
//					xlsArea.setFormulaProcessor(new StandardFormulaProcessor());
//				}
				xlsArea.processFormulas();
			}
			transformer.write();
		}
	}
}
