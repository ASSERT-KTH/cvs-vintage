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

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.torque.om.NumberKey;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.test.BaseScarabOMTestCase;

/**
 * A Testing Suite for the om.ScarabModule class.
 *
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @version $Id: ScarabModuleTest.java,v 1.23 2004/04/07 20:12:22 dep4b Exp $
 */
public class ScarabModuleTest extends BaseScarabOMTestCase
{
    ScarabModule newModule;
    public void setUp() throws Exception{
        super.setUp();
        newModule = (ScarabModule)ModuleManager.getInstance();
        Date d = new Date();
        newModule.setRealName("Test Module " + d.getTime());
        newModule.setOwnerId(new Integer(1));
        newModule.setParentId(new Integer(1));
        newModule.setDescription("This is the new module description");
        
     
        newModule.save();
        
    }
    
    public void tearDown() throws Exception{
        // SHOULD BE DELETING THE MODULE WE JUST CREATED!
        //ScarabModulePeer.doDelete(newModule);
        //assertFalse(ScarabModulePeer.getAllModules().contains(newModule));
        super.tearDown();
    }


    public void testGetParents() throws Exception
    {
        Module module = ModuleManager
            .getInstance(new NumberKey(7), false);
        List parents = module.getAncestors();
        Iterator itr = parents.iterator();
        while (itr.hasNext())
        {
            Module me = (Module) itr.next();
            System.out.println (me.getName());
        }
        System.out.println ("parents=" + parents.size());
    }
   

    public void testIssueTypes() throws Exception
    {
        List issueTypes = newModule.getRModuleIssueTypes();
        for (int i = 0;i<issueTypes.size();i++)
        {
            IssueType issueType = ((RModuleIssueType)issueTypes
                  .get(i)).getIssueType();
            System.out.println("ISSUE TYPE = " + issueType.getName());
            Issue issue = new Issue();
            issue.setModule(newModule);
            issue.setIssueType(issueType);

            testGetAllAttributeValuesMap(issue);
            testGetAttributeGroups(issueType);
            testGetUserAttributes(issueType);
        }
    }

    public void testGetAttributeGroups(IssueType issueType) 
        throws Exception
    {
        System.out.println ("testGetAttributeGroups()");
        List attrGroups = issueType.getAttributeGroups(newModule, true);
        for (int i=0;i<attrGroups.size(); i++)
        {
            AttributeGroup group = (AttributeGroup)attrGroups.get(i);
            System.out.println("attribute group = " + group.getName());
        }
    }


    public void testGetUsers() throws Exception
    {
        ScarabUser[] users = newModule.getUsers(ScarabSecurity.ISSUE__VIEW);
        System.out.println(users);
    }

    public void testGetAllAttributeValuesMap(Issue issue) throws Exception
    {
        System.out.println ("testGetAllAttributeValuesMap()");
        Map attrMap = issue.getAllAttributeValuesMap();
        System.out.println ("getAllAttributeValuesMap().size(): " 
                             + attrMap.size());
        assertEquals (getExpectedSize(issue.getIssueType()), attrMap.size());
        Iterator iter = attrMap.keySet().iterator();
        Attribute attr = null;
        while (iter.hasNext())
        {
            attr = ((AttributeValue)attrMap.get(iter.next()))
                             .getAttribute();
            if (attr.isOptionAttribute())
            {
                int expectedSize = 0;
                switch (Integer.parseInt(attr.getAttributeId().toString()))
                {
                    case 3: expectedSize = 7;break;
                    case 4: expectedSize = 8;break;
                    case 5: expectedSize = 8;break;
                    case 6: expectedSize = 52;break;
                    case 7: expectedSize = 4;break;
                    case 8: expectedSize = 4;break;
                    case 9: expectedSize = 10;break;
                    case 12: expectedSize = 3;break;
                }
                assertTrue(expectedSize >0);
            }
        }
    }

    public void testGetUserAttributes(IssueType issueType)
        throws Exception
    {
        System.out.println ("testGetUserAttributes");
        List attrs =  newModule.getUserAttributes(issueType);
        assertEquals (2, attrs.size());
    }

             
    private int getExpectedSize(IssueType issueType) throws Exception

    {
        int expectedSize = 0;
        switch (Integer.parseInt(issueType.getIssueTypeId().toString()))
        {
            case 1: expectedSize = 12;break;
            case 3: expectedSize = 11;break;
            case 5: expectedSize = 9;break;
            case 7: expectedSize = 9;break;
            case 9: expectedSize = 9;break;
        }
        return expectedSize;
    }
    
    
}
