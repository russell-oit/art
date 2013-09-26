package art.enums;

/**
 * Enum for login or authentication methods
 *
 * @author Timothy Anyona
 */
public enum LoginMethod {

	Internal("internal"), Auto("auto"), WindowsDomain("windowsDomain"),
	Ldap("ldap"), Database("database");
	private String value;

	private LoginMethod(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static LoginMethod getEnum(String method) {
		for (LoginMethod v : values()) {
			if (v.value.equalsIgnoreCase(method)) {
				return v;
			}
		}
		return Internal; //default
	}
}
