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

package com.peergreen.jdbc.internal.datasource.deploy.processor.name;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * User: guillaume
 * Date: 16/10/13
 * Time: 21:38
 */
public class CleanerTestCase {
    @Test
    public void testCleanWithSuffix() throws Exception {
        Cleaner cleaner = new Cleaner();
        assertEquals(cleaner.clean("normal.datasource"), "normal");
    }

    @Test
    public void testCleanWithoutSuffix() throws Exception {
        Cleaner cleaner = new Cleaner();
        assertEquals(cleaner.clean("normal"), "normal");
    }

    @Test
    public void testCleanWithSlash() throws Exception {
        Cleaner cleaner = new Cleaner();
        assertEquals(cleaner.clean("toto/normal.datasource"), "normal");
    }

    @Test
    public void testCleanWithSlashs() throws Exception {
        Cleaner cleaner = new Cleaner();
        assertEquals(cleaner.clean("my/toto/normal.datasource"), "normal");
    }

    @Test
    public void testCleanWithUri() throws Exception {
        Cleaner cleaner = new Cleaner();
        assertEquals(cleaner.clean("my/toto/normal:test.datasource"), "normal_test");
    }
}
