package org.tigris.scarab.util.word;

/* ================================================================
 * Copyright (c) 2003 CollabNet.  All rights reserved.
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

// JDK classes

import org.apache.turbine.Turbine;
import org.tigris.scarab.om.MITList;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.util.ScarabException;

/** 
 * Creates new IssueSearch objects and acts as a regulator on the number
 * of concurrent searches.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @since 0.16.25
 * @version $Id: IssueSearchFactory.java,v 1.4 2004/05/01 19:04:30 dabbous Exp $
 */
public class IssueSearchFactory
{
    public static final IssueSearchFactory INSTANCE = new IssueSearchFactory();
    
    private final int maxInstances;
    private final int maxWait;

    /**
     * The number of objects currently in use
     */
    private int numActive = 0;

    IssueSearchFactory()
    {
        maxInstances = getMaxInstances();
        maxWait = getMaxWait();
    }

    /**
     * Maximum number of concurrent searches.  Given by the
     * scarab.concurrent.search.max property.  if not given
     * or value is negative, unlimited concurrent searches are
     * allowed (will then be limited by db connections).  A
     * value of 0 will disable searching.
     * Implementation note: this method is package-private to 
     * facilitate testing.  
     */
    int getMaxInstances()
    {
        int max = Turbine.getConfiguration()
            .getInt("scarab.concurrent.search.max", -1);
        return max;
    }

    /**
     * How long to wait (in seconds) for a search object if one is not
     * immediately available.  Given by the
     * scarab.concurrent.search.wait property.  if not given
     * or value is negative, we block till an IssueSearch can be created.
     * A value of 0 will fail immediately.
     * Implementation note: this method is package-private to 
     * facilitate testing.  
     */
    int getMaxWait()
    {
        int max = Turbine.getConfiguration()
            .getInt("scarab.concurrent.search.wait", -1);
        max *= 1000;
        return max;
    }

    public IssueSearch getInstance(Issue issue, ScarabUser searcher)
        throws Exception, MaxConcurrentSearchException
    {
        register();
        IssueSearch search = new IssueSearch(issue, searcher);
        return search;
    }

    public IssueSearch 
        getInstance(Module module, IssueType issueType, ScarabUser searcher)
        throws Exception, MaxConcurrentSearchException
    {
        register();
        IssueSearch search = new IssueSearch(module, issueType, searcher);
        return search;
    }

    public IssueSearch getInstance(MITList mitList, ScarabUser searcher)
        throws Exception, MaxConcurrentSearchException
    {
        register();
        IssueSearch search = new IssueSearch(mitList, searcher);
        return search;
    }

    void register()
        throws ScarabException, InterruptedException
    {
        if (maxInstances <= 0) 
        {
            throw new MaxConcurrentSearchException(L10NKeySet.ExceptionSearchIsNotAllowed);
        }
        else 
        {
            synchronized (this)
            {
                long starttime = System.currentTimeMillis();
                // check if we can create one
                while (numActive >= maxInstances) 
                {
                    // we can't create a new instance at this moment
                    try 
                    {
                        if (maxWait > 0)
                        {
                            wait(maxWait);
                        }
                        else if (maxWait < 0) 
                        {
                            wait();
                        } 
                        else // maxWait == 0 
                        {
                            throw MaxConcurrentSearchException.create(
                                    L10NKeySet.ExceptionMaxConcurrentSearch,
                                    ""+this.getMaxWait()
                                    );
                        }
                    }
                    catch(InterruptedException e) 
                    {
                        notify();
                        throw e; //EXCEPTION
                    }
                    if(maxWait > 0 && 
                       ((System.currentTimeMillis() - starttime) >= maxWait)) 
                    {
                        throw MaxConcurrentSearchException.create(
                                L10NKeySet.ExceptionMaxConcurrentSearch,
                                ""+this.getMaxWait()
                                );
                    }
                }    
                numActive++;
            }
        }
    }

    public void notifyDone()
    {
        if (maxInstances > 0) 
        {
            synchronized (this)
            {
                // normally always true, but false is a possibility
                if (numActive > 0) 
                {
                    numActive--;   
                }
                if (maxWait != 0) 
                {
                    this.notifyAll(); // give wait'ers a chance at it
                }
            }
        }
    }
}

