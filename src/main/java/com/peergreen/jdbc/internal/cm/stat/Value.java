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
public class Value implements Updatable {
    private long value;
    private long minimum = Long.MAX_VALUE;
    private long maximum = Long.MIN_VALUE;

    public void update(long value) {
        this.value = value;
        if (value < minimum) {
            minimum = value;
        }
        if (value > maximum) {
            maximum = value;
        }
    }

    public long getValue() {
        return value;
    }

    public long getMinimum() {
        return minimum;
    }

    public long getMaximum() {
        return maximum;
    }
}
