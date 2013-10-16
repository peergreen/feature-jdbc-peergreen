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

import com.peergreen.deployment.DiscoveryPhasesLifecycle;
import com.peergreen.deployment.ProcessorContext;
import com.peergreen.deployment.ProcessorException;
import com.peergreen.deployment.facet.content.Content;
import com.peergreen.deployment.facet.content.ContentException;
import com.peergreen.deployment.processor.Discovery;
import com.peergreen.deployment.processor.Processor;
import com.peergreen.deployment.processor.Uri;
import com.peergreen.jdbc.internal.datasource.Constants;
import com.peergreen.jdbc.internal.datasource.deploy.DataSourceInfo;
import com.peergreen.jdbc.internal.datasource.deploy.processor.name.Cleaner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Processor
@Discovery(DiscoveryPhasesLifecycle.FACET_SCANNER)
@Uri(extension = "datasource")
public class DataSourceFacetScannerProcessor {

    private final Cleaner cleaner;

    public DataSourceFacetScannerProcessor() {
        this(new Cleaner());
    }

    public DataSourceFacetScannerProcessor(final Cleaner cleaner) {
        this.cleaner = cleaner;
    }

    /**
     * Load the properties content and produce a {@link com.peergreen.jdbc.internal.datasource.deploy.DataSourceInfo} facet.
     */
    public void handle(Content content, ProcessorContext context) throws ProcessorException {
        try {
            DataSourceInfo info = load(content.getInputStream(), context.getArtifact().name());
            context.addFacet(DataSourceInfo.class, info);
        } catch (IOException e) {
            throw new ProcessorException("Unable to read the file", e);
        } catch (ContentException e) {
            throw new ProcessorException("Unable to get input stream", e);
        }
    }

    private DataSourceInfo load(final InputStream stream, final String name) throws IOException {
        Properties raw = new Properties();
        raw.load(stream);

        String datasourceName = raw.getProperty(Constants.DATASOURCE_NAME);
        if (datasourceName == null) {
            datasourceName = "jdbc/" + cleaner.clean(name);
        }

        DataSourceInfo info = new DataSourceInfo(datasourceName);
        for (String key : raw.stringPropertyNames()) {
            info.getProperties().put(key, raw.getProperty(key));
        }
        return info;
    }
}
