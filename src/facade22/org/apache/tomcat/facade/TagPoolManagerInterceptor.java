/*
 * $Header: /tmp/cvs-vintage/tomcat/src/facade22/org/apache/tomcat/facade/TagPoolManagerInterceptor.java,v 1.5 2004/02/23 06:06:13 billbarker Exp $
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

package org.apache.tomcat.facade;

import org.apache.jasper.runtime.TagPoolManager;
import org.apache.jasper.runtime.TagPoolManagerImpl;
import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.TomcatException;

/**
 * This interceptor sets up tag pooling if it is enabled, it will
 * add a TagPoolManagerImpl to the application context.  JSPs will
 * then find it and use tag pooling.  To disable tag pooling, just
 * dont include this interceptor.
 *
 * @author Casey Lucas <clucas@armassolutions.com>
 * @see org.apache.jasper.runtime.TagPoolManager
 */
public class TagPoolManagerInterceptor extends BaseInterceptor {

    /**
     * This hook is called when an application context is initialized.
     * Here, we add a TagPoolManagerImpl to the application context.
     *
     * @param ctx
     * @exception TomcatException
     */
    public synchronized void contextInit(Context ctx) throws TomcatException {
        if (debug>0) {
            log("Adding TagPoolManagerImpl: " + ctx);
        }
        TagPoolManager manager = (TagPoolManager) ctx.getAttribute(TagPoolManager.CONTEXT_ATTRIBUTE_NAME);
        if (manager != null) {
            if (debug>0) {
                log("TagPoolManagerImpl already exists for: " + ctx);
            }
        } else {
            manager = new TagPoolManagerImpl();
            ctx.setAttribute(TagPoolManager.CONTEXT_ATTRIBUTE_NAME, manager);
        }
    }

    /**
     * This hook is called when an application context is shutdown. Here,
     * the TagPoolManagerImpl is removed from the application context.
     *
     * @param ctx
     * @exception TomcatException
     */
    public synchronized void contextShutdown(Context ctx) throws TomcatException {
        if (debug>0) {
            log("Removing TagPoolManagerImpl: " + ctx);
        }
        TagPoolManager manager = (TagPoolManager) ctx.getAttribute(TagPoolManager.CONTEXT_ATTRIBUTE_NAME);
        if (manager != null) {
            ctx.removeAttribute(TagPoolManager.CONTEXT_ATTRIBUTE_NAME);
            manager.shutdown();
        } else {
            if (debug>0) {
                log("TagPoolManagerImpl not found for: " + ctx);
            }
        }
    }

}
