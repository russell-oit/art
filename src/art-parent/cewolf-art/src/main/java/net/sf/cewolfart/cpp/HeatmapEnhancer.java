package net.sf.cewolfart.cpp;

import java.awt.Color;
import java.awt.Font;
import java.io.Serializable;
import java.util.Map;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.GrayPaintScale;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.general.*;
import org.jfree.data.xy.*;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import net.sf.cewolfart.ChartPostProcessor;
import net.sf.cewolfart.jfree.XYBlockRenderer;

/**
* A postprocessor for changing details of a Compass plot.
* <BR><b>units</b> none/celsius/fahrenheit/kelvin; optional; default none
* <BR><b>mercuryColor</b> optional; default #FF0000 (i.e., red)
* <BR><b>thermometerColor</b> optional; default #000000 (i.e., black)
* <BR><b>valueColor</b> optional; default #FFFFFF (i.e., white)
* <BR><b>lowerBound</b> optional; default 0.0; starting value for the scale
* <BR><b>warningPoint</b> optional; default 50.0; boundary between normal range and warning range
* <BR><b>criticalPoint</b> optional; default 75.0; boundary between warning range and critical range
* <BR><b>upperBound</b> optional; default 100.0; end value for the scale
* <BR><b>subrangeIndicatorsVisible</b> true/false; optional; default true
* <BR><b>useSubrangePaint</b> true/false; optional; default true; if this is false, then mercuryColor is used
* <BR><b>subrangeColorNormal</b> optional; default #00FF00 (i.e., green)
* <BR><b>subrangeColorWarning</b> optional; default #FFC800 (i.e., orange)
* <BR><b>subrangeColorCritical</b> optional; default #FF0000 (i.e., red)
* <P>
* Usage:<P>
* &lt;chart:chartpostprocessor id="heatmapEnhancer"&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="scalePos" value="celsius" /&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="scaleTextPos" value="#336699" /&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="xLabel" value="#99AACC" /&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="yLabel" value="#CCCCCC" /&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="scaleLabel" value="20" /&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="subdivisions" value="40" /&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="stripWidth" value="60" /&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="lowerBound" value="80" /&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="upperBound" value="80" /&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="showItemLabels" value="true" /&gt;<BR>
* &lt;/chart:chartpostprocessor&gt;
*/

public class HeatmapEnhancer implements ChartPostProcessor, Serializable
{
	static final long serialVersionUID = -8459734218848320685L;

    public void processChart (JFreeChart chart, Map<String,String> params) {

		RectangleEdge scalePos = RectangleEdge.BOTTOM;
		AxisLocation scaleTextPos = AxisLocation.TOP_OR_LEFT;
		Color scaleBorder = null;
		Color lowerColor = null;
		Color upperColor = null;
		String xLabel="", yLabel="", scaleLabel="";
		int subdivisions = 20;
		int stripWidth = 10;
		double lowerBound = 0.0;
		double upperBound = 1.0;
		boolean showItemLabels = false;
		LookupPaintScale colorPaintScale = null;
		PaintScale grayPaintScale = null;

		String str = params.get("xLabel");
		if (str != null && str.trim().length() > 0)
			xLabel = str.trim();

		str = params.get("yLabel");
		if (str != null && str.trim().length() > 0)
			yLabel = str.trim();

		str = params.get("scaleLabel");
		if (str != null && str.trim().length() > 0)
			scaleLabel = str.trim();

		str = params.get("lowerBound");
		if (str != null) {
			try {
				lowerBound = Double.parseDouble(str);
			} catch (NumberFormatException nfex) {
				lowerBound = 0.0;
			}
		}

		str = params.get("upperBound");
		if (str != null) {
			try {
				upperBound = Double.parseDouble(str);
			} catch (NumberFormatException nfex) {
				upperBound = 1.0;
			}
		}

		str = params.get("subdivisions");
		if (str != null) {
			try {
				subdivisions = Integer.parseInt(str);
			} catch (NumberFormatException nfex) {
				subdivisions = 20;
			}
		}

		str = params.get("stripWidth");
		if (str != null) {
			try {
				stripWidth = Integer.parseInt(str);
			} catch (NumberFormatException nfex) {
				stripWidth = 10;
			}
		}

		str = params.get("scalePos");
		if (str != null) {
			if ("top".equals(str))
				scalePos = RectangleEdge.TOP;
			else if ("left".equals(str))
				scalePos = RectangleEdge.LEFT;
			else if ("bottom".equals(str))
				scalePos = RectangleEdge.BOTTOM;
			else if ("right".equals(str))
				scalePos = RectangleEdge.RIGHT;
		}

		str = params.get("scaleTextPos");
		if (str != null) {
			if ("topleft".equals(str))
				scaleTextPos = AxisLocation.TOP_OR_LEFT;
			else if ("topright".equals(str))
				scaleTextPos = AxisLocation.TOP_OR_RIGHT;
			else if ("bottomleft".equals(str))
				scaleTextPos = AxisLocation.BOTTOM_OR_LEFT;
			else if ("bottomright".equals(str))
				scaleTextPos = AxisLocation.BOTTOM_OR_RIGHT;
		}

		str = params.get("scaleBorder");
		if (str != null && str.trim().length() > 0) {
			try {
				scaleBorder = Color.decode(str);
			} catch (NumberFormatException nfex) { }
		}

		str = params.get("showItemLabels");
		if (str != null)
			showItemLabels = "true".equals(str);

		str = params.get("lowerColor");
		if (str != null && str.trim().length() > 0) {
			try {
				lowerColor = Color.decode(str);
			} catch (NumberFormatException nfex) { }
		}

		str = params.get("upperColor");
		if (str != null && str.trim().length() > 0) {
			try {
				upperColor = Color.decode(str);
			} catch (NumberFormatException nfex) { }
		}

		grayPaintScale = new GrayPaintScale(lowerBound, upperBound);
		if ((lowerColor != null) && (upperColor != null))
			grayPaintScale = new LinearPaintScale(lowerBound, lowerColor, upperBound, upperColor);

		double value = 0.0;
		for (int i=1; ; i++ ) {
			str = params.get("color#"+i);
			if (str != null) {
				if (colorPaintScale == null) {
					colorPaintScale = new LookupPaintScale(lowerBound, upperBound, Color.GRAY);
				}
				String[] parts = str.split(":");
				if (parts[0].endsWith("%")) {
					// convert from absolute to relative values
					value = Double.parseDouble(parts[0].substring(0, parts[0].length()-1));
					value = lowerBound + (upperBound - lowerBound) * value / 100.0;
				} else {
					value = Double.parseDouble(parts[0]);
				}
				colorPaintScale.add(value, Color.decode(parts[1]));
				//System.out.println(value+"="+Color.decode(parts[1]));
			} else {
				if (i != 1)
					subdivisions = i - 1;
				break;
			}
		}
		//System.out.println("subdivisions="+subdivisions);

		Plot plot = chart.getPlot();
		if (plot instanceof XYPlot) {
			XYPlot xyplot = (XYPlot) plot;
			xyplot.getDomainAxis().setLabel(xLabel);
			xyplot.getRangeAxis().setLabel(yLabel);
			// the next 3 lines could be parameterized as well
			xyplot.setDomainGridlinesVisible(false);
			xyplot.setRangeGridlinePaint(Color.white);
			xyplot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));

			XYItemRenderer renderer = xyplot.getRenderer();
			if (renderer instanceof XYBlockRenderer) {
				XYBlockRenderer xyRenderer = (XYBlockRenderer) renderer;
				// most basic way possible of doing this - not pretty, always showing FP numbers
				xyRenderer.setBaseItemLabelGenerator(
					new XYItemLabelGenerator() {
						public String generateLabel (XYDataset dataset, int series, int item) {
							XYZDataset ds = (XYZDataset) dataset;
							return ds.getZ(series, item).toString();
						}
					}
				);
				xyRenderer.setBaseItemLabelsVisible(showItemLabels);
				if (colorPaintScale != null) {
					xyRenderer.setPaintScale(colorPaintScale);
				} else {
					xyRenderer.setPaintScale(grayPaintScale);
				}

				if (scalePos != null) {
					NumberAxis zAxis = new NumberAxis(scaleLabel);
					zAxis.setAxisLinePaint(Color.white);
					zAxis.setTickMarkPaint(Color.white);
					//zAxis.setTickLabelFont(new Font("Dialog", 0, 10));
					PaintScaleLegend paintscalelegend = new PaintScaleLegend(grayPaintScale, zAxis);
					if (colorPaintScale != null) {
						paintscalelegend = new PaintScaleLegend(colorPaintScale, zAxis);
					} else {
						paintscalelegend.setSubdivisionCount(subdivisions);
					}
					paintscalelegend.setAxisLocation(scaleTextPos);
					paintscalelegend.setAxisOffset(5.0);
					paintscalelegend.setMargin(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
					if (scaleBorder != null)
						paintscalelegend.setFrame(new BlockBorder(scaleBorder));
					paintscalelegend.setPadding(new RectangleInsets(10.0, 10.0, 10.0, 10.0));
					paintscalelegend.setStripWidth(stripWidth);
					paintscalelegend.setPosition(scalePos);
					chart.addSubtitle(paintscalelegend);
				}
			}
		}
	}
}
