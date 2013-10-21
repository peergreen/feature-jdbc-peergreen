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
import com.peergreen.jdbc.internal.log.Log;

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
    private final Log logger;

    /**
     * Database URL to be used in DriverManager.
     */
    private final String url;

    public DriverManagerNativeConnectionBuilder(final Log logger, final String url) {
        this.logger = logger;
        this.url = url;
    }


    @Override
    public Connection build(final UsernamePasswordInfo info) throws SQLException {
        Connection connection;
        if (info.getUsername().length() == 0) {
            connection = DriverManager.getConnection(url);
            logger.fine("    * New Connection on %s", url);
        } else {
            // Accept password of zero length.
            connection = DriverManager.getConnection(url, info.getUsername(), info.getPassword());
            logger.fine("    * New Connection on %s for user %s", url, info.getUsername());
        }
        return connection;
    }
}
