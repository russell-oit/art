package art.enums;

/**
 * Enum for null value display setting
 *
 * @author Timothy Anyona
 */
public enum DisplayNullValue {

	Yes("yes"), NoNumbersAsBlank("no-numbers-blank"), NoNumbersAsZero("no-numbers-zero");
	private String value;

	private DisplayNullValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static DisplayNullValue getEnum(String value) {
		for (DisplayNullValue v : values()) {
			if (v.value.equalsIgnoreCase(value)) {
				return v;
			}
		}
		return Yes; //default
	}
}
