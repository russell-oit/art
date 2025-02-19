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
package art.datasource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.Map;

/**
 * Represents datasource options
 *
 * @author Timothy Anyona
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasourceOptions implements Serializable {

	private static final long serialVersionUID = 1L;
	private String limitClause;
	private Integer limit;
	private Map<String, Object> hikariCp;

	/**
	 * @return the limitClause
	 */
	public String getLimitClause() {
		return limitClause;
	}

	/**
	 * @param limitClause the limitClause to set
	 */
	public void setLimitClause(String limitClause) {
		this.limitClause = limitClause;
	}

	/**
	 * @return the limit
	 */
	public Integer getLimit() {
		return limit;
	}

	/**
	 * @param limit the limit to set
	 */
	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	/**
	 * 
	 * @return the hikariCp
	 */
	public Map<String, Object> getHikariCp() {
		return hikariCp;
	}

	/**
	 * 
	 * @param hikariCp the hikariCp to set
	 */
	public void setHikariCp(Map<String, Object> hikariCp) {
		this.hikariCp = hikariCp;
	}
}
