/*
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


package org.apache.tomcat.facade;

import org.apache.tomcat.core.*;
import org.apache.jasper.runtime.TagPoolManager;
import org.apache.jasper.runtime.TagPoolManagerImpl;

/**
 * This interceptor sets up tag pooling if it is enabled, it will
 * add a TagPoolManagerImpl to the application context.  JSPs will
 * then find it and use tag pooling.  To disable tag pooling, just
 * dont include this interceptor.
 *
 * @author Casey Lucas <clucas@armassolutions.com>
 * @see TagPoolManager
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
