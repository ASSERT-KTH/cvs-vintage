/*
 * $Header: /tmp/cvs-vintage/struts/contrib/struts-el/src/share/org/apache/strutsel/taglib/tiles/ELDefinitionTagBeanInfo.java,v 1.2 2004/03/14 07:15:04 sraeburn Exp $
 * $Revision: 1.2 $
 * $Date: 2004/03/14 07:15:04 $
 *
 * Copyright 1999-2004 The Apache Software Foundation.
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

package org.apache.strutsel.taglib.tiles;

import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.beans.SimpleBeanInfo;

/**
 * This is the <code>BeanInfo</code> descriptor for the
 * <code>org.apache.strutsel.taglib.tiles.ELDefinitionTag</code> class.  It is
 * needed to override the default mapping of custom tag attribute names to
 * class attribute names.
 */
public class ELDefinitionTagBeanInfo extends SimpleBeanInfo
{
    public  PropertyDescriptor[] getPropertyDescriptors()
    {
        ArrayList proplist = new ArrayList();

        try {
            proplist.add(new PropertyDescriptor("id", ELDefinitionTag.class,
                                                null, "setIdExpr"));
        } catch (IntrospectionException ex) {}
        try {
            proplist.add(new PropertyDescriptor("scope", ELDefinitionTag.class,
                                                null, "setScopeExpr"));
        } catch (IntrospectionException ex) {}
        try {
            proplist.add(new PropertyDescriptor("template", ELDefinitionTag.class,
                                                null, "setTemplateExpr"));
        } catch (IntrospectionException ex) {}
        try {
            proplist.add(new PropertyDescriptor("page", ELDefinitionTag.class,
                                                null, "setPageExpr"));
        } catch (IntrospectionException ex) {}
        try {
            proplist.add(new PropertyDescriptor("role", ELDefinitionTag.class,
                                                null, "setRoleExpr"));
        } catch (IntrospectionException ex) {}
        try {
            proplist.add(new PropertyDescriptor("extends", ELDefinitionTag.class,
                                                null, "setExtendsExpr"));
        } catch (IntrospectionException ex) {}
        
        PropertyDescriptor[] result =
            new PropertyDescriptor[proplist.size()];
        return ((PropertyDescriptor[]) proplist.toArray(result));
    }
}
