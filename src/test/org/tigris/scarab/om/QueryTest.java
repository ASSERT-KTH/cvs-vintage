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

import java.util.Iterator;
import java.util.List;

import org.apache.torque.TorqueException;
import org.tigris.scarab.test.BaseScarabOMTestCase;
import org.tigris.scarab.util.ScarabException;

/**
 * A Testing Suite for the om.Query class.
 *
 * @author <a href="mailto:mumbly@oneofus.org">Tim McNerney</a>
 * @version $Id: QueryTest.java,v 1.17 2004/04/07 20:12:22 dep4b Exp $
 */
public class QueryTest extends BaseScarabOMTestCase
{
    private Query query = null;
    private Query query1 = null;



    public void setUp() throws Exception
    {
    	super.setUp();
        query = Query.getInstance();
        query1 = Query.getInstance();

    }

    /**
     * May be screwing up data for other tests
     * @throws Exception
     */
    public void OFFtestSaveAndDelete() throws Exception
    {
        System.out.println("\ntestSave()");
        createQuery();
        //
        // Make sure the query was persisted correctly.
        //
        Query retQuery = QueryManager.getInstance(query.getQueryId());
        assertEquals(query.getName(), retQuery.getName());
        assertEquals(query.getValue(), retQuery.getValue());

        
        System.out.println("\ntestSetDeleted()");

        //
        // We expect user5 to fail in deleting since not the
        // owner. 
        //
        try
        {
            query.delete(getUser5());
            fail("Shoud have thrown an exception, user 5 is not the owner!");
        }
        catch (Exception ex)
        {
            assertTrue(ex instanceof ScarabException);
        }
        retQuery = QueryManager.getInstance(query.getQueryId(), false);
        assertTrue(!retQuery.getDeleted());
        
        // user 2 should succeed in deleting, as the owner.
        query.delete(getUser2());
        retQuery = QueryManager.getInstance(query.getQueryId(), false);
        assertTrue(retQuery.getDeleted());

    }

    /**
     * @throws TorqueException
     * @throws Exception
     */
    private void createQuery() throws TorqueException, Exception
    {
        query.setUserId(getUser2().getUserId());
        query.setName("Test query 1");
        query.setValue("&searchId=1&searchisp=asc");
        query.setDescription("Description for test query 1");
        query.setModuleId(getModule().getModuleId());
        query.setIssueType(getDefaultIssueType());
        query.setApproved(false);
        query.setScopeId(new Integer(1));
        query.save();
    }

    public void testSaveAndSendEmail() throws Exception
    {
        System.out.println("\ntestSaveAndSendEmail()");
        query1.setUserId(new Integer(2));
        query1.setName("Test query 2");
        query1.setValue("&searchId=2&searchisp=asc");
        query1.setDescription("Description for test query 2");
        query1.setModuleId(getModule().getModuleId());
        query1.setIssueType(getDefaultIssueType());
        query1.setScopeId(new Integer(1));
        query1.saveAndSendEmail(getUser1(), getModule(), null);
        //
        // Make sure the query was persisted correctly.
        //
        Query retQuery = QueryManager.getInstance(query1.getQueryId(), false);
        assertEquals(query1.getName(), retQuery.getName());
        assertEquals(query1.getValue(), retQuery.getValue());

    }
/*
    public void testGetExecuteLink() throws Exception
    {
        System.out.println("\ntestGetExecuteLink()");
        String exLink = query.getExecuteLink("dummy");
        assertEquals("dummy/template/IssueList.vm" + 
            "?action=Search&eventSubmit_doSearch=Search&resultsperpage=25" +
            "&pagenum=1&searchId=1&searchisp=asc&remcurmitl=true", exLink);
    }

    public void testGetEditLink() throws Exception
    {
        System.out.println("\ntestGetEditLink()");
        String edLink = query.getEditLink("dummy");
        assertEquals("dummy/template/EditQuery.vm?queryId=" + 
                     query.getQueryId() + 
                     "&searchId=1&searchisp=asc&remcurmitl=true", edLink);
    }
*/
    public void testGetAllQueryTypes() throws Exception
    {
        String[] scopeNames = {"personal", "module"};
        System.out.println("\ntestGetAllQueryTypes()");
        List scopes = ScopePeer.getAllScopes();
        assertEquals(scopes.size(), 2);
        Iterator it = scopes.iterator();
        Scope scope;
        for (int i = 0; it.hasNext(); i++)
        {
            scope = (Scope) it.next();
            System.out.println("getAllScopes().getName(): <" + scope.getName() + "> expected: <" + scopeNames[i] + ">");
            assertEquals(scope.getName(), scopeNames[i]);
        }
    }

    public void testApprove() throws Exception
    {
        System.out.println("\ntestSetApproved()");
        createQuery();
        //
        // We expect user5 to fail in approving and so we catch
        // the exceptions and proceed. user2 should be successful
        // in approving.
        //
        try
        {
            query.approve(getUser5(), true);
            fail("user1 should fail in approving the query");
        }
        catch (Exception ex)
        {
            assertTrue(ex instanceof ScarabException);
        }
      
        query.approve(getUser2(), true);
        assertTrue(query.getApproved());
    }

    public void testSubscribe() throws Exception
    {
        System.out.println("\ntestSubscribe()");
        createQuery();
        query.subscribe(getUser2(), new Integer(1));
        RQueryUser rqu = query.getRQueryUser(getUser2());
        query.subscribe(getUser2(), new Integer(1));
        assertTrue(rqu.getIsSubscribed());
        // Now if unsubscribed, should fail to return RQueryUser
        query.unSubscribe(getUser2());
        
            rqu = query.getRQueryUser(getUser2());
     

    }

    public void testCopy() throws Exception
    {
        Query newQuery = query.copy();
        assertEquals(newQuery.getName(), query.getName());
        assertEquals(newQuery.getUserId(), query.getUserId());
        assertEquals(newQuery.getValue(), query.getValue());
        RQueryUser rqu = query.getRQueryUser(getUser1());
        RQueryUser rquNew = newQuery.getRQueryUser(getUser1());
        assertEquals(rqu.getIsSubscribed(), rquNew.getIsSubscribed());
    }

 
}
