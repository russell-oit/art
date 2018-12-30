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
public enum DatabaseProtocol {

	Unknown, MySQL, MariaDB, Oracle, CUBRID, Db2, HSQLDB, PostgreSQL,
	SqlServer, Informix, Firebird, Access, SQLite, H2, Vertica, Redshift,
	Teradata, Snowflake, Presto, Drill, MonetDB, Exasol, kdb, Phoenix;

	/**
	 * Returns the database type based on the jdbc url
	 *
	 * @param url the jdbc url
	 * @return the database type
	 */
	public static DatabaseProtocol fromUrl(String url) {
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
		} else if (StringUtils.startsWithAny(url, "jdbc:mariadb", "jdbc:log4jdbc:mariadb")) {
			return MariaDB;
		} else if (StringUtils.startsWithAny(url, "jdbc:ucanaccess", "jdbc:log4jdbc:ucanaccess")) {
			return Access;
		} else if (StringUtils.startsWithAny(url, "jdbc:sqlite", "jdbc:log4jdbc:sqlite")) {
			return SQLite;
		} else if (StringUtils.startsWithAny(url, "jdbc:h2", "jdbc:log4jdbc:h2")) {
			return H2;
		} else if (StringUtils.startsWithAny(url, "jdbc:vertica", "jdbc:log4jdbc:vertica")) {
			return Vertica;
		} else if (StringUtils.startsWithAny(url, "jdbc:redshift", "jdbc:log4jdbc:redshift")) {
			return Redshift;
		} else if (StringUtils.startsWithAny(url, "jdbc:teradata", "jdbc:log4jdbc:teradata")) {
			return Teradata;
		} else if (StringUtils.startsWithAny(url, "jdbc:snowflake", "jdbc:log4jdbc:snowflake")) {
			return Snowflake;
		} else if (StringUtils.startsWithAny(url, "jdbc:presto", "jdbc:log4jdbc:presto")) {
			return Presto;
		} else if (StringUtils.startsWithAny(url, "jdbc:drill", "jdbc:log4jdbc:drill")) {
			return Drill;
		} else if (StringUtils.startsWithAny(url, "jdbc:monetdb", "jdbc:log4jdbc:monetdb")) {
			return MonetDB;
		} else if (StringUtils.startsWithAny(url, "jdbc:exa", "jdbc:log4jdbc:exa")) {
			return Exasol;
		} else if (StringUtils.startsWithAny(url, "jdbc:q", "jdbc:log4jdbc:q")) {
			return kdb;
		} else if (StringUtils.startsWithAny(url, "jdbc:phoenix", "jdbc:log4jdbc:phoenix")) {
			return Phoenix;
		} else {
			return Unknown;
		}
	}

	/**
	 * Returns the limit clause to use for this database type
	 *
	 * @return the limit clause to use
	 */
	public String limitClause() {
		//https://dba.stackexchange.com/questions/30452/ansi-iso-plans-for-limit-standardization
		//https://stackoverflow.com/questions/1528604/how-universal-is-the-limit-statement-in-sql
		//https://en.wikipedia.org/wiki/Select_(SQL)#FETCH_FIRST_clause
		//http://troels.arvin.dk/db/rdbms/#select-limit
		//https://docs.microsoft.com/en-us/sql/t-sql/queries/select-order-by-clause-transact-sql?view=sql-server-2017
		//https://www.postgresql.org/docs/current/sql-select.html#SQL-LIMIT
		//https://oracle-base.com/articles/12c/row-limiting-clause-for-top-n-queries-12cr1
		//http://teradatasql.com/how-do-i-just-select-a-few-sample-records/
		switch (this) {
			case Informix:
				return "first {0}";
			case SqlServer:
			case Access:
			case Teradata:
				return "top {0}";
			case MySQL:
			case MariaDB:
			case SQLite:
			case Vertica:
			case Redshift:
			case Presto:
			case Drill:
			case CUBRID:
			case MonetDB:
			case Exasol:
			case Phoenix:
				return "limit {0}";
			case Firebird:
				return "rows {0}";
			case kdb:
				return "[{0}]";
			default:
				//sql:2008 standard
				return "fetch first {0} rows only";
		}
	}

}
