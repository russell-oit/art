/*
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Enum for report formats
 *
 * @author Timothy Anyona
 */
public enum ReportFormat {

	html("html"), htmlPlain("htmlPlain"), htmlFancy("htmlFancy"), htmlGrid("htmlGrid"),
	htmlDataTable("htmlDataTable"), xls("xls"), xlsZip("xlsZip"), xlsx("xlsx"),
	pdf("pdf"), slk("slk"), slkZip("slkZip"), tsv("tsv"), tsvZip("tsvZip"),
	tsvGz("tsvGz"), xml("xml"), rss20("rss20"), png("png");
	private final String value;

	private ReportFormat(String value) {
		this.value = value;
	}

	/**
	 * Get enum value
	 *
	 * @return
	 */
	public String getValue() {
		return value;
	}

	public String getFilenameExtension() {
		switch (this) {
			case htmlPlain:
				return "html";
			case xls:
				return "xls";
			case xlsx:
				return "xlsx";
			case pdf:
				return "pdf";
			case slk:
				return "slk";
			case tsv:
				return "tsv";
			case tsvGz:
				return "tsv.gz";
			case xlsZip:
			case slkZip:
			case tsvZip:
				return "zip";
			case png:
				return "png";
			default:
				throw new IllegalStateException("Report format does not generate files: " + value);
		}
	}

	public String getDirectOutputClassName() {
		final String PACKAGE_NAME = "art.output.";

		switch (this) {
			case htmlPlain:
				return PACKAGE_NAME + "HtmlPlainOutput";
			case htmlFancy:
				return PACKAGE_NAME + "HtmlFancyOutput";
			case htmlGrid:
				return PACKAGE_NAME + "HtmlGridOutput";
			case htmlDataTable:
				return PACKAGE_NAME + "HtmlDataTableOutput";
			case xls:
			case xlsZip:
				return PACKAGE_NAME + "XlsOutput";
			case xlsx:
				return PACKAGE_NAME + "XlsxOutput";
			case pdf:
				return PACKAGE_NAME + "PdfOutput";
			case slk:
			case slkZip:
				return PACKAGE_NAME + "SlkOutput";
			case tsv:
			case tsvZip:
			case tsvGz:
				return PACKAGE_NAME + "TsvOutput";
			case xml:
				return PACKAGE_NAME + "XmlOutput";
			case rss20:
				return PACKAGE_NAME + "Rss20Output";
			default:
				return null;
		}
	}

	/**
	 * Get a list of all enum values
	 *
	 * @return
	 */
	public static List<ReportFormat> list() {
		//use a new list as Arrays.asList() returns a fixed-size list. can't add or remove from it
		List<ReportFormat> items = new ArrayList<>();
		items.addAll(Arrays.asList(values()));
		return items;
	}

	/**
	 * Convert a value to an enum. If the conversion fails, Active is returned
	 *
	 * @param value
	 * @return
	 */
	public static ReportFormat toEnum(String value) {
		for (ReportFormat v : values()) {
			if (v.value.equalsIgnoreCase(value)) {
				return v;
			}
		}
		throw new IllegalArgumentException("Invalid report format: " + value);
	}

	/**
	 * Get enum description. In case description needs to be different from
	 * internal value
	 *
	 * @return
	 */
	public String getDescription() {
		return value;
	}

	/**
	 * Get description message string for use in the user interface.
	 *
	 * @return
	 */
	public String getLocalizedDescription() {
		return "reportFormat.option." + value;
	}

}
