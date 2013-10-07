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

package com.peergreen.jdbc.internal.cm.statement;

import com.peergreen.jdbc.internal.cm.ConnectionNotifier;
import com.peergreen.jdbc.internal.cm.IPreparedStatement;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Wrapper on a PreparedStatement. This wrapper is used to track close method in
 * order to avoid closing the statement, and putting it instead in a pool.
 * @author Philippe Durieux
 * @author Florent Benoit
 */
public class ReusablePreparedStatement implements IPreparedStatement {

    public static final int NO_LIMIT = 0;
    /**
     * Properties of this statement has been changed ? Needs to be be cleared
     * when reused.
     */
    private boolean changed = false;

    /**
     * Is that this statement is opened ?
     */
    private boolean opened = false;

    /**
     * Being closed. (in close method).
     */
    private boolean closing = false;

    /**
     * Physical PreparedStatement object on which the wrapper is.
     */
    private PreparedStatement ps;

    /**
     * Managed Connection the Statement belongs to.
     */
    private ConnectionNotifier notifier;

    /**
     * Hashcode computed in constructor.
     */
    private int hashCode;

    /**
     * SQL used as statement.
     */
    private String sql;

    /**
     * Logger.
     */
    private Log logger = LogFactory.getLog(ReusablePreparedStatement.class);

    /**
     * Builds a new statement with the given wrapped statement of given
     * connection and given sql query.
     * @param ps the prepared statement.
     * @param notifier managed connection
     * @param sql query.
     */
    public ReusablePreparedStatement(final PreparedStatement ps, final ConnectionNotifier notifier, final String sql) {
        this.ps = ps;
        this.notifier = notifier;
        this.sql = sql;
        hashCode = sql.hashCode();
        opened = true;
    }

    /**
     * @return Sql query used.
     */
    @Override
    public String getSql() {
        return sql;
    }

    /**
     * Gets the preparedstatement used by this wrapper.
     * @return the internal prepared statement
     */
    protected PreparedStatement getInternalPreparedStatement() {
        return this.ps;
    }

    /**
     * @return hashcode of the object
     */
    @Override
    public int hashCode() {
        return hashCode;
    }

    /**
     * @param stmt given statement for comparing it
     * @return true if given object is equals to this current object
     */
    @Override
    public boolean equals(final Object stmt) {
        if (stmt == null) {
            return false;
        }
        // different hashcode, cannot be equals
        if (this.hashCode != stmt.hashCode()) {
            return false;
        }

        // if got same hashcode, try to see if cast is ok.
        if (!(stmt instanceof ReusablePreparedStatement)) {
            logger.warn("Bad class {0}", stmt);
            return false;
        }

        // Cast object
        ReusablePreparedStatement psw = (ReusablePreparedStatement) stmt;
        if (sql == null && psw.getSql() != null) {
            return false;
        }
        if (sql != null && !sql.equals(psw.getSql())) {
            return false;
        }
        try {
            if (psw.getInternalPreparedStatement().getResultSetType() != ps.getResultSetType()) {
                return false;
            }
            if (psw.getInternalPreparedStatement().getResultSetConcurrency() != ps.getResultSetConcurrency()) {
                return false;
            }
        } catch (SQLException e) {
            logger.warn("Cannot compare statements", e);
            return false;
        }
        logger.debug("Found");
        return true;
    }

    /**
     * Force a close on the Prepare Statement. Usually, it's the caller that did
     * not close it explicitly
     * @return true if it was open
     */
    @Override
    public boolean forceClose() {
        if (opened) {
            logger.debug("Statements should be closed explicitly.");
            opened = false;
            return true;
        }
        return false;
    }

    /**
     * Reuses this statement so reset properties.
     * @throws SQLException if reset fails
     */
    @Override
    public void reuse() throws SQLException {
        ps.clearParameters();
        ps.clearWarnings();
        opened = true;
        if (changed) {
            logger.debug("Properties statement have been changed, reset default properties");
            ps.clearBatch();
            ps.setFetchDirection(ResultSet.FETCH_FORWARD);
            ps.setMaxFieldSize(NO_LIMIT);
            ps.setMaxRows(NO_LIMIT);
            ps.setQueryTimeout(NO_LIMIT);
            changed = false;
        }
    }

    /**
     * @return true if this statement has been closed, else false.
     */
    @Override
    public boolean isClosed() {
        return !opened && !closing;
    }

    @Override
    public void setPoolable(final boolean poolable) throws SQLException {
        ps.setPoolable(poolable);
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return ps.isPoolable();
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        ps.closeOnCompletion();
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return ps.isCloseOnCompletion();
    }

    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        return ps.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return ps.isWrapperFor(iface);
    }

    /**
     * Physically close this Statement.
     */
    @Override
    public void forget() {
        try {
            ps.close();
        } catch (SQLException e) {
            logger.error("Cannot close the PreparedStatement", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws SQLException {
        if (!opened) {
            logger.debug("Statement already closed");
            return;
        }
        opened = false;
        closing = true;
        notifier.notifyPsClose(this);
        closing = false;
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return ps.getMaxFieldSize();
    }

    @Override
    public void setMaxFieldSize(final int max) throws SQLException {
        changed = true;
        ps.setMaxFieldSize(max);
    }

    @Override
    public int getMaxRows() throws SQLException {
        return ps.getMaxRows();
    }

    @Override
    public void setMaxRows(final int max) throws SQLException {
        changed = true;
        ps.setMaxRows(max);
    }

    @Override
    public void setEscapeProcessing(final boolean enable) throws SQLException {
        ps.setEscapeProcessing(enable);
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return ps.getQueryTimeout();
    }

    @Override
    public void setQueryTimeout(final int seconds) throws SQLException {
        changed = true;
        ps.setQueryTimeout(seconds);
    }

    @Override
    public void cancel() throws SQLException {
        ps.cancel();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return ps.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        ps.clearWarnings();
    }

    @Override
    public void setCursorName(final String name) throws SQLException {
        ps.setCursorName(name);
    }

    @Override
    public boolean execute(final String sql) throws SQLException {
        // TODO Should we throw an Exception since this method should not be called on a PreparedStatement (per spec) ?
        changed = true;
        return ps.execute(sql);
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return ps.getResultSet();
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return ps.getUpdateCount();
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return ps.getMoreResults();
    }

    @Override
    public void setFetchDirection(final int direction) throws SQLException {
        changed = true;
        ps.setFetchDirection(direction);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return ps.getFetchDirection();
    }

    @Override
    public void setFetchSize(final int rows) throws SQLException {
        changed = true;
        ps.setFetchSize(rows);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return ps.getFetchSize();
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return ps.getResultSetConcurrency();
    }

    @Override
    public int getResultSetType() throws SQLException {
        return ps.getResultSetType();
    }

    @Override
    public void addBatch(final String sql) throws SQLException {
        // TODO Should we throw an Exception since this method should not be called on a PreparedStatement (per spec) ?
        changed = true;
        ps.addBatch(sql);
    }

    @Override
    public void clearBatch() throws SQLException {
        ps.clearBatch();
    }

    @Override
    public int[] executeBatch() throws SQLException {
        return ps.executeBatch();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return ps.getConnection();
    }

    @Override
    public boolean getMoreResults(final int current) throws SQLException {
        return ps.getMoreResults(current);
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return ps.getGeneratedKeys();
    }

    @Override
    public int executeUpdate(final String sql, final int autoGeneratedKeys) throws SQLException {
        // TODO Should we throw an Exception since this method should not be called on a PreparedStatement (per spec) ?
        changed = true;
        return ps.executeUpdate(sql, autoGeneratedKeys);
    }

    @Override
    public int executeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
        // TODO Should we throw an Exception since this method should not be called on a PreparedStatement (per spec) ?
        changed = true;
        return ps.executeUpdate(sql, columnIndexes);
    }

    @Override
    public int executeUpdate(final String sql, final String[] columnNames) throws SQLException {
        // TODO Should we throw an Exception since this method should not be called on a PreparedStatement (per spec) ?
        changed = true;
        return ps.executeUpdate(sql, columnNames);
    }

    @Override
    public boolean execute(final String sql, final int autoGeneratedKeys) throws SQLException {
        // TODO Should we throw an Exception since this method should not be called on a PreparedStatement (per spec) ?
        changed = true;
        return ps.execute(sql, autoGeneratedKeys);
    }

    @Override
    public boolean execute(final String sql, final int[] columnIndexes) throws SQLException {
        // TODO Should we throw an Exception since this method should not be called on a PreparedStatement (per spec) ?
        changed = true;
        return ps.execute(sql, columnIndexes);
    }

    @Override
    public boolean execute(final String sql, final String[] columnNames) throws SQLException {
        // TODO Should we throw an Exception since this method should not be called on a PreparedStatement (per spec) ?
        changed = true;
        return ps.execute(sql, columnNames);
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return ps.getResultSetHoldability();
    }

    // PrepareStatement
    // ---------------------------------------------------


    @Override
    public ResultSet executeQuery() throws SQLException {
        return ps.executeQuery();
    }

    @Override
    public int executeUpdate() throws SQLException {
        changed = true;
        return ps.executeUpdate();
    }

    @Override
    public void setNull(final int parameterIndex, final int sqlType) throws SQLException {
        ps.setNull(parameterIndex, sqlType);
    }

    @Override
    public void setBoolean(final int parameterIndex, final boolean x) throws SQLException {
        ps.setBoolean(parameterIndex, x);
    }

    @Override
    public void setByte(final int parameterIndex, final byte x) throws SQLException {
        ps.setByte(parameterIndex, x);
    }

    @Override
    public void setShort(final int parameterIndex, final short x) throws SQLException {
        ps.setShort(parameterIndex, x);
    }

    @Override
    public void setInt(final int parameterIndex, final int x) throws SQLException {
        ps.setInt(parameterIndex, x);
    }

    @Override
    public void setLong(final int parameterIndex, final long x) throws SQLException {
        ps.setLong(parameterIndex, x);
    }

    @Override
    public void setFloat(final int parameterIndex, final float x) throws SQLException {
        ps.setFloat(parameterIndex, x);
    }

    @Override
    public void setDouble(final int parameterIndex, final double x) throws SQLException {
        ps.setDouble(parameterIndex, x);
    }

    @Override
    public void setBigDecimal(final int parameterIndex, final BigDecimal x) throws SQLException {
        ps.setBigDecimal(parameterIndex, x);
    }

    @Override
    public void setString(final int parameterIndex, final String x) throws SQLException {
        ps.setString(parameterIndex, x);
    }

    @Override
    public void setBytes(final int parameterIndex, final byte[] x) throws SQLException {
        ps.setBytes(parameterIndex, x);
    }

    @Override
    public void setDate(final int parameterIndex, final Date x) throws SQLException {
        ps.setDate(parameterIndex, x);
    }

    @Override
    public void setTime(final int parameterIndex, final Time x) throws SQLException {
        ps.setTime(parameterIndex, x);
    }

    @Override
    public void setTimestamp(final int parameterIndex, final Timestamp x) throws SQLException {
        ps.setTimestamp(parameterIndex, x);
    }

    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        ps.setAsciiStream(parameterIndex, x, length);
    }

    @Override
    public void setUnicodeStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        ps.setUnicodeStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        ps.setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void clearParameters() throws SQLException {
        ps.clearParameters();
    }

    @Override
    public void setObject(final int parameterIndex, final Object x, final int targetSqlType) throws SQLException {
        ps.setObject(parameterIndex, x, targetSqlType);
    }

    @Override
    public void setObject(final int parameterIndex, final Object x) throws SQLException {
        ps.setObject(parameterIndex, x);
    }

    @Override
    public boolean execute() throws SQLException {
        changed = true;
        return ps.execute();
    }

    @Override
    public void addBatch() throws SQLException {
        changed = true;
        ps.addBatch();
    }

    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader, final int length) throws SQLException {
        ps.setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setRef(final int parameterIndex, final Ref x) throws SQLException {
        ps.setRef(parameterIndex, x);
    }

    @Override
    public void setBlob(final int parameterIndex, final Blob x) throws SQLException {
        ps.setBlob(parameterIndex, x);
    }

    @Override
    public void setClob(final int parameterIndex, final Clob x) throws SQLException {
        ps.setClob(parameterIndex, x);
    }

    @Override
    public void setArray(final int parameterIndex, final Array x) throws SQLException {
        ps.setArray(parameterIndex, x);
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return ps.getMetaData();
    }

    @Override
    public void setDate(final int parameterIndex, final Date x, final Calendar cal) throws SQLException {
        ps.setDate(parameterIndex, x, cal);
    }

    @Override
    public void setTime(final int parameterIndex, final Time x, final Calendar cal) throws SQLException {
        ps.setTime(parameterIndex, x, cal);
    }

    @Override
    public void setTimestamp(final int parameterIndex, final Timestamp x, final Calendar cal) throws SQLException {
        ps.setTimestamp(parameterIndex, x, cal);
    }

    @Override
    public void setNull(final int parameterIndex, final int sqlType, final String typeName) throws SQLException {
        ps.setNull(parameterIndex, sqlType, typeName);
    }

    @Override
    public void setURL(final int parameterIndex, final URL x) throws SQLException {
        ps.setURL(parameterIndex, x);
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return ps.getParameterMetaData();
    }

    @Override
    public void setRowId(final int parameterIndex, final RowId x) throws SQLException {
        ps.setRowId(parameterIndex, x);
    }

    @Override
    public void setNString(final int parameterIndex, final String value) throws SQLException {
        ps.setNString(parameterIndex, value);
    }

    @Override
    public void setNCharacterStream(final int parameterIndex, final Reader value, final long length) throws SQLException {
        ps.setNCharacterStream(parameterIndex, value, length);
    }

    @Override
    public void setNClob(final int parameterIndex, final NClob value) throws SQLException {
        ps.setNClob(parameterIndex, value);
    }

    @Override
    public void setClob(final int parameterIndex, final Reader reader, final long length) throws SQLException {
        ps.setClob(parameterIndex, reader, length);
    }

    @Override
    public void setBlob(final int parameterIndex, final InputStream inputStream, final long length) throws SQLException {
        ps.setBlob(parameterIndex, inputStream, length);
    }

    @Override
    public void setNClob(final int parameterIndex, final Reader reader, final long length) throws SQLException {
        ps.setNClob(parameterIndex, reader, length);
    }

    @Override
    public void setSQLXML(final int parameterIndex, final SQLXML xmlObject) throws SQLException {
        ps.setSQLXML(parameterIndex, xmlObject);
    }

    @Override
    public void setObject(final int parameterIndex, final Object x, final int targetSqlType, final int scaleOrLength) throws SQLException {
        ps.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }

    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x, final long length) throws SQLException {
        ps.setAsciiStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x, final long length) throws SQLException {
        ps.setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader, final long length) throws SQLException {
        ps.setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x) throws SQLException {
        ps.setAsciiStream(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x) throws SQLException {
        ps.setBinaryStream(parameterIndex, x);
    }

    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader) throws SQLException {
        ps.setCharacterStream(parameterIndex, reader);
    }

    @Override
    public void setNCharacterStream(final int parameterIndex, final Reader value) throws SQLException {
        ps.setNCharacterStream(parameterIndex, value);
    }

    @Override
    public void setClob(final int parameterIndex, final Reader reader) throws SQLException {
        ps.setClob(parameterIndex, reader);
    }

    @Override
    public void setBlob(final int parameterIndex, final InputStream inputStream) throws SQLException {
        ps.setBlob(parameterIndex, inputStream);
    }

    @Override
    public void setNClob(final int parameterIndex, final Reader reader) throws SQLException {
        ps.setNClob(parameterIndex, reader);
    }

    @Override
    public ResultSet executeQuery(final String sql) throws SQLException {
        // TODO Should we throw an Exception since this method should not be called on a PreparedStatement (per spec) ?
        return ps.executeQuery(sql);
    }

    @Override
    public int executeUpdate(final String sql) throws SQLException {
        // TODO Should we throw an Exception since this method should not be called on a PreparedStatement (per spec) ?
        changed = true;
        return ps.executeUpdate(sql);
    }
}