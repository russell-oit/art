/* ================================================================
 * Cewolf : Chart enabling Web Objects Framework
 * ================================================================
 *
 * Project Info:  http://cewolf.sourceforge.net
 * Project Lead:  Guido Laures (guido@laures.de);
 *
 * (C) Copyright 2002, by Guido Laures
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package net.sf.cewolfart.taglib.tags;

import java.awt.Color;
import java.awt.Paint;
import java.io.Serializable;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import net.sf.cewolfart.CewolfRenderer;
import net.sf.cewolfart.ChartPostProcessor;
import net.sf.cewolfart.taglib.AbstractChartDefinition;
import net.sf.cewolfart.taglib.TaglibConstants;

/**
 * Root tag &lt;chart&gt; of a chart definition. Defines all values for the
 * page scope variable of type ChartDefinition which is used by the img
 * tag to render the appropriate chart.
 * @author  Guido Laures
 */
public abstract class AbstractChartTag extends CewolfTag implements CewolfRootTag, Painted {
	
	private static final long serialVersionUID = 1L;

    protected AbstractChartDefinition chartDefinition = createChartDefinition();

    protected abstract AbstractChartDefinition createChartDefinition();

    public int doStartTag() {
		chartDefinition.setWebRootDir((String) pageContext.getServletContext().getAttribute(CewolfRenderer.WEB_ROOT_DIR));
        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() throws JspException {
        pageContext.setAttribute(getId(), chartDefinition, PageContext.PAGE_SCOPE);
        return doAfterEndTag(EVAL_PAGE);
    }

    public void reset() {
    	chartDefinition = createChartDefinition();
    }

    public String getChartId() {
        return getId();
    }

    /**
     * Setter for property title.
     */
    public void setTitle(String title) {
        chartDefinition.setTitle(title);
    }

    /**
     * Setter for property xAxisLabel.
     */
    public void setXaxislabel (String xAxisLabel) {
        chartDefinition.setXAxisLabel(xAxisLabel);
    }

    /**
     * Setter for property yAxisLabel.
     */
    public void setYaxislabel (String yAxisLabel) {
        chartDefinition.setYAxisLabel(yAxisLabel);
    }

    /**
     * Setter for property xAxisInteger.
     */
    public void setXaxisinteger (boolean xAxisInteger) {
        chartDefinition.setXaxisinteger(xAxisInteger);
    }

    /**
     * Setter for property yAxisInteger.
     */
    public void setYaxisinteger (boolean yAxisInteger) {
        chartDefinition.setYaxisinteger(yAxisInteger);
    }

    /**
     * Setter for property xTicksMarksVisible.
     */
    public void setXtickmarksvisible (boolean xTicksMarksVisible) {
        chartDefinition.setXtickmarksvisible(xTicksMarksVisible);
    }

    /**
     * Setter for property yTicksMarksVisible.
     */
    public void setYtickmarksvisible (boolean yTicksMarksVisible) {
        chartDefinition.setYtickmarksvisible(yTicksMarksVisible);
    }

    /**
     * Setter for property xTicksLabelsVisible.
     */
    public void setXticklabelsvisible (boolean xTicksLabelsVisible) {
        chartDefinition.setXticklabelsvisible(xTicksLabelsVisible);
    }

    /**
     * Setter for property yTicksLabelsVisible.
     */
    public void setYticklabelsvisible (boolean yTicksLabelsVisible) {
        chartDefinition.setYticklabelsvisible(yTicksLabelsVisible);
    }

    /**
     * Setter for property borderVisible.
     */
    public void setBordervisible (boolean borderVisible) {
        chartDefinition.setBorderVisible(borderVisible);
    }

    /**
     * Setter for property plotBorderVisible.
     */
    public void setPlotbordervisible (boolean plotBorderVisible) {
        chartDefinition.setPlotBorderVisible(plotBorderVisible);
    }

    public void setBackground (String src) {
        String srcFile = pageContext.getServletContext().getRealPath(src);
        chartDefinition.setBackground(srcFile);
    }

    public void setBackgroundimagealpha (Float alpha) {
        chartDefinition.setBackgroundImageAlpha(alpha.floatValue());
    }
    
    public void setAntialias (boolean anti) {
        chartDefinition.setAntialias(anti);
    }

    /**
     * Setter for property legend.
     */
    public void setShowlegend (boolean legend) {
        chartDefinition.setShowLegend(legend);
    }

    /**
     * Setter for property legend.
     */
    public void setLegendanchor (String anchor) {
        if ("north".equalsIgnoreCase(anchor)) {
            chartDefinition.setLegendAnchor(TaglibConstants.ANCHOR_NORTH);
        } else if ("south".equalsIgnoreCase(anchor)) {
            chartDefinition.setLegendAnchor(TaglibConstants.ANCHOR_SOUTH);
        } else if ("west".equalsIgnoreCase(anchor)) {
            chartDefinition.setLegendAnchor(TaglibConstants.ANCHOR_WEST);
        } else if ("east".equalsIgnoreCase(anchor)) {
            chartDefinition.setLegendAnchor(TaglibConstants.ANCHOR_EAST);
        }
    }

    public void addChartPostProcessor (ChartPostProcessor pp, Map<String,String> params) {
        chartDefinition.addPostProcessor(pp, params);
    }

    /**
     * Setter for property backgroundPaint.
     */
    public void setBackgroundcolor (String color){
		try {
			chartDefinition.setBackgroundPaint(Color.decode(color));
		} catch (NumberFormatException ex) { }
    }

    /**
     * Setter for property plotBackgroundPaint.
     */
    public void setPlotbackgroundcolor (String color) {
		try {
			chartDefinition.setPlotBackgroundPaint(Color.decode(color));
		} catch (NumberFormatException ex) { }
    }

    /**
     * Setter for property backgroundPaint.
     */
    public void setPaint (Paint paint){
		chartDefinition.setBackgroundPaint(paint);
    }

    /**
     * Setter for property borderPaint.
     */
    public void setBordercolor (String paint){
		try {
			chartDefinition.setBorderPaint(Color.decode(paint));
		} catch (NumberFormatException ex) { }
    }

    /**
     * Setter for property plotBorderPaint.
     */
    public void setPlotbordercolor (String paint){
		try {
			chartDefinition.setPlotBorderPaint(Color.decode(paint));
		} catch (NumberFormatException ex) { }
    }

    /**
	 * Setter for property type.
	 */
    public void setType(String type) {
        chartDefinition.setType(type);
    }
}
