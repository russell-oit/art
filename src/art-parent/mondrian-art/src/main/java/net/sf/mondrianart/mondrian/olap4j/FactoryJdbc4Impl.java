/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2007-2011 Pentaho
// All Rights Reserved.
*/
package net.sf.mondrianart.mondrian.olap4j;

import net.sf.mondrianart.mondrian.rolap.RolapConnection;

import org.olap4j.*;

import java.sql.*;
import java.util.*;
import java.util.concurrent.Executor;

/**
 * Implementation of {@link Factory} for JDBC 4.0.
 *
 * @author jhyde
 * @since Jun 14, 2007
 */
class FactoryJdbc4Impl implements Factory {
    public Connection newConnection(
        MondrianOlap4jDriver driver,
        String url,
        Properties info)
        throws SQLException
    {
        return new MondrianOlap4jConnectionJdbc4(this, driver, url, info);
    }

    public EmptyResultSet newEmptyResultSet(
        MondrianOlap4jConnection olap4jConnection)
    {
        List<String> headerList = Collections.emptyList();
        List<List<Object>> rowList = Collections.emptyList();
        return new EmptyResultSetJdbc4(
            olap4jConnection, headerList, rowList);
    }

    public ResultSet newFixedResultSet(
        MondrianOlap4jConnection olap4jConnection,
        List<String> headerList,
        List<List<Object>> rowList)
    {
        return new EmptyResultSetJdbc4(
            olap4jConnection, headerList, rowList);
    }

    public MondrianOlap4jCellSet newCellSet(
        MondrianOlap4jStatement olap4jStatement)
    {
        return new MondrianOlap4jCellSetJdbc4(olap4jStatement);
    }

    public MondrianOlap4jStatement newStatement(
        MondrianOlap4jConnection olap4jConnection)
    {
        return new MondrianOlap4jStatementJdbc4(olap4jConnection);
    }

    public MondrianOlap4jPreparedStatement newPreparedStatement(
        String mdx,
        MondrianOlap4jConnection olap4jConnection)
        throws OlapException
    {
        return new MondrianOlap4jPreparedStatementJdbc4(olap4jConnection, mdx);
    }

    public MondrianOlap4jDatabaseMetaData newDatabaseMetaData(
        MondrianOlap4jConnection olap4jConnection,
        RolapConnection mondrianConnection)
    {
        return new MondrianOlap4jDatabaseMetaDataJdbc4(
            olap4jConnection, mondrianConnection);
    }

    // Inner classes

    private static class EmptyResultSetJdbc4
        extends FactoryJdbc4Plus.AbstractEmptyResultSet
    {
        EmptyResultSetJdbc4(
            MondrianOlap4jConnection olap4jConnection,
            List<String> headerList,
            List<List<Object>> rowList)
        {
            super(olap4jConnection, headerList, rowList);
        }

		@Override
		public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}
    }

    private static class MondrianOlap4jConnectionJdbc4
        extends FactoryJdbc4Plus.AbstractConnection
    {
        MondrianOlap4jConnectionJdbc4(
            Factory factory,
            MondrianOlap4jDriver driver,
            String url,
            Properties info) throws SQLException
        {
            super(factory, driver, url, info);
        }

		@Override
		public void abort(Executor executor) throws SQLException {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public int getNetworkTimeout() throws SQLException {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}
    }

    private static class MondrianOlap4jCellSetJdbc4
        extends FactoryJdbc4Plus.AbstractCellSet
    {
        public MondrianOlap4jCellSetJdbc4(
            MondrianOlap4jStatement olap4jStatement)
        {
            super(olap4jStatement);
        }

		@Override
		public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}
    }

    private static class MondrianOlap4jStatementJdbc4
        extends MondrianOlap4jStatement
    {
        public MondrianOlap4jStatementJdbc4(
            MondrianOlap4jConnection olap4jConnection)
        {
            super(olap4jConnection);
        }

		@Override
		public void closeOnCompletion() throws SQLException {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public boolean isCloseOnCompletion() throws SQLException {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}
    }

    private static class MondrianOlap4jPreparedStatementJdbc4
        extends FactoryJdbc4Plus.AbstractPreparedStatement
    {
        public MondrianOlap4jPreparedStatementJdbc4(
            MondrianOlap4jConnection olap4jConnection,
            String mdx)
            throws OlapException
        {
            super(olap4jConnection, mdx);
        }

		@Override
		public void closeOnCompletion() throws SQLException {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public boolean isCloseOnCompletion() throws SQLException {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}
    }

    private static class MondrianOlap4jDatabaseMetaDataJdbc4
        extends FactoryJdbc4Plus.AbstractDatabaseMetaData
    {
        public MondrianOlap4jDatabaseMetaDataJdbc4(
            MondrianOlap4jConnection olap4jConnection,
            RolapConnection mondrianConnection)
        {
            super(olap4jConnection, mondrianConnection);
        }

		@Override
		public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public boolean generatedKeyAlwaysReturned() throws SQLException {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}
    }
}

// End FactoryJdbc4Impl.java
