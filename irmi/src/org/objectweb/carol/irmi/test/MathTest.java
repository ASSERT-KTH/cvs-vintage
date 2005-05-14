/**
 * Copyright (c) 2004 Red Hat, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * Component of: Red Hat Application Server
 *
 * Initial Developers: Rafael H. Schloming
 */
package org.objectweb.carol.irmi.test;

import java.rmi.NoSuchObjectException;

/**
 * MathTest
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

public class MathTest extends RMITest {

    private RMath m;

    protected void setUp() throws Exception {
        super.setUp();
        m = (RMath) getObject(RMath.class.getName());
    }

    public void testAddWhole() throws Exception {
        for (int a = 0; a < 10; a++) {
            for (int b = 0; b < 10; b++) {
                testAddWhole(a, b);
            }
        }
    }

    private void testAddWhole(int a, int b) throws Exception {
        int sum = a + b;
        assertEquals(sum, m.add((byte) a, (byte) b));
        assertEquals(sum, m.add((short) a, (short) b));
        assertEquals(sum, m.add((int) a, (int) b));
        assertEquals(sum, m.add((long) a, (long) b));
    }

    public void testAddFloat() throws Exception {
        for (float a = 0; a < 1; a+=0.1) {
            for (float b = 0; b < 1; b+=0.1) {
                float sum = a + b;
                String msg = "a = " + a + ", b = " + b + ", sum = " + sum;
                assertTrue(msg, sum == m.add(a, b));
            }
        }
    }

    public void testAddDouble() throws Exception {
        for (double a = 0; a < 1; a+=0.1) {
            for (double b = 0; b < 1; b+=0.1) {
                double sum = a + b;
                String msg = "a = " + a + ", b = " + b + ", sum = " + sum;
                assertTrue(msg, sum == m.add(a, b));
            }
        }
    }

    public void testAddStringAndChar() throws Exception {
        for (char a = 'a'; a < 'z'; a++) {
            for (char b = 'a'; b < 'z'; b++) {
                String sum = "" + a + b;
                assertEquals(sum, m.add("" + a, "" + b));
                int c = a + b;
                assertEquals(c, m.add(a, b));
            }
        }
    }

    public void testDiv() throws Exception {
        for (int a = 0; a < 100; a+=10) {
            for (int b = 1; b < 10; b++) {
                assertEquals(a/b, m.div(a, b));
            }
        }

        try {
            m.div(1, 0);
            fail("expected ArithmeticException");
        } catch (ArithmeticException e) {
            // succeed
        }
    }

    public void testRemove() throws Exception {
        Remover obj = (Remover) getObject(Remover.class.getName());
        obj.remove();
        try {
            obj.remove();
            fail("expected NoSuchObjectException");
        } catch (NoSuchObjectException e) {
            // succeed
        }
    }

}
