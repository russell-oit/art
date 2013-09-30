package art.job;

import java.util.Date;
import org.apache.commons.lang.StringUtils;

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
		if (isASplitJob()) {
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
		if (isASplitJob()) {
			//split job. get value from the art_user_jobs table
			lastFileName = this.getSharedLastFileName();
		} else {
			//non-split job. get value from jobs table
			lastFileName = this.getLastFileName();
		}

		return lastFileName;
	}
	
	private boolean isASplitJob(){
		boolean splitJob=false;
		if (StringUtils.equalsIgnoreCase(this.getUsesRules(), "Y")
				&& StringUtils.equalsIgnoreCase(this.getAllowSplitting(), "Y")) {
			splitJob=true;
		} 
		
		return splitJob;
	}
}
