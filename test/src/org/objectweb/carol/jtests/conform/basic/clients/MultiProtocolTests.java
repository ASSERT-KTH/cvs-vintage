/**
 * Copyright (C) 2002,2005 - INRIA (www.inria.fr)
 *
 * CAROL: Common Architecture for RMI ObjectWeb Layer
 *
 * This library is developed inside the ObjectWeb Consortium,
 * http://www.objectweb.org
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
 * --------------------------------------------------------------------------
 * $Id: MultiProtocolTests.java,v 1.8 2005/02/08 10:03:48 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jtests.conform.basic.clients;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.objectweb.carol.jtests.conform.basic.server.BasicMultiObjectItf;
import org.objectweb.carol.jtests.conform.basic.server.BasicObjectItf;

/**
 * Class <code>MultiProtocolTests</code> is a Junit BasicTest Test : Test The
 * InitialContext and the PortableRemoteObject situation with remote object
 * @author Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 */
public class MultiProtocolTests extends TestCase {

    /**
     * Name of the basic remote object (in all name services)
     */
    private String basicName = null;

    /**
     * Name of the basic multi remote object (in all name services)
     */
    private String basicMultiName = null;

    /**
     * Name of the basic object ref (in all name services)
     */
    private String basicRefName = null;

    /**
     * Initial Contexts
     */
    private InitialContext ic = null;

    /**
     * TheBasicObject
     */
    private BasicObjectItf ba = null;

    /**
     * TheBasicMultiObject
     */
    private BasicMultiObjectItf bma = null;

    /**
     * Constructor
     * @param String Name for this test
     */
    public MultiProtocolTests(String name) {
        super(name);
    }

    /**
     * Setup Method
     */
    public void setUp() throws Exception {
        super.setUp();

        org.objectweb.carol.util.configuration.CarolConfiguration.init();

        ic = new InitialContext();

        // set the object name
        basicName = "basicname";
        basicMultiName = "basicmultiname";
        basicRefName = "basicrefname";

        // lookup to the remote objects
        ba = (BasicObjectItf) PortableRemoteObject.narrow(ic.lookup(basicName), BasicObjectItf.class);
        bma = (BasicMultiObjectItf) PortableRemoteObject.narrow(ic.lookup(basicMultiName), BasicMultiObjectItf.class);
    }

    /**
     * tearDown method
     */
    public void tearDown() throws Exception {
        basicName = null;
        basicMultiName = null;
        ba = null;
        bma = null;
        ic.close();
        super.tearDown();
    }

    /**
     * Test Method , Test an access on a remote object The default orb is used
     * for this access
     */
    public void testString() {
        try {
            String expected = ba.getString();
            assertEquals(expected, "string");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Can't get string" + e);
        }
    }

    /**
     * Test Method , Test an access on a remote object which also access to
     * remote object, This tests use 2 call via default protocol The default orb
     * is used for this access
     */
    public void testMultiString() {
        try {
            String expected = bma.getMultiString();
            assertEquals(expected, "multi string call: " + "string");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Can't get multi string" + e);
        }
    }

    /**
     * Test Method , Test an access on a refearence
     */
    public void testReferenceString() {
        try {
            String expected = bma.getBasicRefString();
            assertEquals(expected, "string2");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Can't get ref string" + e);
        }
    }

    /**
     * Test Method , Test an access on a remote object The default orb is used
     * for this access
     */
    public void testStub() {
        try {
            BasicObjectItf ob = (BasicObjectItf) PortableRemoteObject
                    .narrow(bma.getBasicObject(), BasicObjectItf.class);
            String expected = ob.getString();
            assertEquals(expected, "string");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Can't narrow Remote Object :" + e);
        }
    }

    /**
     * Suite method
     */
    public static Test suite() {
        return new TestSuite(MultiProtocolTests.class);
    }
}