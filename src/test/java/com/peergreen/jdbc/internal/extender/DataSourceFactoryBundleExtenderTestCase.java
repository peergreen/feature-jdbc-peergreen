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

import org.apache.felix.ipojo.Factory;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.sql.DriverManager;
import java.util.Dictionary;

/**
 * User: guillaume
 * Date: 14/09/13
 * Time: 19:25
 */
public class DataSourceFactoryBundleExtenderTestCase {

    @Mock
    private BundleContext bundleContext;

    @Mock
    private Bundle bundle;

    @Mock
    private Factory factory;

    @Captor
    private ArgumentCaptor<BundleListener> capture;

    @Captor
    private ArgumentCaptor<Dictionary> capture2;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        DriverManager.registerDriver(PseudoDriver.INSTANCE);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        DriverManager.deregisterDriver(PseudoDriver.INSTANCE);
    }

    @Test
    public void testInstanceCreatedWhenServicesFileIsFound() throws Exception {

        when(bundle.getResource(DataSourceFactoryBundleExtender.SERVICES_DRIVER_NAME))
                .thenReturn(getClass().getResource("/java-sql-Driver.txt"));
        Mockito.<Class<?>>when(bundle.loadClass("com.peergreen.jdbc.internal.extender.PseudoDriver"))
               .thenReturn(PseudoDriver.class);
        when(bundle.getState()).thenReturn(Bundle.ACTIVE);

        DataSourceFactoryBundleExtender extender = new DataSourceFactoryBundleExtender(bundleContext, factory);
        extender.start();

        verify(bundleContext).addBundleListener(capture.capture());

        capture.getValue().bundleChanged(new BundleEvent(BundleEvent.STARTED, bundle));

        verify(factory).createComponentInstance(capture2.capture());
        assertTrue(capture2.getValue().get("driver") instanceof PseudoDriver);

    }
}
