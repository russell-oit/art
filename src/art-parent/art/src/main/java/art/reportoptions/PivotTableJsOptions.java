/*
 * ART. A Reporting Tool.
 * Copyright (C) 2018 Enrico Liboni <eliboni@users.sf.net>
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

import java.util.Map;

/**
 * Report options for pivottable.js reports
 *
 * @author Timothy Anyona
 */
public class PivotTableJsOptions extends CsvServerOptions {

	private static final long serialVersionUID = 1L;
	private String cssFile;
	private Map<String, Object> config;
	private String template;

	/**
	 * @return the cssFile
	 */
	public String getCssFile() {
		return cssFile;
	}

	/**
	 * @param cssFile the cssFile to set
	 */
	public void setCssFile(String cssFile) {
		this.cssFile = cssFile;
	}

	/**
	 * @return the config
	 */
	public Map<String, Object> getConfig() {
		return config;
	}

	/**
	 * @param config the config to set
	 */
	public void setConfig(Map<String, Object> config) {
		this.config = config;
	}

	/**
	 * @return the template
	 */
	public String getTemplate() {
		return template;
	}

	/**
	 * @param template the template to set
	 */
	public void setTemplate(String template) {
		this.template = template;
	}

}
