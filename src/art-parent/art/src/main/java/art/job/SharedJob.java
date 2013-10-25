package art.job;

import java.util.Date;
import org.apache.commons.lang3.StringUtils;

/**
 * Class to represent a shared job. Overrides a few properties
 *
 * @author Timothy Anyona
 */
public class SharedJob extends Job {

	private static final long serialVersionUID = 1L;

	@Override
	public Date getLastEndDate() {
		Date lastEndDate;
		if (isSplitJob()) {
			//split job. get value from the art_user_jobs table
			lastEndDate = this.getSharedLastEndDate();
		} else {
			//non-split job. get value from jobs table
			lastEndDate = this.getLastEndDate();
		}

		return lastEndDate;
	}
	
	@Override
	public String getLastFileName() {
		String lastFileName;
		if (isSplitJob()) {
			//split job. get value from the art_user_jobs table
			lastFileName = this.getSharedLastFileName();
		} else {
			//non-split job. get value from jobs table
			lastFileName = this.getLastFileName();
		}

		return lastFileName;
	}
	
	@Override
	public String getLastRunDetails() {
		String lastRunDetails;
		if (isSplitJob()) {
			//split job. get value from the art_user_jobs table
			lastRunDetails = this.getSharedLastRunDetails();
		} else {
			//non-split job. get value from jobs table
			lastRunDetails = this.getLastRunDetails();
		}

		return lastRunDetails;
	}
	
	private boolean isSplitJob(){
		boolean splitJob=false;
		
		if (StringUtils.equalsIgnoreCase(this.getUsesRules(), "Y")
				&& StringUtils.equalsIgnoreCase(this.getAllowSplitting(), "Y")) {
			splitJob=true;
		} 
		
		return splitJob;
	}
}
