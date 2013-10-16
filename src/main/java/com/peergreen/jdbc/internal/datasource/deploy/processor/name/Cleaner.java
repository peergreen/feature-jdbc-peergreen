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

package com.peergreen.jdbc.internal.datasource.deploy.processor.name;

/**
 * User: guillaume
 * Date: 16/10/13
 * Time: 17:55
 */
public class Cleaner {

    public String clean(final String dirty) {
        // TODO Supports URI
        // file:/toto.datasource
        // urn:a:dbgml.datasource
        // toroto.datasource

        String cleaned = dirty;

        // Strip the beginning up to the last '/'
        int slashIndex = cleaned.lastIndexOf('/');
        if (slashIndex != -1) {
            cleaned = cleaned.substring(slashIndex + 1);
        }

        // Remove the leading .datasource (if any)
        if (cleaned.endsWith(".datasource")) {
            cleaned = cleaned.substring(0, cleaned.length() - ".datasource".length());
        }

        // Replace invalid chars
        cleaned = cleaned.replace(':', '_');

        return cleaned;

    }
}
