/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/config/FormPropertyConfig.java,v 1.8 2002/12/23 22:00:24 craigmcc Exp $
 * $Revision: 1.8 $
 * $Date: 2002/12/23 22:00:24 $
 *
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
 *    any, must include the following acknowlegement:
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


package org.apache.struts.config;


import java.io.Serializable;
import java.lang.reflect.Array;
import org.apache.commons.beanutils.ConvertUtils;


/**
 * <p>A JavaBean representing the configuration information of a
 * <code>&lt;form-property&gt;</code> element in a Struts
 * configuration file.<p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.8 $ $Date: 2002/12/23 22:00:24 $
 * @since Struts 1.1
 */

public class FormPropertyConfig implements Serializable {


    // ----------------------------------------------------------- Constructors


    /**
     * Standard no-arguments constructor for dynamic instantiation.
     */
    public FormPropertyConfig() {

        super();

    }


    /**
     * Constructor that preconfigures the relevant properties.
     *
     * @param name Name of this property
     * @param type Fully qualified class name of this property
     * @param initial Initial value of this property (if any)
     */
    public FormPropertyConfig(String name, String type, String initial) {

        this(name, type, initial, 0);

    }


    /**
     * Constructor that preconfigures the relevant properties.
     *
     * @param name Name of this property
     * @param type Fully qualified class name of this property
     * @param initial Initial value of this property (if any)
     * @param size Size of the array to be created if this property is an
     *  array with no defined initial value
     */
    public FormPropertyConfig(String name, String type,
                              String initial, int size) {

        super();
        setName(name);
        setType(type);
        setInitial(initial);
        setSize(size);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Has this component been completely configured?
     */
    protected boolean configured = false;


    /**
     * Have we calculated the initial value object yet?
     */
    protected transient boolean initialized = false;


    /**
     * The calculated initial value for this property.
     */
    protected transient Object initialValue = null;


    // ------------------------------------------------------------- Properties


    /**
     * String representation of the initial value for this property.
     */
    protected String initial = null;

    public String getInitial() {
        return (this.initial);
    }

    public void setInitial(String initial) {
        if (configured) {
            throw new IllegalStateException("Configuration is frozen");
        }
        this.initial = initial;
    }


    /**
     * The JavaBean property name of the property described by this element.
     */
    protected String name = null;

    public String getName() {
        return (this.name);
    }

    public void setName(String name) {
        if (configured) {
            throw new IllegalStateException("Configuration is frozen");
        }
        this.name = name;
    }


    /**
     * <p>The size of the array to be created if this property is an array
     * type and there is no specified <code>initial</code> value.</p>
     *
     * @since Struts 1.1-b3
     */
    protected int size = 0;

    public int getSize() {
        return (this.size);
    }

    public void setSize(int size) {
        if (configured) {
            throw new IllegalStateException("Configuration is frozen");
        }
        this.size = size;
    }
        


    /**
     * The fully qualified Java class name of the implementation class
     * of this bean property, optionally followed by <code>[]</code> to
     * indicate that the property is indexed.
     */
    protected String type = null;

    public String getType() {
        return (this.type);
    }

    public void setType(String type) {
        if (configured) {
            throw new IllegalStateException("Configuration is frozen");
        }
        this.type = type;
    }


    /**
     * Return a Class corresponds to the value specified for the
     * <code>type</code> property, taking into account the trailing "[]"
     * for arrays (as well as the ability to specify primitive Java types).
     */
    public Class getTypeClass() {

        // Identify the base class (in case an array was specified)
        String baseType = getType();
        boolean indexed = false;
        if (baseType.endsWith("[]")) {
            baseType = baseType.substring(0, baseType.length() - 2);
            indexed = true;
        }

        // Construct an appropriate Class instance for the base class
        Class baseClass = null;
        if ("boolean".equals(baseType)) {
            baseClass = Boolean.TYPE;
        } else if ("byte".equals(baseType)) {
            baseClass = Byte.TYPE;
        } else if ("char".equals(baseType)) {
            baseClass = Character.TYPE;
        } else if ("double".equals(baseType)) {
            baseClass = Double.TYPE;
        } else if ("float".equals(baseType)) {
            baseClass = Float.TYPE;
        } else if ("int".equals(baseType)) {
            baseClass = Integer.TYPE;
        } else if ("long".equals(baseType)) {
            baseClass = Long.TYPE;
        } else if ("short".equals(baseType)) {
            baseClass = Short.TYPE;
        } else {
            ClassLoader classLoader =
                Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = this.getClass().getClassLoader();
            }
            try {
                baseClass = classLoader.loadClass(baseType);
            } catch (Throwable t) {
                baseClass = null;
            }
        }

        // Return the base class or an array appropriately
        if (indexed) {
            return (Array.newInstance(baseClass, 0).getClass());
        } else {
            return (baseClass);
        }

    }



    // --------------------------------------------------------- Public Methods


    /**
     * Return an object representing the initial value of this property.
     */
    public Object initial() {

        // Compute our initial value the first time it is requested
        // Don't bother synchronizing, a race is basically harmless
        if (!initialized) {
            try {
                Class clazz = getTypeClass();
                if (clazz.isArray()) {
                    if (initial != null) {
                        initialValue =
                            ConvertUtils.convert(initial, clazz);
                    } else {
                        initialValue =
                            Array.newInstance(clazz.getComponentType(), size);
                    }
                } else {
                    initialValue = ConvertUtils.convert(initial, clazz);
                }
            } catch (Throwable t) {
                initialValue = null;
            }
            initialized = true;
        }

        // Clone if the initial value is an array
        if ((initialValue != null) &&
            (initialValue.getClass().isArray())) {
            int n = Array.getLength(initialValue);
            Class componentType =
                initialValue.getClass().getComponentType();
            Object newValue = Array.newInstance(componentType, n);
            for (int j = 0; j < n; j++) {
                Array.set(newValue, j, Array.get(initialValue, j));
            }
            return (newValue);
        }

        // Return the calculated value
        return (initialValue);

    }


    /**
     * Freeze the configuration of this component.
     */
    public void freeze() {

        configured = true;

    }


    /**
     * Return a String representation of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("FormPropertyConfig[");
        sb.append("name=");
        sb.append(this.name);
        sb.append(",type=");
        sb.append(this.type);
        sb.append(",initial=");
        sb.append(this.initial);
        sb.append("]");
        return (sb.toString());

    }


}
