package net.sf.cewolfart.taglib;

import java.util.Map;

import net.sf.cewolfart.DatasetProducer;

/**
 * @author glaures
 */
public interface DataAware {

	public void setDataProductionConfig(DatasetProducer dsp, Map<String,Object> params, boolean useCache);
}
