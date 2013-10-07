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

package com.peergreen.jdbc.internal.datasource.naming;

import javax.naming.Reference;
import javax.naming.StringRefAddr;

import static com.peergreen.jdbc.internal.datasource.naming.DataSourceObjectFactory.DATASOURCE_NAME_REF;

/**
* Dedicated JNDI {@link javax.naming.Reference} for a {@link javax.sql.DataSource}.
*/
public class DataSourceReference extends Reference {
    public DataSourceReference(final String classname, final String name) {
        super(classname, DataSourceObjectFactory.class.getName(), null);
        add(new StringRefAddr(DATASOURCE_NAME_REF, name));
    }
}
