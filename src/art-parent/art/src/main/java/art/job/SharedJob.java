/*
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.job;

import java.util.Date;

/**
 * Represents a shared job
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
}
