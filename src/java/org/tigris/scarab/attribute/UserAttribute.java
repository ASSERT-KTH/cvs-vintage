package org.tigris.scarab.attribute;

/* ================================================================
 * Copyright (c) 2000-2001 CollabNet.  All rights reserved.
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

// JDK Stuff
import java.util.List;

import org.apache.turbine.Log;

// Scarab Stuff
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.services.user.UserManager;

/**
 *
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @version $Revision: 1.14 $ $Date: 2001/11/01 00:31:24 $
 */
public class UserAttribute extends AttributeValue
{

    /** Gets the Value attribute of the Attribute object
     *
     * @return    The Value value
     */
    public String getUserName()
    {
        return getValue();
    }

    public void setUser(ScarabUser user)
        throws Exception
    {
        setValueOnly(user.getUserName());
        setUserIdOnly(user.getUserId());
    }

    public void setValue(String username)
  
    {
        // can't throw an exception, so just log it
        try
        {
            if ( username != null ) 
            {
                ScarabUser user = UserManager
                    .getInstance(username, getIssue().getIdDomain());
                setUserIdOnly(user.getUserId());
            }
            else
            {
                // any reason to set a username to null, once its already set?
                setUserIdOnly(null);
            }
            
            setValueOnly(username);
        }
        catch (Exception e)
        {
            Log.error(e);
        }
    }

    public void init() throws Exception
    {
    }

    public void setResources(Object resources)
    {
    }
    
    /** displays the attribute.
     * @return Object to display the property. May be a String containing HTML
     * @param data app data. may be needed to render control
     * differently in different circumstances.
     * Not sure about this though. It may be a better
     * idea to handle this on the UI level.
     */
    public Object loadResources() throws Exception
    {
        return null;
    }
}
