/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/util/Attic/ThreadPool.java,v 1.5 2000/05/01 12:29:09 shachor Exp $
 * $Revision: 1.5 $
 * $Date: 2000/05/01 12:29:09 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */

package org.apache.tomcat.util;

import java.util.zip.*;
import java.net.*;
import java.util.*;
import java.io.*;

/**
 * A thread pool that is trying to copy the apache process management.
 *
 * @author Gal Shachor
 */
public class ThreadPool  {

    /*
     * Default values ...
     */
    public static final int MAX_THREADS = 50;
    public static final int MAX_SPARE_THREADS = 25;
    public static final int MIN_SPARE_THREADS = 10;
    public static final int WORK_WAIT_TIMEOUT = 60*1000;

    /*
     * Where the threads are held.
     */
    protected Vector pool;

    /*
     * A monitor thread that monitors the pool for idel threads.
     */
    protected MonitorRunnable monitor;


    /*
     * Max number of threads that you can open in the pool.
     */
    protected int maxThreads;

    /*
     * Min number of idel threads that you can leave in the pool.
     */
    protected int minSpareThreads;

    /*
     * Max number of idel threads that you can leave in the pool.
     */
    protected int maxSpareThreads;

    /*
     * Number of threads in the pool.
     */
    protected int currentThreadCount;

    /*
     * Number of busy threads in the pool.
     */
    protected int currentThreadsBusy;

    /*
     * Flag that the pool should terminate all the threads and stop.
     */
    protected boolean stopThePool;

    public ThreadPool() {
        maxThreads      = MAX_THREADS;
        maxSpareThreads = MAX_SPARE_THREADS;
        minSpareThreads = MIN_SPARE_THREADS;
        currentThreadCount  = 0;
        currentThreadsBusy  = 0;
        stopThePool = false;
    }

    public synchronized void start() {
        adjustLimits();

        openThreads(minSpareThreads);
        monitor = new MonitorRunnable(this);
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMinSpareThreads(int minSpareThreads) {
        this.minSpareThreads = minSpareThreads;
    }

    public int getMinSpareThreads() {
        return minSpareThreads;
    }

    public void setMaxSpareThreads(int maxSpareThreads) {
        this.maxSpareThreads = maxSpareThreads;
    }

    public int getMaxSpareThreads() {
        return maxSpareThreads;
    }

    //
    // You may wonder what you see here ... basically I am trying
    // to maintain a stack of threads. This way locality in time
    // is kept and there is a better chance to find residues of the
    // thread in memory next time it runs.
    //

    /**
     * Executes a given Runnable on a thread in the pool, block if needed.
     */
    public void runIt(Runnable r) {

        if(null == r) {
            throw new NullPointerException();
        }

        if(0 == currentThreadCount || stopThePool) {
            throw new IllegalStateException();
        }

        ControlRunnable c = null;

        // Obtain a free thread from the pool.
        synchronized(this) {
            if(currentThreadsBusy == currentThreadCount) {
                 // All threads are busy
                if(currentThreadCount < maxThreads) {
                    // Not all threads were open,
                    // Open new threads up to the max number of idel threads
                    int toOpen = currentThreadCount + minSpareThreads;
                    openThreads(toOpen);
                } else {
                    // Wait for a thread to become idel.
                    while(currentThreadsBusy == currentThreadCount) {
                        try {
                            this.wait();
                        } catch(Throwable t) {
                            t.printStackTrace();
                        }

                        // Pool was stopped. Get away of the pool.
                        if(0 == currentThreadCount || stopThePool) {
                            throw new IllegalStateException();
                        }
                    }
                }
            }

            // If we are here it means that there is a free thred. Take it.
            c = (ControlRunnable)pool.lastElement();
            pool.removeElement(c);
            currentThreadsBusy++;
        }
        c.runIt(r);
    }

    /**
     * Stop the thread pool
     */
    public synchronized void shutdown() {
        if(!stopThePool) {
            stopThePool = true;
            monitor.terminate();
            monitor = null;
            for(int i = 0 ; i < (currentThreadCount - currentThreadsBusy) ; i++) {
                try {
                    ((ControlRunnable)(pool.elementAt(i))).terminate();
                } catch(Throwable t) {
                    /* 
					 * Do nothing... The show must go on, we are shutting 
					 * down the pool and nothing should stop that.
					 */
                }
            }
            currentThreadsBusy = currentThreadCount = 0;
            pool = null;
            notifyAll();
        }
    }

    /**
     * Called by the monitor thread to harvest idel threads.
     */
    protected synchronized void checkSpareControllers() {

        if(stopThePool) {
            return;
        }
        if((currentThreadCount - currentThreadsBusy) > maxSpareThreads) {
            int toFree = currentThreadCount -
                         currentThreadsBusy -
                         maxSpareThreads;

            for(int i = 0 ; i < toFree ; i++) {
                ControlRunnable c = (ControlRunnable)pool.firstElement();
                pool.removeElement(c);
                c.terminate();
                currentThreadCount --;
            }
        }
    }

    /**
     * Returns the thread to the pool.
     * Called by threads as they are becoming idel.
     */
    protected synchronized void returnController(ControlRunnable c) {

        if(0 == currentThreadCount || stopThePool) {
            c.terminate();
            return;
        }

        currentThreadsBusy--;
        pool.addElement(c);
        notify();
    }

    /*
     * Checks for problematic configuration and fix it.
     * The fix provides reasonable settings for a single CPU
     * with medium load.
     */
    protected void adjustLimits() {
        if(maxThreads <= 0) {
            maxThreads = MAX_THREADS;
        }

        if(maxSpareThreads >= maxThreads) {
            maxSpareThreads = maxThreads;
        }

		if(maxSpareThreads <= 0) {
            if(1 == maxThreads) {
                maxSpareThreads = 1;
            } else {
                maxSpareThreads = maxThreads/2;
            }
        }

        if(minSpareThreads >  maxSpareThreads) {
            minSpareThreads =  maxSpareThreads;
		}

		if(minSpareThreads <= 0) {
            if(1 == maxSpareThreads) {
                minSpareThreads = 1;
            } else {
                minSpareThreads = maxSpareThreads/2;
            }
        }
    }

    protected void openThreads(int toOpen) {

        if(toOpen > maxThreads) {
            toOpen = maxThreads;
        }

        if(0 == currentThreadCount) {
            pool = new Vector(toOpen);
        }

        for(int i = currentThreadCount ; i < toOpen ; i++) {
            pool.addElement(new ControlRunnable(this));
        }

        currentThreadCount = toOpen;
    }

    class MonitorRunnable implements Runnable {
        ThreadPool p;
        Thread     t;
        boolean    shouldTerminate;

        MonitorRunnable(ThreadPool p) {
            shouldTerminate = false;
            this.p = p;
            t = new Thread(this);
            t.start();
        }

        public void run() {
            while(true) {
                try {
                    // Sleep for a while.
                    synchronized(this) {
                        this.wait(WORK_WAIT_TIMEOUT);
                    }

                    // Check if should terminate.
                    // termination happens when the pool is shutting down.
                    if(shouldTerminate) {
                        break;
                    }

                    // Harvest idel threads.
                    p.checkSpareControllers();

                } catch(Throwable t) {
                    t.printStackTrace();
                }
            }
        }

        public synchronized void terminate() {
            shouldTerminate = true;
            this.notify();
        }
    }

    class ControlRunnable implements Runnable {
        ThreadPool p;
        Thread     t;
        Runnable   toRun;
        boolean    shouldTerminate;
        boolean    shouldRun;

        ControlRunnable(ThreadPool p) {
            toRun = null;
            shouldTerminate = false;
            shouldRun = false;
            this.p = p;
            t = new Thread(this);
            t.start();
        }

        public void run() {
            while(true) {
                try {
                    // Wait for work.
                    synchronized(this) {
                        if(!shouldRun && !shouldTerminate) {
                            this.wait();
                        }
                    }

                    // Check if should execute a runnable.
                    try {
                        if(shouldRun) {
                            toRun.run();
                        }
                    } finally {
                        if(shouldRun) {
                            shouldRun = false;

                            // Notify the pool that the thread is now idel.
                            p.returnController(this);
                        }
                    }

                    // Check if should terminate.
                    // termination happens when the pool is shutting down.
                    if(shouldTerminate) {
                        break;
                    }

                } catch(Throwable t) {
                    t.printStackTrace();
                }
            }
        }

        public synchronized void runIt(Runnable toRun) {
            this.toRun = toRun;
            shouldRun = true;
            this.notify();
        }

        public synchronized void terminate() {
            shouldTerminate = true;
            this.notify();
        }
    }
}
