package art.enums;

/**
 * Enum for ldap connection encryption methods
 *
 * @author Timothy Anyona
 */
public enum LdapConnectionEncryptionMethod {

	//include ssl when secure unboundid ssl authentication code is done
	None("None"), StartTLS("StartTLS");
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
}
