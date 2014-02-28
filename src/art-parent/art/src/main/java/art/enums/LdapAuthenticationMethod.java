package art.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Enum for supported ldap authentication methods
 *
 * @author Timothy Anyona
 */
public enum LdapAuthenticationMethod {

	Simple("Simple"), DigestMD5("Digest-MD5");
	private String value;

	private LdapAuthenticationMethod(String value) {
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
	public static List<LdapAuthenticationMethod> list() {
		//use a new list as Arrays.asList() returns a fixed-size list. can't add or remove from it
		List<LdapAuthenticationMethod> items = new ArrayList<>();
		items.addAll(Arrays.asList(values()));
		return items;
	}

	/**
	 * Get enum object based on a string
	 *
	 * @param value
	 * @return
	 */
	public static LdapAuthenticationMethod getEnum(String value) {
		for (LdapAuthenticationMethod v : values()) {
			if (v.value.equalsIgnoreCase(value)) {
				return v;
			}
		}
		return Simple; //default
	}

	/**
	 * Get enum description. In case description needs to be different from
	 * internal value
	 *
	 * @return
	 */
	public String getDescription() {
		return value;
	}
}
