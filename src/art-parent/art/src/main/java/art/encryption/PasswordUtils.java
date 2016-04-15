/*
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
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
package art.encryption;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author Timothy Anyona
 */
public class PasswordUtils {
	/**
	 * Hash a password using the bcrypt algorithm and return the hashed
	 * password.
	 *
	 * @param clearText
	 * @return
	 */
	public static String HashPasswordBcrypt(String clearText) {
		int rounds=10;
		return HashPasswordBcrypt(clearText, rounds);
	}

	/**
	 * Hash a password using the bcrypt algorithm and return the hashed
	 * password.
	 *
	 * @param clearText
	 * @param rounds
	 * @return
	 */
	public static String HashPasswordBcrypt(String clearText, int rounds) {
		//NOTE: bcrypt only uses the first 72 bytes so long texts
		//with the first 72 bytes the same will give the same hash

		//rounds must be 4-31
		//increase rounds to have slower password generation
		if (rounds < 4 || rounds > 31) {
			rounds = 6;
		}

		return BCrypt.hashpw(clearText, BCrypt.gensalt(rounds));
	}

	/**
	 * Verify a password against it's bcrypt hashed equivalent
	 *
	 * @param clearText clear text password
	 * @param hashedPassword hashed password
	 * @return <code>true</code> if password matches hash
	 */
	public static boolean VerifyPasswordBcrypt(String clearText, String hashedPassword) {
		if (clearText == null || hashedPassword == null || hashedPassword.length() == 0) {
			return false;
		}

		//hashedPassword must not be empty string otherwise error will be thrown
		//hashedPassword must be a valid bcrypt hash otherwise error will be thrown
		return BCrypt.checkpw(clearText, hashedPassword);
	}

	/**
	 * Hash a password using the a jdk provided algorithm specified and return
	 * the hashed password.
	 *
	 * @param clearText clear text password
	 * @param algorithm algorithm to use
	 * @return hashed password. null if clearText or algorithm is null
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public static String HashPassword(String clearText, String algorithm)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {

		if (algorithm == null || clearText == null) {
			return null;
		}

		//valid algorithm strings - MD2,MD5,SHA-1,SHA-256,SHA-384,SHA-512
		//Algorithm MD5 will generate a 128bit (16 byte) digested byte[]
		//SHA-1 algorithm  will produce a 160bit (20 byte) digested byte[]
		MessageDigest mdg = MessageDigest.getInstance(algorithm);

		// To avoid the use of the (implicit) platform-specific encoding
		// that can undermine portability of an existent ART instance
		// we enforce the "UTF-8" encoding
		byte[] hashedMsg = mdg.digest(clearText.getBytes("UTF-8"));
		// The String is now digested

		
		//convert byte array to string in hex format
		return Hex.encodeHexString(hashedMsg);
		
	}

	/**
	 * Verify a password against it's hashed equivalent
	 *
	 * @param clearText clear text password
	 * @param hashedPassword hashed password
	 * @param algorithm hashing algorithm
	 * @return <code>true</code> if password matches hash
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public static boolean VerifyPassword(String clearText, String hashedPassword, String algorithm)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {

		if (clearText == null || hashedPassword == null || algorithm == null) {
			return false;
		}

		boolean verified = false;

		if (algorithm.equals("bcrypt")) {
			verified = VerifyPasswordBcrypt(clearText, hashedPassword);
		} else if (hashedPassword.equals(HashPassword(clearText, algorithm))) {
			verified = true;
		}

		return verified;
	}
}
