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

package com.peergreen.jdbc.internal;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.Driver;
import java.util.Properties;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * User: guillaume
 * Date: 16/09/13
 * Time: 12:37
 */
public class BasicDriverManagerDataSourceTestCase {

    @Mock
    private Driver driver;

    @Mock
    private Connection connection;

    @Captor
    private ArgumentCaptor<Properties> properties;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetConnectionWithoutCredentialsProvided() throws Exception {

        when(driver.connect(anyString(), any(Properties.class))).thenReturn(connection);

        BasicDriverManagerDataSource ds = new BasicDriverManagerDataSource(driver);
        ds.setUser("guillaume");
        ds.setPassword("s3cr3t");
        ds.setUrl("jdbc:test");

        assertEquals(ds.getConnection(), connection);
        verify(driver).connect(eq("jdbc:test"), properties.capture());
        assertEquals(properties.getValue().getProperty("user"), "guillaume");
        assertEquals(properties.getValue().getProperty("password"), "s3cr3t");
    }

    @Test
    public void testGetConnectionWithCredentialsProvidedOverrideDefaultCredentials() throws Exception {

        when(driver.connect(anyString(), any(Properties.class))).thenReturn(connection);

        BasicDriverManagerDataSource ds = new BasicDriverManagerDataSource(driver);
        ds.setUser("guillaume");
        ds.setPassword("s3cr3t");
        ds.setUrl("jdbc:test");

        assertEquals(ds.getConnection("florent", "super-s3cr3t"), connection);
        verify(driver).connect(eq("jdbc:test"), properties.capture());
        assertEquals(properties.getValue().getProperty("user"), "florent");
        assertEquals(properties.getValue().getProperty("password"), "super-s3cr3t");
    }
}
