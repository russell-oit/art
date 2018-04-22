/*
 * ART. A Reporting Tool.
 * Copyright (C) 2018 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.destinationoptions;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import java.io.Serializable;

/**
 * Options for amazon s3 - aws sdk destinations
 *
 * @author Timothy Anyona
 */
public class S3AwsSdkOptions implements Serializable {

	private static final long serialVersionUID = 1L;
	private String region;
	private CannedAccessControlList cannedAcl;

	/**
	 * @return the region
	 */
	public String getRegion() {
		return region;
	}

	/**
	 * @param region the region to set
	 */
	public void setRegion(String region) {
		this.region = region;
	}

	/**
	 * @return the cannedAcl
	 */
	public CannedAccessControlList getCannedAcl() {
		return cannedAcl;
	}

	/**
	 * @param cannedAcl the cannedAcl to set
	 */
	public void setCannedAcl(CannedAccessControlList cannedAcl) {
		this.cannedAcl = cannedAcl;
	}

}
