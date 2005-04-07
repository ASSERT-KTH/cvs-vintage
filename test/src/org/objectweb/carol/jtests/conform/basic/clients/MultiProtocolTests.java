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
 * $Id: MultiProtocolTests.java,v 1.14 2005/04/07 15:07:08 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jtests.conform.basic.clients;

import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.rmi.CORBA.Stub;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.omg.CORBA.ORB;

import org.objectweb.carol.jtests.conform.basic.server.BasicMultiObjectItf;
import org.objectweb.carol.jtests.conform.basic.server.BasicObjectItf;
import org.objectweb.carol.jtests.conform.basic.server.BasicRemoteObject;
import org.objectweb.carol.jtests.conform.basic.server.BasicSerializableObject;
import org.objectweb.carol.util.configuration.CarolDefaultValues;
import org.objectweb.carol.util.configuration.ConfigurationRepository;

/**
 * Class <code>MultiProtocolTests</code> is a Junit BasicTest Test : Test The
 * InitialContext and the PortableRemoteObject situation with remote object
 * @author Guillaume Riviere
 * @author Florent Benoit
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
     * @param name the name of the test
     */
    public MultiProtocolTests(String name) {
        super(name);
    }

    /**
     * Setup Method
     * @throws Exception if setup fails (could be narrow)
     */
    public void setUp() throws Exception {
        super.setUp();

        ConfigurationRepository.init();

        ic = new InitialContext();

        // set the object name
        basicName = "basicname";
        basicMultiName = "basicmultiname";

        // lookup to the remote objects
        ba = (BasicObjectItf) PortableRemoteObject.narrow(ic.lookup(basicName), BasicObjectItf.class);
        bma = (BasicMultiObjectItf) PortableRemoteObject.narrow(ic.lookup(basicMultiName), BasicMultiObjectItf.class);
    }

    /**
     * tearDown method
     * @throws Exception if super method fails
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
     * Try to bind/lookup a serializable object
     */
    public void testSerializable() {
        BasicSerializableObject bso = new BasicSerializableObject("test1");
        BasicSerializableObject bsoResult = null;
        try {
            ic.bind("testSerializable", bso);
        } catch (Exception e) {
            fail("Can't bind object : " + e);
        }
        Object lookupObj = null;
        try {
            lookupObj = ic.lookup("testSerializable");
        } catch (Exception e) {
            fail("Can't lookup object : " + e);
        }

        try {
            bsoResult = (BasicSerializableObject) PortableRemoteObject.narrow(lookupObj, BasicSerializableObject.class);
        } catch (Exception e) {
            fail("Can't narrow object : " + e);
        }

        assertTrue(bso.equals(bsoResult));

    }

    /**
     * test context methods
     */
    public void testContextCommonContextMethods() {
        try {
            ic.bind("testContextCommonContextMethods", bma);
        } catch (Exception e) {
            fail("Can't bind object : " + e);
        }
        Object lookupObj = null;
        try {
            lookupObj = ic.lookup("testContextCommonContextMethods");
        } catch (Exception e) {
            fail("Can't lookup object : " + e);
        }

        // Now rebind object
        try {
            ic.rebind("testContextCommonContextMethods", bma);
        } catch (Exception e) {
            fail("Can't rebind object : " + e);
        }

        // Now unbind object
        try {
            ic.unbind("testContextCommonContextMethods");
        } catch (Exception e) {
            fail("Can't unbind object : " + e);
        }

        // verify the unbind
        try {
            lookupObj = ic.lookup("testContextCommonContextMethods");
            fail("lookup of the object should fail as the object has been unbind");
        } catch (Exception e) {
            assertTrue("exception is : " + e.getMessage(), NamingException.class.isAssignableFrom(e.getClass()));
        }

        // bind a new object to test the rename
        BasicSerializableObject bso = new BasicSerializableObject("testContextCommonContextMethods2");
        try {
            ic.bind("testContextCommonContextMethods2", bso);
            ic.rename("testContextCommonContextMethods2", "testContextCommonContextMethods2renamed");
        } catch (Exception e) {
            fail("Can't bind object : " + e);
        }
        // verify the rename
        try {
            lookupObj = ic.lookup("testContextCommonContextMethods2");
            fail("lookup of the object should fail as the object has been renamed");
        } catch (Exception e) {
            assertTrue("exception is : " + e.getMessage(), NamingException.class.isAssignableFrom(e.getClass()));
        }
        try {
            lookupObj = ic.lookup("testContextCommonContextMethods2renamed");
            BasicSerializableObject bsoResult = (BasicSerializableObject) PortableRemoteObject.narrow(lookupObj,
                    BasicSerializableObject.class);
            assertTrue(bso.equals(bsoResult));
        } catch (Exception e) {
            fail("lookup of the object should not fail as a previous object has been renamed");
        }

        // List context (and verify object is present)
        try {
            ic.bind("testContextCommonContextMethods4", bso);
            NamingEnumeration ne = ic.list("");
            boolean found = false;
            while (ne.hasMore() && !found) {
                NameClassPair ncp = (NameClassPair) ne.next();
                String n = ncp.getName();
                if (n.equals("testContextCommonContextMethods4")) {
                    found = true;
                }
            }
            if (!found) {
                fail("object bind was not find in ic.list(\"\")");
            }

            // Use listBindings method now
            ne = ic.listBindings("");
            found = false;
            while (ne.hasMore() && !found) {
                Binding binding = (Binding) ne.next();
                String n = binding.getName();
                if (n.equals("testContextCommonContextMethods4")) {
                    found = true;
                }
            }
            if (!found) {
                fail("object bind was not find in ic.listBindings(\"\")");
            }

            // unbind object
            ic.unbind("testContextCommonContextMethods4");
        } catch (Exception e) {
            fail("list() method fails");
        }

        // test unbind a name not bind
        try {
            ic.unbind("testContextCommonContextMethods4");
        } catch (Exception e) {
            assertTrue(e instanceof NameNotFoundException);
        }

    }

    /**
     * Test Method , Test an access on a remote object The default orb is used
     * for this access
     */
    public void testPortableRemoteObject() {
        BasicRemoteObject bro = new BasicRemoteObject("testPortableRemoteObject");

        // exportObject() method
        try {
            PortableRemoteObject.exportObject(bro);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Cannot export object '" + bro + "' : " + e.getMessage());
        }

        // toStub() method
        try {
            Object remoteVal = PortableRemoteObject.toStub(bro);

            // in case of IIOP, connect stub as it use POA model and stub should
            // be connected
            if (remoteVal instanceof Stub) {
                ((Stub) remoteVal).connect(ORB.init(new String[0], null));
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Cannot use toStub() method on object '" + bro + "' : " + e.getMessage());
        }

        // unexportObject() method
        try {
            PortableRemoteObject.unexportObject(bro);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Cannot unexportObject object '" + bro + "' : " + e.getMessage());
        }

        // test narrow() method
        try {
            Object o = ic.lookup("basicmultiname");
            PortableRemoteObject.narrow(o, BasicMultiObjectItf.class);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Cannot narrow object '" + bro + "'.");
        }

    }

    /**
     * Check that the bind/lookup works on flat registry like JRMP registry Bind
     * objects/obj1 and objects2/obj2 and check that lookup returns different
     * objects On flat registry lookup return same object as it returns only
     * value before / Carol should handle this case to make it working even if
     * this is a flat registry
     */
    public void testCompositeNameId() {

        final String id1 = "objects/testCompositeNameId1";
        final String id2 = "objects/testCompositeNameId2";

        BasicSerializableObject bso1 = new BasicSerializableObject(id1);
        BasicSerializableObject bso2 = new BasicSerializableObject(id2);

        // bind them
        try {
            ic.bind(id1, bso1);
        } catch (Exception e) {
            fail("Can't bind object 1: " + e);
        }

        try {
            ic.bind(id2, bso2);
        } catch (Exception e) {
            fail("Can't bind object 1: " + e);
        }

        // lookup objects
        BasicSerializableObject bsoResult1 = null;
        BasicSerializableObject bsoResult2 = null;

        try {
            bsoResult1 = (BasicSerializableObject) PortableRemoteObject.narrow(ic.lookup(id1),
                    BasicSerializableObject.class);
            bsoResult2 = (BasicSerializableObject) PortableRemoteObject.narrow(ic.lookup(id2),
                    BasicSerializableObject.class);
        } catch (Exception e) {
            fail("Can't lookup object : " + e);
        }

        // Now compare objects and they should be different !
        assertFalse("Should be different : obj1 = " + bsoResult1 + ", obj2 = " + bsoResult2 + ".", bsoResult1
                .equals(bsoResult2));

        // Another check. List context and see if there is our two names
        try {
            NamingEnumeration ne = ic.list("");
            boolean found = false;
            boolean found1 = false;
            boolean found2 = false;
            String listNames = "";
            while (ne.hasMore() && !found) {
                NameClassPair ncp = (NameClassPair) ne.next();
                String n = ncp.getName();
                listNames += n + " : ";
                if (n.equals(id1)) {
                    found1 = true;
                    continue;
                }
                if (n.equals(id2)) {
                    found2 = true;
                    continue;
                }

                found = found1 && found2;
            }
            if (!found1) {
                fail("object named " + id1 + " was not find in the list. Names found were : " + listNames);
            }
            if (!found2) {
                fail("object named " + id2 + " was not find in the list. Names found were : " + listNames);
            }

        } catch (Exception e) {
            fail("Can't list registry : " + e);
        }

        // unbind
        try {
            ic.unbind(id1);
            ic.unbind(id2);
        } catch (Exception e) {
            fail("Can't unbind object : " + e);
        }

    }

    /**
     * Test basic commands on java:comp/env (ENC) or java:comp environment
     */
    public void testJavaCompEnvironment() {

        String tstId = "testJavaEnvironment";
        // bind
        try {
            ic.bind("java:comp/env/testJavaEnvironment", tstId);
        } catch (Exception e) {
            fail("Can't bind java:comp/env/testJavaEnvironment " + e);
        }

        Context javaComp = null;
        try {
            javaComp = (Context) ic.lookup("java:comp/");
        } catch (NamingException e) {
            fail("Cannot lookup java:comp : " + e);
        }

        try {
            javaComp.lookup("env");
        } catch (NamingException e) {
            fail("Cannot lookup javaComp.lookup(\"env\") : " + e);
        }

        Context javaCompEnv = null;
        try {
            javaCompEnv = (Context) ic.lookup("java:comp/env");
        } catch (NamingException e) {
            fail("Cannot lookup java:comp/env : " + e);
        }

        try {
            javaCompEnv.createSubcontext("ejb");
        } catch (NamingException e) {
            fail("Cannot use lookup javaCompEnv.createSubcontext(\"ejb\") " + e);
        }

        // lookup String
        String resId = null;
        try {
            resId = (String) ic.lookup("java:comp/env/testJavaEnvironment");
        } catch (Exception e) {
            fail("Can't lookup object java:comp/env/testJavaEnvironment : " + e);
        }

        assertEquals(tstId, resId);

    }

    /**
     * Test InitialContext with specific Factory / URL
     */
    public void testSingleInitialContext() {
        Hashtable testEnv = new Hashtable();
        String providerURL = ConfigurationRepository.getCurrentConfiguration().getProviderURL();
        testEnv.put(Context.PROVIDER_URL, providerURL);

        Context tmpContext = null;
        try {
            tmpContext = new InitialContext(testEnv);
        } catch (NamingException e) {
            e.printStackTrace();
            fail("Cannot build a new initial context with provider url = " + providerURL);
        }

        String id = "testSingleInitialContext";
        BasicSerializableObject bso1 = new BasicSerializableObject(id);
        // bind it
        try {
            tmpContext.bind(id, bso1);
        } catch (Exception e) {
            fail("Can't bind object 1: " + e);
        }

        // lookup
        BasicSerializableObject bsoResult1 = null;
        try {
            bsoResult1 = (BasicSerializableObject) PortableRemoteObject.narrow(tmpContext.lookup(id),
                    BasicSerializableObject.class);
        } catch (Exception e) {
            fail("Can't lookup object : " + e);
        }

        // Now compare objects and they should be the same
        assertTrue("Should be the same", bso1.equals(bsoResult1));

        // test with adding the JNDI factory now
        Hashtable testEnv2 = new Hashtable();
        String initFactory = ConfigurationRepository.getCurrentConfiguration().getProtocol().getInitialContextFactoryClassName();
        testEnv2.put(Context.PROVIDER_URL, providerURL);
        testEnv2.put(Context.INITIAL_CONTEXT_FACTORY, initFactory);

        Context tmpContext2 = null;
        try {
            tmpContext2 = new InitialContext(testEnv2);
        } catch (NamingException e) {
            e.printStackTrace();
            fail("Cannot build a new initial context with provider url = " + providerURL + " and factory = "
                    + initFactory);
        }

        String id2 = "testSingleInitialContext2";
        BasicSerializableObject bso2 = new BasicSerializableObject(id);
        // bind it
        try {
            tmpContext2.bind(id2, bso2);
        } catch (Exception e) {
            fail("Can't bind object 1: " + e);
        }

        // lookup
        BasicSerializableObject bsoResult2 = null;
        try {
            bsoResult2 = (BasicSerializableObject) PortableRemoteObject.narrow(tmpContext2.lookup(id2),
                    BasicSerializableObject.class);
        } catch (Exception e) {
            fail("Can't lookup object : " + e);
        }

        // Now compare objects and they should be the same
        assertTrue("Should be the same", bso2.equals(bsoResult2));

        // test that it fails with invalid properties
        Hashtable env3 = new Hashtable();
        env3.put(Context.PROVIDER_URL, "dummy://123.123.123.123:9876");
        try {
            new InitialContext(env3);
            fail("The context should fail with invalid properties");
        } catch (NamingException e) {
            // awaited result
            e.printStackTrace();
        }

    }

    /**
     * Test of environment Method
     * bug #300979
     */
    public void testIctxEnvironment() {
        Hashtable testEnv = null;

        try {
            testEnv = ic.getEnvironment();
        } catch (NamingException e) {
            e.printStackTrace();
            fail("Cannot get environment");
        }

        Context newIctx = null;
        try {
            newIctx = new InitialContext(testEnv);
        } catch (NamingException e) {
            e.printStackTrace();
            fail("Cannot get ICTX environment");
        }

        // test factory is the multi factory
        try {
            assertTrue(newIctx.getEnvironment().get(Context.INITIAL_CONTEXT_FACTORY).equals(
                    CarolDefaultValues.MULTI_JNDI));
        } catch (NamingException ne) {
            ne.printStackTrace();
            fail("Should be able to get the environment : " + ne.getMessage());
        }

        String id = "testIctxEnvironment";
        BasicSerializableObject bso1 = new BasicSerializableObject(id);
        // bind it
        try {
            newIctx.bind(id, bso1);
        } catch (Exception e) {
            fail("Can't bind object 1: " + e);
        }

        // lookup
        BasicSerializableObject bsoResult1 = null;
        try {
            bsoResult1 = (BasicSerializableObject) PortableRemoteObject.narrow(newIctx.lookup(id),
                    BasicSerializableObject.class);
        } catch (Exception e) {
            fail("Can't lookup object : " + e);
        }

        // Now compare objects and they should be the same
        assertTrue("Should be the same", bso1.equals(bsoResult1));

    }

    /**
     * Suite method
     * @return the test suite to launch
     */
    public static Test suite() {
        return new TestSuite(MultiProtocolTests.class);
        // In case of launching only one test
        /*TestSuite testSuite = new TestSuite();
        testSuite.addTest(new MultiProtocolTests("testStub"));
        reurn testSuite;*/
    }
}