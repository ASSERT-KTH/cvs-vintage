/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/action/DynaActionForm.java,v 1.1 2002/01/17 00:15:05 craigmcc Exp $
 * $Revision: 1.1 $
 * $Date: 2002/01/17 00:15:05 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
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


package org.apache.struts.action;


import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.struts.config.FormBeanConfig;
import org.apache.struts.config.FormPropertyConfig;


/**
 * <p>Specialized subclass of <code>ActionForm</code> that allows the creation
 * of form beans with dynamic sets of properties, without requiring the
 * developer to create a Java class for each type of form bean.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2002/01/17 00:15:05 $
 * @since Struts 1.1
 */

public class DynaActionForm extends ActionForm implements DynaBean {


    // ----------------------------------------------------- Instance Variables


    /**
     * The DynaActionFormClass with which we are associated.
     */
    protected DynaActionFormClass dynaClass = null;


    /**
     * The set of property values for this DynaActionForm, keyed by
     * property name.
     */
    protected HashMap dynaValues = new HashMap();


    // ----------------------------------------------------- ActionForm Methods


    /**
     * Reset all bean properties to their default state.  This method is
     * called before the properties are repopulated by the controller
     * servlet.
     * <p>
     * The default implementation attempts to forward to the HTTP
     * version of this method.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(ActionMapping mapping, ServletRequest request) {

        try {
            reset(mapping, (HttpServletRequest) request);
        } catch (ClassCastException e) {
            ;
        }

    }


    /**
     * Reset all bean properties to their default state.  This method is
     * called before the properties are repopulated by the controller servlet.
     * <p>
     * The default implementation uses the initial value specified in the
     * FormPropertyConfig element for each property.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {

        String name = mapping.getName();
        if (name == null) {
            return;
        }
        FormBeanConfig config =
            mapping.getApplicationConfig().findFormBeanConfig(name);
        if (config == null) {
            return;
        }
        FormPropertyConfig props[] = config.findFormPropertyConfigs();
        for (int i = 0; i < props.length; i++) {
            set(props[i].getName(), props[i].initial());
        }

    }


    // ------------------------------------------------------- DynaBean Methods


    /**
     * Does the specified mapped property contain a value for the specified
     * key value?
     *
     * @param name Name of the property to check
     * @param key Name of the key to check
     *
     * @exception IllegalArgumentException if there is no property
     *  of the specified name
     */
    public boolean contains(String name, String key) {

        DynaProperty descriptor = getDynaProperty(name);
        Object value = dynaValues.get(name);
        if (value == null) {
            throw new NullPointerException
                ("No mapped value for '" + name + "(" + key + ")'");
        } else if (value instanceof Map) {
            return (((Map) value).containsKey(key));
        } else {
            throw new IllegalArgumentException
                ("Non-mapped property for '" + name + "(" + key + ")'");
        }

    }


    /**
     * Return the value of a simple property with the specified name.
     *
     * @param name Name of the property whose value is to be retrieved
     *
     * @exception IllegalArgumentException if there is no property
     *  of the specified name
     */
    public Object get(String name) {

        // Return any non-null value for the specified property
        Object value = dynaValues.get(name);
        if (value != null) {
            return (value);
        } 

        // Return a null value for a non-primitive property
        Class type = getDynaProperty(name).getType();
        if (!type.isPrimitive()) {
            return (value);
        }

        // Manufacture default values for primitive properties
        if (type == Boolean.TYPE) {
            return (Boolean.FALSE);
        } else if (type == Byte.TYPE) {
            return (new Byte((byte) 0));
        } else if (type == Character.TYPE) {
            return (new Character((char) 0));
        } else if (type == Double.TYPE) {
            return (new Double((double) 0.0));
        } else if (type == Float.TYPE) {
            return (new Float((float) 0.0));
        } else if (type == Integer.TYPE) {
            return (new Integer((int) 0));
        } else if (type == Long.TYPE) {
            return (new Long((int) 0));
        } else if (type == Short.TYPE) {
            return (new Short((short) 0));
        } else {
            return (null);
        }

    }


    /**
     * Return the value of an indexed property with the specified name.
     *
     * @param name Name of the property whose value is to be retrieved
     * @param index Index of the value to be retrieved
     *
     * @exception IllegalArgumentException if there is no property
     *  of the specified name
     * @exception IllegalArgumentException if the specified property
     *  exists, but is not indexed
     * @exception IndexOutOfBoundsException if the specified index
     *  is outside the range of the underlying property
     * @exception NullPointerException if no array or List has been
     *  initialized for this property
     */
    public Object get(String name, int index) {

        DynaProperty descriptor = getDynaProperty(name);
        Object value = dynaValues.get(name);
        if (value == null) {
            throw new NullPointerException
                ("No indexed value for '" + name + "[" + index + "]'");
        } else if (value.getClass().isArray()) {
            return (Array.get(value, index));
        } else if (value instanceof List) {
            return ((List) value).get(index);
        } else {
            throw new IllegalArgumentException
                ("Non-indexed property for '" + name + "[" + index + "]'");
        }

    }


    /**
     * Return the value of a mapped property with the specified name,
     * or <code>null</code> if there is no value for the specified key.
     *
     * @param name Name of the property whose value is to be retrieved
     * @param key Key of the value to be retrieved
     *
     * @exception IllegalArgumentException if there is no property
     *  of the specified name
     * @exception IllegalArgumentException if the specified property
     *  exists, but is not mapped
     */
    public Object get(String name, String key) {

        DynaProperty descriptor = getDynaProperty(name);
        Object value = dynaValues.get(name);
        if (value == null) {
            throw new NullPointerException
                ("No mapped value for '" + name + "(" + key + ")'");
        } else if (value instanceof Map) {
            return (((Map) value).get(key));
        } else {
            throw new IllegalArgumentException
                ("Non-mapped property for '" + name + "(" + key + ")'");
        }

    }


    /**
     * Return the <code>DynaClass</code> instance that describes the set of
     * properties available for this DynaBean.
     */
    public DynaClass getDynaClass() {

        return (this.dynaClass);

    }


    /**
     * Remove any existing value for the specified key on the
     * specified mapped property.
     *
     * @param name Name of the property for which a value is to
     *  be removed
     * @param key Key of the value to be removed
     *
     * @exception IllegalArgumentException if there is no property
     *  of the specified name
     */
    public void remove(String name, String key) {

        DynaProperty descriptor = getDynaProperty(name);
        Object value = dynaValues.get(name);
        if (value == null) {
            throw new NullPointerException
                ("No mapped value for '" + name + "(" + key + ")'");
        } else if (value instanceof Map) {
            ((Map) value).remove(key);
        } else {
            throw new IllegalArgumentException
                ("Non-mapped property for '" + name + "(" + key + ")'");
        }

    }


    /**
     * Set the value of a simple property with the specified name.
     *
     * @param name Name of the property whose value is to be set
     * @param value Value to which this property is to be set
     *
     * @exception ConversionException if the specified value cannot be
     *  converted to the type required for this property
     * @exception IllegalArgumentException if there is no property
     *  of the specified name
     * @exception NullPointerException if an attempt is made to set a
     *  primitive property to null
     */
    public void set(String name, Object value) {

        DynaProperty descriptor = getDynaProperty(name);
        if (value == null) {
            if (descriptor.getType().isPrimitive()) {
                throw new NullPointerException
                    ("Primitive value for '" + name + "'");
            }
        } else if (!isDynaAssignable(descriptor.getType(), value.getClass())) {
            throw new ConversionException
                ("Cannot assign value of type '" +
                 value.getClass().getName() +
                 "' to property '" + name + "' of type '" +
                 descriptor.getType().getName() + "'");
        }
        dynaValues.put(name, value);

    }


    /**
     * Set the value of an indexed property with the specified name.
     *
     * @param name Name of the property whose value is to be set
     * @param index Index of the property to be set
     * @param value Value to which this property is to be set
     *
     * @exception ConversionException if the specified value cannot be
     *  converted to the type required for this property
     * @exception IllegalArgumentException if there is no property
     *  of the specified name
     * @exception IllegalArgumentException if the specified property
     *  exists, but is not indexed
     * @exception IndexOutOfBoundsException if the specified index
     *  is outside the range of the underlying property
     */
    public void set(String name, int index, Object value) {

        DynaProperty descriptor = getDynaProperty(name);
        Object prop = dynaValues.get(name);
        if (prop == null) {
            throw new NullPointerException
                ("No indexed value for '" + name + "[" + index + "]'");
        } else if (prop.getClass().isArray()) {
            Array.set(prop, index, value);
        } else if (prop instanceof List) {
            try {
                ((List) prop).set(index, value);
            } catch (ClassCastException e) {
                throw new ConversionException(e.getMessage());
            }
        } else {
            throw new IllegalArgumentException
                ("Non-indexed property for '" + name + "[" + index + "]'");
        }

    }


    /**
     * Set the value of a mapped property with the specified name.
     *
     * @param name Name of the property whose value is to be set
     * @param key Key of the property to be set
     * @param value Value to which this property is to be set
     *
     * @exception ConversionException if the specified value cannot be
     *  converted to the type required for this property
     * @exception IllegalArgumentException if there is no property
     *  of the specified name
     * @exception IllegalArgumentException if the specified property
     *  exists, but is not mapped
     */
    public void set(String name, String key, Object value) {

        DynaProperty descriptor = getDynaProperty(name);
        Object prop = dynaValues.get(name);
        if (prop == null) {
            throw new NullPointerException
                ("No mapped value for '" + name + "(" + key + ")'");
        } else if (prop instanceof Map) {
            ((Map) prop).put(key, value);
        } else {
            throw new IllegalArgumentException
                ("Non-mapped property for '" + name + "(" + key + ")'");
        }

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Render a String representation of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("DynaActionForm[dynaClass=");
        sb.append(getDynaClass().getName());
        DynaProperty props[] = getDynaClass().getDynaProperties();
        if (props == null) {
            props = new DynaProperty[0];
        }
        for (int i = 0; i < props.length; i++) {
            sb.append(',');
            sb.append(props[i].getName());
            sb.append('=');
            sb.append(get(props[i].getName()));
        }
        sb.append("]");
        return (sb.toString());

    }


    // -------------------------------------------------------- Package Methods


    /**
     * Set the DynaActionFormClass instance that we are associated with.
     *
     * @param dynaClass The DynaActionFormClass instance for this bean
     */
    void setDynaActionFormClass(DynaActionFormClass dynaClass) {

        this.dynaClass = dynaClass;

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Return the property descriptor for the specified property name.
     *
     * @param name Name of the property for which to retrieve the descriptor
     *
     * @exception IllegalArgumentException if this is not a valid property
     *  name for our DynaClass
     */
    protected DynaProperty getDynaProperty(String name) {

        DynaProperty descriptor = getDynaClass().getDynaProperty(name);
        if (descriptor == null) {
            throw new IllegalArgumentException
                ("Invalid property name '" + name + "'");
        }
        return (descriptor);

    }


    /**
     * Is an object of the source class assignable to the destination class?
     *
     * @param dest Destination class
     * @param source Source class
     */
    protected boolean isDynaAssignable(Class dest, Class source) {

        if (dest.isAssignableFrom(source) ||
            ((dest == Boolean.TYPE) && (source == Boolean.class)) ||
            ((dest == Byte.TYPE) && (source == Byte.class)) ||
            ((dest == Character.TYPE) && (source == Character.class)) ||
            ((dest == Double.TYPE) && (source == Double.class)) ||
            ((dest == Float.TYPE) && (source == Float.class)) ||
            ((dest == Integer.TYPE) && (source == Integer.class)) ||
            ((dest == Long.TYPE) && (source == Long.class)) ||
            ((dest == Short.TYPE) && (source == Short.class))) {
            return (true);
        } else {
            return (false);
        }

    }


}
