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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Represents database types
 *
 * @author Timothy Anyona
 */
public enum DatabaseType {
	//https://www.outjected.com/blog/2010/05/21/sorting-java-enums.html

	Other("other"), Demo("demo"), CUBRID("cubrid"), Oracle("oracle"),
	MySQL("mysql"), MariaDB("mariadb"), PostgreSQL("postgresql"),
	SqlServerMicrosoft("sqlserver-ms"), SqlServerJtds("sqlserver-jtds"),
	HsqldbStandAlone("hsqldb-standalone"), HsqldbServer("hsqldb-server"),
	Db2("db2"), OdbcSun("odbc-sun"), SqlLogging("sql-logging"),
	HbasePhoenix("hbase-phoenix"), MsAccessUcanaccess("msaccess-ucanaccess"),
	MsAccessUcanaccessPassword("msaccess-ucanaccess-password"),
	SqlLiteXerial("sqlite-xerial"), CsvCsvjdbc("csv-csjdbc"),
	H2Server("h2-server"), H2Embedded("h2-embedded"),
	Olap4jMondrian("olap4j-mondrian"), Olap4jXmla("olap4j-xmla"),
	Drill("drill"), Firebird("firebird"),
	MonetDB("monetdb"), Vertica("vertica"), Informix("informix"),
	CassandraAdejanovski("cassandra-adejanovski"), Neo4j("neo4j"),
	Exasol("exasol"), Redshift("redshift"), Teradata("teradata"),
	SnowflakeUsWest("snowflake-us-west"), SnowflakeOther("snowflake-other"),
	Presto("presto"), MemSQLMysql("memsql-mysql"), CitusPostgresql("citus-postgresql"),
	AuroraMySQLMariadb("aurora-mysql-mariadb"),
	AuroraPostgreSQLPostgresql("aurora-postgresql-postgresql"),
	GreenplumPostgresql("greenplum-postgresql"),
	TimescaleDBPostgresql("timescaledb-postgresql"), kdb("kdb+"),
	CsvCalcite("csv-calcite"), JsonCalcite("json-calcite"),
	MongoDBCalcite("mongodb-calcite"), CassandraCalcite("cassandra-calcite"),
	ElasticsearchCalcite("elasticsearch-calcite"), JsonCalciteArt("json-calcite-art"),
	BigQueryStarschema("bigquery-starschema"), CockroachDBPostgresql("cockroachdb-postgresql");

	private final String value;

	private DatabaseType(String value) {
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
	public static List<DatabaseType> list() {
		//use a new list as Arrays.asList() returns a fixed-size list. can't add or remove from it
		List<DatabaseType> items = new ArrayList<>();
		items.addAll(Arrays.asList(values()));
		return sortByValue(items);
	}

	/**
	 * Returns enum options without the Demo option
	 *
	 * @return enum options without the Demo option
	 */
	public static List<DatabaseType> listWithoutDemo() {
		List<DatabaseType> items = list();
		items.remove(Demo);
		return items;
	}

	/**
	 * Returns enum options that can be used for the art database
	 *
	 * @return enum options that can be used for the art database
	 */
	public static List<DatabaseType> listForArtDatabase() {
		List<DatabaseType> items = Arrays.asList(Demo, Oracle, MySQL,
				MariaDB, PostgreSQL, SqlServerMicrosoft, SqlServerJtds,
				HsqldbStandAlone, HsqldbServer, Db2, 
				H2Server, H2Embedded, Firebird, Informix);

		return sortByValue(items);
	}

	/**
	 * Sorts a given list of items by enum value
	 *
	 * @param items the items to sort
	 * @return a sorted list of items
	 */
	private static List<DatabaseType> sortByValue(List<DatabaseType> items) {
		//sort by value
		//https://turreta.com/2017/09/27/java-sort-an-enum-type-by-its-properties/
		//http://www.java2s.com/Tutorials/Java/Collection_How_to/List/Sort_List_on_object_fields_enum_constant_values.htm
		Collections.sort(items, Comparator.comparing(DatabaseType::getValue));
		return items;
	}

	/**
	 * Converts a value to an enum. If the value doesn't represent a known enum,
	 * Unknown is returned.
	 *
	 * @param value the value to convert
	 * @return the enum option that corresponds to the value
	 */
	public static DatabaseType toEnum(String value) {
		for (DatabaseType v : values()) {
			if (v.value.equalsIgnoreCase(value)) {
				return v;
			}
		}
		return null;
	}

	/**
	 * Returns this enum option's description
	 *
	 * @return this enum option's description
	 */
	public String getDescription() {
		switch (this) {
			case Oracle:
				return "Oracle - driver not included"; //license doesn't allow redistribution?
			case SqlServerMicrosoft:
				return "SQL Server (Microsoft driver)";
			case SqlServerJtds:
				return "SQL Server (jTDS driver)";
			case HsqldbStandAlone:
				return "HSQLDB Standalone";
			case HsqldbServer:
				return "HSQLDB Server";
			case Db2:
				return "Db2 - driver not included"; //license restrictions? must register to download.
			case OdbcSun:
				return "ODBC (Sun driver) - driver not included";
			case SqlLogging:
				return "SQL Logging";
			case HbasePhoenix:
				return "HBase (Phoenix driver) - driver not included"; //adds 50MB
			case MsAccessUcanaccess:
				return "MS Access (UCanAccess driver)";
			case MsAccessUcanaccessPassword:
				return "MS Access with password (UCanAccess driver)";
			case SqlLiteXerial:
				return "SQLite (Xerial driver)";
			case CsvCsvjdbc:
				return "CSV (CsvJdbc driver)";
			case CsvCalcite:
				return "CSV (Calcite driver)";
			case JsonCalcite:
				return "JSON (Calcite driver)";
			case H2Server:
				return "H2 Server";
			case H2Embedded:
				return "H2 Embedded";
			case Olap4jMondrian:
				return "Olap4j Mondrian";
			case Olap4jXmla:
				return "Olap4j XMLA";
			case Drill:
				return "Drill - driver not included"; //adds 20MB
			case Vertica:
				return "Vertica - driver not included"; //license doesn't allow redistribution. http://vertica-forums.com/viewtopic.php?t=824
			case CassandraAdejanovski:
				return "Cassandra (adejanovski driver) - driver not included";
			case CassandraCalcite:
				return "Cassandra (Calcite driver) - driver not included";
			case MongoDBCalcite:
				return "MongoDB (Calcite driver)";
			case ElasticsearchCalcite:
				return "Elasticsearch (Calcite driver) - driver not included";
			case Neo4j:
				return "Neo4j - driver not included"; //causes issues when in a VM. https://sourceforge.net/p/art/discussion/352129/thread/aa8e9973/
			case Exasol:
				return "Exasol - driver not included"; //license doesn't allow distribution without consent from exasol (details inside jar file)
			case Redshift:
				return "Redshift - driver not included"; //license issues. https://docs.aws.amazon.com/redshift/latest/mgmt/configure-jdbc-connection-with-maven.html
			case Teradata:
				return "Teradata - driver not included"; //license issues. https://downloads.teradata.com/download/license?destination=download/files/7424/187200/1/TeraJDBC__indep_indep.14.10.00.39.zip&message=License%2520Agreement
			case SnowflakeUsWest:
				return "Snowflake (US West Region) - driver not included";
			case SnowflakeOther:
				return "Snowflake (Other Regions) - driver not included";
			case MemSQLMysql:
				return "MemSQL (MySQL driver)";
			case CitusPostgresql:
				return "Citus (PostgreSQL driver)";
			case AuroraMySQLMariadb:
				return "Amazon Aurora MySQL (MariaDB driver)";
			case AuroraPostgreSQLPostgresql:
				return "Amazon Aurora PostgreSQL (PostgreSQL driver)";
			case GreenplumPostgresql:
				return "Greenplum (PostgreSQL driver)";
			case TimescaleDBPostgresql:
				return "TimescaleDB (PostgreSQL driver)";
			case kdb:
				return "kdb+";
			case JsonCalciteArt:
				return "JSON (Calcite - ART driver)";
			case BigQueryStarschema:
				return "BigQuery (Starschema driver) - driver not included";
			case CockroachDBPostgresql:
				return "CockroachDB (PostgreSQL driver)";
			case Presto:
				return "Presto - driver not included";
			default:
				return toString();
		}
	}

}
