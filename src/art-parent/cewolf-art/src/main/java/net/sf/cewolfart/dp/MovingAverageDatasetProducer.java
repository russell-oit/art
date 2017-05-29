package net.sf.cewolfart.dp;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.jfree.data.general.Dataset;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.xy.XYDataset;

import net.sf.cewolfart.DatasetProduceException;
import net.sf.cewolfart.DatasetProducer;

/**
 * @author guido
 */
public class MovingAverageDatasetProducer implements DatasetProducer, Serializable {
	
	static final long serialVersionUID = -3599156193385103768L;

	/**
	 * @see net.sf.cewolfart.DatasetProducer#produceDataset(Map)
	 */
	public Object produceDataset (Map<String,Object> params) throws DatasetProduceException {
		//log.info(params);
		DatasetProducer datasetProducer = (DatasetProducer) params.get("producer");
		//log.info(datasetProducer);
		Dataset dataset = (Dataset) datasetProducer.produceDataset(params);
		String suffix = (String) params.get("suffix");
		int period, skip;
		try {
			period = Integer.parseInt((String) params.get("period"));
			skip = Integer.parseInt((String) params.get("skip"));
		} catch (RuntimeException ex) {
			throw new DatasetProduceException("'period' and 'skip' parameters don't seem to have valid integer values");
		}
		if (dataset instanceof XYDataset) {
	        return MovingAverage.createMovingAverage((XYDataset)dataset, suffix, period, skip);
		} else {
			throw new DatasetProduceException("moving average only supported for XYDatasets");
		}
	}

	/**
	 * @see net.sf.cewolfart.DatasetProducer#hasExpired(Map, Date)
	 */
	public boolean hasExpired(Map<String, Object> params, Date since) {
		return true;
	}

	/**
	 * @see net.sf.cewolfart.DatasetProducer#getProducerId()
	 */
	public String getProducerId() {
		return getClass().getName();
	}

}
