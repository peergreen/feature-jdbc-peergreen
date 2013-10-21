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

package com.peergreen.jdbc.internal.cm;

import com.peergreen.jdbc.internal.cm.pool.Pool;
import com.peergreen.jdbc.internal.cm.pool.internal.UsernamePasswordInfo;
import com.peergreen.jdbc.internal.log.Log;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

/**
 * DataSource implementation. Manage a pool of connections.
 *
 * @author Philippe Durieux
 * @author Florent Benoit
 */
public class ConnectionManager implements ConnectionEventListener {

    /**
     * Logger.
     */
    private final Log logger;

    /**
     * Default sampling period.
     */
    private static final int DEFAULT_SAMPLING = 60;

    /**
     * Transaction manager.
     */
    private final TransactionManager transactionManager;

    /**
     * This HashMap gives the IManagedConnection from its transaction Requests
     * with same tx get always the same connection.
     */
    private Map<Transaction, IManagedConnection> transactions = new HashMap<>();

    /**
     * Number of getConnection() served.
     */
    private int servedOpen = 0;

    /**
     * DataSource name (mainly for logging purpose)
     */
    private String dataSourceName;

    /**
     * The pool of managed connections.
     */
    private Pool<IManagedConnection, UsernamePasswordInfo> pool;


    /**
     * Constructor for ObjectFactory.
     */
    public ConnectionManager(final Log logger, TransactionManager transactionManager) {
        this.logger = logger;
        this.transactionManager = transactionManager;
    }

    /**
     * Gets the name of the datasource.
     *
     * @return the name of the datasource
     */
    public String getDatasourceName() {
        return this.dataSourceName;
    }

    /**
     * Sets the name of the datasource.
     *
     * @param dataSourceName the name of the datasource
     */
    public void setDatasourceName(final String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }


    public void setPool(final Pool<IManagedConnection, UsernamePasswordInfo> pool) {
        if (this.pool == null) {
            this.pool = pool;
        }
    }

    /**
     * sampling period in sec.
     */
    private int samplingPeriod = DEFAULT_SAMPLING; // default sampling period

    /**
     * @return sampling period in sec.
     */
    public int getSamplingPeriod() {
        return this.samplingPeriod;
    }

    /**
     * @param sec sampling period in sec.
     */
    public void setSamplingPeriod(final int sec) {
        if (sec > 0) {
            this.samplingPeriod = sec;
        }
    }

    /**
     * maximum nb of busy connections in last sampling period.
     */
    private int busyMaxRecent = 0;

    /**
     * @return maximum nb of busy connections in last sampling period.
     */
    public int getBusyMaxRecent() {
        return this.busyMaxRecent;
    }

    /**
     * minimum nb of busy connections in last sampling period.
     */
    private int busyMinRecent = 0;

    /**
     * @return minimum nb of busy connections in last sampling period.
     */
    public int getBusyMinRecent() {
        return this.busyMinRecent;
    }

    /**
     * total nb of connection leaks. A connection leak occurs when the caller
     * never issues a close method on the connection.
     */
    private int connectionLeaks = 0;

    /**
     * @return int number of connection leaks.
     */
    public int getConnectionLeaks() {
        return this.connectionLeaks;
    }

    /**
     * @return int number of xa connection served.
     */
    public int getServedOpen() {
        return this.servedOpen;
    }

    /**
     * maximum nb of waiters since datasource creation.
     */
    private int waitersHigh = 0;

    /**
     * @return maximum nb of waiters since the datasource creation.
     */
    public int getWaitersHigh() {
        return this.waitersHigh;
    }

    /**
     * maximum nb of waiters in last sampling period.
     */
    private int waitersHighRecent = 0;

    /**
     * @return maximum nb of waiters in last sampling period.
     */
    public int getWaitersHighRecent() {
        return this.waitersHighRecent;
    }

    /**
     * max waiting time in milliseconds.
     */
    private long waitingHigh = 0;

    /**
     * @return max waiting time since the datasource creation.
     */
    public long getWaitingHigh() {
        return this.waitingHigh;
    }

    /**
     * max waiting time in milliseconds in last sampling period.
     */
    private long waitingHighRecent = 0;

    /**
     * @return max waiting time in last sampling period.
     */
    public long getWaitingHighRecent() {
        return this.waitingHighRecent;
    }

    /**
     * {@inheritDoc}
     */
    public Connection getConnection() throws SQLException {
        return getConnection(null);
    }

    /**
     * Attempts to establish a connection with the data source that this
     * DataSource object represents. - comes from the javax.sql.DataSource
     * interface
     *
     * @param username - the database user on whose behalf the connection is
     *                 being made
     * @param password - the user's password
     * @return a connection to the data source
     * @throws SQLException - if a database access error occurs
     */
    public Connection getConnection(final String username, final String password) throws SQLException {

        UsernamePasswordInfo info = new UsernamePasswordInfo(username, password);

        return getConnection(info);
    }

    private Connection getConnection(final UsernamePasswordInfo info) throws SQLException {
        IManagedConnection mc = null;

        // Get the current Transaction
        Transaction tx = null;
        try {
            tx = this.transactionManager.getTransaction();
        } catch (SystemException e) {
            this.logger.error("ConnectionManager: getTransaction failed", e);
        }
        this.logger.fine("Tx = %s", tx);

        // Get a ManagedConnection in the pool for this user
        mc = openConnection(tx, info);
        Connection ret = mc.getConnection();

        // Enlist XAResource if we are actually in a transaction
        if (tx != null) {
            if (mc.getOpenCount() == 1) { // Only if first/only thread
                try {
                    this.logger.fine("enlist XAResource on %s", tx);
                    tx.enlistResource(mc.getXAResource());
                    ret.setAutoCommit(false);
                } catch (RollbackException e) {
                    // Although tx has been marked to be rolled back,
                    // XAResource has been correctly enlisted.
                    this.logger.warn("XAResource enlisted, but tx is marked rollback", e);
                } catch (IllegalStateException e) {
                    // In case tx is committed, no need to register resource!
                    ret.setAutoCommit(true);
                } catch (Exception e) {
                    this.logger.error("Cannot enlist XAResource, Connection will not be enlisted in a transaction", e);

                    // should return connection in the pool XXX
                    throw new SQLException("Cannot enlist XAResource");
                }
            }
        } else {
            ret.setAutoCommit(true); // in case we do not start a Tx
        }

        // return a Connection object
        return ret;
    }

    /**
     * Lookup connection in the pool for this user/transaction.
     *
     *
     * @param transaction   Transaction the connection is involved
     * @param info
     * @return a free IManagedConnection (never null)
     * @throws SQLException Cannot open a connection because the pool's max size
     *                      is reached
     */
    private synchronized IManagedConnection openConnection(final Transaction transaction, final UsernamePasswordInfo info) throws SQLException {
        IManagedConnection mc = null;
        // If a Connection exists already for this transaction, just return it.
        // If no transaction, never reuse a connection already used.
        if (transaction != null) {
            mc = this.transactions.get(transaction);
            if (mc != null) {
                logger.fine("Reuse a Connection for same transaction");
                mc.hold();
                this.servedOpen++;
                return mc;
            }
        }
        try {
            mc = pool.get(info);
        } catch (Exception e) {
            throw new SQLException("Cannot get a ready Managed Connection from the pool", e);
        }

        mc.setTransaction(transaction);
        if (transaction == null) {
            logger.fine("Got a Connection - no TX: ");
        } else {
            logger.fine("Got a Connection for TX: ");
            // register synchronization
            try {
                transaction.registerSynchronization(new TransactionSynchronization(transaction));
                this.transactions.put(transaction, mc); // only if registerSynchronization was OK.
            } catch (javax.transaction.RollbackException e) {
                // / optimization is probably possible at this point
                logger.warn("Pool mc registered, but transaction is rollback only", e);
            } catch (javax.transaction.SystemException e) {
                logger.error("Error in pool: system exception from transaction manager ", e);
            } catch (IllegalStateException e) {
                // In case transaction has already committed, do as if no transaction.
                logger.warn("Got a Connection - committed TX: ", e);
                mc.setTransaction(null);
            }
        }
        mc.hold();
        this.servedOpen++;
        return mc;
    }

    /**
     * Notifies this <code>ConnectionEventListener</code> that the application
     * has called the method <code>close</code> on its representation of a
     * pooled connection.
     *
     * @param event an event object describing the source of the event
     */
    @Override
    public void connectionClosed(final ConnectionEvent event) {
        IManagedConnection mc = (IManagedConnection) event.getSource();
        closeConnection(mc, XAResource.TMSUCCESS);
    }

    /**
     * Notifies this <code>ConnectionEventListener</code> that a fatal error
     * has occurred and the pooled connection can no longer be used. The driver
     * makes this notification just before it throws the application the
     * <code>SQLException</code> contained in the given
     * <code>ConnectionEvent</code> object.
     *
     * @param event an event object describing the source of the event and
     *              containing the <code>SQLException</code> that the driver is
     *              about to throw
     */
    @Override
    public void connectionErrorOccurred(final ConnectionEvent event) {

        IManagedConnection mc = (IManagedConnection) event.getSource();
        logger.fine("mc=%d", mc.getIdentifier());

        // remove it from the list of open connections for this thread
        // only if it was opened outside a tx.
        closeConnection(mc, XAResource.TMFAIL);
    }

    /**
     * The transaction has committed (or rolled back). We can return its
     * connections to the pool of available connections.
     *
     * @param tx the non null transaction
     */
    public synchronized void freeConnections(final Transaction tx) {
        logger.fine("free connection for Tx = %s", tx);
        IManagedConnection mc = this.transactions.remove(tx);
        if (mc == null) {
            logger.error("pool: no connection found to free for Tx = %s", tx);
            return;
        }
        mc.setTransaction(null);
        if (mc.isOpen()) {
            // Connection not yet closed (but committed).
            logger.fine("Connection not closed by caller");
            return;
        }
        pool.release(mc);
    }

    // -----------------------------------------------------------------------
    // private methods
    // -----------------------------------------------------------------------

    /**
     * Mark a specific Connection in the pool as closed. If it is no longer
     * associated to a Tx, we can free it.
     *
     * @param mc   XAConnection being closed
     * @param flag TMSUCCESS (normal close) or TMFAIL (error) or null if error.
     * @return false if has not be closed (still in use)
     */
    private boolean closeConnection(final IManagedConnection mc, final int flag) {
        // The connection will be available only if not associated
        // to a transaction. Else, it will be reusable only for the
        // same transaction.
        if (!mc.release()) {
            return false;
        }
        if (mc.getTransaction() != null) {
            logger.fine("keep connection for same transaction");
        } else {
            pool.release(mc);
        }

        // delist Resource if in transaction
        Transaction transaction = null;
        try {
            transaction = this.transactionManager.getTransaction();
        } catch (SystemException e) {
            logger.error("Pool: getTransaction failed:", e);
        }
        if (transaction != null && mc.isClosed()) {
            try {
                transaction.delistResource(mc.getXAResource(), flag);
            } catch (Exception e) {
                logger.error("Pool: Exception while delisting resource:", e);
            }
        }
        return true;
    }

    private class TransactionSynchronization implements Synchronization {
        private final Transaction transaction;

        public TransactionSynchronization(final Transaction transaction) {
            this.transaction = transaction;
        }

        @Override
        public void beforeCompletion() {

        }

        @Override
        public void afterCompletion(final int status) {
            freeConnections(transaction);
        }
    }
}