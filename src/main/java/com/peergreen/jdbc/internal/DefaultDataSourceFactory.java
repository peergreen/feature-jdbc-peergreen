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

import com.peergreen.jdbc.internal.cm.bean.Bean;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.osgi.framework.Bundle;
import org.osgi.service.jdbc.DataSourceFactory;

import javax.sql.CommonDataSource;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import static java.lang.String.format;

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

    public static final String DATASOURCE_CLASSNAME = "datasource.classname";
    private final Driver driver;

    @ServiceProperty(name = OSGI_JDBC_DRIVER_CLASS)
    private String driverClass;

    @ServiceProperty(name = OSGI_JDBC_DRIVER_NAME)
    private String driverName;

    @ServiceProperty(name = OSGI_JDBC_DRIVER_VERSION)
    private String driverVersion;

    @ServiceProperty(name = "jdbc.driver.compliant")
    private boolean driverCompliance;

    /**
     * Bundle containing the Driver declaration.
     */
    private Bundle bundle;

    public DefaultDataSourceFactory(@Property(mandatory = true, name = "driver") Driver driver) {
        this.driver = driver;
        this.driverClass = driver.getClass().getName();
        this.driverName = "N/A";
        this.driverVersion = driver.getMajorVersion() + "." + driver.getMinorVersion();
        this.driverCompliance = driver.jdbcCompliant();
    }

    @Property(name = "bundle", mandatory = true)
    public void setBundle(final Bundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public DataSource createDataSource(final Properties props) throws SQLException {

        // Extract DataSource classname to be instantiated
        String classname = props.getProperty(DATASOURCE_CLASSNAME);
        if (classname == null) {
            return createDriverBasedDataSource(props);
        }

        // Create the datasource instance
        return createDataSourceInstance(classname, props, DataSource.class);
    }

    private DataSource createDriverBasedDataSource(final Properties props) throws SQLException {
        try {
            BasicDriverManagerDataSource ds = new BasicDriverManagerDataSource(driver);
            configure(ds, props);
            return ds;
        } catch (Exception e) {
            throw new SQLException(format("Cannot build a Driver based DataSource instance for Driver %s", driverClass), e);
        }
    }

    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource(final Properties props) throws SQLException {

        // Extract DataSource classname to be instantiated
        String classname = props.getProperty(DATASOURCE_CLASSNAME);
        if (classname == null) {
            throw new SQLException(format("Cannot create ConnectionPoolDataSource instance from %s, missing property '%s'", this, DATASOURCE_CLASSNAME));
        }

        // Create the datasource instance
        return createDataSourceInstance(classname, props, ConnectionPoolDataSource.class);
    }

    private <T extends CommonDataSource> T createDataSourceInstance(final String classname, final Properties props, final Class<T> expected) throws SQLException {
        try {
            Class<? extends T> type = bundle.loadClass(classname).asSubclass(expected);
            T datasource = type.newInstance();
            configure(datasource, props);
            return expected.cast(datasource);
        } catch (ClassCastException e) {
            throw new SQLException(format("%s is not implementing %s", classname, expected.getName()), e);
        } catch (Exception e) {
            throw new SQLException(format("Cannot create/configure %s instance", classname), e);
        }
    }

    private void configure(final Object source, final Properties props) throws Exception {
        Bean bean = new Bean(source);
        bean.configure(props);
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultDataSourceFactory{");
        sb.append("driverClass='").append(driverClass).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
