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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents database protocol types
 *
 * @author Timothy Anyona
 */
public enum DatabaseProtocol {
	//https://stackoverflow.com/questions/24157817/jackson-databind-enum-case-insensitive

	Other("Other"), Oracle("Oracle"), Access("MS Access"), Teradata("Teradata"),
	CUBRID("CUBRID"), Db2("Db2"), HSQLDB("HSQLDB"), PostgreSQL("PostgreSQL"),
	SqlServer("SQL Server"), Informix("Informix"), Firebird("Firebird"),
	kdb("kdb+");

	private final String value;

	private DatabaseProtocol(String value) {
		this.value = value;
	}

	/**
	 * Returns this enum option's value
	 *
	 * @return this enum option's value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Returns all enum options
	 *
	 * @return all enum options
	 */
	public static List<DatabaseProtocol> list() {
		//use a new list as Arrays.asList() returns a fixed-size list. can't add or remove from it
		List<DatabaseProtocol> items = new ArrayList<>();
		items.addAll(Arrays.asList(values()));
		//sort by value
		//https://turreta.com/2017/09/27/java-sort-an-enum-type-by-its-properties/
		//http://www.java2s.com/Tutorials/Java/Collection_How_to/List/Sort_List_on_object_fields_enum_constant_values.htm
		//https://stackoverflow.com/questions/38533338/comparator-comparing-of-a-nested-field
		Collections.sort(items, Comparator.comparing(p -> p.getValue().toLowerCase(Locale.ENGLISH)));
		return items;
	}

	/**
	 * Converts a value to an enum. If the value doesn't represent a known enum,
	 * Unknown is returned.
	 *
	 * @param value the value to convert
	 * @return the enum option that corresponds to the value
	 */
	public static DatabaseProtocol toEnum(String value) {
		for (DatabaseProtocol v : values()) {
			if (v.value.equalsIgnoreCase(value)) {
				return v;
			}
		}
		return null;
	}

	/**
	 * Returns the database type based on the jdbc url
	 *
	 * @param url the jdbc url
	 * @return the database type
	 */
	public static DatabaseProtocol fromUrl(String url) {
		final String LOG4JDBC_PREFIX = "jdbc:log4";
		if (StringUtils.startsWith(url, LOG4JDBC_PREFIX)) {
			url = StringUtils.substringAfter(url, LOG4JDBC_PREFIX);
		}

		if (StringUtils.startsWith(url, "jdbc:oracle")) {
			return Oracle;
		} else if (StringUtils.startsWithAny(url, "jdbc:db2", "jdbc:as400")) {
			//db2 on LUW or db2 on ibm i (as/400, iseries, system i or power)
			//https://en.wikipedia.org/wiki/IBM_System_i
			return Db2;
		} else if (StringUtils.startsWith(url, "jdbc:hsqldb")) {
			return HSQLDB;
		} else if (StringUtils.startsWith(url, "jdbc:postgresql")) {
			return PostgreSQL;
		} else if (StringUtils.startsWith(url, "jdbc:cubrid")) {
			return CUBRID;
		} else if (StringUtils.startsWithAny(url, "jdbc:sqlserver", "jdbc:jtds")) {
			return SqlServer;
		} else if (StringUtils.startsWithAny(url, "jdbc:ids", "jdbc:informix-sqli")) {
			return Informix;
		} else if (StringUtils.startsWith(url, "jdbc:firebirdsql")) {
			return Firebird;
		} else if (StringUtils.startsWith(url, "jdbc:ucanaccess")) {
			return Access;
		} else if (StringUtils.startsWith(url, "jdbc:teradata")) {
			return Teradata;
		} else if (StringUtils.startsWith(url, "jdbc:q")) {
			return kdb;
		} else {
			return Other;
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
			case Firebird:
				return "rows {0}";
			case kdb:
				return "[{0}]";
			case Oracle: //12c+
			case Db2:
				//sql:2008 standard
				return "fetch first {0} rows only";
			default:
				return "limit {0}";
		}
	}

	/**
	 * Returns the limit clause with offset to use for this database type
	 *
	 * @return the limit clause with offset to use
	 */
	public String limitClauseWithOffset() {
		//http://www.jooq.org/doc/3.1/manual/sql-building/sql-statements/select-statement/limit-clause/
		switch (this) {
			case Informix:
				return "skip {1} first {0}";
			case Firebird:
				return "first {0} skip {1}";
			case Oracle: //oracle 12c+
			case SqlServer: //sql server 2012+
				//sql:2008 standard
				return "offset {1} rows fetch next {0} rows only";
			default:
				return "limit {0} offset {1}";
		}
	}

	/**
	 * Returns a select statement to use with limit clause applied
	 *
	 * @param body the main body of the select statement, after "select"
	 * @param limit the limit count i.e. number of rows to be returned
	 * @param offset the limit offset i.e. number of rows to skip
	 * @return a select statement to use with limit clause applied
	 */
	public String processSelect(String body, int limit, int offset) {
		String limitClause;
		if (limit < 0) {
			limitClause = "";
		} else {
			limitClause = MessageFormat.format(limitClauseWithOffset(), limit, offset);
		}

		switch (this) {
			case Informix:
			case Firebird:
				return "select " + limitClause + " " + body;
			default:
				return "select " + body + " " + limitClause;
		}
	}

	/**
	 * Returns this enum option's description
	 *
	 * @return this enum option's description
	 */
	public String getDescription() {
		return value;
	}

	/**
	 * Returns the test sql to use for this database type
	 *
	 * @return the test sql to use
	 */
	public String testSql() {
		switch (this) {
			case Oracle:
				return "select 1 from dual";
			case Db2:
				return "select 1 from sysibm.sysdummy1";
			case HSQLDB:
				return "values 1";
			case Informix:
				return "select 1 from systables where tabid = 1";
			case Firebird:
				return "select 1 from RDB$DATABASE";
			default:
				return "select 1";
		}
	}

	/**
	 * Returns the quartz job store delegate to use for this database type
	 *
	 * @return the quartz job store delegate to use
	 */
	public String quartzJobStoreDelegate() {
		switch (this) {
			case Oracle:
				return "org.quartz.impl.jdbcjobstore.oracle.OracleDelegate";
			case HSQLDB:
				return "org.quartz.impl.jdbcjobstore.HSQLDBDelegate";
			case PostgreSQL:
				return "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate";
			case CUBRID:
				return "org.quartz.impl.jdbcjobstore.CUBRIDDelegate";
			case SqlServer:
				return "org.quartz.impl.jdbcjobstore.MSSQLDelegate";
			default:
				return "org.quartz.impl.jdbcjobstore.StdJDBCDelegate";
		}
	}

}
