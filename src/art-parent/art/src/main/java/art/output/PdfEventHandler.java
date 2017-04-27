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

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Event handler for itext events
 *
 * @author Timothy Anyona
 */
public class PdfEventHandler extends PdfPageEventHelper {

	//http://developers.itextpdf.com/examples/itext-action-second-edition/chapter-5#225-moviecountries1.java

	@Override
	public void onEndPage(PdfWriter writer, Document document) {
		Rectangle pageSize = document.getPageSize();
		ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT,
				new Phrase(String.format("%d", writer.getPageNumber())),
				pageSize.getRight(72), pageSize.getBottom(72), 0);
	}

}
