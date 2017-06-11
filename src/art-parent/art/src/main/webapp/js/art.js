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
/*exported setDatasourceFields,escapeHtml,escapeHtmlAttribute,initConfigPage,addSelectDeselectAllHandler,displayReportInfo,displayReportProgress */


//possibly enclose functions in immediately executed function and have global use strict? but jshint/jslint will warn about unused functions
//https://stackoverflow.com/questions/4462478/jslint-is-suddenly-reporting-use-the-function-form-of-use-strict


//non-javascript data types

//http://datatables.net/reference/type/
//https://api.jquery.com/Types/



/**
 * Set content of datasource definition fields depending on the database type.
 * Used in art database and edit datasource pages
 * 
 * @param {string} dbType
 * @param {string} driverElementId
 * @param {string} urlElementId
 * @param {string} testSqlElementId
 */
function setDatasourceFields(dbType, driverElementId, urlElementId, testSqlElementId) {
	var driverElement = document.getElementById(driverElementId);
	var urlElement = document.getElementById(urlElementId);
	var testSqlElement = document.getElementById(testSqlElementId);

	if (dbType === "oracle") {
		driverElement.value = "oracle.jdbc.OracleDriver";
		urlElement.value = "jdbc:oracle:thin:@<server_name>:1521:<sid>";
		testSqlElement.value = "select 1 from dual";
	} else if (dbType === "mysql") {
		driverElement.value = "com.mysql.jdbc.Driver";
		urlElement.value = "jdbc:mysql://<server_name>/<database_name>";
		testSqlElement.value = "select 1";
	} else if (dbType === "mariadb") {
		driverElement.value = "org.mariadb.jdbc.Driver";
		urlElement.value = "jdbc:mariadb://<server_name>/<database_name>";
		testSqlElement.value = "select 1";
	} else if (dbType === "postgresql") {
		driverElement.value = "org.postgresql.Driver";
		urlElement.value = "jdbc:postgresql://<server_name>/<database_name>";
		testSqlElement.value = "select 1";
	} else if (dbType === "hsqldb-standalone") {
		driverElement.value = "org.hsqldb.jdbcDriver";
		urlElement.value = "jdbc:hsqldb:file:<file_path>;shutdown=true;hsqldb.write_delay=false";
		testSqlElement.value = "values 1";
	} else if (dbType === "sqlserver-ms") {
		driverElement.value = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		urlElement.value = "jdbc:sqlserver://<server_name>;databaseName=<database_name>";
		testSqlElement.value = "select 1";
	} else if (dbType === "cubrid") {
		driverElement.value = "cubrid.jdbc.driver.CUBRIDDriver";
		urlElement.value = "jdbc:cubrid:<server_name>:33000:<database_name>:::";
		testSqlElement.value = "select 1";
	} else if (dbType === "other") {
		driverElement.value = "";
		urlElement.value = "";
		testSqlElement.value = "";
	} else if (dbType === "hsqldb-server") {
		driverElement.value = "org.hsqldb.jdbcDriver";
		urlElement.value = "jdbc:hsqldb:hsql://<server_name>:9001/<database_alias>";
		testSqlElement.value = "values 1";
	} else if (dbType === "sqlserver-jtds") {
		driverElement.value = "net.sourceforge.jtds.jdbc.Driver";
		urlElement.value = "jdbc:jtds:sqlserver://<server_name>/<database_name>";
		testSqlElement.value = "select 1";
	} else if (dbType === "sql-logging") {
		driverElement.value = "net.sf.log4jdbc.sql.jdbcapi.DriverSpy";
		urlElement.value = "jdbc:log4" + urlElement.value;
		testSqlElement.value = "";
	} else if (dbType === "db2") {
		driverElement.value = "com.ibm.db2.jcc.DB2Driver";
		urlElement.value = "jdbc:db2://<server_name>/<database_name>";
		testSqlElement.value = "select 1 from sysibm.sysdummy1";
	} else if (dbType === "generic-odbc") {
		driverElement.value = "sun.jdbc.odbc.JdbcOdbcDriver";
		urlElement.value = "jdbc:odbc:<dsn_name>";
		testSqlElement.value = "";
	} else if (dbType === "demo") {
		driverElement.value = "";
		urlElement.value = "demo";
		testSqlElement.value = "";

		var usernameElement = document.getElementById("username");
		if (usernameElement !== null) {
			usernameElement.value = "";
		}
	} else if (dbType === "hbase-phoenix") {
		driverElement.value = "org.apache.phoenix.jdbc.PhoenixDriver";
		urlElement.value = "jdbc:phoenix:<server_name>";
		testSqlElement.value = "";
	} else if (dbType === "msaccess-ucanaccess") {
		driverElement.value = "net.ucanaccess.jdbc.UcanaccessDriver";
		urlElement.value = "jdbc:ucanaccess://c:/file_path.mdb";
		testSqlElement.value = "";
	} else if (dbType === "sqlite-xerial") {
		driverElement.value = "org.sqlite.JDBC";
		urlElement.value = "jdbc:sqlite:<file_path>";
		testSqlElement.value = "select 1";
	} else if (dbType === "csv-csvjdbc") {
		driverElement.value = "org.relique.jdbc.csv.CsvDriver";
		urlElement.value = "jdbc:relique:csv:<directory_with_csvs>";
		testSqlElement.value = "";
	} else if (dbType === "h2-server") {
		driverElement.value = "org.h2.Driver";
		urlElement.value = "jdbc:h2://<server>/<db-name>";
		testSqlElement.value = "select 1";
	} else if (dbType === "h2-embedded") {
		driverElement.value = "org.h2.Driver";
		urlElement.value = "jdbc:h2://<file_path>";
		testSqlElement.value = "select 1";
	} else if (dbType === "olap4j-mondrian") {
		//http://forums.pentaho.com/showthread.php?70625-Mondrian-begining-with-olap4j
		//http://mondrian.pentaho.com/api/mondrian/olap4j/MondrianOlap4jDriver.html
		driverElement.value = "mondrian.olap4j.MondrianOlap4jDriver";
		urlElement.value = "jdbc:mondrian:Jdbc=<jdbc_url>; Catalog=<schema_file_path>; JdbcDrivers=<jdbc_driver>";
		testSqlElement.value = "";
	} else if (dbType === "olap4j-xmla") {
		//http://www.olap4j.org/api/org/olap4j/driver/xmla/XmlaOlap4jDriver.html
		driverElement.value = "org.olap4j.driver.xmla.XmlaOlap4jDriver";
		urlElement.value = "jdbc:xmla:Server=<xmla_url>";
		testSqlElement.value = "";
	} else if (dbType === "couchbase") {
		driverElement.value = "com.couchbase.jdbc.CBDriver";
		urlElement.value = "jdbc:couchbase://<server_name>:8093";
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
	return s.replace(forAttribute ? /[&<>'"]/g : /[&<>]/g, function (c) {
		return MAP[c];
	});
}

function escapeHtmlContent(s) {
	return s.replace(/[&<>]/g, function (c) {
		return MAP[c];
	});
}

function escapeHtmlAttribute(s) {
	return s.replace(/[&<>'"]/g, function (c) {
		return MAP[c];
	});
}
//END code for escaping html content

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
 * Actions to perform when datatables completes initializing a table
 */
function datatablesInitComplete() {
	$('div.dataTables_filter input').focus();

//	$('.dataTables_length select').addClass('selectpicker');
////	$('.dataTables_length select').attr({'data-toggle': 'dropdown', 'data-hover': 'dropdown'});
////	$('.dataTables_length select').attr('data-hover','dropdown');
//
//	//Enable Bootstrap-Select
//	$('.selectpicker').selectpicker({
//		iconBase: 'fa',
//		tickIcon: 'fa-check-square'
//	});
//	
//	$('button.dropdown-toggle').dropdownHover({
//		delay: 100
//	});
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
 * @returns {jQuery} datatables jquery object
 */
function initConfigTable(tbl, pageLength, showAllRowsText, contextPath, localeCode,
		addColumnFilters) {

	if (pageLength === undefined || isNaN(pageLength)) {
		pageLength = 10;
	}

	if (addColumnFilters === undefined) {
		addColumnFilters = true;
	}

	/** @type {jQuery} */
	var columnFilterRow = null;
	if (addColumnFilters) {
		columnFilterRow = createColumnFilters(tbl);
	}

	//use initialization that returns a jquery object. to be able to use plugins
	/** @type {jQuery} */
	var oTable = tbl.dataTable({
		orderClasses: false,
		pagingType: "full_numbers",
		lengthMenu: [[5, 10, 25, -1], [5, 10, 25, showAllRowsText]],
		pageLength: pageLength,
		columnDefs: [{
				orderable: false,
				className: 'select-checkbox',
				targets: 0
			},
			{
				targets: "dtHidden", //target name matches class name of th.
				visible: false
			}
		],
		dom: 'lBfrtip',
		buttons: [
			'selectAll',
			'selectNone',
			{
				extend: 'colvis',
				postfixButtons: ['colvisRestore']
			},
			{
				extend: 'excel',
				exportOptions: {
					columns: ':visible'
				}
			},
			{
				extend: 'pdf',
				exportOptions: {
					columns: ':visible'
				}
			},
			{
				extend: 'print',
				exportOptions: {
					columns: ':visible'
				}
			}
		],
		select: {
			style: 'multi',
			selector: 'td:first-child'
		},
		order: [[1, 'asc']],
		language: {
			url: contextPath + "/js/dataTables/i18n/dataTables_" + localeCode + ".json"
		},
		initComplete: datatablesInitComplete
	});

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
 * Error handler for http errors after ajax calls
 * 
 * @param {jqXHR} xhr
 */
function ajaxErrorHandler(xhr) {
	bootbox.alert(xhr.responseText);
}

/**
 * Display notification if an action was successful. String arguments should be html escaped
 * Message goes to a div with id "ajaxResponse"
 * 
 * @param {string} actionText - message to display
 * @param {string} [recordName] - name of record acted upon, if applicable
 */
function notifyActionSuccess(actionText, recordName) {
	var msg;
	msg = alertCloseButton + actionText;
	if (recordName !== undefined) {
		msg = msg + ": " + recordName;
	}
	$("#ajaxResponse").attr("class", "alert alert-success alert-dismissable").html(msg);
	$.notify(actionText, "success");
}

/**
 * Display notification if an action was not successful. String arguments should be html escaped
 * Message goes to a div with id "ajaxResponse"
 * 
 * @param {string} errorOccurredText - basic error occurred message
 * @param {string} errorMessage - error details
 */
function notifyActionError(errorOccurredText, errorMessage) {
	var msg;
	msg = alertCloseButton + "<p>" + errorOccurredText + "</p><p>" + errorMessage + "</p>";
	$("#ajaxResponse").attr("class", "alert alert-danger alert-dismissable").html(msg);
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
function notifyLinkedRecordsExist(linkedRecords, cannotDeleteRecordText, linkedRecordsExistText) {
	var msg;
	msg = alertCloseButton + linkedRecordsExistText + "<ul>";

	$.each(linkedRecords, function (index, value) {
		msg += "<li>" + value + "</li>";
	});

	msg += "</ul>";

	$("#ajaxResponse").attr("class", "alert alert-danger alert-dismissable").html(msg);
	$.notify(cannotDeleteRecordText, "error");
}

/**
 * Display notification if record cannot be deleted because important linked records exist.
 * String arguments should be html escaped. Message goes to a div with id "ajaxResponse"
 * 
 * @param {array} nonDeletedRecords - array with names of non-deleted records
 * @param {string} someRecordsNotDeletedText - basic message shown in notification
 */
function notifySomeRecordsNotDeleted(nonDeletedRecords, someRecordsNotDeletedText) {
	var msg;
	msg = alertCloseButton + someRecordsNotDeletedText + "<ul>";

	$.each(nonDeletedRecords, function (index, value) {
		msg += "<li>" + value + "</li>";
	});

	msg += "</ul>";

	$("#ajaxResponse").attr("class", "alert alert-danger alert-dismissable").html(msg);
	$.notify(someRecordsNotDeletedText, "error");
}

/**
 * Callback for delete record ajax done successfully. String arguments should be html escaped
 * 
 * @param {PlainObject} response
 * @param {DataTables.Api} table
 * @param {jQuery} row
 * @param {string} recordDeletedText 
 * @param {string} recordName
 * @param {string} errorOccurredText
 * @param {boolean} deleteRow 
 * @param {string} [cannotDeleteRecordText]
 * @param {string} [linkedRecordsExistText]
 */
function deleteDoneHandler(response, table, row, recordDeletedText, recordName, errorOccurredText,
		deleteRow, cannotDeleteRecordText, linkedRecordsExistText) {

	var linkedRecords = response.data;
	if (response.success) {
		if (deleteRow) {
			table.row(row).remove().draw(false); //draw(false) to prevent datatables from going back to page 1
		}
		notifyActionSuccess(recordDeletedText, recordName);
	} else if (linkedRecords !== null && linkedRecords.length > 0) {
		notifyLinkedRecordsExist(linkedRecords, cannotDeleteRecordText, linkedRecordsExistText);
	} else {
		notifyActionError(errorOccurredText, escapeHtmlContent(response.errorMessage));
	}
}

/**
 * Send delete request and process results. String arguments should be html escaped
 * 
 * @param {string} contextPath
 * @param {string} deleteUrl - url portion after the "app" section
 * @param {string|number} recordId
 * @param {DataTables.Api} table
 * @param {jQuery} row
 * @param {string} recordDeletedText - message shown after successful deletion
 * @param {string} recordName
 * @param {string} errorOccurredText
 * @param {boolean} deleteRow - whether to delete the table row for the affected record
 * @param {string} [cannotDeleteRecordText]
 * @param {string} [linkedRecordsExistText]
 */
function sendDeleteRequest(contextPath, deleteUrl, recordId,
		table, row, recordDeletedText, recordName, errorOccurredText,
		deleteRow, cannotDeleteRecordText, linkedRecordsExistText) {

	var request = $.ajax({
		type: "POST",
		dataType: "json",
		url: contextPath + "/" + deleteUrl,
		data: {id: recordId}
	});

	//register http success callback
	request.done(function (response) {
		deleteDoneHandler(response, table, row, recordDeletedText,
				recordName, errorOccurredText,
				deleteRow, cannotDeleteRecordText, linkedRecordsExistText);
	});
	//register http error callback
	request.fail(ajaxErrorHandler);
}

/**
 * Register handler for delete button click and process delete record actions
 * 
 * @param {jQuery} tbl
 * @param {DataTables.Api} table
 * @param {string} deleteButtonSelector
 * @param {boolean} showConfirmDialog
 * @param {string} deleteRecordText
 * @param {string} okText - confirm dialog ok button text
 * @param {string} cancelText - confirm dialog cancel button text
 * @param {string} contextPath
 * @param {string} deleteUrl
 * @param {string} recordDeletedText
 * @param {string} errorOccurredText
 * @param {boolean} deleteRow
 * @param {string} cannotDeleteRecordText
 * @param {string} linkedRecordsExistText
 */
function addDeleteRecordHandler(tbl, table, deleteButtonSelector,
		showConfirmDialog, deleteRecordText, okText, cancelText,
		contextPath, deleteUrl, recordDeletedText, errorOccurredText,
		deleteRow, cannotDeleteRecordText, linkedRecordsExistText) {

	//delete record
	tbl.find('tbody').on('click', deleteButtonSelector, function () {
		var row = $(this).closest("tr"); //jquery object
		var recordName = escapeHtmlContent(row.data("name"));
		var recordId = row.data("id");

		if (showConfirmDialog) {
			//display confirm dialog
			bootbox.confirm({
				message: deleteRecordText + ": <b>" + recordName + "</b>",
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
						sendDeleteRequest(contextPath, deleteUrl, recordId,
								table, row, recordDeletedText, recordName, errorOccurredText,
								deleteRow, cannotDeleteRecordText, linkedRecordsExistText);

					} //end if result
				} //end callback
			}); //end bootbox confirm
		} else {
			sendDeleteRequest(contextPath, deleteUrl, recordId,
					table, row, recordDeletedText, recordName, errorOccurredText,
					deleteRow, cannotDeleteRecordText, linkedRecordsExistText);
		}

	}); //end on click
}

/**
 * Initialize datatable and delete record handler for a configuration page
 * 
 * @param {jQuery} tbl
 * @param {number} pageLength
 * @param {string} showAllRowsText
 * @param {string} contextPath
 * @param {string} localeCode
 * @param {boolean} addColumnFilters
 * @param {string} deleteButtonSelector
 * @param {boolean} showConfirmDialog
 * @param {string} deleteRecordText
 * @param {string} okText
 * @param {string} cancelText
 * @param {string} deleteUrl
 * @param {string} recordDeletedText
 * @param {string} errorOccurredText
 * @param {boolean} deleteRow
 * @param {string} cannotDeleteRecordText
 * @param {string} linkedRecordsExistText
 * @returns {jQuery} datatables jquery object
 */
function initConfigPage(tbl, pageLength, showAllRowsText, contextPath, localeCode, addColumnFilters,
		deleteButtonSelector,
		showConfirmDialog, deleteRecordText, okText, cancelText,
		deleteUrl, recordDeletedText, errorOccurredText,
		deleteRow, cannotDeleteRecordText, linkedRecordsExistText) {

	var oTable = initConfigTable(tbl, pageLength, showAllRowsText, contextPath,
			localeCode, addColumnFilters);

	//get datatables api object
	var table = oTable.api();

	addDeleteRecordHandler(tbl, table, deleteButtonSelector,
			showConfirmDialog, deleteRecordText, okText, cancelText,
			contextPath, deleteUrl, recordDeletedText, errorOccurredText,
			deleteRow, cannotDeleteRecordText, linkedRecordsExistText);

	return oTable;
}

/**
 * Add handler for select all/deselect all links used with lou-multiselect plugin
 */
function addSelectDeselectAllHandler() {
	//handle select all/deselect all
	$('.select-all').click(function (e) {
		//http://fuelyourcoding.com/jquery-events-stop-misusing-return-false/
		e.preventDefault();
		var item = $(this).data('item');
		$(item).multiSelect('select_all');
	});
	$('.deselect-all').click(function (e) {
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