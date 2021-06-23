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
package art.utils;

import art.enums.ReportType;
import art.servlets.Config;
import java.io.File;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;

/**
 * Provides helper methods to be used within the application
 *
 * @author Timothy Anyona
 */
public class ArtHelper {

	/**
	 * Returns the default show legend option depending on the report type
	 *
	 * @param reportType the report type
	 * @return the default show legend option
	 */
	public boolean getDefaultShowLegendOption(ReportType reportType) {
		if (reportType == ReportType.HeatmapChart) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Returns the default show labels option depending on the report type
	 *
	 * @param reportType the report type
	 * @return the default show labels option
	 */
	public boolean getDefaultShowLabelsOption(ReportType reportType) {
		if (reportType == ReportType.Pie2DChart || reportType == ReportType.Pie3DChart) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Sets the tinymce language attribute if the language file exists
	 * 
	 * @param locale the locale
	 * @param model the model
	 */
	public void setTinymceLanguage(Locale locale, Model model) {
		HttpServletRequest request = null;
		setTinymceLanguage(locale, model, request);
	}

	/**
	 * Sets the tinymce language attribute if the language file exists
	 * 
	 * @param locale the locale
	 * @param request the request
	 */
	public void setTinymceLanguage(Locale locale, HttpServletRequest request) {
		Model model = null;
		setTinymceLanguage(locale, model, request);
	}

	/**
	 * Sets the tinymce language attribute if the language file exists
	 * 
	 * @param locale the locale
	 * @param model the model
	 * @param request the request
	 */
	private void setTinymceLanguage(Locale locale, Model model, HttpServletRequest request) {
		//https://www.tiny.cloud/docs/configure/localization/#language
		String localeString = locale.toString();
		String languageFileName = localeString + ".js";

		String languageFilePath = Config.getJsPath()
				+ "tinymce-5.8.1" + File.separator
				+ "langs" + File.separator
				+ languageFileName;

		File languageFile = new File(languageFilePath);

		if (languageFile.exists()) {
			String tinymceLangAttribute = "tinymceLang";
			if (model != null) {
				model.addAttribute(tinymceLangAttribute, localeString);
			}
			if (request != null) {
				request.setAttribute(tinymceLangAttribute, localeString);
			}
		}
	}

}
