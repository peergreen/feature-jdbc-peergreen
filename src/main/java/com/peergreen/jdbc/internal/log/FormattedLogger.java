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

package com.peergreen.jdbc.internal.log;

import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;

/**
 * User: guillaume
 * Date: 21/10/13
 * Time: 14:25
 */
public class FormattedLogger implements Log {
    private final Logger delegate;

    public FormattedLogger(final String name) {
        this(Logger.getLogger(name));
    }

    public FormattedLogger(final Logger delegate) {
        this.delegate = delegate;
    }

    @Override
    public Log create(final String name) {
        return new FormattedLogger(delegate.getName() + "." + name);
    }

    @Override
    public void fine(String message, Object... objects) {
        if (delegate.isLoggable(Level.FINE)) {
            delegate.fine(format(message, objects));
        }
    }

    @Override
    public void info(String message, Object... objects) {
        if (delegate.isLoggable(Level.INFO)) {
            delegate.info(format(message, objects));
        }
    }

    @Override
    public void warn(String message, Object... objects) {
        if (delegate.isLoggable(Level.WARNING)) {
            Throwable t = getLastThrowable(objects);
            String format = format(message, objects);
            if (t != null) {
                delegate.log(Level.WARNING, format, t);
            } else {
                delegate.warning(format);
            }
        }
    }

    private Throwable getLastThrowable(final Object[] parameters) {
        if (parameters.length == 0) {
            return null;
        }

        Object last = parameters[parameters.length - 1];
        if (last instanceof Throwable) {
            return (Throwable) last;
        }

        return null;
    }

    @Override
    public void error(String message, Object... objects) {
        if (delegate.isLoggable(Level.SEVERE)) {
            Throwable t = getLastThrowable(objects);
            String format = format(message, objects);
            if (t != null) {
                delegate.log(Level.SEVERE, format, t);
            } else {
                delegate.severe(format);
            }
        }
    }

}
