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

package com.peergreen.jdbc.internal.cm.stat;

import com.peergreen.jdbc.internal.cm.ConnectionManagerListener;
import com.peergreen.jdbc.internal.cm.pool.PoolLifecycleListener;

import javax.transaction.Transaction;

/**
 * User: guillaume
 * Date: 08/10/13
 * Time: 16:48
 */
public class DataSourceStatisticsListener implements PoolLifecycleListener, ConnectionManagerListener {

    // Boundaries
    private final long from;
    private long to;

    // Counters
    private Duration waitedTime = new Duration();
    private Value busy = new Value();
    private Counter waiters = new Counter();
    private Increment createdConnections = new Increment();
    private Increment destroyedConnections = new Increment();
    private Increment reuseOfConnections = new Increment();
    private Increment timeoutRejection = new Increment();
    private Increment overflowRejection = new Increment();
    private Increment failureRejection = new Increment();
    private Increment servedConnections = new Increment();
    private Counter inTransaction = new Counter();
    private Increment completedConnections = new Increment();
    private PartitionIncrement perTransactionConnections = new PartitionIncrement();
    private Increment enlistmentFailures = new Increment();

    public DataSourceStatisticsListener() {
        this(System.currentTimeMillis());
    }

    public DataSourceStatisticsListener(final long from) {
        this.from = from;
    }

    public Duration getWaitedTime() {
        return waitedTime;
    }

    public Value getBusy() {
        return busy;
    }

    /**
     * Waiters' counter.
     */
    public Counter getWaiters() {
        return waiters;
    }

    /**
     * Total number of opened physical connections since the datasource
     * creation.
     */
    public Increment getCreatedConnections() {
        return createdConnections;
    }

    public Increment getDestroyedConnections() {
        return destroyedConnections;
    }

    public Increment getReuseOfConnections() {
        return reuseOfConnections;
    }

    public Increment getTimeoutRejection() {
        return timeoutRejection;
    }

    public Increment getOverflowRejection() {
        return overflowRejection;
    }

    public Increment getFailureRejection() {
        return failureRejection;
    }

    public Increment getServedConnections() {
        return servedConnections;
    }

    public Counter getInTransaction() {
        return inTransaction;
    }

    public Increment getCompletedConnections() {
        return completedConnections;
    }

    public PartitionIncrement getPerTransactionConnections() {
        return perTransactionConnections;
    }

    public Increment getEnlistmentFailures() {
        return enlistmentFailures;
    }

    public long getFrom() {
        return from;
    }

    public long getTo() {
        return to;
    }

    @Override
    public void connectionCreated() {
        createdConnections.update(1);
        update();
    }

    private void update() {
        // Update top boundary
        to = System.currentTimeMillis();
    }

    @Override
    public void connectionDestroyed() {
        destroyedConnections.update(1);
        update();
    }

    @Override
    public void connectionValidated() {
        reuseOfConnections.update(1);
        update();
    }

    @Override
    public void waiterStartWaiting() {
        waiters.update(1);
        update();
    }

    @Override
    public void waiterStopWaiting(final long elapsed, final boolean timedOut) {
        waiters.update(-1);
        waitedTime.update(elapsed);
        update();
    }

    @Override
    public void waiterRejectedTimeout() {
        timeoutRejection.update(1);
        update();
    }

    @Override
    public void waiterRejectedOverflow() {
        overflowRejection.update(1);
        update();
    }

    @Override
    public void waiterRejectedFailure() {
        failureRejection.update(1);
        update();
    }

    @Override
    public void busyConnections(final int current) {
        busy.update(current);
        update();
    }

    @Override
    public void connectionEnlisted(final Transaction transaction) {
        inTransaction.update(1);
        update();
    }

    @Override
    public void connectionEnlistmentError() {
        enlistmentFailures.update(1);
        update();
    }

    @Override
    public void connectionDelisted(final Transaction transaction) {
        inTransaction.update(-1);
        update();
    }

    @Override
    public void connectionServed() {
        servedConnections.update(1);
        update();
    }

    @Override
    public void connectionReusedInSameTransaction(final Transaction transaction) {
        perTransactionConnections.increment(transaction);
        update();
    }

    @Override
    public void connectionFreedAfterTransactionCompletion(final Transaction transaction) {
        completedConnections.update(1);
        perTransactionConnections.forget(transaction);
        update();
    }
}
