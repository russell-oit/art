/*
 * ART. A Reporting Tool.
 * Copyright (C) 2019 Enrico Liboni <eliboni@users.sf.net>
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
package art.selfservice;

import art.utils.ArtUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.itfsw.query.builder.support.model.JsonRule;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Represents self service options
 *
 * @author Timothy Anyona
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SelfServiceOptions implements Serializable {

	private static final long serialVersionUID = 1L;
	private List<String> columns;
	private String columnsString;
	private Object jqueryRule;

	/**
	 * @return the jqueryRule
	 */
	public Object getJqueryRule() {
		return jqueryRule;
	}

	/**
	 * @param jqueryRule the jqueryRule to set
	 */
	public void setJqueryRule(Object jqueryRule) {
		this.jqueryRule = jqueryRule;
	}

	/**
	 * @return the columnsString
	 */
	public String getColumnsString() {
		return columnsString;
	}

	/**
	 * @param columnsString the columnsString to set
	 */
	public void setColumnsString(String columnsString) {
		this.columnsString = columnsString;
	}

	/**
	 * @return the columns
	 */
	public List<String> getColumns() {
		return columns;
	}

	/**
	 * @param columns the columns to set
	 */
	public void setColumns(List<String> columns) {
		this.columns = columns;
	}

	/**
	 * Returns the itfsw querybuilder rule object for the current jquery
	 * querybuilder rule object
	 *
	 * @return the itfsw querybuilder rule object
	 * @throws java.io.IOException
	 */
	@JsonIgnore
	public JsonRule getJavaRule() throws IOException {
		String jqueryRuleString = ArtUtils.objectToJson(jqueryRule);
		JsonRule javaRule = ArtUtils.jsonToObjectIgnoreUnknown(jqueryRuleString, JsonRule.class);
		return javaRule;
	}

	/**
	 * Returns the json representation of the current jquery querybuilder rule
	 * object
	 *
	 * @return the json representation of the current jquery querybuilder rule
	 * @throws JsonProcessingException
	 */
	@JsonIgnore
	public String getJqueryRuleString() throws JsonProcessingException {
		String jqueryRuleString = ArtUtils.objectToJson(jqueryRule);
		return jqueryRuleString;
	}

}
