/*
 * $Header: /tmp/cvs-vintage/struts/contrib/struts-el/src/share/org/apache/strutsel/taglib/html/ELMultiboxTagBeanInfo.java,v 1.3 2003/03/09 05:51:09 dmkarr Exp $
 * $Revision: 1.3 $
 * $Date: 2003/03/09 05:51:09 $
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
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
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Struts", and "Apache Software
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

package org.apache.strutsel.taglib.html;

import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.beans.SimpleBeanInfo;

/**
 * This is the <code>BeanInfo</code> descriptor for the
 * <code>org.apache.strutsel.taglib.html.ELMultiboxTag</code> class.  It is needed
 * to override the default mapping of custom tag attribute names to class
 * attribute names.
 *<p>
 * In particular, it provides for the mapping of the custom tag attribute
 * <code>disabled</code> to the instance variable <code>disabledExpr</code>.
 *<p>
 * This is because the value of the unevaluated EL expression has to be kept
 * separately from the evaluated value, which is stored in the base class. This
 * is related to the fact that the JSP compiler can choose to reuse different
 * tag instances if they received the same original attribute values, and the
 * JSP compiler can choose to not re-call the setter methods, because it can
 * assume the same values are already set.
 */
public class ELMultiboxTagBeanInfo extends SimpleBeanInfo
{
    public  PropertyDescriptor[] getPropertyDescriptors()
    {
        ArrayList proplist = new ArrayList();

        try {
            proplist.add(new PropertyDescriptor("accesskey", ELMultiboxTag.class,
                                                null, "setAccesskeyExpr"));
        } catch (IntrospectionException ex) {}
        try {
            proplist.add(new PropertyDescriptor("alt", ELMultiboxTag.class,
                                                null, "setAltExpr"));
        } catch (IntrospectionException ex) {}
        try {
            proplist.add(new PropertyDescriptor("altKey", ELMultiboxTag.class,
                                                null, "setAltKeyExpr"));
        } catch (IntrospectionException ex) {}
        try {
            proplist.add(new PropertyDescriptor("disabled", ELMultiboxTag.class,
                                                null, "setDisabledExpr"));
        } catch (IntrospectionException ex) {}
        try {
            proplist.add(new PropertyDescriptor("name", ELMultiboxTag.class,
                                                null, "setNameExpr"));
        } catch (IntrospectionException ex) {}
        try {
            proplist.add(new PropertyDescriptor("onblur", ELMultiboxTag.class,
                                                null, "setOnblurExpr"));
        } catch (IntrospectionException ex) {}
        try {
            proplist.add(new PropertyDescriptor("onchange", ELMultiboxTag.class,
                                                null, "setOnchangeExpr"));
        } catch (IntrospectionException ex) {}
        try {
            proplist.add(new PropertyDescriptor("onclick", ELMultiboxTag.class,
                                                null, "setOnclickExpr"));
        } catch (IntrospectionException ex) {}
        try {
            proplist.add(new PropertyDescriptor("ondblclick", ELMultiboxTag.class,
                                                null, "setOndblclickExpr"));
        } catch (IntrospectionException ex) {}
        try {
            proplist.add(new PropertyDescriptor("onfocus", ELMultiboxTag.class,
                                                null, "setOnfocusExpr"));
        } catch (IntrospectionException ex) {}
        try {
            proplist.add(new PropertyDescriptor("onkeydown", ELMultiboxTag.class,
                                                null, "setOnkeydownExpr"));
        } catch (IntrospectionException ex) {}
        try {
            proplist.add(new PropertyDescriptor("onkeypress", ELMultiboxTag.class,
                                                null, "setOnkeypressExpr"));
        } catch (IntrospectionException ex) {}
        try {
            proplist.add(new PropertyDescriptor("onkeyup", ELMultiboxTag.class,
                                                null, "setOnkeyupExpr"));
        } catch (IntrospectionException ex) {}
        try {
            proplist.add(new PropertyDescriptor("onmousedown", ELMultiboxTag.class,
                                                null, "setOnmousedownExpr"));
        } catch (IntrospectionException ex) {}
        try {
            proplist.add(new PropertyDescriptor("onmousemove", ELMultiboxTag.class,
                                                null, "setOnmousemoveExpr"));
        } catch (IntrospectionException ex) {}
        try {
            proplist.add(new PropertyDescriptor("onmouseout", ELMultiboxTag.class,
                                                null, "setOnmouseoutExpr"));
        } catch (IntrospectionException ex) {}
        try {
            proplist.add(new PropertyDescriptor("onmouseover", ELMultiboxTag.class,
                                                null, "setOnmouseoverExpr"));
        } catch (IntrospectionException ex) {}
        try {
            proplist.add(new PropertyDescriptor("onmouseup", ELMultiboxTag.class,
                                                null, "setOnmouseupExpr"));
        } catch (IntrospectionException ex) {}
        try {
            proplist.add(new PropertyDescriptor("property", ELMultiboxTag.class,
                                                null, "setPropertyExpr"));
        } catch (IntrospectionException ex) {}
        try {
            proplist.add(new PropertyDescriptor("style", ELMultiboxTag.class,
                                                null, "setStyleExpr"));
        } catch (IntrospectionException ex) {}
        try {
            proplist.add(new PropertyDescriptor("styleClass", ELMultiboxTag.class,
                                                null, "setStyleClassExpr"));
        } catch (IntrospectionException ex) {}
        try {
            proplist.add(new PropertyDescriptor("styleId", ELMultiboxTag.class,
                                                null, "setStyleIdExpr"));
        } catch (IntrospectionException ex) {}
        try {
            proplist.add(new PropertyDescriptor("tabindex", ELMultiboxTag.class,
                                                null, "setTabindexExpr"));
        } catch (IntrospectionException ex) {}
        try {
            proplist.add(new PropertyDescriptor("title", ELMultiboxTag.class,
                                                null, "setTitleExpr"));
        } catch (IntrospectionException ex) {}
        try {
            proplist.add(new PropertyDescriptor("titleKey", ELMultiboxTag.class,
                                                null, "setTitleKeyExpr"));
        } catch (IntrospectionException ex) {}
        try {
            proplist.add(new PropertyDescriptor("value", ELMultiboxTag.class,
                                                null, "setValueExpr"));
        } catch (IntrospectionException ex) {}
        
        PropertyDescriptor[] result =
            new PropertyDescriptor[proplist.size()];
        return ((PropertyDescriptor[]) proplist.toArray(result));
    }
}
