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

package com.peergreen.jdbc.internal.cm.stat;

/**
 * User: guillaume
 * Date: 08/10/13
 * Time: 16:48
 */
public class PoolStatistics implements StatVisitor {

    private Duration waitedTime = new Duration("waitedTime");
    private Counter busy = new Counter("busy");
    private Counter waiters = new Counter("waiters");
    private final long from;
    private long to;

    public PoolStatistics() {
        this(System.currentTimeMillis());
    }

    public PoolStatistics(final long from) {
        this.from = from;
    }

    public Duration getWaitedTime() {
        return waitedTime;
    }

    public Counter getBusy() {
        return busy;
    }

    public Counter getWaiters() {
        return waiters;
    }

    public long getFrom() {
        return from;
    }

    public long getTo() {
        return to;
    }

    @Override
    public void visit(final String name, final long value) {
        // Update counters
        waitedTime.visit(name, value);
        busy.visit(name, value);
        waiters.visit(name, value);

        // Update top boundary
        to = System.currentTimeMillis();
    }
}
