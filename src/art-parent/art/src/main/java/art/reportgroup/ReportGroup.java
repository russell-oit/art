package art.reportgroup;

import java.io.Serializable;

/**
 * Class to represent report groups. Stored in ART_QUERY_GROUPS
 *
 * @author Timothy Anyona
 */
public class ReportGroup implements Serializable {

	private static final long serialVersionUID = 1L;
	private int reportGroupId;
	private String name;
	private String description;

	/**
	 * Get the value of description
	 *
	 * @return the value of description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the value of description
	 *
	 * @param description new value of description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Get the value of name
	 *
	 * @return the value of name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the value of name
	 *
	 * @param name new value of name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the value of reportGroupId
	 *
	 * @return the value of reportGroupId
	 */
	public int getReportGroupId() {
		return reportGroupId;
	}

	/**
	 * Set the value of reportGroupId
	 *
	 * @param reportGroupId new value of reportGroupId
	 */
	public void setReportGroupId(int reportGroupId) {
		this.reportGroupId = reportGroupId;
	}
}
