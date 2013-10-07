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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * User: guillaume
 * Date: 15/10/13
 * Time: 14:44
 */
public class ReusablePreparedStatementTestCase {

    @Mock
    private PreparedStatement delegate;
    @Mock
    private ConnectionNotifier notifier;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testForceCloseWhenOpened() throws Exception {
        ReusablePreparedStatement ps = new ReusablePreparedStatement(delegate, notifier, "SELECT * FROM CLIENTS");
        assertTrue(ps.forceClose());
    }

    @Test
    public void testForceCloseWhenClosed() throws Exception {
        ReusablePreparedStatement ps = new ReusablePreparedStatement(delegate, notifier, "SELECT * FROM CLIENTS");
        ps.close();
        assertFalse(ps.forceClose());
    }

    @Test
    public void testCloseSendsNotifications() throws Exception {
        ReusablePreparedStatement ps = new ReusablePreparedStatement(delegate, notifier, "SELECT * FROM CLIENTS");
        ps.close();
        verify(notifier).notifyPsClose(ps);
    }

    @Test
    public void testCloseSendsNoNotificationsWhenAlreadyClosed() throws Exception {
        ReusablePreparedStatement ps = new ReusablePreparedStatement(delegate, notifier, "SELECT * FROM CLIENTS");
        ps.close();
        ps.close();
        verify(notifier, only()).notifyPsClose(ps);
    }

    @Test
    public void testForgetClosesTheInternalPreparedStatement() throws Exception {
        ReusablePreparedStatement ps = new ReusablePreparedStatement(delegate, notifier, "SELECT * FROM CLIENTS");
        ps.forget();
        verify(delegate).close();
    }

    @Test
    public void testReuseNotChanged() throws Exception {
        ReusablePreparedStatement ps = new ReusablePreparedStatement(delegate, notifier, "SELECT * FROM CLIENTS");
        ps.reuse();
        verify(delegate).clearParameters();
        verify(delegate).clearWarnings();
    }

    @Test
    public void testReuseChanged() throws Exception {
        ReusablePreparedStatement ps = new ReusablePreparedStatement(delegate, notifier, "SELECT * FROM CLIENTS");
        // force changed state
        ps.addBatch();
        ps.reuse();

        verify(delegate).clearBatch();
        verify(delegate).setFetchDirection(ResultSet.FETCH_FORWARD);
        verify(delegate).setMaxFieldSize(ReusablePreparedStatement.NO_LIMIT);
        verify(delegate).setMaxRows(ReusablePreparedStatement.NO_LIMIT);
        verify(delegate).setQueryTimeout(ReusablePreparedStatement.NO_LIMIT);
    }
}
