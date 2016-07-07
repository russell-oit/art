package art.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents application authentication methods
 *
 * @author Timothy Anyona
 */
public enum ArtAuthenticationMethod {

	//values used by the login page
	Internal("Internal"), Auto("Auto"), WindowsDomain("windowsDomain"),
	Database("Database"), LDAP("LDAP"), CAS("CAS"),
	//values used internally by ART
	Repository("repository"), Custom("custom"), Public("public");

	private final String value;

	private ArtAuthenticationMethod(String value) {
		this.value = value;
	}

	/**
	 * Returns this enum option's value
	 *
	 * @return this enum option's value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Returns all enum options
	 *
	 * @return all enum options
	 */
	public static List<ArtAuthenticationMethod> list() {
		//use a new list as Arrays.asList() returns a fixed-size list. can't add or remove from it
		List<ArtAuthenticationMethod> items = new ArrayList<>();
		items.addAll(Arrays.asList(values()));
		return items;
	}

	/**
	 * Converts a value to an enum. If the conversion fails, Internal is
	 * returned
	 *
	 * @param value the value to convert
	 * @return the enum option that corresponds to the value
	 */
	public static ArtAuthenticationMethod toEnum(String value) {
		return toEnum(value, Internal);
	}

	/**
	 * Converts a value to an enum. If the conversion fails, the specified
	 * default is returned
	 *
	 * @param value the value to convert
	 * @param defaultEnum the default enum option to use
	 * @return the enum option that corresponds to the value
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
	 * Returns this enum option's description
	 *
	 * @return enum option description
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
