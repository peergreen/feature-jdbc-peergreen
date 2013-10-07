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

package com.peergreen.jdbc.internal.cm.pool.internal.dm;

import com.peergreen.jdbc.internal.cm.pool.internal.NativeConnectionBuilder;
import com.peergreen.jdbc.internal.cm.pool.internal.UsernamePasswordInfo;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * User: guillaume
 * Date: 10/10/13
 * Time: 10:05
 */
public class DriverManagerNativeConnectionBuilder implements NativeConnectionBuilder {
    /**
     * Logger.
     */
    private static final Log logger = LogFactory.getLog(DriverManagerNativeConnectionBuilder.class);

    /**
     * Database URL to be used in DriverManager.
     */
    private final String url;

    public DriverManagerNativeConnectionBuilder(final String url) {
        this.url = url;
    }


    @Override
    public Connection build(final UsernamePasswordInfo info) throws SQLException {
        Connection connection;
        if (info.getUsername().length() == 0) {
            connection = DriverManager.getConnection(url);
            logger.debug("    * New Connection on {0}", url);
        } else {
            // Accept password of zero length.
            connection = DriverManager.getConnection(url, info.getUsername(), info.getPassword());
            logger.debug("    * New Connection on {0} for user {1}", url, info.getUsername());
        }
        return connection;
    }
}
