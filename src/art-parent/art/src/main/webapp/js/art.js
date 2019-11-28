/**
 * @file ART javascript. General functions and global variables
 * @author Timothy Anyona
 * 
 * @copyright Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART. If not, see <http://www.gnu.org/licenses/>.
 */


//global variables

/**
 * alert button html used in ajax responses
 * 
 * @constant
 * @type String
 */
var alertCloseButton = '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>';

/**
 * alert button html used in ajax responses. alert can be shown again after being closed
 * 
 * @constant
 * @type String
 */
var reusableAlertCloseButton = '<button type="button" class="close" aria-hidden="true">x</button>';

/**
 * TinyMCE init configuration
 * 
 * @type String
 */
var tinymceConfig = {
	selector: "textarea.editor",
	plugins: [
		"advlist autolink lists link image charmap print preview hr anchor pagebreak",
		"searchreplace visualblocks visualchars code",
		"nonbreaking table contextmenu directionality",
		"paste textcolor"
	],
	toolbar1: "insertfile undo redo | styleselect | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent",
	toolbar2: "print preview | forecolor backcolor | link image | code",
	image_advtab: true,
	//https://stackoverflow.com/questions/44133697/how-do-i-remove-the-branding-from-tinymce-where-it-says-powered-by-tinymce
	//https://www.tiny.cloud/docs/configure/editor-appearance/
	branding: false,
	//https://codepen.io/nirajmchauhan/pen/EjQLpV
	paste_data_images: true,
	file_picker_callback: function (callback, value, meta) {
		if (meta.filetype === 'image') {
			$('#upload').trigger('click');
			$('#upload').on('change', function () {
				var file = this.files[0];
				var reader = new FileReader();
				reader.onload = function (e) {
					callback(e.target.result, {
						alt: ''
					});
				};
				reader.readAsDataURL(file);
			});
		}
	}
};


//functions

//some functions require certain javascript libraries to be included in the page
//before they are called, e.g. jquery, bootbox, notify

//tell jshint/jslint about global variables that are defined elsewhere
/*global $, bootbox */

//tell jslint to assume a browser (browser variables e.g. document will be available)
/*jslint browser:true */

//tell jslint to accept messy white space (ignore jslint's whitespace conventions)
/*jslint white: true */

//tell jslint to accept many var statements per function
/*jslint vars: true */

//tell jslint to accept missing "use strict"
/*jslint sloppy: true */

//tell jshint/jslint to ignore unused initial function parameters that are required in the function signature
//and also global variables (including functions) that are defined in the current file but used elsewhere
//http://jslinterrors.com/unused-a
/*jslint unparam: true, node: true */
/*jshint unused: true, node: true */
/*exported setDatasourceFields,escapeHtml,escapeHtmlAttribute,addSelectDeselectAllHandler,displayReportInfo,displayReportProgress */


//possibly enclose functions in immediately executed function and have global use strict? but jshint/jslint will warn about unused functions
//https://stackoverflow.com/questions/4462478/jslint-is-suddenly-reporting-use-the-function-form-of-use-strict


//non-javascript data types

//http://datatables.net/reference/type/
//https://api.jquery.com/Types/



/**
 * Set content of datasource definition fields depending on the database type.
 * Used in art database and edit datasource pages
 * 
 * @param {string} databaseType
 * @param {string} driverElementId
 * @param {string} urlElementId
 * @param {string} testSqlElementId
 * @param {string} databaseProtocolElementId
 */
function setDatasourceFields(databaseType, driverElementId, urlElementId,
		testSqlElementId, databaseProtocolElementId) {

	var driverElement = document.getElementById(driverElementId);
	var urlElement = document.getElementById(urlElementId);
	var testSqlElement = document.getElementById(testSqlElementId);

	//https://stackoverflow.com/questions/13343566/set-select-option-selected-by-value
	var databaseProtocolSelector = "#" + databaseProtocolElementId;

	if (databaseType === "Oracle") {
		$(databaseProtocolSelector).val('Oracle');
		driverElement.value = "oracle.jdbc.OracleDriver";
		urlElement.value = "jdbc:oracle:thin:@<server>:1521:<sid>";
		//testSqlElement.value = "select 1 from dual";
		testSqlElement.value = "isValid";
	} else if (databaseType === "MySQL") {
		//https://mariadb.com/kb/en/library/about-mariadb-connector-j/
		$(databaseProtocolSelector).val('MySQL');
		driverElement.value = "com.mysql.jdbc.Driver";
		urlElement.value = "jdbc:mysql://<server>/<database>?disableMariaDbDriver";
		//testSqlElement.value = "select 1";
		testSqlElement.value = "isValid";
	} else if (databaseType === "MemSQLMysql") {
		$(databaseProtocolSelector).val('MySQL');
		driverElement.value = "com.mysql.jdbc.Driver";
		urlElement.value = "jdbc:mysql://<server>/<database>?disableMariaDbDriver";
		//testSqlElement.value = "select 1";
		testSqlElement.value = "isValid";
	} else if (databaseType === "MariaDB") {
		$(databaseProtocolSelector).val('MariaDB');
		driverElement.value = "org.mariadb.jdbc.Driver";
		urlElement.value = "jdbc:mariadb://<server>/<database>";
		//testSqlElement.value = "select 1";
		testSqlElement.value = "isValid";
	} else if (databaseType === "AuroraMySQLMariadb") {
		//https://mariadb.com/kb/en/library/failover-and-high-availability-with-mariadb-connector-j/#specifics-for-amazon-aurora
		//https://stackoverflow.com/questions/44020489/db-connections-increase-after-setting-aurora-in-mariadb-connector
		//https://stackoverflow.com/questions/31250975/what-database-driver-should-be-used-to-access-aws-aurora
		$(databaseProtocolSelector).val('MariaDB');
		driverElement.value = "org.mariadb.jdbc.Driver";
		urlElement.value = "jdbc:mariadb:aurora://<cluster_end_point>/<database>";
		//testSqlElement.value = "select 1";
		testSqlElement.value = "isValid";
	} else if (databaseType === "PostgreSQL") {
		$(databaseProtocolSelector).val('PostgreSQL');
		driverElement.value = "org.postgresql.Driver";
		urlElement.value = "jdbc:postgresql://<server>/<database>";
		//testSqlElement.value = "select 1";
		testSqlElement.value = "isValid";
	} else if (databaseType === "CitusPostgresql") {
		$(databaseProtocolSelector).val('PostgreSQL');
		driverElement.value = "org.postgresql.Driver";
		urlElement.value = "jdbc:postgresql://<server>/<database>";
		//testSqlElement.value = "select 1";
		testSqlElement.value = "isValid";
	} else if (databaseType === "AuroraPostgreSQLPostgresql") {
		$(databaseProtocolSelector).val('PostgreSQL');
		driverElement.value = "org.postgresql.Driver";
		urlElement.value = "jdbc:postgresql://<server>/<database>";
		//testSqlElement.value = "select 1";
		testSqlElement.value = "isValid";
	} else if (databaseType === "GreenplumPostgresql") {
		$(databaseProtocolSelector).val('PostgreSQL');
		driverElement.value = "org.postgresql.Driver";
		urlElement.value = "jdbc:postgresql://<server>/<database>";
		//testSqlElement.value = "select 1";
		testSqlElement.value = "isValid";
	} else if (databaseType === "TimescaleDBPostgresql") {
		$(databaseProtocolSelector).val('PostgreSQL');
		driverElement.value = "org.postgresql.Driver";
		urlElement.value = "jdbc:postgresql://<server>/<database>";
		//testSqlElement.value = "select 1";
		testSqlElement.value = "isValid";
	} else if (databaseType === "HsqldbStandAlone") {
		$(databaseProtocolSelector).val('HSQLDB');
		driverElement.value = "org.hsqldb.jdbc.JDBCDriver";
		urlElement.value = "jdbc:hsqldb:file:<file_path>;shutdown=true;hsqldb.write_delay=false";
		//testSqlElement.value = "values 1";
		testSqlElement.value = "isValid";
	} else if (databaseType === "HsqldbServer") {
		$(databaseProtocolSelector).val('HSQLDB');
		driverElement.value = "org.hsqldb.jdbc.JDBCDriver";
		urlElement.value = "jdbc:hsqldb:hsql://<server>:9001/<database_alias>";
		//testSqlElement.value = "values 1";
		testSqlElement.value = "isValid";
	} else if (databaseType === "SqlServerMicrosoft") {
		$(databaseProtocolSelector).val('SqlServer');
		driverElement.value = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		urlElement.value = "jdbc:sqlserver://<server>;databaseName=<database>";
		//testSqlElement.value = "select 1";
		testSqlElement.value = "isValid";
	} else if (databaseType === "SqlServerJtds") {
		$(databaseProtocolSelector).val('SqlServer');
		driverElement.value = "net.sourceforge.jtds.jdbc.Driver";
		urlElement.value = "jdbc:jtds:sqlserver://<server>/<database>";
		testSqlElement.value = "select 1"; //JDBC 3.0 driver. Doesn't support connection.isVaid()
	} else if (databaseType === "CUBRID") {
		$(databaseProtocolSelector).val('CUBRID');
		driverElement.value = "cubrid.jdbc.driver.CUBRIDDriver";
		urlElement.value = "jdbc:cubrid:<server>:33000:<database>:::";
		testSqlElement.value = "select 1"; //Only supports JDBC 2.0
	} else if (databaseType === "Other") {
		$(databaseProtocolSelector).val('');
		driverElement.value = "";
		urlElement.value = "";
		testSqlElement.value = "";
	} else if (databaseType === "SqlLogging") {
		driverElement.value = "net.sf.log4jdbc.sql.jdbcapi.DriverSpy";
		urlElement.value = "jdbc:log4" + urlElement.value;
		testSqlElement.value = "";
	} else if (databaseType === "Db2") {
		$(databaseProtocolSelector).val('Db2');
		driverElement.value = "com.ibm.db2.jcc.DB2Driver";
		urlElement.value = "jdbc:db2://<server>:<port>/<database>";
		testSqlElement.value = "select 1 from sysibm.sysdummy1";
	} else if (databaseType === "OdbcSun") {
		$(databaseProtocolSelector).val('');
		driverElement.value = "sun.jdbc.odbc.JdbcOdbcDriver";
		urlElement.value = "jdbc:odbc:<dsn_name>";
		testSqlElement.value = "";
	} else if (databaseType === "Demo") {
		$(databaseProtocolSelector).val('HSQLDB');
		driverElement.value = "";
		urlElement.value = "demo";
		testSqlElement.value = "";

		var usernameElement = document.getElementById("username");
		if (usernameElement !== null) {
			usernameElement.value = "";
		}
	} else if (databaseType === "HbasePhoenix") {
		$(databaseProtocolSelector).val('Phoenix');
		driverElement.value = "org.apache.phoenix.jdbc.PhoenixDriver";
		urlElement.value = "jdbc:phoenix:<server>";
		testSqlElement.value = "";
	} else if (databaseType === "MsAccessUcanaccess") {
		$(databaseProtocolSelector).val('Access');
		driverElement.value = "net.ucanaccess.jdbc.UcanaccessDriver";
		urlElement.value = "jdbc:ucanaccess://<file_path>";
		testSqlElement.value = "";
	} else if (databaseType === "MsAccessUcanaccessPassword") {
		$(databaseProtocolSelector).val('Access');
		driverElement.value = "net.ucanaccess.jdbc.UcanaccessDriver";
		urlElement.value = "jdbc:ucanaccess://<file_path>;jackcessOpener=art.utils.CryptCodecOpener";
		testSqlElement.value = "";

		var usernameElement = document.getElementById("username");
		if (usernameElement !== null) {
			usernameElement.value = "";
		}
	} else if (databaseType === "SqlLiteXerial") {
		$(databaseProtocolSelector).val('SQLite');
		driverElement.value = "org.sqlite.JDBC";
		urlElement.value = "jdbc:sqlite:<file_path>";
		testSqlElement.value = "select 1";
	} else if (databaseType === "CsvCsvjdbc") {
		$(databaseProtocolSelector).val('');
		driverElement.value = "org.relique.jdbc.csv.CsvDriver";
		urlElement.value = "jdbc:relique:csv:<directory_with_csvs>";
		testSqlElement.value = "";
	} else if (databaseType === "H2Server") {
		$(databaseProtocolSelector).val('H2');
		driverElement.value = "org.h2.Driver";
		urlElement.value = "jdbc:h2://<server>/<database>";
		//testSqlElement.value = "select 1";
		testSqlElement.value = "isValid";
	} else if (databaseType === "H2Embedded") {
		$(databaseProtocolSelector).val('H2');
		driverElement.value = "org.h2.Driver";
		urlElement.value = "jdbc:h2://<file_path>";
		//testSqlElement.value = "select 1";
		testSqlElement.value = "isValid";
	} else if (databaseType === "Olap4jMondrian") {
		//http://forums.pentaho.com/showthread.php?70625-Mondrian-begining-with-olap4j
		//http://mondrian.pentaho.com/api/mondrian/olap4j/MondrianOlap4jDriver.html
		$(databaseProtocolSelector).val('');
		driverElement.value = "mondrian.olap4j.MondrianOlap4jDriver";
		urlElement.value = "jdbc:mondrian:Jdbc=<jdbc_url>; Catalog=<schema_file_path>; JdbcDrivers=<jdbc_driver>";
		testSqlElement.value = "";
	} else if (databaseType === "Olap4jXmla") {
		//http://www.olap4j.org/api/org/olap4j/driver/xmla/XmlaOlap4jDriver.html
		$(databaseProtocolSelector).val('');
		driverElement.value = "org.olap4j.driver.xmla.XmlaOlap4jDriver";
		urlElement.value = "jdbc:xmla:Server=<xmla_url>";
		testSqlElement.value = "";
	} else if (databaseType === "Drill") {
		//https://stackoverflow.com/questions/31654658/apache-drill-connection-to-drill-in-embedded-mode-java/33442630
		$(databaseProtocolSelector).val('Drill');
		driverElement.value = "org.apache.drill.jdbc.Driver";
		urlElement.value = "jdbc:drill:drillbit=<server>";
		testSqlElement.value = "select 1 from sys.version";
	} else if (databaseType === "Firebird") {
		//https://gist.github.com/mariuz/1043473
		//https://stackoverflow.com/questions/3424206/firebird-connection-with-java
		//https://stackoverflow.com/questions/37492890/incompatible-wire-encryption-levels-requested-on-client-and-server-with-firebird
		//https://www.firebirdsql.org/file/documentation/drivers_documentation/java/faq.html#how-can-i-solve-the-error-connection-rejected-no-connection-character-set-specified
		//http://www.firebirdfaq.org/faq30/
		$(databaseProtocolSelector).val('Firebird');
		driverElement.value = "org.firebirdsql.jdbc.FBDriver";
		urlElement.value = "jdbc:firebirdsql://<server>/<file_path or database_alias>?encoding=UTF8";
		//testSqlElement.value = "select 1 from RDB$DATABASE";
		testSqlElement.value = "isValid";
	} else if (databaseType === "MonetDB") {
		//https://en.wikibooks.org/wiki/SQL_Dialects_Reference/Select_queries/Select_without_tables
		$(databaseProtocolSelector).val('MonetDB');
		driverElement.value = "nl.cwi.monetdb.jdbc.MonetDriver";
		urlElement.value = "jdbc:monetdb://<server>/<database>";
		testSqlElement.value = "select 1";
	} else if (databaseType === "Vertica") {
		//https://my.vertica.com/docs/7.1.x/HTML/Content/Authoring/ConnectingToHPVertica/ClientJDBC/CreatingAndConfiguringAConnection.htm
		$(databaseProtocolSelector).val('Vertica');
		driverElement.value = "com.vertica.jdbc.Driver";
		urlElement.value = "jdbc:vertica://<server>/<database>";
		testSqlElement.value = "select 1 from dual";
	} else if (databaseType === "Informix") {
		//https://gist.github.com/ikenna/5706366
		$(databaseProtocolSelector).val('Informix');
		driverElement.value = "com.informix.jdbc.IfxDriver";
		urlElement.value = "jdbc:informix-sqli://<server>:<port>/<database>";
		//testSqlElement.value = "select 1 from systables where tabid=1";
		testSqlElement.value = "isValid";
	} else if (databaseType === "CassandraAdejanovski") {
		//https://datastax-oss.atlassian.net/browse/JAVA-975
		//http://docs.datastax.com/en/cql/3.1/cql/cql_using/use_query_system_c.html
		//https://stackoverflow.com/questions/34055752/select-contant-value-is-cassandra
		$(databaseProtocolSelector).val('');
		driverElement.value = "com.github.adejanovski.cassandra.jdbc.CassandraDriver";
		urlElement.value = "jdbc:cassandra://<server>/<keyspace>";
		testSqlElement.value = "select release_version from system.local";
	} else if (databaseType === "Neo4j") {
		$(databaseProtocolSelector).val('');
		driverElement.value = "org.neo4j.jdbc.Driver";
		urlElement.value = "jdbc:neo4j:bolt://<server>";
		testSqlElement.value = "";
	} else if (databaseType === "Exasol") {
		$(databaseProtocolSelector).val('Exasol');
		driverElement.value = "com.exasol.jdbc.EXADriver";
		urlElement.value = "jdbc:exa://<server>;schema=<database>";
		testSqlElement.value = "";
	} else if (databaseType === "Redshift") {
		$(databaseProtocolSelector).val('Redshift');
		driverElement.value = "com.amazon.redshift.jdbc.Driver";
		urlElement.value = "jdbc:redshift://<server>/<database>";
		//testSqlElement.value = "select 1";
		testSqlElement.value = "isValid";
	} else if (databaseType === "Teradata") {
		$(databaseProtocolSelector).val('Teradata');
		driverElement.value = "com.teradata.jdbc.TeraDriver";
		urlElement.value = "jdbc:teradata://<server>/DATABASE=<database>";
		testSqlElement.value = "";
	} else if (databaseType === "SnowflakeUsWest") {
		$(databaseProtocolSelector).val('Snowflake');
		driverElement.value = "net.snowflake.client.jdbc.SnowflakeDriver";
		urlElement.value = "jdbc:snowflake://<account_name>.snowflakecomputing.com/?warehouse=<warehouse>&db=<database>&schema=<schema>";
		testSqlElement.value = "select 1"; //JDBC 1.0 driver
	} else if (databaseType === "SnowflakeOther") {
		//https://docs.snowflake.net/manuals/user-guide/jdbc-configure.html
		//https://docs.snowflake.net/manuals/user-guide/intro-regions.html
		$(databaseProtocolSelector).val('Snowflake');
		driverElement.value = "net.snowflake.client.jdbc.SnowflakeDriver";
		urlElement.value = "jdbc:snowflake://<account_name>.<region_id>.snowflakecomputing.com/?warehouse=<warehouse>&db=<database>&schema=<schema>";
		testSqlElement.value = "select 1";
	} else if (databaseType === "Presto") {
		$(databaseProtocolSelector).val("Presto");
		driverElement.value = "com.facebook.presto.jdbc.PrestoDriver";
		urlElement.value = "jdbc:presto://<server>:<port>/<catalog>/<schema>";
		testSqlElement.value = "select 1";
	} else if (databaseType === "kdb") {
		$(databaseProtocolSelector).val("kdb");
		driverElement.value = "jdbc";
		urlElement.value = "jdbc:q:<server>:<port>";
		testSqlElement.value = "";
	}
}

//BEGIN code for escaping html content
//http://shebang.brandonmintern.com/foolproof-html-escaping-in-javascript/
var MAP = {'&': '&amp;',
	'<': '&lt;',
	'>': '&gt;',
	'"': '&quot;',
	"'": '&#39;'};

function escapeHtml(s, forAttribute) {
	if (s === null || s === undefined) {
		return s;
	}
	return s.replace(forAttribute ? /[&<>'"]/g : /[&<>]/g, function (c) {
		return MAP[c];
	});
}

function escapeHtmlContent(s) {
	if (s === null || s === undefined) {
		return s;
	}
	return s.replace(/[&<>]/g, function (c) {
		return MAP[c];
	});
}

function escapeHtmlAttribute(s) {
	if (s === null || s === undefined) {
		return s;
	}
	return s.replace(/[&<>'"]/g, function (c) {
		return MAP[c];
	});
}
//END code for escaping html content

//https://gist.github.com/getify/3667624
function escapeDoubleQuotes(s) {
	if (s === null || s === undefined) {
		return s;
	}
	return s.replace(/\\([\s\S])|(")/g, "\\$1$2");
}

/**
 * Initializes dropdown hover where a button is used for the drop down
 * 
 * @param {string} ancestor - an optional ancestor of the button dropdown toggle
 * @returns {undefined}
 */
function initializeButtonHover(ancestor) {
	//https://api.jquery.com/descendant-selector/
	var selector;
	if (ancestor === undefined) {
		selector = '';
	} else {
		selector = ancestor + ' ';
	}
	selector += 'button.dropdown-toggle';

	//bootstrap-dropdown-hover
//	$(selector).bootstrapDropdownHover({
//		hideTimeout: 100
//	});

	//bootstrap-hover-dropdown
	$(selector).dropdownHover({
		delay: 100
	});
}

/**
 * Initializes dropdown hover for bootstrap-selects
 * 
 * @returns {undefined}
 */
function initializeSelectHover() {
	//activate dropdown-hover. to make bootstrap-select open on hover
	//must come after bootstrap-select initialization
	initializeButtonHover();

	//refresh needed when using bootstrap-dropdown-hover to avoid console error
	//not needed when using bootstrap-hover-dropdown with bootstrap-select 1.10.0
	//always needed with bootstrap-select 1.13
	//bootstrap-select 1.13 with hover causes dropdowns to open upwards. set dropupAuto to false?
	//$('.selectpicker').selectpicker('refresh');
}

/**
 * Create column filter input boxes
 * 
 * @param {jQuery} tbl - table to work with
 * @returns {jQuery} column filter row that has been added
 */
function createColumnFilters(tbl) {
	//add row to thead to enable column filtering
	//use clone so that plugins work properly? e.g. colvis
	var headingRow = tbl.find('thead tr:first');
	var columnFilterRow = headingRow.clone();
	//insert cloned row as first row because datatables will put heading styling on the last thead row
	columnFilterRow.insertBefore(headingRow);
	//put search fields into cloned row
	columnFilterRow.find('th').each(function () {
		if ($(this).hasClass('noFilter')) {
			$(this).html('');
		} else {
			var title = $(this).text();
			$(this).html('<input type="text" class="form-control input-sm" placeholder="' + title + '">');
		}
	});

	return columnFilterRow;
}

/**
 * Apply column filters
 * 
 * @param {jQuery} tbl
 * @param {DataTables.Api} table - datatables api instance
 */
function applyColumnFilters(tbl, table) {
	//https://datatables.net/examples/api/multi_filter.html
	tbl.find('thead input').on('keyup change', function () {
		table
				.column($(this).parent().index() + ':visible')
				.search(this.value)
				.draw();
	});
}

/**
 * Determines if the current browser is a touch/mobile browser
 * 
 * @returns {boolean} true if this is a mobile browser
 */
function isMobile() {
	//https://stackoverflow.com/questions/3514784/what-is-the-best-way-to-detect-a-mobile-device-in-jquery
	var isMobile = ('ontouchstart' in document.documentElement || navigator.userAgent.match(/Mobi/));
	return isMobile;
}

/**
 * Actions to perform when datatables completes initializing a table
 */
function datatablesInitComplete() {
	if (!isMobile()) {
		$('div.dataTables_filter input').trigger("focus");
	}

	//https://datatables.net/forums/discussion/49138/how-to-put-space-between-buttons-and-show-x-entries
	//https://developer.snapappointments.com/bootstrap-select/examples/#width
	$('.dataTables_length select').selectpicker({
		width: '100px'
	});

	var ancestor = '.dataTables_length';
	initializeButtonHover(ancestor);
}

/**
 * Get options used for datatables used in configuration pages
 * 
 * @param {number} pageLength
 * @param {string} showAllRowsText
 * @param {string} contextPath
 * @param {string} localeCode
 * @param {array} columnDefs - column definitions
 * @param {boolean} hasSelect - whether to include select column
 * @returns {jQuery} datatables jquery object
 */
function getConfigTableOptions(pageLength, showAllRowsText, contextPath,
		localeCode, columnDefs, hasSelect) {

	if (pageLength === undefined || isNaN(pageLength)) {
		pageLength = 20;
	}

	var defaultColumnDefs = [
		{
			targets: "selectCol",
			className: 'select-checkbox',
			orderable: false,
			searchable: false
		},
		{
			targets: "dtHidden", //target name matches class name of th.
			visible: false
		},
		{
			targets: "actionCol",
			orderable: false,
			searchable: false
		}
	];

	var finalColumnDefs;
	if (columnDefs === undefined) {
		finalColumnDefs = defaultColumnDefs;
	} else {
		finalColumnDefs = defaultColumnDefs.concat(columnDefs);
	}

	var options = {
		orderClasses: false,
		pagingType: "full_numbers",
		lengthMenu: [[10, 20, 50, -1], [10, 20, 50, showAllRowsText]],
		pageLength: pageLength,
		columnDefs: finalColumnDefs,
		language: {
			url: contextPath + "/js/dataTables/i18n/dataTables_" + localeCode + ".json"
		},
		initComplete: datatablesInitComplete
	};

	if (hasSelect) {
		$.extend(options, {
			order: [[1, 'asc']],
			dom: 'lBfrtip',
			buttons: [
				'selectAll',
				'selectNone'
			],
			select: {
				style: 'multi',
				selector: 'td:first-child'
			}
		});
	}

	return options;
}

/**
 * Initialise datatable
 * 
 * @param {jQuery} tbl
 * @param {number} pageLength
 * @param {string} showAllRowsText
 * @param {string} contextPath
 * @param {string} localeCode
 * @param {boolean} addColumnFilters
 * @param {array} columnDefs - column definitions
 * @param {boolean} hasSelect - whether to include select column
 * @returns {jQuery} datatables jquery object
 */
function initTable(tbl, pageLength, showAllRowsText, contextPath, localeCode,
		addColumnFilters, columnDefs, hasSelect) {

	if (addColumnFilters === undefined) {
		addColumnFilters = true;
	}

	/** @type {jQuery} */
	var columnFilterRow = null;
	if (addColumnFilters) {
		columnFilterRow = createColumnFilters(tbl);
	}

	var options = getConfigTableOptions(pageLength, showAllRowsText,
			contextPath, localeCode, columnDefs, hasSelect);

	//use initialization that returns a jquery object. to be able to use plugins
	/** @type {jQuery} */
	var oTable = tbl.dataTable(options);

	if (columnFilterRow !== null) {
		//move column filter row after heading row
		columnFilterRow.insertAfter(columnFilterRow.next());

		//get datatables api object
		/** @type {DataTables.Api} */
		var table = oTable.api();

		// Apply the column filter
		applyColumnFilters(tbl, table);
	}

	return oTable;
}

/**
 * Initialise datatable used in configuration pages
 * 
 * @param {jQuery} tbl
 * @param {number} pageLength
 * @param {string} showAllRowsText
 * @param {string} contextPath
 * @param {string} localeCode
 * @param {boolean} addColumnFilters
 * @param {array} columnDefs - column definitions
 * @returns {jQuery} datatables jquery object
 */
function initConfigTable(tbl, pageLength, showAllRowsText, contextPath, localeCode,
		addColumnFilters, columnDefs) {

	var hasSelect = true;
	return initTable(tbl, pageLength, showAllRowsText, contextPath,
			localeCode, addColumnFilters, columnDefs, hasSelect);
}

/**
 * Initialise basic datatable without a select column
 * 
 * @param {jQuery} tbl
 * @param {number} pageLength
 * @param {string} showAllRowsText
 * @param {string} contextPath
 * @param {string} localeCode
 * @param {boolean} addColumnFilters
 * @param {array} columnDefs - column definitions
 * @returns {jQuery} datatables jquery object
 */
function initBasicTable(tbl, pageLength, showAllRowsText, contextPath, localeCode,
		addColumnFilters, columnDefs) {

	var hasSelect = false;
	return initTable(tbl, pageLength, showAllRowsText, contextPath,
			localeCode, addColumnFilters, columnDefs, hasSelect);
}

function initAjaxConfigTable(tbl, pageLength, showAllRowsText, contextPath, localeCode,
		dataUrl, errorOccurredText, showErrors, columnDefs, columns) {

	var hasSelect = true;
	var options = getConfigTableOptions(pageLength, showAllRowsText,
			contextPath, localeCode, columnDefs, hasSelect);

	$.extend(options, {
		deferRender: true,
		ajax: {
			type: "GET",
			dataType: "json",
			url: contextPath + "/" + dataUrl,
			dataSrc: function (response) {
				//https://stackoverflow.com/questions/35475964/datatables-ajax-call-error-handle
				if (response.success) {
					return response.data;
				} else {
					notifyActionErrorReusable(errorOccurredText, response.errorMessage, showErrors);
					return "";
				}
			},
			error: ajaxErrorHandler
		},
		columns: columns,
		autoWidth: false,
		drawCallback: function () {
			initializeButtonHover();
		},
		createdRow: function (row, data, dataIndex) {
			$(row).attr('data-id', data.dtId);
			$(row).attr('data-name', data.dtName);
		},
		rowId: function (data) {
			//https://datatables.net/reference/option/rowId
			//https://stackoverflow.com/questions/36663037/datatables-rowid-starting-with-a-number-issue/36663176#36663176
			return 'row_' + data.dtId;
		}
	});

	//use initialization that returns a jquery object. to be able to use plugins
	/** @type {jQuery} */
	var oTable = tbl.dataTable(options);

	return oTable;
}

/**
 * Error handler for http errors after ajax calls
 * 
 * @param {jqXHR} xhr
 * @param {string} status
 * @param {string} error
 */
function ajaxErrorHandler(xhr, status, error) {
	//https://api.jquery.com/jquery.ajax/
	var message = getAjaxErrorMessage(xhr);
	bootbox.alert(message);
}

/**
 * Show http errors after ajax calls by non-admin users
 * 
 * @param {jqXHR} xhr
 * @param {string} errorOccurredText - optional alert title
 */
function showUserAjaxError(xhr, errorOccurredText) {
	var message = getAjaxErrorMessage(xhr);
	bootbox.alert({
		title: errorOccurredText, //can be empty for alerts. if empty, alert won't have title
		message: message //must not be empty
	});
}

/**
 * Gets the message to display for an ajax call error
 * 
 * @param {jqXHR} xhr
 * @returns {String}
 */
function getAjaxErrorMessage(xhr) {
	var message;

	var responseText = xhr.responseText; //can be undefined e.g. if request header name is empty string
	if (responseText) {
		message = responseText;
	} else {
		message = xhr.status + " " + xhr.statusText;
	}

	return message;
}

/**
 * Display notification if an action was successful. String arguments should be html escaped
 * Message goes to a div with id "ajaxResponse". Alert created will be re-displayed
 * if closed manually. Additional handler code needs to be added to enable re-display.
 * 
 * @param {string} actionText - message to display
 * @param {string} [recordName] - name of record acted upon, if applicable 
 */
function notifyActionSuccessReusable(actionText, recordName) {
	var reusableAlert = true;
	notifyActionSuccess(actionText, recordName, reusableAlert);
}

/**
 * Display notification if an action was successful. String arguments should be html escaped
 * Message goes to a div with id "ajaxResponse"
 * 
 * @param {string} actionText - message to display
 * @param {string} [recordName] - name of record acted upon, if applicable
 * @param {boolean} reusableAlert - whether to create a reusable alert
 */
function notifyActionSuccess(actionText, recordName, reusableAlert) {
	var msg;

	if (reusableAlert) {
		msg = reusableAlertCloseButton;
	} else {
		msg = alertCloseButton;
	}

	msg += actionText;
	if (recordName !== undefined) {
		msg = msg + ": " + recordName;
	}

	$("#ajaxResponse").attr("class", "alert alert-success alert-dismissable").html(msg);
	if (reusableAlert) {
		$("#ajaxResponse").show();
	}

	$.notify(actionText, "success");
}

/**
 * Display notification if an action was not successful. String arguments should be html escaped
 * Message goes to a div with id "ajaxResponse". Alert created will be re-displayed
 * if closed manually. Additional handler code needs to be added to enable re-display.
 * 
 * @param {string} errorOccurredText - basic error occurred message
 * @param {string} errorMessage - error details
 * @param {boolean} showErrors - whether to show error details 
 */
function notifyActionErrorReusable(errorOccurredText, errorMessage, showErrors) {
	var reusableAlert = true;
	notifyActionError(errorOccurredText, errorMessage, showErrors, reusableAlert);
}

/**
 * Display notification if an action was not successful. String arguments should be html escaped
 * Message goes to a div with id "ajaxResponse"
 * 
 * @param {string} errorOccurredText - basic error occurred message
 * @param {string} errorMessage - error details
 * @param {boolean} showErrors - whether to show error details
 * @param {boolean} reusableAlert - whether to create a reusable alert
 */
function notifyActionError(errorOccurredText, errorMessage, showErrors, reusableAlert) {
	var msg;

	if (reusableAlert) {
		msg = reusableAlertCloseButton;
	} else {
		msg = alertCloseButton;
	}

	msg += "<p>" + errorOccurredText + "</p>";
	if (showErrors && errorMessage) {
		msg += "<p>" + escapeHtmlContent(errorMessage) + "</p>";
	}

	$("#ajaxResponse").attr("class", "alert alert-danger alert-dismissable").html(msg);
	if (reusableAlert) {
		$("#ajaxResponse").show();
	}

	$.notify(errorOccurredText, "error");
}

/**
 * Display notification if record cannot be deleted because important linked records exist.
 * String arguments should be html escaped. Message goes to a div with id "ajaxResponse"
 * 
 * @param {array} linkedRecords - array with names of linked records
 * @param {string} cannotDeleteRecordText - basic message shown in notification
 * @param {string} linkedRecordsExistText - more detailed message shown in div
 */
function notifyLinkedRecordsExistReusable(linkedRecords, cannotDeleteRecordText, linkedRecordsExistText) {
	var reusableAlert = true;
	notifyLinkedRecordsExist(linkedRecords, cannotDeleteRecordText, linkedRecordsExistText, reusableAlert);
}

/**
 * Display notification if record cannot be deleted because important linked records exist.
 * String arguments should be html escaped. Message goes to a div with id "ajaxResponse"
 * 
 * @param {array} linkedRecords - array with names of linked records
 * @param {string} cannotDeleteRecordText - basic message shown in notification
 * @param {string} linkedRecordsExistText - more detailed message shown in div
 * @param {boolean} reusableAlert - whether to create a reusable alert
 */
function notifyLinkedRecordsExist(linkedRecords, cannotDeleteRecordText, linkedRecordsExistText, reusableAlert) {
	var msg;

	if (reusableAlert) {
		msg = reusableAlertCloseButton;
	} else {
		msg = alertCloseButton;
	}

	msg += linkedRecordsExistText + "<ul>";

	$.each(linkedRecords, function (index, value) {
		msg += "<li>" + value + "</li>";
	});

	msg += "</ul>";

	$("#ajaxResponse").attr("class", "alert alert-danger alert-dismissable").html(msg);
	if (reusableAlert) {
		$("#ajaxResponse").show();
	}

	$.notify(cannotDeleteRecordText, "error");
}

/**
 * Display notification if record cannot be deleted because important linked records exist.
 * String arguments should be html escaped. Message goes to a div with id "ajaxResponse".
 * Alert created will be re-displayed  * if closed manually.
 * Additional handler code needs to be added to enable re-display.
 * 
 * @param {array} nonDeletedRecords - array with names of non-deleted records
 * @param {string} someRecordsNotDeletedText - basic message shown in notification 
 */
function notifySomeRecordsNotDeletedReusable(nonDeletedRecords, someRecordsNotDeletedText) {
	var reusableAlert = true;
	notifySomeRecordsNotDeleted(nonDeletedRecords, someRecordsNotDeletedText, reusableAlert);
}

/**
 * Display notification if record cannot be deleted because important linked records exist.
 * String arguments should be html escaped. Message goes to a div with id "ajaxResponse"
 * 
 * @param {array} nonDeletedRecords - array with names of non-deleted records
 * @param {string} someRecordsNotDeletedText - basic message shown in notification
 * @param {boolean} reusableAlert - whether to create a reusable alert
 */
function notifySomeRecordsNotDeleted(nonDeletedRecords, someRecordsNotDeletedText, reusableAlert) {
	var msg;

	if (reusableAlert) {
		msg = reusableAlertCloseButton;
	} else {
		msg = alertCloseButton;
	}

	msg += someRecordsNotDeletedText + "<ul>";

	$.each(nonDeletedRecords, function (index, value) {
		msg += "<li>" + value + "</li>";
	});

	msg += "</ul>";

	$("#ajaxResponse").attr("class", "alert alert-danger alert-dismissable").html(msg);
	if (reusableAlert) {
		$("#ajaxResponse").show();
	}

	$.notify(someRecordsNotDeletedText, "error");
}

function addDeleteRecordHandler(tbl, table, deleteRecordText, okText, cancelText,
		deleteUrl, recordDeletedText, errorOccurredText, showErrors,
		cannotDeleteRecordText, linkedRecordsExistText) {

	tbl.find('tbody').on('click', ".deleteRecord", function () {
		var row = $(this).closest("tr"); //jquery object
		var recordName = escapeHtmlContent(row.data("name"));
		var recordId = row.data("id");

		//display confirm dialog
		bootbox.confirm({
			message: deleteRecordText + ": <b>" + recordName + "</b>",
			buttons: {
				confirm: {
					label: okText
				},
				cancel: {
					label: cancelText
				}
			},
			callback: function (result) {
				if (result) {
					//user confirmed delete. make delete request
					$.ajax({
						type: "POST",
						dataType: "json",
						url: deleteUrl,
						data: {id: recordId},
						success: function (response) {
							var linkedRecords = response.data;
							if (response.success) {
								table.row(row).remove().draw(false); //draw(false) to prevent datatables from going back to page 1
								notifyActionSuccessReusable(recordDeletedText, recordName);
							} else if (linkedRecords !== null && linkedRecords.length > 0) {
								notifyLinkedRecordsExistReusable(linkedRecords, cannotDeleteRecordText, linkedRecordsExistText);
							} else {
								notifyActionErrorReusable(errorOccurredText, escapeHtmlContent(response.errorMessage), showErrors);
							}
						},
						error: ajaxErrorHandler
					});
				} //end if result
			} //end callback
		}); //end bootbox confirm
	}); //end on click
}

function addDeleteRecordsHandler(table, deleteRecordText, okText, cancelText,
		deleteUrl, recordsDeletedText, errorOccurredText, showErrors,
		selectRecordsText, someRecordsNotDeletedText) {

	$('#deleteRecords').on("click", function () {
		var selectedRows = table.rows({selected: true});
		//https://stackoverflow.com/questions/45563129/datatables-selected-rows-data-attribute
		//https://datatables.net/reference/api/rows().nodes()
		var nodes = selectedRows.nodes();
		if (nodes.length > 0) {
			var ids = $.map(nodes, function (item) {
				return $(item).data("id");
			});
			bootbox.confirm({
				message: deleteRecordText + ": <b>" + ids + "</b>",
				buttons: {
					cancel: {
						label: cancelText
					},
					confirm: {
						label: okText
					}
				},
				callback: function (result) {
					if (result) {
						//user confirmed delete. make delete request
						$.ajax({
							type: "POST",
							dataType: "json",
							url: deleteUrl,
							data: {ids: ids},
							success: function (response) {
								var nonDeletedRecords = response.data;
								if (response.success) {
									selectedRows.remove().draw(false);
									notifyActionSuccessReusable(recordsDeletedText, ids);
								} else if (nonDeletedRecords !== null && nonDeletedRecords.length > 0) {
									var nonDeletedIds = [];
									$.each(nonDeletedRecords, function (index, recordDetails) {
										//https://stackoverflow.com/questions/9133102/how-to-grab-substring-before-a-specified-character-jquery-or-javascript
										var rowIdString = recordDetails.split(' - ', 1)[0];
										var rowIdInt = parseInt(rowIdString);
										nonDeletedIds.push(rowIdInt);
									});

									var deletedIds = ids.filter(function (item) {
										if (nonDeletedIds.indexOf(item) === -1) {
											//id not in non deleted. so must have been deleted.
											return true;
										} else {
											return false;
										}
									});

									$.each(deletedIds, function (index, deletedId) {
										//https://stackoverflow.com/questions/43131847/datatable-jquery-how-to-remove-a-row-from-a-table-based-on-an-id
										//https://stackoverflow.com/questions/4146502/jquery-selectors-on-custom-data-attributes-using-html5
										//https://stackoverflow.com/questions/2487747/selecting-element-by-data-attribute
										//https://datatables.net/reference/api/row()
										//https://datatables.net/reference/type/row-selector
										var selector = "#row_" + deletedId;
										table.row(selector).remove();
									});
									table.draw(false);

									notifySomeRecordsNotDeletedReusable(nonDeletedRecords, someRecordsNotDeletedText);
								} else {
									notifyActionErrorReusable(errorOccurredText, escapeHtmlContent(response.errorMessage), showErrors);
								}
							},
							error: ajaxErrorHandler
						});
					} //end if result
				} //end callback
			}); //end bootbox confirm
		} else {
			bootbox.alert(selectRecordsText);
		}
	});
}

/**
 * Add handler for select all/deselect all links used with lou-multiselect plugin
 */
function addSelectDeselectAllHandler() {
	//handle select all/deselect all
	$('.select-all').on("click", function (e) {
		//http://fuelyourcoding.com/jquery-events-stop-misusing-return-false/
		e.preventDefault();
		var item = $(this).data('item');
		$(item).multiSelect('select_all');
	});
	$('.deselect-all').on("click", function (e) {
		e.preventDefault();
		var item = $(this).data('item');
		$(item).multiSelect('deselect_all');
	});
}

var workCount = 0;
function artAddWork() {
	workCount++;

	if (workCount > 0) {
//		Element.show('spinner');
		jQuery('#spinner').show();
	}

//	console.log("artAddWork " + workCount);
}
function artRemoveWork() {
	workCount--;

	if (workCount <= 0) {
//		Element.hide('spinner');
		jQuery('#spinner').hide();
	}

//	console.log("artRemoveWork " + workCount);
}


function escapeRegExp(string) {
	//return string.replace(/([.*+?^=!:$(){}|\[\]\/\\])/g, "\\$1");
	//https://stackoverflow.com/questions/3561493/is-there-a-regexp-escape-function-in-javascript
	//https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Regular_Expressions
	return string.replace(/[.*+?^$(){}|[\]\\]/g, '\\$&'); // $& means the whole matched string
}

function filterReportGroups(filterVal, columnVal) {
	//https://yadcf-showcase.appspot.com/dom_source_externally_triggered.html
	//https://stackoverflow.com/questions/58990022/yadcf-plugin-using-select-filter-type-with-custom-func
	if (filterVal === "~" && columnVal === "") {
		return true;
	}

	var escapedFilterVal = escapeRegExp(filterVal);
	var dataSeparator = ", ";
	var regexList = [
		"^" + escapedFilterVal + "$",
		"^" + escapedFilterVal + ",",
		dataSeparator + escapedFilterVal + "$",
		dataSeparator + escapedFilterVal + ","
	];
	var regex = regexList.join("|");

	var found = columnVal.search(regex);
	if (found === -1) {
		return false;
	} else {
		return true;
	}
}
