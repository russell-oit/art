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
import java.io.Serializable;
import java.util.List;

/**
 * Options for column naming for template reports
 *
 * @author Timothy Anyona
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemplateResultOptions implements Serializable {

	private static final long serialVersionUID = 1L;
	private boolean useLowerCaseProperties = false;
	private boolean useColumnLabels = true;
	private List<String> files;

	/**
	 * @return the useLowerCaseProperties
	 */
	public boolean isUseLowerCaseProperties() {
		return useLowerCaseProperties;
	}

	/**
	 * @param useLowerCaseProperties the useLowerCaseProperties to set
	 */
	public void setUseLowerCaseProperties(boolean useLowerCaseProperties) {
		this.useLowerCaseProperties = useLowerCaseProperties;
	}

	/**
	 * @return the useColumnLabels
	 */
	public boolean isUseColumnLabels() {
		return useColumnLabels;
	}

	/**
	 * @param useColumnLabels the useColumnLabels to set
	 */
	public void setUseColumnLabels(boolean useColumnLabels) {
		this.useColumnLabels = useColumnLabels;
	}

	/**
	 * @return the files
	 */
	public List<String> getFiles() {
		return files;
	}

	/**
	 * @param files the files to set
	 */
	public void setFiles(List<String> files) {
		this.files = files;
	}
}
