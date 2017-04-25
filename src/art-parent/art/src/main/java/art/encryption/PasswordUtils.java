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
package art.encryption;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Hex;

/**
 * Provides methods for generating and verifying password hashes
 *
 * @author Timothy Anyona
 */
public class PasswordUtils {

	/**
	 * Hashes a password using the bcrypt algorithm
	 *
	 * @param clearText the password to hash
	 * @return the bcrypt hash
	 */
	public static String HashPasswordBcrypt(String clearText) {
		int rounds = 10;
		return HashPasswordBcrypt(clearText, rounds);
	}

	/**
	 * Hashes a password using the bcrypt algorithm
	 *
	 * @param clearText the password to hash
	 * @param rounds the number of rounds to use
	 * @return the bcrypt hash
	 */
	public static String HashPasswordBcrypt(String clearText, int rounds) {
		//NOTE: bcrypt only uses the first 72 bytes so long texts
		//with the first 72 bytes the same will give the same hash

		//rounds must be 4-31
		//increase rounds to have slower password generation
		if (rounds < 4 || rounds > 31) {
			throw new IllegalArgumentException("Invalid rounds: " + rounds + ". Expected 4-31");
		}

		return BCrypt.hashpw(clearText, BCrypt.gensalt(rounds));
	}

	/**
	 * Returns <code>true</code> if the password matches the bcrypt hash
	 *
	 * @param clearText the clear text password
	 * @param hashedPassword the bcrypt hash
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
	 * Hashes a password using the the jdk provided algorithm specified
	 *
	 * @param clearText the clear text password
	 * @param algorithm the algorithm to use e.g. MD2, MD5, SHA-1, SHA-256,
	 * SHA-384, SHA-512
	 * @return password hash. null if clearText or algorithm is null
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
	 * Returns <code>true</code> if the password matches the hash
	 *
	 * @param clearText the clear text password
	 * @param hashedPassword the password hash
	 * @param algorithm the hashing algorithm. bcrypt or MD2, MD5, SHA-1, SHA-256,
	 * SHA-384, SHA-512
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
