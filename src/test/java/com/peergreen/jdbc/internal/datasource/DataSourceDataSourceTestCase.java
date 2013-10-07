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

import com.peergreen.jdbc.internal.datasource.naming.DataSourceReference;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.jndi.JNDIContextManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.naming.Context;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import java.sql.Connection;
import java.util.Properties;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * User: guillaume
 * Date: 16/09/13
 * Time: 14:06
 */
public class DataSourceDataSourceTestCase {

    @Mock
    private DataSourceFactory factory;

    @Mock
    private TransactionManager transactionManager;

    @Mock
    private JNDIContextManager contextManager;

    @Mock
    private Context context;

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Captor
    private ArgumentCaptor<Properties> properties;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDataSourceIsCreatedAtStartup() throws Exception {
        DataSourceDataSource component = new DataSourceDataSource(factory, transactionManager, contextManager);
        component.setUrl("jdbc:test");
        component.setUsername("guillaume");
        component.setPassword("s3cr3t");

        component.start();

        verify(factory).createDataSource(properties.capture());

        assertEquals(properties.getValue().getProperty(DataSourceFactory.JDBC_URL), "jdbc:test");
        assertEquals(properties.getValue().getProperty(DataSourceFactory.JDBC_USER), "guillaume");
        assertEquals(properties.getValue().getProperty(DataSourceFactory.JDBC_PASSWORD), "s3cr3t");
    }

    @Test
    public void testDataSourceGetConnection() throws Exception {

        when(factory.createDataSource(any(Properties.class))).thenReturn(dataSource);
        when(dataSource.getConnection("guillaume", "s3cr3t")).thenReturn(connection);

        DataSourceDataSource component = new DataSourceDataSource(factory, transactionManager, contextManager);
        component.setUrl("jdbc:test");
        component.setUsername("guillaume");
        component.setPassword("s3cr3t");

        component.start();

        assertNotNull(component.getConnection());

    }

    @Test
    public void testDataSourceGetConnectionWithUserInfo() throws Exception {

        when(factory.createDataSource(any(Properties.class))).thenReturn(dataSource);
        when(dataSource.getConnection("toto", "tata")).thenReturn(connection);

        DataSourceDataSource component = new DataSourceDataSource(factory, transactionManager, contextManager);
        component.setUrl("jdbc:test");
        component.setUsername("guillaume");
        component.setPassword("s3cr3t");

        component.start();

        assertNotNull(component.getConnection("toto", "tata"));

    }

    @Test
    public void testDataSourceIsBoundInJndi() throws Exception {
        when(factory.createDataSource(any(Properties.class))).thenReturn(dataSource);
        when(contextManager.newInitialContext()).thenReturn(context);

        DataSourceDataSource component = new DataSourceDataSource(factory, transactionManager, contextManager);
        component.setUrl("jdbc:test");
        component.setUsername("guillaume");
        component.setPassword("s3cr3t");
        component.setBind(true);
        component.setDatasourceName("jdbc/Datasource");

        component.start();
        verify(context).rebind(eq("jdbc/Datasource"), any(DataSourceReference.class));

    }
}
