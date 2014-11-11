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
import art.parameter.Parameter;
import de.laures.cewolf.links.PieSectionLinkGenerator;
import de.laures.cewolf.tooltips.PieToolTipGenerator;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Timothy Anyona
 */
public class PieChart extends AbstractChart implements PieToolTipGenerator, PieSectionLinkGenerator {

	private static final Logger logger = LoggerFactory.getLogger(PieChart.class);
	private static final long serialVersionUID = 1L;

	public PieChart(ReportType reportType) {
		if (reportType == ReportType.Pie2D) {
			setType("pie");
		} else if (reportType == ReportType.Pie3D) {
			setType("pie3d");
		} else {
			throw new IllegalArgumentException("Unsupported report type: " + reportType);
		}
	}

	@Override
	public void fillDataset(ResultSet rs) throws SQLException {
		Objects.requireNonNull(rs, "resultset must not be null");

		DefaultPieDataset dataset = new DefaultPieDataset();

		ResultSetMetaData rsmd = rs.getMetaData();
		if (rsmd.getColumnCount() == 3) {
			setHasHyperLinks(true);
			hyperLinks = new HashMap<>();
		} else {
			setHasHyperLinks(false);
		}

		while (rs.next()) {
			String category = rs.getString(1);
			double value = rs.getDouble(2);

			//add dataset value
			dataset.setValue(category, value);

			//add hyperlink
			if (isHasHyperLinks()) {
				String hyperLink = rs.getString(3);
				hyperLinks.put(category, hyperLink);
			}

			//add drilldown link
			addDrilldownLink(category, value);
		}

		setDataset(dataset);
	}

	private void addDrilldownLink(String category, double value) {
		//set drill down links
		if (drilldown != null) {
			StringBuilder sb = new StringBuilder(200);

			//add base url
			addDrilldownBaseUrl(sb);

			//add drilldown parameters
			if (drilldownParams != null) {
				for (Parameter drilldownParam : drilldownParams) {
					//drill down on col 1 = drill on data value. drill down on col 2 = category name
					String paramName = drilldownParam.getName();
					String paramValue;
					if (drilldownParam.getDrilldownColumnIndex() == 1) {
						paramValue = String.valueOf(value);
					} else {
						paramValue = category;
					}
					addUrlParameter(paramName, paramValue, sb);
				}
			}

			//add parameters from parent report
			addParentParameters(sb);

			String drilldownUrl = sb.toString();
			drilldownLinks.put(category, drilldownUrl);
		}
	}

	@Override
	public String generateToolTip(PieDataset data, @SuppressWarnings("rawtypes") Comparable key, int pieIndex) {
		//get data value to be used in tooltip
		double dataValue = data.getValue(pieIndex).doubleValue();

		//format value
		NumberFormat nf = NumberFormat.getInstance(getLocale());
		String formattedValue = nf.format(dataValue);

		//category name and value
		//return String.valueOf(key) + "=" + formattedValue;
		return formattedValue;
	}

	@Override
	public String generateLink(Object data, Object category) {
		String link = "";

		String key = String.valueOf(category);
		if (hyperLinks != null) {
			link = hyperLinks.get(key);
		} else if (drilldownLinks != null) {
			link = drilldownLinks.get(key);
		}

		return link;
	}

}
