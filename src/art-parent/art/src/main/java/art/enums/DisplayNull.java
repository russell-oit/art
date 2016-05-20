package art.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents display null settings
 *
 * @author Timothy Anyona
 */
public enum DisplayNull {

	Yes("Yes"), NoNumbersAsBlank("NoNumbersAsBlank"), NoNumbersAsZero("NoNumbersAsZero");
	
	private final String value;

	private DisplayNull(String value) {
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
	public static List<DisplayNull> list() {
		//use a new list as Arrays.asList() returns a fixed-size list. can't add or remove from it
		List<DisplayNull> items = new ArrayList<>();
		items.addAll(Arrays.asList(values()));
		return items;
	}

	/**
	 * Converts a value to an enum. If the conversion fails, Yes is returned
	 *
	 * @param value the value to convert
	 * @return the enum option that corresponds to the value
	 */
	public static DisplayNull toEnum(String value) {
		return toEnum(value, Yes);
	}

	/**
	 * Convert a value to an enum. If the conversion fails, the specified
	 * default is returned
	 *
	 * @param value the value to convert
	 * @param defaultEnum the default enum option to use
	 * @return the enum option that corresponds to the value
	 */
	public static DisplayNull toEnum(String value, DisplayNull defaultEnum) {
		for (DisplayNull v : values()) {
			if (v.value.equalsIgnoreCase(value)) {
				return v;
			}
		}
		return defaultEnum;
	}

	/**
	 * Returns this enum option's description
	 *
	 * @return this enum option's description
	 */
	public String getDescription() {
		return value;
	}

	/**
	 * Returns this enum option's i18n message string
	 *
	 * @return this enum option's i18n message string
	 */
	public String getLocalizedDescription() {
		return "displayNull.option." + value;
	}
}
