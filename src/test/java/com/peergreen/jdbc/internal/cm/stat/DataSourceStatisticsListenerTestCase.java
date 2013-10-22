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

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.transaction.Transaction;

import static org.testng.Assert.assertEquals;

/**
 * User: guillaume
 * Date: 22/10/13
 * Time: 14:10
 */
public class DataSourceStatisticsListenerTestCase {

    @Mock
    private Transaction transaction1;

    @Mock
    private Transaction transaction2;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetWaitedTime() throws Exception {
        DataSourceStatisticsListener statistics = new DataSourceStatisticsListener();

        Duration duration = statistics.getWaitedTime();

        assertEquals(duration.getTotal(), 0);
        statistics.waiterStopWaiting(42, false);
        assertEquals(duration.getTotal(), 42);
        statistics.waiterStopWaiting(12, false);
        assertEquals(duration.getTotal(), 54);
    }

    @Test
    public void testGetBusy() throws Exception {
        DataSourceStatisticsListener statistics = new DataSourceStatisticsListener();

        Value value = statistics.getBusy();

        assertEquals(value.getValue(), 0);
        statistics.busyConnections(42);
        assertEquals(value.getValue(), 42);
        statistics.busyConnections(12);
        assertEquals(value.getValue(), 12);
    }

    @Test
    public void testGetWaiters() throws Exception {
        DataSourceStatisticsListener statistics = new DataSourceStatisticsListener();

        Counter counter = statistics.getWaiters();

        assertEquals(counter.getLatest(), 0);
        statistics.waiterStartWaiting();
        assertEquals(counter.getLatest(), 1);
        statistics.waiterStartWaiting();
        statistics.waiterStartWaiting();
        assertEquals(counter.getLatest(), 3);
        statistics.waiterStopWaiting(100, false);
        assertEquals(counter.getLatest(), 2);
        statistics.waiterStopWaiting(100, false);
        statistics.waiterStopWaiting(100, false);
        assertEquals(counter.getLatest(), 0);
    }

    @Test
    public void testGetCreatedConnections() throws Exception {
        DataSourceStatisticsListener statistics = new DataSourceStatisticsListener();

        Increment increment = statistics.getCreatedConnections();

        assertEquals(increment.getValue(), 0);
        statistics.connectionCreated();
        assertEquals(increment.getValue(), 1);
        statistics.connectionCreated();
        statistics.connectionCreated();
        assertEquals(increment.getValue(), 3);
    }

    @Test
    public void testGetDestroyedConnections() throws Exception {
        DataSourceStatisticsListener statistics = new DataSourceStatisticsListener();

        Increment increment = statistics.getDestroyedConnections();

        assertEquals(increment.getValue(), 0);
        statistics.connectionDestroyed();
        assertEquals(increment.getValue(), 1);
        statistics.connectionDestroyed();
        statistics.connectionDestroyed();
        assertEquals(increment.getValue(), 3);
    }

    @Test
    public void testGetReuseOfConnections() throws Exception {
        DataSourceStatisticsListener statistics = new DataSourceStatisticsListener();

        Increment increment = statistics.getReuseOfConnections();

        assertEquals(increment.getValue(), 0);
        statistics.connectionValidated();
        assertEquals(increment.getValue(), 1);
        statistics.connectionValidated();
        statistics.connectionValidated();
        assertEquals(increment.getValue(), 3);
    }

    @Test
    public void testGetTimeoutRejection() throws Exception {
        DataSourceStatisticsListener statistics = new DataSourceStatisticsListener();

        Increment increment = statistics.getTimeoutRejection();

        assertEquals(increment.getValue(), 0);
        statistics.waiterRejectedTimeout();
        assertEquals(increment.getValue(), 1);
        statistics.waiterRejectedTimeout();
        statistics.waiterRejectedTimeout();
        assertEquals(increment.getValue(), 3);
    }

    @Test
    public void testGetOverflowRejection() throws Exception {
        DataSourceStatisticsListener statistics = new DataSourceStatisticsListener();

        Increment increment = statistics.getOverflowRejection();

        assertEquals(increment.getValue(), 0);
        statistics.waiterRejectedOverflow();
        assertEquals(increment.getValue(), 1);
        statistics.waiterRejectedOverflow();
        statistics.waiterRejectedOverflow();
        assertEquals(increment.getValue(), 3);
    }

    @Test
    public void testGetFailureRejection() throws Exception {
        DataSourceStatisticsListener statistics = new DataSourceStatisticsListener();

        Increment increment = statistics.getFailureRejection();

        assertEquals(increment.getValue(), 0);
        statistics.waiterRejectedFailure();
        assertEquals(increment.getValue(), 1);
        statistics.waiterRejectedFailure();
        statistics.waiterRejectedFailure();
        assertEquals(increment.getValue(), 3);
    }

    @Test
    public void testGetServedConnections() throws Exception {
        DataSourceStatisticsListener statistics = new DataSourceStatisticsListener();

        Increment increment = statistics.getServedConnections();

        assertEquals(increment.getValue(), 0);
        statistics.connectionServed();
        assertEquals(increment.getValue(), 1);
        statistics.connectionServed();
        statistics.connectionServed();
        assertEquals(increment.getValue(), 3);
    }

    @Test
    public void testGetInTransactions() throws Exception {
        DataSourceStatisticsListener statistics = new DataSourceStatisticsListener();

        Counter counter = statistics.getInTransaction();

        assertEquals(counter.getLatest(), 0);
        statistics.connectionEnlisted(transaction1);
        assertEquals(counter.getLatest(), 1);
        statistics.connectionDelisted(transaction1);
        assertEquals(counter.getLatest(), 0);

        statistics.connectionEnlisted(transaction1);
        statistics.connectionEnlisted(transaction2);
        assertEquals(counter.getLatest(), 2);
        statistics.connectionDelisted(transaction1);
        assertEquals(counter.getLatest(), 1);
        statistics.connectionDelisted(transaction2);
        assertEquals(counter.getLatest(), 0);

    }

    @Test
    public void testGetEnlistmentFailures() throws Exception {
        DataSourceStatisticsListener statistics = new DataSourceStatisticsListener();
        Increment increment = statistics.getEnlistmentFailures();

        assertEquals(increment.getValue(), 0);
        statistics.connectionEnlistmentError();
        assertEquals(increment.getValue(), 1);
        statistics.connectionEnlistmentError();
        statistics.connectionEnlistmentError();
        assertEquals(increment.getValue(), 3);
    }

    @Test
    public void testGetCompletedConnections() throws Exception {
        DataSourceStatisticsListener statistics = new DataSourceStatisticsListener();
        Increment increment = statistics.getCompletedConnections();

        assertEquals(increment.getValue(), 0);
        statistics.connectionFreedAfterTransactionCompletion(transaction1);
        assertEquals(increment.getValue(), 1);
        statistics.connectionFreedAfterTransactionCompletion(transaction1);
        statistics.connectionFreedAfterTransactionCompletion(transaction1);
        assertEquals(increment.getValue(), 3);
    }

    @Test
    public void testGetPerTransactionConnections() throws Exception {
        DataSourceStatisticsListener statistics = new DataSourceStatisticsListener();
        PartitionIncrement partition = statistics.getPerTransactionConnections();

        statistics.connectionReusedInSameTransaction(transaction1);
        assertEquals(partition.getAverage(), 1d);
        statistics.connectionReusedInSameTransaction(transaction1);
        statistics.connectionReusedInSameTransaction(transaction1);
        statistics.connectionReusedInSameTransaction(transaction2);
        assertEquals(partition.getAverage(), 2d);

        statistics.connectionFreedAfterTransactionCompletion(transaction2);
        assertEquals(partition.getAverage(), 3d);
        statistics.connectionFreedAfterTransactionCompletion(transaction1);
        assertEquals(partition.getAverage(), 0d);

        assertEquals(partition.getMaximum(), 3);
    }
}
