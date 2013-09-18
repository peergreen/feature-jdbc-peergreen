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

package com.peergreen.jdbc.internal.datasource;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.service.jdbc.DataSourceFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Properties;

/**
 * User: guillaume
 * Date: 16/09/13
 * Time: 14:06
 */
public class DataSourceServiceTestCase {

    @Mock
    private DataSourceFactory factory;

    @Captor
    private ArgumentCaptor<Properties> properties;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDataSourceIsCreatedAtStartup() throws Exception {
        DataSourceService component = new DataSourceService(factory);
        component.setUrl("jdbc:test");
        component.setUsername("guillaume");
        component.setPassword("s3cr3t");

        component.start();

        verify(factory).createDataSource(properties.capture());

        assertEquals(properties.getValue().getProperty(DataSourceFactory.JDBC_URL), "jdbc:test");
        assertEquals(properties.getValue().getProperty(DataSourceFactory.JDBC_USER), "guillaume");
        assertEquals(properties.getValue().getProperty(DataSourceFactory.JDBC_PASSWORD), "s3cr3t");
    }
}
