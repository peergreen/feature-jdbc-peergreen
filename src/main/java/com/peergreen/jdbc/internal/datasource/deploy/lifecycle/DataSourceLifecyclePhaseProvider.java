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

package com.peergreen.jdbc.internal.datasource.deploy.lifecycle;

import com.peergreen.deployment.DeploymentMode;
import com.peergreen.deployment.FacetLifeCyclePhaseProvider;
import com.peergreen.jdbc.internal.datasource.deploy.DataSourceInfo;

import java.util.Arrays;
import java.util.List;

public class DataSourceLifecyclePhaseProvider implements FacetLifeCyclePhaseProvider<DataSourceInfo> {

    private static final List<String> DEPLOY_PHASES     = Arrays.asList("ds-start");
    private static final List<String> UPDATE_PHASES     = Arrays.asList("ds-compute-diff", "ds-update");
    private static final List<String> UNDEPLOY_PHASES   = Arrays.asList("ds-stop");

    @Override
    public List<String> getLifeCyclePhases(DeploymentMode deploymentMode) {
        switch (deploymentMode) {
            case DEPLOY:
                return DEPLOY_PHASES;
            case UPDATE:
                return UPDATE_PHASES;
            case UNDEPLOY:
                return UNDEPLOY_PHASES;
            default:
                throw new IllegalStateException("Deployment mode '" + deploymentMode + "' not supported");
        }
    }



}
