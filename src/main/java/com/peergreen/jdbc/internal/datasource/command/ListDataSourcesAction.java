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

package com.peergreen.jdbc.internal.datasource.command;

import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import org.apache.felix.gogo.commands.Action;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.HandlerDeclaration;
import org.apache.felix.service.command.CommandSession;
import org.fusesource.jansi.Ansi;
import org.osgi.framework.ServiceReference;

import com.peergreen.jdbc.internal.datasource.Constants;

/**
 * User: guillaume
 * Date: 15/03/13
 * Time: 22:03
 */
@Component
@Command(name = "list-datasources",
        scope = "jdbc",
        description = "List registered DataSources.")
@HandlerDeclaration("<sh:command xmlns:sh='org.ow2.shelbie'/>")
public class ListDataSourcesAction implements Action {

    private List<ServiceReference<?>> references = new ArrayList<>();

    @Override
    public Object execute(CommandSession session) throws Exception {
        Ansi buffer = Ansi.ansi();
        buffer.render("%d registered DataSources:", references.size());
        for (ServiceReference<?> reference : references) {
            Object name = reference.getProperty(Constants.DATASOURCE_NAME);
            if (name != null) {
                buffer.newline();
                buffer.render(" * %s", name);
            }
        }

        return buffer.toString();
    }

    @Bind(aggregate = true, optional = true)
    public void bindDataSource(DataSource datasource, ServiceReference<?> reference) {
        references.add(reference);
    }
}
