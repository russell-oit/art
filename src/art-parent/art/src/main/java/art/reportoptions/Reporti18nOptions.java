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
import java.util.Map;

/**
 * Represents report i18n options
 * 
 * @author Timothy Anyona
 */
public class Reporti18nOptions {
	
	private List<Map<String, String>> name;
	private List<Map<String, String>> shortDescription;
	private List<Map<String, String>> description;

	/**
	 * @return the name
	 */
	public List<Map<String, String>> getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(List<Map<String, String>> name) {
		this.name = name;
	}

	/**
	 * @return the shortDescription
	 */
	public List<Map<String, String>> getShortDescription() {
		return shortDescription;
	}

	/**
	 * @param shortDescription the shortDescription to set
	 */
	public void setShortDescription(List<Map<String, String>> shortDescription) {
		this.shortDescription = shortDescription;
	}

	/**
	 * @return the description
	 */
	public List<Map<String, String>> getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(List<Map<String, String>> description) {
		this.description = description;
	}
	
}
