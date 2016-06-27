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
package art.chart;

import de.laures.cewolf.ChartPostProcessor;
import java.awt.Color;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import de.laures.cewolf.jfree.WaferMapPlot;
import de.laures.cewolf.jfree.WaferMapRenderer;
import java.util.Map.Entry;

/**
 * A postprocessor for setting alternative colors for pie charts, category
 * plots, XY plots and spider web plots. It takes numbered parameters containing
 * the hex color values. Based on the cewolf SeriesPaintProcessor class. See
 * https://sourceforge.net/p/cewolf/discussion/192228/thread/3f62c8b3/
 * <P>
 * Usage:
 * <P>
 * &lt;chart:chartpostprocessor id="seriesPaint"&gt;<BR>
 * &nbsp;&nbsp;&lt;chart:param name="0" value="#FFFFAA" /&gt;<BR>
 * &nbsp;&nbsp;&lt;chart:param name="1" value="#AAFFAA" /&gt;<BR>
 * &nbsp;&nbsp;&lt;chart:param name="2" value="#FFAAFF" /&gt;<BR>
 * &nbsp;&nbsp;&lt;chart:param name="3" value="#FFAAAA" /&gt;<BR>
 * &lt;/chart:chartpostprocessor&gt;
 *
 * @author Timothy Anyona
 */
public class ArtSeriesPaintProcessor implements ChartPostProcessor, Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public void processChart(JFreeChart chart, Map<String, String> params) {
		Plot plot = chart.getPlot();

		// pie charts
		if (plot instanceof PiePlot) {
			PiePlot piePlot = (PiePlot) plot;

			@SuppressWarnings("rawtypes")
			List keys = piePlot.getDataset().getKeys();
			
			for (Entry<String, String> entry : params.entrySet()) {
				int seriesId = Integer.parseInt(entry.getKey());
				String colorStr = entry.getValue();
				piePlot.setSectionPaint((Comparable) keys.get(seriesId), Color.decode(colorStr));
			}

			// category plots
		} else if (plot instanceof CategoryPlot) {
			CategoryItemRenderer render = ((CategoryPlot) plot).getRenderer();

			for (Entry<String, String> entry : params.entrySet()) {
				int seriesId = Integer.parseInt(entry.getKey());
				String colorStr = entry.getValue();
				render.setSeriesPaint(seriesId, Color.decode(colorStr));
			}

			// spider web plots
		} else if (plot instanceof SpiderWebPlot) {
			SpiderWebPlot swPlot = (SpiderWebPlot) plot;
			
			for (Entry<String, String> entry : params.entrySet()) {
				int seriesId = Integer.parseInt(entry.getKey());
				String colorStr = entry.getValue();
				swPlot.setSeriesPaint(seriesId, Color.decode(colorStr));
			}

		// XY plots
		} else if (plot instanceof XYPlot) {
			XYItemRenderer render = ((XYPlot) plot).getRenderer();

			for (Entry<String, String> entry : params.entrySet()) {
				int seriesId = Integer.parseInt(entry.getKey());
				String colorStr = entry.getValue();
				render.setSeriesPaint(seriesId, Color.decode(colorStr));
			}

			// Wafer Map plots
		} else if (plot instanceof WaferMapPlot) {
			WaferMapRenderer render = ((WaferMapPlot) plot).getRenderer();

			for (Entry<String, String> entry : params.entrySet()) {
				int seriesId = Integer.parseInt(entry.getKey());
				String colorStr = entry.getValue();
				render.setSeriesPaint(seriesId, Color.decode(colorStr));
			}
		}
	}

}
