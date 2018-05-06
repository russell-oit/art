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
package art.accessright;

import art.migration.PrefixTransformer;
import art.report.Report;
import art.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.univocity.parsers.annotations.Nested;
import com.univocity.parsers.annotations.Parsed;
import java.io.Serializable;

/**
 * Represents a user-report right
 *
 * @author Timothy Anyona
 */
public class UserReportRight implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Parsed
	private int parentId; //used for import/export of linked records e.g. reports
	@Nested(headerTransformer = PrefixTransformer.class, args = "user")
	private User user;
	@JsonIgnore
	private Report report;

	/**
	 * @return the parentId
	 */
	public int getParentId() {
		return parentId;
	}

	/**
	 * @param parentId the parentId to set
	 */
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @return the report
	 */
	public Report getReport() {
		return report;
	}

	/**
	 * @param report the report to set
	 */
	public void setReport(Report report) {
		this.report = report;
	}
}
