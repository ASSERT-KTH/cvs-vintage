//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.core.io;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Random;

import junit.framework.TestCase;


public class CloneInputStreamTest extends TestCase {
    byte[] test;

    public void testCloneSingle() throws Exception {
        InputStream master = new ByteArrayInputStream(test);
        CloneStreamMaster model = new CloneStreamMaster(master);
        CloneInputStream clone1 = model.getClone();
        CloneInputStream clone2 = model.getClone();

        byte[] result1 = new byte[20000];
        byte[] result2 = new byte[20000];

        for (int i = 0; i < 20000; i++) {
            if ((i % 2) == 0) {
                result1[i] = (byte) clone1.read();
                result2[i] = (byte) clone2.read();
            } else {
                result2[i] = (byte) clone2.read();
                result1[i] = (byte) clone1.read();
            }
        }

        for (int i = 0; i < 20000; i++) {
            assertTrue(result1[i] == test[i]);
            assertTrue(result2[i] == test[i]);
        }
    }

    public void testEOFSingle() throws Exception {
        InputStream master = new ByteArrayInputStream(test);
        CloneStreamMaster model = new CloneStreamMaster(master);
        CloneInputStream clone1 = model.getClone();

        int lastRead = clone1.read();
        int pos = 1;

        while (lastRead != -1) {
            lastRead = clone1.read();
            pos++;
        }

        assertTrue(pos == 20001);
    }

    public void testCloneMulti() throws Exception {
        InputStream master = new ByteArrayInputStream(test);
        CloneStreamMaster model = new CloneStreamMaster(master);
        CloneInputStream clone1 = model.getClone();
        CloneInputStream clone2 = model.getClone();

        byte[] result1 = new byte[20000];
        byte[] result2 = new byte[20000];

        byte[] buffer = new byte[50];

        for (int i = 0; i < (20000 / 50); i++) {
            if ((i % 2) == 0) {
                clone1.read(result1, i * 50, 50);
                clone2.read(result2, i * 50, 50);
            } else {
                clone2.read(result2, i * 50, 50);
                clone1.read(result1, i * 50, 50);
            }
        }

        for (int i = 0; i < 20000; i++) {
            assertTrue(result1[i] == test[i]);
            assertTrue(result2[i] == test[i]);
        }
    }

    public void testEOFMulti1() throws Exception {
        InputStream master = new ByteArrayInputStream(test);
        CloneStreamMaster model = new CloneStreamMaster(master);
        CloneInputStream clone1 = model.getClone();

        byte[] buffer = new byte[50];

        int lastRead = clone1.read(buffer);
        int pos = lastRead;

        while (lastRead != -1) {
            lastRead = clone1.read(buffer);
            pos += lastRead;
        }

        assertTrue(pos == 19999);
    }

    public void testEOFMulti2() throws Exception {
        InputStream master = new ByteArrayInputStream(test);
        CloneStreamMaster model = new CloneStreamMaster(master);
        CloneInputStream clone1 = model.getClone();

        byte[] buffer = new byte[30];

        int lastRead = clone1.read(buffer);
        int pos = lastRead;

        while (lastRead == 30) {
            lastRead = clone1.read(buffer);
            pos += lastRead;
        }

        assertTrue(pos == 20000);
    }

    /* (non-Javadoc)
 * @see junit.framework.TestCase#setUp()
 */
    protected void setUp() throws Exception {
        super.setUp();

        Random random = new Random();
        test = new byte[20000];
        random.nextBytes(test);
    }
}
