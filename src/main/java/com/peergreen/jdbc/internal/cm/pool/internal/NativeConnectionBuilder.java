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

package com.peergreen.jdbc.internal.cm.pool.internal;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * User: guillaume
 * Date: 11/10/13
 * Time: 10:13
 */
public interface NativeConnectionBuilder {
    Connection build(UsernamePasswordInfo info) throws SQLException;
}
