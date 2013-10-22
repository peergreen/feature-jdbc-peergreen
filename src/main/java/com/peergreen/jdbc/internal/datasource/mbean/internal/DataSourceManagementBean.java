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

package com.peergreen.jdbc.internal.datasource.mbean.internal;

import com.peergreen.jdbc.internal.cm.TransactionIsolation;
import com.peergreen.jdbc.internal.datasource.DataSourceDataSource;
import com.peergreen.jdbc.internal.datasource.mbean.ConnectionStatisticsMXBean;
import com.peergreen.jdbc.internal.datasource.mbean.DataSourceMXBean;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.sql.SQLException;

import static java.lang.String.format;

/**
 * User: guillaume
 * Date: 22/10/13
 * Time: 16:52
 */
public class DataSourceManagementBean implements DataSourceMXBean {
    private final DataSourceDataSource delegate;
    private final ConnectionStatisticsMXBean statistics;
    private final MBeanServer server;
    private final ObjectName name;

    public DataSourceManagementBean(final DataSourceDataSource delegate, final ConnectionStatisticsMXBean statistics) throws MalformedObjectNameException {
        this.delegate = delegate;
        this.statistics = statistics;
        server = ManagementFactory.getPlatformMBeanServer();
        name = new ObjectName(format("peergreen:type=DataSource,name=%s", delegate.getDataSourceName()));
    }

    public void start() {
        try {
            server.registerMBean(this, name);
        } catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
            throw new IllegalStateException(format("Cannot register %s MBean", name));
        }
    }

    public void stop() {
        try {
            server.unregisterMBean(name);
        } catch (InstanceNotFoundException | MBeanRegistrationException e) {
            // Ignored
        }
    }

    @Override
    public String getDataSourceName() {
        return delegate.getDataSourceName();
    }

    @Override
    public String getDatabaseUrl() {
        return delegate.getUrl();
    }

    @Override
    public String getDatabaseUsername() {
        return delegate.getUsername();
    }

    @Override
    public int getLoginTimeout() {
        try {
            return delegate.getLoginTimeout();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setLoginTimeout(final int timeout) {
        try {
            delegate.setLoginTimeout(timeout);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getPoolMinimumSize() {
        return delegate.getPoolMin();
    }

    @Override
    public void setPoolMinimumSize(final int minimumSize) {
        delegate.setPoolMin(minimumSize);
    }

    @Override
    public int getPoolMaximumSize() {
        return delegate.getPoolMax();
    }

    @Override
    public void setPoolMaximumSize(final int maximumSize) {
        delegate.setPoolMax(maximumSize);
    }

    @Override
    public int getPoolMaximumWaiters() {
        return delegate.getMaxWaiters();
    }

    @Override
    public void setPoolMaximumWaiters(final int maximumWaiters) {
        delegate.setMaxWaiters(maximumWaiters);
    }

    @Override
    public long getPoolWaiterTimeout() {
        return delegate.getWaiterTimeout();
    }

    @Override
    public void setPoolWaiterTimeout(final long timeout) {
        delegate.setWaiterTimeout(timeout);
    }

    @Override
    public int getJdbcCheckLevel() {
        return delegate.getCheckLevel();
    }

    @Override
    public void setJdbcCheckLevel(final int checkLevel) {
        delegate.setCheckLevel(checkLevel);
    }

    @Override
    public String getJdbcTestStatement() {
        return delegate.getTestStatement();
    }

    @Override
    public void setJdbcTestStatement(final String statement) {
        delegate.setTestStatement(statement);
    }

    @Override
    public long getJdbcMaxAge() {
        return delegate.getMaxAge();
    }

    @Override
    public void setJdbcMaxAge(final long maxAge) {
        delegate.setMaxAge(maxAge);
    }

    @Override
    public String getJdbcTransactionIsolation() {
        return delegate.getTransactionIsolation().name();
    }

    @Override
    public void setJdbcTransactionIsolation(final String transactionIsolation) {
        delegate.setTransactionIsolation(TransactionIsolation.valueOf(transactionIsolation));
    }

    @Override
    public int getJdbcPreparedStatementCacheSize() {
        return delegate.getPreparedStatementCacheSize();
    }

    @Override
    public void setJdbcPreparedStatementCacheSize(final int cacheSize) {
        delegate.setPreparedStatementCacheSize(cacheSize);
    }

    @Override
    public ConnectionStatisticsMXBean getGlobalConnectionStatistics() {
        return statistics;
    }

    @Override
    public ConnectionStatisticsMXBean getLastSampleConnectionStatistics() {
        return null;
    }
}
