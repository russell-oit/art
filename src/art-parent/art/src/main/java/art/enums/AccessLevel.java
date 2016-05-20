package art.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents user access levels
 *
 * @author Timothy Anyona
 */
public enum AccessLevel {

	NormalUser(0), ScheduleUser(5), JuniorAdmin(10), MidAdmin(30),
	StandardAdmin(40), SeniorAdmin(80), SuperAdmin(100), RepositoryUser(-1);
	
	private final int value;

	private AccessLevel(int value) {
		this.value = value;
	}

	/**
	 * Returns this enum option's value
	 *
	 * @return this enum option's value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Returns all enum options
	 *
	 * @return all enum options
	 */
	public static List<AccessLevel> list() {
		//use a new list as Arrays.asList() returns a fixed-size list. can't add or remove from it
		List<AccessLevel> items = new ArrayList<>();
		items.addAll(Arrays.asList(values()));
		return items;
	}

	/**
	 * Converts a value to an enum. If the conversion fails, NormalUser is
	 * returned
	 *
	 * @param value the value to convert
	 * @return the enum option that corresponds to the value
	 */
	public static AccessLevel toEnum(int value) {
		return toEnum(value, NormalUser);
	}

	/**
	 * Converts a value to an enum. If the conversion fails, the specified
	 * default is returned
	 *
	 * @param value the value to convert
	 * @param defaultEnum the default enum option to use
	 * @return the enum option that corresponds to the value
	 */
	public static AccessLevel toEnum(int value, AccessLevel defaultEnum) {
		for (AccessLevel v : values()) {
			if (v.value == value) {
				return v;
			}
		}
		return defaultEnum;
	}

	/**
	 * Returns this enum option's description
	 *
	 * @return enum option description
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
