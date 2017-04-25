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
package art.chart;

import java.awt.Color;
import java.awt.Font;
import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.renderer.category.StandardBarPainter;

/**
 * Provides methods for preparing for chart generation including preparing the
 * theme
 *
 * @author Timothy Anyona
 */
public class ChartUtils {

	/**
	 * Prepare the theme to be used by charts
	 * 
	 * @param pdfFontName the custom font to be used in charts, or null/blank
	 */
	public static void prepareTheme(String pdfFontName) {
		//reset jfreechat theme to the default theme ("jfree"). jasper reports sets it to the legacy theme and this affects the speedometer chart
		//use legacy theme to ensure you have white plot backgrounds. this was changed in jfreechart 1.0.11 to default to grey
		//if you use createLegacyTheme(), custom font isn't applied in pdf output for some reason
		StandardChartTheme chartTheme = (StandardChartTheme) StandardChartTheme.createJFreeTheme();
		chartTheme.setBarPainter(new StandardBarPainter()); //remove white line/glossy effect on 2D bar graphs with the jfree theme

		//change default colours. default "jfree" theme has plot background colour of light grey
		chartTheme.setPlotBackgroundPaint(Color.WHITE); //default is grey

		//set grid lines to light grey so that they are visible with a default plot background colour of white
		chartTheme.setDomainGridlinePaint(Color.LIGHT_GRAY); //default is white
		chartTheme.setRangeGridlinePaint(Color.LIGHT_GRAY); //default is white

		//also allow use of custom font to enable display of non-ascii characters
		if (StringUtils.isNotBlank(pdfFontName)) {
			Font oldExtraLargeFont = chartTheme.getExtraLargeFont();
			Font oldLargeFont = chartTheme.getLargeFont();
			Font oldRegularFont = chartTheme.getRegularFont();

			Font extraLargeFont = new Font(pdfFontName, oldExtraLargeFont.getStyle(), oldExtraLargeFont.getSize());
			Font largeFont = new Font(pdfFontName, oldLargeFont.getStyle(), oldLargeFont.getSize());
			Font regularFont = new Font(pdfFontName, oldRegularFont.getStyle(), oldRegularFont.getSize());

			chartTheme.setExtraLargeFont(extraLargeFont);
			chartTheme.setLargeFont(largeFont);
			chartTheme.setRegularFont(regularFont);
		}

		ChartFactory.setChartTheme(chartTheme);
	}
}
