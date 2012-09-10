/**
 * Encrypt / Decrypt a string given a fixed key
 *
 * Note: this does provide an obfuscation-like protection
 *       for strings, but using this class and the same key
 *       it is possible to decrypt string (i.e. it is not
 *       as sure as private/public keys)
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
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encrypt / Decrypt a string given a fixed key.
 *
 * Note: this does provide an obfuscation-like protection
 *       for strings, but using this class and the same key
 *       it is possible to decrypt string (i.e. it is not
 *       as sure as private/public keys)
 * 
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class Encrypter {

    final static Logger logger = LoggerFactory.getLogger(Encrypter.class);
    private static String staticKey = "d-jhbgy&5153tygo8176!"; // this is used as an additional static key
    private static String defaultPassword = "1tra"; //to allow use and single point of replacing the default password

    /**
     * Encrypt overload that uses default password
     * 
     * @param cleartext
     * @return cipher text
     */
    public static String encrypt(String cleartext) {
        return encrypt(cleartext, defaultPassword);
    }

    /** Encrypt the 'creartext' string using the given key
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
            return org.apache.commons.codec.binary.Base64.encodeBase64String(enc);
            //return new sun.misc.BASE64Encoder().encode(enc);
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

    /** D-ecrypt the 'cryptedtext' string using the given key
     * 
     * @param cryptedtext 
     * @param key 
     * @return clear text
     */
    public static String decrypt(String cryptedtext, String key) {
        try {
            Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
            pbeCipher.init(Cipher.DECRYPT_MODE, getKey(key), getParamSpec());
            // Decode base64 to get bytes
            //byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(cryptedtext);
            byte[] dec = org.apache.commons.codec.binary.Base64.decodeBase64(cryptedtext);
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
     * Hash a password using the algorithm specified and return the hashed password.
    
     * @param clearText clear text password
     * @param algorithm algorithm to use
     * @return hashed password
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException  
     */
    public static String HashPassword(String clearText, String algorithm)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {

        String hashedPassword;

        // Algorithm MD5 will generate a 128bit (16 byte) digested byte[]
        // otherwise, SHA-1 algorithm  will produce a 160bit (20 byte) digested byte[]

        if (algorithm == null || clearText == null) {
            hashedPassword = clearText;
        } else if (algorithm.equals("none")) {
            hashedPassword = clearText;
        } else if (algorithm.equals("bcrypt")) {
            //NOTE: bcrypt only uses the first 72 bytes so long texts with the first 72 bytes the same will give the same hash
            hashedPassword = BCrypt.hashpw(clearText, BCrypt.gensalt(6)); //4-31. increase gensalt factor to have slower password generation
        } else {
            //either md5,sha-1,sha-256,sha-512
            MessageDigest mdg = MessageDigest.getInstance(algorithm);

            // To avoid the use of the (implicit) platform-specific encoding
            // that can undermine portability of an existent ART instance
            // we enforce the "UTF-8" encoding

            byte[] hashedMsg = mdg.digest(clearText.getBytes("UTF-8"));
            // The String is now digested

            int v;
            StringBuilder d = new StringBuilder(22);
            for (int i = 0; i < hashedMsg.length; i++) {
                v = hashedMsg[i] & 0xFF;
                if (v < 16) {
                    d.append("0");
                }
                d.append(Integer.toString(v, 16));
            }

            hashedPassword = d.toString();
        }

        return hashedPassword;
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

        boolean verified = false;

        if (StringUtils.equals(algorithm,"bcrypt")) {
            verified = BCrypt.checkpw(clearText, hashedPassword);
        } else if (StringUtils.equals(hashedPassword,HashPassword(clearText, algorithm))) {
            verified = true;
        }

        return verified;
    }
}
