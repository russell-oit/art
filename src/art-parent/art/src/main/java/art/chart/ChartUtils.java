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

import art.enums.ReportFormat;
import de.laures.cewolf.ChartValidationException;
import de.laures.cewolf.DatasetProduceException;
import de.laures.cewolf.PostProcessingException;
import de.laures.cewolf.taglib.SimpleChartDefinition;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.renderer.category.StandardBarPainter;

/**
 *
 * @author Timothy Anyona
 */
public class ChartUtils {

	public static void prepareTheme(String pdfFontName) {
		//reset jfreechat theme to the default theme ("jfree"). jasper reports sets it to the legacy theme and this affects the speedometer chart
		//use legacy theme to ensure you have white plot backgrounds. this was changed in jfreechart 1.0.11 to default to grey
		//if you use createLegacyTheme(), custom font isn't applied in pdf output for some reason
		StandardChartTheme chartTheme = (StandardChartTheme) StandardChartTheme.createJFreeTheme();
		chartTheme.setBarPainter(new StandardBarPainter()); //remove white line/glossy effect on 2D bar graphs with the jfree theme

		//change default colours. default "jfree" theme has plot background colour of light grey
//			chartTheme.setPlotBackgroundPaint(Color.white); //default is grey
//			chartTheme.setDomainGridlinePaint(Color.lightGray); //default is white
//			chartTheme.setRangeGridlinePaint(Color.lightGray); //default is white
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

	public static void generateFile(AbstractChart artChart, ReportFormat reportFormat, String outputFileName)
			throws IOException, DatasetProduceException, ChartValidationException, PostProcessingException {

		SimpleChartDefinition chartDefinition = new SimpleChartDefinition();
		chartDefinition.setPlotBackgroundPaint(Color.decode("#FFFFFF"));
		chartDefinition.setTitle(artChart.getTitle());
		chartDefinition.setType(artChart.getType());
		chartDefinition.setXAxisLabel(artChart.getxAxisLabel());
		chartDefinition.setYAxisLabel(artChart.getyAxisLabel());
		chartDefinition.setBackgroundPaint(Color.decode(artChart.getBgColor()));

		final boolean useCache = false;
		//not currently using producer parameters - equivalent to the <cewolf:producer> tag
		//must never be null otherwise setDataProductionConfig() will throw an exception
		chartDefinition.setDataProductionConfig(artChart, new HashMap<String, Object>(), useCache);

		JFreeChart jfreeChart = chartDefinition.getChart();
		switch (reportFormat) {
			case png:
				ChartUtilities.saveChartAsPNG(new File(outputFileName), jfreeChart, artChart.getWidth(), artChart.getHeight());
				break;
			case pdf:

			default:
				throw new IllegalArgumentException("Unsupported report format: " + reportFormat);
		}
	}

}
