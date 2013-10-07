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

import org.testng.annotations.Test;

import java.util.Properties;

import static org.testng.Assert.assertEquals;

/**
 * User: guillaume
 * Date: 10/10/13
 * Time: 17:02
 */
public class BeanTestCase {
    @Test
    public void testInstanceConfiguration() throws Exception {
        Properties p = new Properties();
        p.setProperty("hello", "Guillaume");

        HelloJavaBean instance = new HelloJavaBean();
        Bean bean = new Bean(instance);
        bean.configure(p);

        assertEquals(instance.getHello(), "Guillaume");
    }
    @Test
    public void testPrimitiveTypes() throws Exception {
        Properties p = new Properties();
        p.setProperty("byteValue", "0");
        p.setProperty("charValue", "a");
        p.setProperty("intValue", "42");
        p.setProperty("booleanValue", "true");
        p.setProperty("longValue", "123456789");
        p.setProperty("shortValue", "122");
        p.setProperty("doubleValue", "122.45");
        p.setProperty("floatValue", "12.45");

        PrimitiveJavaBean instance = new PrimitiveJavaBean();
        Bean bean = new Bean(instance);
        bean.configure(p);

        assertEquals(instance.getByteValue(), 0);
        assertEquals(instance.getCharValue(), 'a');
        assertEquals(instance.getIntValue(), 42);
        assertEquals(instance.isBooleanValue(), true);
        assertEquals(instance.getLongValue(), 123456789l);
        assertEquals(instance.getShortValue(), 122);
        assertEquals(instance.getDoubleValue(), 122.45d);
        assertEquals(instance.getFloatValue(), 12.45f);

    }



    private static class HelloJavaBean {
        private String hello;

        public String getHello() {
            return hello;
        }

        public void setHello(final String hello) {
            this.hello = hello;
        }
    }

    private static class PrimitiveJavaBean {
        private byte byteValue;
        private char charValue;
        private int intValue;
        private long longValue;
        private boolean booleanValue;
        private short shortValue;
        private double doubleValue;
        private float floatValue;

        public byte getByteValue() {
            return byteValue;
        }

        public void setByteValue(final byte byteValue) {
            this.byteValue = byteValue;
        }

        public char getCharValue() {
            return charValue;
        }

        public void setCharValue(final char charValue) {
            this.charValue = charValue;
        }

        public int getIntValue() {
            return intValue;
        }

        public void setIntValue(final int intValue) {
            this.intValue = intValue;
        }

        public long getLongValue() {
            return longValue;
        }

        public void setLongValue(final long longValue) {
            this.longValue = longValue;
        }

        public boolean isBooleanValue() {
            return booleanValue;
        }

        public void setBooleanValue(final boolean booleanValue) {
            this.booleanValue = booleanValue;
        }

        public short getShortValue() {
            return shortValue;
        }

        public void setShortValue(final short shortValue) {
            this.shortValue = shortValue;
        }

        public double getDoubleValue() {
            return doubleValue;
        }

        public void setDoubleValue(final double doubleValue) {
            this.doubleValue = doubleValue;
        }

        public float getFloatValue() {
            return floatValue;
        }

        public void setFloatValue(final float floatValue) {
            this.floatValue = floatValue;
        }
    }
}
