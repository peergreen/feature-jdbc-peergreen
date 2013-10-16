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
    String DATASOURCE_NAME = "datasource.name";
    String URL = "url";
    String USERNAME = "username";
    String PASSWORD = "password";
    String JNDI_BIND = "jndi.bind";
    String JDBC_CHECK_LEVEL = "jdbc.check.level";
    String JDBC_MAX_AGE = "jdbc.max.age";
    String JDBC_MAX_OPENTIME = "jdbc.max.opentime";
    String JDBC_TEST_STATEMENT = "jdbc.test.statement";
    String JDBC_TRANSACTION_ISOLATION = "jdbc.transaction.isolation";
    String JDBC_PREPAREDSTATEMENT_CACHESIZE = "jdbc.preparedstatement.cachesize";
    String POOL_MIN = "pool.min";
    String POOL_MAX = "pool.max";
    String POOL_WAITERS_MAX = "pool.waiters.max";
    String POOL_WAITERS_TIMEOUT = "pool.waiters.timeout";
    String LOGIN_TIMEOUT = "login.timeout";
    String SAMPLING_PERIOD = "sampling.period";
}
