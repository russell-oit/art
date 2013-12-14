package art.enums;

/**
 * Enum for user access levels
 *
 * @author Timothy Anyona
 */
public enum AccessLevel {

	NormalUser(0), ScheduleUser(5), JuniorAdmin(10), MidAdmin(30),
	StandardAdmin(40), SeniorAdmin(80), SuperAdmin(100), RepositoryUser(-1);
	private int value;

	private AccessLevel(int value) {
		this.value = value;
	}

	/**
	 * Get enum value
	 *
	 * @return
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Get enum object based on a value
	 *
	 * @param value
	 * @return
	 */
	public static AccessLevel getEnum(int value) {
		for (AccessLevel v : values()) {
			if (v.value == value) {
				return v;
			}
		}
		return NormalUser;
	}

	/**
	 * Get enum description. In case description needs to be different from
	 * internal value
	 *
	 * @return
	 */
	public String getDescription() {
		switch (this) {
			case NormalUser:
				return "NormalUser";
			case ScheduleUser:
				return "ScheduleUser";
			case JuniorAdmin:
				return "JuniorAdmin";
			case MidAdmin:
				return "MidAdmin";
			case StandardAdmin:
				return "StandardAdmin";
			case SeniorAdmin:
				return "SeniorAdmin";
			case SuperAdmin:
				return "SuperAdmin";
			case RepositoryUser:
				return "RepositoryUser";
			default:
				return String.valueOf(value);
		}
	}
}
