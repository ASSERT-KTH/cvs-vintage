/*
 * @(#)TagExtraInfo.java	1.5 99/08/08
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
 * Extra Tag Information for a Custom Tag;
 * this class is mentioned in the Tag Library Descriptor file (TLD).
 *
 * This class must be used:
 *  - if the tag defines any scripting variables
 *  - if the tag wants to provide translation-time validation of the tag
 *    attributes.
 *
 */

public abstract class TagExtraInfo {

    /**
     * information on scripting variables defined by this tag
     *
     * @param data The translation-time TagData instance.
     */
    public VariableInfo[] getVariableInfo(TagData data) {
	return new VariableInfo[0];
    }

    /**
     * Translation-time validation of the attributes.  The argument is a
     * translation-time, so request-time attributes are indicated as such.
     *
     * @param data The translation-time TagData instance.
     */

    public boolean isValid(TagData data) {
	return true;
    }

    /**
     * Set the TagInfo for this class
     *
     * @param tagInfo The TagInfo this instance is extending
     */
    public void setTagInfo(TagInfo tagInfo) {
	this.tagInfo = tagInfo;
    }

    /**
     * Get the TagInfo for this class
     *
     * @returns the taginfo instnace this instance is extending
     */
    public TagInfo getTagInfo() {
	return tagInfo;
    }
    
    // protected data
    protected TagInfo tagInfo;
}

