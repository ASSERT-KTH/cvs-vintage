/*
 * ContentType.java
 * Created on 17. maj 2003, 00:39
 *
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is Eudora Mail Import Filter plugin for Columba.
 *
 * The Initial Developer of the Original Code is Karl Peder Olesen.
 * Portions created by Karl Peder Olesen are Copyright (C) 2003
 *
 * All Rights Reserved.
 * 
 */
package org.columba.mail;

import java.util.StringTokenizer;

/**
 * Class representing a Content-Type header with type, subType and parameters.
 * Main use is to parse a Content-Type header (without "Content-Type: ") to
 * get type, subType and boundary.
 *
 * @author  Karl Peder Olesen
 * @version 1.0
 */
class ContentType
{
    
    private String mType;
    private String mSubType;
    private String mParameters;
    private String mBoundary;   // the boundary parameter
    

    /** Creates new instance and sets content type from a string (header minus "Content-Type: " */
    public ContentType(String input)
    {
        
        String s = input.trim();
        mType = s.substring(0, s.indexOf('/'));
        mSubType = (new StringTokenizer(s.substring(s.indexOf('/') + 1))).nextToken();
        mParameters = s.substring(s.indexOf('/') + mSubType.length() + 1); // parameters ~ rest of string
        int pos = s.indexOf("boundary=");
        pos = pos + 9;
        if (s.indexOf(';', pos) == -1)
            mBoundary = s.substring(pos).trim();
        else
            mBoundary = s.substring(pos, s.indexOf(';', pos)).trim();
        if (mBoundary.startsWith("\""))
            mBoundary = mBoundary.substring(1, mBoundary.length()-1); // strip "'s
    }
    
    public String getType() { return mType; }
    
    public String getSubType() { return mSubType; }
    
    public String getBoundary() { return mBoundary; }
 
    /** outputs the Content-Type definition (minus "Content-Type: ") */
    public String toString()
    {
        return mType + "/" + mSubType + mParameters;
    }
}
