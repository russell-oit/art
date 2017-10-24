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
package art.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents report formats
 *
 * @author Timothy Anyona
 */
public enum ReportFormat {

	html("html"), htmlPlain("htmlPlain"), htmlFancy("htmlFancy"), htmlGrid("htmlGrid"),
	htmlDataTable("htmlDataTable"), xls("xls"), xlsZip("xlsZip"), xlsx("xlsx"),
	pdf("pdf"), docx("docx"), odt("odt"), ods("ods"), pptx("pptx"), slk("slk"), slkZip("slkZip"),
	tsv("tsv"), tsvZip("tsvZip"), tsvGz("tsvGz"), xml("xml"), rss20("rss20"), png("png"),
	json("json"), jsonBrowser("jsonBrowser"), csv("csv");

	private final String value;

	private ReportFormat(String value) {
		this.value = value;
	}

	/**
	 * Returns <code>true</code> if for a tabular report, this format can use
	 * column formatting options
	 *
	 * @return <code>true</code> if for a tabular report, this format can use
	 * column formatting options
	 */
	public boolean isUseColumnFormatting() {
		switch (this) {
			case xls:
			case xlsx:
			case ods:
				return false;
			default:
				return true;
		}
	}

	/**
	 * Returns <code>true</code> if this is a json or jsonBrowser report format
	 *
	 * @return <code>true</code> if this is a json or jsonBrowser report format
	 */
	public boolean isJson() {
		switch (this) {
			case json:
			case jsonBrowser:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns <code>true</code> if this is a html, htmlDataTable, htmlFancy,
	 * htmlGrid or htmlPlain report format
	 *
	 * @return <code>true</code> if this is a html, htmlDataTable, htmlFancy,
	 * htmlGrid or htmlPlain report format
	 */
	public boolean isHtml() {
		switch (this) {
			case html:
			case htmlDataTable:
			case htmlFancy:
			case htmlGrid:
			case htmlPlain:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Return's the file name extension to use for this report format
	 *
	 * @return the file name extension to use for this report format
	 */
	public String getFilenameExtension() {
		switch (this) {
			case html:
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
				return "gz";
			case xlsZip:
			case slkZip:
			case tsvZip:
				return "zip";
			case png:
				return "png";
			case xml:
				return "xml";
			case rss20:
				return "html";
			case docx:
				return "docx";
			case odt:
				return "odt";
			case pptx:
				return "pptx";
			case ods:
				return "ods";
			default:
				return value;
		}
	}

	/**
	 * Returns this enum option's value
	 *
	 * @return this enum option's value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Returns all enum options
	 *
	 * @return all enum options
	 */
	public static List<ReportFormat> list() {
		//use a new list as Arrays.asList() returns a fixed-size list. can't add or remove from it
		List<ReportFormat> items = new ArrayList<>();
		items.addAll(Arrays.asList(values()));
		return items;
	}

	/**
	 * Converts a value to an enum. If the conversion fails,
	 * IllegalArgumentException is thrown
	 *
	 * @param value the value to convert
	 * @return the enum option that corresponds to the value
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
	 * Converts a value to an enum. If the conversion fails, the specified
	 * default is returned
	 *
	 * @param value the value to convert
	 * @param defaultEnum the default enum option to use
	 * @return the enum option that corresponds to the value
	 */
	public static ReportFormat toEnum(String value, ReportFormat defaultEnum) {
		for (ReportFormat v : values()) {
			if (v.value.equalsIgnoreCase(value)) {
				return v;
			}
		}
		return defaultEnum;
	}

	/**
	 * Returns this enum option's description
	 *
	 * @return
	 */
	public String getDescription() {
		return value;
	}

	/**
	 * Returns this enum option's i18n message string
	 *
	 * @return this enum option's i18n message string
	 */
	public String getLocalizedDescription() {
		return "reports.format." + value;
	}
}
