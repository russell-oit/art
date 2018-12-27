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
package art.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * Represents database types
 *
 * @author Timothy Anyona
 */
public enum DatabaseType {

	Unknown, MySQL, MariaDB, Oracle, CUBRID, Db2, HSQLDB, PostgreSQL,
	SqlServer, Informix, Firebird;

	/**
	 * Returns the database type based on the jdbc url
	 *
	 * @param url the jdbc url
	 * @return the database type
	 */
	public static DatabaseType fromUrl(String url) {
		if (StringUtils.startsWithAny(url, "jdbc:oracle", "jdbc:log4jdbc:oracle")) {
			return Oracle;
		} else if (StringUtils.startsWithAny(url, "jdbc:db2", "jdbc:as400", "jdbc:log4jdbc:db2", "jdbc:log4jdbc:as400")) {
			//db2 on LUW or db2 on ibm i (as/400, iseries, system i or power)
			//https://en.wikipedia.org/wiki/IBM_System_i
			return Db2;
		} else if (StringUtils.startsWithAny(url, "jdbc:hsqldb", "jdbc:log4jdbc:hsqldb")) {
			return HSQLDB;
		} else if (StringUtils.startsWithAny(url, "jdbc:postgresql", "jdbc:log4jdbc:postgresql")) {
			return PostgreSQL;
		} else if (StringUtils.startsWithAny(url, "jdbc:cubrid", "jdbc:log4jdbc:cubrid")) {
			return CUBRID;
		} else if (StringUtils.startsWithAny(url, "jdbc:sqlserver", "jdbc:jtds", "jdbc:log4jdbc:sqlserver", "jdbc:log4jdbc:jtds")) {
			return SqlServer;
		} else if (StringUtils.startsWithAny(url, "jdbc:ids", "jdbc:informix-sqli", "jdbc:log4jdbc:ids", "jdbc:log4jdbc:informix-sqli")) {
			return Informix;
		} else if (StringUtils.startsWithAny(url, "jdbc:firebirdsql", "jdbc:log4jdbc:firebirdsql")) {
			return Firebird;
		} else {
			return Unknown;
		}
	}
}
