/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
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
package art.password;

import art.servlets.Config;
import org.apache.commons.lang3.StringUtils;

/**
 * Provides methods to determine if a password meets the internal password
 * policy
 *
 * @author Timothy Anyona
 */
public class PasswordValidator {

	/**
	 * Returns <code>true</code> if the given password passes the minimum
	 * password length criteria
	 *
	 * @param password the password
	 * @return <code>true</code> if the given password passes the minimum
	 * password length criteria
	 */
	public static boolean validateLength(String password) {
		boolean valid = true;
		int passwordMinLength = Config.getSettings().getPasswordMinLength();
		if (passwordMinLength > 0) {
			if (StringUtils.length(password) < passwordMinLength) {
				valid = false;
			}
		}

		return valid;
	}

	/**
	 * Returns <code>true</code> if the given password passes the minimum
	 * lowercase characters criteria
	 *
	 * @param password the password
	 * @return <code>true</code> if the given password passes the minimum
	 * lowercase characters criteria
	 */
	public static boolean validateLowercase(String password) {
		boolean valid = true;
		int passwordMinLowercase = Config.getSettings().getPasswordMinLowercase();
		if (passwordMinLowercase > 0) {
			if (password == null) {
				valid = false;
			} else {
				int count = 0;

				//https://codereview.stackexchange.com/questions/77164/counting-the-uppercase-lowercase-numbers-and-special-characters-in-a-string
				//https://stackoverflow.com/questions/25224954/how-to-count-uppercase-and-lowercase-letters-in-a-string
				for (char c : password.toCharArray()) {
					if (Character.isLowerCase(c)) {
						count++;
					}
				}

				if (count < passwordMinLowercase) {
					valid = false;
				}
			}
		}

		return valid;
	}

	/**
	 * Returns <code>true</code> if the given password passes the minimum
	 * uppercase characters criteria
	 *
	 * @param password the password
	 * @return <code>true</code> if the given password passes the minimum
	 * uppercase characters criteria
	 */
	public static boolean validateUppercase(String password) {
		boolean valid = true;
		int passwordMinUppercase = Config.getSettings().getPasswordMinUppercase();
		if (passwordMinUppercase > 0) {
			if (password == null) {
				valid = false;
			} else {
				int count = 0;
				for (char c : password.toCharArray()) {
					if (Character.isUpperCase(c)) {
						count++;
					}
				}

				if (count < passwordMinUppercase) {
					valid = false;
				}
			}
		}

		return valid;
	}

	/**
	 * Returns <code>true</code> if the given password passes the minimum
	 * numeric characters criteria
	 *
	 * @param password the password
	 * @return <code>true</code> if the given password passes the minimum
	 * numeric characters criteria
	 */
	public static boolean validateNumeric(String password) {
		boolean valid = true;
		int passwordMinNumeric = Config.getSettings().getPasswordMinNumeric();
		if (passwordMinNumeric > 0) {
			if (password == null) {
				valid = false;
			} else {
				int count = 0;
				for (char c : password.toCharArray()) {
					if (Character.isDigit(c)) {
						count++;
					}
				}

				if (count < passwordMinNumeric) {
					valid = false;
				}
			}
		}

		return valid;
	}

	/**
	 * Returns <code>true</code> if the given password passes the minimum
	 * special characters criteria
	 *
	 * @param password the password
	 * @return <code>true</code> if the given password passes the minimum
	 * special characters criteria
	 */
	public static boolean validateSpecial(String password) {
		boolean valid = true;
		int passwordMinSpecial = Config.getSettings().getPasswordMinSpecial();
		if (passwordMinSpecial > 0) {
			if (password == null) {
				valid = false;
			} else {
				int count = 0;
				for (char c : password.toCharArray()) {
					if (!Character.isLetterOrDigit(c) && !Character.isWhitespace(c)) {
						count++;
					}
				}

				if (count < passwordMinSpecial) {
					valid = false;
				}
			}
		}

		return valid;
	}

}
