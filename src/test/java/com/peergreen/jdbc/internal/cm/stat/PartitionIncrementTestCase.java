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
 * Date: 24/10/13
 * Time: 13:58
 */
public class PartitionIncrementTestCase {
    @Test
    public void testAverage() throws Exception {
        PartitionIncrement partition = new PartitionIncrement();

        partition.increment("guillaume");
        assertEquals(partition.getAverage(), 1d);

        partition.increment("florent");
        assertEquals(partition.getAverage(), 1d);

        partition.increment("guillaume");
        assertEquals(partition.getAverage(), 1.5d);

        partition.forget("guillaume");
        assertEquals(partition.getAverage(), 1d);
    }

    @Test
    public void testMaximum() throws Exception {
        PartitionIncrement partition = new PartitionIncrement();

        partition.increment("guillaume");
        assertEquals(partition.getMaximum(), 1);

        partition.increment("florent");
        assertEquals(partition.getMaximum(), 1);

        partition.increment("guillaume");
        assertEquals(partition.getMaximum(), 2);

        partition.forget("guillaume");
        assertEquals(partition.getMaximum(), 2);
    }
}
