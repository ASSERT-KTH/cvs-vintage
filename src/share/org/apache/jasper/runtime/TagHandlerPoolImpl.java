/*
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
 */

package org.apache.jasper.runtime;

import javax.servlet.jsp.tagext.Tag;
import java.util.Stack;
import java.util.EmptyStackException;
import java.util.Enumeration;
import org.apache.tomcat.util.log.Log;

/**
 * This class provides a basic implementation of TagHandlerPool.
 * Its pooling strategy is to grow the pool so that a caller
 * never has to wait for a tag handler.  Therefore in the worst
 * case, the pool size will be equal to the total number of
 * concurently used tags (that are at the same reuse scope.)  This
 * implementation does not shrink the pool.
 *
 * @author Casey Lucas <clucas@armassolutions.com>
 * @see TagPoolManagerImpl
 */
public class TagHandlerPoolImpl implements TagHandlerPool {

    /**
     * the class of tag handler that we store
     */
    Class myHandlerClass = null;

    /**
     * the collection of available handlers
     */
    Stack myHandlers = new Stack();

    /**
     * Tomcat logging mechanism
     */
    Log myLog = null;

    /**
     * Unique name for this pool
     * 
     * @see TagPoolGenerator
     */
    String myPoolName = null;

    /**
     * Keep this constructor private because we need to know what type
     * of handlers to create.
     */
    private TagHandlerPoolImpl() {
    }

    /**
     * Create a TagHandlerPoolImpl that will store objects of the given
     * class type
     *
     * @param handlerClass
     *               tag handler class
     */
    public TagHandlerPoolImpl(Class handlerClass, String poolName) {
        if (! Tag.class.isAssignableFrom(handlerClass)) {
            throw new IllegalArgumentException
                ("TagHandlerPoolImpl should only be used with Tag objects");
        }
        myHandlerClass = handlerClass;
        myPoolName = poolName;
        myLog = Log.getLog(TagPoolManagerImpl.LOG_NAME, this);
        if (myLog.getLevel() >= Log.INFORMATION) {
            myLog.log("New tag pool named '" + myPoolName + "' created to handle " +
                handlerClass.getName(), Log.INFORMATION);
        }
    }

    /**
     * Obtain a tag handler.  This implementation allocates one if one
     * is not available.  So the collection will grow with concurent
     * tag handler use.
     *
     * @return tag handler
     */
    public Tag getHandler() {
        Tag returnValue = null;

        try {
            synchronized (myHandlers) {
                if (myHandlers.empty()) {
                    if (myLog.getLevel() >= Log.DEBUG) {
                        myLog.log("Allocating new tag of type " +
                            myHandlerClass.getName(), Log.DEBUG);
                    }
                    returnValue = (Tag) myHandlerClass.newInstance();
                } else {
                    if (myLog.getLevel() >= Log.DEBUG) {
                        myLog.log("Obtaining cached tag of type " +
                            myHandlerClass.getName(), Log.DEBUG);
                    }
                    returnValue = (Tag) myHandlers.pop();
                }
            }
        }
        // Ignore these two errors.  The jsp page should get the same error
        // if it tries to allocate a tag handler.
        catch (InstantiationException exception) {
            if (myLog.getLevel() >= Log.WARNING) {
                myLog.log("Failed to allocate tag of type " + myHandlerClass.getName() +
                    ": " + exception.toString(), Log.WARNING);
            }
        }
        catch (IllegalAccessException exception) {
            if (myLog.getLevel() >= Log.WARNING) {
                myLog.log("Failed to allocate tag of type " + myHandlerClass.getName() +
                    ": " + exception.toString(), Log.WARNING);
            }
        }

        return returnValue;
    }


    /**
     * This method releases a tag handler obtained from getHandler.
     * The JSP shouls always call this method after finished using a
     * tag handler.
     * 
     * @param usedTag tag previously obtained from getHandler
     * @param removeFromPool
     *                Set to true to remove handler from pool.  One reason for this
     *                is an exception during tag processing.
     */
    public void releaseHandler(Tag usedTag, boolean removeFromPool) {
        if (myLog.getLevel() >= Log.DEBUG) {
            String message = null;
            if (removeFromPool) {
                message = "Removing tag of type " + myHandlerClass.getName() + " from cache";
            } else {
                message = "Returning tag of type " + myHandlerClass.getName() + " to cache";
            }
            myLog.log(message, Log.DEBUG);
        }
        if (removeFromPool) {
            usedTag.release();
        } else {
            synchronized (myHandlers) {
                myHandlers.push(usedTag);
            }
        }
    }

    /**
     * This method performs pool shutdown.  It will call Tag.release
     * for all of its tag handlers.
     */
    public void shutdown() {
        synchronized (myHandlers) {
            if (myLog.getLevel() >= Log.INFORMATION) {
                myLog.log("Shutting down pool '" + myPoolName + "', pool contained " +
                    myHandlers.size() + " tags", Log.INFORMATION);
            }
            Enumeration handlers = myHandlers.elements();
            while (handlers.hasMoreElements()) {
                ((Tag) handlers.nextElement()).release();
            }
            myHandlers.removeAllElements();
        }
    }
}

