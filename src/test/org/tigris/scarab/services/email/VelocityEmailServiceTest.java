package org.tigris.scarab.services.email;

/* ================================================================
 * Copyright (c) 2000-2002 CollabNet.  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement: "This product includes
 * software developed by Collab.Net <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" or 
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
 * prior written permission of Collab.Net.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Collab.Net.
 */ 


import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.ResourceBundle;

import org.apache.fulcrum.TurbineServices;
import org.apache.fulcrum.localization.LocalizationService;
import org.apache.velocity.VelocityContext;
import org.tigris.scarab.test.BaseTurbineTestCase;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.util.ScarabConstants;

/**
 * A Testing Suite for the VelocityEmailService class.
 * 
 * @author <a href="mailto:epugh@opensourceconnections.com">Eric Pugh </a>
 * @version $Id: VelocityEmailServiceTest.java,v 1.1 2004/04/07 20:11:27 dep4b
 *          Exp $
 */
public class VelocityEmailServiceTest extends BaseTurbineTestCase
{

	/**
	 * @author pti
	 *
	 * MOCK object to calculate the Issue Link in the templates
	 */
	public class MockLink {
		/**
		 * Calculate a dummy test url from the issue.
		 * 
		 * @param  issue	An issue 'hashmap'
		 * @return A test url based on the issue id.
		 */
		public String getIssueIdLink(HashMap issue) {
			return "http://issue.test.org/" + issue.get("UniqueId");
		}
	}
	
	private HashMap issue;
	private HashMap user;
	private HashMap rModuleIssueType;
	private VelocityContext context;
	private VelocityEmailService ves;
	private LocalizationService locs;
	private ResourceBundle l10n;
	private MockLink link;


	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	public void setUp() throws Exception {
		super.setUp();
        
		ves = (VelocityEmailService)TurbineServices.getInstance().getService("EmailService");
		
		// Create a new context for Velocity
		context = new VelocityContext();
        
		// Get a suitable resource bundle and put it in the context
		locs = (LocalizationService)TurbineServices.getInstance().getService("LocalizationService");
		ScarabLocalizationTool slt = new ScarabLocalizationTool();
		slt.init(ScarabLocalizationTool.DEFAULT_LOCALE);
        context.put(ScarabConstants.LOCALIZATION_TOOL,slt);
        
        // Create a user mock object based on a hashmap to represent the modifier
		user = new HashMap();
		user.put("Name","tester");
		user.put("UserName", "Ted E. Ster");
		user.put("Email", "tester@test.org");

		// Create an RModuleIssueType mock object based on a hashmap
		rModuleIssueType = new HashMap();
		rModuleIssueType.put("DisplayName","Defect");

		// Create an issue mock object based on a hashmap and add it to the context
        issue = new HashMap();
        context.put("issue", issue);
		issue.put("ModifiedBy", user);
		issue.put("DefaultText", "This is the default issue text.");
		issue.put("UniqueId", "TEST1");
		issue.put("RModuleIssueType",rModuleIssueType);

		// Create a link mock object and add it to the context
		link = new MockLink();
		context.put("link", link);
		
	}
	
	/**
	 * Count the number of lines in a message 
	 * 
	 * @param msg	The email message whose lines will be counted
	 * @return		The number of lines in the message
	 * @throws IOException
	 */
	private int countLines(String msg) throws IOException {
		BufferedReader rdr = new BufferedReader(new StringReader(msg));
		int i = 0;
		while (rdr.readLine() != null) {
			i++;
		}
		return i;
	}
	
    public void testModifyIssueMessage() throws Exception {
        String msg = ves.handleRequest(context, "email/ModifyIssue.vm");
        assertNotNull("Null ModifyIssue Message Returned",msg);
        assertTrue("Unexpected few lines in ModifyIssue Message.",countLines(msg) > 10);
        assertTrue("Name expected in ModifyIssue Message.", 
        			msg.indexOf((String)user.get("Name")) > 0);
        assertTrue("UserName expected in ModifyIssue Message.", 
    			msg.indexOf((String)user.get("UserName")) > 0);
        assertTrue("Email expected in ModifyIssue Message.", 
    			msg.indexOf((String)user.get("Email")) > 0);
        assertTrue("IssueId expected in ModifyIssue Message.", 
    			msg.indexOf((String)issue.get("UniqueId")) > 0);
        assertTrue("DefaultText expected in ModifyIssue Message.", 
    			msg.indexOf((String)issue.get("DefaultText")) > 0);
        assertTrue("IssueType expected in ModifyIssue Message.", 
    			msg.indexOf((String)rModuleIssueType.get("DisplayName")) > 0);
        assertTrue("Link url to issue expected in ModifyIssue Message.", 
    			msg.indexOf(link.getIssueIdLink(issue)) > 0);
        
        System.out.println(msg);
    }
}

