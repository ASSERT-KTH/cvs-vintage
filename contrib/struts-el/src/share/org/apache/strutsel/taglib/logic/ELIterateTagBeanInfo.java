/*
 * $Header: /tmp/cvs-vintage/struts/contrib/struts-el/src/share/org/apache/strutsel/taglib/logic/ELIterateTagBeanInfo.java,v 1.5 2003/02/19 03:54:38 dmkarr Exp $
 * $Revision: 1.5 $
 * $Date: 2003/02/19 03:54:38 $
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

package org.apache.strutsel.taglib.logic;

import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.SimpleBeanInfo;
import java.lang.reflect.Method;

/**
 * This is the <code>BeanInfo</code> descriptor for the
 * <code>org.apache.strutsel.taglib.bean.ELIterateTag</code> class.  It is
 * needed to override the default mapping of custom tag attribute names to
 * class attribute names.
 *<p>
 * In particular, it provides for the mapping of the custom tag attribute
 * <code>collection</code> to the class attribute <code>collectionExpr</code>.
 *<p>
 * This is necessary because the base class,
 * <code>org.apache.struts.taglib.bean.IterateTag</code> already defines a
 * <code>collection</code> attribute of type <code>java.lang.Object</code>.
 * The <code>org.apache.strutsel.taglib.bean.ELIterateTag</code> subclass cannot
 * use this attribute because the custom tag attribute <code>collection</code>
 * has to be of type <code>java.lang.String</code> in order to be evaluated by
 * the JSTL EL engine.
 */
public class ELIterateTagBeanInfo extends SimpleBeanInfo
{
    public  PropertyDescriptor[] getPropertyDescriptors()
    {
        PropertyDescriptor[]  result   = new PropertyDescriptor[9];

        try {
            result[0] =
                new PropertyDescriptor("collection", ELIterateTag.class,
                                       null, "setCollectionExpr");
            result[1] = new PropertyDescriptor("id", ELIterateTag.class,
                                               null, "setIdExpr");
            result[2] = new PropertyDescriptor("indexId", ELIterateTag.class,
                                               null, "setIndexIdExpr");
            result[3] = new PropertyDescriptor("length", ELIterateTag.class,
                                               null, "setLengthExpr");
            result[4] = new PropertyDescriptor("name", ELIterateTag.class,
                                               null, "setNameExpr");
            result[5] = new PropertyDescriptor("offset", ELIterateTag.class,
                                               null, "setOffsetExpr");
            result[6] = new PropertyDescriptor("property", ELIterateTag.class,
                                               null, "setPropertyExpr");
            result[7] = new PropertyDescriptor("scope", ELIterateTag.class,
                                               null, "setScopeExpr");
            result[8] = new PropertyDescriptor("type", ELIterateTag.class,
                                               null, "setTypeExpr");
        }
        catch (IntrospectionException ex) {
            ex.printStackTrace();
        }
        
        return (result);
    }
}
