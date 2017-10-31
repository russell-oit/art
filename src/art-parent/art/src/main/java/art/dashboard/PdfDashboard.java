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
package art.dashboard;

import art.enums.ReportFormat;
import art.enums.ReportType;
import art.output.PdfHelper;
import art.report.Report;
import art.report.ReportService;
import art.reportparameter.ReportParameter;
import art.runreport.ParameterProcessorResult;
import art.runreport.ReportOutputGenerator;
import art.runreport.ReportRunner;
import art.runreport.RunReportHelper;
import art.servlets.Config;
import art.user.User;
import art.utils.FilenameHelper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Generates pdf output for dashboard reports
 *
 * @author Timothy Anyona
 */
public class PdfDashboard {

	private static final Logger logger = LoggerFactory.getLogger(PdfDashboard.class);

	public static void generatePdf(ParameterProcessorResult paramProcessorResult,
			Report dashboardReport, User user, Locale locale, String outputFileName,
			MessageSource messageSource) throws Exception {

		logger.debug("Entering generatePdf: dashboardReport={}, user={}, locale={}, outputFileName='{}'",
				dashboardReport, user, locale, outputFileName);

		Map<String, ReportParameter> reportParamsMap = paramProcessorResult.getReportParamsMap();

		String dashboardXml = dashboardReport.getReportSource();
		logger.debug("dashboardXml='{}'", dashboardXml);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(dashboardXml)));
		Element rootNode = document.getDocumentElement();

		XPath xPath = XPathFactory.newInstance().newXPath();

		List<Integer> reportIds = new ArrayList<>();

		ReportType dashboardReportType = dashboardReport.getReportType();
		if (dashboardReportType == ReportType.Dashboard) {
			NodeList objectIdNodes = (NodeList) xPath.evaluate("COLUMN/PORTLET/OBJECTID", rootNode, XPathConstants.NODESET);
			//http://viralpatel.net/blogs/java-xml-xpath-tutorial-parse-xml/
			for (int i = 0; i < objectIdNodes.getLength(); i++) {
				String objectIdString = objectIdNodes.item(i).getFirstChild().getNodeValue();
				reportIds.add(Integer.parseInt(objectIdString));
			}

			NodeList queryIdNodes = (NodeList) xPath.evaluate("COLUMN/PORTLET/QUERYID", rootNode, XPathConstants.NODESET);
			for (int i = 0; i < queryIdNodes.getLength(); i++) {
				String queryIdString = queryIdNodes.item(i).getFirstChild().getNodeValue();
				reportIds.add(Integer.parseInt(queryIdString));
			}

			NodeList reportIdNodes = (NodeList) xPath.evaluate("COLUMN/PORTLET/REPORTID", rootNode, XPathConstants.NODESET);
			for (int i = 0; i < reportIdNodes.getLength(); i++) {
				String reportIdString = reportIdNodes.item(i).getFirstChild().getNodeValue();
				reportIds.add(Integer.parseInt(reportIdString));
			}
		} else if (dashboardReportType == ReportType.GridstackDashboard) {
			NodeList objectIdNodes = (NodeList) xPath.evaluate("ITEM/OBJECTID", rootNode, XPathConstants.NODESET);
			//http://viralpatel.net/blogs/java-xml-xpath-tutorial-parse-xml/
			for (int i = 0; i < objectIdNodes.getLength(); i++) {
				String objectIdString = objectIdNodes.item(i).getFirstChild().getNodeValue();
				reportIds.add(Integer.parseInt(objectIdString));
			}

			NodeList queryIdNodes = (NodeList) xPath.evaluate("ITEM/QUERYID", rootNode, XPathConstants.NODESET);
			for (int i = 0; i < queryIdNodes.getLength(); i++) {
				String queryIdString = queryIdNodes.item(i).getFirstChild().getNodeValue();
				reportIds.add(Integer.parseInt(queryIdString));
			}

			NodeList reportIdNodes = (NodeList) xPath.evaluate("ITEM/REPORTID", rootNode, XPathConstants.NODESET);
			for (int i = 0; i < reportIdNodes.getLength(); i++) {
				String reportIdString = reportIdNodes.item(i).getFirstChild().getNodeValue();
				reportIds.add(Integer.parseInt(reportIdString));
			}
		}

		List<String> reportFileNames = new ArrayList<>();

		ReportFormat reportFormat = ReportFormat.pdf;

		ReportService reportService = new ReportService();

		for (Integer reportId : reportIds) {
			Report report = reportService.getReport(reportId);
			ReportType reportType = report.getReportType();

			if (reportType.isStandardOutput() || reportType.isChart()) {
				ReportRunner reportRunner = new ReportRunner();
				try {
					reportRunner.setUser(user);
					reportRunner.setReport(report);
					reportRunner.setReportParamsMap(reportParamsMap);

					RunReportHelper runReportHelper = new RunReportHelper();
					int resultSetType = runReportHelper.getResultSetType(reportType);

					reportRunner.execute(resultSetType);

					FilenameHelper filenameHelper = new FilenameHelper();
					String baseFileName = filenameHelper.getBaseFilename(report, locale);
					String exportPath = Config.getReportsExportPath();
					String extension = filenameHelper.getFilenameExtension(report, reportType, reportFormat);
					String fileName = baseFileName + "." + extension;
					String reportFileName = exportPath + fileName;

					ReportOutputGenerator reportOutputGenerator = new ReportOutputGenerator();

					reportOutputGenerator.setIsJob(true);
					reportOutputGenerator.setPdfPageNumbers(false);

					//use blank passwords when generating individual files to make merge more straightforward
					report.setOpenPassword("");
					report.setModifyPassword("");

					FileOutputStream fos = new FileOutputStream(reportFileName);
					try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(fos, "UTF-8"))) {
						reportOutputGenerator.generateOutput(report, reportRunner,
								reportFormat, locale, paramProcessorResult, writer,
								reportFileName, user, messageSource);
					} finally {
						fos.close();
					}

					reportFileNames.add(reportFileName);
				} finally {
					reportRunner.close();
				}
			}
		}

		if (CollectionUtils.isNotEmpty(reportFileNames)) {
			try {
				mergeFiles(reportFileNames, outputFileName);
			} finally {
				for (String reportFileName : reportFileNames) {
					File reportFile = new File(reportFileName);
					FileUtils.deleteQuietly(reportFile);
				}
			}

			PdfHelper pdfHelper = new PdfHelper();
			pdfHelper.addProtections(dashboardReport, outputFileName);
		}
	}

	/**
	 * Merges pdf files into one pdf file
	 *
	 * @param reportFileNames the full path of the pdf files to merge
	 * @param outputFileName the output file name of the pdf file to generate
	 * @throws IOException
	 */
	private static void mergeFiles(List<String> reportFileNames, String outputFileName) throws IOException {
		//https://stackoverflow.com/questions/3585329/how-to-merge-two-pdf-files-into-one-in-java
		//https://stackoverflow.com/questions/37589590/merge-pdf-files-using-pdfbox
		//https://issues.apache.org/jira/browse/PDFBOX-3188
		if (CollectionUtils.isEmpty(reportFileNames)) {
			return;
		}

		PDFMergerUtility ut = new PDFMergerUtility();
		for (String reportFileName : reportFileNames) {
			ut.addSource(reportFileName);
		}

		ut.setDestinationFileName(outputFileName);
		ut.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

		addPageNumbers(outputFileName);
	}

	/**
	 * Adds page numbers to a pdf file
	 *
	 * @param outputFileName the path to the pdf file
	 * @throws IOException
	 */
	private static void addPageNumbers(String outputFileName) throws IOException {
		//https://stackoverflow.com/questions/16817293/how-to-add-a-page-number-to-the-output-pdf-when-merging-two-pdfs
		//http://www.oodlestechnologies.com/blogs/How-to-Add-Footer-on-Each-Page-of-a-PDF-document-without-iText
		//https://pdfbox.apache.org/2.0/migration.html
		//https://pdfbox.apache.org/docs/2.0.5/javadocs/org/apache/pdfbox/pdmodel/PDPageContentStream.html#newLineAtOffset(float,%20float)
		//https://pdfbox.apache.org/docs/2.0.3/javadocs/org/apache/pdfbox/pdmodel/common/PDRectangle.html
		try (PDDocument doc = PDDocument.load(new File(outputFileName))) {
			PDFont font = PDType1Font.HELVETICA;
			float fontSize = 12f;
			final float RIGHT_MARGIN = 72f;
			final float BOTTOM_MARGIN = 36f;

			int pageNumber = 0;
			for (PDPage page : doc.getPages()) {
				pageNumber++;
				boolean compress = true;
				try (PDPageContentStream footercontentStream = new PDPageContentStream(doc, page, AppendMode.APPEND, compress)) {
					footercontentStream.beginText();
					footercontentStream.setFont(font, fontSize);
					footercontentStream.newLineAtOffset((PDRectangle.A4.getUpperRightX() - RIGHT_MARGIN), (PDRectangle.A4.getLowerLeftY() + BOTTOM_MARGIN));
					footercontentStream.showText(String.valueOf(pageNumber));
					footercontentStream.endText();
				}
			}
			doc.save(outputFileName);
		}
	}
}
