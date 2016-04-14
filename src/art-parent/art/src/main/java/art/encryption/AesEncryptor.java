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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Timothy Anyona
 */
public class AesEncryptor {
	//http://javapointers.com/tutorial/how-to-encrypt-and-decrypt-using-aes-in-java/
	//https://stackoverflow.com/questions/15554296/simple-java-aes-encrypt-decrypt-example
	//https://www.grc.com/passwords.htm

	private static final Logger logger = LoggerFactory.getLogger(AesEncryptor.class);
	private static final String IV = "9F962822F431B19B"; // 16 bytes IV
	private static final String KEY = "XH6YUHlrofcQDZjd"; // 128 bit key

	public static String encrypt(String clearText) {
		try {
			IvParameterSpec ivParameterSpec = new IvParameterSpec(IV.getBytes("UTF-8"));
			SecretKeySpec secretKeySpec = new SecretKeySpec(KEY.getBytes("UTF-8"), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");

			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
			byte[] encrypted = cipher.doFinal(clearText.getBytes());
			return Base64.encodeBase64String(encrypted);
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
			logger.error("Error", ex);
		}

		return null;
	}

	public static String decrypt(String cipherText) {
		try {
			IvParameterSpec ivParameterSpec = new IvParameterSpec(IV.getBytes("UTF-8"));
			SecretKeySpec secretKeySpec = new SecretKeySpec(KEY.getBytes("UTF-8"), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");

			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
			byte[] decryptedBytes = cipher.doFinal(Base64.decodeBase64(cipherText));
			return new String(decryptedBytes);
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
			logger.error("Error", ex);
		}

		return null;
	}
}
