package art.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Enum for pdf page size
 *
 * @author Timothy Anyona
 */
public enum PdfPageSize {

	A4("A4"), A4Landscape("A4 Landscape"), Letter("Letter"),
	LetterLandscape("Letter Landscape");

	private String value;

	private PdfPageSize(String value) {
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
	public static List<PdfPageSize> list() {
		//use a new list as Arrays.asList() returns a fixed-size list. can't add or remove from it
		List<PdfPageSize> items = new ArrayList<>();
		items.addAll(Arrays.asList(values()));
		return items;
	}

	/**
	 * Get enum object based on a string
	 *
	 * @param value
	 * @return
	 */
	public static PdfPageSize getEnum(String value) {
		for (PdfPageSize v : values()) {
			if (v.value.equalsIgnoreCase(value)) {
				return v;
			}
		}
		return A4; //default
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
