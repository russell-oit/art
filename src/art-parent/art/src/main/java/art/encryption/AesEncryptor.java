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

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides encryption and decryption of strings using the AES-128 algorithm
 *
 * @author Timothy Anyona
 */
public class AesEncryptor {
	//http://javapointers.com/tutorial/how-to-encrypt-and-decrypt-using-aes-in-java/
	//https://stackoverflow.com/questions/15554296/simple-java-aes-encrypt-decrypt-example
	//http://www.madirish.net/561
	//https://www.grc.com/passwords.htm

	private static final Logger logger = LoggerFactory.getLogger(AesEncryptor.class);
	private static final String KEY = "XH6YUHlrofcQDZjd"; // 128 bit key (16 bytes)
	private static final int AES_128_IV_LENGTH = 16; //16 bytes

	/**
	 * Encrypts a string
	 *
	 * @param clearText the string to encrypt, not null
	 * @return the encrypted string, null if an error occurred or if clearText
	 * is null
	 */
	public static String encrypt(String clearText) {
		if (clearText == null) {
			return null;
		}

		//use random IV that will be prepended to the cipher text
		//so that the same string generates different cipher text
		byte[] IVBytes = RandomUtils.nextBytes(AES_128_IV_LENGTH); //can use SecureRandom but that may block if there's insufficient entropy

		try {
			IvParameterSpec ivParameterSpec = new IvParameterSpec(IVBytes);
			SecretKeySpec secretKeySpec = new SecretKeySpec(KEY.getBytes("UTF-8"), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");

			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
			byte[] encryptedBytes = cipher.doFinal(clearText.getBytes("UTF-8"));
			byte[] finalEncryptedBytes = ArrayUtils.addAll(IVBytes, encryptedBytes);
			return Base64.encodeBase64String(finalEncryptedBytes);
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
			logger.error("Error", ex);
		}

		return null;
	}

	/**
	 * Decrypts a string
	 *
	 * @param cipherText the encrypted string
	 * @return the decrypted string, null if an error occurred or if cipherText
	 * is null
	 */
	public static String decrypt(String cipherText) {
		if (cipherText == null) {
			return null;
		}

		try {
			byte[] encryptedBytes = Base64.decodeBase64(cipherText);
			byte[] IVBytes = ArrayUtils.subarray(encryptedBytes, 0, AES_128_IV_LENGTH);
			byte[] finalEncryptedBytes = ArrayUtils.subarray(encryptedBytes, AES_128_IV_LENGTH, encryptedBytes.length);
			IvParameterSpec ivParameterSpec = new IvParameterSpec(IVBytes);
			SecretKeySpec secretKeySpec = new SecretKeySpec(KEY.getBytes("UTF-8"), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");

			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
			byte[] decryptedBytes = cipher.doFinal(finalEncryptedBytes);
			return new String(decryptedBytes);
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
			logger.error("Error", ex);
		}

		return null;
	}
	
}
