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
import com.peergreen.jdbc.internal.cm.managed.JManagedConnection;
import com.peergreen.jdbc.internal.cm.pool.PoolFactory;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import javax.sql.ConnectionEventListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class ManagedConnectionFactory implements PoolFactory<IManagedConnection, UsernamePasswordInfo> {
    /**
     * Logger.
     */
    private static final Log logger = LogFactory.getLog(ManagedConnectionFactory.class);

    /**
     * Nb of milliseconds in a day.
     */
    private static final long ONE_DAY = 1440L * 60L * 1000L;
    private final ConnectionEventListener listener;

    /**
     * Isolation level for JDBC.
     */
    private TransactionIsolation isolationLevel = TransactionIsolation.TRANSACTION_UNDEFINED;

    /**
     * max open time for a connection, in millisec.
     */
    private long maxOpenTime = ONE_DAY;

    /**
     * Max age of a Connection in milliseconds. When the time is elapsed, the
     * connection will be closed. This avoids keeping connections open too long
     * for nothing.
     */
    private long maxAge = ONE_DAY;

    /**
     * Level of checking on connections when got from the pool. this avoids
     * reusing bad connections because too old, for example when database was
     * restarted...
     * <ol>
     *     <li>0 = no checking (default)</li>
     *     <li>1 = check that still physically opened.</li>
     *     <li>2 = try a null statement.</li>
     * </ol>
     */
    private int checkLevel = 0;

    /**
     * test statement used when checkLevel=2.
     */
    private String testStatement;

    private final NativeConnectionBuilder builder;

    public ManagedConnectionFactory(final NativeConnectionBuilder builder, final ConnectionEventListener listener) {
        this.listener = listener;
        this.builder = builder;
    }

    /**
     * Sets the transaction isolation level of the connections.
     *
     * @param level the level of isolation.
     */
    public void setTransactionIsolation(final TransactionIsolation level) {
        isolationLevel = level;
    }

    /**
     * Gets the transaction isolation level.
     *
     * @return transaction isolation level.
     */
    public TransactionIsolation getTransactionIsolation() {
        return isolationLevel;
    }

    public int getCheckLevel() {
        return checkLevel;
    }

    public void setCheckLevel(final int checkLevel) {
        this.checkLevel = checkLevel;
    }

    public String getTestStatement() {
        return testStatement;
    }

    public void setTestStatement(final String testStatement) {
        this.testStatement = testStatement;
    }

    /**
     * @return max age for connections (in millisecs).
     */
    public long getMaxOpenTime() {
        return this.maxOpenTime;
    }

    public void setMaxOpenTime(final long maxOpenTime) {
        this.maxOpenTime = maxOpenTime;
    }

    /**
     * @return max age for connections (in millisecond).
     */
    public long getMaxAge() {
        return this.maxAge;
    }

    /**
     * @param age max age of connection in milliseconds.
     */
    public void setMaxAge(final long age) {
        this.maxAge = age;
    }



    public IManagedConnection create(final UsernamePasswordInfo info) throws SQLException {
        // Create the native connection in the builder
        Connection connection = builder.build(info);

        // Attempt to set the transaction isolation level
        // Depending on the underlying database, this may not succeed.
        if (this.isolationLevel != TransactionIsolation.TRANSACTION_UNDEFINED) {
            try {
                logger.debug("Set transaction isolation to {0}", this.isolationLevel);
                connection.setTransactionIsolation(isolationLevel.level());
            } catch (SQLException e) {
                logger.error("Cannot set transaction isolation to {0}", isolationLevel.name(), e);
                this.isolationLevel = TransactionIsolation.TRANSACTION_UNDEFINED;
            }
        }

        // Create the IManagedConnection object
        // return the XAConnection
        JManagedConnection mc = new JManagedConnection(connection, this);
        mc.addConnectionEventListener(listener);
        return mc;
    }

    @Override
    public boolean validate(final IManagedConnection mc) {
        if (this.checkLevel > 0) {
            try {
                ConnectionProxy handle = mc.getConnectionProxy();
                if (handle.isPhysicallyClosed()) {
                    logger.warn("The JDBC connection has been closed!");
                    return false;
                }
                if (this.checkLevel > 1) {
                    Statement stmt = handle.createStatement();
                    stmt.execute(this.testStatement);
                    stmt.close();
                }
            } catch (Exception e) {
                logger.error("DataSource error: removing invalid mc", e);
                return false;
            }
        }

        return true;
    }

    @Override
    public void destroy(final IManagedConnection mc) {
        mc.remove();
    }
}