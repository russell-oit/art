package art.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents ldap connection encryption methods
 *
 * @author Timothy Anyona
 */
public enum LdapConnectionEncryptionMethod {

	None("None"), SSL("SSL"), StartTLS("StartTLS");
	
	private final String value;

	private LdapConnectionEncryptionMethod(String value) {
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
	public static List<LdapConnectionEncryptionMethod> list() {
		//use a new list as Arrays.asList() returns a fixed-size list. can't add or remove from it
		List<LdapConnectionEncryptionMethod> items = new ArrayList<>();
		items.addAll(Arrays.asList(values()));
		return items;
	}

	/**
	 * Converts a value to an enum. If the conversion fails, None is returned
	 *
	 * @param value the value to convert
	 * @return the enum option that corresponds to the value
	 */
	public static LdapConnectionEncryptionMethod toEnum(String value) {
		return toEnum(value, None);
	}

	/**
	 * Convert a value to an enum. If the conversion fails, the specified
	 * default is returned
	 *
	 * @param value the value to convert
	 * @param defaultEnum the default enum option to use
	 * @return the enum option that corresponds to the value
	 */
	public static LdapConnectionEncryptionMethod toEnum(String value, LdapConnectionEncryptionMethod defaultEnum) {
		for (LdapConnectionEncryptionMethod v : values()) {
			if (v.value.equalsIgnoreCase(value)) {
				return v;
			}
		}
		return defaultEnum;
	}

	/**
	 * Returns this enum option's description
	 *
	 * @return this enum option's description
	 */
	public String getDescription() {
		return value;
	}
}
