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
 * Increment represents a counter that is only incremented when a new value is provided.
 */
public class Increment implements Updatable {
    private long value;

    public Increment() {
        this(0);
    }

    public Increment(final long initial) {
        this.value = initial;
    }

    public long getValue() {
        return value;
    }

    @Override
    public void update(final long value) {
        // increment counter
        this.value++;
    }
}
