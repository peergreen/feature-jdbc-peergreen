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
 * Time: 16:37
 */
public interface ConnectionStatisticsMXBean {

    // Measures time boundaries (begin / end)
    // --------------------------------
    long getMonitoringBeginning();
    long getMonitoringEnding();

    // Waited time
    // --------------------------------
    long getWaitedTimeTotal();
    long getWaitedTimeMinimum();
    long getWaitedTimeMaximum();
    double getWaitedTimeAverage();

    // Waiters
    // --------------------------------
    long getNumberOfWaiters();
    long getNumberOfWaitersMinimum();
    long getNumberOfWaitersMaximum();

    // Busy connections
    // --------------------------------
    long getNumberOfBusyConnections();
    long getNumberOfBusyConnectionsMinimum();
    long getNumberOfBusyConnectionsMaximum();

    // Connections
    // --------------------------------
    long getNumberOfCreatedConnections();
    long getNumberOfDestroyedConnections();
    long getNumberOfReusedConnections();
    long getNumberOfServedConnections();
    long getNumberOfCompletedTransactionalConnections();

    // Connections in transaction
    // --------------------------------
    long getNumberOfConnectionsInTransaction();
    long getNumberOfConnectionsInTransactionMinimum();
    long getNumberOfConnectionsInTransactionMaximum();

    // Connections rejection
    // --------------------------------
    long getNumberOfRejectedConnectionsForTimeout();
    long getNumberOfRejectedConnectionsForOverflow();
    long getNumberOfRejectedConnectionsForFailure();

    // Per Transaction connections
    // --------------------------------
    double getNumberOfConnectionsReusedPerTransactionAverage();
    long getNumberOfConnectionsReusedPerTransactionMaximum();
}
