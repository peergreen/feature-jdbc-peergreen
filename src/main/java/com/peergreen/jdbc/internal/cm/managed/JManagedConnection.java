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

package com.peergreen.jdbc.internal.cm.managed;

import com.peergreen.jdbc.internal.cm.ConnectionProxy;
import com.peergreen.jdbc.internal.cm.IManagedConnection;
import com.peergreen.jdbc.internal.cm.IPreparedStatement;
import com.peergreen.jdbc.internal.cm.handle.DefaultConnectionProxy;
import com.peergreen.jdbc.internal.cm.handle.ErrorNotifierConnectionProxy;
import com.peergreen.jdbc.internal.cm.pool.internal.ManagedConnectionFactory;
import com.peergreen.jdbc.internal.cm.statement.ReusablePreparedStatement;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.StatementEventListener;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * This class represents the connection managed by the pool. This connection is
 * a managed connection and is notified of the transaction events.
 * @author Philippe Durieux
 * @author Florent Benoit
 */
public class JManagedConnection implements IManagedConnection {

    /**
     * Logger.
     */
    private static final Log logger = LogFactory.getLog(JManagedConnection.class);

    public static final int NO_CACHE = 0;
    /**
     * No PreparedStatement cache by default.
     * Note that the pool is responsible to keep that value in sync with its own configuration.
     */
    public static final int DEFAULT_STATEMENT_CACHE_SIZE = NO_CACHE;

    /**
     * Connection to the database.
     */
    private Connection physicalConnection = null;

    /**
     * Connection returned to the user.
     */
    private ConnectionProxy m_connectionProxy = null;

    /**
     * Maximum of prepared statements.
     */
    private int pstmtmax = DEFAULT_STATEMENT_CACHE_SIZE;

    /**
     * Current number of opened prepared statements.
     */
    private int psOpenNb = 0;

    /**
     * Event listeners (of PooledConnection).
     */
    private Vector<ConnectionEventListener> eventListeners = new Vector<ConnectionEventListener>();

    /**
     * count of opening this connection. >0 if open.
     */
    private int open = 0;

    /**
     * Transaction timeout value.
     */
    private int timeout = 0;

    /**
     * Transaction the connection is involved with.
     */
    private Transaction transaction = null;

    /**
     * Counter of all managed connections created.
     */
    private static int objcount = 0;

    /**
     * Identifier of this connection.
     */
    private final int identifier;

    /**
     * Prepared statements that were reused.
     */
    private int reUsedPreparedStatements = 0;

    /**
     * List of PreparedStatement in the pool.
     */
    private final Map<String, IPreparedStatement> psList = Collections.synchronizedMap(new HashMap<String, IPreparedStatement>());

    private final ManagedConnectionFactory factory;

    /**
     * Time of the death for this connection.
     */
    private long deathTime = 0;

    /**
     * Time for closing this connection.
     */
    private long closeTime = 0;

    /**
     * Builds a new managed connection on a JDBC connection.
     * @param physicalConnection the physical JDBC Connection.
     * @param factory
     */
    public JManagedConnection(final Connection physicalConnection, final ManagedConnectionFactory factory) {
        this.physicalConnection = physicalConnection;
        this.factory = factory;

        this.m_connectionProxy = new ErrorNotifierConnectionProxy(new DefaultConnectionProxy(this, physicalConnection), this);
        deathTime = System.currentTimeMillis() + factory.getMaxAge();

        identifier = objcount++;
    }

    /**
     * @return The identifier of this JManagedConnection
     */
    @Override
    public int getIdentifier() {
        return identifier;
    }

    /**
     * Dynamically change the prepared statement pool size.
     * @param max the maximum of prepared statement.
     */
    @Override
    public void setPstmtMax(final int max) {
        pstmtmax = max;
    }

    /**
     * @return value of reused prepared statement.
     */
    @Override
    public int getReUsedPreparedStatements() {
        return reUsedPreparedStatements;
    }

    /**
     * @return true if connection max age has expired
     */
    @Override
    public boolean isAged() {
        return (deathTime < System.currentTimeMillis());
    }

    /**
     * @return true if connection is still open
     */
    @Override
    public boolean isOpen() {
        return (open > 0);
    }

    /**
     * @return open count
     */
    @Override
    public int getOpenCount() {
        return open;
    }

    /**
     * Check if the connection has been unused for too long time. This occurs
     * usually when the caller forgot to call close().
     * @return true if open time has been reached, and not involved in a transaction.
     */
    @Override
    public boolean inactive() {
        return (open > 0 && transaction == null && closeTime < System.currentTimeMillis());
    }

    /**
     * @return true if connection is closed
     */
    @Override
    public boolean isClosed() {
        return (open <= 0);
    }

    /**
     * Notify as opened.
     */
    @Override
    public void hold() {
        open++;
        closeTime = System.currentTimeMillis() + factory.getMaxOpenTime();
    }

    /**
     * notify as closed.
     * @return true if normal close.
     */
    @Override
    public boolean release() {
        open--;
        if (open < 0) {
            logger.warn("connection was already closed");
            open = 0;
            return false;
        }
        if (transaction == null && open > 0) {
            logger.error("connection-open counter overflow");
            open = 0;
        }
        return true;
    }

    /**
     * Set the associated transaction.
     * @param transaction Transaction
     */
    @Override
    public void setTransaction(final Transaction transaction) {
        this.transaction = transaction;
    }

    /**
     * @return the Transaction
     */
    @Override
    public Transaction getTransaction() {
        return transaction;
    }

    /**
     * remove this item, ignoring exception on close.
     */
    @Override
    public void remove() {
        // Close the physical connection
        try {
            close();
        } catch (java.sql.SQLException ign) {
            logger.error("Could not close Connection: ", ign);
        }

        // remove all references (for GC)
        transaction = null;

    }

    // -----------------------------------------------------------------
    // Other methods
    // -----------------------------------------------------------------

    /**
     * Try to find a PreparedStatement in the pool.
     * @param sql the given sql query.
     * @throws SQLException if an error in the database occurs.
     * @return a given prepared statement.
     */
    @Override
    public PreparedStatement prepareStatement(final String sql) throws SQLException {
        return prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }

    /**
     * Try to find a PreparedStatement in the pool for the given options.
     * @param sql the sql of the prepared statement
     * @param resultSetType the type of resultset
     * @param resultSetConcurrency the concurrency of this resultset
     * @return a preparestatement object
     * @throws SQLException if an errors occurs on the database.
     */
    private PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency)
            throws SQLException {

        logger.debug("sql = {0}", sql);
        // No PreparedStatement pooling
        if (pstmtmax == NO_CACHE) {
            return physicalConnection.prepareStatement(sql, resultSetType, resultSetConcurrency);
        }
        IPreparedStatement ps = null;
        synchronized (psList) {
            ps = psList.get(sql);
            if (ps != null) {
                if (!ps.isClosed()) {
                    logger.warn("reuse an open pstmt");
                }
                ps.reuse();
                reUsedPreparedStatements++;
            } else {
                // Not found in cache. Create a new one.
                PreparedStatement aps = physicalConnection.prepareStatement(sql, resultSetType, resultSetConcurrency);
                ps = new ReusablePreparedStatement(aps, this, sql);

                psList.put(sql, ps);
            }
            psOpenNb++;
        }
        return ps;
    }

    /**
     * A PreparedStatement has been logically closed.
     * @param ps a prepared statement.
     */
    @Override
    public void notifyPsClose(final IPreparedStatement ps) {
        logger.debug(ps.getSql());
        synchronized (psList) {
            psOpenNb--;
            if (psList.size() >= pstmtmax) {
                // Choose a closed element to remove.
                IPreparedStatement lru = null;
                Iterator<IPreparedStatement> i = psList.values().iterator();
                while (i.hasNext()) {
                    lru = i.next();
                    if (lru.isClosed()) {
                        // actually, remove the first closed element.
                        i.remove();
                        lru.forget();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public ConnectionProxy getConnectionProxy() {
        return m_connectionProxy;
    }

    // XAResource
    // ---------------------------------------------------------------------------------

    @Override
    public void commit(final Xid xid, final boolean b) throws XAException {
        logger.debug("XA-COMMIT for {0}", xid);

        // Commit the transaction
        try {
            physicalConnection.commit();
        } catch (SQLException e) {
            logger.error("Cannot commit transaction", e);
            notifyError(e);
            throw new XAException("Error on commit");
        }
    }

    /**
     * Determine if the resource manager instance represented by the target
     * object is the same as the resource manager instance represented by the
     * parameter xaResource.
     * @param xaResource An XAResource object
     * @return True if same RM instance, otherwise false.
     * @throws XAException XA protocol error
     */
    @Override
    public boolean isSameRM(final XAResource xaResource) throws XAException {
        // In this pseudo-driver, we must return true only if
        // both objects refer to the same XAResource, and not
        // the same Resource Manager, because actually, we must
        // send commit/rollback on each XAResource involved in
        // the transaction.
        if (xaResource.equals(this)) {
            logger.debug("isSameRM = true {0}", this);
            return true;
        }
        logger.debug("isSameRM = false {0}", this);
        return false;

    }

    /**
     * Inform the resource manager to roll back work done on behalf of a
     * transaction branch.
     * @param xid transaction xid
     * @throws XAException XA protocol error
     */
    @Override
    public void rollback(final Xid xid) throws XAException {
        logger.debug("XA-ROLLBACK for {0}", xid);

        // Make sure that we are not in AutoCommit mode
        try {
            if (physicalConnection.getAutoCommit()) {
                logger.error("Rollback called on XAResource with AutoCommit set");
                throw (new XAException(XAException.XA_HEURCOM));
            }
        } catch (SQLException e) {
            logger.error("Cannot getAutoCommit", e);
            notifyError(e);
            throw (new XAException("Error on getAutoCommit"));
        }

        // Rollback the transaction
        try {
            physicalConnection.rollback();
        } catch (SQLException e) {
            logger.error("Cannot rollback transaction", e);
            notifyError(e);
            throw (new XAException("Error on rollback"));
        }

    }

    /**
     * Ends the work performed on behalf of a transaction branch.
     * @param xid transaction xid
     * @param flags currently unused
     * @throws XAException XA protocol error
     */
    @Override
    public void end(final Xid xid, final int flags) throws XAException {
        logger.debug("XA-END for {0}", xid);
    }

    /**
     * Tell the resource manager to forget about a heuristically completed
     * transaction branch.
     * @param xid transaction xid
     * @throws XAException XA protocol error
     */
    @Override
    public void forget(final Xid xid) throws XAException {
        logger.debug("XA-FORGET for {0}", xid);
    }

    /**
     * Obtain the current transaction timeout value set for this XAResource
     * instance.
     * @return the current transaction timeout in seconds
     * @throws XAException XA protocol error
     */
    @Override
    public int getTransactionTimeout() throws XAException {
        logger.debug("getTransactionTimeout for {0}", this);
        return timeout;
    }

    /**
     * Ask the resource manager to prepare for a transaction commit of the
     * transaction specified in xid.
     * @param xid transaction xid
     * @throws XAException XA protocol error
     * @return always OK
     */
    @Override
    public int prepare(final Xid xid) throws XAException {
        logger.debug("XA-PREPARE for {0}", xid);
        // No 2PC on standard JDBC drivers
        return XAResource.XA_OK;
    }

    /**
     * Obtain a list of prepared transaction branches from a resource manager.
     * @param flag unused parameter.
     * @return an array of transaction Xids
     * @throws XAException XA protocol error
     */
    @Override
    public Xid[] recover(final int flag) throws XAException {
        logger.debug("XA-RECOVER for {0}", this);
        // Not implemented
        return null;
    }

    /**
     * Set the current transaction timeout value for this XAResource instance.
     * @param seconds timeout value, in seconds.
     * @return always true
     * @throws XAException XA protocol error
     */
    @Override
    public boolean setTransactionTimeout(final int seconds) throws XAException {
        logger.debug("setTransactionTimeout to {0} for {1}", seconds, this);
        timeout = seconds;
        return true;
    }

    /**
     * Start work on behalf of a transaction branch specified in xid.
     * @param xid transaction xid
     * @param flags unused parameter
     * @throws XAException XA protocol error
     */
    @Override
    public void start(final Xid xid, final int flags) throws XAException {
        logger.debug("XA-START for {0}", xid);
    }

    // ---------------------------------------------------------------------------------
    // XAResource


    // Comparable<IManagedConnection>
    // ---------------------------------------------------------------------------------

    /**
     * Compares this object with another specified object.
     * @param other the object to compare
     * @return a value detecting if these objects are matching or not.
     */
    @Override
    public int compareTo(final IManagedConnection other) {
        int diff = this.getReUsedPreparedStatements() - other.getReUsedPreparedStatements();
        if (diff == 0) {
            return this.getIdentifier() - other.getIdentifier();
        }
        return diff;
    }

    // ---------------------------------------------------------------------------------
    // Comparable<IManagedConnection>

    // XAConnection
    // ---------------------------------------------------------------------------------

    @Override
    public XAResource getXAResource() throws SQLException {
        return this;
    }

    // ---------------------------------------------------------------------------------
    // XAConnection

    // PooledConnection
    // ---------------------------------------------------------------------------------

    /**
     * Create an object handle for a database connection.
     * @exception SQLException - if a database-access error occurs
     * @return connection used by this managed connection
     */
    @Override
    public Connection getConnection() throws SQLException {
        // Just return the already created object.
        return getConnectionProxy();
    }

    /**
     * Close the database connection.
     * @exception SQLException - if a database-access error occurs
     */
    @Override
    public void close() throws SQLException {

        // Close the actual Connection here.
        if (physicalConnection != null) {
            physicalConnection.close();
        } else {
            logger.error("Connection already closed. Stack of this new close()", new Exception());
        }
        physicalConnection = null;
        m_connectionProxy = null;
    }

    /**
     * Add an event listener.
     * @param listener event listener
     */
    @Override
    public void addConnectionEventListener(final ConnectionEventListener listener) {
        eventListeners.addElement(listener);
    }

    /**
     * Remove an event listener.
     * @param listener event listener
     */
    @Override
    public void removeConnectionEventListener(final ConnectionEventListener listener) {
        eventListeners.removeElement(listener);
    }

    @Override
    public void addStatementEventListener(final StatementEventListener listener) {
        // TODO To be implemented
    }

    @Override
    public void removeStatementEventListener(final StatementEventListener listener) {
        // TODO To be implemented
    }

    // ---------------------------------------------------------------------------------
    // PooledConnection

    // ConnectionNotifier
    // ---------------------------------------------------------------------

    /**
     * Notify a Close event on Connection.
     */
    @Override
    public void notifyClose() {
        // Close all PreparedStatement not already closed
        // When a Connection has been closed, no PreparedStatement should
        // remain open. This can avoids lack of cursor on some databases.
        synchronized (psList) {
            if (psOpenNb > 0) {
                IPreparedStatement jst = null;
                Iterator<IPreparedStatement> i = psList.values().iterator();
                while (i.hasNext()) {
                    jst = i.next();
                    if (jst.forceClose()) {
                        psOpenNb--;
                    }
                }
                if (psOpenNb != 0) {
                    logger.warn("Bad psOpenNb value = {0}", psOpenNb);
                    psOpenNb = 0;
                }
            }
        }

        // Notify event to listeners
        for (int i = 0; i < eventListeners.size(); i++) {
            ConnectionEventListener l = eventListeners.elementAt(i);
            l.connectionClosed(new ConnectionEvent(this));
        }

    }

    /**
     * Notify an Error event on Connection.
     * @param ex the given exception
     */
    @Override
    public void notifyError(final SQLException ex) {
        // Notify event to listeners
        for (int i = 0; i < eventListeners.size(); i++) {
            ConnectionEventListener l = eventListeners.elementAt(i);
            l.connectionErrorOccurred(new ConnectionEvent(this, ex));
        }
    }

    // ---------------------------------------------------------------------
    // ConnectionNotifier


}