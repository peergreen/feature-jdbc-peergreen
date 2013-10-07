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

package com.peergreen.jdbc.internal.cm.pool.internal.ds;

import com.peergreen.jdbc.internal.cm.pool.internal.UsernamePasswordInfo;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.sql.DataSource;

import java.sql.Connection;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * User: guillaume
 * Date: 11/10/13
 * Time: 11:31
 */
public class DataSourceNativeConnectionBuilderTestCase {


    @Mock
    private DataSource source;
    @Mock
    private Connection connection;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testBuildEmptyPassword() throws Exception {
        when(source.getConnection()).thenReturn(connection);
        DataSourceNativeConnectionBuilder builder = new DataSourceNativeConnectionBuilder(source);
        assertEquals(builder.build(new UsernamePasswordInfo("", "")), connection);
    }

    @Test
    public void testBuild() throws Exception {
        when(source.getConnection("guillaume", "secret")).thenReturn(connection);
        DataSourceNativeConnectionBuilder builder = new DataSourceNativeConnectionBuilder(source);
        assertEquals(builder.build(new UsernamePasswordInfo("guillaume", "secret")), connection);
    }

    @Test
    public void testSetLoginTimeoutIsPropagated() throws Exception {
        DataSourceNativeConnectionBuilder builder = new DataSourceNativeConnectionBuilder(source);
        builder.setLoginTimeout(42);
        verify(source).setLoginTimeout(42);
    }
}
