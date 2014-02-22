package art.enums;

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
		return Arrays.asList(values());
	}

	/**
	 * Get enum object based on a string
	 *
	 * @param value
	 * @return
	 */
	public static LdapConnectionEncryptionMethod getEnum(String value) {
		for (LdapConnectionEncryptionMethod v : values()) {
			if (v.value.equalsIgnoreCase(value)) {
				return v;
			}
		}
		return None; //default
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
