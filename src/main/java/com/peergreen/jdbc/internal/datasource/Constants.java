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

package com.peergreen.jdbc.internal.datasource;

/**
 * Recognized {@link javax.sql.DataSource} configuration properties.
 */
public interface Constants {
    /**
     * Required name of the DataSource, used in many places:
     * <ul>
     *     <li>Logger's configuration: a logger instance is dedicated to this DataSource, it's then easy to switch on/off logs for a given DataSource</li>
     *     <li>OSGi Service property: the DataSource is automatically exposed as an OSGi Service (interface {@link javax.sql.DataSource}), qualified with the DataSource name</li>
     *     <li>JNDI Binding: if requested, the DataSource may be bound into the global JNDI using the provided DataSource name</li>
     * </ul>
     */
    String DATASOURCE_NAME = "datasource.name";
    String URL = "url";
    String USERNAME = "username";
    String PASSWORD = "password";

    /**
     * If set, the DataSource will also be registered in the global JNDI under its name
     * (provided through {@link #DATASOURCE_NAME} property) (defaults to {@literal true}).
     */
    String JNDI_BIND = "jndi.bind";

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
    String JDBC_CHECK_LEVEL = "jdbc.check.level";

    /**
     * Max age of a Connection in milliseconds. When the time is elapsed, the
     * connection will be closed. This avoids keeping connections open too long
     * for nothing (defaults to 1 day).
     */
    String JDBC_MAX_AGE = "jdbc.max.age";

    /**
     * SQL test statement for connection's verification (only used when {@link #JDBC_CHECK_LEVEL}=2) (defaults to {@literal null}).
     */
    String JDBC_TEST_STATEMENT = "jdbc.test.statement";

    /**
     * Transaction isolation level for JDBC connections (defaults to {@link com.peergreen.jdbc.internal.cm.TransactionIsolation#TRANSACTION_UNDEFINED}).
     * Possible values:
     * <ul>
     *     <li>{@link com.peergreen.jdbc.internal.cm.TransactionIsolation#TRANSACTION_UNDEFINED}: Default, do not change isolation level</li>
     *     <li>{@link com.peergreen.jdbc.internal.cm.TransactionIsolation#TRANSACTION_SERIALIZABLE}</li>
     *     <li>{@link com.peergreen.jdbc.internal.cm.TransactionIsolation#TRANSACTION_REPEATABLE_READ}</li>
     *     <li>{@link com.peergreen.jdbc.internal.cm.TransactionIsolation#TRANSACTION_READ_UNCOMMITTED}</li>
     *     <li>{@link com.peergreen.jdbc.internal.cm.TransactionIsolation#TRANSACTION_READ_COMMITTED}</li>
     *     <li>{@link com.peergreen.jdbc.internal.cm.TransactionIsolation#TRANSACTION_NONE}: Do not use, this is an error.</li>
     * </ul>
     */
    String JDBC_TRANSACTION_ISOLATION = "jdbc.transaction.isolation";

    /**
     * No PreparedStatement cache by default (0).
     */
    String JDBC_PREPAREDSTATEMENT_CACHESIZE = "jdbc.preparedstatement.cachesize";

    /**
     * Minimum size of the connection pool (default to 0).
     */
    String POOL_MIN = "pool.min";

    /**
     * Maximum size of the connection pool (default value is 99999, no limit).
     */
    String POOL_MAX = "pool.max";

    /**
     * Maximum numbers of waiters allowed to wait for a Connection (default to 1000).
     */
    String POOL_WAITERS_MAX = "pool.waiters.max";

    /**
     * Maximum number of milliseconds to wait for a connection when the pool is empty (defaults to 10 seconds).
     */
    String POOL_WAITERS_TIMEOUT = "pool.waiters.timeout";


    // Not used at the moment

    String JDBC_MAX_OPENTIME = "jdbc.max.opentime";
    String LOGIN_TIMEOUT = "login.timeout";
}
