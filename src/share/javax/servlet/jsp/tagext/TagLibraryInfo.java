/*
 * @(#)TagLibraryInfo.java	1.11 99/08/18
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

import javax.servlet.jsp.tagext.TagInfo;

import java.net.URL;

import java.io.InputStream;

/**
 * Information on the Tag Library;
 * this class is instantiated from the Tag Library Descriptor file (TLD).
 */

abstract public class TagLibraryInfo {

    /**
     * Constructor
     *
     * This will invoke the constructors for TagInfo, and TagAttributeInfo
     * after parsing the TLD file.
     *
     * @param prefix the prefix actually used by the taglib directive
     * @param uri the URI actually used by the taglib directive
     * @param tldis the input stream to the TLD
     *
     * Changed the signature from URL to String to support relative URIs.
     *    - akv (This needs review.)
     */

    protected TagLibraryInfo(String prefix, String uri) {
	this.prefix = prefix;
	this.uri    = uri;
    }

    // TODO -- want a package private constructor with data spelled out?

    /**
     * @return the URI from the <%@ taglib directive for this library
     */
   
    public String getURI() {
        return uri;
    }

    /**
     * @return the prefix assigned to this taglib from the <%taglib directive
     */

    public String getPrefixString() {
	return prefix;
    }

    // ==== methods using the TLD data =======

    /**
     * @return the prefered short name for the library
     */
    public String getShortName() {
        return shortname;
    }

    /**
     * @return a reliable URN to a TLD like this
     */
    public String getReliableURN() {
        return urn;
    }

    /**
     * @return the info string for this tag lib
     */
   
    public String getInfoString() {
        return info;
    }

    /**
     * The required version.
     * TODO -- minimal?
     */
   
    public String getRequiredVersion() {
        return jspversion;
    }

    /**
     * @return the tags defined in this tag lib
     */
   
    public TagInfo[] getTags() {
        return tags;
    }

    /**
     * Get the TagInfo for a given tag name
     */

    public TagInfo getTag(String shortname) {
        TagInfo tags[] = getTags();

        if (tags == null || tags.length == 0) {
            System.err.println("No tags");
            return null;
        }

        for (int i=0; i < tags.length; i++) {
            if (tags[i].getTagName().equals(shortname)) {
                return tags[i];
            }
        }
        return null;
    }


    // Protected fields

    protected String        prefix;
    protected String        uri;

    protected TagInfo[]     tags;

    // Tag Library Data
    protected String tlibversion; // required
    protected String jspversion;  // optional
    protected String shortname;   // required
    protected String urn;         // required
    protected String info;        // optional
}
