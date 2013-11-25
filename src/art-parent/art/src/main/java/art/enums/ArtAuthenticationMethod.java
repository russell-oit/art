package art.enums;

/**
 * Enum for application authentication methods
 *
 * @author Timothy Anyona
 */
public enum ArtAuthenticationMethod {

	//values used by the login page
	Internal("Internal"), Auto("Auto"), WindowsDomain("windowsDomain"),
	LDAP("LDAP"), Database("Database"),
	//values used internally by ART
	Repository("repository"), Custom("custom"), Public("public");
	private String value;

	private ArtAuthenticationMethod(String value) {
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
	public static ArtAuthenticationMethod getEnum(String value) {
		for (ArtAuthenticationMethod v : values()) {
			if (v.value.equalsIgnoreCase(value)) {
				return v;
			}
		}
		return Internal; //default
	}

	/**
	 * Get enum description. In case description needs to be different from
	 * value. Only used for display in user interface.
	 *
	 * @return
	 */
	public String getDescription() {
		switch (this) {
			case WindowsDomain:
				return "Windows Domain";
			default:
				return value;
		}
	}
}
