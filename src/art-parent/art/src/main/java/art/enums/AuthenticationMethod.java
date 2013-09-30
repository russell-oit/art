package art.enums;

/**
 * Enum for login or authentication methods
 *
 * @author Timothy Anyona
 */
public enum AuthenticationMethod {

	Internal("internal"), Auto("auto"), WindowsDomain("windowsDomain"),
	Ldap("ldap"), Database("database"), ArtRepository("artRepository");
	private String value;

	private AuthenticationMethod(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static AuthenticationMethod getEnum(String method) {
		for (AuthenticationMethod v : values()) {
			if (v.value.equalsIgnoreCase(method)) {
				return v;
			}
		}
		return Internal; //default
	}
}
