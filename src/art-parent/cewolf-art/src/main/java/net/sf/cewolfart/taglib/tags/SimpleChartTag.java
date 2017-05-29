/*
 * Created on 13.04.2003
 */
package net.sf.cewolfart.taglib.tags;

import java.util.Map;

import net.sf.cewolfart.CewolfRenderer;
import net.sf.cewolfart.DatasetProducer;
import net.sf.cewolfart.taglib.AbstractChartDefinition;
import net.sf.cewolfart.taglib.DataAware;
import net.sf.cewolfart.taglib.SimpleChartDefinition;

/**
 * @author guido
 */
public class SimpleChartTag extends AbstractChartTag implements DataAware {
    
	static final long serialVersionUID = -3313178284141986292L;

    protected AbstractChartDefinition createChartDefinition() {
        return new SimpleChartDefinition();
    }

     public void setDataProductionConfig (DatasetProducer dsp, Map<String,Object> params, boolean useCache) {
        ((SimpleChartDefinition) chartDefinition).setDataProductionConfig(dsp, params, useCache);
    }
}
