package art.enums;

/**
 * Enum for pdf page size
 *
 * @author Timothy Anyona
 */
public enum PdfPageSize {

	A4("A4"), A4Landscape("A4-landscape"), Letter("Letter"),
	LetterLandscape("Letter-landscape"), Unknown("unknown");
	
	private String value;

	private PdfPageSize(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static PdfPageSize getEnum(String method) {
		for (PdfPageSize v : values()) {
			if (v.value.equalsIgnoreCase(method)) {
				return v;
			}
		}
		return A4; //default
	}
}
