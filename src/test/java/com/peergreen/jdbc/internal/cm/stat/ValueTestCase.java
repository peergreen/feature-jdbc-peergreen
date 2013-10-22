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
 * Time: 15:28
 */
public class ValueTestCase {
    @Test
    public void testValue() throws Exception {
        Value value = new Value();

        value.update(10);
        assertEquals(value.getValue(), 10);

        value.update(100);
        assertEquals(value.getValue(), 100);

        value.update(5);
        assertEquals(value.getValue(), 5);
    }

    @Test
    public void testMinimum() throws Exception {
        Value value = new Value();

        value.update(10);
        assertEquals(value.getMinimum(), 10);

        value.update(100);
        assertEquals(value.getMinimum(), 10);

        value.update(5);
        assertEquals(value.getMinimum(), 5);
    }

    @Test
    public void testMaximum() throws Exception {
        Value value = new Value();

        value.update(10);
        assertEquals(value.getMaximum(), 10);

        value.update(100);
        assertEquals(value.getMaximum(), 100);

        value.update(5);
        assertEquals(value.getMaximum(), 100);
    }
}
