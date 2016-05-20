package art.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents supported ldap authentication methods
 *
 * @author Timothy Anyona
 */
public enum LdapAuthenticationMethod {

	Simple("Simple"), DigestMD5("Digest-MD5");
	
	private final String value;

	private LdapAuthenticationMethod(String value) {
		this.value = value;
	}

	/**
	 * Returns this enum option's value
	 *
	 * @return this enum option's value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Returns all enum options
	 *
	 * @return all enum options
	 */
	public static List<LdapAuthenticationMethod> list() {
		//use a new list as Arrays.asList() returns a fixed-size list. can't add or remove from it
		List<LdapAuthenticationMethod> items = new ArrayList<>();
		items.addAll(Arrays.asList(values()));
		return items;
	}

	/**
	 * Converts a value to an enum. If the conversion fails, Simple is returned
	 *
	 * @param value the value to convert
	 * @return the enum option that corresponds to the value
	 */
	public static LdapAuthenticationMethod toEnum(String value) {
		return toEnum(value, Simple);
	}

	/**
	 * Converts a value to an enum. If the conversion fails, the specified
	 * default is returned
	 *
	 * @param value the value to convert
	 * @param defaultEnum the default enum option to use
	 * @return the enum option that corresponds to the value
	 */
	public static LdapAuthenticationMethod toEnum(String value, LdapAuthenticationMethod defaultEnum) {
		for (LdapAuthenticationMethod v : values()) {
			if (v.value.equalsIgnoreCase(value)) {
				return v;
			}
		}
		return defaultEnum;
	}

	/**
	 * Returns this enum option's description
	 *
	 * @return
	 */
	public String getDescription() {
		return value;
	}
}
