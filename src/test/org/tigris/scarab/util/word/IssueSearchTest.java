package org.tigris.scarab.util.word;

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

import org.apache.torque.om.NumberKey;

import org.tigris.scarab.test.BaseTestCase;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.AttributeOptionManager;
import org.tigris.scarab.om.AttributeOption;

/**
 * A Testing Suite for the om.IssueSearch class.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: IssueSearchTest.java,v 1.2 2003/03/20 22:05:01 dlr Exp $
 */
public class IssueSearchTest extends BaseTestCase
{
    IssueSearch search;

    /**
     * Creates a new instance.
     *
     */
    public IssueSearchTest()
    {
        super(IssueSearchTest.class.getName());
    }

    public static junit.framework.Test suite()
    {
        return new IssueSearchTest();
    }

    protected void runTest()
        throws Throwable
    {
        log("Running IssueSearch tests");
        testSingleOptionAttribute();
        testWrongOptionAttribute();
        testUserWithAny();
        testUserWithCreatedBy();
        testUserWithAssignedTo();
        testSingleOptionAndUserWithAny();
    }

    private IssueSearch getSearch()
        throws Exception
    {
        Module module = getModule();
        IssueType it = getDefaultIssueType();
        IssueSearch search = new IssueSearch(module, it, getUser1());
        search.setIssueListAttributeColumns(
            module.getDefaultRModuleUserAttributes(it));
        return search;
    }

    private void testSingleOptionAttribute()
        throws Exception
    {
        IssueSearch search = getSearch();
        AttributeValue platformAV = AttributeValue
            .getNewInstance(getPlatformAttribute(), search);
        AttributeOption sgi = 
            AttributeOptionManager.getInstance(new NumberKey(21));
        platformAV.setAttributeOption(sgi);
        search.addAttributeValue(platformAV);
        List results = search.getQueryResults();
        assertTrue("Should be one result.", (results.size() == 1));
    }

    private void testWrongOptionAttribute()
        throws Exception
    {
        IssueSearch search = getSearch();
        AttributeValue platformAV = AttributeValue
            .getNewInstance(getPlatformAttribute(), search);
        AttributeOption notsgi = 
            AttributeOptionManager.getInstance(new NumberKey(20));
        platformAV.setAttributeOption(notsgi);
        search.addAttributeValue(platformAV);
        List results = search.getQueryResults();
        assertTrue("Should be no result.", (results.size() == 0));
    }

    private void testUserWithAny()
        throws Exception
    {
        IssueSearch search = getSearch();
        search.addUserCriteria(getUser5().getUserId().toString(), 
                               IssueSearch.ANY_KEY);
        List results = search.getQueryResults();
        assertTrue("Should be one result.", (results.size() == 1));
    }

    private void testUserWithCreatedBy()
        throws Exception
    {
        IssueSearch search = getSearch();
        search.addUserCriteria(getUser5().getUserId().toString(), 
                               IssueSearch.CREATED_BY_KEY);
        List results = search.getQueryResults();
        assertTrue("Should be one result.", (results.size() == 1));
    }

    private void testUserWithAssignedTo()
        throws Exception
    {
        IssueSearch search = getSearch();
        search.addUserCriteria(getUser5().getUserId().toString(), 
            getAssignAttribute().getAttributeId().toString());
        List results = search.getQueryResults();
        assertTrue("Should be no results.", (results.size() == 0));
    }

    private void testSingleOptionAndUserWithAny()
        throws Exception
    {
        IssueSearch search = getSearch();
        AttributeValue platformAV = AttributeValue
            .getNewInstance(getPlatformAttribute(), search);
        AttributeOption sgi = 
            AttributeOptionManager.getInstance(new NumberKey(21));
        platformAV.setAttributeOption(sgi);
        search.addAttributeValue(platformAV);
        search.addUserCriteria(getUser5().getUserId().toString(), 
                               IssueSearch.ANY_KEY);
        List results = search.getQueryResults();
        assertTrue("Should be one result.", (results.size() == 1));
    }
}

