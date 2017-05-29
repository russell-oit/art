package net.sf.cewolfart.cpp;

import java.awt.Color;
import java.awt.Font;
import java.io.Serializable;
import java.util.Map;

import net.sf.cewolfart.ChartPostProcessor;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.title.LegendTitle;
import org.jfree.ui.RectangleInsets;

/**
* A postprocessor for modifying the legend of a chart. It supports the following parameters:
* <BR><b>fontname</b> optional; default SansSerif
* <BR><b>fontsize</b> optional; default is 18
* <BR><b>paint</b> optional; default #000000 (i.e., black)
* <BR><b>backgroundpaint</b> optional; default #FFFFFF (i.e., white)
* <BR><b>bold</b> true/false; optional; default true
* <BR><b>italic</b> true/false; optional; default false
* <BR><b>top</b> optional; default 1; sets the top padding between the legend border and the legend 
* <BR><b>left</b> optional; default 1; sets the left padding between the legend border and the legend 
* <BR><b>right</b> optional; default 1; sets the right padding between the legend border and the legend 
* <BR><b>bottom</b> optional; default 1; sets the bottom padding between the legend border and the legend 
* <P>
* Usage:<P>
* &lt;chart:chartpostprocessor id="subTitle"&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="fontname" value="Serif" /&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="fontsize" value="24" /&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="paint" value="#FF8800" /&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="backgroundpaint" value="#0088FF" /&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="bold" value="false" /&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="italic" value="true" /&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="top" value="5" /&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="left" value="5" /&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="right" value="5" /&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="bottom" value="5" /&gt;<BR>
* &lt;/chart:chartpostprocessor&gt;
* <P>
*/

public class LegendEnhancer implements ChartPostProcessor, Serializable
{
	static final long serialVersionUID = -6071718115056160390L;

    public void processChart (JFreeChart chart, Map<String,String> params) {
		String fontName = "SansSerif";
		Color paint = null;
		Color backgroundPaint = null;
		int fontSize = 18;
		boolean isBold = true;
		boolean isItalic = false;
		double top = 5.0;
		double left = 5.0;
		double right = 5.0;
		double bottom = 5.0;

		String fontNameParam = params.get("fontname");
		if (fontNameParam != null && fontNameParam.trim().length() > 0)
			fontName = fontNameParam.trim();

		String fontSizeParam = params.get("fontsize");
		if (fontSizeParam != null && fontSizeParam.trim().length() > 0) {
			try {
				fontSize = Integer.parseInt(fontSizeParam);
				if (fontSize < 1)
					fontSize = 18;
			} catch (NumberFormatException nfex) { }
		}

		String paintParam = params.get("paint");
		if (paintParam != null && paintParam.trim().length() > 0) {
			try {
				paint = Color.decode(paintParam);
			} catch (NumberFormatException nfex) { }
		}

		String backgroundpaintParam = params.get("backgroundpaint");
		if (backgroundpaintParam != null && backgroundpaintParam.trim().length() > 0) {
			try {
				backgroundPaint = Color.decode(backgroundpaintParam);
			} catch (NumberFormatException nfex) { }
		}

		String boldParam = params.get("bold");
		if (boldParam != null)
			isBold = "true".equals(boldParam.toLowerCase());

		String italicParam = params.get("italic");
		if (italicParam != null)
			isItalic = "true".equals(italicParam.toLowerCase());

		String str = params.get("top");
		if (str != null && str.trim().length() > 0) {
			try {
				top = Double.parseDouble(str);
				if (top < 0)
					top = 5.0;
			} catch (NumberFormatException nfex) { }
		}

		str = params.get("left");
		if (str != null && str.trim().length() > 0) {
			try {
				left = Double.parseDouble(str);
				if (left < 0)
					left = 5.0;
			} catch (NumberFormatException nfex) { }
		}

		str = params.get("right");
		if (str != null && str.trim().length() > 0) {
			try {
				right = Double.parseDouble(str);
				if (right < 0)
					right = 5.0;
			} catch (NumberFormatException nfex) { }
		}

		str = params.get("bottom");
		if (str != null && str.trim().length() > 0) {
			try {
				bottom = Double.parseDouble(str);
				if (bottom < 0)
					bottom = 5.0;
			} catch (NumberFormatException nfex) { }
		}

		LegendTitle legend = chart.getLegend();

		//legend.setLegendItemGraphicPadding(new RectangleInsets(top, left, bottom, right));
		legend.setItemLabelPadding(new RectangleInsets(top, left, bottom, right));

		Font font = new Font(fontName,
							(isBold ? Font.BOLD : 0) + (isItalic ? Font.ITALIC : 0),
							fontSize);
		legend.setItemFont(font);

		if (paint != null)
			legend.setItemPaint(paint);
		if (backgroundPaint != null)
			legend.setBackgroundPaint(backgroundPaint);
	}
}
