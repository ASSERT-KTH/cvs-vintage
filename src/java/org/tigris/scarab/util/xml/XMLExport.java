package org.tigris.scarab.util.xml;

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
import java.util.ArrayList;
import java.util.Iterator;

import org.tigris.scarab.om.Issue.FederatedId;
import org.apache.commons.util.StringUtils;

/**
 * @author <a href="mailto:kevin.minshull@bitonic.com">Kevin Minshull</a>
 * @version $Id: XMLExport.java,v 1.6 2002/02/14 00:48:18 jon Exp $
 */
public class XMLExport
{
    /**
     * Parses a list of issues.
     *
     * @param issueList a comma separated list of federated id's and federated id ranges.
     * @return an <code>ArrayList</code> of <code>FederatedId</code>
     */
    public List parseIssueList(String issueList)
        throws Exception
    {
        String[] issues = StringUtils.split(issueList, ",");
        int resultsSize = issues.length;
        ArrayList results = new ArrayList(resultsSize);
        for (int i = 0; i < issues.length; i++)
        {
            if (issues[i].indexOf("-") == -1)
            {
                addFederatedId(results, issues[i]);
            }
            else
            {
                String[] issue = StringUtils.split(issues[i], "-");
                if (issue.length != 2)
                {
                    throw new Exception("Federated id range not valid: " + issues[i]);
                }
                FederatedId fidStart = createFederatedId(issue[0]);
                FederatedId fidStop = createFederatedId(issue[1]);
                if (!fidStart.getPrefix().equalsIgnoreCase(fidStop.getPrefix()))
                {
                    throw new Exception("Federated id prefix does not match: " + issues[i]);
                }
                if (fidStart.getCount() > fidStop.getCount())
                {
                    throw new Exception("Federated id range not valid: " + issues[i]);
                }
                resultsSize += fidStop.getCount() - fidStart.getCount() + 1;
                results.ensureCapacity(resultsSize);
                addFederatedId(results, fidStart);
                for (int j = fidStart.getCount() + 1; j < fidStop.getCount(); j++)
                {
                    addFederatedId(results, fidStart.getPrefix() + j);
                }
                if (!fidStop.getPrefix().equals(fidStart.getPrefix()) ||
                    fidStop.getCount() != fidStart.getCount())
                {
                    addFederatedId(results, fidStop);
                }
            }
        }
        
        return results;
    }
    
    /**
     * Catches and rethrows parsing errors when creating the federated id.
     */
    private static FederatedId createFederatedId(String id)
        throws Exception
    {
        FederatedId fid = null;
        try
        {
            fid = new FederatedId(id.trim());
        }
        catch (Exception e)
        {
            throw new Exception("Invaild federated id: " + id);
        }
        return fid;
    }
    
    /**
     * Adds the specified federated id to the array list
     */
    private static void addFederatedId(ArrayList al, String id)
        throws Exception
    {
        addFederatedId(al, createFederatedId(id));
    }
    
    /**
     * Adds the specified federated id to the array list
     */
    private static void addFederatedId(ArrayList al, FederatedId fid)
        throws Exception
    {
        String fidPrefix = fid.getPrefix();
        int fidCount = fid.getCount();
        Iterator iter = al.iterator();
        while (iter.hasNext())
        {
            FederatedId test = (FederatedId)iter.next();
            if (test.getPrefix() == null)
            {
                throw new Exception ("Invalid prefix code for id: " + 
                    test.getCount() + 
                    " Did you remember to enter the Module prefix code?");
            }
            else if (test.getCount() <= 0)
            {
                throw new Exception ("Invalid federated id number. " + 
                    "Cannot have a number less than or equal to zero.");
            }
            if (test.getPrefix().equals(fidPrefix) && test.getCount() == fidCount)
            {
                throw new Exception("Federated id already specified: " + 
                    fid.getPrefix() + fid.getCount());
            }
        }
        al.add(fid);
    }
}
