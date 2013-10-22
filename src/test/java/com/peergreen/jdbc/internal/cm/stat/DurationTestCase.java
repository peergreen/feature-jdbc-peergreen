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

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;

/**
 * User: guillaume
 * Date: 22/10/13
 * Time: 14:17
 */
public class DurationTestCase {
    @Test
    public void testInitialisation() throws Exception {
        Duration duration = new Duration();
        assertEquals(duration.getTotal(), 0);
        assertEquals(duration.getMinimum(), Long.MAX_VALUE);
        assertEquals(duration.getMaximum(), Long.MIN_VALUE);
        assertEquals(duration.getAverage(), 0d);
    }

    @Test
    public void testMinimum() throws Exception {
        Duration duration = new Duration();

        duration.update(100);
        assertEquals(duration.getMinimum(), 100);

        duration.update(10);
        assertEquals(duration.getMinimum(), 10);

        duration.update(50);
        assertEquals(duration.getMinimum(), 10);
    }

    @Test
    public void testMaximum() throws Exception {
        Duration duration = new Duration();

        duration.update(10);
        assertEquals(duration.getMaximum(), 10);

        duration.update(100);
        assertEquals(duration.getMaximum(), 100);

        duration.update(50);
        assertEquals(duration.getMaximum(), 100);
    }

    @Test
    public void testAverage() throws Exception {
        Duration duration = new Duration();

        duration.update(10);
        assertEquals(duration.getAverage(), 10d);

        duration.update(100);
        assertEquals(duration.getAverage(), 55d);

        duration.update(70);
        assertEquals(duration.getAverage(), 60d);
    }

    @Test
    public void testTotal() throws Exception {
        Duration duration = new Duration();

        duration.update(10);
        assertEquals(duration.getTotal(), 10);

        duration.update(100);
        assertEquals(duration.getTotal(), 110);

        duration.update(70);
        assertEquals(duration.getTotal(), 180);
    }

    @Test
    public void testTimeUnitIsMilliseconds() throws Exception {
        Duration duration = new Duration();
        assertEquals(duration.getUnit(), TimeUnit.MILLISECONDS);
    }
}
