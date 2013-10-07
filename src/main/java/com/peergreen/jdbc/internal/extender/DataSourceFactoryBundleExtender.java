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

package com.peergreen.jdbc.internal.extender;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

/**
 * User: guillaume
 * Date: 13/09/13
 * Time: 12:10
 */
@Component
@Instantiate
public class DataSourceFactoryBundleExtender implements BundleTrackerCustomizer<List<ComponentInstance>> {

    public static final String SERVICES_DRIVER_NAME = "/META-INF/services/java.sql.Driver";
    private BundleTracker<?> bundleTracker;

    private final Factory factory;

    public DataSourceFactoryBundleExtender(BundleContext bundleContext,
                                           @Requires(from = "com.peergreen.jdbc.internal.DefaultDataSourceFactory") Factory factory) {
        this.bundleTracker = new BundleTracker<>(bundleContext, Bundle.ACTIVE, this);
        this.factory = factory;
    }

    @Validate
    public void start() {
        bundleTracker.open();
    }

    @Invalidate
    public void stop() {
        bundleTracker.close();
    }

    @Override
    public List<ComponentInstance> addingBundle(final Bundle bundle, final BundleEvent event) {
        URL driverUrl = bundle.getResource(SERVICES_DRIVER_NAME);
        if (driverUrl != null) {
            List<String> drivers = getDriverNames(driverUrl);
            if (drivers.isEmpty()) {
                // Not interested
                return null;
            }

            return createDataSourceFactories(bundle, drivers);
        }
        return null;
    }

    private List<ComponentInstance> createDataSourceFactories(final Bundle bundle, final List<String> drivers) {
        List<ComponentInstance> instances = new ArrayList<>();
        for (String driver : drivers) {
            ComponentInstance instance = createDataSourceFactory(bundle, driver);
            if (instance != null) {
                instances.add(instance);
            }
        }
        if (instances.isEmpty()) {
            return null;
        }
        return instances;
    }

    private ComponentInstance createDataSourceFactory(final Bundle bundle, final String driver) {
        try {
            Class<? extends Driver> driverClass = bundle.loadClass(driver).asSubclass(Driver.class);

            Dictionary<String, Object> configuration = new Hashtable<>();
            configuration.put("driver", driverClass.newInstance());
            configuration.put("bundle", bundle);

            return factory.createComponentInstance(configuration);
        } catch (UnacceptableConfiguration | MissingHandlerException | ConfigurationException e) {
            // TODO Log something
            System.out.println("---------------- createDataSourceFactory (iPOJO) ---------------------");
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            // Ignored
            System.out.println("---------------- createDataSourceFactory (CNFE) ---------------------");
            e.printStackTrace();
            return null;
        }
    }

    private List<String> getDriverNames(final URL resource) {
        List<String> drivers = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.openStream()))) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                line = line.trim();
                //  Ignore empty and commented line
                if ((line.length() > 0) && (line.charAt(0) != '#')) {
                    drivers.add(line);
                }
            }
        } catch (IOException ioe) {
            return Collections.emptyList();
        }

        return drivers;
    }

    @Override
    public void modifiedBundle(final Bundle bundle, final BundleEvent event, final List<ComponentInstance> instances) {

    }

    @Override
    public void removedBundle(final Bundle bundle, final BundleEvent event, final List<ComponentInstance> instances) {
        for (ComponentInstance instance : instances) {
            instance.dispose();
        }
    }
}
