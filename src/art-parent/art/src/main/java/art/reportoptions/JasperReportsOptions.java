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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.List;

/**
 * Options for jasper reports
 * 
 * @author Timothy Anyona
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JasperReportsOptions implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<String> subreports;
	private List<String> files;

	/**
	 * @return the subreports
	 */
	public List<String> getSubreports() {
		return subreports;
	}

	/**
	 * @param subreports the subreports to set
	 */
	public void setSubreports(List<String> subreports) {
		this.subreports = subreports;
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
