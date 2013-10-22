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

import com.peergreen.jdbc.internal.cm.stat.DataSourceStatisticsListener;
import com.peergreen.jdbc.internal.datasource.mbean.ConnectionStatisticsMXBean;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

import static java.lang.String.format;

/**
 * User: guillaume
 * Date: 23/10/13
 * Time: 16:46
 */
public class ConnectionPoolStatisticsManagementBean implements ConnectionStatisticsMXBean {

    private final MBeanServer server;
    private final ObjectName name;
    private final DataSourceStatisticsListener statistics;

    public ConnectionPoolStatisticsManagementBean(String datasource, DataSourceStatisticsListener statistics) throws MalformedObjectNameException {
        this.statistics = statistics;
        server = ManagementFactory.getPlatformMBeanServer();
        name = new ObjectName(format("peergreen:type=DataSourceStatistics,name=%s", datasource));
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
    public long getMonitoringBeginning() {
        return statistics.getFrom();
    }

    @Override
    public long getMonitoringEnding() {
        return statistics.getTo();
    }

    @Override
    public long getWaitedTimeTotal() {
        return statistics.getWaitedTime().getTotal();
    }

    @Override
    public long getWaitedTimeMinimum() {
        return statistics.getWaitedTime().getMinimum();
    }

    @Override
    public long getWaitedTimeMaximum() {
        return statistics.getWaitedTime().getMaximum();
    }

    @Override
    public double getWaitedTimeAverage() {
        return statistics.getWaitedTime().getAverage();
    }

    @Override
    public long getNumberOfWaiters() {
        return statistics.getWaiters().getLatest();
    }

    @Override
    public long getNumberOfWaitersMinimum() {
        return statistics.getWaiters().getMinimum();
    }

    @Override
    public long getNumberOfWaitersMaximum() {
        return statistics.getWaiters().getMaximum();
    }

    @Override
    public long getNumberOfBusyConnections() {
        return statistics.getBusy().getValue();
    }

    @Override
    public long getNumberOfBusyConnectionsMinimum() {
        return statistics.getBusy().getMinimum();
    }

    @Override
    public long getNumberOfBusyConnectionsMaximum() {
        return statistics.getBusy().getMaximum();
    }

    @Override
    public long getNumberOfCreatedConnections() {
        return statistics.getCreatedConnections().getValue();
    }

    @Override
    public long getNumberOfDestroyedConnections() {
        return statistics.getDestroyedConnections().getValue();
    }

    @Override
    public long getNumberOfReusedConnections() {
        return statistics.getReuseOfConnections().getValue();
    }

    @Override
    public long getNumberOfRejectedConnectionsForTimeout() {
        return statistics.getTimeoutRejection().getValue();
    }

    @Override
    public long getNumberOfRejectedConnectionsForOverflow() {
        return statistics.getOverflowRejection().getValue();
    }

    @Override
    public long getNumberOfRejectedConnectionsForFailure() {
        return statistics.getFailureRejection().getValue();
    }

    @Override
    public long getNumberOfServedConnections() {
        return statistics.getServedConnections().getValue();
    }

    @Override
    public long getNumberOfCompletedTransactionalConnections() {
        return statistics.getCompletedConnections().getValue();
    }

    @Override
    public long getNumberOfConnectionsInTransaction() {
        return statistics.getInTransaction().getLatest();
    }

    @Override
    public long getNumberOfConnectionsInTransactionMinimum() {
        return statistics.getInTransaction().getMinimum();
    }

    @Override
    public long getNumberOfConnectionsInTransactionMaximum() {
        return statistics.getInTransaction().getMaximum();
    }

    @Override
    public double getNumberOfConnectionsReusedPerTransactionAverage() {
        return statistics.getPerTransactionConnections().getAverage();
    }

    @Override
    public long getNumberOfConnectionsReusedPerTransactionMaximum() {
        return statistics.getPerTransactionConnections().getMaximum();
    }
}
