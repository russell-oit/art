package net.sf.cewolfart.cpp;

import java.awt.Color;
import java.awt.Font;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import net.sf.cewolfart.ChartPostProcessor;

import net.sf.cewolfart.jfree.WaferMapPlot;

import org.jfree.chart.*;
import org.jfree.chart.block.Arrangement;
import org.jfree.chart.block.GridArrangement;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.ui.*;

/**
* A postprocessor for adding custom legend titles to a wafer map plot. It is useful in conjunction
* with a SeriesPaintprocessor to show custom labels instead of numerical values in the legend.
* The <i>name</i> of each parameter is an RGB color, while the <i>value</i> is the label for that color.<BR>
* The <b>valign</b> parameter (top, center or bottom) can be used to to place the legend more precisely.<P>
* The <b>showCellValues</b> parameter can be used to show the values of non-empty cells.<P>
* &lt;cewolf:chartpostprocessor id="waferMapLegend"&gt;<BR>
*	&nbsp;&nbsp;&nbsp;&lt;cewolf:param name="valign" value="bottom" /&gt;<BR>
*	&nbsp;&nbsp;&nbsp;&lt;cewolf:param name="showCellValues" value="true" /&gt;<BR>
*	&nbsp;&nbsp;&nbsp;&lt;cewolf:param name="#14ff1a" value="one" /&gt;<BR>
*	&nbsp;&nbsp;&nbsp;&lt;cewolf:param name="#ff0000" value="two" /&gt;<BR>
*	&nbsp;&nbsp;&nbsp;&lt;cewolf:param name="#ff00fe" value="three" /&gt; <BR>
*	&nbsp;&nbsp;&nbsp;&lt;cewolf:param name="#7640ff" value="four" /&gt; <BR>
*	&nbsp;&nbsp;&nbsp;&lt;cewolf:param name="#4896ff" value="five" /&gt; <BR>
*	&nbsp;&nbsp;&nbsp;&lt;cewolf:param name="#d29428" value="six" /&gt;<BR>
*	&nbsp;&nbsp;&nbsp;&lt;cewolf:param name="#ffd0d4" value="seven" /&gt;<BR>
*	&nbsp;&nbsp;&nbsp;&lt;cewolf:param name="#9c0c1e" value="eight" /&gt;<BR>
* &lt;/cewolf:chartpostprocessor&gt;
* <P>
* See the meter.jsp page of the sample web app for a usage example.
*/

public class WaferMapLegendProcessor implements ChartPostProcessor, Serializable {

	static final long serialVersionUID = -1915129061254557435L;

	public void processChart (JFreeChart chart, Map<String,String> params) {
		Plot plot = chart.getPlot();
		if (plot instanceof WaferMapPlot) {

			// whether or not to show grid values
			String str = params.get("showCellValues");
			if (str != null)
				((WaferMapPlot) plot).setShowCellValues("true".equals(str));

			// manage the legend's vertical alignment
			VerticalAlignment vAlign = VerticalAlignment.CENTER;
			String vAlignParam = params.get("valign");
			if (vAlignParam != null) {
				if ("top".equalsIgnoreCase(vAlignParam)) {
					vAlign = VerticalAlignment.TOP;
				} else if ("bottom".equalsIgnoreCase(vAlignParam)) {
					vAlign = VerticalAlignment.BOTTOM;
				}
			}

			final LegendItemCollection legendItemCollection = new LegendItemCollection();
			if (params.size() > 0) {
				String colorStr = null;
				String legendDesc = null;
				Iterator<Map.Entry<String,String>> iter = params.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<String,String> entry = iter.next();
					colorStr = entry.getKey();
					if (colorStr.startsWith("#")) {
						legendDesc = entry.getValue();
						legendItemCollection.add(new LegendItem(legendDesc, Color.decode(colorStr)));
					}
				}
			}

			LegendTitle legend = chart.getLegend();
			if (legend != null) {
				legend.setVerticalAlignment(vAlign);

				legend.setSources(new LegendItemSource[] {
					new LegendItemSource() {
						public LegendItemCollection getLegendItems() {
							return legendItemCollection;
						}
					}
				});
			}
		}
	}
}

