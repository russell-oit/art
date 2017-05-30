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
package art.saiku;

import java.io.Serializable;

/**
 * Represents a saiku report
 * 
 * @author Timothy Anyona
 */
public class SaikuReport implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private int reportId;
	private String name;
	private String shortDescription;
	private String description;

	/**
	 * @return the reportId
	 */
	public int getReportId() {
		return reportId;
	}

	/**
	 * @param reportId the reportId to set
	 */
	public void setReportId(int reportId) {
		this.reportId = reportId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the shortDescription
	 */
	public String getShortDescription() {
		return shortDescription;
	}

	/**
	 * @param shortDescription the shortDescription to set
	 */
	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 29 * hash + this.reportId;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final SaikuReport other = (SaikuReport) obj;
		if (this.reportId != other.reportId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "SaikuReport{" + "name=" + name + '}';
	}
	
	public String getPath(){
		return "/" + name;
	}
	
	public String getId(){
		return "#" + getPath();
	}
	
	public String getType(){
		return "FILE";
	}
	
	public String getFileType(){
		return "saiku";
	}
}
