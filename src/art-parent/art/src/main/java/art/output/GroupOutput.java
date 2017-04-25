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
package art.output;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Generates a group report
 * 
 * @author Timothy Anyona
 */
public abstract class GroupOutput {
	
	protected String fullOutputFilename;
	protected String reportName;
	protected PrintWriter out;
	protected String contextPath;

	/**
	 * @return the contextPath
	 */
	public String getContextPath() {
		return contextPath;
	}

	/**
	 * @param contextPath the contextPath to set
	 */
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}
	
	/**
	 * @return the fullOutputFilename
	 */
	public String getFullOutputFileName() {
		return fullOutputFilename;
	}

	/**
	 * @param fullOutputFileName the fullOutputFilename to set
	 */
	public void setFullOutputFileName(String fullOutputFileName) {
		this.fullOutputFilename = fullOutputFileName;
	}
	
	/**
	 * @return the reportName
	 */
	public String getReportName() {
		return reportName;
	}

	/**
	 * @param reportName the reportName to set
	 */
	public void setReportName(String reportName) {
		this.reportName = reportName;
	}
	
	/**
	 * @return the writer
	 */
	public PrintWriter getWriter() {
		return out;
	}

	/**
	 * Sets the output stream
	 *
	 *
	 * @param writer
	 */
	public void setWriter(PrintWriter writer) {
		this.out = writer;
	}
	
	/**
	 * Generates a group report
	 *
	 * @param rs the resultset to use. Needs to be a scrollable.
	 * @param splitCol the group column
	 * @return number of rows output
	 * @throws SQLException
	 */
	public abstract int generateGroupReport(ResultSet rs, int splitCol) throws SQLException;
}
