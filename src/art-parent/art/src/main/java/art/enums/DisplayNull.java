package art.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Enum for null value display setting
 *
 * @author Timothy Anyona
 */
public enum DisplayNull {

	Yes("Yes"), NoNumbersAsBlank("NoNumbersAsBlank"), NoNumbersAsZero("NoNumbersAsZero");
	private String value;

	private DisplayNull(String value) {
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
	public static List<DisplayNull> list() {
		//use a new list as Arrays.asList() returns a fixed-size list. can't add or remove from it
		List<DisplayNull> items = new ArrayList<>();
		items.addAll(Arrays.asList(values()));
		return items;
	}

	/**
	 * Get enum object based on a string
	 *
	 * @param value
	 * @return
	 */
	public static DisplayNull getEnum(String value) {
		for (DisplayNull v : values()) {
			if (v.value.equalsIgnoreCase(value)) {
				return v;
			}
		}
		return Yes; //default
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

	/**
	 * Get description message string for use in the user interface.
	 *
	 * @return
	 */
	public String getLocalisedDescription() {
		return "displayNull.option." + value;
	}
}
