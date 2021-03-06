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

package com.peergreen.jdbc.internal.cm.pool;

/**
 * User: guillaume
 * Date: 21/10/13
 * Time: 16:44
 */
public interface PoolLifecycleListener {
    void connectionCreated();
    void connectionDestroyed();
    void connectionValidated();

    void waiterStartWaiting();
    void waiterStopWaiting(long waitedTime, boolean timedOut);
    void waiterRejectedTimeout();
    void waiterRejectedOverflow();
    void waiterRejectedFailure();

    void busyConnections(int current);
}
