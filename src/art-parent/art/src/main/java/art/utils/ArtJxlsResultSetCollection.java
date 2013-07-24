/**
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART.  If not, see <http://www.gnu.org/licenses/>.
 */
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
@SuppressWarnings("rawtypes")
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