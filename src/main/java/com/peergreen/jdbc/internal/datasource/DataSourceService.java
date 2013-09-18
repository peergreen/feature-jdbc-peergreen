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

import static java.lang.String.format;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;
import javax.sql.DataSource;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.apache.felix.ipojo.annotations.Updated;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.service.jdbc.DataSourceFactory;

/**
 * User: guillaume
 * Date: 13/09/13
 * Time: 16:14
 */
@Component
@Provides(
        specifications = DataSource.class,
        properties = @StaticServiceProperty(name = "datasource.name", type = "java.lang.String", mandatory = true)
)
public class DataSourceService extends ForwardingDataSource {

    @Property(mandatory = true)
    private String driverClass;

    private String url;

    private String username;

    private String password;

    private final DataSourceFactory dataSourceFactory;

    private DataSource delegate;

    private Dictionary<String, Object> properties = new Hashtable<>();

    public DataSourceService(@Requires(filter = "(osgi.jdbc.driver.class=${driverClass})")
                             final DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
    }

    @Updated
    public void update(Dictionary<String, Object> configuration) {
        this.properties = configuration;
    }

    @Property(name = "url", mandatory = true)
    public void setUrl(final String url) {
        this.url = url;
    }

    @Property(name = "username", mandatory = true)
    public void setUsername(final String username) {
        this.username = username;
    }

    @Property(name = "password", mandatory = true)
    public void setPassword(final String password) {
        this.password = password;
    }

    @Validate
    public void start() throws SQLException {
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_URL, url);
        props.setProperty(DataSourceFactory.JDBC_USER, username);
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, password);

        // Collect additional properties
        // Transform them into String if not already done
        for (String key : Collections.list(properties.keys())) {
            Object value = properties.get(key);
            if (value != null) {
                props.setProperty(key, value.toString());
            }
        }

        delegate = dataSourceFactory.createDataSource(props);
    }

    @Invalidate
    public void stop() {
        delegate = null;
    }

    @Override
    protected DataSource delegate() {
        return delegate;
    }

    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        if (!isWrapperFor(iface)) {
            throw new SQLException(format("Class (interface) %s is not superclass of" +
                                          " (resp. implemented by) this DataSource type %s",
                                          iface.getName(),
                                          getClass().getName()));
        }
        if (iface.isAssignableFrom(getClass())) {
            return iface.cast(this);
        }
        return super.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        if (iface.isAssignableFrom(getClass())) {
            return true;
        }
        return super.isWrapperFor(iface);
    }
}
