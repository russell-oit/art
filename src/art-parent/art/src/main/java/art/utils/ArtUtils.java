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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for utility methods. Only static methods, without dependencies on other
 * art classes
 *
 * @author Timothy Anyona
 */
public class ArtUtils {

	final static Logger logger = LoggerFactory.getLogger(ArtUtils.class);
	public static final String RECIPIENT_ID = "recipient_id"; //column name in data query that contains recipient identifier column
	public static final String RECIPIENT_COLUMN = "recipient_column"; //column name in data query that contains recipient identifier
	public static final String RECIPIENT_ID_TYPE = "recipient_id_type"; //column name in data query to indicate if recipient id is a number or not
	public static final String JOB_GROUP = "jobGroup"; //group name for quartz jobs
	public static final String TRIGGER_GROUP = "triggerGroup"; //group name for quartz triggers

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
	public static String getRandomString() {
		return "-" + RandomStringUtils.randomAlphanumeric(10);
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
}
