/*
 * @(#)TagAttributeInfo.java	1.10 99/08/08
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

/**
 * Information on Tag Attributes;
 * this class is instantiated from the Tag Library Descriptor file (TLD).
 *
 * Only the information needed to generate code is included here.  Other information
 * like SCHEMA for validation belongs elsewhere.
 */

public class TagAttributeInfo {
    /**
     * "id" is wired in to be ID.  There is no real benefit in having it be something else
     * IDREFs are not handled any differently.
     */

    public static final String ID = "id";

    /**
     * Constructor for TagAttributeInfo.
     * No public constructor; this class is to be instantiated only from the
     * TagLibrary code under request from some JSP code that is parsing a
     * TLD (Tag Library Descriptor).
     *
     * @param name The name of the attribute
     * @param type The name of the type of the attribute
     * @param reqTime Can this attribute hold a request-time Attribute
     */
    // TODO -- add the content descriptor...

    public TagAttributeInfo(String name, boolean required, boolean rtexprvalue, 
                            String type, boolean reqTime) 
    {
	this.name = name;
        this.required = required;
        this.rtexprvalue = rtexprvalue;
        this.type = type;
	this.reqTime = reqTime;
    }

    /**
     * @returns the name of the attribute
     */

    public String getName() {
	return name;
    }

    /**
     * @returns the type of the attribute
     */

    public String getTypeName() {
	return type;
    }

    /**
     * Can this attribute hold a request-time value?
     */

    public boolean canBeRequestTime() {
	return reqTime;
    }

    /**
     * Is this required or not?
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Convenience method that goes through an array of TagAttributeInfo
     * objects and looks for "id".
     */
    public static TagAttributeInfo getIdAttribute(TagAttributeInfo a[]) {
	for (int i=0; i<a.length; i++) {
	    if (a[i].getName().equals(ID)) {
		return a[i];
	    }
	}
	return null;		// no such attribute
    }

    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append("name = "+name+" ");
        b.append("type = "+type+" ");
        b.append("reqTime = "+reqTime+" ");
        b.append("required = "+required+" ");
        b.append("rtexprvalue = "+rtexprvalue+" ");
        return b.toString();
    }

    /*
     * fields
     */

    private String name;
    private String type;
    private boolean reqTime;
    private boolean required;
    private boolean rtexprvalue;
}
