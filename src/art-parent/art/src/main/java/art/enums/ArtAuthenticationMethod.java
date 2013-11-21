package art.enums;

/**
 * Enum for application authentication methods
 *
 * @author Timothy Anyona
 */
public enum ArtAuthenticationMethod {

	//values used by the login page
	Internal("internal"), Auto("auto"), WindowsDomain("windowsDomain"),
	Ldap("ldap"), Database("database"),
	//values used internally by ART
	Repository("repository"), Custom("custom"), Public("public"), Unknown("unknown");
	private String value;

	private ArtAuthenticationMethod(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static ArtAuthenticationMethod getEnum(String method) {
		for (ArtAuthenticationMethod v : values()) {
			if (v.value.equalsIgnoreCase(method)) {
				return v;
			}
		}
		return Internal; //default
	}
}
