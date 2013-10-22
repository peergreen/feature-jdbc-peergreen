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

package com.peergreen.jdbc.internal.datasource.mbean;

/**
 * User: guillaume
 * Date: 22/10/13
 * Time: 15:58
 */
public interface DataSourceMXBean {

    String getDataSourceName();
    String getDatabaseUrl();
    String getDatabaseUsername();

    int getLoginTimeout();
    void setLoginTimeout(int timeout);

    int getPoolMinimumSize();
    void setPoolMinimumSize(int minimumSize);

    int getPoolMaximumSize();
    void setPoolMaximumSize(int maximumSize);

    int getPoolMaximumWaiters();
    void setPoolMaximumWaiters(int maximumWaiters);

    long getPoolWaiterTimeout();
    void setPoolWaiterTimeout(long timeout);

    int getJdbcCheckLevel();
    void setJdbcCheckLevel(int checkLevel);

    String getJdbcTestStatement();
    void setJdbcTestStatement(String statement);

    long getJdbcMaxAge();
    void setJdbcMaxAge(long maxAge);

    String getJdbcTransactionIsolation();
    void setJdbcTransactionIsolation(String transactionIsolation);

    int getJdbcPreparedStatementCacheSize();
    void setJdbcPreparedStatementCacheSize(int cacheSize);

    ConnectionStatisticsMXBean getGlobalConnectionStatistics();
    ConnectionStatisticsMXBean getLastSampleConnectionStatistics();

}
