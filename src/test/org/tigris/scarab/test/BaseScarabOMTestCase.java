package org.tigris.scarab.test;

import java.io.File;

import junit.framework.TestCase;

import org.apache.fulcrum.TurbineServices;
import org.apache.torque.om.NumberKey;
import org.apache.turbine.TurbineConfig;
import org.apache.turbine.TurbineXmlConfig;
import org.tigris.scarab.om.ActivitySet;
import org.tigris.scarab.om.ActivitySetManager;
import org.tigris.scarab.om.ActivitySetTypePeer;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeManager;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueManager;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.IssueTypeManager;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserManager;
/**
 * Test case that provides scaffolding for other Scarab test cases.
 * 
 * @author     <a href="mailto:epugh@upstate.com">Eric Pugh</a>
 */
public class BaseScarabOMTestCase extends TestCase {
	private static TurbineConfig tc = null;
	private static Module module = null;
	private static IssueType defaultIssueType = null;
	protected static int nbrDfltModules = 7;
	protected static int nbrDfltIssueTypes = 5;
	private ScarabUser user0 = null;
	private ScarabUser user1 = null;
	private ScarabUser user2 = null;
	private Issue issue0 = null;
	private Attribute platformAttribute = null;
	private Attribute assignAttribute = null;
	private Attribute ccAttribute = null;

	private static boolean initialized = false;

	public BaseScarabOMTestCase() {
	}

	public BaseScarabOMTestCase(String name) throws Exception {
		super(name);
	}
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {

		if (!initialized) {
			initTurbine();
			initScarab();
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

	/**
	 * Grab Module #5 for testing. This is the same as what the web
	 * application does and this is setup in ScarabPage.tempWorkAround()
	 */
	private void initScarab() throws Exception {
		module = ModuleManager.getInstance(new NumberKey(5), false);
		defaultIssueType =
			IssueTypeManager.getInstance(new NumberKey(1), false);
	}
	
	private void initTurbine() throws Exception {
		File directoryFile = new File("src/test");
		String directory = directoryFile.getAbsolutePath();
		tc =
		    new TurbineXmlConfig(directory, "TestTurbineConfiguration.xml");
		tc.init();
		
	}

	protected ScarabUser getUser1()
	throws Exception
	{
		if (user1 == null)
		{
			user1 = ScarabUserManager.getInstance(new NumberKey(1), false);
		}
		return user1;
	}

	protected ScarabUser getUser2()
	throws Exception
	{
		if (user2 == null)
		{
			user2 = ScarabUserManager.getInstance(new NumberKey(2), false);
		}
		return user2;
	}

	protected ScarabUser getUser5()
	throws Exception
	{
		if (user0 == null)
		{
			user0 = ScarabUserManager.getInstance(new NumberKey(5), false);
		}
		return user0;
	}
	

	protected Issue getIssue0() throws Exception {
		if (issue0 == null) {
			issue0 = IssueManager.getInstance(new NumberKey(1), false);
		}
		return issue0;
	}

	protected Attribute getPlatformAttribute() throws Exception {
		if (platformAttribute == null) {
			platformAttribute = AttributeManager.getInstance(new NumberKey(5));
		}
		return platformAttribute;
	}

	protected ActivitySet getEditActivitySet() throws Exception {
		Attachment attach = new Attachment();
		attach.setTextFields(
			getUser1(),
			getIssue0(),
			Attachment.MODIFICATION__PK);
		attach.setName("commenttest");
		attach.save();

		ActivitySet trans =
			ActivitySetManager.getInstance(
				ActivitySetTypePeer.EDIT_ISSUE__PK,
				getUser1(),
				attach);
		trans.save();
		return trans;
	}
	
	/**
	 * If something like an Issue needs a mapping to a Module, then
	 * this is Module #5 that you can use. For example, you should call:
	 * issue.setModule(getModule()) in your Test before you use any
	 * of the rest of the methods on the Issue object.
	 */
	protected Module getModule()
	{
		return BaseScarabOMTestCase.module;
	}

	protected IssueType getDefaultIssueType()
	{
		return BaseScarabOMTestCase.defaultIssueType;
	}
	protected Attribute getAssignAttribute()
	throws Exception
	{
		if (assignAttribute == null)
		{
			assignAttribute  = AttributeManager.getInstance(new NumberKey(2));
		}
		return assignAttribute;
	}
	
	protected Attribute getCcAttribute()
	throws Exception
	{
		if (ccAttribute == null)
		{
			ccAttribute  = AttributeManager.getInstance(new NumberKey(13));
		}
		return ccAttribute;
	}

}
