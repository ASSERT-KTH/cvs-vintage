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


import org.tigris.scarab.test.BaseScarabOMTestCase;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.AttributeManager;
import org.tigris.scarab.om.AttributeOptionManager;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.util.IteratorWithSize;

/**
 * A Testing Suite for the om.IssueSearch class.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: IssueSearchTest.java,v 1.9 2004/04/07 20:12:22 dep4b Exp $
 */
public class IssueSearchTest extends BaseScarabOMTestCase
{
    private IssueSearch search;

    private int[] attributeIds = {3, 4, 6, 7, 8}; //, 9, 12};
    private int[] optionIds = {1, 8, 24, 54, 58}; //, 62, 88};

    private IssueSearch getSearch()
        throws Exception, MaxConcurrentSearchException
    {
        Module module = getModule();
        IssueType it = getDefaultIssueType();
        IssueSearch search = IssueSearchFactory.INSTANCE
            .getInstance(module, it, getUser1());
        search.setIssueListAttributeColumns(
            module.getDefaultRModuleUserAttributes(it));
        return search;
    }

    public void testSingleOptionAttribute()
        throws Exception
    {
        IssueSearch search = getSearch();
        AttributeValue platformAV = AttributeValue
            .getNewInstance(getPlatformAttribute(), search);
        AttributeOption sgi = 
            AttributeOptionManager.getInstance(new Integer(21));
        platformAV.setAttributeOption(sgi);
        search.addAttributeValue(platformAV);
        IteratorWithSize results = search.getQueryResults();
        assertTrue("Should be one result.", (results.size() == 1));
        IssueSearchFactory.INSTANCE.notifyDone();
    }

    public void testWrongOptionAttribute()
        throws Exception
    {
        IssueSearch search = getSearch();
        AttributeValue platformAV = AttributeValue
            .getNewInstance(getPlatformAttribute(), search);
        AttributeOption notsgi = 
            AttributeOptionManager.getInstance(new Integer(20));
        platformAV.setAttributeOption(notsgi);
        search.addAttributeValue(platformAV);
        IteratorWithSize results = search.getQueryResults();
        assertTrue("Should be no result.", (results.size() == 0));
        IssueSearchFactory.INSTANCE.notifyDone();
    }

    public void testUserWithAny()
        throws Exception
    {
        IssueSearch search = getSearch();
        search.addUserCriteria(getUser5().getUserId().toString(), 
                               IssueSearch.ANY_KEY);
        IteratorWithSize results = search.getQueryResults();
        assertTrue("Should be one result.", (results.size() == 1));
        IssueSearchFactory.INSTANCE.notifyDone();
    }

    public void testUserWithCreatedBy()
        throws Exception
    {
        IssueSearch search = getSearch();
        search.addUserCriteria(getUser5().getUserId().toString(), 
                               IssueSearch.CREATED_BY_KEY);
        IteratorWithSize results = search.getQueryResults();
        assertTrue("Should be one result.", (results.size() == 1));
        IssueSearchFactory.INSTANCE.notifyDone();
    }

    public void testUserWithAssignedTo()
        throws Exception
    {
        IssueSearch search = getSearch();
        search.addUserCriteria(getUser5().getUserId().toString(), 
            getAssignAttribute().getAttributeId().toString());
        IteratorWithSize results = search.getQueryResults();
        assertTrue("Should be no results.", (results.size() == 0));
        IssueSearchFactory.INSTANCE.notifyDone();
    }

    public void testUserWithAssignedToAndCreatedDate()
        throws Exception
    {
        IssueSearch search = getSearch();
        search.addUserCriteria(getUser5().getUserId().toString(), 
            getAssignAttribute().getAttributeId().toString());
        search.setMinDate("01/01/2000");
        IteratorWithSize results = search.getQueryResults();
        assertTrue("Should be no results.", (results.size() == 0));
        IssueSearchFactory.INSTANCE.notifyDone();
    }


    public void testSingleOptionAndUserWithAny()
        throws Exception
    {
        IssueSearch search = getSearch();
        AttributeValue platformAV = AttributeValue
            .getNewInstance(getPlatformAttribute(), search);
        AttributeOption sgi = 
            AttributeOptionManager.getInstance(new Integer(21));
        platformAV.setAttributeOption(sgi);
        search.addAttributeValue(platformAV);
        search.addUserCriteria(getUser5().getUserId().toString(), 
                               IssueSearch.ANY_KEY);
        IteratorWithSize results = search.getQueryResults();
        assertTrue("Should be one result.", (results.size() == 1));
        IssueSearchFactory.INSTANCE.notifyDone();
    }


    public void testLargeQuery()
        throws Exception
    {
        IssueSearch search = getSearch();
        search.setMinDate("01/01/2000"); // 1
        AttributeValue av;
        AttributeOption o;
        for (int i = 0; i < attributeIds.length; i++) 
        {
            av = AttributeValue.getNewInstance(AttributeManager.getInstance(
                 new Integer(attributeIds[i])), search);     
            o = AttributeOptionManager.getInstance(new Integer(optionIds[i]));
            av.setAttributeOption(o);
            search.addAttributeValue(av); // 6
        }
        
        search.addUserCriteria(getUser5().getUserId().toString(), 
                               IssueSearch.ANY_KEY); // 7

        search.setStateChangeFromOptionId(new Integer(2));
        search.setStateChangeToOptionId(new Integer(1)); // 8
        search.setStateChangeFromDate("01/01/2000");
        search.setStateChangeToDate("01/01/2004"); // 9

        IteratorWithSize results = search.getQueryResults();
        assertTrue("Should be no results.", (results.size() == 0));
        IssueSearchFactory.INSTANCE.notifyDone();

        av = AttributeValue.getNewInstance(getPlatformAttribute(), search);
        o = AttributeOptionManager.getInstance(new Integer(21));
        av.setAttributeOption(o);
        System.out.println("av size=" + search.getAttributeValues().size());
        search.addAttributeValue(av); // 11
        System.out.println("after av size=" + search.getAttributeValues().size());

        try 
        {
            search.getQueryResults();
            fail("Should have thrown ComplexQueryException");            
        }
        catch (ComplexQueryException e)
        {
            // expected
        }
        IssueSearchFactory.INSTANCE.notifyDone();
    }
}

