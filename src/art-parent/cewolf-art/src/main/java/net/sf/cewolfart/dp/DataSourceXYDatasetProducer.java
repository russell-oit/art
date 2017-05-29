package net.sf.cewolfart.dp;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import net.sf.cewolfart.DatasetProduceException;
import net.sf.cewolfart.DatasetProducer;

/**
 * @author glaures
 */
public class DataSourceXYDatasetProducer implements DatasetProducer, Serializable {

	static final long serialVersionUID = 4624928252168845205L;

	public static final String PARAM_SERIES_LIST = "series";

	/**
	 * @see net.sf.cewolfart.DatasetProducer#produceDataset(Map)
	 */
	public Object produceDataset (Map<String,Object> params) throws DatasetProduceException {
		/*
		DataSourceXYSeries series = new DataSourceXYSeries("select * from xy;");
		XYSeriesCollection dataset = new XYSeriesCollection();
		try {
			DataSource ds = getDataSource((String)params.get(PARAM_DATASOURCE));
			dataset.addSeries(series.produceXYSeries(ds));
		} catch (NamingException nEx) {
			nEx.printStackTrace();
			throw new DatasetProduceException(nEx.getMessage());
		}
		*/
		return null;
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
		return toString();
	}

}
