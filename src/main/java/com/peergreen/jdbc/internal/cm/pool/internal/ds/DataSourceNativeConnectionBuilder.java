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

import com.peergreen.jdbc.internal.cm.pool.internal.NativeConnectionBuilder;
import com.peergreen.jdbc.internal.cm.pool.internal.UsernamePasswordInfo;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * User: guillaume
 * Date: 10/10/13
 * Time: 10:05
 */
public class DataSourceNativeConnectionBuilder implements NativeConnectionBuilder {

    /**
     * Logger.
     */
    private static final Log logger = LogFactory.getLog(DataSourceNativeConnectionBuilder.class);

    /**
     * Factory for native connection.
     */
    private final DataSource source;


    public DataSourceNativeConnectionBuilder(final DataSource source) {
        this.source = source;
    }

    public void setLoginTimeout(int timeout) {
        try {
            source.setLoginTimeout(timeout);
        } catch (SQLException e) {
            logger.warn("Cannot set login timeout to %d. It will be ignored", timeout, e);
        }
    }

    @Override
    public Connection build(final UsernamePasswordInfo info) throws SQLException {
        Connection connection;
        if (info.getUsername().length() == 0) {
            connection = source.getConnection();
        } else {
            // Accept password of zero length.
            connection = source.getConnection(info.getUsername(), info.getPassword());
        }
        return connection;
    }
}
