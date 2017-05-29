package net.sf.cewolfart.taglib.tags;

import net.sf.cewolfart.taglib.AbstractChartDefinition;
import net.sf.cewolfart.taglib.CombinedChartDefinition;
import net.sf.cewolfart.taglib.PlotContainer;
import net.sf.cewolfart.taglib.PlotDefinition;


/**
 * Chart tag subclass to handle combined charts
 *
 * @author guido
 * @author tbardzil
 *
 */
public class CombinedChartTag extends AbstractChartTag implements PlotContainer {

	static final long serialVersionUID = 6452599006714569892L;

    protected AbstractChartDefinition createChartDefinition() {
        return new CombinedChartDefinition();
    }

	public void addPlot(PlotDefinition pd){
		((CombinedChartDefinition) chartDefinition).addPlot(pd);
	}

    /**
     * Setter for property layout [tb]
     * @param layout
     */
    public void setLayout(String layout) {
        ((CombinedChartDefinition) chartDefinition).setLayout(layout);
    }
}
