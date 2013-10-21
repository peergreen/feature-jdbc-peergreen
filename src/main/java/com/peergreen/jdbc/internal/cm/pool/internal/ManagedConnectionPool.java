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
import com.peergreen.jdbc.internal.cm.pool.PoolFactory;
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
     * total number of opened physical connections since the datasource
     * creation.
     */
    private int openedCount = 0;

    /**
     * default user.
     */
    private String userName = null;
    /**
     * default passwd.
     */
    private String password = null;
    /**
     * count max waiters during current period.
     */
    private int waiterCount = 0;
    /**
     * count max waiting time during current period.
     */
    private long maxWaitedTime = 0;
    /**
     * count max busy connection during current period.
     */
    private int busyMax = 0;
    /**
     * count min busy connection during current period.
     */
    private int busyMin = 0;
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
     * total nb of open connection failures because waiter overflow.
     */
    private int rejectedFull = 0;
    /**
     * total nb of open connection failures because timeout.
     */
    private int rejectedTimeout = 0;
    /**
     * total nb of waiters since datasource creation.
     */
    private int totalWaiterCount = 0;
    /**
     * total waiting time in milliseconds.
     */
    private long totalWaitedTime = 0;
    /**
     * total nb of physical connection failures.
     */
    private int connectionFailures = 0;
    /**
     * total nb of open connection failures for any other reason.
     */
    private int rejectedOther = 0;
    /**
     * PreparedStatement cache size
     */
    private int preparedStatementCacheSize = DEFAULT_PREPARED_STATEMENT_CACHE_SIZE;

    public ManagedConnectionPool(final Log logger, final PoolFactory<IManagedConnection, UsernamePasswordInfo> factory) {
        this.logger = logger;
        this.factory = factory;
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

    public int getOpenedCount() {
        return openedCount;
    }

    public long getMaxWaitedTime() {
        return maxWaitedTime;
    }

    public int getWaiterCount() {
        return waiterCount;
    }

    public int getBusyMax() {
        return busyMax;
    }

    public int getBusyMin() {
        return busyMin;
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

    public int getRejectedFull() {
        return rejectedFull;
    }

    public int getRejectedTimeout() {
        return rejectedTimeout;
    }

    public int getTotalWaiterCount() {
        return totalWaiterCount;
    }

    public long getTotalWaitedTime() {
        return totalWaitedTime;
    }

    public int getConnectionFailures() {
        return connectionFailures;
    }

    public int getRejectedOther() {
        return rejectedOther;
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
        }

        // Then discard connections
        List<IManagedConnection> used = new ArrayList<>(connections);
        for (IManagedConnection connection : used) {
            discard(connection);
        }
    }

    /**
     * compute current min/max busyConnections.
     */
    public void recomputeBusy() {
        int busy = getCurrentBusy();
        if (this.busyMax < busy) {
            this.busyMax = busy;
        }
        if (this.busyMin > busy) {
            this.busyMin = busy;
        }
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

        // Close (physically) connections lost (opened for too long time)
        for (Iterator<IManagedConnection> i = this.connections.iterator(); i.hasNext(); ) {
            IManagedConnection mc = i.next();
            if (mc.inactive()) {
                logger.warn("close a timed out open connection %d", mc.getIdentifier());
                i.remove();
                // destroy mc
                factory.destroy(mc);
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
                discard(mc);
            }
        }
        recomputeBusy();

        // Recreate more Connections while poolMin is not reached
        while (this.connections.size() < this.poolMin) {
            IManagedConnection mc = null;
            try {
                mc = factory.create(new UsernamePasswordInfo(userName, password));
                openedCount++;
            } catch (Exception e) {
                throw new IllegalStateException("Could not create " + this.poolMin + " mcs in the pool : ", e);
            }
            // tx = null. Assumes maxage already configured.
            this.availables.add(mc);
            this.connections.add(mc);
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
                        if (this.currentWaiters < this.maxWaiters) {
                            this.currentWaiters++;
                            // Store the maximum concurrent waiters
                            if (this.waiterCount < this.currentWaiters) {
                                this.waiterCount = this.currentWaiters;
                            }
                            if (before == 0) {
                                before = System.currentTimeMillis();
                                logger.fine("Wait for a free Connection, %d", this.connections.size());
                            }

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
                            if (expired) {
                                // We have been waked up by the timeout.
                                this.totalWaiterCount++;
                                this.totalWaitedTime += waited;
                                if (this.maxWaitedTime < waited) {
                                    this.maxWaitedTime = waited;
                                }
                            } else {
                                if (!this.availables.isEmpty() || this.connections.size() < this.poolMax) {
                                    // We have been notified by a released connection
                                    logger.fine("Notified after %d milliseconds", waited);
                                    this.totalWaiterCount++;
                                    this.totalWaitedTime += waited;
                                    if (this.maxWaitedTime < waited) {
                                        this.maxWaitedTime = waited;
                                    }
                                }
                                continue;
                            }
                        }
                    }
                    if (expired && this.availables.isEmpty() && isMaximumSizeReached()) {
                        if (before > 0) {
                            this.rejectedTimeout++;
                            logger.warn("Cannot create a Connection - timeout");
                        } else {
                            this.rejectedFull++;
                            logger.warn("Cannot create a Connection");
                        }
                        throw new SQLException("No more connections in <DATASOURCE>");
                    }
                    continue;
                }
                logger.fine("empty free list: Create a new Connection");
                try {
                    // create a new ManagedConnection
                    mc = factory.create(info);
                    openedCount++;
                } catch (Exception e) {
                    connectionFailures++;
                    rejectedOther++;
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
                    before = 0;
                    mc = null;
                }
            }
        }

        // Update PreparedStatement cache size value
        mc.setPstmtMax(preparedStatementCacheSize);
        recomputeBusy();
        return mc;
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
     * Destroy an mc because connection closed or error occured.
     *
     * @param mc The mc to be destroyed
     */
    @Override
    public synchronized void discard(final IManagedConnection mc) {
        this.connections.remove(mc);
        factory.destroy(mc);
        // Notify 1 thread waiting for a Connection.
        if (this.currentWaiters > 0) {
            notify();
        }
        recomputeBusy();
    }

    public int getCurrentFree() {
        return availables.size();
    }

}