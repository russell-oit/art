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
package art.encryption;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import org.apache.commons.codec.binary.Base64;
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
public class DesEncryptor {

	private static final Logger logger = LoggerFactory.getLogger(DesEncryptor.class);
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

}
