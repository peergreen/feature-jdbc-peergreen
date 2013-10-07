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

/**
 * User: guillaume
 * Date: 07/10/13
 * Time: 12:20
 */
public enum TransactionIsolation {
    TRANSACTION_UNDEFINED(-1),
    TRANSACTION_NONE(Connection.TRANSACTION_NONE),
    TRANSACTION_SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE),
    TRANSACTION_READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
    TRANSACTION_READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
    TRANSACTION_REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ);

    private final int level;

    TransactionIsolation(final int isolationLevel) {
        level = isolationLevel;
    }

    public int level() {
        return level;
    }
}
