/*
 * @(#)TagInfo.java	1.16 99/08/18
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
 * Tag information for a tag in a Tag Library;
 * this class is instantiated from the Tag Library Descriptor file (TLD).
 *
 */

public class TagInfo {

    /**
     * static constant for getBodyContent() when it is JSP
     */

    public static final String BODY_CONTENT_JSP = "JSP";

    /**
     * static constant for getBodyContent() when it is Tag dependent
     */

    public static final String BODY_CONTENT_TAG_DEPENDENT = "TAGDEPENDENT";


    /**
     * static constant for getBodyContent() when it is empty
     */

    public static final String BODY_CONTENT_EMPTY = "EMPTY";

    /**
     * Constructor for TagInfo.
     * No public constructor; this class is to be instantiated only from the
     * TagLibrary code under request from some JSP code that is parsing a
     * TLD (Tag Library Descriptor).
     *
     * @param tagName The name of this tag
     * @param tagClassName The name of the tag handler class
     * @param bodycontent Information on the body content of these tags
     * @param infoString The (optional) string information for this tag
     * @param taglib The instance of the tag library that contains us.
     * @param tagExtraInfo The instance providing extra Tag info.  May be null
     * @param attributeInfo An array of AttributeInfo data from descriptor.
     * May be null;
     *
     */
    public TagInfo(String tagName,
	    String tagClassName,
	    String bodycontent,
	    String infoString,
	    TagLibraryInfo taglib,
	    TagExtraInfo tagExtraInfo,
	    TagAttributeInfo[] attributeInfo) {
	this.tagName       = tagName;
	this.tagClassName  = tagClassName;
	this.bodyContent   = bodycontent;
	this.infoString    = infoString;
	this.tagLibrary    = taglib;
	this.tagExtraInfo  = tagExtraInfo;
	this.attributeInfo = attributeInfo;

	if (tagExtraInfo != null)
            tagExtraInfo.setTagInfo(this);
    }
			 
    /**
     * Tag name
     */

    public String getTagName() {
	return tagName;
    }

    /**
     * A null return means no information on attributes
     */

   public TagAttributeInfo[] getAttributes() {
       return attributeInfo;
   }

    /**
     * Information on the object created by this tag at runtime.
     * Null means no such object created.
     *
     * Default is null if the tag has no "id" attribute,
     * otherwise, {"id", Object}
     */

   public VariableInfo[] getVariableInfo(TagData data) {
       TagExtraInfo tei = getTagExtraInfo();
       if (tei == null) {
	   return null;
       }
       return tei.getVariableInfo(data);
   }

    /**
     * Translation-time validation of the attributes.  The argument is a
     * translation-time, so request-time attributes are indicated as such.
     *
     * @param data The translation-time TagData instance.
     */


   public boolean isValid(TagData data) {
       TagExtraInfo tei = getTagExtraInfo();
       if (tei == null) {
	   return true;
       }
       return tei.isValid(data);
   }


    /**
      The instance (if any) for extra tag information
      */
    public TagExtraInfo getTagExtraInfo() {
	return tagExtraInfo;
    }


    /**
     * Name of the class that provides the (run-time handler for this tag
     */
    
    public String getTagClassName() {
	return tagClassName;
    }


    /**
     * @return the body content (hint) string
     */

    public String getBodyContent() { return bodyContent; }

    /**
     * @return the info string
     */

    public String getInfoString() { return infoString; }

    /**
     * @return the tab library instance we belong to
     */

    public TagLibraryInfo getTagLibrary() { return tagLibrary; }


    /**
     * Stringify for debug purposes...
     */
    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append("name = "+tagName+" ");
        b.append("class = "+tagClassName+" ");
        b.append("body = "+bodyContent+" ");
        b.append("info = "+infoString+" ");
        b.append("attributes = {\n");
        for(int i = 0; i < attributeInfo.length; i++)
            b.append("\t"+attributeInfo[i].toString());
        b.append("\n}\n");
        return b.toString();
    }

    /*
     * private fields
     */

    private String             tagName; // the name of the tag
    private String             tagClassName;
    private String             bodyContent;
    private String             infoString;
    private TagLibraryInfo     tagLibrary;
    private TagExtraInfo       tagExtraInfo; // instance of TagExtraInfo
    private TagAttributeInfo[] attributeInfo;
}
