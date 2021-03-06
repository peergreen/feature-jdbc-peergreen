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
 * Counter represents a counter that is incremented/decremented
 * when a new value is provided.
 */
public class Counter implements Updatable {
    private long latest;
    private long minimum = Long.MAX_VALUE;
    private long maximum = Long.MIN_VALUE;

    public Counter() {
        this(0);
    }

    public Counter(final long initial) {
        this.latest = initial;
    }

    public long getLatest() {
        return latest;
    }

    public long getMinimum() {
        return minimum;
    }

    public long getMaximum() {
        return maximum;
    }

    @Override
    public void update(final long value) {
        // increment or decrement counter
        latest += value;

        // Update minimum and maximum values
        if (latest < minimum) {
            minimum = latest;
        }
        if (latest > maximum) {
            maximum = latest;
        }
    }
}
