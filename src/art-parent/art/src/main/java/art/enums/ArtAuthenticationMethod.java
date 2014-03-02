package art.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Enum for application authentication methods
 *
 * @author Timothy Anyona
 */
public enum ArtAuthenticationMethod {

	//values used by the login page
	Internal("Internal"), Auto("Auto"), WindowsDomain("windowsDomain"),
	Database("Database"), LDAP("LDAP"),
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
	 * Get a list of all enum values
	 *
	 * @return
	 */
	public static List<ArtAuthenticationMethod> list() {
		//use a new list as Arrays.asList() returns a fixed-size list. can't add or remove from it
		List<ArtAuthenticationMethod> items = new ArrayList<>();
		items.addAll(Arrays.asList(values()));
		return items;
	}

	/**
	 * Convert a value to an enum. If the conversion fails, Internal is returned
	 *
	 * @param value
	 * @return
	 */
	public static ArtAuthenticationMethod toEnum(String value) {
		return toEnum(value, Internal);
	}

	/**
	 * Convert a value to an enum. If the conversion fails, the specified
	 * default is returned
	 *
	 * @param value
	 * @param defaultEnum
	 * @return
	 */
	public static ArtAuthenticationMethod toEnum(String value, ArtAuthenticationMethod defaultEnum) {
		for (ArtAuthenticationMethod v : values()) {
			if (v.value.equalsIgnoreCase(value)) {
				return v;
			}
		}
		return defaultEnum;
	}

	/**
	 * Get enum description. In case description needs to be different from
	 * internal value
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
