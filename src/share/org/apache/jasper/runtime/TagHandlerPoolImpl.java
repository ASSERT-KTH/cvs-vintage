/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/runtime/TagHandlerPoolImpl.java,v 1.5 2004/02/23 06:26:32 billbarker Exp $
 *
 *
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.jasper.runtime;

import java.util.Enumeration;
import java.util.Stack;

import javax.servlet.jsp.tagext.Tag;

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

