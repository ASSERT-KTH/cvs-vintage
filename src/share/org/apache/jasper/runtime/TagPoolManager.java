/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/runtime/TagPoolManager.java,v 1.4 2004/02/23 06:26:32 billbarker Exp $
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
 * @see org.apache.tomcat.facade.TagPoolManagerInterceptor
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
     * @return the pool that should be used for this poolName and handlerClass
     */
    public TagHandlerPool getPool(String poolName, Class handlerClass);

    /**
     * This method allows the pool manager to shutdown all of its
     * pools.  Normally, this involves calling relase for all
     * its tag handlers.
     */
    public void shutdown();
}

