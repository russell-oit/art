/*
 * Pdf output using itext. 
 * Version 1.0
 * Marios Timotheou

 see http://itextdocs.lowagie.com/examples/com/lowagie/examples/objects/tables/pdfptable/FragmentTable.java
 */
package art.output;

import art.servlets.ArtDBCP;
import art.utils.ArtQueryParam;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate pdf output
 *
 * @author Marios Timotheou
 */
public class pdfOutput implements ArtOutputInterface {

	final static Logger logger = LoggerFactory.getLogger(pdfOutput.class);
	String filename;
	String fullFileName;
	PrintWriter htmlout;
	String queryName;
	String fileUserName;
	int maxRows;
	int columns;
	String y_m_d;
	String h_m_s;
	boolean oddline = true;
	Map<Integer, ArtQueryParam> displayParams;
	Document document;
	PdfPTable table;
	PdfPCell cell;
	PdfWriter pdfout;
	float pos;
	int counter;
	float evengray = 1.0f;
	float oddgray = 0.75f;
	float headergray = 0.5f;
	FontSelector fsBody; //fonts to use for document body
	FontSelector fsHeading; //fonts to use for document title and column headings
	String exportPath;

	/**
	 * Constructor
	 */
	public pdfOutput() {
	}

	@Override
	public String getName() {
		return "Pdf (pdf)";
	}

	@Override
	public String getContentType() {
		return "text/html;charset=utf-8";
	}

	@Override
	public void setExportPath(String s) {
		exportPath = s;
	}

	@Override
	public String getFileName() {
		return fullFileName;
	}

	@Override
	public void setWriter(PrintWriter o) {
		htmlout = o;
	}

	@Override
	public void setQueryName(String s) {
		queryName = s;
	}

	@Override
	public void setFileUserName(String s) {
		fileUserName = s;
	}

	@Override
	public void setMaxRows(int i) {
		maxRows = i;
	}

	@Override
	public void setColumnsNumber(int i) {
		columns = i;
	}

	@Override
	public void setDisplayParameters(Map<Integer, ArtQueryParam> t) {
		displayParams = t;
	}

	@Override
	public void beginHeader() {
		//initialize objects required for output
		initializeOutput();

		table = null;
		cell = null;

		table = new PdfPTable(columns);
		table.getDefaultCell().setBorder(0);

		//use percentage width
		/*
		 * table.setHorizontalAlignment(0); float width =
		 * document.getPageSize().getWidth(); table.setTotalWidth(width - 72);
		 * //end result will have 72pt (1 inch) left and right margins
		 * table.setLockedWidth(true);
		 */

		table.setWidthPercentage(100f); //default is 80%
		table.setHeaderRows(1);

		try {
			Paragraph title = new Paragraph(fsHeading.process(queryName + " - " + y_m_d.replace('_', '-') + " " + h_m_s.replace('_', ':')));
			title.setAlignment(Element.ALIGN_CENTER);
			document.add(title);

			// Output parameter values list
			outputParameters(document, fsBody, displayParams);

		} catch (DocumentException e) {
			logger.error("Error", e);
		}

	}

	public void outputParameters(Document doc, FontSelector fs, Map<Integer, ArtQueryParam> displayParams) {
		// Output parameter values list	

		try {
			if (displayParams != null && displayParams.size() > 0) {

				doc.add(new Paragraph(fs.process("Parameters\n")));

				String[] params = new String[displayParams.size()];
				int index = 0;
				for (Map.Entry<Integer, ArtQueryParam> entry : displayParams.entrySet()) {
					ArtQueryParam param = entry.getValue();
					String paramName = param.getName();
					Object pValue = param.getParamValue();
					String outputString="";

					if (pValue instanceof String) {
						String paramValue = (String) pValue;
						outputString = paramName + ": " + pValue; //default to displaying parameter value

						if (param.usesLov()) {
							//for lov parameters, show both parameter value and display string if any
							Map<String, String> lov = param.getLovValues();
							if (lov != null) {
								//get friendly/display string for this value
								String paramDisplayString = lov.get(paramValue);
								if (!StringUtils.equals(paramValue, paramDisplayString)) {
									//parameter value and display string differ. show both
									outputString = paramName + ": " + paramDisplayString + " (" + paramValue + ")";
								}
							}
						}
					} else if (pValue instanceof String[]) { // multi
						String[] paramValues = (String[]) pValue;
						outputString = paramName + ": " + StringUtils.join(paramValues, ", "); //default to showing parameter values only

						if (param.usesLov()) {
							//for lov parameters, show both parameter value and display string if any
							Map<String, String> lov = param.getLovValues();
							if (lov != null) {
								//get friendly/display string for all the parameter values
								String[] paramDisplayStrings = new String[paramValues.length];
								for (int i = 0; i < paramValues.length; i++) {
									String value = paramValues[i];
									String display = lov.get(value);
									if (!StringUtils.equals(display, value)) {
										//parameter value and display string differ. show both
										paramDisplayStrings[i] = display + " (" + value + ")";
									} else {
										paramDisplayStrings[i] = value;
									}
								}
								outputString = paramName + ": " + StringUtils.join(paramDisplayStrings, ", ");
							}
						}
					}

					params[index] = outputString;
					index++;
				}

				//show parameters in a numbered list
				List list = new List(true, 10);
				//set font to use for the list item numbers
				Font font = new Font(Font.HELVETICA, 8, Font.NORMAL);
				list.setListSymbol(new Chunk("1", font)); //item number will get font from chunk details. chunk string doesn't matter for numbered lists

				//add list items
				int size = params.length - 1;
				for (int i = size; i >= 0; i--) {
					Phrase ph = fs.process(params[i] + ""); //add empty string to prevent NPE if value is null
					ph.setLeading(12); //set spacing before the phrase
					ListItem listItem = new ListItem(ph);
					list.add(listItem);
				}

				doc.add(list);

				doc.add(new Paragraph(fs.process("\n")));
			}
		} catch (Exception e) {
			logger.error("Error", e);
		}

	}

	@Override
	public void addHeaderCell(String s) {
		cell = new PdfPCell(new Paragraph(fsHeading.process(s + ""))); //add empty string to prevent NPE if value is null
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setPaddingLeft(5f);
		cell.setPaddingRight(5f);
		cell.setGrayFill(headergray);
		table.addCell(cell);
	}
	
	@Override
	public void addHeaderCellLeft(String s) {
		addHeaderCell(s);
	}

	@Override
	public void endHeader() {
		//nope;
	}

	@Override
	public void beginLines() {
	}

	@Override
	public void addCellString(String s) {
		cell = new PdfPCell(new Paragraph(fsBody.process(s + ""))); //add empty string to prevent NPE if value is null

		cell.setPaddingLeft(5f);
		cell.setPaddingRight(5f);
		cell.setGrayFill((oddline ? evengray : oddgray));
		table.addCell(cell);
	}

	@Override
	public void addCellDouble(Double d) {
		cell = new PdfPCell(new Paragraph(fsBody.process(d + "")));
		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cell.setPaddingLeft(5f);
		cell.setPaddingRight(5f);
		cell.setGrayFill((oddline ? evengray : oddgray));
		table.addCell(cell);
	}

	@Override
	public void addCellLong(Long l) {
		cell = new PdfPCell(new Paragraph(fsBody.process(l + "")));
		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cell.setPaddingLeft(5f);
		cell.setPaddingRight(5f);
		cell.setGrayFill((oddline ? evengray : oddgray));
		table.addCell(cell);
	}

	/**
	 *
	 * @param i
	 */
	public void addCellInt(int i) {
		cell = new PdfPCell(new Paragraph(fsBody.process(i + "")));
		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cell.setPaddingLeft(10f);
		cell.setPaddingRight(10f);
		cell.setGrayFill((oddline ? evengray : oddgray));
		table.addCell(cell);
	}

	@Override
	public void addCellDate(java.util.Date d) {
		cell = new PdfPCell(new Paragraph(fsBody.process(ArtDBCP.getDateDisplayString(d))));
		cell.setPaddingLeft(5f);
		cell.setPaddingRight(5f);
		cell.setGrayFill((oddline ? evengray : oddgray));
		table.addCell(cell);
	}

	@Override
	public boolean newLine() {
		counter++;

		if (counter % 2 == 0) {
			oddline = true;
		} else {
			oddline = false;
		}

		// split table in smaller pieces in order to save memory:
		// fragment size should come from ArtDBCP servlet, from  web.xml or properties		
		if (counter % 500 == 500 - 1) {
			try {
				document.add(table);
				table.deleteBodyRows();
				table.setSkipFirstHeader(true);
			} catch (DocumentException e) {
				logger.error("Error", e);
			}
		}

		// Check rows number		
		if (counter < maxRows) {
			return true;
		} else {
			//htmlout not needed for scheduled jobs
			if (htmlout != null) {
				htmlout.println("<span style=\"color:red\">Too many rows (>"
						+ maxRows
						+ ")! Data not completed. Please narrow your search!</span>");
			}
			addCellString("Maximum number of rows exceeded! Query not completed.");

			return false;
		}
	}

	@Override
	public void endLines() {
		// flush and close files
		try {
			document.add(new Paragraph(fsBody.process("\n")));
			document.add(table);
			document.close();

			//htmlout not needed for scheduled jobs
			if (htmlout != null) {
				htmlout.println("<p><div align=\"Center\"><table border=\"0\" width=\"90%\">");
				htmlout.println("<tr><td colspan=2 class=\"data\" align=\"center\" >"
						+ "<a  type=\"application/octet-stream\" href=\"../export/" + filename + "\" target=\"_blank\"> "
						+ filename + "</a>"
						+ "</td></tr>");
				htmlout.println("</table></div></p>");
			}
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}

	@Override
	public boolean isShowQueryHeaderAndFooter() {
		return true;
	}

	/**
	 * Set font selector objects to be used for body text and header text Also
	 * used by pdfgraph class
	 *
	 * @param body
	 * @param header
	 */
	public void setFontSelectors(FontSelector body, FontSelector header) {
		//use fontselector and potentially custom fonts with specified encoding to enable display of more non-ascii characters
		//first font added to selector wins

		//use custom font if defined			
		if (ArtDBCP.isUseCustomPdfFont()) {
			String fontName = ArtDBCP.getArtSetting("pdf_font_name");
			String encoding = ArtDBCP.getArtSetting("pdf_font_encoding");
			boolean embedded = ArtDBCP.isPdfFontEmbedded();

			Font bodyFont = FontFactory.getFont(fontName, encoding, embedded);
			bodyFont.setSize(8);
			bodyFont.setStyle(Font.NORMAL);
			body.addFont(bodyFont);

			Font headingFont = FontFactory.getFont(fontName, encoding, embedded);
			headingFont.setSize(10);
			headingFont.setStyle(Font.BOLD);
			header.addFont(headingFont);
		}

		//add default font after custom font			
		body.addFont(FontFactory.getFont(BaseFont.HELVETICA, 8, Font.NORMAL));
		header.addFont(FontFactory.getFont(BaseFont.HELVETICA, 10, Font.BOLD));
	}

	//Build filename for output file
	private void buildOutputFileName() {
		// Build filename										
		Date today = new Date();

		String dateFormat = "yyyy_MM_dd";
		String timeFormat = "HH_mm_ss";

		SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
		y_m_d = dateFormatter.format(today);

		dateFormatter.applyPattern(timeFormat);
		h_m_s = dateFormatter.format(today);

		filename = fileUserName + "-" + queryName + "-" + y_m_d + "-" + h_m_s + ArtDBCP.getRandomString() + ".pdf";
		filename = ArtDBCP.cleanFileName(filename); //replace characters that would make an invalid filename
		fullFileName = exportPath + filename;
	}

	/**
	 * Initialise objects required to generate output
	 */
	private void initializeOutput() {
		buildOutputFileName();

		try {
			Rectangle pageSize;
			switch (Integer.parseInt(ArtDBCP.getArtSetting("page_size"))) {
				case 1:
					pageSize = PageSize.A4;
					break;
				case 2:
					pageSize = PageSize.A4.rotate();
					break;
				case 3:
					pageSize = PageSize.LETTER;
					break;
				case 4:
					pageSize = PageSize.LETTER.rotate();
					break;
				default:
					pageSize = PageSize.A4;
			}
			//set document margins
			//document = new Document(pageSize);
			document = new Document(pageSize, 72, 72, 72, 72); //document with 72pt (1 inch) margins for left, right, top, bottom

			PdfWriter.getInstance(document, new FileOutputStream(fullFileName));
			document.addTitle(queryName);
			document.addAuthor("ART - http://art.sourceforge.net");

			//use fontselector and potentially custom fonts with specified encoding to enable display of more non-ascii characters
			//first font added to selector wins
			fsBody = new FontSelector();
			fsHeading = new FontSelector();
			setFontSelectors(fsBody, fsHeading);

			HeaderFooter footer = new HeaderFooter(new Phrase(""), true);
			footer.setAlignment(Element.ALIGN_CENTER);
			document.setFooter(footer);

			document.open();

		} catch (DocumentException de) {
			logger.error("Error", de);
		} catch (IOException e) {
			logger.error("Error", e);
		}
	}
}
