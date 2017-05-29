/*
 * Created on 13.04.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.sf.cewolfart.taglib.tags;

import net.sf.cewolfart.taglib.AbstractChartDefinition;
import net.sf.cewolfart.taglib.AxisTypes;
import net.sf.cewolfart.taglib.OverlaidChartDefinition;
import net.sf.cewolfart.taglib.PlotContainer;
import net.sf.cewolfart.taglib.PlotDefinition;


/**
 * @author guido
 */
public class OverlaidChartTag extends AbstractChartTag implements PlotContainer {

	static final long serialVersionUID = 3879037601548824461L;

    protected AbstractChartDefinition createChartDefinition() {
        return new OverlaidChartDefinition();
    }

	public void addPlot(PlotDefinition pd){
		((OverlaidChartDefinition)chartDefinition).addPlot(pd);
	}
    
	/**
	 * Sets the xAxisType.
	 * @param xAxisType The xAxisType to set
	 */
	public void setxaxistype(String xAxisType) {
        final int xAxisTypeConst = AxisTypes.typeList.indexOf(xAxisType);
		((OverlaidChartDefinition)chartDefinition).setXAxisType(xAxisTypeConst);
	}

	/**
	 * Sets the yAxisType.
	 * @param yAxisType The yAxisType to set
	 */
	public void setyaxistype(String yAxisType) {
        final int yAxisTypeConst = AxisTypes.typeList.indexOf(yAxisType);
        ((OverlaidChartDefinition)chartDefinition).setYAxisType(yAxisTypeConst);
	}

}
