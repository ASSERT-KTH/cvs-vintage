/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/TagCache.java,v 1.3 2004/02/23 02:45:12 billbarker Exp $
 * $Revision: 1.3 $
 * $Date: 2004/02/23 02:45:12 $
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
 *  See the License for the specific language 
 */

package org.apache.jasper.compiler;

import java.util.Hashtable;

import java.lang.reflect.Method;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;

import org.apache.jasper.JasperException;
import org.apache.jasper.Constants;

/**
 * A simple cache to hold results of one-time evaluation for a custom 
 * tag. 
 *
 * @author Anil K. Vijendran (akv@eng.sun.com)
 */
public class TagCache {
    String shortTagName;
    Hashtable methodMaps;
    BeanInfo tagClassInfo;
    Class tagHandlerClass;
    
    TagCache(String shortTagName) {
        this.shortTagName = shortTagName;
        this.methodMaps = new Hashtable();
    }

    private void addSetterMethod(String attrName, Method m) {
        methodMaps.put(attrName, m);
    }
    
    Method getSetterMethod(String attrName) {
        return (Method) methodMaps.get(attrName);
    }

    void setTagHandlerClass(Class tagHandlerClass) 
        throws JasperException
    {
        try {
            this.tagClassInfo = Introspector.getBeanInfo(tagHandlerClass);
            this.tagHandlerClass = tagHandlerClass;
            PropertyDescriptor[] pd = tagClassInfo.getPropertyDescriptors();
            for(int i = 0; i < pd.length; i++) {
                /* FIXME: why is the check for null below needed?? -akv */
                /* 
                   FIXME: should probably be checking for things like
                          pageContext, bodyContent, and parent here -akv
                */
                if (pd[i].getWriteMethod() != null)
                    addSetterMethod(pd[i].getName(), pd[i].getWriteMethod());
            }
        } catch (IntrospectionException ex) {
            throw new JasperException(Constants.getString("jsp.error.unable.to_introspect",
                                                          new Object[] {
                                                              tagHandlerClass.getName(),
                                                              ex.getMessage()
                                                          }
                                                          ));
        }
    }

    Class getTagHandlerClass() {
        return tagHandlerClass;
    }
    
    BeanInfo getTagClassInfo() {
        return tagClassInfo;
    }
}

    
