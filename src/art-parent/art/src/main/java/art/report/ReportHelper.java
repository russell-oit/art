/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
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
package art.report;

import java.util.StringTokenizer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Provides utility methods to help with reports
 *
 * @author Timothy Anyona
 */
public class ReportHelper {

	/**
	 * Sets chart option properties from a string representation of the options
	 * 
	 * @param optionsString the options string
	 * @param chartOptions the chart options object to set
	 */
	public void setChartOptionsFromString(String optionsString, ChartOptions chartOptions) {
		if(StringUtils.isBlank(optionsString)){
			return;
		}
		
		StringTokenizer st = new StringTokenizer(optionsString.trim(), " ");

		String token;
		while (st.hasMoreTokens()) {
			token = st.nextToken();

			if (token.startsWith("rotate_at") || token.startsWith("rotateAt")) {
				String tmp = StringUtils.substringAfter(token, ":");
				chartOptions.setRotateAt(NumberUtils.toInt(tmp));
			} else if (token.startsWith("remove_at") || token.startsWith("removeAt")) {
				String tmp = StringUtils.substringAfter(token, ":");
				chartOptions.setRemoveAt(NumberUtils.toInt(tmp));
			} else if (token.startsWith("noleg")) {
				chartOptions.setShowLegend(false);
			} else if (StringUtils.startsWithIgnoreCase(token, "showLegend")) {
				chartOptions.setShowLegend(true);
			} else if (token.startsWith("nolab")) {
				chartOptions.setShowLabels(false);
			} else if (StringUtils.startsWithIgnoreCase(token, "showLabels")) {
				chartOptions.setShowLabels(true);
			} else if (StringUtils.startsWithIgnoreCase(token, "showPoints")) {
				chartOptions.setShowPoints(true);
			} else if (StringUtils.startsWithIgnoreCase(token, "showData")) {
				chartOptions.setShowData(true);
			} else if (token.contains("x")) { //must come after named options e.g. rotate_at
				int idx = token.indexOf("x");
				String width = token.substring(0, idx);
				String height = token.substring(idx + 1);
				chartOptions.setWidth(NumberUtils.toInt(width));
				chartOptions.setHeight(NumberUtils.toInt(height));
			} else if (token.contains(":")) { //must come after named options e.g. rotate_at
				int idx = token.indexOf(":");
				String yMin = token.substring(0, idx);
				String yMax = token.substring(idx + 1);
				chartOptions.setyAxisMin(NumberUtils.toDouble(yMin));
				chartOptions.setyAxisMax(NumberUtils.toDouble(yMax));
			} else if (token.startsWith("#")) {
				chartOptions.setBackgroundColor(token);
			}
		}
	}
}
