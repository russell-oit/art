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
import com.lowagie.text.Chunk;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfCopy.PageStamp;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import java.io.File;
import java.io.FileOutputStream;
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

		//http://itext.2136553.n4.nabble.com/Merging-Problem-td3177394.html
		//https://stackoverflow.com/questions/34818288/itext-2-1-7-pdfcopy-addpagepage-cant-find-page-reference
		//http://tutorialspointexamples.com/itext-merge-pdf-files-in-java/
		//https://stackoverflow.com/questions/23062345/function-that-can-use-itext-to-concatenate-merge-pdfs-together-causing-some
		if (CollectionUtils.isNotEmpty(reportFileNames)) {
			//pdfcopy with throw an error if no pages are added
			File outputFile = new File(outputFileName);
			com.lowagie.text.Document doc = new com.lowagie.text.Document();
			FileOutputStream outputStream = new FileOutputStream(outputFile);
			PdfCopy copy = new PdfCopy(doc, outputStream);
			doc.open();

			//https://dkbalachandar.wordpress.com/2016/06/09/itext-pdf-page-header-with-title-and-number/
			Rectangle pageSize = PageSize.A4;
			float x = pageSize.getRight(72);
			float y = pageSize.getBottom(72);
			int pageCount = 0;

			for (String reportFileName : reportFileNames) {
				PdfReader reader = new PdfReader(reportFileName);
				int totalPages = reader.getNumberOfPages();
				for (int i = 1; i <= totalPages; i++) {
					PdfImportedPage page = copy.getImportedPage(reader, i);

					//add page numbers
					pageCount++;
					PageStamp stamp = copy.createPageStamp(page);
					Chunk chunk = new Chunk(String.format("%d", pageCount));
					if (i == 1) {
						chunk.setLocalDestination("p" + pageCount);
					}
					//http://developers.itextpdf.com/examples/stampingcontentexistingpdfsitext5/headerandfooterexamples
					ColumnText.showTextAligned(stamp.getUnderContent(),
							com.lowagie.text.Element.ALIGN_RIGHT, new Phrase(chunk),
							x, y, 0);
					stamp.alterContents();

					copy.addPage(page);
				}
			}
			doc.close();

			for (String reportFileName : reportFileNames) {
				File reportFile = new File(reportFileName);
				FileUtils.deleteQuietly(reportFile);
			}

			PdfHelper pdfHelper = new PdfHelper();
			pdfHelper.addProtections(dashboardReport, outputFileName);
		}
	}
}
