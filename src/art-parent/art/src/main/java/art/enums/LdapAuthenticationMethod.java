package art.enums;

/**
 * Enum for supported ldap authentication methods
 *
 * @author Timothy Anyona
 */
public enum LdapAuthenticationMethod {

	Simple("simple"), DigestMD5("digest-md5"), Unknown("unknown");
	private String value;

	private LdapAuthenticationMethod(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static LdapAuthenticationMethod getEnum(String method) {
		for (LdapAuthenticationMethod v : values()) {
			if (v.value.equalsIgnoreCase(method)) {
				return v;
			}
		}
		return Simple; //default
	}
}
