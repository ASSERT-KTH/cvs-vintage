package org.tigris.scarab.services.user;

/* ================================================================
 * Copyright (c) 2001 Collab.Net.  All rights reserved.
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
import org.tigris.scarab.om.ScarabUser;
import java.util.List;

import org.tigris.scarab.om.ScarabModulePeer;

import org.apache.torque.om.ObjectKey;
import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.TurbineServices;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.commons.util.StringUtils;
import org.apache.turbine.RunData;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;

/**
 * This class has static methods for working with a ScarabUser object
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: UserManager.java,v 1.5 2002/03/02 02:33:01 jmcnally Exp $
 */
public abstract class UserManager
{
    /**
     * Retrieves an implementation of UserService, base on the settings in
     * ScarabResources.
     *
     * @return an implementation of UserService.
     */
    public static UserService getService()
    {
        return (UserService)TurbineServices.getInstance().
            getService(UserService.SERVICE_NAME);    
    }

    public static ScarabUser getInstance()
        throws Exception
    {
        return getService().getInstance();
    }

    public static ScarabUser getInstance(ObjectKey id)
        throws TorqueException
    {
        ScarabUser user = null;
        try
        {
            user = getService().getInstance(id);
        }
        catch (Exception e)
        {
            throw new TorqueException(e);
        }
        return user;
    }

    public static List getUsers(ObjectKey[] userIds)
        throws Exception
    {
        return getService().getUsers(userIds);
    }

    public static ScarabUser getInstance(String username, String domainName)
        throws Exception
    {
        return getService().getInstance(username, domainName);
    }

    public static List getUsers(String[] usernames, String domainName)
        throws Exception
    {
        return getService().getUsers(usernames, domainName);
    }
}
