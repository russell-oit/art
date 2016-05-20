package art.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents pdf page sizes
 *
 * @author Timothy Anyona
 */
public enum PdfPageSize {

	A4("A4"), A4Landscape("A4 Landscape"), Letter("Letter"), LetterLandscape("Letter Landscape");

	private final String value;

	private PdfPageSize(String value) {
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
	public static List<PdfPageSize> list() {
		//use a new list as Arrays.asList() returns a fixed-size list. can't add or remove from it
		List<PdfPageSize> items = new ArrayList<>();
		items.addAll(Arrays.asList(values()));
		return items;
	}

	/**
	 * Converts a value to an enum. If the conversion fails, A4 is returned
	 *
	 * @param value the value to convert
	 * @return the enum option that corresponds to the value
	 */
	public static PdfPageSize toEnum(String value) {
		return toEnum(value, A4);
	}

	/**
	 * Converts a value to an enum. If the conversion fails, the specified
	 * default is returned
	 *
	 * @param value the value to convert
	 * @param defaultEnum the default enum option to use
	 * @return the enum option that corresponds to the value
	 */
	public static PdfPageSize toEnum(String value, PdfPageSize defaultEnum) {
		for (PdfPageSize v : values()) {
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
}
