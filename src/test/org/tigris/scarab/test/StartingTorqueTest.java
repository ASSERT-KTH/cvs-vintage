/*
 * Created on Feb 1, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.tigris.scarab.test;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.torque.Torque;
import org.tigris.scarab.om.Activity;
import org.tigris.scarab.om.ActivityManager;

import junit.framework.TestCase;

/**
 * @author Eric Pugh
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class StartingTorqueTest extends TestCase {

	public void testStartingTorque() {
		try {
			Configuration config =
				new PropertiesConfiguration("src/test/TestTurbineResources.properties");

			Torque.init(config);

			System.out.println("Success Initing Torque!");
			Activity activity = ActivityManager.getInstance(new Long(1));
			assertNotNull(activity);
			assertEquals(1,activity.getActivityId().intValue());
		} catch (Exception e) {
			System.out.println("Can't initialize Torque!!");
			e.printStackTrace();
		}
	}
}
