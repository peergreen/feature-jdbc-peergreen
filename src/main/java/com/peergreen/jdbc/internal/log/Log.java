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

package com.peergreen.jdbc.internal.log;

/**
 * Simple log API intended to delegate to a {@link java.util.logging.Logger}.
 */
public interface Log {

    Log create(String name);

    void fine(String message, Object... objects);

    void info(String message, Object... objects);

    void warn(String message, Object... objects);

    void error(String message, Object... objects);
}
