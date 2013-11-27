package art.enums;

/**
 * Enum for smtp connection encryption methods
 *
 * @author Timothy Anyona
 */
public enum SmtpConnectionEncryptionMethod {

	None("None"), StartTLS("StartTLS");
	private String value;

	private SmtpConnectionEncryptionMethod(String value) {
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
	public static SmtpConnectionEncryptionMethod getEnum(String value) {
		for (SmtpConnectionEncryptionMethod v : values()) {
			if (v.value.equalsIgnoreCase(value)) {
				return v;
			}
		}
		return None; //default
	}
}
