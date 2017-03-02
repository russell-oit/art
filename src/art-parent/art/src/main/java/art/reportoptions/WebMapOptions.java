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

import java.util.List;

/**
 * Represents report options for leaflet and openlayers reports
 *
 * @author Timothy Anyona
 */
public class WebMapOptions {

	private String height = "400px";
	private String cssFile;
	private String dataFile;
	private List<String> jsFiles;
	private List<String> cssFiles;

	/**
	 * @return the height
	 */
	public String getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(String height) {
		this.height = height;
	}

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
	 * @return the dataFile
	 */
	public String getDataFile() {
		return dataFile;
	}

	/**
	 * @param dataFile the dataFile to set
	 */
	public void setDataFile(String dataFile) {
		this.dataFile = dataFile;
	}

	/**
	 * @return the jsFiles
	 */
	public List<String> getJsFiles() {
		return jsFiles;
	}

	/**
	 * @param jsFiles the jsFiles to set
	 */
	public void setJsFiles(List<String> jsFiles) {
		this.jsFiles = jsFiles;
	}

	/**
	 * @return the cssFiles
	 */
	public List<String> getCssFiles() {
		return cssFiles;
	}

	/**
	 * @param cssFiles the cssFiles to set
	 */
	public void setCssFiles(List<String> cssFiles) {
		this.cssFiles = cssFiles;
	}
}
