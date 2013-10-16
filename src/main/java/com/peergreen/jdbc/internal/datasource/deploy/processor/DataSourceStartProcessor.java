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

package com.peergreen.jdbc.internal.datasource.deploy.processor;

import com.peergreen.deployment.ProcessorContext;
import com.peergreen.deployment.ProcessorException;
import com.peergreen.deployment.processor.Phase;
import com.peergreen.deployment.processor.Processor;
import com.peergreen.jdbc.internal.datasource.deploy.DataSourceInfo;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.InstanceStateListener;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.apache.felix.ipojo.annotations.Requires;

import java.util.concurrent.CountDownLatch;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;

@Processor
@Phase("ds-start")
public class DataSourceStartProcessor {

    public static final int ONE = 1;
    private final Factory datasource;

    public DataSourceStartProcessor(@Requires(from = "com.peergreen.jdbc.internal.datasource.DataSourceDataSource") Factory datasource) {
        this.datasource = datasource;
    }

    public void handle(DataSourceInfo info, ProcessorContext context) throws ProcessorException {
        ComponentInstance instance = null;
        try {
            instance = datasource.createComponentInstance(info.getProperties());
        } catch (UnacceptableConfiguration | MissingHandlerException | ConfigurationException e) {
            throw new ProcessorException(format("%s DataSource creation failed", info.getName()), e);
        }

        // Associate instance and info for future usage
        info.setInstance(instance);

        //
        switch (instance.getState()) {
            case ComponentInstance.STOPPED:
            case ComponentInstance.DISPOSED:
                // Should not happen (Exception normally thrown earlier)
                throw new ProcessorException(format("Cannot build DataSource instance named '%s' (see logs for details)", info.getName()), new Exception());
            case ComponentInstance.INVALID:
                waitForInstance(info, instance);
            default:
                // nothing to do, instance is already valid
        }
    }

    private void waitForInstance(final DataSourceInfo info, final ComponentInstance instance) throws ProcessorException {

        // Instance is not valid at this moment, but nothing prevent it to become valid in the near future
        // Place a listener to be notified when its state will change
        final CountDownLatch latch = new CountDownLatch(1);
        InstanceStateListener listener = new InstanceStateListener() {
            @Override
            public void stateChanged(final ComponentInstance instance, final int newState) {
                if (newState == ComponentInstance.VALID) {
                    // unlock latch
                    latch.countDown();
                }
            }
        };

        instance.addInstanceStateListener(listener);

        try {
            // Wait for the instance to become valid
            if (!latch.await(ONE, SECONDS)) {
                // Exited on timeout expiration
                // That means the instance is still not valid now
                throw new ProcessorException(format("Waited '%s' DataSource to activate for too long, consider it lost.", info.getName()), new Exception());
            } // else, instance is now valid, continue

            // Remove the listener
            instance.removeInstanceStateListener(listener);
        } catch (InterruptedException e) {
            throw new ProcessorException(format("Interrupted during '%s' DataSource wait for activation.", info.getName()), new Exception());
        }
    }
}
