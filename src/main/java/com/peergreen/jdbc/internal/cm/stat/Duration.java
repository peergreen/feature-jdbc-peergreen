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
 * Time: 16:43
 */
public class Duration implements StatVisitor {
    private final String name;
    private long values = 0;
    private long total = 0;
    private long minimum = Long.MAX_VALUE;
    private long maximum = Long.MIN_VALUE;
    private double average = 0;

    public Duration(final String name) {
        this.name = name;
    }

    public void visit(String name, long value) {
        if (this.name.equals(name)) {
            total += value;
            average = total / ++values;
            if (value < minimum) {
                minimum = value;
            }
            if (value > maximum) {
                maximum = value;
            }
        }
    }

    public String getName() {
        return name;
    }

    public long getTotal() {
        return total;
    }

    public long getMinimum() {
        return minimum;
    }

    public long getMaximum() {
        return maximum;
    }

    public double getAverage() {
        return average;
    }
}
