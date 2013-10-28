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

import javax.sql.XAConnection;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Specify all interface that are used by the Managed Connection.
 * @author Florent BENOIT
 */
public interface IManagedConnection extends Comparable<IManagedConnection>,
                                            XAConnection,
                                            XAResource,
                                            ConnectionNotifier {

    /**
     * @return value of reused prepared statement.
     */
    int getReUsedPreparedStatements();


    /**
     * @return The identifier of the managed connection.
     */
    int getIdentifier();


    /**
     * @return open count
     */
    int getOpenCount();

    /**
     * @return the Transaction
     */
    Transaction getTransaction();

    /**
     * Notify as opened.
     */
    void hold();

    /**
     * @return true if connection max age has expired
     */
    boolean isAged();

    /**
     * Check if the connection has been unused for too long time.
     * This occurs usually when the caller forgot to call close().
     * @return true if open time has been reached, and not involved in a tx.
     */
    boolean inactive();

    /**
     * @return true if connection is closed
     */
    boolean isClosed();

    /**
     * @return true if connection is still open
     */
    boolean isOpen();

    /**
     * notify as closed.
     * @return true if normal close.
     */
    boolean release();

    /**
     * remove this item, ignoring exception on close.
     */
    void remove();

    /**
     * Dynamically change the prepared statement pool size.
     * @param max the maximum of prepared statement.
     */
    void setPstmtMax(final int max);

    /**
     * Set the associated transaction.
     * @param tx Transaction
     */
    void setTransaction(final Transaction tx);

    /**
     * Try to find a PreparedStatement in the pool.
     * @param sql the given sql query.
     * @throws SQLException if an error in the database occurs.
     * @return a given prepared statement.
     */
    PreparedStatement prepareStatement(final String sql) throws SQLException;

    /**
     * @return the connection handle
     */
    ConnectionProxy getConnectionProxy();

}