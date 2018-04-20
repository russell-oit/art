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
package art.reportoptions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;

/**
 * Represents report options for datamaps reports
 *
 * @author Timothy Anyona
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatamapsOptions implements Serializable {

	private static final long serialVersionUID = 1L;
	private String width = "500px";
	private String height = "300px";
	private String datamapsJsFile;
	private String dataFile;
	private String dataType = "json";
	private String mapFile;
	private String cssFile;

	/**
	 * @return the width
	 */
	public String getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(String width) {
		this.width = width;
	}

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
	 * @return the datamapsJsFile
	 */
	public String getDatamapsJsFile() {
		return datamapsJsFile;
	}

	/**
	 * @param datamapsJsFile the datamapsJsFile to set
	 */
	public void setDatamapsJsFile(String datamapsJsFile) {
		this.datamapsJsFile = datamapsJsFile;
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
	 * @return the dataType
	 */
	public String getDataType() {
		return dataType;
	}

	/**
	 * @param dataType the dataType to set
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	/**
	 * @return the mapFile
	 */
	public String getMapFile() {
		return mapFile;
	}

	/**
	 * @param mapFile the mapFile to set
	 */
	public void setMapFile(String mapFile) {
		this.mapFile = mapFile;
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
}
