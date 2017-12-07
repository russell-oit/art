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
package art.reportoptions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Options for pdf output
 *
 * @author Timothy Anyona
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PdfOptions {

	private boolean pdfCanPrint = true;
	private boolean pdfCanCopyContent = true;
	private boolean pdfCanModify = true;
	private int keyLength = 128; //40, 128 or 256. using 256 requires JCE unlimited strength jurisdiction policy files. https://pdfbox.apache.org/2.0/dependencies.html
	private boolean preferAes = true;

	/**
	 * @return the pdfCanPrint
	 */
	public boolean isPdfCanPrint() {
		return pdfCanPrint;
	}

	/**
	 * @param pdfCanPrint the pdfCanPrint to set
	 */
	public void setPdfCanPrint(boolean pdfCanPrint) {
		this.pdfCanPrint = pdfCanPrint;
	}

	/**
	 * @return the pdfCanCopyContent
	 */
	public boolean isPdfCanCopyContent() {
		return pdfCanCopyContent;
	}

	/**
	 * @param pdfCanCopyContent the pdfCanCopyContent to set
	 */
	public void setPdfCanCopyContent(boolean pdfCanCopyContent) {
		this.pdfCanCopyContent = pdfCanCopyContent;
	}

	/**
	 * @return the pdfCanModify
	 */
	public boolean isPdfCanModify() {
		return pdfCanModify;
	}

	/**
	 * @param pdfCanModify the pdfCanModify to set
	 */
	public void setPdfCanModify(boolean pdfCanModify) {
		this.pdfCanModify = pdfCanModify;
	}

	/**
	 * @return the keyLength
	 */
	public int getKeyLength() {
		return keyLength;
	}

	/**
	 * @param keyLength the keyLength to set
	 */
	public void setKeyLength(int keyLength) {
		this.keyLength = keyLength;
	}

	/**
	 * @return the preferAes
	 */
	public boolean isPreferAes() {
		return preferAes;
	}

	/**
	 * @param preferAes the preferAes to set
	 */
	public void setPreferAes(boolean preferAes) {
		this.preferAes = preferAes;
	}

}
