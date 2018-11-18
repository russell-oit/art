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
import art.settings.CustomSettings;
import art.settings.EncryptionPassword;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Objects;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

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
	//https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html

	public static final String DEFAULT_KEY = "XH6YUHlrofcQDZjd"; // 128 bit key (16 bytes)
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
		String key = null;
		EncryptionPassword encryptionPassword = null;
		return encrypt(clearText, key, encryptionPassword);
	}

	/**
	 * Encrypts a string
	 *
	 * @param clearText the string to encrypt, not null
	 * @param key the key to use. If null, the current key will be used
	 * @param encryptionPassword the encryption password configuration. If null,
	 * the current configuration will be used.
	 * @return the encrypted string, null if clearText is null
	 * @throws java.lang.Exception
	 */
	public static String encrypt(String clearText, String key,
			EncryptionPassword encryptionPassword) throws Exception {

		if (clearText == null) {
			return null;
		}

		String effectiveKey = getEffectiveKey(key);
		EncryptionPassword effectiveEncryptionPassword = getEffectiveEncryptionPassword(encryptionPassword);

		SecretKey secretKey = generateSecretKey(effectiveKey, effectiveEncryptionPassword);

		//use random IV that will be prepended to the cipher text
		//so that the same string generates different cipher text
		byte[] IVBytes = RandomUtils.nextBytes(AES_CBC_IV_LENGTH_BYTES); //can use SecureRandom but that may block if there's insufficient entropy
		IvParameterSpec ivParameterSpec = new IvParameterSpec(IVBytes);

		Cipher cipher = Cipher.getInstance(TRANSFORMATION);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

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
		String key = null;
		EncryptionPassword encryptionPassword = null;
		return decrypt(cipherText, key, encryptionPassword);
	}

	/**
	 * Decrypts a string
	 *
	 * @param cipherText the encrypted string
	 * @param key the key to use. If null, the current key will be used.
	 * @param encryptionPassword the encryption password configuration. If null,
	 * the current configuration will be used.
	 * @return the decrypted string, null if cipherText is null
	 * @throws java.lang.Exception
	 */
	public static String decrypt(String cipherText, String key,
			EncryptionPassword encryptionPassword) throws Exception {

		if (cipherText == null || cipherText.equals("")) {
			return cipherText;
		}

		String effectiveKey = getEffectiveKey(key);
		EncryptionPassword effectiveEncryptionPassword = getEffectiveEncryptionPassword(encryptionPassword);

		SecretKey secretKey = generateSecretKey(effectiveKey, effectiveEncryptionPassword);

		byte[] encryptedBytes = Base64.decodeBase64(cipherText);
		byte[] IVBytes = ArrayUtils.subarray(encryptedBytes, 0, AES_CBC_IV_LENGTH_BYTES);
		byte[] finalEncryptedBytes = ArrayUtils.subarray(encryptedBytes, AES_CBC_IV_LENGTH_BYTES, encryptedBytes.length);

		IvParameterSpec ivParameterSpec = new IvParameterSpec(IVBytes);

		Cipher cipher = Cipher.getInstance(TRANSFORMATION);
		cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

		byte[] decryptedBytes = cipher.doFinal(finalEncryptedBytes);

		return new String(decryptedBytes);
	}

	/**
	 * Returns the current encryption/decryption key in use
	 *
	 * @return the current encryption/decryption key in use
	 */
	public static String getCurrentEncryptionKey() {
		String key;

		String settingsEncryptionKey = null;
		CustomSettings customSettings = Config.getCustomSettings();
		if (customSettings != null) {
			//may be null if encrypt/decrypt method run from command line while art is not started
			settingsEncryptionKey = customSettings.getEncryptionKey();
		}

		if (StringUtils.isEmpty(settingsEncryptionKey)) {
			key = DEFAULT_KEY;
		} else {
			key = settingsEncryptionKey;
		}

		return key;
	}

	/**
	 * Returns the effective encryption key to use
	 *
	 * @param key the passed key
	 * @return the effective encryption key to use
	 */
	private static String getEffectiveKey(String key) {
		String effectiveKey;

		if (StringUtils.isEmpty(key)) {
			effectiveKey = getCurrentEncryptionKey();
		} else {
			effectiveKey = key;
		}

		return effectiveKey;
	}

	/**
	 * Returns the effective encryption password configuration to use
	 *
	 * @param encryptionPassword the passed configuration. If null, the current
	 * configuration will be used.
	 * @return the effective encryption password configuration to use
	 */
	private static EncryptionPassword getEffectiveEncryptionPassword(EncryptionPassword encryptionPassword) {
		EncryptionPassword effectiveEncryptionPassword = null;

		if (encryptionPassword == null) {
			CustomSettings customSettings = Config.getCustomSettings();
			if (customSettings != null) {
				effectiveEncryptionPassword = customSettings.getEncryptionPassword();
			}
		} else {
			effectiveEncryptionPassword = encryptionPassword;
		}

		return effectiveEncryptionPassword;
	}

	/**
	 * Returns the secret key object to use
	 *
	 * @param key the encryption key string
	 * @param encryptionPassword the encryption password configuration
	 * @return the secret key object
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	private static SecretKey generateSecretKey(String key, EncryptionPassword encryptionPassword)
			throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeySpecException {

		if (encryptionPassword == null || StringUtils.isEmpty(encryptionPassword.getPassword())) {
			return generateSecretKeyUsingKey(key);
		} else {
			return generateSecretKeyUsingPassword(encryptionPassword);
		}
	}

	/**
	 * Returns the secret key object to use
	 *
	 * @param key the encryption key string
	 * @return the secret key object
	 * @throws UnsupportedEncodingException
	 */
	private static SecretKey generateSecretKeyUsingKey(String key) throws UnsupportedEncodingException {
		Objects.requireNonNull(key, "key must not be null");
		SecretKey secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
		return secretKey;
	}

	/**
	 * Returns the secret key object to use
	 *
	 * @param encryptionPassword the encryption password configuration
	 * @return the secret key object
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws UnsupportedEncodingException
	 */
	private static SecretKey generateSecretKeyUsingPassword(EncryptionPassword encryptionPassword)
			throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException {

		Objects.requireNonNull(encryptionPassword, "encryptionPassword must not be null");

		String password = encryptionPassword.getPassword();
		Objects.requireNonNull(password, "password must not be null");

		int keyLength = encryptionPassword.getKeyLength();

		//https://stackoverflow.com/questions/992019/java-256-bit-aes-password-based-encryption
		//https://github.com/martinwithaar/Encryptor4j/blob/master/src/main/java/org/encryptor4j/factory/AbsKeyFactory.java
		//https://cryptosense.com/blog/parameter-choice-for-pbkdf2/
		//https://stackoverflow.com/questions/44223813/aes-custom-password-key
		final int ITERATION_COUNT = 10_000;
		final String SALT = "(cFp((.]1x)60Re3w";
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		KeySpec spec = new PBEKeySpec(password.toCharArray(), SALT.getBytes("UTF-8"), ITERATION_COUNT, keyLength);
		SecretKey tmp = factory.generateSecret(spec);
		SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

		return secret;
	}

	/**
	 * Allow testing of encryption/decryption from the command line
	 *
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		//https://stackoverflow.com/questions/18093928/what-does-could-not-find-or-load-main-class-mean
		//https://stackoverflow.com/questions/219585/including-all-the-jars-in-a-directory-within-the-java-classpath
		//from WEB-INF\classes\ directory
		//windows: java -cp "../lib/*;../etc/*;." art.encryption.AesEncryptor
		//linux: java -cp "../lib/*:../etc/*:." art.encryption.AesEncryptor 

		//https://commons.apache.org/proper/commons-cli/usage.html
		//https://commons.apache.org/proper/commons-cli/properties.html
		//https://stackoverflow.com/questions/11741625/apache-commons-cli-ordering-help-options
		//https://stackoverflow.com/questions/48205610/java-commons-cli-overriding-required-parameters-while-using-help-parameter
		//https://self-learning-java-tutorial.blogspot.com/2016/12/commons-cli-parsing-command-line-options.html
		//https://self-learning-java-tutorial.blogspot.com/2016/12/commons-cli-optiongroup-group-mutually.html
		Options options = new Options();
		HelpFormatter helpFormatter = new HelpFormatter();

		try {
			final String ENCRYPT_OPTION = "e";
			final String DECRYPT_OPTION = "d";
			final String TEXT_OPTION = "t";
			final String KEY_OPTION = "k";
			final String HELP_OPTION = "h";
			final String PASSWORD_OPTION = "p";
			final String KEY_LENGTH_OPTION = "l";

			Option helpOption = Option.builder(HELP_OPTION)
					.longOpt("help")
					.desc("show usage")
					.build();

			Option encryptOption = Option.builder(ENCRYPT_OPTION)
					.longOpt("encrypt")
					.desc("perform encryption")
					.build();

			Option decryptOption = Option.builder(DECRYPT_OPTION)
					.longOpt("decrypt")
					.desc("perform decryption")
					.build();

			Option textOption = Option.builder(TEXT_OPTION)
					.longOpt("text")
					.desc("the text to encrypt/decrypt")
					.hasArg()
					.required()
					.build();

			Option keyOption = Option.builder(KEY_OPTION)
					.longOpt("key")
					.desc("the encryption/decryption key")
					.hasArg()
					.build();

			Option passwordOption = Option.builder(PASSWORD_OPTION)
					.longOpt("password")
					.desc("the encryption/decryption password")
					.hasArg()
					.build();

			Option keyLengthOption = Option.builder(KEY_LENGTH_OPTION)
					.longOpt("key-length")
					.desc("the key length. used with the password option")
					.hasArg()
					.build();

			OptionGroup actionGroup = new OptionGroup();
			actionGroup.addOption(encryptOption);
			actionGroup.addOption(decryptOption);
			actionGroup.setRequired(true);

			options.addOptionGroup(actionGroup);
			options.addOption(textOption);
			options.addOption(keyOption);
			options.addOption(helpOption);
			options.addOption(passwordOption);
			options.addOption(keyLengthOption);

			//handle optional help option
			//https://stackoverflow.com/questions/10798208/commons-cli-required-groups
			Options options1 = new Options();
			options1.addOption(helpOption);

			CommandLineParser commandLineParser = new DefaultParser();
			//https://commons.apache.org/proper/commons-cli/javadocs/api-1.3.1/org/apache/commons/cli/DefaultParser.html#stopAtNonOption
			//also options1 options must come before others in order to be effective
			boolean stopAtNonOption = true;
			CommandLine commandLine1 = commandLineParser.parse(options1, args, stopAtNonOption);

			if (ArrayUtils.isNotEmpty(commandLine1.getOptions())) {
				if (commandLine1.hasOption(HELP_OPTION)) {
					helpFormatter.printHelp("AesEncryptor", options);
				}
			} else {
				CommandLine commandLine = commandLineParser.parse(options, args);

				if (!commandLine.hasOption(KEY_OPTION) && !commandLine.hasOption(PASSWORD_OPTION)) {
					System.out.println("Must specify either key or password option");
					helpFormatter.printHelp("AesEncryptor", options);
					return;
				} else if (commandLine.hasOption(KEY_OPTION) && commandLine.hasOption(PASSWORD_OPTION)) {
					System.out.println("Must specify only key or password option");
					helpFormatter.printHelp("AesEncryptor", options);
					return;
				} else if (commandLine.hasOption(PASSWORD_OPTION)
						&& !commandLine.hasOption(KEY_LENGTH_OPTION)) {
					System.out.println("Must specify key length option with the password option");
					helpFormatter.printHelp("AesEncryptor", options);
					return;
				}

				String text = commandLine.getOptionValue(TEXT_OPTION);
				String key = commandLine.getOptionValue(KEY_OPTION);
				String password = commandLine.getOptionValue(PASSWORD_OPTION);
				int keyLength = NumberUtils.toInt(commandLine.getOptionValue(KEY_LENGTH_OPTION));

				EncryptionPassword encryptionPassword = new EncryptionPassword();
				encryptionPassword.setPassword(password);
				encryptionPassword.setKeyLength(keyLength);

				if ((commandLine.hasOption(KEY_OPTION) && StringUtils.isEmpty(key))
						|| (commandLine.hasOption(PASSWORD_OPTION) && StringUtils.isEmpty(password))) {
					System.out.println("Using key: '" + getCurrentEncryptionKey() + "'");
				}

				if (commandLine.hasOption(ENCRYPT_OPTION)) {
					String encryptedText = encrypt(text, key, encryptionPassword);
					System.out.println("Encrypted text is '" + encryptedText + "'");
				} else {
					String decryptedText = decrypt(text, key, encryptionPassword);
					System.out.println("Decrypted text is '" + decryptedText + "'");
				}
			}
		} catch (ParseException ex) {
			System.out.println(ex.getMessage());
			helpFormatter.printHelp("AesEncryptor", options);
		} catch (Exception ex) {
			ex.printStackTrace(System.out);
		}
	}

}
