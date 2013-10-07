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

import com.peergreen.jdbc.internal.datasource.Constants;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Unbind;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * User: guillaume
 * Date: 15/10/13
 * Time: 16:58
 */
@Component
@Instantiate
@Provides(specifications = {DataSourceObjectFactory.class, ObjectFactory.class})
public class DataSourceObjectFactory implements ObjectFactory {

    public static final String DATASOURCE_NAME_REF = "datasource.name";
    private Map<String, DataSource> datasources = new HashMap<>();

    @Override
    public Object getObjectInstance(final Object obj,
                                    final Name name,
                                    final Context nameCtx,
                                    final Hashtable<?, ?> environment) throws Exception {
        if (obj instanceof Reference) {
            Reference reference = (Reference) obj;
            String jndiName = (String) reference.get(DATASOURCE_NAME_REF).getContent();
            return datasources.get(jndiName);
        }
        return null;
    }

    @Bind(aggregate = true, optional = true)
    public void bindDataSource(DataSource dataSource, Map<String, Object> properties) {
        String name = getJndiName(properties);
        datasources.put(name, dataSource);
    }

    @Unbind
    public void unbindDataSource(Map<String, Object> properties) {
        String name = getJndiName(properties);
        datasources.remove(name);
    }

    private String getJndiName(final Map<String, Object> properties) {
        return (String) properties.get(Constants.DATASOURCE_NAME);
    }
}
