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

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * User: guillaume
 * Date: 22/10/13
 * Time: 14:26
 */
public class CounterTestCase {

    @Test
    public void testInitialisation() throws Exception {
        Counter counter = new Counter();
        assertEquals(counter.getLatest(), 0);
        assertEquals(counter.getMinimum(), Long.MAX_VALUE);
        assertEquals(counter.getMaximum(), Long.MIN_VALUE);
    }

    @Test
    public void testLatest() throws Exception {
        Counter counter = new Counter();

        assertEquals(counter.getLatest(), 0);
        counter.update(10);
        assertEquals(counter.getLatest(), 10);
        counter.update(10);
        assertEquals(counter.getLatest(), 20);
        counter.update(-15);
        assertEquals(counter.getLatest(), 5);
    }

    @Test
    public void testMinimum() throws Exception {
        Counter counter = new Counter();

        counter.update(10);
        assertEquals(counter.getMinimum(), 10);
        counter.update(20);
        assertEquals(counter.getMinimum(), 10);
        counter.update(-25);
        assertEquals(counter.getMinimum(), 5);
    }

    @Test
    public void testMaximum() throws Exception {
        Counter counter = new Counter();

        counter.update(10);
        assertEquals(counter.getMaximum(), 10);
        counter.update(20);
        assertEquals(counter.getMaximum(), 30);
        counter.update(-25);
        assertEquals(counter.getMaximum(), 30);
    }
}
