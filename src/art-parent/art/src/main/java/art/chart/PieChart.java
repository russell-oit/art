/*
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
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
package art.chart;

import art.enums.ReportType;
import art.graph.PdfGraph;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Timothy Anyona
 */
public class PieChart extends AbstractChart {
	
	private static final Logger logger = LoggerFactory.getLogger(PieChart.class);
	private static final long serialVersionUID = 1L;

	public PieChart(ReportType reportType) {
		if(reportType==ReportType.Pie2D){
			setType("pie");
		} else if(reportType==ReportType.Pie3D){
			setType("pie3d");
		} else {
			throw new IllegalArgumentException("Unsupported report type: " + reportType);
		}
	}

	@Override
	public void fillDataset(ResultSet rs) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
	@Override
	public void processChart (JFreeChart chart, Map<String,String> params) {
		PiePlot plot = (PiePlot) chart.getPlot();

		// switch off labels
		String labelFormat = params.get("labelFormat");
		if (StringUtils.equals(labelFormat,"off")) {
			plot.setLabelGenerator(null);
		} else {
			plot.setLabelGenerator(new StandardPieSectionLabelGenerator(labelFormat));
		}

	}

	
}
