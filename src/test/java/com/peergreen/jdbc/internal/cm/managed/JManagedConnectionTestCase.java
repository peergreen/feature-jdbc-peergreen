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

import com.peergreen.jdbc.internal.cm.pool.internal.ManagedConnectionFactory;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.transaction.Transaction;
import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * User: guillaume
 * Date: 15/10/13
 * Time: 09:43
 */
public class JManagedConnectionTestCase {

    @Mock
    private Connection connection;
    @Mock
    private ManagedConnectionFactory factory;
    @Mock
    private Transaction transaction;
    @Mock
    private ConnectionEventListener listener;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testConnectionAgeIsInBoundaries() throws Exception {
        // Provide a great maximum so that I'm sure that test will always be ok
        when(factory.getMaxAge()).thenReturn(5000l);

        JManagedConnection mc = new JManagedConnection(connection, factory);
        assertFalse(mc.isAged());
    }

    @Test
    public void testConnectionAgeIsHigher() throws Exception {
        // Provide a low maximum
        when(factory.getMaxAge()).thenReturn(10l);

        JManagedConnection mc = new JManagedConnection(connection, factory);
        Thread.sleep(100);
        assertTrue(mc.isAged());
    }

    @Test
    public void testConnectionIsInitiallyNotOpen() throws Exception {
        // Provide a minimum value
        when(factory.getMaxAge()).thenReturn(1000l);

        JManagedConnection mc = new JManagedConnection(connection, factory);
        assertFalse(mc.isOpen());
        assertTrue(mc.isClosed());
    }

    @Test
    public void testConnectionIsOpenWhenHold() throws Exception {
        // Provide a minimum value
        when(factory.getMaxAge()).thenReturn(1000l);

        JManagedConnection mc = new JManagedConnection(connection, factory);
        mc.hold();
        assertTrue(mc.isOpen());
        assertFalse(mc.isClosed());
    }

    @Test
    public void testConnectionIsNotOpenWhenHoldAndReleased() throws Exception {
        // Provide a minimum value
        when(factory.getMaxAge()).thenReturn(1000l);

        JManagedConnection mc = new JManagedConnection(connection, factory);
        mc.hold();
        mc.release();
        assertFalse(mc.isOpen());
        assertTrue(mc.isClosed());
    }

    @Test
    public void testConnectionReleasedButNotHold() throws Exception {
        // Provide a minimum value
        when(factory.getMaxAge()).thenReturn(1000l);

        JManagedConnection mc = new JManagedConnection(connection, factory);
        assertFalse(mc.release());
        assertEquals(mc.getOpenCount(), 0);
        assertTrue(mc.isClosed());
    }

    @Test
    public void testConnectionIsActiveWhenInTransaction() throws Exception {
        // Provide a minimum value
        when(factory.getMaxAge()).thenReturn(1000l);

        JManagedConnection mc = new JManagedConnection(connection, factory);
        mc.hold();
        mc.setTransaction(transaction);
        assertFalse(mc.inactive());
    }

    @Test
    public void testConnectionIsActiveWhenInTransactionButMaxAgeReached() throws Exception {
        // Provide a minimal value
        when(factory.getMaxAge()).thenReturn(10l);

        JManagedConnection mc = new JManagedConnection(connection, factory);
        mc.hold();
        mc.setTransaction(transaction);
        Thread.sleep(50);
        assertFalse(mc.inactive());
    }

    @Test
    public void testConnectionIsActiveWhenInTransactionAndStillUsed() throws Exception {
        // Provide a minimum value
        when(factory.getMaxAge()).thenReturn(1000l);

        JManagedConnection mc = new JManagedConnection(connection, factory);
        mc.hold();
        mc.setTransaction(transaction);
        assertFalse(mc.inactive());
    }

    @Test
    public void testConnectionIsInactiveWhenTooAged() throws Exception {
        // Provide a minimal value
        when(factory.getMaxAge()).thenReturn(10l);

        JManagedConnection mc = new JManagedConnection(connection, factory);
        mc.hold();
        Thread.sleep(50);
        assertTrue(mc.inactive());
    }

    @Test
    public void testConnectionCleanup() throws Exception {
        // Provide a minimum value
        when(factory.getMaxAge()).thenReturn(100l);

        JManagedConnection mc = new JManagedConnection(connection, factory);
        mc.setTransaction(transaction);
        mc.remove();
        assertNull(mc.getTransaction());
        assertNull(mc.getConnection());
        assertNull(mc.getConnectionProxy());
        verify(connection).close();
    }

    @Test
    public void testConnectionNotifyClose() throws Exception {
        // Provide a minimum value
        when(factory.getMaxAge()).thenReturn(100l);

        JManagedConnection mc = new JManagedConnection(connection, factory);
        mc.addConnectionEventListener(listener);
        mc.notifyClose();

        verify(listener).connectionClosed(any(ConnectionEvent.class));
    }

    @Test
    public void testConnectionNotifyError() throws Exception {
        // Provide a minimum value
        when(factory.getMaxAge()).thenReturn(100l);

        JManagedConnection mc = new JManagedConnection(connection, factory);
        mc.addConnectionEventListener(listener);
        mc.notifyError(new SQLException());

        verify(listener).connectionErrorOccurred(any(ConnectionEvent.class));
    }

    // TODO Add tests about PreparedStatements, XAResource, ...
}
