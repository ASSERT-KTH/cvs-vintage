/*
 * @(#) LmiBasicTest.java	1.0 02/07/15
 *
 * Copyright (C) 2002 - INRIA (www.inria.fr)
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
 *
 */
package org.objectweb.carol.jtests.conform.basic.clients.lmi;

// test server import
import org.objectweb.carol.jtests.conform.basic.server.BasicServer;

// java import
import java.util.Properties;
import javax.rmi.PortableRemoteObject;

// javax import
import javax.naming.InitialContext;

// junit import 
import junit.framework.TestSuite;
import junit.framework.Test;
import junit.framework.TestCase;

// Test import
import org.objectweb.carol.jtests.conform.basic.clients.MultiProtocolTests;

/*
 * Class <code>IiopBasicTest</code> is a Junit BasicTest Test :
 * local client for the java multi protocol
 * access via lmi
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002   
 */
public class LmiBasicTest extends MultiProtocolTests {
 
    
    /**
     * Constructor
     * @param String Name for this test
     */
    public LmiBasicTest (String name) {
	super(name);
    }   

    /**
     * Setup Method
     */
    public void setUp() {	
	try {
	    // start a local server with all bindings
	    BasicServer.start();
	    setInitialContext(new InitialContext());
	    super.setUp();	     

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("SetUp() Fail" + e);
	}
	
    }
    
    /**
     * tearDown method
     */
    public void tearDown() {
	try {
	    // stop local server

	    super.tearDown();
	} catch (Exception e) {
	    fail("tearDown() Fail" + e);
	} 
    } 

    /**
     * Suite method 
     */
    public static Test suite() { 
	return new TestSuite(LmiBasicTest.class); 	
    }

}
