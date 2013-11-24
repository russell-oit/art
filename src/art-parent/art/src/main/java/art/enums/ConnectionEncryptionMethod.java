package art.enums;

/**
 * Enum for connection encryption methods e.g. smtp connection, ldap connection
 *
 * @author Timothy Anyona
 */
public enum ConnectionEncryptionMethod {

	None("none"), SSL("ssl"), StartTLS("starttls");
	private String value;

	private ConnectionEncryptionMethod(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static ConnectionEncryptionMethod getEnum(String value) {
		for (ConnectionEncryptionMethod v : values()) {
			if (v.value.equalsIgnoreCase(value)) {
				return v;
			}
		}
		return None; //default
	}
}
