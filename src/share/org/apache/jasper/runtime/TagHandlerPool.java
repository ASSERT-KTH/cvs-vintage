/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/runtime/TagHandlerPool.java,v 1.4 2004/02/23 06:26:32 billbarker Exp $
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

import javax.servlet.jsp.tagext.Tag;

/**
 * This interface allows pooling of tag handlers.
 *
 * @author Casey Lucas <clucas@armassolutions.com>
 * @see TagPoolManager
 */
public interface TagHandlerPool {

    /**
     * This method is called by JSPs to obtain a tag handler.
     *
     * @return Tag handler appropriate for this pool
     */
    public Tag getHandler();

    /**
     * This method is called by JSPs when they are finished using a
     * tag handler obtained from getHandler
     * 
     * @param usedTag
     * @param removeFromPool
     *                Set to true if this handler should be removed from the pool.
     *                This might occur if an exception is thrown during tag usage.
     */
    public void releaseHandler(Tag usedTag, boolean removeFromPool);

    /**
     * This method is called to shutdown this pool.  It is normally
     * called by TagPoolManager.shutdown.  It should perform cleanup
     * and call Tag.release for any stored tags.
     */
    public void shutdown();
}

