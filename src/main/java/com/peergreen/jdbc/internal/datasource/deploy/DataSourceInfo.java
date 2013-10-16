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

package com.peergreen.jdbc.internal.datasource.deploy;

import com.peergreen.jdbc.internal.datasource.Constants;
import org.apache.felix.ipojo.ComponentInstance;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * User: guillaume
 * Date: 16/10/13
 * Time: 16:11
 */
public class DataSourceInfo {
    private final String name;
    private final Dictionary<String, String> properties = new Hashtable<>();
    private ComponentInstance instance;

    public DataSourceInfo(final String name) {
        this.name = name;
        properties.put(Constants.DATASOURCE_NAME, name);
    }

    public String getName() {
        return name;
    }

    public Dictionary<String, String> getProperties() {
        return properties;
    }

    public ComponentInstance getInstance() {
        return instance;
    }

    public void setInstance(final ComponentInstance instance) {
        this.instance = instance;
    }
}
