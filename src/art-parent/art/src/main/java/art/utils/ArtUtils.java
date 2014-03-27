/**
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Days;
import org.joda.time.LocalDate;

/**
 * Class for utility methods. Only static methods, without dependencies on other
 * art classes
 *
 * @author Timothy Anyona
 */
public class ArtUtils {

	public static final String RECIPIENT_ID = "recipient_id"; //column name in data query that contains recipient identifier column
	public static final String RECIPIENT_COLUMN = "recipient_column"; //column name in data query that contains recipient identifier
	public static final String RECIPIENT_ID_TYPE = "recipient_id_type"; //column name in data query to indicate if recipient id is a number or not
	public static final String JOB_GROUP = "jobGroup"; //group name for quartz jobs
	public static final String TRIGGER_GROUP = "triggerGroup"; //group name for quartz triggers
	public static final String PUBLIC_USER = "public_user"; //username for the public/anonymous/guest user
	public static final String ART_USER_INVALID = "user not created in ART"; //log message on login failure
	public static final String ART_USER_DISABLED = "user disabled in ART"; //log message on login failure
	public static final int DEFAULT_CONNECTION_POOL_TIMEOUT = 20;

	public static List<String> getFileDetailsFromResult(String result) {
		List<String> details = new ArrayList<String>();
		if (StringUtils.indexOf(result, "\n") > -1) {
			String fileName = StringUtils.substringBefore(result, "\n");
			fileName = StringUtils.replace(fileName, "\r", "");
			String resultMessage = StringUtils.substringAfter(result, "\n");
			details.add(fileName);
			details.add(resultMessage);
		} else {
			details.add(result);
			details.add("");
		}
		return details;
	}

	/**
	 * Generate a random number within a given range
	 *
	 * @param minimum
	 * @param maximum
	 * @return
	 */
	public static long getRandomNumber(long minimum, long maximum) {
		Random rn = new Random();
		long randomNum = minimum + (long) (rn.nextDouble() * (maximum - minimum));
		return randomNum;
	}

	/**
	 * Generate a random number within a given range
	 *
	 * @param minimum
	 * @param maximum
	 * @return
	 */
	public static int getRandomNumber(int minimum, int maximum) {
		Random rn = new Random();
		int range = maximum - minimum + 1;
		int randomNum = rn.nextInt(range) + minimum;
		return randomNum;
	}

	/**
	 * Utility method to remove characters from query name that may result in an
	 * invalid output file name.
	 *
	 * @param fileName query name
	 * @return modified query name to be used in file names
	 */
	public static String cleanFileName(String fileName) {
		return fileName.replace('/', '_').replace('*', '_').replace('&', '_').replace('?', '_').replace('!', '_').replace('\\', '_').replace('[', '_').replace(']', '_').replace(':', '_').replace('|', '_').replace('<', '_').replace('>', '_').replace('"', '_');
	}

	/**
	 * Get random string to be appended to output filenames
	 *
	 * @return random string to be appended to output filenames
	 */
	public static String getRandomFileNameString() {
		return "-" + RandomStringUtils.randomAlphanumeric(10);
	}
	
	/**
	 * Get random string that can be used as a unique id e.g. in file names
	 *
	 * @return unique id string
	 */
	public static String getUniqueId() {
		//can potentially use randomUUID but it may block if the server lacks sufficient entropy?
		//https://stackoverflow.com/questions/14532976/performance-of-random-uuid-generation-with-java-7-or-java-6
		return System.currentTimeMillis() + "-" + RandomStringUtils.randomAlphanumeric(20);
	}

	/**
	 * Get a string to be used for correctly sorting dates irrespective of the
	 * date format used to display dates
	 *
	 * @param dt
	 * @return
	 */
	public static String getDateSortString(java.util.Date dt) {
		String sortKey;
		if (dt == null) {
			sortKey = "null";
		} else {
			SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss:SSS");
			sortKey = sf.format(dt);
		}
		return sortKey;
	}

	public static String getJndiDatasourceUrl(String url) {
		String finalUrl = url;
		if (!StringUtils.startsWith(finalUrl, "java:")) {
			finalUrl = "java:comp/env/" + finalUrl;
		}
		return finalUrl;
	}

	/**
	 * Get database types to be displayed when defining a database connection
	 *
	 * @return map with database types. key=database type identifier,
	 * value=database name
	 */
	public static Map<String, String> getDatabaseTypes() {
		//use linkedhashmap so that items are displayed in the order listed here
		Map<String, String> databaseTypes = new LinkedHashMap<>();

		databaseTypes.put("demo", "Demo");
		databaseTypes.put("cubrid", "CUBRID");
		databaseTypes.put("oracle", "Oracle");
		databaseTypes.put("mysql", "MySQL");
		databaseTypes.put("postgresql", "PostgreSQL");
		databaseTypes.put("sqlserver-ms", "SQL Server (Microsoft driver)");
		databaseTypes.put("sqlserver-jtds", "SQL Server (jTDS driver)");
		databaseTypes.put("hsqldb-standalone", "HSQLDB (Standalone mode)");
		databaseTypes.put("hsqldb-server", "HSQLDB (Server mode)");
		databaseTypes.put("db2", "DB2");
		databaseTypes.put("odbc", "Generic ODBC");
		databaseTypes.put("log4jdbc", "SQL Logging");
		databaseTypes.put("other", "Other");

		return databaseTypes;
	}

	/**
	 * Return number of days between two dates
	 *
	 * @param before earlier date
	 * @param after later date
	 * @return days in "civil" time rather than "mathematical" time e.g. Monday
	 * 10pm to Tuesday 7am will return 1 day, not 0.
	 */
	public static int daysBetween(Date before, Date after) {
		if (before == null || after == null) {
			return Integer.MAX_VALUE;
		}

		//consider "civil" days rather than "mathematical" days. so use LocalDate and not DateTime
		//see //https://stackoverflow.com/questions/3802893/number-of-days-between-two-dates-in-joda-time
		return Days.daysBetween(new LocalDate(before.getTime()), new LocalDate(after.getTime())).getDays();
	}

	/**
	 * Return number of days between a date and today
	 *
	 * @param date
	 * @return positive integer if date is earlier than today. negative
	 * otherwise. days in "civil" time rather than "mathematical" time e.g.
	 * Monday 10pm to Tuesday 7am will return 1 day, not 0.
	 */
	public static int daysUntilToday(Date date) {
		if (date == null) {
			return Integer.MAX_VALUE;
		}

		return Days.daysBetween(new LocalDate(date.getTime()), new LocalDate()).getDays();
	}

}
