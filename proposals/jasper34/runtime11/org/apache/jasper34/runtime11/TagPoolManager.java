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

package org.apache.jasper34.runtime11;


/**
 * This interface provides methods for tag handler pooling.
 * It specifies management of pools of tag handlers.  Normally,
 * one TagPoolManager derivative is stored per application
 * context.  This allows tag pooling on a per web application
 * scope.<br>
 *
 * TagPoolManagers manage TagHandlerPools.  TagHandlerPools
 * are uniquely named per reuse scope.  The current JSP spec
 * allows for tag reuse if all of these conditions are met:
 * <ul>
 * <li>tag scope doesnt conflict
 * <li>tags are of the same type
 * <li>tags use the same set of attributes
 * </ul>
 *
 * @author Casey Lucas <clucas@armassolutions.com>
 * @see TagPoolManagerInterceptor
 */
public interface TagPoolManager {

    /**
     * This constant is the name of the TagPoolManager attribute
     * stored into each ServletContext (if tag pooling is enabled.)
     */
    public static final String CONTEXT_ATTRIBUTE_NAME = "org.apache.jasper.runtime.TagPoolManager";

    /**
     * Obtain a named pool.  Each uniquely named pool holds tag
     * handlers.
     *
     * @param poolName unique name of the tag pool
     * @param handlerClass
     *                 the type of tag handler objects stored by the pool
     * @return
     */
    public TagHandlerPool getPool(String poolName, Class handlerClass);

    /**
     * This method allows the pool manager to shutdown all of its
     * pools.  Normally, this involves calling relase for all
     * its tag handlers.
     */
    public void shutdown();
}

