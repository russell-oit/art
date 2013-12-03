package art.reportgroup;

import java.io.Serializable;
import java.text.Collator;
import java.util.Comparator;

/**
 * Class to represent report groups. Stored in ART_QUERY_GROUPS
 *
 * @author Timothy Anyona
 */
public class ReportGroup implements Serializable, Comparable<ReportGroup> {

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

	@Override
	public String toString() {
		return "ReportGroup{" + "reportGroupId=" + reportGroupId + ", name=" + name + ", description=" + description + '}';
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 29 * hash + this.reportGroupId;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		//should use getters instead of directly accessing fields. in case of ORM use
		//should also use obj instanceof ReportGroup in case of super class, sub class comparison
		//see http://stackoverflow.com/questions/27581/overriding-equals-and-hashcode-in-java
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ReportGroup other = (ReportGroup) obj;
		if (this.reportGroupId != other.reportGroupId) {
			return false;
		}
		return true;
	}

	/* used netbeans to automatically generate equals and hashcode overrides
	 * 
	 //alternative equals implementation shown below
	 //see http://www.javapractices.com/topic/TopicAction.do?Id=17
	 @Override
	 public boolean equals(Object obj) {
	 //check for self-comparison
	 if (this == obj) {
	 return true;
	 }
		
	 if (!(obj instanceof ReportGroup)) {
	 return false;
	 }

	 //cast to native object is now safe
	 ReportGroup other = (ReportGroup) obj;
		 
	 //now a proper field-by-field evaluation can be made
	 if (this.reportGroupId != other.reportGroupId) {
	 return false;
	 }
	 return true;
	 }
	 * 
	 */
	//default string.compareTo may not be appropriate for end user display
	//see http://www.javapractices.com/topic/TopicAction.do?Id=207
	private static int compare(String aThis, String aThat) {
		Collator collator = Collator.getInstance();
		collator.setStrength(Collator.TERTIARY);
		return collator.compare(aThis, aThat);
	}

	@Override
	public int compareTo(ReportGroup o) {
		//Sorting on reportGroupId is natural sorting for ReportGroup.
		//compareTo must match equals i.e. return 0 for where equals method returns true (so must use same fields as equals)
		//see http://java67.blogspot.com/2012/10/how-to-sort-object-in-java-comparator-comparable-example.html

		return this.reportGroupId > o.reportGroupId ? 1 : (this.reportGroupId < o.reportGroupId ? -1 : 0);
	}

	/*
	 * Another implementation or Comparator interface to sort list of ReportGroup object
	 * based upon report group name.
	 */
	public static class OrderByName implements Comparator<ReportGroup> {

		@Override
		public int compare(ReportGroup g1, ReportGroup g2) {
//			return g1.name.compareTo(g2.name);
			return ReportGroup.compare(g1.name, g2.name);
		}
	}
	//example comparator for int field
	/*
	 // Comparator implementation to Sort Order object based on Amount
	 public static class OrderByAmount implements Comparator<Order> {

	 @Override
	 public int compare(Order o1, Order o2) {
	 return o1.amount > o2.amount ? 1 : (o1.amount < o2.amount ? -1 : 0);
	 }
	 }
	 * */
}
