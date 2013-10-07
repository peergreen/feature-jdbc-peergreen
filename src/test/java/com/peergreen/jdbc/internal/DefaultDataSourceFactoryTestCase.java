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

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.Bundle;
import org.osgi.service.jdbc.DataSourceFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;
import java.io.PrintWriter;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * User: guillaume
 * Date: 15/10/13
 * Time: 15:20
 */
public class DefaultDataSourceFactoryTestCase {
    @Mock
    private Driver driver;

    @Mock
    private Bundle bundle;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateConnectionPoolDataSource() throws Exception {

        doReturn(EmptyConnectionPoolDataSource.class)
                .when(bundle)
                .loadClass(EmptyConnectionPoolDataSource.class.getName());

        DefaultDataSourceFactory factory = new DefaultDataSourceFactory(driver);
        factory.setBundle(bundle);

        Properties props = new Properties();
        props.setProperty(DefaultDataSourceFactory.DATASOURCE_CLASSNAME, EmptyConnectionPoolDataSource.class.getName());
        props.setProperty(DataSourceFactory.JDBC_URL, "some+jdbc://url");
        props.setProperty("ignored", "should not fail");

        EmptyConnectionPoolDataSource dataSource = (EmptyConnectionPoolDataSource) factory.createConnectionPoolDataSource(props);
        assertEquals(dataSource.getUrl(), "some+jdbc://url");

    }

    public static class EmptyConnectionPoolDataSource implements ConnectionPoolDataSource {

        private String url;

        @Override
        public PooledConnection getPooledConnection() throws SQLException {
            return null;
        }

        @Override
        public PooledConnection getPooledConnection(final String user, final String password) throws SQLException {
            return null;
        }

        @Override
        public PrintWriter getLogWriter() throws SQLException {
            return null;
        }

        @Override
        public void setLogWriter(final PrintWriter out) throws SQLException {

        }

        @Override
        public void setLoginTimeout(final int seconds) throws SQLException {

        }

        @Override
        public int getLoginTimeout() throws SQLException {
            return 0;
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return null;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(final String url) {
            this.url = url;
        }
    }
}
