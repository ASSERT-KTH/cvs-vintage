package org.tigris.scarab.om;

/* ================================================================
 * Copyright (c) 2000-2002 CollabNet.  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement: "This product includes
 * software developed by Collab.Net <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" or 
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
 * prior written permission of Collab.Net.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Collab.Net.
 */ 

import org.apache.torque.TorqueException;

/** 
 * This class is the home of where we store user preferences
 * right now the design is based on adding columns to the database
 * for each thing you want to store. Eventually, we will want
 * to come up with a real system for dealing with this.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: UserPreference.java,v 1.7 2003/04/04 02:51:47 jon Exp $
 */
public class UserPreference 
    extends org.tigris.scarab.om.BaseUserPreference
{
    /**
     * Gets a UserPreference object
     * @return new UserPreference object
     * @deprecated Use UserPreferenceManager.getInstance()
     */
    public static UserPreference getInstance()
        throws TorqueException
    {
        return UserPreferenceManager.getInstance();
    }

    /**
     * Gets a UserPrefernce object for a specific user
     * @return null if userid could not be found
     * @deprecated Use UserPreferenceManager.getInstance(Integer)
     */
    public static UserPreference getInstance(Integer userid)
        throws Exception
    {
        return UserPreferenceManager.getInstance(userid);
    }
    
    /**
     * Internally, this method will trim the String length
     * to 255 characters if it is greater than 255 because
     * the database column is onl 255 characters. This should
     * not have an adverse affect because the AcceptLanguage header
     * parser really only needs the first few characters.
     */
    public void setAcceptLanguage(String lang)
    {
        String newLang = (lang != null && lang.length() > 255) ? 
                         lang.substring(0,254) : lang;
        super.setAcceptLanguage(newLang);
    }
}
