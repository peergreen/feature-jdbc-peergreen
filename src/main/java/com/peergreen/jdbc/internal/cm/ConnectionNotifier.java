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

import com.peergreen.jdbc.internal.cm.statement.ReusablePreparedStatement;

import java.sql.SQLException;

/**
 * User: guillaume
 * Date: 04/10/13
 * Time: 14:11
 */
public interface ConnectionNotifier {
    /**
     * Notify a Close event on Connection.
     */
    void notifyClose();

    /**
     * Notify an Error event on Connection.
     * @param ex the given exception
     */
    void notifyError(SQLException ex);

    void notifyPsClose(IPreparedStatement preparedStatement);
}
