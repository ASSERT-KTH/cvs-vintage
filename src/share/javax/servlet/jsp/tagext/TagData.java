/*
 * @(#)TagData.java	1.12 99/08/06
 * 
 * Copyright (c) 1999 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 * 
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 * 
 * CopyrightVersion 1.0
 */
 
package javax.servlet.jsp.tagext;

import java.util.Hashtable;

/**
 * Tag instance attribute(s)/value(s); often this data is fully static in the
 * case where none of the attributes have runtime expresssions as their values.
 * Thus this class is intended to expose an immutable interface to a set of
 * immutable attribute/value pairs.
 *
 * This class is cloneable so implementations can create a static instance
 * and then just clone it before adding the request-time expressions.
 */

public class TagData implements Cloneable {
    /**
     * Distinguished value for an attribute to indicate its value
     * is a request-time expression which is not yet available because
     * this TagData instance is being used at translation-time.
     */
    // TODO -- revisit clonable; do we need a clone() method?
    // TODO -- Should we just use an array?
    // TODO -- should there be a factory?

    public static final Object REQUEST_TIME_VALUE = new Object();

    /**
     * Constructor for a TagData
     *
     * For simplicity and speed, we are just using primitive types.
     * A typical constructor may be
     *
     * static final Object[][] att = {{"connection", "conn0"}, {"id", "query0"}};
     * static final TagData td = new TagData(att);
     *
     * In an implementation that uses the clonable approach sketched
     * above all values must be Strings except for those holding the
     * distinguished object REQUEST_TIME_VALUE.

     * @param atts the static attribute and values.  May be null.
     */
    public TagData(Object[] atts[]) {
	attributes = new Hashtable(atts.length);

	if (atts != null) {
	    for (int i = 0; i < atts.length; i++) {
		attributes.put(atts[i][0], atts[i][1]);
	    }
	}
    }

    /**
     * Constructor for a TagData
     *
     * If you already have the attributes in a hashtable, use this
     * constructor. 
     *
     ***** NEED TO REMOVE THIS COMMENT: JUST FOR pelegri/lpgc's eyes ****
     * Since the JSP translator internally has attributes in a
     * hashtable whenever a TagData needs to be constructed at
     * translation time, this constructor is used... akv
     ***** 
     */
    public TagData(Hashtable attrs) {
        this.attributes = attrs;
    }

    /**
     * @return the value of the id attribute or null
     */

    public String getId() {
	return getAttributeString(TagAttributeInfo.ID);
    }

    /**
     * @return the attribute's value object. Returns the
     * distinguished object REQUEST_TIME_VALUE if the value is
     * request time and we are using TagData at translation time.
     * Returns null if the attribute is not set.
     */
    // TODO -- means we cannot distinguish from an unset attribute an
    // TODO -- one that is set to null.

    public Object getAttribute(String attName) {
	return attributes.get(attName);
    }

    /**
     * Set the value of this attribute to be 
     */
    public void setAttribute(String attName,
			     Object value) {
	attributes.put(attName, value);
    }

    /**
     * @return the attribute value string
     *
     * @throw ClassCastException if attribute value is not a String
     */

    public String getAttributeString(String attName) {
	return (String) attributes.get(attName);
    }

    // private data

    private Hashtable attributes;	// the tagname/value map
}
