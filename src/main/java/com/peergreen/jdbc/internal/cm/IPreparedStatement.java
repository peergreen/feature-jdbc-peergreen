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

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Extends the SQL PreparedStatement interface with other methods.
 * @author Florent BENOIT
 */
public interface IPreparedStatement extends PreparedStatement {

    /**
     * @return true if this statement has been closed, else false.
     */
    boolean isClosed();

    /**
     * Reuses this statement so reset properties.
     * @throws SQLException if reset fails
     */
    void reuse() throws SQLException;

    /**
     * Physically close this Statement.
     */
    void forget();

    /**
     * Force a close on the Prepared Statement. Usually, it's the caller that did
     * not close it explicitly
     * @return true if it was open
     */
    boolean forceClose();

    String getSql();
}