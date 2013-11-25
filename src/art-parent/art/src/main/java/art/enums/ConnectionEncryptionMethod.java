package art.enums;

/**
 * Enum for ldap connection encryption methods e.g. smtp connection, ldap connection
 *
 * @author Timothy Anyona
 */
public enum ConnectionEncryptionMethod {

	None("None"), StartTLS("StartTLS");
	private String value;

	private ConnectionEncryptionMethod(String value) {
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
	public static ConnectionEncryptionMethod getEnum(String value) {
		for (ConnectionEncryptionMethod v : values()) {
			if (v.value.equalsIgnoreCase(value)) {
				return v;
			}
		}
		return None; //default
	}

	/**
	 * Get enum description. In case description needs to be different from
	 * value. Only used for display in user interface.
	 *
	 * @return
	 */
	public String getDescription() {
		return value;
	}
}
