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

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import static java.lang.String.format;

/**
 * Basic DataSource implementation that does not perform any pooling or instance control.
 * This is just a {@link Connection} factory based on the given {@link java.sql.Driver}.
 */
public class BasicDriverManagerDataSource implements DataSource {
    private final Driver driver;
    private String url;
    private String username;
    private String password;
    private int loginTimeout = 0;
    private final Properties properties;
    private PrintWriter logger;

    public BasicDriverManagerDataSource(final Driver driver) {
        // TODO this writer prints nowhere
        this(driver, new PrintWriter(new ByteArrayOutputStream()));
    }

    public BasicDriverManagerDataSource(final Driver driver, final PrintWriter logger) {
        this.driver = driver;
        this.properties = new Properties();
        this.logger = logger;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUser(final String username) {
        this.username = username;
        properties.setProperty("user", username);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
        properties.setProperty("password", password);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return driver.connect(url, properties);
    }

    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        Properties info = new Properties();
        info.setProperty("user", username);
        info.setProperty("password", password);
        return driver.connect(url, info);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return logger;
    }

    @Override
    public void setLogWriter(final PrintWriter out) throws SQLException {
        this.logger = out;
    }

    @Override
    public void setLoginTimeout(final int seconds) throws SQLException {
        this.loginTimeout = seconds;
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return loginTimeout;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        throw new SQLException(format("%s does not wrap another DataSource instance", getClass().getName()));
    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return false;
    }
}
