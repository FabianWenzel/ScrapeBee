package de.scrapebee;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hilfsklasse für sonstige Funktionen
 */
public class Helper {

	/**
	 * Liefert MD5 Hash zu einem String
	 * http://stackoverflow.com/questions/415953/how-can-i-generate-an-md5-hash
	 * @param content
	 * @return MD5 Hashwert
	 * @throws NoSuchAlgorithmException
	 */
	public static String getMD5(String content) throws NoSuchAlgorithmException {
		
		MessageDigest m = MessageDigest.getInstance("MD5");
		m.reset();
		m.update(content.getBytes());
		byte[] digest = m.digest();
		BigInteger bigInt = new BigInteger(1,digest);
		String hashtext = bigInt.toString(16);
		// Now we need to zero pad it if you actually want the full 32 chars.
		while(hashtext.length() < 32 ){
		  hashtext = "0"+hashtext;
		}
		
		return hashtext;
	}
}
