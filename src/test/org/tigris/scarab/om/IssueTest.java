package org.tigris.scarab.om;

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

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.tigris.scarab.test.BaseTestCase;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.Module;
import org.apache.torque.om.NumberKey;


/**
 * A Testing Suite for the om.Issue class.
 *
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @version $Id: IssueTest.java,v 1.16 2002/10/28 22:00:33 jon Exp $
 */
public class IssueTest extends BaseTestCase
{
    private List issueList = new ArrayList();;

    /**
     * Creates a new instance.
     *
     */
    public IssueTest()
    {
        super("IssueTest");
    }

    public static junit.framework.Test suite()
    {
        return new IssueTest();
    }

    protected void runTest()
        throws Throwable
    {
        createTestIssues(); 
        loopThruTestIssues();
        testAssignIssue();
        testGetUserAttributeValues();
        testGetEligibleUsers();
        testGetUsersToEmail();
    }
    
    private void createTestIssues() throws Exception
    {
        // loops thru module and issue type combinations
        // creates an issue in each combination
        for (int i = 1;i<nbrDfltModules+1;i++)
        {
            for (int j = 1;j<nbrDfltIssueTypes+1;j++)
            {
                Module module = ScarabModulePeer
                    .retrieveByPK(new NumberKey(Integer.toString(i)));
                IssueType issueType = IssueTypePeer
                    .retrieveByPK(new NumberKey(Integer.toString(j)));
                Issue issue = Issue.getNewInstance(module, issueType);
                issueList.add(issue);
            }
        }
    }

    private void loopThruTestIssues() throws Exception
    {
        for (int i = 1;i<issueList.size();i++)
        {
            Issue issue = (Issue)issueList.get(i);
            System.out.println("MODULE=" + issue.getModule().getName());
            System.out.println("ISSUE TYPE = " + issue.getIssueType()
                                                      .getName());
            testGetUniqueId(issue);
            testGetAllAttributeValuesMap(issue);
         }
    }

    private void testGetUniqueId(Issue issue) throws Exception
    {
        String strUniqueID = null;
        strUniqueID = issue.getUniqueId();
        System.out.println ("Unique id: " + strUniqueID);   
    }

    private void testGetAllAttributeValuesMap(Issue issue) throws Exception
    {
        System.out.println ("testGetAllAttributeValuesMap()");
        HashMap map = issue.getAllAttributeValuesMap();
        System.out.println ("getAllAttributeValuesMap().size(): " + map.size());
        int expectedSize = 11;
        switch (Integer.parseInt(issue.getTypeId().toString()))
        {
            case 1: expectedSize = 11;break;
            case 2: expectedSize = 11;break;
            case 3: expectedSize = 10;break;
            case 4: expectedSize = 10;break;
            case 5: expectedSize = 8;break;
            case 6: expectedSize = 8;break;
            case 7: expectedSize = 8;break;
            case 8: expectedSize = 8;break;
            case 9: expectedSize = 8;break;
            case 10: expectedSize = 8;
        }
        assertEquals (expectedSize, map.size());
    }

    private void testAssignIssue() throws Exception
    {
        System.out.println ("testAssignUser()");
        Attribute attribute = getAssignAttribute();
        ScarabUser assignee = getUser2();
        ScarabUser assigner = getUser1();
        String attachmentText =  "User " + assigner.getUserName()
                              + " has added user "
                              + assignee.getUserName() + " to "
                              + attribute.getName() + ".";
        getIssue0().assignUser(getUser1(),getUser2(), attachmentText, 
                               attribute, "test reason");
    }
               
    private void testGetUserAttributeValues() throws Exception
    {
        System.out.println ("testAssociatedUsers()");
        List attVals = getIssue0().getUserAttributeValues();
        AttributeValue attVal = (AttributeValue)attVals.get(0);
        assertEquals(attVal.getAttributeId().toString(), "2");
    }

    private void testGetEligibleUsers() throws Exception
    {
        System.out.println ("testGetEligibleUsers()");
        List users = getIssue0().getEligibleUsers(getAssignAttribute());
        assertEquals(users.size(), 5);
    }

    private void testGetUsersToEmail() throws Exception
    {
        System.out.println ("testGetUsersToEmail()");
        List users = getIssue0().getUsersToEmail(AttributePeer.EMAIL_TO);
        assertEquals(users.size(), 2);
    }
}
