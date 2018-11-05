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

import art.servlets.Config;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides encryption and decryption of strings using the AES algorithm
 *
 * @author Timothy Anyona
 */
public class AesEncryptor {
	//http://javapointers.com/tutorial/how-to-encrypt-and-decrypt-using-aes-in-java/
	//https://stackoverflow.com/questions/15554296/simple-java-aes-encrypt-decrypt-example
	//http://www.madirish.net/561
	//https://www.grc.com/passwords.htm
	//https://docs.oracle.com/javase/7/docs/api/javax/crypto/Cipher.html
	//https://crypto.stackexchange.com/questions/50782/what-size-of-initialization-vector-iv-is-needed-for-aes-encryption
	//https://security.stackexchange.com/questions/90848/encrypting-using-aes-256-can-i-use-256-bits-iv
	//https://stackoverflow.com/questions/6729834/need-solution-for-wrong-iv-length-in-aes

	private static final Logger logger = LoggerFactory.getLogger(AesEncryptor.class);
	private static final String DEFAULT_KEY = "XH6YUHlrofcQDZjd"; // 128 bit key (16 bytes)
	private static final String TRANSFORMATION = "AES/CBC/PKCS5PADDING";
	private static final int AES_CBC_IV_LENGTH_BYTES = 16; //AES in CBC mode always uses a 128 bit IV (16 bytes)

	/**
	 * Encrypts a string
	 *
	 * @param clearText the string to encrypt, not null
	 * @return the encrypted string, null if clearText is null
	 * @throws java.lang.Exception
	 */
	public static String encrypt(String clearText) throws Exception {
		String key = getEncryptionKey();
		return encrypt(clearText, key);
	}

	/**
	 * Encrypts a string
	 *
	 * @param clearText the string to encrypt, not null
	 * @param key the encryption key to use
	 * @return the encrypted string, null if clearText is null
	 * @throws java.lang.Exception
	 */
	public static String encrypt(String clearText, String key) throws Exception {
		if (clearText == null) {
			return null;
		}

		if (StringUtils.isBlank(key)) {
			key = getEncryptionKey();
		}

		//use random IV that will be prepended to the cipher text
		//so that the same string generates different cipher text
		byte[] IVBytes = RandomUtils.nextBytes(AES_CBC_IV_LENGTH_BYTES); //can use SecureRandom but that may block if there's insufficient entropy

		IvParameterSpec ivParameterSpec = new IvParameterSpec(IVBytes);
		SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
		Cipher cipher = Cipher.getInstance(TRANSFORMATION);

		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
		byte[] encryptedBytes = cipher.doFinal(clearText.getBytes("UTF-8"));
		byte[] finalEncryptedBytes = ArrayUtils.addAll(IVBytes, encryptedBytes);
		return Base64.encodeBase64String(finalEncryptedBytes);
	}

	/**
	 * Decrypts a string
	 *
	 * @param cipherText the encrypted string
	 * @return the decrypted string, null if cipherText is null
	 * @throws java.lang.Exception
	 */
	public static String decrypt(String cipherText) throws Exception {
		String key = getEncryptionKey();
		return decrypt(cipherText, key);
	}

	/**
	 * Decrypts a string
	 *
	 * @param cipherText the encrypted string
	 * @param key the decryption key to use
	 * @return the decrypted string, null if cipherText is null
	 * @throws java.lang.Exception
	 */
	public static String decrypt(String cipherText, String key) throws Exception {
		if (cipherText == null || cipherText.equals("")) {
			return cipherText;
		}

		if (StringUtils.isBlank(key)) {
			key = getEncryptionKey();
		}

		byte[] encryptedBytes = Base64.decodeBase64(cipherText);
		byte[] IVBytes = ArrayUtils.subarray(encryptedBytes, 0, AES_CBC_IV_LENGTH_BYTES);
		byte[] finalEncryptedBytes = ArrayUtils.subarray(encryptedBytes, AES_CBC_IV_LENGTH_BYTES, encryptedBytes.length);
		IvParameterSpec ivParameterSpec = new IvParameterSpec(IVBytes);
		SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
		Cipher cipher = Cipher.getInstance(TRANSFORMATION);

		cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
		byte[] decryptedBytes = cipher.doFinal(finalEncryptedBytes);
		return new String(decryptedBytes);
	}

	/**
	 * Returns the encryption/decryption key to use
	 *
	 * @return the encryption/decryption key to use
	 */
	private static String getEncryptionKey() {
		String key;
		String newEncryptionKey = Config.getCustomSettings().getNewEncryptionKey();
		if (StringUtils.isNotBlank(newEncryptionKey)) {
			key = newEncryptionKey;
		} else {
			key = DEFAULT_KEY;
		}

		return key;
	}

}
