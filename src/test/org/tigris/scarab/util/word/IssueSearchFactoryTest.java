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

import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.test.BaseScarabOMTestCase;

/**
 * A Testing Suite for the util.word.IssueSearchFactory class.  This class
 * includes concurrency tests, which have timings on a much shorter time
 * scale than is possible in production.  Attempts to shorten the maxWait
 * even further causes tests to fail, just because threads to not necessarily
 * wake up immediately on the notifyAll signal.  if failures are seen, try
 * adjusting maxWait used here up before assuming the code is broken.  Max
 * wait in production is measured in seconds, values in this class are
 * given in millis.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: IssueSearchFactoryTest.java,v 1.5 2004/04/07 20:12:22 dep4b Exp $
 */
public class IssueSearchFactoryTest extends BaseScarabOMTestCase
{
    private IssueSearch search;

  
    /**
     * Testing the factory's limit using one thread
     * can't test maxWait this way.
     */
    public void testSingleThread()
        throws Exception
    {
        IssueSearchFactory issueSearchFactory = new IssueSearchFactory()
            {
                int getMaxInstances()
                {
                    return 5;
                }
                int getMaxWait()
                {
                    return 0;
                }
            };

        Module module = getModule();
        IssueType it = getDefaultIssueType();
        ScarabUser user = getUser1();

        IssueSearch[] isa = new IssueSearch[5];
        for (int i=0; i<5; i++) 
        {
            isa[i] = issueSearchFactory.getInstance(module, it, user);
        }
        
        // try to get one more than max
        try 
        {
            issueSearchFactory.getInstance(module, it, getUser1());
            fail("Created more than maxInstances");
        }
        catch (MaxConcurrentSearchException expected)
        {
        }

        // let the factory know we are done
        issueSearchFactory.notifyDone();

        // try again should work this time
        try 
        {
            issueSearchFactory.getInstance(module, it, getUser1());
        }
        catch (MaxConcurrentSearchException failure)
        {
            fail("Could not create new instance after returning one.");
        }
    }

    /**
     * I can't seem to grok this one.  I know I should have paid more attention to 
     * threads in Java 101.
     * @throws Exception
     */
    public void OFFtestConcurrency()
        throws Exception 
    {
        String message = multipleThreads(1);
        assertTrue(message, message.length() == 0);
        message = multipleThreads(2 * 50); // 2 * maxWait
        assertTrue("Didn't timeout. " + message, 
                   message.startsWith("Exception: ")); 
    }

    private String multipleThreads(final int holdTime) 
        throws Exception 
    {
        final long startTime = System.currentTimeMillis();

        final StringBuffer sb = new StringBuffer(20);

        final ISFactoryTest[] pts = new ISFactoryTest[2 * 5]; // 2 * maxActive
        final ThreadGroup threadGroup = new ThreadGroup("foo") 
            {
                public void uncaughtException(Thread t, Throwable e) 
                {
                    for (int i = 0; i < pts.length; i++) 
                    {
                        pts[i].stop();
                    }

                    sb.append("Exception: " + e.getMessage());
                }
            };

        // would like to use variables here but an inner class does not
        // initialize its version of the local variable until after the
        // ctor is executed and these methods are called from the ctor
        // we have to hardcode.
        IssueSearchFactory issueSearchFactory = new IssueSearchFactory()
            {

                int getMaxInstances()
                {
                    return 5;
                }
                int getMaxWait()
                {
                    return 50;
                }
            };

        Module module = getModule();
        IssueType it = getDefaultIssueType();
        ScarabUser user = getUser1();


        for (int i = 0; i < pts.length; i++)
        {
            pts[i] = new ISFactoryTest(threadGroup, holdTime, 
                                       issueSearchFactory,
                                       module, it, user);
        }

        // let threads run for a bit
        Thread.sleep(1000);
        for (int i = 0; i < pts.length; i++) 
        {
            pts[i].stop();
        }

        // check for deadlock, give threads time to complete an iteration
        Thread.sleep(200);
        for (int i = 0; i < pts.length; i++) 
        {
            //System.out.println(pts[i].getState());
            if (!pts[i].getState().startsWith("Stopped")) 
            {
                sb.append("Possible deadlock. First try increasing sleep time.");
            }
        }

        long time = System.currentTimeMillis() - startTime;
        System.out.println("Multithread test time = " + time + " ms");
        return sb.toString();
    }

    private static int currentThreadCount = 0;

    private class ISFactoryTest implements Runnable
    {
        /**
         * The number of milliseconds to hold the object
         */
        private final int isHoldTime;
        private final Module module;
        private final IssueType it;
        private final ScarabUser user;
        private final IssueSearchFactory isFactory;

        private boolean isRun;

        private String state;
        private int runCounter;

        protected ISFactoryTest(ThreadGroup threadGroup, int isHoldTime, 
                                IssueSearchFactory isFactory, 
                                Module module, IssueType it, ScarabUser user) 
        {
            this.isHoldTime = isHoldTime;
            this.module = module;
            this.it = it;
            this.user = user;
            this.isFactory = isFactory;
            Thread thread = new Thread(threadGroup, this, 
                                       "Thread+" + currentThreadCount++);
            thread.setDaemon(false);
            thread.start();
        }

        public void run()
        {
            isRun = true;
            runCounter = 0;
            while (isRun) 
            {
                runCounter++; 
                try 
                {
                    IssueSearch is = null;
                    state = "Getting IS";
                    is = isFactory.getInstance(module, it, user);
                    state = "Using IS";
                    assertTrue(null != is);
                    Thread.sleep(isHoldTime);
                    state = "Returning IS";
                    isFactory.notifyDone();
                    // if we let this thread immediately enter into competition
                    // for the lock on the factory, it sometimes beats those
                    // that were waiting.  
                    Thread.sleep(10);
                } 
                catch (RuntimeException e) 
                {
                    throw e;
                } 
                catch (Exception e) 
                {
                    throw new RuntimeException(e.toString());
                }
            }
            state = "Stopped";
        }

        public void stop()
        {
            isRun = false;
        }
        
        public String getState()
        {
            return state + "; ran " + runCounter + " times";
        }
    }
}

