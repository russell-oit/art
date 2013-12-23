/**
 * Copyright (C) 2013 Enrico Liboni <eliboni@users.sourceforge.net>
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

package art.report;

import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Class to provide methods related to reports
 * 
 * @author Timothy Anyona
 */
@Service
public class ReportService {
	final static Logger logger = LoggerFactory.getLogger(ReportService.class);
	private final ReportRepository reportRepository;
	
	@Autowired
	public ReportService(ReportRepository reportRepository){
		this.reportRepository=reportRepository;
	}
	
	public ReportService(){
		this.reportRepository=new ReportRepository();
	}
	
	/**
	 * Get the reports that a user can access from the reports page. Excludes
	 * disabled reports and some report types e.g. lovs
	 *
	 * @param username
	 * @return
	 * @throws SQLException
	 */
	@Cacheable(value="reports")
	public List<AvailableReport> getAvailableReports(String username) throws SQLException {
		logger.info("test cache miss"); //TODO remove
		
		return reportRepository.getAvailableReports(username);
	}
	
}
