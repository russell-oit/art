package art.utils; 

import java.sql.*;
import java.util.*;
import org.apache.commons.beanutils.ResultSetDynaClass;


/**
 * Class to allow JDBC ResultSet to be used with XLSTransformer.
 * Based on the code in the jxls ResultSetCollection class
 * 
 * @author Timothy Anyona
 */
public class ArtJxlsResultSetCollection extends AbstractCollection {	    
	
	ResultSet resultSet;
	ResultSetDynaClass rsDynaClass;	
	int numberOfRows;
	
    /**
     * 
     * @param rs
     * @param lowerCaseProperties
     * @param useColumnLabel
     * @throws SQLException
     * @throws NullPointerException
     */
    public ArtJxlsResultSetCollection(ResultSet rs, boolean lowerCaseProperties, boolean useColumnLabel) throws SQLException, NullPointerException {
		resultSet = rs;        
		rsDynaClass = new ResultSetDynaClass(resultSet, lowerCaseProperties, useColumnLabel);
		setNumberOfRows();
	}
	
	//get number of rows. Needed for size property
	private void setNumberOfRows() throws SQLException {
		if (resultSet != null) {
			resultSet.last();
			numberOfRows = resultSet.getRow();
			resultSet.beforeFirst();
		}
	}
	
	//size property needed for a class that implements AbstractCollection
    @Override
	public int size() {
		return numberOfRows;
	}
	
	//iterator property needed for a class that implements AbstractCollection
    @Override
	public Iterator iterator() {
		return rsDynaClass.iterator();
	}
	
}