package art.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Enum for ldap connection encryption methods
 *
 * @author Timothy Anyona
 */
public enum LdapConnectionEncryptionMethod {

	None("None"), SSL("SSL"), StartTLS("StartTLS");
	private String value;

	private LdapConnectionEncryptionMethod(String value) {
		this.value = value;
	}

	/**
	 * Get enum value
	 *
	 * @return
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Get a list of all enum values
	 *
	 * @return
	 */
	public static List<LdapConnectionEncryptionMethod> list() {
		//use a new list as Arrays.asList() returns a fixed-size list. can't add or remove from it
		List<LdapConnectionEncryptionMethod> items = new ArrayList<>();
		items.addAll(Arrays.asList(values()));
		return items;
	}

	/**
	 * Convert a value to an enum. If the conversion fails, None is returned
	 *
	 * @param value
	 * @return
	 */
	public static LdapConnectionEncryptionMethod toEnum(String value) {
		return toEnum(value, None);
	}

	/**
	 * Convert a value to an enum. If the conversion fails, the specified
	 * default is returned
	 *
	 * @param value
	 * @param defaultEnum
	 * @return
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
	 * Get enum description. In case description needs to be different from
	 * internal value
	 *
	 * @return
	 */
	public String getDescription() {
		return value;
	}
}
