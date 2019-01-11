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
	private String enclose;
	private String startEnclose;
	private String endEnclose;

	/**
	 * @return the enclose
	 */
	public String getEnclose() {
		return enclose;
	}

	/**
	 * @param enclose the enclose to set
	 */
	public void setEnclose(String enclose) {
		this.enclose = enclose;
	}

	/**
	 * @return the startEnclose
	 */
	public String getStartEnclose() {
		return startEnclose;
	}

	/**
	 * @param startEnclose the startEnclose to set
	 */
	public void setStartEnclose(String startEnclose) {
		this.startEnclose = startEnclose;
	}

	/**
	 * @return the endEnclose
	 */
	public String getEndEnclose() {
		return endEnclose;
	}

	/**
	 * @param endEnclose the endEnclose to set
	 */
	public void setEndEnclose(String endEnclose) {
		this.endEnclose = endEnclose;
	}

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
}
