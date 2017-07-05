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
package art.parameter;

import java.util.List;
import java.util.Map;

/**
 * Represents parameter i18n options
 * 
 * @author Timothy Anyona
 */
public class Parameteri18nOptions {
	
	private List<Map<String, String>> label;
	private List<Map<String, String>> helpText;
	private List<Map<String, String>> defaultValue;

	/**
	 * @return the label
	 */
	public List<Map<String, String>> getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(List<Map<String, String>> label) {
		this.label = label;
	}

	/**
	 * @return the helpText
	 */
	public List<Map<String, String>> getHelpText() {
		return helpText;
	}

	/**
	 * @param helpText the helpText to set
	 */
	public void setHelpText(List<Map<String, String>> helpText) {
		this.helpText = helpText;
	}

	/**
	 * @return the defaultValue
	 */
	public List<Map<String, String>> getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @param defaultValue the defaultValue to set
	 */
	public void setDefaultValue(List<Map<String, String>> defaultValue) {
		this.defaultValue = defaultValue;
	}
}
