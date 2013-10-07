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

import com.peergreen.jdbc.internal.cm.ConnectionProxy;
import com.peergreen.jdbc.internal.cm.IManagedConnection;
import com.peergreen.jdbc.internal.cm.TransactionIsolation;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.sql.ConnectionEventListener;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * User: guillaume
 * Date: 11/10/13
 * Time: 10:29
 */
public class ManagedConnectionFactoryTestCase {

    public static final String SELECT_FROM_DUAL = "SELECT * FROM DUAL";
    @Mock
    private ConnectionEventListener listener;

    @Mock
    private NativeConnectionBuilder builder;

    @Mock
    private IManagedConnection mc;

    @Mock
    private ConnectionProxy handle;

    @Mock
    private Connection connection;

    @Mock
    private Statement statement;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateTransactionIsolationUndefined() throws Exception {

        when(builder.build(null)).thenReturn(connection);

        ManagedConnectionFactory factory = new ManagedConnectionFactory(builder, listener);
        factory.setTransactionIsolation(TransactionIsolation.TRANSACTION_UNDEFINED);
        IManagedConnection mc = factory.create(null);

        verify(connection, never()).setTransactionIsolation(anyInt());

        assertNotNull(mc);
        Connection handle = mc.getConnection();
        assertNotNull(handle);
        assertFalse(handle.isClosed());

        assertEquals(mc.getOpenCount(), 0);
        assertFalse(mc.isOpen());
        assertNull(mc.getTransaction());
    }

    @Test
    public void testCreateWithTransactionIsolation() throws Exception {

        when(builder.build(null)).thenReturn(connection);

        ManagedConnectionFactory factory = new ManagedConnectionFactory(builder, listener);
        factory.setTransactionIsolation(TransactionIsolation.TRANSACTION_SERIALIZABLE);
        IManagedConnection mc = factory.create(null);

        verify(connection).setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

        assertNotNull(mc);
    }

    @Test
    public void testValidateWithCheckLevelZero() throws Exception {

/*
        when(mc.getConnectionProxy()).thenReturn(handle);
        when(handle.isPhysicallyClosed()).thenReturn(false);
*/

        ManagedConnectionFactory factory = new ManagedConnectionFactory(builder, listener);
        assertTrue(factory.validate(mc));
        verifyZeroInteractions(mc);
    }

    @Test
    public void testValidateWithCheckLevelOneOk() throws Exception {

        when(mc.getConnectionProxy()).thenReturn(handle);
        when(handle.isPhysicallyClosed()).thenReturn(false);

        ManagedConnectionFactory factory = new ManagedConnectionFactory(builder, listener);
        factory.setCheckLevel(1);
        assertTrue(factory.validate(mc));
    }

    @Test
    public void testValidateWithCheckLevelOneKo() throws Exception {

        when(mc.getConnectionProxy()).thenReturn(handle);
        when(handle.isPhysicallyClosed()).thenReturn(true);

        ManagedConnectionFactory factory = new ManagedConnectionFactory(builder, listener);
        factory.setCheckLevel(1);
        assertFalse(factory.validate(mc));
    }

    @Test
    public void testValidateWithCheckLevelTwoOk() throws Exception {

        when(mc.getConnectionProxy()).thenReturn(handle);
        when(handle.isPhysicallyClosed()).thenReturn(false);
        when(handle.createStatement()).thenReturn(statement);

        ManagedConnectionFactory factory = new ManagedConnectionFactory(builder, listener);
        factory.setCheckLevel(2);
        factory.setTestStatement(SELECT_FROM_DUAL);
        assertTrue(factory.validate(mc));

        verify(statement).execute(SELECT_FROM_DUAL);
    }

    @Test
    public void testValidateWithCheckLevelTwoKo() throws Exception {

        when(mc.getConnectionProxy()).thenReturn(handle);
        when(handle.isPhysicallyClosed()).thenReturn(false);
        when(handle.createStatement()).thenReturn(statement);
        when(statement.execute(SELECT_FROM_DUAL)).thenThrow(SQLException.class);

        ManagedConnectionFactory factory = new ManagedConnectionFactory(builder, listener);
        factory.setCheckLevel(2);
        factory.setTestStatement(SELECT_FROM_DUAL);
        assertFalse(factory.validate(mc));
    }

    @Test
    public void testDestroy() throws Exception {
        ManagedConnectionFactory factory = new ManagedConnectionFactory(builder, listener);
        factory.destroy(mc);
        verify(mc).remove();
    }
}
