package org.tigris.scarab.om;

/* ================================================================
 * Copyright (c) 2000-2001 CollabNet.  All rights reserved.
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

import org.apache.torque.om.NumberKey;
import org.tigris.scarab.test.BaseTestCase;
import org.tigris.scarab.util.ScarabException;

import java.util.Iterator;
import java.util.List;

/**
 * A Testing Suite for the om.Query class.
 *
 * @author <a href="mailto:mumbly@oneofus.org">Tim McNerney</a>
 * @version $Id: QueryTest.java,v 1.4 2001/10/24 06:10:59 jon Exp $
 */
public class QueryTest extends BaseTestCase
{
    private Query query = null;
    private Query query1 = null;

    /**
     * Creates a new instance.
     *
     */
    public QueryTest()
    {
        super("QueryTest");
    }

    public static junit.framework.Test suite()
    {
        return new QueryTest();
    }

    protected void runTest()
            throws Throwable
    {
        query = Query.getInstance();
        query1 = Query.getInstance();

        testGetAllQueryTypes();
        testSave();
        testSaveAndSendEmail();
        testGetExecuteLink();
        testGetEditLink();
        testApprove();
        testDelete();
    }

    private void testSave() throws Exception
    {
        System.out.println("\ntestSave()");
// FIXME: what should this be now?
//        query.setTypeId(Query.USER__PK);
        query.setUserId(new NumberKey(2));
        query.setName("Test query 1");
        query.setValue("&searchId=1&searchisp=asc");
        query.setDescription("Description for test query 1");
        query.setModuleId(getModule().getModuleId());
        query.setApproved(false);
        query.save();
        //
        // Make sure the query was persisted correctly.
        //
        Query retQuery = (Query) QueryPeer.retrieveByPK(query.getQueryId());
        assertEquals(query.getName(), retQuery.getName());
        assertEquals(query.getValue(), retQuery.getValue());

    }

    private void testSaveAndSendEmail() throws Exception
    {
        System.out.println("\ntestSaveAndSendEmail()");
// FIXME: what should this be now?
//        query1.setTypeId(Query.GLOBAL__PK);
        query1.setUserId(new NumberKey(2));
        query1.setName("Test query 2");
        query1.setValue("&searchId=2&searchisp=asc");
        query1.setDescription("Description for test query 2");
        query1.setModuleId(getModule().getModuleId());
        query1.saveAndSendEmail(getUser1(), getModule(), null);
        //
        // Make sure the query was persisted correctly.
        //
        Query retQuery = (Query) QueryPeer.retrieveByPK(query1.getQueryId());
        assertEquals(query1.getName(), retQuery.getName());
        assertEquals(query1.getValue(), retQuery.getValue());

    }

    private void testGetExecuteLink() throws Exception
    {
        System.out.println("\ntestGetExecuteLink()");
        String exLink = query.getExecuteLink("dummy");
        assertEquals(exLink, "dummy/template/IssueList.vm?action=Search&eventSubmit_doSearch=Search&resultsperpage=25&pagenum=1&searchId=1&searchisp=asc");
    }

    private void testGetEditLink() throws Exception
    {
        System.out.println("\ntestGetEditLink()");
        String edLink = query.getEditLink("dummy");
        assertEquals(edLink, "dummy/template/EditQuery.vm?queryId=" + query.getQueryId() + "&searchId=1&searchisp=asc");
    }

    private void testGetAllQueryTypes() throws Exception
    {
        String[] typeNames = {"Personal profile", "All users"};
        System.out.println("\ntestGetAllQueryTypes()");
// FIXME: what should this be now?
//        List types = query.getAllQueryTypes();
//        System.out.println("getAllQueryTypes().size(): " + types.size() + " expected: 2");
//        assertEquals(types.size(), 2);
//        Iterator it = types.iterator();
// FIXME: what should this be now?
/*
        QueryType qt;
        for (int i = 0; it.hasNext(); i++)
        {
            qt = (QueryType) it.next();
            System.out.println("getAllQueryTypes().getName(): <" + qt.getName() + "> expected: <" + typeNames[i] + ">");
            assertEquals(qt.getName(), typeNames[i]);
        }
*/
    }

    private void testApprove() throws Exception
    {
        boolean caught = false;
        System.out.println("\ntestSetApproved()");

        //
        // We expect user2 to fail in approving and so we catch
        // the exceptions and proceed. user1 should be successful
        // in approving.
        //
        try
        {
            query.approve(getUser2(), true);
        }
        catch (ScarabException ex)
        {
            caught = true;
        }
        assert(caught);
        caught = false;
        query.approve(getUser1(), true);
        assert(query.getApproved());
    }

    private void testDelete() throws Exception
    {
        boolean caught = false;
        System.out.println("\ntestSetDeleted()");

        //
        // We expect user2 to fail in deleting since not the
        // owner. user1 should be successful in deleting.
        //
        try
        {
            query.delete(getUser2());
        }
        catch (ScarabException ex)
        {
            caught = true;
        }
        Query retQuery = (Query) QueryPeer.retrieveByPK(query.getQueryId());
        assert(!retQuery.getDeleted());
        assert(caught);
        query.delete(getUser1());
        retQuery = (Query) QueryPeer.retrieveByPK(query.getQueryId());
        assert(retQuery.getDeleted());
    }
}
