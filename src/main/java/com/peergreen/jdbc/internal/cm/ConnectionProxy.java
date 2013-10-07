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

package com.peergreen.jdbc.internal.cm;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Extends the SQL Connection interface with a method to get the physical SQL connection.
 * @author Florent BENOIT
 */
public interface ConnectionProxy extends Connection {

    /**
     * Gets the physical connection to the database.
     * @return physical connection to the database
     */
    Connection getConnection();

    /**
     * @return true if the connection to the database is closed or not.
     * @throws SQLException if a database access error occurs
     */
    boolean isPhysicallyClosed() throws SQLException;
}