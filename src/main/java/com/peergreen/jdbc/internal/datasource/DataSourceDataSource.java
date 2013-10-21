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

import com.peergreen.jdbc.internal.cm.ConnectionManager;
import com.peergreen.jdbc.internal.cm.TransactionIsolation;
import com.peergreen.jdbc.internal.cm.pool.internal.ManagedConnectionFactory;
import com.peergreen.jdbc.internal.cm.pool.internal.ManagedConnectionPool;
import com.peergreen.jdbc.internal.cm.pool.internal.ds.DataSourceNativeConnectionBuilder;
import com.peergreen.jdbc.internal.datasource.naming.DataSourceReference;
import com.peergreen.jdbc.internal.log.FormattedLogger;
import com.peergreen.jdbc.internal.log.Log;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Updated;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.jndi.JNDIContextManager;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Logger;

import static com.peergreen.jdbc.internal.datasource.Constants.DATASOURCE_NAME;
import static java.lang.String.format;

/**
 * User: guillaume
 * Date: 13/09/13
 * Time: 16:14
 */
@Component
@Provides(specifications = DataSource.class)
public class DataSourceDataSource extends ForwardingDataSource {

    @Property(mandatory = true)
    private String driverClass;

    private String url;

    private String username;

    private String password;

    private Integer checkLevel;
    private Long maxAge;
    private Long maxOpenTime;
    private String testStatement;
    private TransactionIsolation transactionIsolation;

    private Integer preparedStatementCacheSize;
    private Integer poolMin;
    private Integer poolMax;
    private Integer maxWaiters;
    private Long waiterTimeout;

    private Integer loginTimeout;
    @ServiceProperty(name = DATASOURCE_NAME, mandatory = true)
    private String datasourceName;
    private Integer samplingPeriod;

    private boolean bind;

    private final DataSourceFactory dataSourceFactory;
    private final TransactionManager transactionManager;
    private final JNDIContextManager contextManager;

    private DataSource delegate;

    private Dictionary<String, Object> properties = new Hashtable<>();
    private ConnectionManager manager;
    private DataSourceNativeConnectionBuilder builder;
    private ManagedConnectionFactory factory;
    private ManagedConnectionPool pool;

    private Logger parentLogger;

    public DataSourceDataSource(@Requires(filter = "(osgi.jdbc.driver.class=${driverClass})")
                                final DataSourceFactory dataSourceFactory,
                                @Requires TransactionManager transactionManager,
                                @Requires JNDIContextManager contextManager) {
        this.dataSourceFactory = dataSourceFactory;
        this.transactionManager = transactionManager;
        this.contextManager = contextManager;
    }

    private String getDataSourceLoggerName() {
        // Expecting something like:
        // com.peergreen.jdbc.internal.datasource.DataSourceDataSource[jdbc/MyDataSource]
        return format("%s[%s]", getClass().getName(), datasourceName);
    }


    @Updated
    public void update(Dictionary<String, Object> configuration) {
        this.properties = configuration;
    }

    @Property(name = Constants.URL, mandatory = true)
    public void setUrl(final String url) {
        this.url = url;
    }

    @Property(name = Constants.USERNAME, mandatory = true)
    public void setUsername(final String username) {
        this.username = username;
    }

    @Property(name = Constants.PASSWORD, mandatory = true)
    public void setPassword(final String password) {
        this.password = password;
    }

    @Property(name = Constants.JNDI_BIND, value = "true")
    public void setBind(final boolean bind) {
        this.bind = bind;
    }

    @Property(name = Constants.JDBC_CHECK_LEVEL)
    public void setCheckLevel(final Integer checkLevel) {
        this.checkLevel = checkLevel;
        if (factory != null) {
            factory.setCheckLevel(checkLevel);
        }
    }

    @Property(name = Constants.JDBC_MAX_AGE)
    public void setMaxAge(final Long maxAge) {
        this.maxAge = maxAge;
        if (factory != null) {
            factory.setMaxAge(maxAge);
        }
    }

    @Property(name = Constants.JDBC_MAX_OPENTIME)
    public void setMaxOpenTime(final Long maxOpenTime) {
        this.maxOpenTime = maxOpenTime;
        if (factory != null) {
            factory.setMaxOpenTime(maxOpenTime);
        }
    }

    @Property(name = Constants.JDBC_TEST_STATEMENT)
    public void setTestStatement(final String testStatement) {
        this.testStatement = testStatement;
        if (factory != null) {
            factory.setTestStatement(testStatement);
        }
    }

    @Property(name = Constants.JDBC_TRANSACTION_ISOLATION)
    public void setTransactionIsolation(final TransactionIsolation transactionIsolation) {
        this.transactionIsolation = transactionIsolation;
        if (factory != null) {
            factory.setTransactionIsolation(transactionIsolation);
        }
    }

    @Property(name = Constants.JDBC_PREPAREDSTATEMENT_CACHESIZE)
    public void setPreparedStatementCacheSize(final Integer preparedStatementCacheSize) {
        this.preparedStatementCacheSize = preparedStatementCacheSize;
        if (pool != null) {
            pool.setPreparedStatementCacheSize(preparedStatementCacheSize);
        }
    }

    @Property(name = Constants.POOL_MIN)
    public void setPoolMin(final Integer poolMin) {
        this.poolMin = poolMin;
        if (pool != null) {
            pool.setPoolMin(poolMin);
        }
    }

    @Property(name = Constants.POOL_MAX)
    public void setPoolMax(final Integer poolMax) {
        this.poolMax = poolMax;
        if (pool != null) {
            pool.setPoolMax(poolMax);
        }
    }

    @Property(name = Constants.POOL_WAITERS_MAX)
    public void setMaxWaiters(final Integer maxWaiters) {
        this.maxWaiters = maxWaiters;
        if (pool != null) {
            pool.setMaxWaiters(maxWaiters);
        }
    }

    @Property(name = Constants.POOL_WAITERS_TIMEOUT)
    public void setWaiterTimeout(final Long waiterTimeout) {
        this.waiterTimeout = waiterTimeout;
        if (pool != null) {
            pool.setWaiterTimeout(waiterTimeout);
        }
    }

    @Property(name = Constants.LOGIN_TIMEOUT)
    public void setLoginTimeout(final Integer loginTimeout) {
        this.loginTimeout = loginTimeout;
        if (builder != null) {
            builder.setLoginTimeout(loginTimeout);
        }
    }

    public void setDatasourceName(final String datasourceName) {
        this.datasourceName = datasourceName;
    }

    @Property(name = Constants.SAMPLING_PERIOD)
    public void setSamplingPeriod(final Integer samplingPeriod) {
        this.samplingPeriod = samplingPeriod;
    }

    @Validate
    public void start() throws SQLException {

        parentLogger = Logger.getLogger(getDataSourceLoggerName());

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

        manager = new ConnectionManager(getConnectionManagerLogger(), transactionManager);
        builder = new DataSourceNativeConnectionBuilder(getConnectionBuilderLogger(), delegate);
        factory = new ManagedConnectionFactory(getItemFactoryLogger(), builder, manager);
        pool = new ManagedConnectionPool(getPoolLogger(), factory);
        manager.setPool(pool);

        if (checkLevel != null) {
            factory.setCheckLevel(checkLevel);
        }

        if (maxAge != null) {
            factory.setMaxAge(maxAge);
        }
        if (maxOpenTime != null) {
            factory.setMaxOpenTime(maxOpenTime);
        }
        if (testStatement != null) {
            factory.setTestStatement(testStatement);
        }
        if (transactionIsolation != null) {
            factory.setTransactionIsolation(transactionIsolation);
        }

        if (loginTimeout != null) {
            builder.setLoginTimeout(loginTimeout);
        }

        pool.setUserName(username);
        pool.setPassword(password);

        if (preparedStatementCacheSize != null) {
            pool.setPreparedStatementCacheSize(preparedStatementCacheSize);
        }
        if (poolMax != null) {
            pool.setPoolMax(poolMax);
        }
        if (poolMin != null) {
            pool.setPoolMin(poolMin);
        }
        if (maxWaiters != null) {
            pool.setMaxWaiters(maxWaiters);
        }
        if (waiterTimeout != null) {
            pool.setWaiterTimeout(waiterTimeout);
        }

        manager.setDatasourceName(datasourceName);
        //manager.setSamplingPeriod();

        pool.start();

        // Only perform JNDI binding if requested (by default)
        if (bind) {
            try {
                Context context = contextManager.newInitialContext();
                context.rebind(datasourceName, new DataSourceReference(DataSourceDataSource.class.getName(), datasourceName));
                context.close();
            } catch (NamingException e) {
                throw new SQLException(format("Cannot rebind DataSource in %s", datasourceName), e);
            }
        }

    }

    private Log getConnectionManagerLogger() {
        return new FormattedLogger(Logger.getLogger(parentLogger.getName() + ".ConnectionManager"));
    }

    private Log getConnectionBuilderLogger() {
        return new FormattedLogger(Logger.getLogger(parentLogger.getName() + ".ConnectionBuilder"));
    }

    private Log getItemFactoryLogger() {
        return new FormattedLogger(Logger.getLogger(parentLogger.getName() + ".ItemFactory"));
    }

    private Log getPoolLogger() {
        return new FormattedLogger(Logger.getLogger(parentLogger.getName() + ".Pool"));
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return parentLogger;
    }

    @Invalidate
    public void stop() {
        if (bind) {
            try {
                Context context = contextManager.newInitialContext();
                context.unbind(datasourceName);
                context.close();
            } catch (NamingException e) {
                // Ignored
            }
        }
        pool.stop();
        delegate = null;
    }

    @Override
    protected DataSource delegate() {
        return delegate;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return manager.getConnection();
    }

    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        return manager.getConnection(username, password);
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
