/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
		if (isSplitJob()) {
			//split job. get value from the art_user_jobs table
			return sharedLastEndDate;
		} else {
			//non-split job. get value from jobs table
			return lastEndDate;
		}
	}
	
	@Override
	public String getLastFileName() {
		if (isSplitJob()) {
			//split job. get value from the art_user_jobs table
			return sharedLastFileName;
		} else {
			//non-split job. get value from jobs table
			return lastFileName;
		}
	}
	
	@Override
	public String getLastRunDetails() {
		if (isSplitJob()) {
			//split job. get value from the art_user_jobs table
			return sharedLastRunDetails;
		} else {
			//non-split job. get value from jobs table
			return lastRunDetails;
		}
	}
}
