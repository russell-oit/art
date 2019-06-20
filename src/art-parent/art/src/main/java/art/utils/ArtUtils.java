/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import static java.time.temporal.ChronoUnit.DAYS;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides general utility methods
 *
 * @author Timothy Anyona
 */
public class ArtUtils {

	private static final Logger logger = LoggerFactory.getLogger(ArtUtils.class);
	public static final String RECIPIENT_ID = "recipient_id"; //column name in data query that contains the recipient identifier
	public static final String RECIPIENT_COLUMN = "recipient_column"; //column name in data query that contains recipient identifier column
	public static final String RECIPIENT_ID_TYPE = "recipient_id_type"; //column name in data query to indicate if recipient id is a number or not
	public static final String EMAIL_CC = "email_cc"; //column name in data query that contains email cc column
	public static final String EMAIL_BCC = "email_bcc"; //column name in data query that contains email bcc column
	public static final String JOB_GROUP = "jobGroup"; //group name for quartz jobs
	public static final String TRIGGER_GROUP = "triggerGroup"; //group name for quartz triggers
	public static final String PUBLIC_USER = "public_user"; //username for the public/anonymous/guest user
	public static final String ART_USER_INVALID = "user not created in ART"; //log message on login failure
	public static final String ART_USER_DISABLED = "user disabled in ART"; //log message on login failure
	public static final int DEFAULT_CONNECTION_POOL_TIMEOUT = 20;
	public static final String ISO_DATE_FORMAT = "yyyy-MM-dd";
	public static final String ISO_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
	public static final String ISO_DATE_TIME_SECONDS_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String ISO_DATE_TIME_MILLISECONDS_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
	public static final SimpleDateFormat isoDateFormatter = new SimpleDateFormat(ISO_DATE_FORMAT);
	public static final SimpleDateFormat isoDateTimeFormatter = new SimpleDateFormat(ISO_DATE_TIME_FORMAT);
	public static final SimpleDateFormat isoDateTimeSecondsFormatter = new SimpleDateFormat(ISO_DATE_TIME_SECONDS_FORMAT);
	public static final SimpleDateFormat isoDateTimeMillisecondsFormatter = new SimpleDateFormat(ISO_DATE_TIME_MILLISECONDS_FORMAT);
	public static final String FILE_NAME_DATE_FORMAT = "yyyy_MM_dd-HH_mm_ss_SSS";
	public static final SimpleDateFormat fileNameDateFormatter = new SimpleDateFormat(FILE_NAME_DATE_FORMAT);
	public static final String PARAM_PREFIX = "p-"; //prefix for report parameters in html element names or from url
	public static final String WHITE_HEX_COLOR_CODE = "#FFFFFF";

	public static List<String> getFileDetailsFromResult(String result) {
		List<String> details = new ArrayList<>();
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
		Random random = new Random();
		long randomNum = minimum + (long) (random.nextDouble() * (maximum - minimum));
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
		Random random = new Random();
		int range = maximum - minimum + 1;
		int randomNum = random.nextInt(range) + minimum;
		return randomNum;
	}

	/**
	 * Removes characters from the base file name part of a file name that may
	 * be invalid or result in a dangerous file name on the system
	 *
	 * @param baseFilename the initial base file name
	 * @return final base file name with unwanted characters replaced with
	 * underscores
	 */
	public static String cleanBaseFilename(String baseFilename) {
		//only allow english alphabets, numbers, underscore, dash, space
		String clean = baseFilename.replaceAll("[^a-zA-Z0-9_\\-\\s]+", "_");
		return clean;
	}

	/**
	 * Removes characters from file name that may be invalid or result in a
	 * dangerous file name on the system
	 *
	 * @param filename the initial file name, including the extension
	 * @return final file name with unwanted characters replaced with
	 * underscores
	 */
	public static String cleanFilename(String filename) {
		String finalFilename;

		String base = FilenameUtils.getBaseName(filename);
		String extension = FilenameUtils.getExtension(filename);

		if (StringUtils.containsAny(extension, "aes", "gpg")) {
			//allow second extension to be used for encryped files
			String base2 = FilenameUtils.getBaseName(base);
			String extension2 = FilenameUtils.getExtension(base);
			String cleanBase2 = cleanBaseFilename(base2);
			finalFilename = cleanBase2 + "." + extension2 + "." + extension;
		} else {
			String cleanBase = cleanBaseFilename(base);
			finalFilename = cleanBase + "." + extension;
		}

		return finalFilename;
	}

	/**
	 * Get random string to be appended to output filenames
	 *
	 * @return random string to be appended to output filenames
	 */
	public static String getRandomFileNameString() {
		return "-" + RandomStringUtils.randomAlphanumeric(5);
	}

	/**
	 * Get random string that can be used as a unique record id
	 *
	 * @return unique id string
	 */
	public static String getUniqueId() {
		//can potentially use randomUUID but it may block if the server lacks sufficient entropy?
		//https://stackoverflow.com/questions/14532976/performance-of-random-uuid-generation-with-java-7-or-java-6
		return System.currentTimeMillis() + "-" + RandomStringUtils.randomAlphanumeric(5);
	}

//	/**
//	 * Get random string that can be used as a unique file name
//	 *
//	 * @param objectId report id or job id
//	 * @param extension file extension to use e.g. pdf, xls etc
//	 * @return
//	 */
//	public static String getUniqueFileName(int objectId, String extension) {
//		//can potentially use randomUUID but it may block if the server lacks sufficient entropy?
//		//https://stackoverflow.com/questions/14532976/performance-of-random-uuid-generation-with-java-7-or-java-6
//		return objectId + "-" + System.currentTimeMillis() + "-"
//				+ RandomStringUtils.randomAlphanumeric(20) + "." + extension;
//	}
	/**
	 * Get random string that can be used as a unique file name
	 *
	 * @param objectId report id or job id
	 * @return
	 */
	public static String getUniqueFileName(int objectId) {
		//can potentially use randomUUID but it may block if the server lacks sufficient entropy?
		//https://stackoverflow.com/questions/14532976/performance-of-random-uuid-generation-with-java-7-or-java-6
		return objectId + "-" + System.currentTimeMillis() + "-"
				+ RandomStringUtils.randomAlphanumeric(20);
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

	public static DataSource getJndiDataSource(String jndiName) throws NamingException {
		logger.debug("Entering getJndiDataSource: jndiName='{}'", jndiName);

		//throw exception if jndi url is null, rather than returning a null connection, which would be useless
		Objects.requireNonNull(jndiName, "jndiName must not be null");

		InitialContext ic = new InitialContext();
		logger.debug("jndiName='{}'", jndiName);
		return (DataSource) ic.lookup(jndiName);
	}

	public static Connection getJndiConnection(String jndiName) throws NamingException, SQLException {
		return getJndiDataSource(jndiName).getConnection();
	}

	/**
	 * Return number of days between two dates
	 *
	 * @param before earlier date
	 * @param after later date
	 * @return days in "civil" time rather than "mathematical" time e.g. Monday
	 * 10pm to Tuesday 7am will return 1 day, not 0.
	 */
	public static long daysBetween(Date before, Date after) {
		if (before == null || after == null) {
			return Long.MAX_VALUE;
		}

		//consider "civil" days rather than "mathematical" days. so use LocalDate and not DateTime
		//see https://stackoverflow.com/questions/3802893/number-of-days-between-two-dates-in-joda-time
		//https://stackoverflow.com/questions/27005861/calculate-days-between-two-dates-in-java-8
		return DAYS.between(toLocalDate(before), toLocalDate(after));
	}

	/**
	 * Return number of days between a date and today
	 *
	 * @param date
	 * @return positive integer if date is earlier than today. negative
	 * otherwise. days in "civil" time rather than "mathematical" time e.g.
	 * Monday 10pm to Tuesday 7am will return 1 day, not 0.
	 */
	public static long daysUntilToday(Date date) {
		if (date == null) {
			return Long.MAX_VALUE;
		}

		//https://stackoverflow.com/questions/27005861/calculate-days-between-two-dates-in-java-8
		return DAYS.between(toLocalDate(date), LocalDate.now());
	}

	/**
	 * Converts a java.util.Date to a java.time.LocalDate object using the
	 * system default timezone
	 *
	 * @param date the java.util.Date object
	 * @return the java.time.LocalDate equivalent
	 */
	public static LocalDate toLocalDate(Date date) {
		if (date == null) {
			return null;
		} else {
			//https://stackoverflow.com/questions/21242110/convert-java-util-date-to-java-time-localdate
			return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		}
	}

	/**
	 * Converts a java.util.Date to a java.time.LocalDateTime object using the
	 * system default timezone
	 *
	 * @param date the java.util.Date object
	 * @return the java.time.LocalDateTime equivalent
	 */
	public static LocalDateTime toLocalDateTime(Date date) {
		if (date == null) {
			return null;
		} else {
			return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		}
	}

	/**
	 * Get a date object from a timestamp. Used in jsp pages to enable using
	 * jstl formatDate function with timestamp (long) values
	 *
	 * @param timestamp
	 * @return
	 */
	public static Date getDate(long timestamp) {
		return new Date(timestamp);
	}

//	// escape the ' char in a parameter value
//	public static String escapeSql(String s) {
//		if (s == null) {
//			return s;
//		} else {
//			return StringUtils.replace(s, "'", "''");
//		}
//	}
	public static String ColorToHexString(Color color) {
		//http://www.javacreed.com/how-to-get-the-hex-value-from-color/

		if (color == null) {
			return null;
		}

		String hexColor = Integer.toHexString(color.getRGB() & 0xffffff);
		if (hexColor.length() < 6) {
			hexColor = "000000".substring(0, 6 - hexColor.length()) + hexColor;
		}
		return "#" + hexColor;
	}

	/**
	 * Returns a date object with the time component set to zero
	 *
	 * @param date the date to set
	 * @return the same date with time as zero
	 */
	public static Date zeroTime(Date date) {
		if (date == null) {
			return null;
		}

		//https://stackoverflow.com/questions/17821601/set-time-to-000000
		//https://stackoverflow.com/questions/20414343/how-to-set-time-of-java-util-date-instance-to-000000
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	/**
	 * Returns formatted json string from an unformatted json string
	 *
	 * @param jsonString the unformatted json string
	 * @return formatted json string
	 * @throws IOException
	 */
	public static String prettyPrintJsonString(String jsonString) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		Object json = mapper.readValue(jsonString, Object.class);
		String prettyString = mapper.writeValueAsString(json);
		return prettyString;
	}

	/**
	 * Returns formatted json string representation of an object
	 *
	 * @param object the object
	 * @return formatted json string
	 * @throws JsonProcessingException
	 */
	public static String objectToPrettyJson(Object object) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		String prettyString = mapper.writeValueAsString(object);
		return prettyString;
	}

	/**
	 * Returns a json string representation of an object
	 *
	 * @param object the object
	 * @return json string representation of the object
	 * @throws JsonProcessingException
	 */
	public static String objectToJson(Object object) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = mapper.writeValueAsString(object);
		return jsonString;
	}

	/**
	 * Returns an object populated according to a json string
	 *
	 * @param <T> the type of the object to populate
	 * @param jsonString the json string
	 * @param clazz the class of the object to populate
	 * @return an object populated according to a json string or null if the
	 * string is null or blank
	 * @throws IOException
	 */
	public static <T> T jsonToObject(String jsonString, Class<T> clazz) throws IOException {
		//https://stackoverflow.com/questions/34296761/objectmapper-readvalue-can-return-null-value
		if (StringUtils.isBlank(jsonString)) {
			return null;
		} else {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(jsonString, clazz);
		}
	}

	/**
	 * Returns an object populated according to a json string without throwing
	 * an error if unknown properties are encountered in the object hierarchy
	 *
	 * @param <T> the type of the object to populate
	 * @param jsonString the json string
	 * @param clazz the class of the object to populate
	 * @return an object populated according to a json string or null if the
	 * string is null or blank
	 * @throws IOException
	 */
	public static <T> T jsonToObjectIgnoreUnknown(String jsonString, Class<T> clazz) throws IOException {
		//https://stackoverflow.com/questions/34296761/objectmapper-readvalue-can-return-null-value
		if (StringUtils.isBlank(jsonString)) {
			return null;
		} else {
			//https://stackoverflow.com/questions/5455014/ignoring-new-fields-on-json-objects-using-jackson
			//https://gist.github.com/jonikarppinen/9b7f3872257bce27f8e2
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			return mapper.readValue(jsonString, clazz);
		}
	}

	/**
	 * Converts an object to a map representation, with the key being the
	 * property names and the value being the property values
	 *
	 * @param object the object to convert
	 * @return the map representation. A linked hash map.
	 * @throws java.lang.IllegalAccessException
	 * @throws java.lang.reflect.InvocationTargetException
	 * @throws java.lang.NoSuchMethodException
	 */
	public static Map<String, Object> objectToMap(Object object) throws
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {

		if (object == null) {
			return null;
		}

		ObjectMapper mapper = new ObjectMapper();
		if (object instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = mapper.convertValue(object, Map.class);
			return map;
		} else {
			//https://github.com/vaadin/framework/issues/8980
			Map<String, Object> tempMap = new LinkedHashMap<>();
			Class<?> c = object.getClass();
			Field[] fields = c.getDeclaredFields();

			Map<String, Object> properties = PropertyUtils.describe(object);
			properties.remove("metaClass");
			properties.remove("class");
			Set<String> propertyNames = properties.keySet();
			//iterate over fields to get properties in declared order rather than alphabetical order
			for (Field field : fields) {
				String fieldName = field.getName();
				if (propertyNames.contains(fieldName)) {
					Object finalValue;
					Object value = properties.get(fieldName);
					if (value instanceof ObjectId) {
						ObjectId objectId = (ObjectId) value;
						finalValue = objectId.toString();
					} else {
						finalValue = value;
					}
					tempMap.put(fieldName, finalValue);
				}
			}

			@SuppressWarnings("unchecked")
			Map<String, Object> map = mapper.convertValue(tempMap, Map.class);
			return map;
		}
	}

	/**
	 * Returns <code>true</code> if a list of strings contains a given string,
	 * performing a case insensitive search
	 *
	 * @param list the list of strings to search
	 * @param searchString the string to search for
	 * @return <code>true</code> if a list of strings contains a given string
	 */
	public static boolean containsIgnoreCase(List<String> list, String searchString) {
		//https://stackoverflow.com/questions/8751455/arraylist-contains-case-sensitivity
		//https://stackoverflow.com/questions/15824733/option-to-ignore-case-with-contains-method/24829584#24829584
		for (String listItem : list) {
			if (StringUtils.equalsIgnoreCase(listItem, searchString)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a locale object based on a string. If the string is null or
	 * blank, the system/jvm default locale is returned
	 *
	 * @param localeString the string representation of the locale
	 * @return the locale object
	 */
	public static Locale getLocaleFromString(String localeString) {
		if (StringUtils.isBlank(localeString)) {
			return Locale.getDefault();
		} else if (StringUtils.contains(localeString, "-")) {
			return Locale.forLanguageTag(localeString);
		} else {
			return LocaleUtils.toLocale(localeString);
		}
	}

	/**
	 * Returns an i18n value to use, given a particular locale, taking into
	 * consideration the i18n options defined
	 *
	 * @param locale the locale to use
	 * @param i18nValueOptions the i18n definition of locales and values
	 * @return the localized value to use, or null if a localization is not
	 * found
	 */
	public static String getLocalizedValue(Locale locale,
			List<Map<String, String>> i18nValueOptions) {

		String localizedValue = null;

		if (CollectionUtils.isNotEmpty(i18nValueOptions) && locale != null) {
			boolean valueFound = false;
			for (Map<String, String> i18nValueOption : i18nValueOptions) {
				//https://stackoverflow.com/questions/1509391/how-to-get-the-one-entry-from-hashmap-without-iterating
				// Get the first entry that the iterator returns
				Entry<String, String> entry = i18nValueOption.entrySet().iterator().next();
				String localeSetting = entry.getKey();
				String localeValue = entry.getValue();
				String[] locales = StringUtils.split(localeSetting, ",");
				for (String localeString : locales) {
					if (StringUtils.equalsIgnoreCase(localeString.trim(), locale.toString())) {
						localizedValue = localeValue;
						valueFound = true;
						break;
					}
				}

				if (valueFound) {
					break;
				}
			}
		}

		return localizedValue;
	}

	/**
	 * Encode the main part of a url
	 *
	 * @param s the main url
	 * @return the main url encoded
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public static String encodeMainUrl(String s) throws MalformedURLException, URISyntaxException {
		//https://stackoverflow.com/questions/6198894/java-encode-url/6199056#6199056
		URL u = new URL(s);
		return new URI(
				u.getProtocol(),
				u.getAuthority(),
				u.getPath(),
				u.getQuery(),
				u.getRef()).
				toString();
	}

	/**
	 * Returns a string representation of a number, using . as decimal character
	 * and without thousands separator
	 *
	 * @param number the number to format
	 * @return the formatted number
	 */
	public static String formatNumberForComputer(Object number) {
		if (number == null) {
			return "";
		}

		//https://stackoverflow.com/questions/703396/how-to-nicely-format-floating-numbers-to-string-without-unnecessary-decimal-0
		final int OPTIONAL_DECIMAL_COUNT = 300;
		String optionalDecimals = StringUtils.repeat("#", OPTIONAL_DECIMAL_COUNT);
		String pattern = "0." + optionalDecimals;

		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
		df.applyPattern(pattern);
		df.setGroupingUsed(false);

		String formattedNumber = df.format(number);
		return formattedNumber;
	}

	/**
	 * Converts a string of comma separated integer values to an object list
	 *
	 * @param ids the string of comma separated values to convert
	 * @return an object list containing Integer values
	 */
	public static List<Object> idsToObjectList(String ids) {
		List<Object> idsList = new ArrayList<>();

		if (StringUtils.isNotBlank(ids)) {
			String[] idsArrayString = StringUtils.split(ids, ",");
			for (String idString : idsArrayString) {
				Integer idInteger = Integer.valueOf(idString.trim());
				idsList.add(idInteger);
			}
		}

		return idsList;
	}

	/**
	 * Converts a string of comma separated integer values to an object array
	 *
	 * @param ids the string of comma separated values to convert
	 * @return an object array containing Integer values
	 */
	public static Object[] idsToObjectArray(String ids) {
		List<Object> idsList = idsToObjectList(ids);
		Object[] idsArray = idsList.toArray(new Object[idsList.size()]);
		return idsArray;
	}

	/**
	 * Zips multiple files
	 *
	 * @param outputFileName the full file name of the zip file to generate
	 * @param inputFileNames the full file name of the files to zip
	 * @throws IOException
	 */
	public static void zipFiles(String outputFileName, String... inputFileNames) throws IOException {
		List<String> inputFileNamesList = Arrays.asList(inputFileNames);
		zipFiles(outputFileName, inputFileNamesList);
	}

	/**
	 * Zips multiple files
	 *
	 * @param outputFileName the full file name of the zip file to generate
	 * @param inputFileNames the full file name of the files to zip
	 * @throws IOException
	 */
	public static void zipFiles(String outputFileName, List<String> inputFileNames) throws IOException {
		//http://www.baeldung.com/java-compress-and-uncompress
		//https://www.mkyong.com/java/how-to-compress-files-in-zip-format/
		try (FileOutputStream fos = new FileOutputStream(outputFileName);
				ZipOutputStream zos = new ZipOutputStream(fos)) {
			for (String inputFileName : inputFileNames) {
				File fileToZip = new File(inputFileName);
				try (FileInputStream fis = new FileInputStream(fileToZip)) {
					ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
					zos.putNextEntry(zipEntry);

					byte[] bytes = new byte[1024];
					int length;
					while ((length = fis.read(bytes)) >= 0) {
						zos.write(bytes, 0, length);
					}
				}
			}
		}
	}

	/**
	 * Generates a new file name for a given file name
	 *
	 * @param filename the original file name
	 * @return the new file name
	 */
	public static String renameFile(String filename) {
		String extension = FilenameUtils.getExtension(filename);
		String newFilename = FilenameUtils.getBaseName(filename);
		newFilename += getRandomFileNameString();
		if (StringUtils.isNotBlank(extension)) {
			newFilename += "." + extension;
		}
		return newFilename;
	}

	/**
	 * Returns a Jackson ObjectMapper that only considers properties when
	 * serializing/deserializing and doesn't include get() or set() methods that
	 * don't have properties attached to them
	 *
	 * @return a Jackson ObjectMapper that only considers properties when
	 * serializing/deserializing
	 */
	public static ObjectMapper getPropertyOnlyObjectMapper() {
		//https://stackoverflow.com/questions/7105745/how-to-specify-jackson-to-only-use-fields-preferably-globally
		//https://www.baeldung.com/jackson-field-serializable-deserializable-or-not
		//https://www.baeldung.com/jackson-annotations
		//https://stackoverflow.com/questions/3907929/should-i-declare-jacksons-objectmapper-as-a-static-field
		//https://stackoverflow.com/questions/18611565/how-do-i-correctly-reuse-jackson-objectmapper
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		return mapper;
	}

	/**
	 * Returns the base url for a request
	 *
	 * @param request the http servlet request
	 * @return the base url
	 */
	public static String getBaseUrl(HttpServletRequest request) {
		if (request == null) {
			return null;
		}

		//https://stackoverflow.com/questions/2222238/httpservletrequest-to-complete-url
		//https://stackoverflow.com/questions/16675191/get-full-url-and-query-string-in-servlet-for-both-http-and-https-requests/16675399
		String baseUrl = request.getScheme() + "://"
				+ request.getServerName()
				+ ("http".equals(request.getScheme()) && request.getServerPort() == 80 || "https".equals(request.getScheme()) && request.getServerPort() == 443 ? "" : ":" + request.getServerPort())
				+ request.getContextPath();

		return baseUrl;
	}

}
