/*
 * Created on 13.04.2003
 */
package net.sf.cewolfart.taglib;

import java.io.Serializable;
import java.util.Map;

import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Dataset;

import net.sf.cewolfart.ChartValidationException;
import net.sf.cewolfart.DatasetProduceException;
import net.sf.cewolfart.DatasetProducer;

/**
 * @author guido
 */
public class SimpleChartDefinition extends AbstractChartDefinition implements DataAware {

	static final long serialVersionUID = -1330286307731143710L;

	private DataContainer dataAware = new DataContainer();

    protected JFreeChart produceChart() throws DatasetProduceException, ChartValidationException {
    	return CewolfChartFactory.getChartInstance(type, title, xAxisLabel, yAxisLabel, getDataset(), showLegend);
    }

    public Dataset getDataset() throws DatasetProduceException {
        return dataAware.getDataset();
    }

    public void setDataProductionConfig(DatasetProducer dsp, Map<String,Object> params, boolean useCache) {
    	dataAware.setDataProductionConfig(dsp, params, useCache);
    }
}
