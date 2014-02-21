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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encrypt / Decrypt a string given a fixed key.
 *
 * Note: this does provide an obfuscation-like protection for strings, but using
 * this class and the same key it is possible to decrypt string (i.e. it is not
 * as sure as private/public keys)
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class Encrypter {

	private static final Logger logger = LoggerFactory.getLogger(Encrypter.class);
	private static final String staticKey = "d-jhbgy&5153tygo8176!"; // this is used as an additional static key
	private static final String defaultPassword = "1tra"; //to allow use and single point of replacing the default password

	/**
	 * Encrypt overload that uses default password
	 *
	 * @param cleartext
	 * @return cipher text
	 */
	public static String encrypt(String cleartext) {
		return encrypt(cleartext, defaultPassword);
	}

	/**
	 * Encrypt the 'creartext' string using the given key
	 *
	 * @param cleartext
	 * @param key
	 * @return cipher text
	 */
	public static String encrypt(String cleartext, String key) {
		try {
			Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
			pbeCipher.init(Cipher.ENCRYPT_MODE, getKey(key), getParamSpec());
			// Encode the string into bytes using utf-8
			byte[] utf8 = cleartext.getBytes("UTF8");
			// Encrypt
			byte[] enc = pbeCipher.doFinal(utf8);
			// Encode bytes to base64 to get a string
			return Base64.encodeBase64String(enc);
		} catch (Exception e) {
			logger.error("Error", e);
		}
		return null;
	}

	/**
	 * Decrypt overload that uses default password
	 *
	 * @param cryptedtext
	 * @return clear text
	 */
	public static String decrypt(String cryptedtext) {
		return decrypt(cryptedtext, defaultPassword);
	}

	/**
	 * D-ecrypt the 'cryptedtext' string using the given key
	 *
	 * @param cryptedtext
	 * @param key
	 * @return clear text
	 */
	public static String decrypt(String cryptedtext, String key) {
		if (cryptedtext == null) {
			return null;
		}

		try {
			Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
			pbeCipher.init(Cipher.DECRYPT_MODE, getKey(key), getParamSpec());
			// Decode base64 to get bytes
			byte[] dec = Base64.decodeBase64(cryptedtext);
			// Decrypt
			byte[] utf8 = pbeCipher.doFinal(dec);
			// Decode using utf-8
			return new String(utf8, "UTF8");
		} catch (Exception e) {
			logger.error("Error", e);
		}
		return null;
	}

	private static PBEParameterSpec getParamSpec() {
		// Salt
		byte[] salt = {
			(byte) 0xc8, (byte) 0x73, (byte) 0x21, (byte) 0x99,
			(byte) 0x73, (byte) 0xc7, (byte) 0xee, (byte) 0x8c
		};
		// Iteration count
		int count = 23;
		// Create PBE parameter set
		PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, count);
		return pbeParamSpec;
	}

	private static SecretKey getKey(String pwd) {
		try {
			pwd = staticKey + pwd;
			PBEKeySpec pbeKeySpec;
			SecretKeyFactory keyFac;
			pbeKeySpec = new PBEKeySpec(pwd.toCharArray());
			keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
			SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);
			return pbeKey;
		} catch (Exception e) {
			logger.error("Error", e);
		}
		return null;
	}

	/**
	 * Hash a password using the bcrypt algorithm and return the hashed
	 * password.
	 *
	 * @param clearText
	 * @return
	 */
	public static String HashPasswordBcrypt(String clearText) {
		return HashPasswordBcrypt(clearText, 6);
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
			rounds = 6; //default
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
