package art.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	 * Get a list of all enum values
	 *
	 * @return
	 */
	public static List<AccessLevel> list() {
		//use a new list as Arrays.asList() returns a fixed-size list. can't add or remove from it
		List<AccessLevel> items = new ArrayList<>();
		items.addAll(Arrays.asList(values()));
		return items;
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
				return "Normal User";
			case ScheduleUser:
				return "Schedule User";
			case JuniorAdmin:
				return "Junior Admin";
			case MidAdmin:
				return "Mid Admin";
			case StandardAdmin:
				return "Standard Admin";
			case SeniorAdmin:
				return "Senior Admin";
			case SuperAdmin:
				return "Super Admin";
			case RepositoryUser:
				return "Repository User";
			default:
				return this.name();
		}
	}
}
