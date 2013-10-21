/**
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 * Proprietary and confidential.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.jdbc.internal.cm.handle;

import com.peergreen.jdbc.internal.cm.ConnectionProxy;
import com.peergreen.jdbc.internal.cm.IManagedConnection;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * This class represent a connection linked to the physical and XA connections.
 * All errors are reported to the managed connection. This connection is
 * returned to the client.
 * @author Philippe Durieux
 * @author Florent Benoit
 */
public class DefaultConnectionProxy implements ConnectionProxy {

    /**
     * JDBC connection provided by the DriverManager.
     */
    private Connection physicalConnection = null;

    /**
     * XA connection which receive events.
     */
    private IManagedConnection xaConnection = null;

    /**
     * Buils a Connection (viewed by the user) which rely on a Managed
     * connection and a physical connection.
     * @param xaConnection the XA connection.
     * @param physicalConnection the connection to the database.
     */
    public DefaultConnectionProxy(final IManagedConnection xaConnection, final Connection physicalConnection) {
        this.xaConnection = xaConnection;
        this.physicalConnection = physicalConnection;
    }

    /**
     * Gets the physical connection to the database.
     * @return physical connection to the database
     */
    @Override
    public Connection getConnection() {
        return physicalConnection;
    }

    /**
     * @return true if the connection to the database is closed or not.
     * @throws SQLException if a database access error occurs
     */
    @Override
    public boolean isPhysicallyClosed() throws SQLException {
        return physicalConnection.isClosed();
    }


    @Override
    public PreparedStatement prepareStatement(final String sql) throws SQLException {
        // Use the xaConnection Object (which allow to have the PreparedStatement pool) but only for method with SQL
        return xaConnection.prepareStatement(sql);
    }

    @Override
    public void close() throws SQLException {
        xaConnection.notifyClose();
    }

    // Simple method delegation to the physical connection

    @Override
    public CallableStatement prepareCall(final String sql) throws SQLException {
        return physicalConnection.prepareCall(sql);
    }

    @Override
    public String nativeSQL(final String sql) throws SQLException {
        return physicalConnection.nativeSQL(sql);
    }

    @Override
    public void setAutoCommit(final boolean autoCommit) throws SQLException {
        physicalConnection.setAutoCommit(autoCommit);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return physicalConnection.getAutoCommit();
    }

    @Override
    public void commit() throws SQLException {
        physicalConnection.commit();
    }

    @Override
    public void rollback() throws SQLException {
        physicalConnection.rollback();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return physicalConnection.isClosed();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return physicalConnection.getMetaData();
    }

    @Override
    public void setReadOnly(final boolean readOnly) throws SQLException {
        physicalConnection.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return physicalConnection.isReadOnly();
    }

    @Override
    public void setCatalog(final String catalog) throws SQLException {
        physicalConnection.setCatalog(catalog);
    }

    @Override
    public String getCatalog() throws SQLException {
        return physicalConnection.getCatalog();
    }

    @Override
    public void setTransactionIsolation(final int level) throws SQLException {
        physicalConnection.setTransactionIsolation(level);
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return physicalConnection.getTransactionIsolation();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return physicalConnection.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        physicalConnection.clearWarnings();
    }

    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency) throws SQLException {
        return physicalConnection.createStatement(resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        return physicalConnection.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        return physicalConnection.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return physicalConnection.getTypeMap();
    }

    @Override
    public void setTypeMap(final Map<String, Class<?>> map) throws SQLException {
        physicalConnection.setTypeMap(map);
    }

    @Override
    public void setHoldability(final int holdability) throws SQLException {
        physicalConnection.setHoldability(holdability);
    }

    @Override
    public int getHoldability() throws SQLException {
        return physicalConnection.getHoldability();
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return physicalConnection.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(final String name) throws SQLException {
        return physicalConnection.setSavepoint(name);
    }

    @Override
    public void rollback(final Savepoint savepoint) throws SQLException {
        physicalConnection.rollback(savepoint);
    }

    @Override
    public void releaseSavepoint(final Savepoint savepoint) throws SQLException {
        physicalConnection.releaseSavepoint(savepoint);
    }

    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        return physicalConnection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        return physicalConnection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        return physicalConnection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final int autoGeneratedKeys) throws SQLException {
        return physicalConnection.prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final int[] columnIndexes) throws SQLException {
        return physicalConnection.prepareStatement(sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final String[] columnNames) throws SQLException {
        return physicalConnection.prepareStatement(sql, columnNames);
    }

    @Override
    public Clob createClob() throws SQLException {
        return physicalConnection.createClob();
    }

    @Override
    public Blob createBlob() throws SQLException {
        return physicalConnection.createBlob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return physicalConnection.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return physicalConnection.createSQLXML();
    }

    @Override
    public boolean isValid(final int timeout) throws SQLException {
        return physicalConnection.isValid(timeout);
    }

    @Override
    public void setClientInfo(final String name, final String value) throws SQLClientInfoException {
        physicalConnection.setClientInfo(name, value);
    }

    @Override
    public void setClientInfo(final Properties properties) throws SQLClientInfoException {
        physicalConnection.setClientInfo(properties);
    }

    @Override
    public String getClientInfo(final String name) throws SQLException {
        return physicalConnection.getClientInfo(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return physicalConnection.getClientInfo();
    }

    @Override
    public Array createArrayOf(final String typeName, final Object[] elements) throws SQLException {
        return physicalConnection.createArrayOf(typeName, elements);
    }

    @Override
    public Struct createStruct(final String typeName, final Object[] attributes) throws SQLException {
        return physicalConnection.createStruct(typeName, attributes);
    }

    @Override
    public void setSchema(final String schema) throws SQLException {
        physicalConnection.setSchema(schema);
    }

    @Override
    public String getSchema() throws SQLException {
        return physicalConnection.getSchema();
    }

    @Override
    public void abort(final Executor executor) throws SQLException {
        physicalConnection.abort(executor);
    }

    @Override
    public void setNetworkTimeout(final Executor executor, final int milliseconds) throws SQLException {
        physicalConnection.setNetworkTimeout(executor, milliseconds);
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return physicalConnection.getNetworkTimeout();
    }

    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        return physicalConnection.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return physicalConnection.isWrapperFor(iface);
    }

    @Override
    public Statement createStatement() throws SQLException {
        return physicalConnection.createStatement();
    }
}