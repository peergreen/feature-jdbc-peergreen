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
 * Time: 16:47
 */
public class EmptyPoolLifecycleListener implements PoolLifecycleListener {
    @Override
    public void connectionCreated() {

    }

    @Override
    public void connectionDestroyed() {

    }

    @Override
    public void connectionValidated() {

    }

    @Override
    public void waiterStartWaiting() {

    }

    @Override
    public void waiterStopWaiting(final long waitedTime, final boolean timedOut) {

    }

    @Override
    public void waiterRejectedTimeout() {

    }

    @Override
    public void waiterRejectedOverflow() {

    }

    @Override
    public void waiterRejectedFailure() {

    }

    @Override
    public void busyConnections(final int current) {

    }
}
