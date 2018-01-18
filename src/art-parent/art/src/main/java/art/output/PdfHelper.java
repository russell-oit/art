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

import art.enums.PageOrientation;
import art.report.Report;
import art.reportoptions.PdfOptions;
import art.servlets.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.FontSelector;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;

/**
 * Provides common methods and variables used in pdf generation
 *
 * @author Timothy Anyona
 */
public class PdfHelper {

	public static final String PDF_AUTHOR_ART = "ART - http://art.sourceforge.net";
	public static final float HEADER_GRAY = 0.9f;
	public static final float CELL_PADDING_LEFT = 5f;
	public static final float CELL_PADDING_RIGHT = 5f;

	/**
	 * Adds page numbers to a pdf document
	 *
	 * @param document the pdf document
	 */
	public void addPageNumbers(Document document) {
		HeaderFooter footer = new HeaderFooter(new Phrase(""), true);
		footer.setAlignment(Element.ALIGN_RIGHT);
		footer.setBorder(Rectangle.NO_BORDER);
		document.setFooter(footer);
	}

	/**
	 * Returns the page size to use for the document
	 *
	 * @param report the report, not null
	 * @return the page size to use
	 * @throws IllegalArgumentException
	 */
	public Rectangle getPageSize(Report report) throws IllegalArgumentException {
		Objects.requireNonNull(report, "report must not be null");

		Rectangle pageSize;

		PageOrientation pageOrientation = report.getPageOrientation();

		switch (pageOrientation) {
			case Portrait:
				pageSize = PageSize.A4;
				break;
			case Landscape:
				pageSize = PageSize.A4.rotate();
				break;
			default:
				throw new IllegalArgumentException("Unexpected page orientation: " + pageOrientation);
		}

		return pageSize;
	}

	/**
	 * Sets font selector objects to be used for body text and header text
	 *
	 * @param body the font selector for body text, not null
	 * @param header the font selector for header text, not null
	 */
	public void setFontSelectors(FontSelector body, FontSelector header) {
		//use fontselector and potentially custom fonts with specified encoding
		//to enable display of more non-ascii characters
		//first font added to selector wins

		Objects.requireNonNull(body, "body must not be null");
		Objects.requireNonNull(header, "header must not be null");

		//use custom font if defined			
		if (Config.isUseCustomPdfFont()) {
			String fontName = Config.getSettings().getPdfFontName();
			String encoding = Config.getSettings().getPdfFontEncoding();
			boolean embedded = Config.getSettings().isPdfFontEmbedded();

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

	/**
	 * Adds protections to a pdf file, including user and owner passwords, and
	 * whether one can print or copy contents
	 *
	 * @param report the report object for the report associated with the file.
	 * The repot object contains pdf options e.g. whether one can print
	 * @param fullOutputFileName the full file path to the pdf file
	 * @param dynamicOpenPassword dynamic open password
	 * @param dynamicModifyPassword dynamic modify password
	 * @throws java.io.IOException
	 */
	public void addProtections(Report report, String fullOutputFileName,
			String dynamicOpenPassword, String dynamicModifyPassword) throws IOException {
		//https://www.tutorialspoint.com/pdfbox/
		//https://self-learning-java-tutorial.blogspot.co.ke/2016/03/pdfbox-encrypt-password-protect-pdf.html
		//https://pdfbox.apache.org/2.0/cookbook/encryption.html

		Objects.requireNonNull(report, "report must not be null");

		File file = new File(fullOutputFileName);
		if (!file.exists()) {
			return;
		}

		String userPassword;
		String reportOpenPassword = report.getOpenPassword();
		if (StringUtils.isEmpty(reportOpenPassword)) {
			userPassword = dynamicOpenPassword;
		} else {
			userPassword = reportOpenPassword;
		}

		if (userPassword == null) {
			userPassword = "";
		}

		String ownerPassword;
		String reportModifyPassword = report.getModifyPassword();
		if (StringUtils.isEmpty(reportModifyPassword)) {
			ownerPassword = dynamicModifyPassword;
		} else {
			ownerPassword = reportModifyPassword;
		}

		if (ownerPassword == null) {
			ownerPassword = "";
		}

		String options = report.getOptions();
		PdfOptions pdfOptions;
		if (StringUtils.isBlank(options)) {
			pdfOptions = new PdfOptions();
		} else {
			ObjectMapper mapper = new ObjectMapper();
			pdfOptions = mapper.readValue(options, PdfOptions.class);
		}

		if (StringUtils.equals(userPassword, "") && StringUtils.equals(ownerPassword, "")
				&& pdfOptions.isPdfCanPrint() && pdfOptions.isPdfCanCopyContent()
				&& pdfOptions.isPdfCanModify()) {
			//nothing to secure
			return;
		}

		try (PDDocument doc = PDDocument.load(file)) {
			AccessPermission ap = new AccessPermission();

			ap.setCanPrint(pdfOptions.isPdfCanPrint());
			ap.setCanExtractContent(pdfOptions.isPdfCanCopyContent());
			ap.setCanModify(pdfOptions.isPdfCanModify());

			// Owner password (to open the file with all permissions)
			// User password (to open the file but with restricted permissions)
			StandardProtectionPolicy spp = new StandardProtectionPolicy(ownerPassword, userPassword, ap);
			int keyLength = pdfOptions.getKeyLength();
			spp.setEncryptionKeyLength(keyLength);
			spp.setPreferAES(pdfOptions.isPreferAes());
			spp.setPermissions(ap);
			doc.protect(spp);

			doc.save(fullOutputFileName);
		}
	}

}
