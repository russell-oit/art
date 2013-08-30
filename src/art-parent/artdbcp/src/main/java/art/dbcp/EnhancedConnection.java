/**
 * Copyright 2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of artdbcp.
 *
 * artdbcp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 2.1 of the License.
 *
 * artdbcp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with artdbcp.  If not, see <http://www.gnu.org/licenses/>.
 */
/* To compile this using jdk 1.3 you should comment the jdk1.4/1.5 lines 
 at the bottom */
/* To compile this using jdk 1.4/1.5 you should also comment the jdk1.6 lines 
 at the bottom */

/* 20050612  Statements are closed automatically when the EnanchedConnection is 
 returned to the pool
 20050612  test conection with a dummy sql, if it fails refresh the connection
 */
package art.dbcp;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 
 * @author Enrico Liboni
 */
public class EnhancedConnection implements Connection {

    private boolean inUse = false;
    private int inUseCount = 0;    
    private long lastUsedTime = 0;
    private Connection c;
    private final boolean DEBUG = false;
    private List<Statement> openStatements;

    EnhancedConnection(String url, String username, String password) throws SQLException {
        Properties dbprops = new Properties();
        dbprops.put("user", username);
        dbprops.put("password", password);
        if (DEBUG) {
            System.err.println("EnanchedConnection:   constructor");
        }
        Driver d = DriverManager.getDriver(url); // get the right driver for the given url
        c = d.connect(url, dbprops); // get the connection
        openStatements = new ArrayList<Statement>();
    }

    /**
     * 
     */
    public void open() {
        inUse = true;
        inUseCount++;
        lastUsedTime = new java.util.Date().getTime();
    }

    @Override
    public void close() throws SQLException {
        inUseCount--;
        if (inUseCount == 0) {
            // close any open statement / prepared statement
            for (int i = 0; i < openStatements.size(); i++) {
                Statement st = openStatements.get(i);
                if (st != null) {
                    st.close();
                }
                if (DEBUG) {
                    System.err.println("EnanchedConnection: closing statement " + i);
                }
            }
            openStatements.clear();

            inUse = false;
            if (DEBUG) {
                System.err.println("EnanchedConnection:   close()");
            }

        } else {
            System.err.println("ARTDB EnanchedConnection: more threads are using the same connection - statements not closed: inuse count" + inUseCount);
        }
    }

    /**
     * 
     * @throws SQLException
     */
    protected void realClose() throws SQLException {
        try {
            //this.close(); // close all the open statements: not needed... since the driver should do this
            c.close();    // note: the caller (Datasource)  must remove the object from the pool
            if (DEBUG) {
                System.err.println("ARTDB EnanchedConnection:   realClose() - effective close");
            }
        } catch (SQLException ex) {
            throw new SQLException("EnanchedConnection: error in realClose(): cause: " + ex.getMessage());
        }
    }

    /**
     * 
     * @param testSQL
     * @throws SQLException
     */
    protected void test(String testSQL) throws SQLException {
        Statement st = c.createStatement();
        ResultSet rs = st.executeQuery(testSQL);
        while (rs.next()) {
        }
        rs.close();
        st.close();
    }

    /**
     * 
     * @return
     */
    public boolean getInUse() {
        return inUse;
    }

    /**
     * 
     * @return
     */
    public int getInUseCount() {
        return inUseCount;
    }

    /**
     * 
     * @return
     */
    public long getLastUsedTime() {
        return lastUsedTime;
    }

    /**
     * Here are all the standard JDBC2 Connection methods except for: close() it
     * has been re-defined above for pool handling createStatement and
     * prepareStatement where the "open" statements are maintained in a list
     * in order to close it when the connection is returned to the pool.
   **
     */
    @Override
    public Statement createStatement() throws SQLException {
        Statement st = c.createStatement();
        openStatements.add(st);
        return st;
    }

    @Override
    public PreparedStatement prepareStatement(String Sql)
            throws SQLException {
        PreparedStatement ps = c.prepareStatement(Sql);
        openStatements.add(ps);
        return ps;
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return c.getMetaData();
    }

    // JDBC2
    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency)
            throws SQLException {
        Statement st = c.createStatement(resultSetType, resultSetConcurrency);
        openStatements.add(st);
        return st;
    }

    // JDBC2
    @Override
    public PreparedStatement prepareStatement(String Sql, int resultSetType, int resultSetConcurrency)
            throws SQLException {
        PreparedStatement ps = c.prepareStatement(Sql, resultSetType, resultSetConcurrency);
        openStatements.add(ps);
        return ps;
    }

    // JDBC2
    @Override
    public CallableStatement prepareCall(String Sql)
            throws SQLException {
        CallableStatement cs = c.prepareCall(Sql);
        openStatements.add(cs);
        return cs;
    }

    @Override
    public CallableStatement prepareCall(String Sql, int resultSetType, int resultSetConcurrency)
            throws SQLException {
        CallableStatement cs = c.prepareCall(Sql, resultSetType, resultSetConcurrency);
        openStatements.add(cs);
        return cs;
    }

    // JDBC2
    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return c.getTypeMap();
    }

    // JDBC2
    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        c.setTypeMap(map);
    }

    @Override
    public String nativeSQL(String Sql) throws SQLException {
        return c.nativeSQL(Sql);
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        c.setAutoCommit(autoCommit);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return c.getAutoCommit();
    }

    @Override
    public void commit() throws SQLException {
        c.commit();
    }

    @Override
    public void rollback() throws SQLException {
        c.rollback();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return c.isClosed();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        c.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return c.isReadOnly();
    }

    @Override
    public void setCatalog(String Catalog) throws SQLException {
        c.setCatalog(Catalog);
    }

    @Override
    public String getCatalog() throws SQLException {
        return c.getCatalog();
    }

    @Override
    public java.sql.SQLWarning getWarnings() throws SQLException {
        return c.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        c.clearWarnings();
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        c.setTransactionIsolation(level);
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return c.getTransactionIsolation();
    }

    /*
     * START JDK 1.4/1.5 only
     */
    @Override
    public void setHoldability(int i) throws SQLException {
        c.setHoldability(i);
    }

    @Override
    public int getHoldability() throws SQLException {
        return c.getHoldability();
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return c.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String s) throws SQLException {
        return c.setSavepoint(s);
    }

    @Override
    public void rollback(Savepoint s) throws SQLException {
        c.rollback(s);
    }

    @Override
    public void releaseSavepoint(Savepoint s) throws SQLException {
        c.releaseSavepoint(s);
    }

    @Override
    public Statement createStatement(int x, int y, int z) throws SQLException {
        Statement st = c.createStatement(x, y, z);
        openStatements.add(st);
        return st;
    }

    @Override
    public PreparedStatement prepareStatement(String Sql, int x, int y, int z) throws SQLException {
        PreparedStatement ps = c.prepareStatement(Sql, x, y, z);
        openStatements.add(ps);
        return ps;
    }

    @Override
    public PreparedStatement prepareStatement(String Sql, int a) throws SQLException {
        PreparedStatement ps = c.prepareStatement(Sql, a);
        openStatements.add(ps);
        return ps;
    }

    @Override
    public PreparedStatement prepareStatement(String Sql, int a[]) throws SQLException {
        PreparedStatement ps = c.prepareStatement(Sql, a);
        openStatements.add(ps);
        return ps;
    }

    @Override
    public PreparedStatement prepareStatement(String Sql, String a[]) throws SQLException {
        PreparedStatement ps = c.prepareStatement(Sql, a);
        openStatements.add(ps);
        return ps;
    }

    @Override
    public CallableStatement prepareCall(String Sql, int x, int y, int z) throws SQLException {
        CallableStatement cs = c.prepareCall(Sql, x, y, z);
        openStatements.add(cs);
        return cs;
    }
    /*
     * END JDK 1.4/1.5 only
     */
    

    /*
     * START JDK 1.6 only
     */
    @Override
    public Clob createClob() throws SQLException {
        return c.createClob();
    }

    @Override
    public Blob createBlob() throws SQLException {
        return c.createBlob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return c.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return c.createSQLXML();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return c.isValid(timeout);
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        c.setClientInfo(name, value);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        c.setClientInfo(properties);
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return c.getClientInfo(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return c.getClientInfo();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return c.createArrayOf(typeName, elements);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return c.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return c.isWrapperFor(iface);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return c.createStruct(typeName, attributes);
    }

	
    /*
     * END JDK 1.6 only
     */
    
    
    
    /*
     * START JDK 1.7 only
     */
    
    /*
    @Override
	public void abort(Executor arg0) throws SQLException {
		c.abort(arg0);
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		return c.getNetworkTimeout();
	}

	@Override
	public String getSchema() throws SQLException {
		return c.getSchema();
	}

	@Override
	public void setNetworkTimeout(Executor arg0, int arg1) throws SQLException {
		c.setNetworkTimeout(arg0, arg1);
	}

	@Override
	public void setSchema(String arg0) throws SQLException {
		c.setSchema(arg0);
	}
	*/
    
    /*
     * END JDK 1.7 only
     */
}
