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

import java.util.List;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.torque.om.NumberKey;

import org.tigris.scarab.om.ScarabModulePeer;
import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.services.module.ModuleManager;

import org.tigris.scarab.test.BaseTestCase;

/**
 * A Testing Suite for the om.ScarabModule class.
 *
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @version $Id: ScarabModuleTest.java,v 1.6 2002/03/12 19:27:42 elicia Exp $
 */
public class ScarabModuleTest extends BaseTestCase
{
    /**
     * Creates a new instance.
     *
     */
    public ScarabModuleTest()
    {
        super("ScarabModuleTest");
    }

    public static junit.framework.Test suite()
    {
        return new ScarabModuleTest();
    }

    protected void runTest()
        throws Throwable
    {
        testGetParents();
        testCreateNew();
    }
    
    private void testGetParents() throws Exception
    {
        log("testGetParents()");
        ModuleEntity module = (ModuleEntity) ScarabModulePeer.retrieveByPK(new NumberKey(7));
        List parents = module.getAncestors();
        Iterator itr = parents.iterator();
        while (itr.hasNext())
        {
            ModuleEntity me = (ModuleEntity) itr.next();
            System.out.println (me.getName());
        }
        System.out.println ("parents=" + parents.size());
    }

    private void testCreateNew() throws Exception
    {
        log("testCreateNew()");
        ModuleEntity me = ModuleManager.getInstance();
        me.setRealName("New Module");
        me.setOwnerId(new NumberKey(1));
        me.setParentId(new NumberKey(1));
        me.setDescription("This is the new module description");
        me.save();
        testInitialData(me);
    }


    private void testInitialData(ModuleEntity me) throws Exception
    {
        ScarabModule module = (ScarabModule) me;
        List issueTypes = module.getRModuleIssueTypes();
        for (int i = 0;i<issueTypes.size();i++)
        {
            IssueType issueType = ((RModuleIssueType)issueTypesi
                  .get(i)).getIssueType();
            System.out.println("ISSUE TYPE = " + issueType.getName());
            Issue issue = new Issue();
            issue.setModule(me);
            issue.setIssueType(issueType);
            testGetAllAttributeValuesMap(issue);
            testGetAttributeGroups(module, issueType);
        }
    }

    private void testGetAllAttributeValuesMap(Issue issue) throws Exception
    {
        System.out.println ("testGetAllAttributeValuesMap()");
        HashMap attrMap = issue.getAllAttributeValuesMap();
        System.out.println ("getAllAttributeValuesMap().size(): " 
                             + attrMap.size());
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
        assertEquals (expectedSize, attrMap.size());
        Iterator iter = attrMap.keySet().iterator();
        while (iter.hasNext())
        {
            Attribute attr = ((AttributeValue)attrMap.get(iter.next()))
                             .getAttribute();
            List attrOptions = attr.getAttributeOptions();
            if (attr.isOptionAttribute())
            {
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
            }
        }
    }

    private void testGetAttributeGroups(ScarabModule module, 
                                        IssueType issueType) 
        throws Exception
    {
        System.out.println ("testGetAttributeGroups()");
        List attrGroups = module.getAttributeGroups(issueType);
        for (int i=0;i<attrGroups.size(); i++)
        {
            AttributeGroup group = (AttributeGroup)attrGroups.get(i);
            System.out.println("attribute group = " + group.getName());
        }
    }
             
}
