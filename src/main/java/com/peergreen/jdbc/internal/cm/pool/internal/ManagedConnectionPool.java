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

package com.peergreen.jdbc.internal.cm.pool.internal;

import com.peergreen.jdbc.internal.cm.IManagedConnection;
import com.peergreen.jdbc.internal.cm.pool.AdjustablePool;
import com.peergreen.jdbc.internal.cm.pool.EmptyPoolLifecycleListener;
import com.peergreen.jdbc.internal.cm.pool.PoolFactory;
import com.peergreen.jdbc.internal.cm.pool.PoolLifecycleListener;
import com.peergreen.jdbc.internal.log.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ManagedConnectionPool implements AdjustablePool<IManagedConnection, UsernamePasswordInfo> {

    /**
     * Logger.
     */
    private final Log logger;

    /**
     * High Value for no limit for the connection pool.
     */
    private static final int NO_LIMIT = 99999;

    /**
     * 1 second == 1000 milliseconds
     */
    private static final int SECOND = 1000;

    /**
     * Default timeout for waiters (10s).
     */
    private static final long WAITER_TIMEOUT = 10 * SECOND;

    /**
     * Max waiters (by default).
     */
    private static final int DEFAULT_MAX_WAITERS = 1000;

    /**
     * max number of remove at once in the freelist We avoid removing too much
     * mcs at once for performance reasons.
     */
    private static final int MAX_REMOVE_FREELIST = 10;
    public static final int DEFAULT_PREPARED_STATEMENT_CACHE_SIZE = 12;

    private final PoolFactory<IManagedConnection, UsernamePasswordInfo> factory;

    /**
     * List of IManagedConnection not currently used. This avoids closing and
     * reopening physical connections. We try to keep a minimum of minConPool
     * elements here.
     */
    private Set<IManagedConnection> availables = new TreeSet<>();

    /**
     * Total list of IManagedConnection physically opened.
     */
    private List<IManagedConnection> connections = new LinkedList<>();

    /**
     * default user.
     */
    private String userName = null;

    /**
     * default passwd.
     */
    private String password = null;

    /**
     * minimum size of the connection pool.
     */
    private int poolMin = 0;

    /**
     * maximum size of the connection pool. default value is "NO LIMIT".
     */
    private int poolMax = NO_LIMIT;

    /**
     * max nb of milliseconds to wait for a connection when pool is empty.
     */
    private long waiterTimeout = WAITER_TIMEOUT;

    /**
     * max nb of waiters allowed to wait for a Connection.
     */
    private int maxWaiters = DEFAULT_MAX_WAITERS;

    /**
     * nb of threads waiting for a Connection.
     */
    private int currentWaiters = 0;

    /**
     * PreparedStatement cache size
     */
    private int preparedStatementCacheSize = DEFAULT_PREPARED_STATEMENT_CACHE_SIZE;

    private PoolLifecycleListener listener = new EmptyPoolLifecycleListener();

    public ManagedConnectionPool(final Log logger, final PoolFactory<IManagedConnection, UsernamePasswordInfo> factory) {
        this.logger = logger;
        this.factory = factory;
    }

    public void setPoolLifecycleListener(final PoolLifecycleListener listener) {
        this.listener = listener;
    }

    /**
     * @param max max pool size.
     */
    public synchronized void setPoolMax(final int max) {
        if (poolMax != max) {
            if (max < 0 || max > NO_LIMIT) {
                // New size is no limit
                poolMax = NO_LIMIT;
                if (currentWaiters > 0) {
                    notifyAll();
                }
            } else {
                if (currentWaiters > 0 && poolMax < max) {
                    // New size is bigger
                    // TODO only notify some
                    notify();
                }
                poolMax = max;
                adjust();
            }
        }
    }

    /**
     * @param min minimum connection pool size to be set.
     */
    public synchronized void setPoolMin(final int min) {
        if (poolMin != min) {
            poolMin = min;
            adjust();
        }
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * @param cacheSize PreparedStatement cache size.
     */
    public void setPreparedStatementCacheSize(final int cacheSize) {
        this.preparedStatementCacheSize = cacheSize;
        // Set the value in each connection.
        for (IManagedConnection mc : this.connections) {
            mc.setPstmtMax(cacheSize);
        }
    }

    public int getPreparedStatementCacheSize() {
        return preparedStatementCacheSize;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public int getPoolMin() {
        return poolMin;
    }

    public int getPoolMax() {
        return poolMax;
    }

    public void setWaiterTimeout(final long waiterTimeout) {
        this.waiterTimeout = waiterTimeout;
    }

    public long getWaiterTimeout() {
        return waiterTimeout;
    }

    public int getMaxWaiters() {
        return maxWaiters;
    }

    public void setMaxWaiters(final int maxWaiters) {
        this.maxWaiters = maxWaiters;
    }

    public int getCurrentWaiters() {
        return currentWaiters;
    }

    public int getCurrentOpened() {
        return connections.size();
    }

    /**
     * @return int number of busy xa connection.
     */
    public int getCurrentBusy() {
        return this.connections.size() - this.availables.size();
    }

    public void start() {
        // Create initial set of managed connections
        adjust();
    }

    public void stop() {
        // Remove available elements first
        List<IManagedConnection> available = new ArrayList<>(availables);
        for (IManagedConnection connection : available) {
            availables.remove(connection);
            factory.destroy(connection);
            listener.connectionDestroyed();
        }

        // Then discard connections
        List<IManagedConnection> all = new ArrayList<>(connections);
        for (IManagedConnection connection : all) {
            discard(connection);
        }
    }

    /**
     * compute current min/max busyConnections.
     */
    public void recomputeBusy() {
        listener.busyConnections(getCurrentBusy());
    }

    /**
     * Adjust the pool size, according to poolMax and poolMin values. Also
     * remove old connections in the availables.
     */
    @Override
    public synchronized void adjust() {
        //logger.debug(this.dSName);

        // Remove max aged elements in freelist
        // - Not more than MAX_REMOVE_FREELIST
        // - Don't reduce pool size less than poolMin
        int count = this.connections.size() - this.poolMin;
        // In case count is null, a new connection will be
        // recreated just after
        if (count >= 0) {
            if (count > MAX_REMOVE_FREELIST) {
                count = MAX_REMOVE_FREELIST;
            }
            for (Iterator<IManagedConnection> i = this.availables.iterator(); i.hasNext(); ) {
                IManagedConnection mc = i.next();
                if (mc.isAged()) {
                    logger.fine("remove a timed out connection");
                    i.remove();
                    discard(mc);
                    count--;
                    if (count <= 0) {
                        break;
                    }
                }
            }
        }
        recomputeBusy();

        // This section should not be useful with leak detection system
        // We may use discard() here
        // Close (physically) connections lost (opened for too long time)
        for (Iterator<IManagedConnection> i = this.connections.iterator(); i.hasNext(); ) {
            IManagedConnection mc = i.next();
            if (mc.inactive()) {
                logger.warn("close a timed out open connection %d", mc.getIdentifier());
                i.remove();
                // destroy mc
                factory.destroy(mc);
                listener.connectionDestroyed();
                // manager.setConnectionLeaks(manager.getConnectionLeaks() + 1);
                // Notify 1 thread waiting for a Connection.
                if (this.currentWaiters > 0) {
                    notify();
                }
            }
        }

        // Shrink the pool in case of max pool size
        // This occurs when max pool size has been reduced by admin console.
        if (this.poolMax != NO_LIMIT) {
            while (this.availables.size() > this.poolMin && this.connections.size() > this.poolMax) {
                IManagedConnection mc = this.availables.iterator().next();
                this.availables.remove(mc);
                // As we're reducing the pool's size, it's not necessary to wake up a waiter
                discard(mc, false);
            }
        }
        recomputeBusy();

        // Recreate more Connections while poolMin is not reached
        while (this.connections.size() < this.poolMin) {
            IManagedConnection mc = null;
            try {
                mc = factory.create(new UsernamePasswordInfo(userName, password));
                listener.connectionCreated();
            } catch (Exception e) {
                throw new IllegalStateException("Could not create " + this.poolMin + " mcs in the pool : ", e);
            }
            // tx = null. Assumes maxage already configured.
            this.availables.add(mc);
            this.connections.add(mc);

            // Notify 1 thread waiting for a Connection.
            if (this.currentWaiters > 0) {
                notify();
            }
        }
    }

    public IManagedConnection get() throws Exception {
        return get(null);
    }

    @Override
    public synchronized IManagedConnection get(UsernamePasswordInfo info) throws Exception {

        // Do not accept un-valued parameter: use a default
        if (info == null) {
            info = new UsernamePasswordInfo(userName, password);
        }

        IManagedConnection mc = null;
        // Loop until a valid mc is found
        long timeout = this.waiterTimeout;
        long before = 0;
        while (mc == null) {
            // try to find an mc in the free list
            if (this.availables.isEmpty()) {
                // In case we have reached the maximum limit of the pool,
                // we must wait until a connection is released.
                if (isMaximumSizeReached()) {
                    boolean expired = true;
                    // If a timeout has been specified, wait, unless maxWaiters
                    // is reached.
                    if (timeout > 0) {
                        if (isWaitPossible()) {
                            this.currentWaiters++;

                            if (before == 0) {
                                before = System.currentTimeMillis();
                                logger.fine("Wait for a free Connection, %d", this.connections.size());
                            }

                            listener.waiterStartWaiting();

                            try {
                                wait(timeout);
                            } catch (InterruptedException ign) {
                                logger.warn("Interrupted");
                            } finally {
                                this.currentWaiters--;
                            }
                            long after = System.currentTimeMillis();
                            long waited = after - before;
                            timeout = this.waiterTimeout - waited;
                            expired = (timeout <= 0);

                            listener.waiterStopWaiting(timeout, expired);

                            if (!expired) {
                                // I'm really not sure that we should test connection pseudo availability here
                                // We have been notified by a released connection
                                if (!this.availables.isEmpty() || this.connections.size() < this.poolMax) {
                                    logger.fine("Notified after %d milliseconds", waited);
                                    // Go to the beginning of the loop (there should be a connection for us)
                                    continue;
                                } // else: no connection left, strange case, should not happen
                            }
                        }
                    }
                    if (expired && this.availables.isEmpty() && isMaximumSizeReached()) {
                        if (before > 0) {
                            listener.waiterRejectedTimeout();
                            logger.warn("Cannot create a Connection - timeout");
                        } else {
                            listener.waiterRejectedOverflow();
                            logger.warn("Cannot create a Connection");
                        }
                        throw new SQLException("No more connections");
                    }
                    continue;
                }
                logger.fine("empty free list: Create a new Connection");
                try {
                    // create a new ManagedConnection
                    mc = factory.create(info);
                    listener.connectionCreated();
                } catch (Exception e) {
                    listener.waiterRejectedFailure();
                    logger.warn("Cannot create new Connection for transaction", e);
                    throw e;
                }
                this.connections.add(mc);
            } else {
                mc = this.availables.iterator().next();
                this.availables.remove(mc);
                // Check the connection before reusing it
                if (!factory.validate(mc)) {
                    factory.destroy(mc);
                    listener.connectionDestroyed();
                    before = 0;
                    mc = null;
                } else {
                    listener.connectionValidated();
                }
            }
        }

        // Update PreparedStatement cache size value
        mc.setPstmtMax(preparedStatementCacheSize);
        recomputeBusy();
        return mc;
    }

    private boolean isWaitPossible() {
        return this.currentWaiters < this.maxWaiters;
    }

    private boolean isMaximumSizeReached() {
        return this.connections.size() >= this.poolMax;
    }

    /**
     * Free item and return it in the free list.
     *
     * @param item The item to be freed
     */
    @Override
    public synchronized void release(final IManagedConnection item) {
        // Add it to the free list
        // Even if maxage is reached, because we avoids going under min pool
        // size.
        // PoolKeeper will manage aged connections.
        this.availables.add(item);
        logger.fine("item added to availables: %d", item.getIdentifier());

        // Notify 1 thread waiting for a Connection.
        if (this.currentWaiters > 0) {
            notify();
        }
        recomputeBusy();
    }

    /**
     * Destroy an mc because connection closed or error occurred.
     * Notify a waiter (if any) that a connection may be available.
     * Calls {@link #discard(com.peergreen.jdbc.internal.cm.IManagedConnection, boolean)} with {@literal true}.
     *
     * @param mc The mc to be destroyed
     */
    @Override
    public synchronized void discard(final IManagedConnection mc) {
        discard(mc, true);
    }

    /**
     * Destroy an mc because connection closed or error occurred.
     *
     * @param mc The mc to be destroyed
     * @param notify if any waiter thread should be notified
     */
    public synchronized void discard(final IManagedConnection mc, boolean notify) {
        this.connections.remove(mc);
        factory.destroy(mc);
        listener.connectionDestroyed();
        // Notify 1 thread waiting for a Connection.
        if (notify && (this.currentWaiters > 0)) {
            notify();
        }
        recomputeBusy();
    }

    public int getCurrentFree() {
        return availables.size();
    }

}