package org.tigris.scarab.test;

import java.io.File;

import junit.framework.TestCase;

import org.apache.fulcrum.TurbineServices;
import org.apache.turbine.TurbineConfig;
import org.apache.turbine.TurbineXmlConfig;
/**
 * Test case that just starts up Turbine.  All Scarab specific
 * logic needs to be implemented in your own test cases.
 * 
 * @author     <a href="mailto:epugh@upstate.com">Eric Pugh</a>
 */
public class BaseTurbineTestCase extends TestCase {
	private static TurbineConfig tc = null;
	
	private static boolean initialized = false;

	public BaseTurbineTestCase() {
	}

	public BaseTurbineTestCase(String name) throws Exception {
		super(name);
	}
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {

		if (!initialized) {
			initTurbine();
	
			initialized=true;
		}
	}
	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		if (tc != null) {
			TurbineServices.getInstance().shutdownServices();
			tc=null;
		}
	}

		
	private void initTurbine() throws Exception {
		File directoryFile = new File("src/test");
		String directory = directoryFile.getAbsolutePath();

		tc =
		    new TurbineXmlConfig(directory, "TestTurbineConfiguration.xml");
		tc.init();
		
	}

	
}
