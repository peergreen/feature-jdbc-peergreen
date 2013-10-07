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
import com.peergreen.jdbc.internal.cm.pool.PoolFactory;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;


/**
 * User: guillaume
 * Date: 11/10/13
 * Time: 11:44
 */
public class ManagedConnectionPoolTestCase {

    @Mock
    private PoolFactory<IManagedConnection, UsernamePasswordInfo> factory;
    @Mock
    private IManagedConnection mc;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testPoolInitConnectionsAreCreatedAtStartup() throws Exception {

        when(factory.create(any(UsernamePasswordInfo.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                return mock(IManagedConnection.class);
            }
        });

        ManagedConnectionPool pool = new ManagedConnectionPool(factory);
        pool.setPoolMin(10);
        pool.start();

        assertEquals(pool.getOpenedCount(), 10);
        verify(factory, times(10)).create(any(UsernamePasswordInfo.class));
    }

    @Test
    public void testGet() throws Exception {
        when(factory.create(any(UsernamePasswordInfo.class))).thenReturn(mc);
        ManagedConnectionPool pool = new ManagedConnectionPool(factory);
        pool.start();

        assertEquals(pool.getCurrentFree(), 0);

        assertNotNull(pool.get());

        assertEquals(pool.getCurrentFree(), 0);
        assertEquals(pool.getCurrentBusy(), 1);
        assertEquals(pool.getOpenedCount(), 1);
    }

    @Test
    public void testRelease() throws Exception {
        when(factory.create(any(UsernamePasswordInfo.class))).thenReturn(mc);
        ManagedConnectionPool pool = new ManagedConnectionPool(factory);
        pool.start();
        pool.release(pool.get());

        assertEquals(pool.getCurrentFree(), 1);
        assertEquals(pool.getCurrentBusy(), 0);
        assertEquals(pool.getOpenedCount(), 1);
    }

    @Test
    public void testDiscard() throws Exception {
        when(factory.create(any(UsernamePasswordInfo.class))).thenReturn(mc);
        ManagedConnectionPool pool = new ManagedConnectionPool(factory);
        pool.start();
        pool.discard(pool.get());

        assertEquals(pool.getCurrentFree(), 0);
        assertEquals(pool.getCurrentBusy(), 0);
        assertEquals(pool.getOpenedCount(), 1);

        verify(factory).destroy(mc);
    }

    @Test
    public void testGetWaitTimeout() throws Exception {
        when(factory.create(any(UsernamePasswordInfo.class))).thenReturn(mc);
        when(factory.validate(mc)).thenReturn(true);
        ManagedConnectionPool pool = new ManagedConnectionPool(factory);
        pool.setPoolMax(1);
        pool.setWaiterTimeout(100);
        pool.start();

        pool.get();
        try {
            pool.get();
        } catch (Exception e) {
            // Expects a timeout
            assertTrue(e instanceof SQLException);
            assertEquals(pool.getRejectedTimeout(), 1);
            assertEquals(pool.getCurrentBusy(), 1);
            assertEquals(pool.getWaiterCount(), 1);
            return;
        }

        fail();
    }

    @Test
    public void testGetMultiThreadedWithWaiters() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        when(factory.create(any(UsernamePasswordInfo.class))).thenReturn(mc);
        when(factory.validate(mc)).thenReturn(true);
        final ManagedConnectionPool pool = new ManagedConnectionPool(factory);
        pool.setPoolMax(1);
        pool.setWaiterTimeout(100);
        pool.start();

        // Get the one connection from the pool
        IManagedConnection item = pool.get();

        Future<IManagedConnection> submit = executor.submit(new Callable<IManagedConnection>() {
            @Override
            public IManagedConnection call() throws Exception {
                return pool.get();
            }
        });

        pool.release(item);

        // Verify that the same ManagedConnection is reused
        assertEquals(submit.get(), mc);
    }

    @Test
    public void testGetMultiThreadedWithNumerousWaiters() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        when(factory.create(any(UsernamePasswordInfo.class))).thenReturn(mc);
        when(factory.validate(mc)).thenReturn(true);

        final ManagedConnectionPool pool = new ManagedConnectionPool(factory);
        pool.setPoolMax(1);
        pool.start();

        // Get the one connection from the pool
        IManagedConnection item = pool.get();

        Future<IManagedConnection> t1 = executor.submit(new GetAndReleaseCallable(pool));
        Future<IManagedConnection> t2 = executor.submit(new GetAndReleaseCallable(pool));
        Future<IManagedConnection> t3 = executor.submit(new GetAndReleaseCallable(pool));
        Future<IManagedConnection> t4 = executor.submit(new GetAndReleaseCallable(pool));
        Future<IManagedConnection> t5 = executor.submit(new GetAndReleaseCallable(pool));
        Future<IManagedConnection> t6 = executor.submit(new GetAndReleaseCallable(pool));
        Future<IManagedConnection> t7 = executor.submit(new GetAndReleaseCallable(pool));

        pool.release(item);

        // Verify that the same ManagedConnection is reused
        assertEquals(t1.get(), mc);
        assertEquals(t2.get(), mc);
        assertEquals(t3.get(), mc);
        assertEquals(t4.get(), mc);
        assertEquals(t5.get(), mc);
        assertEquals(t6.get(), mc);
        assertEquals(t7.get(), mc);
    }

    private static class GetAndReleaseCallable implements Callable<IManagedConnection> {
        private final ManagedConnectionPool pool;

        public GetAndReleaseCallable(final ManagedConnectionPool pool) {
            this.pool = pool;
        }

        @Override
        public IManagedConnection call() throws Exception {
            IManagedConnection extracted = pool.get();
            Thread.sleep(20);
            pool.release(extracted);
            return extracted;
        }
    }

}
