/*
 * Copyright 2003,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.struts.chain.util;

/**
 * <p>Utility methods to load application classes and create instances.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2004/03/08 02:50:54 $
 */

public final class ClassUtils {


    // ---------------------------------------------------------- Static Methods


    /**
     * <p>Return the <code>Class</code> object for the specified fully
     * qualified class name, from this web application's class loader.
     *
     * @param className Fully qualified class name
     *
     * @exception ClassNotFoundException if the specified class cannot
     *  be loaded
     */
    public static Class getApplicationClass(String className)
        throws ClassNotFoundException {

        if (className == null) {
            throw new NullPointerException("getApplicationClass called with null className");
        }

        ClassLoader classLoader =
            Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = ClassUtils.class.getClassLoader();
        }
        return (classLoader.loadClass(className));

    }


    /**
     * <p>Return a new instance of the specified fully qualified class name,
     * after loading the class (if necessary) from this web application's
     * class loader.</p>
     *
     * @param className Fully qualified class name
     *
     * @exception ClassNotFoundException if the specified class cannot
     *  be loaded
     * @exception IllegalAccessException if this class is not concrete
     * @exception InstantiationException if this class has no zero-arguments
     *  constructor
     */
    public static Object getApplicationInstance(String className)
        throws ClassNotFoundException, IllegalAccessException,
               InstantiationException {

        return (getApplicationClass(className).newInstance());

    }



}
