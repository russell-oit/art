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
package art.migration;

import art.datasource.Datasource;
import art.enums.MigrationLocation;
import art.enums.MigrationRecordType;
import java.io.Serializable;

/**
 * Represents an export records operation
 *
 * @author Timothy Anyona
 */
public class ExportRecords implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String EMBEDDED_SCHEDULES_FILENAME = "art-export-Schedules.csv";
	public static final String EMBEDDED_HOLIDAYS_FILENAME = "art-export-Holidays.csv";
	public static final String EMBEDDED_USERS_FILENAME = "art-export-Users.csv";
	public static final String EMBEDDED_USERGROUPS_FILENAME = "art-export-UserGroups.csv";
	public static final String EMBEDDED_REPORTS_FILENAME = "art-export-Reports.csv";
	public static final String EMBEDDED_REPORTGROUPS_FILENAME = "art-export-ReportGroups.csv";
	public static final String EMBEDDED_REPORTPARAMETERS_FILENAME = "art-export-ReportParameters.csv";
	public static final String EMBEDDED_USERRULEVALUES_FILENAME = "art-export-UserRuleValues.csv";
	public static final String EMBEDDED_USERGROUPRULEVALUES_FILENAME = "art-export-UserGroupRuleValues.csv";
	public static final String EMBEDDED_REPORTRULES_FILENAME = "art-export-ReportRules.csv";

	private MigrationRecordType recordType;
	private String ids;
	private MigrationLocation location = MigrationLocation.File;
	private Datasource datasource;

	/**
	 * @return the recordType
	 */
	public MigrationRecordType getRecordType() {
		return recordType;
	}

	/**
	 * @param recordType the recordType to set
	 */
	public void setRecordType(MigrationRecordType recordType) {
		this.recordType = recordType;
	}

	/**
	 * @return the ids
	 */
	public String getIds() {
		return ids;
	}

	/**
	 * @param ids the ids to set
	 */
	public void setIds(String ids) {
		this.ids = ids;
	}

	/**
	 * @return the location
	 */
	public MigrationLocation getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(MigrationLocation location) {
		this.location = location;
	}

	/**
	 * @return the datasource
	 */
	public Datasource getDatasource() {
		return datasource;
	}

	/**
	 * @param datasource the datasource to set
	 */
	public void setDatasource(Datasource datasource) {
		this.datasource = datasource;
	}
}
