/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides a method to validate a file name, mitigating security issues with
 * file names for files to be stored on the system
 *
 * @author Timothy Anyona
 */
public class FinalFilenameValidator {

	//http://vlaurie.com/computers2/Articles/filenames.htm
	//https://stackoverflow.com/questions/1720191/java-util-regex-importance-of-pattern-compile
	//https://stackoverflow.com/questions/1360113/is-java-regex-thread-safe
	//http://www.regexplanet.com/advanced/java/index.html
	//https://www.owasp.org/index.php/Unrestricted_File_Upload
	//ideally only accept Alpha-Numeric characters and only 1 dot as an input for the file name and the extension; in which the file name and also the extension should not be empty at all
	//file name length also limited. should be less than 255 (for ntfs partitions)
//	private static final String FILENAME_REGEX = "[a-zA-Z0-9]{1,200}\\.[a-zA-Z0-9]{1,10}";
	//here we are also accepting underscore, dash and space
	private static final String FILENAME_REGEX = "[a-zA-Z0-9_\\-\\s]{1,100}\\.[a-zA-Z0-9]{1,10}";
	private static final Pattern FILENAME_PATTERN = Pattern.compile(FILENAME_REGEX);

	/**
	 * Returns <code>true</code> if the given file name is suitable to be used
	 * for a file on the computer
	 *
	 * @param finalFilename the file name to check. Includes the extension.
	 * @return <code>true</code> if the given file name is suitable to be used
	 * for a file on the computer
	 */
	public static boolean isValid(String finalFilename) {
		Matcher matcher = FILENAME_PATTERN.matcher(finalFilename);
		return matcher.matches();
	}
}
