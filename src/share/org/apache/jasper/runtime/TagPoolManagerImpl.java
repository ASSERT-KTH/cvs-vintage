/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/runtime/TagPoolManagerImpl.java,v 1.4 2004/02/23 06:26:32 billbarker Exp $
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

import java.util.Hashtable;
import java.util.Enumeration;
import org.apache.tomcat.util.log.Log;

/**
 * This class provides a basic implementation for TagPoolManager.
 * It simply manages a collection of named pools, including their
 * retrieval and cleanup.
 *
 * @author Casey Lucas <clucas@armassolutions.com>
 * @see org.apache.tomcat.facade.TagPoolManagerInterceptor
 */
public class TagPoolManagerImpl implements TagPoolManager {

    /**
     * the collection of tag pools
     */
    Hashtable myPools = new Hashtable();

    /**
     * logging capabilities
     */
    Log myLog = null;


    /**
     * This constant is the log named that can be used in configuration
     * files to enable logging.
     */
    public static final String LOG_NAME = "tag_pool_log";


    /**
     * This constructor just sets up logging.
     */
    public TagPoolManagerImpl() {
        myLog = Log.getLog(LOG_NAME, this);
    }

    /**
     * Obtain a pool by the given name that provides handlers for
     * the given class.  If no pool is available for the given name
     * then allocate a new one and return it.
     *
     * @param poolName name of the requested pool
     * @param handlerClass
     *                 class of the tag handlers
     * @return named pool
     */
    public TagHandlerPool getPool(String poolName, Class handlerClass) {
        TagHandlerPool returnValue = null;
        synchronized (myPools) {
            returnValue = (TagHandlerPool) myPools.get(poolName);
            if (returnValue == null) {
                returnValue = new TagHandlerPoolImpl(handlerClass, poolName);
                myPools.put(poolName, returnValue);
            }

            if (myLog.getLevel() >= Log.INFORMATION) {
                myLog.log("Getting pool named '" +
                    poolName + "' for tag handler class '" +
                    handlerClass.getName() + "'", Log.INFORMATION);
                myLog.log("Number of pools is now " + myPools.size(), Log.INFORMATION);
            }
        }

        return returnValue;
    }


    /**
     * This method is called when the tag pools should be shutdown.  It
     * calls shutdown for each of the tag pools.
     */
    public void shutdown() {
        synchronized (myPools) {
            if (myLog.getLevel() >= Log.INFORMATION) {
                myLog.log("Shutting down " + myPools.size() + " pools", Log.INFORMATION);
            }

            Enumeration pools = myPools.elements();
            while (pools.hasMoreElements()) {
                ((TagHandlerPool) pools.nextElement()).shutdown();
            }
        }
    }

}

