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

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.osgi.service.jdbc.DataSourceFactory;

/**
 * Note: this component needs to be 'immediate', otherwise no real object instance is created
 * and service properties stay un-valued.
 */
@Component(immediate = true)
@Provides(
        // Mark this DataSourceFactory as ours
        properties = @StaticServiceProperty(
                name = "provider",
                type = "java.lang.String",
                value = "Peergreen",
                immutable = true)
)
public class DefaultDataSourceFactory implements DataSourceFactory {

    private final Driver driver;

    @ServiceProperty(name = OSGI_JDBC_DRIVER_CLASS)
    private String driverClass;

    @ServiceProperty(name = OSGI_JDBC_DRIVER_NAME)
    private String driverName;

    @ServiceProperty(name = OSGI_JDBC_DRIVER_VERSION)
    private String driverVersion;

    @ServiceProperty(name = "jdbc.driver.compliant")
    private boolean driverCompliance;

    public DefaultDataSourceFactory(@Property(mandatory = true, name = "driver") Driver driver) {
        this.driver = driver;
        this.driverClass = driver.getClass().getName();
        this.driverName = "N/A";
        this.driverVersion = driver.getMajorVersion() + "." + driver.getMinorVersion();
        this.driverCompliance = driver.jdbcCompliant();
    }

    @Override
    public DataSource createDataSource(final Properties props) throws SQLException {
        // Extract useful properties
        String url = props.getProperty(JDBC_URL);
        String username = props.getProperty(JDBC_USER);
        String password = props.getProperty(JDBC_PASSWORD);

        // Create the DataSource
        BasicDriverManagerDataSource ds = new BasicDriverManagerDataSource(driver);
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        return ds;
    }

    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource(final Properties props) throws SQLException {
        throw new SQLException("Not implemented yet");
    }

    @Override
    public XADataSource createXADataSource(final Properties props) throws SQLException {
        throw new SQLException("Not implemented yet");
    }

    @Override
    public Driver createDriver(final Properties props) throws SQLException {
        // TODO Maybe we should create (and try to configure) another Driver instance here ?
        return driver;
    }
}
