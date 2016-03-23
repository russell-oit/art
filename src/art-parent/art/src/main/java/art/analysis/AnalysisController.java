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
package art.analysis;

import art.report.ReportService;
import com.tonbeller.jpivot.olap.model.OlapModel;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author Timothy Anyona
 */
@Controller
public class AnalysisController {
	
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AnalysisController.class);
	
	@Autowired
	private ReportService reportService;
	
	@RequestMapping(value = "/app/showAnalysis", method = {RequestMethod.GET, RequestMethod.POST})
	public String showDashboard(@RequestParam("reportId") Integer reportId,
			HttpServletRequest request, Model model, Locale locale) {
		
		
		return "showAnalysis";
		
	}
	
	@RequestMapping(value = "/app/jpivotError", method = {RequestMethod.GET, RequestMethod.POST})
	public String jpivotError() {
		return "jpivotError";
	}
	
	@RequestMapping(value = "/app/jpivotBusy", method = {RequestMethod.GET, RequestMethod.POST})
	public String jpivotBusy() {
		return "jpivotBusy";
	}
}
