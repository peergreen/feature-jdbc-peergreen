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

import java.util.HashMap;
import java.util.Map;

/**
 * PartitionIncrement represents a counter that increment values given a provided key.
 */
public class PartitionIncrement {

    private Map<Object, Integer> counters = new HashMap<>();
    private int maximum = 0;

    public synchronized double getAverage() {
        double total = 0;
        for (Integer value : counters.values()) {
            total += value;
        }
        if (total == 0) {
            return 0;
        }
        return total / counters.size();
    }

    public long getMaximum() {
        return maximum;
    }

    public synchronized void increment(final Object key) {
        Integer i = counters.get(key);
        if (i == null) {
            i = 1;
        } else {
            i++;
        }
        counters.put(key, i);
        if (i > maximum) {
            maximum = i;
        }
    }

    public synchronized void forget(final Object key) {
        counters.remove(key);
    }
}
