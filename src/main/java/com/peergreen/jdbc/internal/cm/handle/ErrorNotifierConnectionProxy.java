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

import com.peergreen.jdbc.internal.cm.ConnectionNotifier;
import com.peergreen.jdbc.internal.cm.ConnectionProxy;

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
 * User: guillaume
 * Date: 04/10/13
 * Time: 14:08
 */
public class ErrorNotifierConnectionProxy implements ConnectionProxy {
    private final ConnectionProxy delegate;
    private final ConnectionNotifier notifier;

    public ErrorNotifierConnectionProxy(final ConnectionProxy delegate, final ConnectionNotifier notifier) {
        this.delegate = delegate;
        this.notifier = notifier;
    }

    @Override
    public Connection getConnection() {
        return delegate.getConnection();
    }

    @Override
    public boolean isPhysicallyClosed() throws SQLException {
        try {
            return delegate.isPhysicallyClosed();
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public Statement createStatement() throws SQLException {
        try {
            return delegate.createStatement();
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public PreparedStatement prepareStatement(final String sql) throws SQLException {
        try {
            return delegate.prepareStatement(sql);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public CallableStatement prepareCall(final String sql) throws SQLException {
        try {
            return delegate.prepareCall(sql);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public String nativeSQL(final String sql) throws SQLException {
        try {
            return delegate.nativeSQL(sql);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public void setAutoCommit(final boolean autoCommit) throws SQLException {
        try {
            delegate.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        try {
            return delegate.getAutoCommit();
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public void commit() throws SQLException {
        try {
            delegate.commit();
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public void rollback() throws SQLException {
        try {
            delegate.rollback();
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public void close() throws SQLException {
        try {
            delegate.close();
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public boolean isClosed() throws SQLException {
        try {
            return delegate.isClosed();
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        try {
            return delegate.getMetaData();
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public void setReadOnly(final boolean readOnly) throws SQLException {
        try {
            delegate.setReadOnly(readOnly);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        try {
            return delegate.isReadOnly();
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public void setCatalog(final String catalog) throws SQLException {
        try {
            delegate.setCatalog(catalog);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public String getCatalog() throws SQLException {
        try {
            return delegate.getCatalog();
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public void setTransactionIsolation(final int level) throws SQLException {
        try {
            delegate.setTransactionIsolation(level);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        try {
            return delegate.getTransactionIsolation();
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        try {
            return delegate.getWarnings();
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public void clearWarnings() throws SQLException {
        try {
            delegate.clearWarnings();
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency) throws SQLException {
        try {
            return delegate.createStatement(resultSetType, resultSetConcurrency);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        try {
            return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        try {
            return delegate.prepareCall(sql, resultSetType, resultSetConcurrency);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        try {
            return delegate.getTypeMap();
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public void setTypeMap(final Map<String, Class<?>> map) throws SQLException {
        try {
            delegate.setTypeMap(map);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public void setHoldability(final int holdability) throws SQLException {
        try {
            delegate.setHoldability(holdability);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public int getHoldability() throws SQLException {
        try {
            return delegate.getHoldability();
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        try {
            return delegate.setSavepoint();
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public Savepoint setSavepoint(final String name) throws SQLException {
        try {
            return delegate.setSavepoint(name);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public void rollback(final Savepoint savepoint) throws SQLException {
        try {
            delegate.rollback(savepoint);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public void releaseSavepoint(final Savepoint savepoint) throws SQLException {
        try {
            delegate.releaseSavepoint(savepoint);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        try {
            return delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        try {
            return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        try {
            return delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final int autoGeneratedKeys) throws SQLException {
        try {
            return delegate.prepareStatement(sql, autoGeneratedKeys);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final int[] columnIndexes) throws SQLException {
        try {
            return delegate.prepareStatement(sql, columnIndexes);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final String[] columnNames) throws SQLException {
        try {
            return delegate.prepareStatement(sql, columnNames);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public Clob createClob() throws SQLException {
        try {
            return delegate.createClob();
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public Blob createBlob() throws SQLException {
        try {
            return delegate.createBlob();
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public NClob createNClob() throws SQLException {
        try {
            return delegate.createNClob();
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        try {
            return delegate.createSQLXML();
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public boolean isValid(final int timeout) throws SQLException {
        try {
            return delegate.isValid(timeout);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public void setClientInfo(final String name, final String value) throws SQLClientInfoException {
        try {
            delegate.setClientInfo(name, value);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public void setClientInfo(final Properties properties) throws SQLClientInfoException {
        try {
            delegate.setClientInfo(properties);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public String getClientInfo(final String name) throws SQLException {
        try {
            return delegate.getClientInfo(name);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        try {
            return delegate.getClientInfo();
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public Array createArrayOf(final String typeName, final Object[] elements) throws SQLException {
        try {
            return delegate.createArrayOf(typeName, elements);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public Struct createStruct(final String typeName, final Object[] attributes) throws SQLException {
        try {
            return delegate.createStruct(typeName, attributes);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public void setSchema(final String schema) throws SQLException {
        try {
            delegate.setSchema(schema);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public String getSchema() throws SQLException {
        try {
            return delegate.getSchema();
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public void abort(final Executor executor) throws SQLException {
        try {
            delegate.abort(executor);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public void setNetworkTimeout(final Executor executor, final int milliseconds) throws SQLException {
        try {
            delegate.setNetworkTimeout(executor, milliseconds);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        try {
            return delegate.getNetworkTimeout();
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        try {
            return delegate.unwrap(iface);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        try {
            return delegate.isWrapperFor(iface);
        } catch (SQLException e) {
            notifier.notifyError(e);
            throw e;
        }
    }
}
