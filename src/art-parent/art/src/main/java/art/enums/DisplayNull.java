package art.enums;

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
		return "settings.displayNullOption." + this.toString();
	}
}
