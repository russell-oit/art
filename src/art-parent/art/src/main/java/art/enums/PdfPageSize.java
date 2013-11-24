package art.enums;

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

	public String getValue() {
		return value;
	}

	public static PdfPageSize getEnum(String value) {
		for (PdfPageSize v : values()) {
			if (v.value.equalsIgnoreCase(value)) {
				return v;
			}
		}
		return A4; //default
	}
}
