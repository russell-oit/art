package net.sf.cewolfart.cpp;

import java.awt.Color;
import java.io.Serializable;
import java.util.*;

import net.sf.cewolfart.ChartPostProcessor;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;

import net.sf.cewolfart.jfree.WaferMapPlot;
import net.sf.cewolfart.jfree.WaferMapRenderer;

/**
* A postprocessor for setting alternative colors for pie charts, category plots, XY plots and spider web plots.
* It takes numbered parameters containing the hex color values.
* <P>
* Usage:<P>
* &lt;chart:chartpostprocessor id="seriesPaint"&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="0" value="#FFFFAA" /&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="1" value="#AAFFAA" /&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="2" value="#FFAAFF" /&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="3" value="#FFAAAA" /&gt;<BR>
* &lt;/chart:chartpostprocessor&gt;
*/

public class SeriesPaintProcessor implements ChartPostProcessor, Serializable
{
	static final long serialVersionUID = -2290498142826058256L;

	@Override
    public void processChart (JFreeChart chart, Map<String,String> params) {
        Plot plot = chart.getPlot();

		// pie charts
		if (plot instanceof PiePlot) {
			PiePlot piePlot = (PiePlot) plot;

			@SuppressWarnings("rawtypes")
			List keys = piePlot.getDataset().getKeys();
			
			//https://sourceforge.net/p/cewolf/discussion/192228/thread/3f62c8b3/
			for (Map.Entry<String, String> entry : params.entrySet()) {
				int seriesId = Integer.parseInt(entry.getKey());
				String colorStr = entry.getValue();
				@SuppressWarnings("rawtypes")
				Comparable key = (Comparable) keys.get(seriesId);
				piePlot.setSectionPaint(key, Color.decode(colorStr));
			}

		// category plots
		} else if (plot instanceof CategoryPlot) {
			CategoryItemRenderer render = ((CategoryPlot) plot).getRenderer();

			for (Map.Entry<String, String> entry : params.entrySet()) {
				int seriesId = Integer.parseInt(entry.getKey());
				String colorStr = entry.getValue();
				render.setSeriesPaint(seriesId, Color.decode(colorStr));
			}

		// spider web plots
		} else if (plot instanceof SpiderWebPlot) {
			SpiderWebPlot swPlot = (SpiderWebPlot) plot;

			for (Map.Entry<String, String> entry : params.entrySet()) {
				int seriesId = Integer.parseInt(entry.getKey());
				String colorStr = entry.getValue();
				swPlot.setSeriesPaint(seriesId, Color.decode(colorStr));
			}

		// XY plots
		} else if (plot instanceof XYPlot) {
			XYItemRenderer render = ((XYPlot) plot).getRenderer();

			for (Map.Entry<String, String> entry : params.entrySet()) {
				int seriesId = Integer.parseInt(entry.getKey());
				String colorStr = entry.getValue();
				render.setSeriesPaint(seriesId, Color.decode(colorStr));
			}

		// Wafer Map plots
		} else if (plot instanceof WaferMapPlot) {
			WaferMapRenderer render = ((WaferMapPlot) plot).getRenderer();

			for (Map.Entry<String, String> entry : params.entrySet()) {
				int seriesId = Integer.parseInt(entry.getKey());
				String colorStr = entry.getValue();
				render.setSeriesPaint(seriesId, Color.decode(colorStr));
			}
		}
	}
}

