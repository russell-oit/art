package art.reportgroup;

import org.apache.commons.collections.Predicate;

/**
 * Class to filter an iterator that contains report groups by the group id
 *
 * @author Timothy Anyona
 */
public class ReportGroupEqualsFilter implements Predicate {

	private int reportGroupId;

	public ReportGroupEqualsFilter(int reportGroupId) {
		this.reportGroupId = reportGroupId;
	}

	@Override
	public boolean evaluate(Object o) {
		if (!(o instanceof ReportGroup)) {
			return false;
		}

		ReportGroup group = (ReportGroup) o;
		if (group.getReportGroupId() == reportGroupId) {
			return true;
		} else {
			return false;
		}
	}
}
