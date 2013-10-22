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

package com.peergreen.jdbc.internal.cm;

import javax.transaction.Transaction;

/**
 * User: guillaume
 * Date: 24/10/13
 * Time: 11:47
 */
public class EmptyConnectionManagerListener implements ConnectionManagerListener {
    @Override
    public void connectionEnlisted(final Transaction transaction) {

    }

    @Override
    public void connectionEnlistmentError() {

    }

    @Override
    public void connectionDelisted(final Transaction transaction) {

    }

    @Override
    public void connectionServed() {

    }

    @Override
    public void connectionReusedInSameTransaction(final Transaction transaction) {

    }

    @Override
    public void connectionFreedAfterTransactionCompletion(final Transaction transaction) {

    }
}
