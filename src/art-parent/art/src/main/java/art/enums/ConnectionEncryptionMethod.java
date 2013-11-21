package art.enums;

/**
 * Enum for connection encryption methods e.g. smtp connection, ldap connection
 *
 * @author Timothy Anyona
 */
public enum ConnectionEncryptionMethod {

	None("none"), SSL("ssl"), StartTLS("starttls"), Unknown("unknown");
	private String value;

	private ConnectionEncryptionMethod(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static ConnectionEncryptionMethod getEnum(String method) {
		for (ConnectionEncryptionMethod v : values()) {
			if (v.value.equalsIgnoreCase(method)) {
				return v;
			}
		}
		return None; //default
	}
}
