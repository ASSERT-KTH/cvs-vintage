/*
 * $Header: /tmp/cvs-vintage/struts/src/examples/org/apache/struts/webapp/exercise/TestBean.java,v 1.3 2004/01/13 12:48:44 husted Exp $
 * $Revision: 1.3 $
 * $Date: 2004/01/13 12:48:44 $
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
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
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


package org.apache.struts.webapp.exercise;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.LabelValueBean;


/**
 * General purpose test bean for Struts custom tag tests.
 *
 * @version $Revision: 1.3 $ $Date: 2004/01/13 12:48:44 $
 */

public class TestBean extends ActionForm {


    // ------------------------------------------------------------- Properties


    /**
     * A collection property where the elements of the collection are
     * of type <code>LabelValueBean</code>.
     */
    private Collection beanCollection = null;

    public Collection getBeanCollection() {
        if (beanCollection == null) {
            Vector entries = new Vector(10);

            entries.add(new LabelValueBean("Label 0", "Value 0"));
            entries.add(new LabelValueBean("Label 1", "Value 1"));
            entries.add(new LabelValueBean("Label 2", "Value 2"));
            entries.add(new LabelValueBean("Label 3", "Value 3"));
            entries.add(new LabelValueBean("Label 4", "Value 4"));
            entries.add(new LabelValueBean("Label 5", "Value 5"));
            entries.add(new LabelValueBean("Label 6", "Value 6"));
            entries.add(new LabelValueBean("Label 7", "Value 7"));
            entries.add(new LabelValueBean("Label 8", "Value 8"));
            entries.add(new LabelValueBean("Label 9", "Value 9"));

            beanCollection = entries;
        }

        return (beanCollection);
    }

    public void setBeanCollection(Collection beanCollection) {
        this.beanCollection = beanCollection;
    }


    /**
     * A multiple-String SELECT element using a bean collection.
     */
    private String[] beanCollectionSelect = { "Value 1", "Value 3",
                                              "Value 5" };

    public String[] getBeanCollectionSelect() {
        return (this.beanCollectionSelect);
    }

    public void setBeanCollectionSelect(String beanCollectionSelect[]) {
        this.beanCollectionSelect = beanCollectionSelect;
    }


    /**
     * A boolean property whose initial value is true.
     */
    private boolean booleanProperty = true;

    public boolean getBooleanProperty() {
        return (booleanProperty);
    }

    public void setBooleanProperty(boolean booleanProperty) {
        this.booleanProperty = booleanProperty;
    }


    /**
     * A multiple-String SELECT element using a collection.
     */
    private String[] collectionSelect = { "Value 2", "Value 4",
                                          "Value 6" };

    public String[] getCollectionSelect() {
        return (this.collectionSelect);
    }

    public void setCollectionSelect(String collectionSelect[]) {
        this.collectionSelect = collectionSelect;
    }


    /**
     * A double property.
     */
    private double doubleProperty = 321.0;

    public double getDoubleProperty() {
        return (this.doubleProperty);
    }

    public void setDoubleProperty(double doubleProperty) {
        this.doubleProperty = doubleProperty;
    }


    /**
     * A boolean property whose initial value is false
     */
    private boolean falseProperty = false;

    public boolean getFalseProperty() {
        return (falseProperty);
    }

    public void setFalseProperty(boolean falseProperty) {
        this.falseProperty = falseProperty;
    }


    /**
     * A float property.
     */
    private float floatProperty = (float) 123.0;

    public float getFloatProperty() {
        return (this.floatProperty);
    }

    public void setFloatProperty(float floatProperty) {
        this.floatProperty = floatProperty;
    }


    /**
     * Integer arrays that are accessed as an array as well as indexed.
     */
    private int intArray[] = { 0, 10, 20, 30, 40 };

    public int[] getIntArray() {
        return (this.intArray);
    }

    public void setIntArray(int intArray[]) {
        this.intArray = intArray;
    }

    private int intIndexed[] = { 0, 10, 20, 30, 40 };

    public int getIntIndexed(int index) {
        return (intIndexed[index]);
    }

    public void setIntIndexed(int index, int value) {
        intIndexed[index] = value;
    }


    private int intMultibox[] = new int[0];

    public int[] getIntMultibox() {
        return (this.intMultibox);
    }

    public void setIntMultibox(int intMultibox[]) {
        this.intMultibox = intMultibox;
    }

    /**
     * An integer property.
     */
    private int intProperty = 123;

    public int getIntProperty() {
        return (this.intProperty);
    }

    public void setIntProperty(int intProperty) {
        this.intProperty = intProperty;
    }


    /**
     * A long property.
     */
    private long longProperty = 321;

    public long getLongProperty() {
        return (this.longProperty);
    }

    public void setLongProperty(long longProperty) {
        this.longProperty = longProperty;
    }


    /**
     * A multiple-String SELECT element.
     */
    private String[] multipleSelect = { "Multiple 3", "Multiple 5",
                                        "Multiple 7" };

    public String[] getMultipleSelect() {
        return (this.multipleSelect);
    }

    public void setMultipleSelect(String multipleSelect[]) {
        this.multipleSelect = multipleSelect;
    }


    /**
     * A nested reference to another test bean (populated as needed).
     */
    private TestBean nested = null;

    public TestBean getNested() {
        if (nested == null)
            nested = new TestBean();
        return (nested);
    }


    /**
     * A String property with an initial value of null.
     */
    private String nullProperty = null;

    public String getNullProperty() {
        return (this.nullProperty);
    }

    public void setNullProperty(String nullProperty) {
        this.nullProperty = nullProperty;
    }


    /**
     * A short property.
     */
    private short shortProperty = (short) 987;

    public short getShortProperty() {
        return (this.shortProperty);
    }

    public void setShortProperty(short shortProperty) {
        this.shortProperty = shortProperty;
    }


    /**
     * A single-String value for a SELECT element.
     */
    private String singleSelect = "Single 5";

    public String getSingleSelect() {
        return (this.singleSelect);
    }

    public void setSingleSelect(String singleSelect) {
        this.singleSelect = singleSelect;
    }


    /**
     * String arrays that are accessed as an array as well as indexed.
     */
    private String stringArray[] =
    { "String 0", "String 1", "String 2", "String 3", "String 4" };

    public String[] getStringArray() {
        return (this.stringArray);
    }

    public void setStringArray(String stringArray[]) {
        this.stringArray = stringArray;
    }

    private String stringIndexed[] =
    { "String 0", "String 1", "String 2", "String 3", "String 4" };

    public String getStringIndexed(int index) {
        return (stringIndexed[index]);
    }

    public void setStringIndexed(int index, String value) {
        stringIndexed[index] = value;
    }


    private String stringMultibox[] = new String[0];

    public String[] getStringMultibox() {
        return (this.stringMultibox);
    }

    public void setStringMultibox(String stringMultibox[]) {
        this.stringMultibox = stringMultibox;
    }

    /**
     * A String property.
     */
    private String stringProperty = "This is a string";

    public String getStringProperty() {
        return (this.stringProperty);
    }

    public void setStringProperty(String stringProperty) {
        this.stringProperty = stringProperty;
    }

    /**
     * An empty String property.
     */
    private String emptyStringProperty = "";

    public String getEmptyStringProperty() {
        return (this.emptyStringProperty);
    }

    public void setEmptyStringProperty(String emptyStringProperty) {
        this.emptyStringProperty = emptyStringProperty;
    }


    /**
     * A single-String value for a SELECT element based on resource strings.
     */
    private String resourcesSelect = "Resources 2";

    public String getResourcesSelect() {
        return (this.resourcesSelect);
    }

    public void setResourcesSelect(String resourcesSelect) {
        this.resourcesSelect = resourcesSelect;
    }


    /**
     * A property that allows a null value but is still used in a SELECT.
     */
    private String withNulls = null;

    public String getWithNulls() {
        return (this.withNulls);
    }

    public void setWithNulls(String withNulls) {
        this.withNulls = withNulls;
    }


    /**
     * A List property.
     */
    private List listProperty = null;

    public List getListProperty() {
        if (listProperty == null) {
            listProperty = new ArrayList();
            listProperty.add("dummy");
        }
        return listProperty;
    }

    public void setListProperty(List listProperty) {
        this.listProperty = listProperty;
    }

    /**
     * An empty List property.
     */
    private List emptyListProperty = null;

    public List getEmptyListProperty() {
        if (emptyListProperty == null) {
            emptyListProperty = new ArrayList();
        }
        return emptyListProperty;
    }

    public void setEmptyListProperty(List emptyListProperty) {
        this.emptyListProperty = emptyListProperty;
    }


    /**
     * A Map property.
     */
    private Map mapProperty = null;

    public Map getMapProperty() {
        if (mapProperty == null) {
            mapProperty = new HashMap();
            mapProperty.put("dummy", "dummy");
        }
        return mapProperty;
    }

    public void setMapProperty(Map mapProperty) {
        this.mapProperty = mapProperty;
    }

    /**
     * An empty Map property.
     */
    private Map emptyMapProperty = null;

    public Map getEmptyMapProperty() {
        if (emptyMapProperty == null) {
            emptyMapProperty = new HashMap();
        }
        return emptyMapProperty;
    }

    public void setEmptyMapProperty(Map emptyMapProperty) {
        this.emptyMapProperty = emptyMapProperty;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Reset the properties that will be received as input.
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {

        booleanProperty = false;
        collectionSelect = new String[0];
        intMultibox = new int[0];
        multipleSelect = new String[0];
        stringMultibox = new String[0];
        if (nested != null)
            nested.reset(mapping, request);

    }


}
