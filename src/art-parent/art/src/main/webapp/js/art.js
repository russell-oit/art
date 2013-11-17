var workCount = 0;
function artAddWork() {
	workCount++;

	//alert("WorkCount is: " + workCount);

	if (workCount > 0) {
		Element.show('systemWorking');
	}

	//alert("artAddWork " + workCount);

}
function artRemoveWork() {
	workCount--;

	//alert("WorkCount is: " + workCount);

	if (workCount <= 0) {
		Element.hide('systemWorking');
	}
	//alert("artRemoveWork: " + workCount);
}


function startInNewWindow(page) {
	OpenWin = this.open(page, "CtrlWindow", "toolbar=yes,menubar=no,location=no,statusbar=no,scrollbars=yes,resizable=yes,width=800,height=600");
}

function btnup(btn) {
	btn.className = 'buttonup';
}

function btndn(btn) {
	btn.className = 'buttondn';
}

function openHelp(page) {
	OpenWin = this.open(page, "CtrlWindow", "toolbar=yes,menubar=no,locationno,statusbar=no,scrollbars=yes,resizable=yes,width=400,height=600");
}

function writeStatus(message) {
	document.getElementById("statusDiv").innerHTML = message;
}

function writeInfo(message) {
	document.getElementById("infoDiv").innerHTML = message;
}

function Start(page) {
	OpenWinMenu = this.open(page, "", "toolbar=yes,menubar=yes,location=no,status=yes,statusbar=yes,scrollbars=yes,resizable=yes,width=800,height=600");
}

function validateTinyMCE(tinyAreaId, maxLen) {
	if (tinyMCE.get(tinyAreaId).getContent().length >= maxLen) {
		alert('Message Too Long! Reduce text or formatting options');
		return false;
	} else {
		return true;
	}
}

function showHide(item) {
	if (item.className == "collapse") {
		item.className = "expand";
	} else {
		item.className = "collapse";
	}
}

function setClass(obj, cssName) {
	obj.className = cssName;
}

//for use with gridtables
function highLight(obj, cssName) {
	if (obj.className != "slct"
			&& obj.className != "slct2"
			) {
		obj.className = cssName;
	}
}
function selectRow(obj) {
	if (obj.className != 'slct') {
		obj.className = 'slct';
	} else {
		obj.className = 'hiliterows';
	}
}

function selectRow2(obj) {
	if (obj.className != 'slct2') {
		obj.className = 'slct2';
	} else {
		obj.className = 'hiliterows';
	}
}

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
	} else if (dbType === "postgresql") {
		driverElement.value = "org.postgresql.Driver";
		urlElement.value = "jdbc:postgresql://<server_name>/<database_name>";
		testSqlElement.value = "select 1";
	} else if (dbType === "hsqldb-standalone") {
		driverElement.value = "org.hsqldb.jdbcDriver";
		urlElement.value = "jdbc:hsqldb:file:<file_path>;shutdown=true;hsqldb.write_delay=false";
		testSqlElement.value = "select 1 from INFORMATION_SCHEMA.SYSTEM_USERS";
	} else if (dbType === "sqlserver-ms") {
		driverElement.value = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		urlElement.value = "jdbc:sqlserver://<server_name>;databaseName=<database_name>";
		testSqlElement.value = "select 1";
	} else if (dbType === "cubrid") {
		driverElement.value = "cubrid.jdbc.driver.CUBRIDDriver";
		urlElement.value = "jdbc:cubrid:<server_name>:33000:<database_name>";
		testSqlElement.value = "select 1";
	} else if (dbType === "other") {
		driverElement.value = "";
		urlElement.value = "";
		testSqlElement.value = "";
	} else if (dbType === "hsqldb-server") {
		driverElement.value = "org.hsqldb.jdbcDriver";
		urlElement.value = "jdbc:hsqldb:hsql://<server_name>:9001/<database_alias>";
		testSqlElement.value = "select 1 from INFORMATION_SCHEMA.SYSTEM_USERS";
	} else if (dbType === "sqlserver-jtds") {
		driverElement.value = "net.sourceforge.jtds.jdbc.Driver";
		urlElement.value = "jdbc:jtds:sqlserver://<server_name>/<database_name>";
		testSqlElement.value = "select 1";
	} else if (dbType === "log4jdbc") {
		driverElement.value = "net.sf.log4jdbc.DriverSpy";
		urlElement.value = "jdbc:log4" + urlElement.value;
		testSqlElement.value = "";
	} else if (dbType === "jndi") {
		driverElement.value = "";
		urlElement.value = "";
		testSqlElement.value = "";
	} else if (dbType === "db2") {
		driverElement.value = "com.ibm.db2.jcc.DB2Driver";
		urlElement.value = "jdbc:db2://<server_name>/<database_name>";
		testSqlElement.value = "select 1 from sysibm.sysdummy1";
	} else if (dbType === "odbc") {
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
	}
}
//
