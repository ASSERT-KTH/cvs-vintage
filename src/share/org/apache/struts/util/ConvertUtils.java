/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/util/Attic/ConvertUtils.java,v 1.6 2001/02/02 02:57:50 craigmcc Exp $
 * $Revision: 1.6 $
 * $Date: 2001/02/02 02:57:50 $
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


package org.apache.struts.util;


import java.lang.reflect.Array;


/**
 * Utility methods for converting String values to objects of the specified
 * class.  If you specify a Java primitive type, or an array of a Java
 * primitive type, as a destination type, a scalar or array of the coresponding
 * Java wrapper class will be created instead.  If you attempt to convert an
 * Object or Object array of a non-String and non-primitive type, it will be
 * converted to a scalar String or array of Strings, as appropriate.
 *
 * @author Craig R. McClanahan
 * @author Ralph Schaer
 * @author Chris Audley
 * @version $Revision: 1.6 $ $Date: 2001/02/02 02:57:50 $
 */

public final class ConvertUtils {


    // ------------------------------------------------------ Static Properties


    /**
     * The default value for Boolean conversions.
     */
    private static Boolean defaultBoolean = Boolean.FALSE;

    public boolean getDefaultBoolean() {
        return (defaultBoolean.booleanValue());
    }

    public void setDefaultBoolean(boolean defaultBoolean) {
        this.defaultBoolean = new Boolean(defaultBoolean);
    }


    /**
     * The default value for Byte conversions.
     */
    private static Byte defaultByte = new Byte((byte) 0);

    public byte getDefaultByte() {
        return (defaultByte.byteValue());
    }

    public void setDefaultByte(byte defaultByte) {
        this.defaultByte = new Byte(defaultByte);
    }


    /**
     * The default value for Character conversions.
     */
    private static Character defaultCharacter = new Character(' ');

    public char getDefaultCharacter() {
        return (defaultCharacter.charValue());
    }

    public void setDefaultCharacter(char defaultCharacter) {
        this.defaultCharacter = new Character(defaultCharacter);
    }


    /**
     * The default value for Double conversions.
     */
    private static Double defaultDouble = new Double((double) 0.0);

    public double getDefaultDouble() {
        return (defaultDouble.doubleValue());
    }

    public void setDefaultDouble(double defaultDouble) {
        this.defaultDouble = new Double(defaultDouble);
    }


    /**
     * The default value for Float conversions.
     */
    private static Float defaultFloat = new Float((float) 0.0);

    public float getDefaultFloat() {
        return (defaultFloat.floatValue());
    }

    public void setDefaultFloat(float defaultFloat) {
        this.defaultFloat = new Float(defaultFloat);
    }


    /**
     * The default value for Integer conversions.
     */
    private static Integer defaultInteger = new Integer(0);

    public int getDefaultInteger() {
        return (defaultInteger.intValue());
    }

    public void setDefaultInteger(int defaultInteger) {
        this.defaultInteger = new Integer(defaultInteger);
    }


    /**
     * The default value for Long conversions.
     */
    private static Long defaultLong = new Long((long) 0);

    public long getDefaultLong() {
        return (defaultLong.longValue());
    }

    public void setDefaultLong(long defaultLong) {
        this.defaultLong = new Long(defaultLong);
    }


    /**
     * The default value for Short conversions.
     */
    private static Short defaultShort = new Short((short) 0);

    public short getDefaultShort() {
        return (defaultShort.shortValue());
    }

    public void setDefaultShort(short defaultShort) {
        this.defaultShort = new Short(defaultShort);
    }


    // ------------------------------------------------------- Static Variables


    /**
     * The Class object for java.lang.String.
     */
    private static Class stringClass = String.class;


    // --------------------------------------------------------- Public Classes


    /**
     * Convert the specified value into a String.  If the specified value
     * is an array, the first element (converted to a String) will be
     * returned.
     *
     * @param value Value to be converted (may be null)
     */
    public static String convert(Object value) {

        if (value == null) {
            return ((String) null);
        } else if (value.getClass().isArray()) {
            value = Array.get(value, 0);
            if (value == null)
                return ((String) null);
            else
                return (value.toString());
        } else {
            return (value.toString());
        }

    }


    /**
     * Convert the specified value to an object of the specified class (if
     * possible).  Otherwise, return a String representation of the value.
     * If you specify <code>type</code> as the name of a Java primitive
     * type, an instance of the corresponding wrapper class (initialized
     * to the correct value) is returned instead.
     *
     * @param value Value to be converted (may be null)
     * @param clazz Java class to be converted to (must be java.lang.String
     *  or one of the primitive type wrappers)
     */
    public static Object convert(String value, Class clazz) {

        if (clazz == stringClass) {
            if (value == null)
                return ((String) null);
            else
                return (value);
        } else if (clazz == Integer.TYPE) {
            return (convertInteger(value, defaultInteger));
        } else if (clazz == Boolean.TYPE) {
            return (convertBoolean(value, defaultBoolean));
        } else if (clazz == Long.TYPE) {
            return (convertLong(value, defaultLong));
        } else if (clazz == Double.TYPE) {
            return (convertDouble(value, defaultDouble));
        } else if (clazz == Character.TYPE) {
            return (convertCharacter(value, defaultCharacter));
        } else if (clazz == Byte.TYPE) {
            return (convertByte(value, defaultByte));
        } else if (clazz == Float.TYPE) {
            return (convertFloat(value, defaultFloat));
        } else if (clazz == Short.TYPE) {
            return (convertShort(value, defaultShort));
        } else if (clazz == Integer.class) {
            return (convertInteger(value, null));
        } else if (clazz == Boolean.class) {
            return (convertBoolean(value, null));
        } else if (clazz == Long.class) {
            return (convertLong(value, null));
        } else if (clazz == Double.class) {
            return (convertDouble(value, null));
        } else if (clazz == Character.class) {
            return (convertCharacter(value, null));
        } else if (clazz == Byte.class) {
            return (convertByte(value, null));
        } else if (clazz == Float.class) {
            return (convertFloat(value, null));
        } else if (clazz == Short.class) {
            return (convertShort(value, null));
        } else {
            if (value == null)
                return ((String) null);
            else
                return (value.toString());
        }

    }


    /**
     * Convert an array of specified values to an array of objects of the
     * specified class (if possible).  If you specify <code>type</code>
     * as one of the Java primitive types, an array of that type will be
     * returned; otherwise an array of the requested type (must be String
     * or a Java wrapper class for the primitive types) will be returned.
     *
     * @param value Value to be converted (may be null)
     * @param clazz Java array class to be converted to (must be String[],
     *  or an array of one of the Java primitive types)
     */
    public static Object convert(String values[], Class clazz) {

        Class type = clazz.getComponentType();
        if (type == stringClass) {
            if (values == null)
                return ((String[]) null);
            else
                return (values);
        }

        int len = values.length;

        if (type == Integer.TYPE) {
            int array[] = new int[len];
            for (int i = 0; i < len; i++)
                array[i] = convertInteger(values[i], defaultInteger).intValue();
            return (array);
        } else if (type == Boolean.TYPE) {
            boolean array[] = new boolean[len];
            for (int i = 0; i < len; i++)
                array[i] = convertBoolean(values[i], defaultBoolean).booleanValue();
            return (array);
        } else if (type == Long.TYPE) {
            long array[] = new long[len];
            for (int i = 0; i < len; i++)
                array[i] = convertLong(values[i], defaultLong).longValue();
            return (array);
        } else if (type == Double.TYPE) {
            double array[] = new double[len];
            for (int i = 0; i < len; i++)
                array[i] = convertDouble(values[i], defaultDouble).doubleValue();
            return (array);
        } else if (type == Character.TYPE) {
            char array[] = new char[len];
            for (int i = 0; i < len; i++)
                array[i] = convertCharacter(values[i], defaultCharacter).charValue();
            return (array);
        } else if (type == Byte.TYPE) {
            byte array[] = new byte[len];
            for (int i = 0; i < len; i++)
                array[i] = convertByte(values[i], defaultByte).byteValue();
            return (array);
        } else if (type == Float.TYPE) {
            float array[] = new float[len];
            for (int i = 0; i < len; i++)
                array[i] = convertFloat(values[i], defaultFloat).floatValue();
            return (array);
        } else if (type == Short.TYPE) {
            short array[] = new short[len];
            for (int i = 0; i < len; i++)
                array[i] = convertShort(values[i], defaultShort).shortValue();
            return (array);
        } else if (type == Integer.class) {
            Integer array[] = new Integer[len];
            for (int i = 0; i < len; i++)
                array[i] = convertInteger(values[i], null);
            return (array);
        } else if (type == Boolean.class) {
            Boolean array[] = new Boolean[len];
            for (int i = 0; i < len; i++)
                array[i] = convertBoolean(values[i], null);
            return (array);
        } else if (type == Long.class) {
            Long array[] = new Long[len];
            for (int i = 0; i < len; i++)
                array[i] = convertLong(values[i], null);
            return (array);
        } else if (type == Double.class) {
            Double array[] = new Double[len];
            for (int i = 0; i < len; i++)
                array[i] = convertDouble(values[i], null);
            return (array);
        } else if (type == Character.class) {
            Character array[] = new Character[len];
            for (int i = 0; i < len; i++)
                array[i] = convertCharacter(values[i], null);
            return (array);
        } else if (type == Byte.class) {
            Byte array[] = new Byte[len];
            for (int i = 0; i < len; i++)
                array[i] = convertByte(values[i], null);
            return (array);
        } else if (type == Float.class) {
            Float array[] = new Float[len];
            for (int i = 0; i < len; i++)
                array[i] = convertFloat(values[i], null);
            return (array);
        } else if (type == Short.class) {
            Short array[] = new Short[len];
            for (int i = 0; i < len; i++)
                array[i] = convertShort(values[i], null);
            return (array);
        } else {
            if (values == null)
                return ((String[]) null);
            else {
                String array[] = new String[len];
                for (int i = 0; i < len; i++)
                    array[i] = values[i].toString();
                return (array);
            }
        }

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Convert a String value to a corresponding Boolean value.
     *
     * @param value The string value to convert
     * @param defaultValue Default value to return on a conversion error
     */
    private static Boolean convertBoolean(String value, Boolean defaultValue) {

        if (value == null)
            return (defaultValue);
        else if (value.equalsIgnoreCase("yes") ||
                 value.equalsIgnoreCase("true") ||
                 value.equalsIgnoreCase("on"))
            return (Boolean.TRUE);
        else if (value.equalsIgnoreCase("no") ||
                 value.equalsIgnoreCase("false") ||
                 value.equalsIgnoreCase("off"))
            return (Boolean.FALSE);
        else
            return (defaultValue);

    }


    /**
     * Convert a String value to a corresponding Byte value.
     *
     * @param value The string value to convert
     * @param defaultValue Default value to return on a conversion error
     */
    private static Byte convertByte(String value, Byte defaultValue) {

        try {
            return (new Byte(value));
        } catch (NumberFormatException e) {
            return (defaultValue);
        }

    }


    /**
     * Convert a String value to a corresponding Character value.
     *
     * @param value The string value to convert
     * @param defaultValue Default value to return on a conversion error
     */
    private static Character convertCharacter(String value,
                                              Character defaultValue) {

        if (value == null)
            return (defaultValue);
        else if (value.length() == 0)
            return (new Character(' '));
        else
            return (defaultValue);

    }


    /**
     * Convert a String value to a corresponding Double value.
     *
     * @param value The string value to convert
     * @param defaultValue Default value to return on a conversion error
     */
    private static Double convertDouble(String value,
                                        Double defaultValue) {

        try {
            return (new Double(value));
        } catch (NumberFormatException e) {
            return (defaultValue);
        }

    }


    /**
     * Convert a String value to a corresponding Float value.
     *
     * @param value The string value to convert
     * @param defaultValue Default value to return on a conversion error
     */
    private static Float convertFloat(String value,
                                      Float defaultValue) {

        try {
            return (new Float(value));
        } catch (NumberFormatException e) {
            return (defaultValue);
        }

    }


    /**
     * Convert a String value to a corresponding Integer value.
     *
     * @param value The string value to convert
     * @param defaultValue Default value to return on a conversion error
     */
    private static Integer convertInteger(String value,
                                          Integer defaultValue) {

        try {
            return (new Integer(value));
        } catch (NumberFormatException e) {
            return (defaultValue);
        }

    }


    /**
     * Convert a String value to a corresponding Long value.
     *
     * @param value The string value to convert
     * @param defaultValue Default value to return on a conversion error
     */
    private static Long convertLong(String value, Long defaultValue) {

        try {
            return (new Long(value));
        } catch (NumberFormatException e) {
            return (defaultValue);
        }

    }


    /**
     * Convert a String value to a corresponding Short value.
     *
     * @param value The string value to convert
     * @param defaultValue Default value to return on a conversion error
     */
    private static Short convertShort(String value, Short defaultValue) {

        try {
            return (new Short(value));
        } catch (NumberFormatException e) {
            return (defaultValue);
        }

    }


}
