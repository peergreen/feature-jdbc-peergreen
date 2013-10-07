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

package com.peergreen.jdbc.internal.cm.bean;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import static java.lang.String.format;

/**
 * User: guillaume
 * Date: 09/10/13
 * Time: 21:13
 */
public class Bean {
    private final Object instance;
    private final Class<?> type;

    public Bean(final Object instance) {
        this(instance, instance.getClass());
    }

    public Bean(final Object instance, final Class<?> type) {
        this.instance = instance;
        this.type = type;
    }

    public Object configure(Properties properties) throws Exception {
        configure(instance, properties);
        return instance;
    }

    protected void configure(final Object instance, final Properties properties) throws Exception {
        for (String name : properties.stringPropertyNames()) {
            Method setter = findPropertySetter(name);
            if (setter != null) {
                setProperty(instance, setter, properties.getProperty(name));
            }
        }
    }

    private void setProperty(final Object instance, final Method setter, final String value) throws Exception {
        Class<?> expected = setter.getParameterTypes()[0];
        Object converted = convert(value.trim(), expected);
        setter.invoke(instance, converted);
    }

    private Object convert(final String value, final Class<?> expected) {
        if (String.class.equals(expected)) {
            return value;
        } else if ((Integer.class.equals(expected)) || Integer.TYPE.equals(expected)) {
            return Integer.valueOf(value);
        } else if ((Short.class.equals(expected)) || Short.TYPE.equals(expected)) {
            return Short.valueOf(value);
        } else if ((Double.class.equals(expected)) || Double.TYPE.equals(expected)) {
            return Double.valueOf(value);
        } else if ((Byte.class.equals(expected)) || Byte.TYPE.equals(expected)) {
            return Byte.valueOf(value);
        } else if ((Float.class.equals(expected)) || Float.TYPE.equals(expected)) {
            return Float.valueOf(value);
        } else if ((Long.class.equals(expected)) || Long.TYPE.equals(expected)) {
            return Long.valueOf(value);
        } else if ((Boolean.class.equals(expected)) || Boolean.TYPE.equals(expected)) {
            return Boolean.valueOf(value);
        } else if ((Character.class.equals(expected)) || Character.TYPE.equals(expected)) {
            if (value.length() == 1) {
                return value.charAt(0);
            }
            throw new IllegalStateException(format("Provided String '%s' is not adaptable to a single char", value));
        } else if (URL.class.equals(expected)) {
            try {
                return new URL(value);
            } catch (MalformedURLException e) {
                throw new IllegalStateException(e);
            }
        } else if (URI.class.equals(expected)) {
            try {
                return new URI(value);
            } catch (URISyntaxException e) {
                throw new IllegalStateException(e);
            }
        }
        return null;
    }

    private Method findPropertySetter(final String name) {
        String setterName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
        for (Method method : type.getMethods()) {
            if (setterName.equals(method.getName())) {
                if (method.getParameterTypes().length == 1) {
                    return method;
                }
            }
        }
        return null;
    }
}
